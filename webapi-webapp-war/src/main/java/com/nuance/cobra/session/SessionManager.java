package com.nuance.cobra.session;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.nuance.cobra.utils.BoundedExecutorService;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.WebAPIRateLimiter;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.plugin.v1.shareable.IResponseSender;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.websocket.CloseReason;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * This class acts as a single access point to fetch sessions
 */
public class SessionManager
{

    private static final Logger logger = LogManager.getLogger(SessionManager.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(SessionManager.class);

    private static HashMap<String,Object> sessionEncryptionStateMap = new HashMap<>();


    private static BoundedExecutorService boundedExecutorService = null;

    /**
     * The list of active sessions keyed by session identifiers
     */
    private final Map<String, Session> sessions;

    /**
     * Gauge for calculating current number of WebAPI sessions per tenant
     */
    private final Gauge sessionPerTenantGauge = Gauge.build()
                                                     .name("webapi_active_sessions")
                                                     .help("Current number of WebApi sessions")
                                                     .labelNames(MetricsConstants.TENANT_LABEL)
                                                     .register();

    /**
     * Counter for calculating number of WebAPI sessions per tenant
     */
    private final Counter sessionsPerTenantCounter = Counter.build()
                                                            .name("webapi_sessions_total")
                                                            .help("WebApi sessions counter")
                                                            .labelNames(MetricsConstants.TENANT_LABEL)
                                                            .register();
    /**
     * javax.websocket.Session <--> webapi session id
     * This implies that the websocket session/connection is the webapi session.
     */
    private final BiMap<String, String> wsSessionToWebapiSession = HashBiMap.create();
    /**
     * Rate limiter for concurrent sessions
     */
    private WebAPIRateLimiter sessionRateLimiter;

    private SessionCleanUpRunnable sessionCleanupRunnable = new SessionCleanUpRunnable(this);

    /**
     * The static instance of the SessionManager. Contained in a holder to support lazy-init without
     * requiring synchronization.
     */
    private static class SessionManagerHolder
    {
        // Must not be final to support testing
        private static SessionManager instance = new SessionManager();
    }

    private ScheduledFuture cleanupFuture;

    private static CombinedConfiguration composeSessionConfigWithStartSessionParams(Map<String, Object> startSessionParams, CombinedConfiguration tenantConfig)
    {
        logger.trace("SessionManager fromStartSessionParamsAndTenantConfig");

        populateUserId(startSessionParams, tenantConfig);

        return CobraConfigurationUtils.addStartSessionParamsToConfiguration(tenantConfig, startSessionParams);
    }

    private static Session createSession(String sessionId, CombinedConfiguration sessionConfig)
    {
        return new Session(sessionId, sessionConfig);
    }

    private static void populateUserId(Map<String, Object> requestParameters, Configuration tenantConfiguration)
    {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String uid = uuid;

        if (requestParameters.containsKey(WebApiConfig.Property.CLIENT_USER_ID.key))
        {
            uid = requestParameters.get(WebApiConfig.Property.CLIENT_USER_ID.key).toString();
            if (tenantConfiguration.getBoolean(WebApiConfig.Property.APPEND_SUFFIX_TO_USERID.key))
            {
                //Append a 4 character suffix for election hashing (NCS)
                uid += "-" + uuid.substring(0, 4);
            }
        }

        EsapiLogger.trace(esapiLogger, "Using userId = %s", uid);
        requestParameters.put(WebApiConfig.Property.CLIENT_USER_ID.key, uid);

        //We should never really use a tenant-set user id as we don't always want the same
        // uid for all of a tenant's sessions.
    }

    /**
     * A private constructor to conform with the Singleton pattern
     */
    private SessionManager() {
        ObservableMap<String, Session> observableSessions = FXCollections.observableMap(new ConcurrentHashMap<>());

        observableSessions.addListener((MapChangeListener<String, Session>) change -> {
            if (change.wasAdded() && !change.wasRemoved()) {
                sessionPerTenantGauge.labels(change.getValueAdded().getTenantKey()).inc();
            } else if (change.wasRemoved() && !change.wasAdded()) {
                sessionPerTenantGauge.labels(change.getValueRemoved().getTenantKey()).dec();
            }
        });
        sessions = observableSessions;
    }

    public static String createSessionId()
    {
        final UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public void init()
    {
        startCleanUp();

        int bound = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.BOUNDED_THREAD_POOL_SIZE.key);
        boundedExecutorService = new BoundedExecutorService(bound);
        sessionRateLimiter = new WebAPIRateLimiter(WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.GLOBAL_RATE_LIMIT_CONCURRENT_SESSIONS.key));
    }

    public void destroy()
    {
        stopCleanUp();
        closeAllSessions();
        boundedExecutorService.shutdownAndAwaitTermination();
    }

