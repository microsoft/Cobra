/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.session;

import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.nuance.plugin.ncs.utils.HashType;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.ncs.utils.StringObfuscator;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.PdxInterfacePlugin;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioContainerType;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioHandlingSpec;
import com.nuance.plugin.nmsp.v1.nmspgw.SameCodecMGWPolicy;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.server.NVCGWServer;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.SingleThreadExecutionHandlerPool;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_SECRET;
import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_TYPE;

/**
 * Data class containing session related info.
 *
 * @author epigeon
 */
public class Session {
    private final Optional<Integer> derogatoryMaxMessageSizeForCurrentNMAID = Optional.empty();

    public static final AudioHandlingSpec LEGACY_AUDIO_HANDLING_POLICY =
            new AudioHandlingSpec(null, null, AudioContainerType.unspecified);
    public static final String PING_INTERVAL_SECS_STRING = "Ping_IntervalSecs";
    public static final String PING_MISSED_RESPONSES_BEFORE_DISCONNECT = "Ping_MissedResponsesBeforeDisconnect";
    public static final String SESSION_TIMEOUT_SECS_PARAM_NAME = "sessionTimeoutSecs";
    public static final int SESSION_ID_LENGTH = 16;
    private static final String UNKNOWN = "UNKNOWN";
    private static final String CONFIGURATION_NMAID_KEY = "nmaidKey";
    private static final Logger logger = LogManager.getLogger();
    private final Channel clientChannel;
    private final Map<Long, ISessionListener> transcodeRequests;
    private final List<BCPMessage> messageQueue;
    private final EventExecutorGroup singleThreadExecutionHandler;
    private final List<Parameter> resourceParameters = Collections.synchronizedList(new ArrayList<>());
    private final long sessionCreationTime;
    private final SessionId sessionId;
    private WebAPIOrchestrator webAPIOrchestrator;
    private final HashMap<String, Object> bcpMessagePdxDict = new HashMap<>();
    private boolean clientDisconnected = true;
    private String disconnectionReason = "client disconnected";
    private STATE state;
    private short copVersion;
    private short bcpVersion = BCPMessage.BCP_MAXIMUM_VERSION;
    private IClientHandler client;
    private ISessionListener clientSessionListener;
    private IAudioListener clientAudioListener;
    private ICodecResource.Codec fromCodec; // From device codec
    private ICodecResource.Codec toCodec;   // To device codec
    private String nmaid = UNKNOWN;
    private String commandName = UNKNOWN;
    private String dictationLanguage = null;
    private String parent = null;
    private boolean connectToMds = false;
    private short deviceType;
    private String buildNumber = null;
    private String deviceModel;
    private String phoneModel;
    private int bufferSize;
    private String cleansedUserId = "";
    private String deviceId;
    private String subscriberId;
    private long pingRequestId = 0;
    private long roundTripTime = 0;
    private long lastPingRequest = 0;
    private float pingIntervalSecs = 0.0f; // 0 means deactivated
    private int missedPingResponsesBeforeDisconnect = 3;
    private int missedPings = 0;
    private PingRequestTimerTask pingRequestTimerTask = null;
    private SessionIdleTimerTask sessionIdleTimerTask = null;
    // We use the connect timeout until Confirm is received
    private long sessionTimeoutSecs = 10;

    public enum STATE {
        INIT, AUTHENTICATING, CHALLENGING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    private ScheduledFuture<?> pingRequestScheduledFuture = null;
    private ScheduledFuture<?> sessionIdleScheduledFuture = null;
    private ScheduledFuture<?> vapRecordTimerScheduledFuture = null;
    private long streamingStartTime;
    private int numTransactions = 0;
    private short currentTransactionId = -1;
    private AudioHandlingSpec outAudioHandlingSpec;
    private boolean oggContainer = false;
    private boolean pdxKeysAsciiEncoded;
    private String clientNmaidKey;
    public Session(Channel clientChannel, WebAPIOrchestrator webAPIOrchestrator) {
        this(clientChannel, LEGACY_AUDIO_HANDLING_POLICY, webAPIOrchestrator);
    }

