package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxCommand;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxObject;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXSequence;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.executor.Executor;
import com.nuance.plugin.nmsp.v1.webapi.executor.*;
import com.nuance.plugin.nmsp.v1.webapi.streaming.StreamingService;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import joptsimple.internal.Strings;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

public class WebAPIOrchestrator {
    private static final Logger logger = ESAPI.getLogger(WebAPIOrchestrator.class);

    private static final String RECEIVER_COMMANDS = "RECEIVER_PDX_COMMANDS";
    public static final String PACKAGES = "packages";
    public static final String REQUEST_INFO = "REQUEST_INFO";
    public static final String COMMAND = "command";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("plugin-nmsp-streamer-%s").build();
    public static final ExecutorService executor = Executors.newCachedThreadPool(threadFactory); //this thread pool will resize according to load.

    private IWebApiEndpoint webApiEndpoint;

    private WebSocketHttpSession webapiSocketSession = null;
    private final IWebApiFeatures webApiFeatures;
    private Executor activeWebApiExecutor = null;

    private String webAPISessionID = "";
    private final WebAPIEventListener responseEvents = new WebAPIEventListener();
    private BlockingQueue<StreamingService.AudioPacket> audioBufferQueue = new LinkedBlockingDeque<>();

    private final PDXDictionary audioInfo = new PDXDictionary();
    PDXDictionary audioPacketInfo = null;
    protected HashMap<String, Object> queryInfo = new HashMap<>();
    short transactionID = -1;

    public void initWebsocketSession() {
        webapiSocketSession = new WebSocketHttpSession(new FakeHttpSession());
    }

    public void terminateWebsocketSession()
    {
        try
        {
            if (webApiEndpoint != null)
            {
                webApiEndpoint.onClose(webapiSocketSession, null);
                webApiEndpoint = null;
            }
            if (webapiSocketSession != null)
            {
                webapiSocketSession.close();
            }
        }
        catch (IOException e)
        {
            logger.error(Logger.EVENT_UNSPECIFIED, "Fail to close mock websockets session", e);
        }
        webapiSocketSession = null;
    }

    public short getTransactionID() {
        return transactionID;
    }

    public IWebApiEndpoint getWebApiEndpoint() {
        return webApiEndpoint;
    }

    public CombinedConfiguration getSessionConfigs() throws PdxException {
        if (isWebapiSessionAlive()) {
            return getActiveSession().getSessionConfig();
        } else {
            throw new PdxException("No active webapi session found");
        }
    }

    public ISession getActiveSession() {
        return webApiEndpoint.getWebapiSession(getWebAPISessionID());
    }

    public WebAPIOrchestrator(IWebApiFeatures webApiFeatures) throws IOException {
        this.webApiFeatures = webApiFeatures;

        webApiEndpoint = webApiFeatures.getWebApiEndpoint(responseEvents);
    }

    public String getWebAPISessionID() {
        return webAPISessionID;
    }

    public boolean isWebapiSessionAlive() {
        return (!Strings.isNullOrEmpty(getWebAPISessionID()) && getActiveSession() != null);
    }

    public void setWebAPISessionID(String webAPISessionID) {
        this.webAPISessionID = webAPISessionID;
    }

    public WebSocketHttpSession getWebapiSocketSession() {
        return webapiSocketSession;
    }

    public IWebApiFeatures getWebApiFeatures() {
        return webApiFeatures;
    }

    public PDXDictionary getAudioInfo() {
        return audioInfo;
    }

    public void sendToWebapi(IWebapiOrchestratorEvents responseListener, String message, boolean async) throws IOException {
        responseEvents.setListener(responseListener);

        if (async) {
            executor.submit(
                    () -> {
                        try {
                            webApiEndpoint.onMessage(getWebapiSocketSession(), message);
                        } catch (IOException e) {
                            logger.warning(Logger.EVENT_UNSPECIFIED, e.getMessage(), e);
                        }
                    }
            );
        } else {
            webApiEndpoint.onMessage(getWebapiSocketSession(), message);
        }
    }