    private void closeAllSessions()
    {
        //In reality, it seems like all the websocket connections get closed before we get here.
        //But let's keep the code anyway, in case servlet behavior changes.

        logger.debug("closeAllSessions");
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "shutdown");
        for(Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Session> entry = it.next();
            Session session = entry.getValue();
            logger.debug("Closing session {}", session.getSessionId());
            IResponseSender apiConnectionCloser = (IResponseSender)(session.getProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER));
            apiConnectionCloser.closeConnection(closeReason);
        }
    }

    /**
     * Fetches the single static instance of the session manager
     *
     * @return The instance of the session manager
     */
    public static SessionManager getInstance()
    {
        return SessionManagerHolder.instance;
    }

    public static BoundedExecutorService getBoundedExecutorService()
    {
        return boundedExecutorService;
    }

    /**
     * Starts a session
     *
     * @return The newly created session
     */
    public Session startSession(Map<String, Object> requestParameters, CombinedConfiguration tenantConfig)
            throws CobraException
    {
        logger.trace("SessionManager startSession");

        final String sessionId = createSessionId();

        final String tenantKey = (String)requestParameters.get(WebApiConfig.Property.APPLICATION_KEY.key);

        if (tenantKey == null || tenantKey.isEmpty()){
            logger.debug("Tenant key is either null or empty");
            return null;
        }
        final CombinedConfiguration config = composeSessionConfigWithStartSessionParams(requestParameters, tenantConfig);

        final Session session = createSession(sessionId, config);

        putSession(session, tenantKey);

        logger.debug("{} is not null", session);

        return session;
    }

    public void putSession(Session session) throws CobraException
    {
        putSession(session, Session.getTenantKey(session));
    }

    private void putSession(Session session, String tenantKey) throws CobraException
    {
        if(!sessionRateLimiter.checkIfRateLimitExceeded(tenantKey, session.getSessionConfig()
                                                                         .getInt(
                                                                                 WebApiConfig.Property.TENANT_RATE_LIMIT_CONCURRENT_SESSIONS.key)))
        {
            sessions.put(session.getSessionId(), session);
            sessionsPerTenantCounter.labels(tenantKey)
                                    .inc();
        }
    }

    public void removeSession(String sessionId, boolean checkForTimeout)
    {
        logger.debug("SessionManager removeSession: {}", sessionId);
        Session session = getSession(sessionId, checkForTimeout);
        if(null != session)
        {
            session.setEndSessionTime(Calendar.getInstance());
            String tenantKey = Session.getTenantKey(session);
            if(null != tenantKey)
            {
                sessionRateLimiter.release(tenantKey);
            }
        }
        sessions.remove(sessionId);
    }

    //For NLPS
    public Session startSessionNlps(Map<String, Object> requestParameters, String sessionId, String companyName, String applicationName)
            throws InvalidConfigurationException, CobraException
    {
        logger.trace("SessionManager startSession (NLPS)");

        final String applicationKey = companyName + "_" + applicationName;

        final CombinedConfiguration tenantConfig = WebApiConfigManager.getCombinedConfigForTenant(applicationKey);

        final CombinedConfiguration config = composeSessionConfigWithStartSessionParams(requestParameters, tenantConfig);

        if (!config.getBoolean(WebApiConfig.Property.NLPS_ENABLED.key))
        {
            throw new InvalidConfigurationException("NLPS Not allowed"); //for this tenant
        }

        final Session session = createSession(sessionId, config);

        putSession(session, applicationKey);

        return session;
    }


    /**
     * Ends a session
     *
     * @param sessionId
     *         The identifier of the session to end
     *
     * @return true if the session was found and ended
     */
    public boolean endSession(String sessionId)
    {
        logger.trace("SessionManager endSession");
        if (!sessions.containsKey(sessionId))
        {
            logger.debug("sessions doesn't contain {}!", sessionId);
            return false;
        }

        removeSession(sessionId, false);

        String wsSessionId = Maps.synchronizedBiMap(wsSessionToWebapiSession).inverse().get(sessionId);
        if (wsSessionId != null) { Maps.synchronizedBiMap(wsSessionToWebapiSession).remove(wsSessionId); }

        return true;
    }

    /**
     * Returns the session associated with the identifier given
     *
     * @param sessionId
     *         The session identifier
     * @param checkForTimeout
     *         Should the time out check be performed or not for this fetch
     *
     * @return The session
     */
    public Session getSession(String sessionId, boolean checkForTimeout)
    {
        if (sessionId == null)
        {
            //sessionless
            return null;
        }

        Session session = sessions.get(sessionId);
        logger.debug("Looking for sessionID: {} found: {}", sessionId, session);

        if(session == null)
        {
            return null;
        }

        else if(checkForTimeout && session.isExpired())
        {
            return null; //maybe we want a custom exception here to let people know the session has expired.
        }

        session.updateLastActivityTime();
        return session;
    }

    /**
     * Private constructor to comply with the singleton pattern
     * When the instance is created, it creates its own "Expired session garbage collector"
     */
    private void startCleanUp()
    {
        logger.warn("SessionManager startCleanUp");
        int timeoutMs = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.SESSION_CLEAN_UP_SLEEP_TIME_MILLISECONDS.key);
        cleanupFuture = ScheduledTaskUtils.scheduleWithFixedDelay(sessionCleanupRunnable, timeoutMs, timeoutMs, TimeUnit.MILLISECONDS);
    }

    private void stopCleanUp()
    {
        logger.warn("SessionManager stopCleanUp");
        cleanupFuture.cancel(true);
    }

    // Method to support tests
    Map<String, Session> getSessionMap()
    {
        return sessions;
    }

    public void putWsSession(javax.websocket.Session wsSession, String webApiSessionId)
    {
        try
        {
            Maps.synchronizedBiMap(wsSessionToWebapiSession).put(wsSession.getId(), webApiSessionId);
        }
        catch(Exception e)
        {
            logger.error("Can't add key/value to wsSessionToWebapiSession - wsSession id:" + wsSession.getId() + " - sessionId: " + webApiSessionId + " - error: " + e.getMessage());
        }
    }

    public String getSessionId(javax.websocket.Session session)
    {
        if(session != null) {
            return Maps.synchronizedBiMap(wsSessionToWebapiSession).get(session.getId());
        }
        return null;
    }

    public Session getSession(javax.websocket.Session session)
    {
        String sessionId = getSessionId(session);
        if (sessionId == null) {
            return null;
        }

        return getSession(sessionId, false);
    }

    public Map<String, Object> getSessionEncryptionStateMap()
    {
        return sessionEncryptionStateMap;
    }

    public WebAPIRateLimiter getSessionRateLimiter(){
        return sessionRateLimiter;
    }
}