    private Session(
            final Channel clientChannel,
            final AudioHandlingSpec outAudioHandlingSpec,
            final WebAPIOrchestrator webAPIOrchestrator) {
        this.sessionCreationTime = System.currentTimeMillis();
        this.clientChannel = clientChannel;
        this.outAudioHandlingSpec = outAudioHandlingSpec;
        this.webAPIOrchestrator = webAPIOrchestrator;

        this.sessionId = new SessionId(SESSION_ID_LENGTH);
        ThreadContext.put(ThreadContextRunnable.SESSION_ID, PdxInterfacePlugin.THREAD_CONTEXT_PREFIX + ":" + sessionId.asString());

        logger.trace("outAudioHandlingSpec='{}'", outAudioHandlingSpec);

        this.state = STATE.INIT;
        this.transcodeRequests = Collections.synchronizedMap(new HashMap<>());
        this.messageQueue = Collections.synchronizedList(new ArrayList<>());
        // Get a single thread EventExecutorGroup, this will be used for
        // every Netty Channel Pipeline in this session. Also every thread, e.g. timer thread,
        // started in the session must use this executor to start the thread.
        this.singleThreadExecutionHandler = SingleThreadExecutionHandlerPool.getHandler(logger);
        // EventExecutorGroup implements ExecutorService directly in Netty 4.x
        this.pingRequestTimerTask = new PingRequestTimerTask(this, singleThreadExecutionHandler);
        this.sessionIdleTimerTask = new SessionIdleTimerTask(this, singleThreadExecutionHandler);
    }

    private static String asSecurityCompliantString(final Exception e) {
        return e.getClass().getName();
    }

    public WebAPIOrchestrator getWebAPIOrchestrator() {
        return webAPIOrchestrator;
    }

    public void setWebAPIOrchestrator(WebAPIOrchestrator webAPIOrchestrator)
    {
        this.webAPIOrchestrator = webAPIOrchestrator;
    }

    public boolean hasOggContainer() {
        return oggContainer;
    }

    public void setOggContainer(Boolean ogg) {
        oggContainer = ogg;
    }

    public void addTranscodeRequest(long requestId, ISessionListener sessionListener) {
        transcodeRequests.put(requestId, sessionListener);
    }

    public void clientMessageReceived() {
        stopSessionIdleTimer();
        startSessionIdleTimer();
        if (pingRequestScheduledFuture != null) {
            pingRequestScheduledFuture.cancel(true);
            pingRequestScheduledFuture = NVCGWServer.gwTimer.scheduleAtFixedRate(pingRequestTimerTask, (long) (1000.0 * pingIntervalSecs), (long) (1000.0 * pingIntervalSecs), TimeUnit.MILLISECONDS);
        }
    }

    public void start() {
        setState(Session.STATE.CONNECTED);

        startPingRequestTimer();
    }

    public void end(int reason) {
        disconnectingClient();

        state = Session.STATE.DISCONNECTED;

        if (client != null) {
            client.close(this);
            client = null;
        }

        transcodeRequests.clear();
        resourceParameters.clear();

        webAPIOrchestrator = null;
        stopPingRequestTimer();
        stopSessionIdleTimer();
    }

    public void disconnectingClient() {
        if (state == Session.STATE.CONNECTED) {
            stopPingRequestTimer();
        } else if (state == Session.STATE.AUTHENTICATING ||
                state == Session.STATE.CHALLENGING) {
            // Do nothing
        }
        state = Session.STATE.DISCONNECTING;

        getWebAPIOrchestrator().onClientDisconnect();
    }

    /**
     * @return the bcpVersion
     */
    public short getBcpVersion() {
        return bcpVersion;
    }