    public void streamToWebApi(byte[] buffer, boolean async) throws IOException {
        if (async) {
            executor.submit(
                    () -> {
                        try {
                            webApiEndpoint.onBinary(getWebapiSocketSession(), buffer);
                        } catch (IOException e) {
                            logger.warning(Logger.EVENT_UNSPECIFIED, e.getMessage(), e);
                        }
                    }
            );
        } else {
            webApiEndpoint.onBinary(getWebapiSocketSession(), buffer);
        }
    }

    public void setAudioPacketInfo(PDXDictionary audioPacketInfo) {
        audioPacketInfo.addUTF8String("time", LocalDateTime.now().format(formatter));
        if (this.audioPacketInfo == null) {
            audioInfo.getSequence(PACKAGES).addDictionary(audioPacketInfo);
        }
        this.audioPacketInfo = audioPacketInfo;
    }

    public void audioBytes(byte[] audio) {
        if (getAudioBufferQueue() != null) {
            getAudioBufferQueue().add(new StreamingService.AudioPacket(StreamingService.AudioPacket.MessageType.DATA, audio));
        }
    }

    public void onRecordEnd(long audioId) {
        setAudioId(audioId);
        audioInfo.addUTF8String("end_time", LocalDateTime.now().format(formatter));

        if (audioPacketInfo != null) {
            audioInfo.getSequence(PACKAGES).addDictionary(audioPacketInfo);
        }

        if (getAudioBufferQueue() != null) {
            getAudioBufferQueue().add(new StreamingService.AudioPacket(StreamingService.AudioPacket.MessageType.MESSAGE, "audio_end".getBytes()));
        }
    }

    public void doRecordBegin(long audioId) {
        setAudioId(audioId);
        audioInfo.addUTF8String("start_time", LocalDateTime.now().format(formatter));
        audioInfo.addUTF8String("nss_server", "webapi_nmsp_plugin:0");
        audioInfo.addSequence(PACKAGES, new PDXSequence());
        getAudioBufferQueue().clear();
    }

    public long getAudioId() {
        return audioInfo.getInteger("audio_id");
    }

    public void setAudioId(long audioId) {
        audioInfo.addInteger("audio_id", (int)audioId);
    }

    public BlockingQueue<StreamingService.AudioPacket> getAudioBufferQueue() {
        return audioBufferQueue;
    }

    public void onBCPData(final Session nmspSession, BCPDataMessage bcpDataMessage) {
        try {
            PdxObject data = new PdxObject(bcpDataMessage);
            String name = (String) data.getDict().get("name");
            queryInfo.putAll(data.getDict());
            if (name != null) {
                queryInfo.put(name, data.getDict());
            }

            ((LinkedList<PdxCommand>)queryInfo.get(RECEIVER_COMMANDS)).add(data.getCommand());

            handleNextStage(nmspSession);
        } catch (Exception t) {
            logger.error(Logger.EVENT_UNSPECIFIED, "Fail to execute NMSP request.", t);
            sendError(nmspSession, ResponseUtils.buildError(nmspSession, t.getMessage(), ResponseCodes.CompletionCause.OPERATION_FAILED));
        }
    }

    public void onBCPBegin(final Session nmspSession, BCPBeginMessage message) {
        if (activeWebApiExecutor != null && !activeWebApiExecutor.isTerminated()) {
            activeWebApiExecutor.cancelCommand();
        }

        queryInfo = message.getQueryBeginInfo();
        queryInfo.put(RECEIVER_COMMANDS, new LinkedList<PdxCommand>());
        transactionID = message.getTransactionId();

        execute(nmspSession, new StartSessionExecutor(nmspSession, queryInfo, getWebAPISessionID()));
    }

