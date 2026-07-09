package com.nuance.cobra.session;

import com.nuance.cobra.utils.JvmUtils;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.CloseReason;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class SessionCleanUpRunnable
        implements Runnable
{
    private static final Logger logger = LogManager.getLogger(SessionCleanUpRunnable.class);

    private final SessionManager sessionManager;

    private List<String> sessionsToCleanup = new LinkedList<>();

    SessionCleanUpRunnable(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    private int iterations = 0;

    /**
     * This will clean up expired sessions
     */
    @Override
    public void run()
    {
        logger.debug("Starting Session clean up!");

        ensureCleanupOfPreviouslyMarkedSessions();

        Map<String, Session> sessions = sessionManager.getSessionMap();
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            Session session = entry.getValue();
            if (session.isExpired() && session.getProperty(Session.SessionPropertyKeys.SESSIONLESS_KEY) != Boolean.TRUE)
            {
                logger.info("Session {} has expired. Closing the session!", session.getSessionId());

                sessionsToCleanup.add(entry.getKey()); //make sure this session goes away (on the next pass)

                try
                {
                    session.cancelActiveCommand("Session is expiring");

                    CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "idleTimeout");
                    //Closing the javax.websocket.Session will implicity do the end session request
                    IResponseSender apiConnectionCloser =
                            (IResponseSender) (session.getProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER));
                    apiConnectionCloser.closeConnection(closeReason);
                } catch (Exception e)
                {
                    logger.warn("Caught exception while ending session {} in cleanup: {}", session.getSessionId(), e.getMessage());
                }
            }
        }

        //Explicit clean up for WebAPIRateLimiter of concurrent sessions
        sessionManager.getSessionRateLimiter()
                      .garbageCollect(sessionManager.getSessionMap());

        outputSessionCounts();

        logger.debug("Leaving session clean up");
    }

    //The apiConnectionCloser.closeConnection() should cleanup the session.
    //But, in case that failed, we end the session here again, but on the next pass.
    private void ensureCleanupOfPreviouslyMarkedSessions()
    {
        for (String sessionToCleanup : sessionsToCleanup)
        {
            try
            {
                CombinedConfiguration toClear = sessionManager.getSession(sessionToCleanup, false).getSessionConfig();
                sessionManager.endSession(sessionToCleanup);
                CobraConfigurationUtils.clearCombinedConfiguration(toClear, sessionToCleanup);
                sessionManager.removeSession(sessionToCleanup, false);
            } catch (Exception e)
            {
                //Do nothing. We will get exceptions most of the time because sessionManager.getSession() gives an NPE
                // when the session was already cleaned up
            }
        }
        sessionsToCleanup.clear();
    }

    private void outputSessionCounts()
    {
        int iterationsBeforeOutput = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.SESSION_CLEAN_UP_ITERATIONS_BEFORE_OUTPUT_STATS.key);
        if (++iterations < iterationsBeforeOutput)
        {
            return;
        }

        iterations = 0;

        //Output JVM stats
        JvmUtils.outputJvmMemory(logger);

        //Output number of sessions
        logger.warn("Number of sessions in SessionManager: {}", sessionManager.getSessionMap().size());

        Map<String, Session> sessions = sessionManager.getSessionMap();
        for (Map.Entry<String, Session> entry : sessions.entrySet())
        {
            Session session = entry.getValue();

            int olderThanMinutes = 60;
            Calendar now = Calendar.getInstance();
            Calendar startSessionTimePlusTimeout = (Calendar) session.getStartSessionTime().clone();
            startSessionTimePlusTimeout.add(Calendar.MINUTE, olderThanMinutes);
            if(now.after(startSessionTimePlusTimeout))
            {
                logger.warn("Session {}: started at \"{}\", with last activity \"{}\" is in session map and is older than {} minutes.",
                        session.getSessionId(), session.getStartSessionTimeString(),
                        session.getLastActivityTimeString(), olderThanMinutes);
            }
        }
    }
}