    /**
     * @param bcpVersion the bcpVersion to set
     */
    public void setBcpVersion(short bcpVersion) {
        this.bcpVersion = bcpVersion;
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize the bufferSize to set
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Optional<Integer> getDerogatoryMaxMessageSizeForCurrentNMAID() {
        return derogatoryMaxMessageSizeForCurrentNMAID;
    }

    /**
     * @param buildNumber the buildNumber to set
     */
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the client
     */
    public IClientHandler getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(IClientHandler client) {
        this.client = client;
    }

    /**
     * @return the channel
     */
    public Channel getClientChannel() {
        return clientChannel;
    }

    /**
     * @return the clientSessionListener
     */
    public ISessionListener getClientSessionListener() {
        return clientSessionListener;
    }

    /**
     * @param clientSessionListener the clientSessionListener to set
     */
    public void setClientSessionListener(ISessionListener clientSessionListener) {
        this.clientSessionListener = clientSessionListener;
    }

    /**
     * @return the copVersion
     */
    public short getCopVersion() {
        return copVersion;
    }

    /**
     * @param copVersion the copVersion to set
     */
    public void setCopVersion(short copVersion) {
        this.copVersion = copVersion;
    }

    /**
     * @param deviceType the deviceType to set
     */
    public void setDeviceType(short deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * @return the fromCodec
     */
    public ICodecResource.Codec getFromCodec() {
        return fromCodec;
    }

    /**
     * @param fromCodec the fromCodec to set
     */
    public void setFromCodec(ICodecResource.Codec fromCodec) {
        this.fromCodec = fromCodec;
    }

    public boolean requiresGeneratedAudioTranscoding(final ICodecResource.Codec codecFromEngine, final ICodecResource.Codec codecToClient) {
        return codecFromEngine != codecToClient
                || outAudioHandlingSpec.getSameCodecMGWPolicy() == SameCodecMGWPolicy.throughGW;
    }

    /**
     * @return the messageQueue
     */
    public List<BCPMessage> getMessageQueue() {
        return messageQueue;
    }

    /**
     * @return the missedPingResponsesBeforeDisconnect
     */
    public int getMissedPingResponsesBeforeDisconnect() {
        return missedPingResponsesBeforeDisconnect;
    }

    /**
     * @param missedPingResponsesBeforeDisconnect the missedPingResponsesBeforeDisconnect to set
     */
    public void setMissedPingResponsesBeforeDisconnect(int missedPingResponsesBeforeDisconnect) {
        if (missedPingResponsesBeforeDisconnect > 1) {
            this.missedPingResponsesBeforeDisconnect = missedPingResponsesBeforeDisconnect;
        }
    }

    /**
     * @return the missedPings
     */
    public int getMissedPings() {
        return missedPings;
    }

    /**
     * @param missedPings the missedPings to set
     */
    public void setMissedPings(int missedPings) {
        this.missedPings = missedPings;
    }

    /**
     * @return the nmaid
     */
    public String getNmaid() {
        return nmaid;
    }

    /**
     * @param nmaid the k to set
     */
    public void setNmaid(String nmaid) {
        this.nmaid = nmaid;
    }

    /**
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * @param parent
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * @return the resourceParameters
     */
    public List<Parameter> getResourceParameters() {
        return resourceParameters;
    }

    public ISessionListener getTranscodeListener(long requestId) {
        return transcodeRequests.remove(requestId); // remove to make sure TranscodeComplete is only sent once
    }

    /**
     * @return the pingIntervalSecs
     */
    public float getPingIntervalSecs() {
        return pingIntervalSecs;
    }

    /**
     * @param pingIntervalSecs the pingIntervalSecs to set
     */
    public void setPingIntervalSecs(float pingIntervalSecs) {
        if (pingIntervalSecs > 0.0f) {
            this.pingIntervalSecs = pingIntervalSecs;
        }
    }

    /**
     * @return the pingRequestId
     */
    public long getPingRequestId() {
        return pingRequestId;
    }

    /**
     * @param pingRequestId the pingRequestId to set
     */
    public void setPingRequestId(long pingRequestId) {
        this.pingRequestId = pingRequestId;
    }

    /**
     * @return the pingRequestScheduledFuture
     */
    public ScheduledFuture<?> getPingRequestScheduledFuture() {
        return pingRequestScheduledFuture;
    }

    /**
     * @return the roundTripTime
     */
    public long getRoundTripTime() {
        return roundTripTime;
    }

    /**
     * @param roundTripTime the roundTripTime to set
     */
    public void setRoundTripTime(long roundTripTime) {
        this.roundTripTime = roundTripTime;
    }

    /**
     * @return the sessionId
     */
    public byte[] getSessionId() {
        return sessionId.asByteArray();
    }

    /**
     * @return the sessionTimeoutSecs
     */
    public long getSessionTimeoutSecs() {
        return sessionTimeoutSecs;
    }

    /**
     * @param sessionTimeoutSecs the sessionTimeoutSecs to set
     */
    public void setSessionTimeoutSecs(long sessionTimeoutSecs) {
        this.sessionTimeoutSecs = sessionTimeoutSecs;
        // if timer already started, restart it with the new value
        if (sessionIdleScheduledFuture != null) {
            sessionIdleScheduledFuture.cancel(true);
            startSessionIdleTimer();
        }
    }

    /**
     * @return the singleThreadExecutionHandler (EventExecutorGroup which implements ExecutorService)
     */
    public EventExecutorGroup getSingleThreadExecutionHandler() {
        return singleThreadExecutionHandler;
    }

    /**
     * Backward compatibility method. Returns the executor.
     * In Netty 4.x, EventExecutorGroup already implements ExecutorService.
     */
    public java.util.concurrent.ExecutorService getExecutor() {
        return singleThreadExecutionHandler;
    }

    /**
     * @return the state
     */
    public STATE getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(STATE state) {
        this.state = state;
    }

    /**
     * @return the toCodec
     */
    public ICodecResource.Codec getToCodec() {
        return toCodec;
    }

    /**
     * @param toCodec the toCodec to set
     */
    public void setToCodec(ICodecResource.Codec toCodec) {
        this.toCodec = toCodec;
    }

    public void setOutAudioHandlingSpec(AudioHandlingSpec outAudioHandlingSpec) {
        this.outAudioHandlingSpec = outAudioHandlingSpec;
    }

    public String getSessionIdString() {
        return sessionId.asString();
    }

    /**
     * @return the clientAudioListener
     */
    public IAudioListener getClientAudioListener() {
        return clientAudioListener;
    }

    /**
     * @param clientAudioListener the clientAudioListener to set
     */
    public void setClientAudioListener(IAudioListener clientAudioListener) {
        this.clientAudioListener = clientAudioListener;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return cleansedUserId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(final String userId) {
        if (userId != null) {
            this.cleansedUserId = cleanseId(userId, "userId");
        }
    }

    public String getObfuscatedUserId() throws IOException {
        if(getUserId() == null) {
            return getDeviceId();
        }
        return getObfuscatedString(getUserId());
    }

    public String getDeviceId() throws IOException {
        return getObfuscatedString(deviceId);
    }

    public String getSubscriberId() throws IOException {
        return getObfuscatedString(subscriberId);
    }

    public String getObfuscatedString(String value) throws IOException {
        var configs = PdxInterfaceConfig.getPluginConfMap(getWebAPIOrchestrator().getWebApiFeatures().getConfigs());
        var hashType = HashType.fromValue(configs.getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString()));
        var salt = configs.getString(HASH_SECRET.getKey(), HASH_SECRET.getDefaultValue().toString());
        return StringObfuscator.getObfuscatedString(hashType, salt, value);
    }

    /**
     * @param deviceId: the deviceId to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    private String cleanseId(final String id, final String logMessage) {
        try {
            return URLEncoder.encode(id, StandardCharsets.UTF_8.name()).replace("%", "");
        } catch (final UnsupportedEncodingException e) {
            logger.warn("Error encoding: {}, logMessage: {}", asSecurityCompliantString(e), logMessage);
            return id;
        }
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    private void startPingRequestTimer() {
        if (pingIntervalSecs > 0) {
            pingRequestScheduledFuture = NVCGWServer.gwTimer.scheduleAtFixedRate(pingRequestTimerTask, (long) (1000.0 * pingIntervalSecs), (long) (1000.0 * pingIntervalSecs), TimeUnit.MILLISECONDS);
        }
    }

    public void startSessionIdleTimer() {
        sessionIdleScheduledFuture = NVCGWServer.gwTimer.schedule(sessionIdleTimerTask, sessionTimeoutSecs, TimeUnit.SECONDS);
    }

    private void stopPingRequestTimer() {
        if (pingRequestScheduledFuture != null) {
            pingRequestScheduledFuture.cancel(true);
            pingRequestScheduledFuture = null;
        }
    }

    private void stopSessionIdleTimer() {
        if (sessionIdleScheduledFuture != null) {
            sessionIdleScheduledFuture.cancel(true);
            sessionIdleScheduledFuture = null;
        }
    }

    public void stopVapRecordTimer() {

        if (vapRecordTimerScheduledFuture != null) {
            vapRecordTimerScheduledFuture.cancel(true);
            vapRecordTimerScheduledFuture = null;
        }
    }

    /**
     * @return the lastPingRequest
     */
    public long getLastPingRequest() {
        return lastPingRequest;
    }

    /**
     * @param lastPingRequest the lastPingRequest to set
     */
    public void setLastPingRequest(long lastPingRequest) {
        this.lastPingRequest = lastPingRequest;
    }

    /**
     * @param clientDisconnected the clientDisconnected to set
     */
    public void setClientDisconnected(boolean clientDisconnected) {
        this.clientDisconnected = clientDisconnected;
    }

    public void setConnectToMds(boolean connectToMds) {
        this.connectToMds = connectToMds;
    }

    public String getDisconnectionReason() {
        return disconnectionReason;
    }

    public void setDisconnectionReason(String disconnectionReason) {
        this.disconnectionReason = disconnectionReason;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public long getStreamingStartTime() {
        return streamingStartTime;
    }

    public void setStreamingStartTime(long streamingStartTime) {
        this.streamingStartTime = streamingStartTime;
    }

    public String getDictationLanguage() {
        return dictationLanguage;
    }

    public void setDictationLanguage(String dictationLanguage) {
        this.dictationLanguage = dictationLanguage;
    }

    public Object getServerNmaidKey() throws IOException {
        CombinedConfiguration config = webAPIOrchestrator.getWebApiFeatures().getConfigs(getNmaid());

        Object nmaidKey = config.getProperty(CONFIGURATION_NMAID_KEY);

        if (nmaidKey instanceof String) {
            nmaidKey = new Object[]{nmaidKey};
        }

        return nmaidKey;
    }

    public String getClientNmaidKey() {
        return this.clientNmaidKey;
    }

    public void setClientNmaidKey(String clientNmaidKey) {
        this.clientNmaidKey = clientNmaidKey;
    }

    public int getNumTransactions() {
        return numTransactions;
    }

    public void setNumTransactions(int numTransactions) {
        this.numTransactions = numTransactions;
    }

    public long getSessionCreationTime() {
        return sessionCreationTime;
    }

    public short getCurrentTransactionId() {
        return currentTransactionId;
    }

    public void setCurrentTransactionId(short currentTransactionId) {
        this.currentTransactionId = currentTransactionId;
    }

    public Map<String, Object> getBcpMessagePdxDictCopy() {
        return Collections.unmodifiableMap(bcpMessagePdxDict);
    }

    public void putBcpMessagePdxDicts(Map<String, Object> pdxDict) {
        bcpMessagePdxDict.putAll(pdxDict);
    }

    public boolean isPdxKeysAsciiEncoded() {
        return pdxKeysAsciiEncoded;
    }

    public void setPdxKeysAsciiEncoded(boolean pdxKeysAsciiEncoded) {
        this.pdxKeysAsciiEncoded = pdxKeysAsciiEncoded;
    }
}