    private void execute(final Session nmspSession, Executor command){
        try {
            if (activeWebApiExecutor instanceof StartSessionExecutor && command instanceof StartSessionExecutor) {
                //don't double start session.
                return;
            }

            activeWebApiExecutor = command;
            activeWebApiExecutor.execute();
        } catch (Exception t) {
            logger.error(Logger.EVENT_UNSPECIFIED, "Fail to execute NMSP request.", t);
            sendError(nmspSession, ResponseUtils.buildError(nmspSession, t.getMessage(), ResponseCodes.CompletionCause.OPERATION_FAILED));
        }
    }

    public void onClientDisconnect() {
        if (activeWebApiExecutor != null) {
            activeWebApiExecutor.onClientDisconnected();
        }

        terminateWebsocketSession();
    }

    public void commandDone(final Session nmspSession, boolean isFinal) {
        activeWebApiExecutor = null;

        if (!isFinal) {
            handleNextStage(nmspSession);
        }
    }

    private void handleNextStage(final Session nmspSession) {
        if (activeWebApiExecutor != null) {
            return;
        }

        if (!Strings.isNullOrEmpty(getWebAPISessionID())) {
            Executor newCommand = buildCommand(nmspSession);
            if (newCommand != null) {
                logger.debug(Logger.EVENT_UNSPECIFIED, "====== executing webapi command with " + queryInfo);
                execute(nmspSession, newCommand);
            }
        } else {
            sendError(nmspSession, ResponseUtils.buildError(nmspSession,"Running command without a valid session.", ResponseCodes.CompletionCause.INVALID_REQUEST));
        }
    }

    protected Executor buildCommand(final Session nmspSession) {
        boolean gotQueryEnd = ((LinkedList<PdxCommand>)queryInfo.get(RECEIVER_COMMANDS)).contains(PdxCommand.QueryEnd);
        switch ((String) queryInfo.get(COMMAND)) {
            case "NVC_ASR_CMD":
            case "NMDP_ASR_CMD":
                if (queryInfo.containsKey(REQUEST_INFO) || gotQueryEnd) {
                    return new SpeechRecognitionExecutor(nmspSession, queryInfo, getWebAPISessionID());
                }
                break;

            case "NVC_TTS_CMD":
            case "NMDP_TTS_CMD":
                if (queryInfo.containsKey("TEXT_TO_READ") || gotQueryEnd) {
                    return new SpeechSynthesisExecutor(nmspSession, queryInfo, getWebAPISessionID());
                }
                break;

            case "DRAGON_NLU_ASR_CMD":
            case "NDSP_ASR_APP_CMD":
                //NLU + ASR
                if (queryInfo.containsKey(REQUEST_INFO) || gotQueryEnd) {
                    return new InterpretationWithSpeechRecognitionExecutor(nmspSession, queryInfo, getWebAPISessionID());
                }
                break;

            case "DRAGON_NLU_APPSERVER_CMD":
            case "NDSP_APP_CMD":
                //NLU
                if (queryInfo.containsKey(REQUEST_INFO) || gotQueryEnd) {
                    return new InterpretationExecutor(nmspSession, queryInfo, getWebAPISessionID());
                }
                break;

            default:
                logger.warning(Logger.EVENT_UNSPECIFIED, "Command not implemented (" + queryInfo.get(COMMAND) + ")");
                sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Command " + queryInfo.get(COMMAND) + " not implemented.", ResponseCodes.CompletionCause.INVALID_REQUEST));
        }

        return null;
    }

    public void sendError(final Session nmspSession, byte[] responseResult) {
        try {
            if(webApiEndpoint != null)
            {
                webApiEndpoint.onClose(webapiSocketSession, null);
            }
            activeWebApiExecutor = null;
            webAPISessionID = null;
        } catch (IOException e) {
            logger.debug(Logger.EVENT_UNSPECIFIED, "failed to close session with webapi");
        }
        nmspSession.getClient().sendData(nmspSession, getTransactionID(), nmspSession.getLastPingRequest(),
                    responseResult);
    }
}
