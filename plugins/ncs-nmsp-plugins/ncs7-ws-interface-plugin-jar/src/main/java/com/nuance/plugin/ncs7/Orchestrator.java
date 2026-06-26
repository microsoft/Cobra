package com.nuance.plugin.ncs7;

import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import com.nuance.plugin.ncs7.translators.*;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Orchestrator {
    private static final Logger logger = LogManager.getLogger();
    AbstractCommandTranslator command;

    public IWebApiFeatures getWebApiFeatures() {
        return webApiFeatures;
    }

    public void setWebApiFeatures(IWebApiFeatures webApiFeatures) {
        this.webApiFeatures = webApiFeatures;
    }

    public enum ProtocolVersion {
        v1, v2;
    }

    final Session webSocketSession;
    IWebApiEndpoint webApi;
    private String webapiSessionID = null;
    Integer audioId;
    Long startTime;
    Long endTime;
    String codec;
    List<Object> productImprovements;
    String codecId;
    String sessionId;
    private IWebApiFeatures webApiFeatures;
    String appId;

    public enum MessageType{
        DATA, MESSAGE, TERMINATE, NOTIFY, SUBSCRIBER
    }

    // client input buffers
    public final BlockingQueue<Map.Entry<MessageType, Object>> clientMessageBufferQueue = new LinkedBlockingDeque<>();
    public final BlockingQueue<Map.Entry<MessageType, Object>> clientAudioBytesBufferQueue = new LinkedBlockingDeque<>();

    //webapi return buffers
    public final BlockingQueue<Map.Entry<MessageType, Object>> webapiMessageBufferQueue = new LinkedBlockingDeque<>();

    Map<String, JSONObject> dynamicVocabulary = new HashMap<>();

    public Map<String, JSONObject> getDynamicVocabulary() {
        return dynamicVocabulary;
    }

    public void addDynamicVocabulary(String vocabularyKey, JSONObject dynamicVocabulary) {
            this.dynamicVocabulary.put(vocabularyKey, dynamicVocabulary);
    }

    public void removeDynamicVocabulary(String dynamicVocabulary) {
        this.dynamicVocabulary.remove(dynamicVocabulary);
    }

    public void clearDynamicVocabulary() {
        this.dynamicVocabulary.clear();
    }

    public Session getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebApi(IWebApiEndpoint webApi) {
        this.webApi = webApi;
    }

    public IWebApiEndpoint getWebApi() {
        return webApi;
    }

    public Integer getAudioId() {
        return audioId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setAudioId(Integer audioId) {
        this.audioId = audioId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getWebapiSessionID() {
        return webapiSessionID;
    }

    public void setWebapiSessionID(String webapiSessionID) {
        this.webapiSessionID = webapiSessionID;
    }

    public ProtocolVersion getProtocolVersion() {
        if (getWebSocketSession().getPathParameters().get("pathParam") != null) {
            return ProtocolVersion.valueOf(getWebSocketSession().getPathParameters().get("pathParam").toLowerCase());
        } else {
            return ProtocolVersion.v1;
        }
    }

    public String getCodec() {
        if (codec == null) {
            try {
                String codecConfigKey;
                if (command instanceof SpeechSynthesisTranslator) {
                    codecConfigKey = "speechSynthesisCodec";
                } else if (command instanceof SpeechRecognitionTranslator) {
                    codecConfigKey = "speechRecognitionCodec";
                } else {
                    return null;
                }
                String codecCobraName = webApi.getActiveSession().getSessionConfig().getString(codecConfigKey);
                return ICodecResource.Codec.fromWebApiString(codecCobraName).getMimeType();
            } catch (Exception e) {
                logger.error("Error while getting Orchestrator codec.", e);
            }
        }
        return codec;
    }

    public void setProductImprovements(List<Object> productImprovements) {
        if (productImprovements != null) {
            this.productImprovements = productImprovements;
        }
    }

    public void setCodec(String codec) {
        if (codec != null) {
            this.codec = codec;
        }
    }

    public Integer getCodecId() {
        if (codecId == null) {
            try {
                return (int)ICodecResource.Codec.fromMimeTypeString(getCodec()).getNmaidCode();
            } catch (NumberFormatException e) {
                logger.error("Error while getting Orchestrator codec ID. Codec ID is not a valid integer.", e);
            } catch (Exception e) {
                logger.error("Error while getting Orchestrator codec ID.", e);
            }
        }
        try {
            return Integer.parseInt(codecId);
        } catch (NumberFormatException e) {
            logger.error("Error while getting Orchestrator codec ID. Codec ID is not a valid integer.", e);
        }
        return null;
    }

    public void setCodecId(String codecId) {
        this.codecId = codecId;
    }

    public Orchestrator(Session webSocketSession) {
        this.webSocketSession = webSocketSession;
        
        createConsumer(clientMessageBufferQueue); //start a consumer for control messages
    }

    private void createConsumer(BlockingQueue<Map.Entry<MessageType, Object>> queue) {
        AbstractCommandTranslator.executor.submit(
                () -> {
                    boolean terminate = false;
                    while (!terminate) {
                        try {
                            Map.Entry<MessageType, Object> entry = queue.poll(AbstractCommandTranslator.THREAD_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            if (entry == null) {
                                //timeout, cleanup and terminate
                                entry = Map.entry(MessageType.NOTIFY, "");
                            }

                            logger.debug("====== Orchestrator Handling event: {}", entry);

                            Map<String, Object> message = null;

                            switch (entry.getKey()) {
                                case MESSAGE:
                                    message = (Map<String, Object>)entry.getValue();
                                    if (message != null) {
                                        logger.debug(">====== Orchestrator parsing request: {}", message);
                                        doQuery(message);
                                    }
                                    break;
                                case TERMINATE:
                                    terminate = true;
                                    break;
                                case NOTIFY:
                                    Thread.yield();
                                    break;
                                case SUBSCRIBER:
                                    message = (Map<String, Object>)entry.getValue();
                                    if(message != null && command instanceof StartSessionTranslator) {
                                        NcsRequest ncsRequest = NcsRequest.fromMap(message);
                                        command.setDeviceModel(ncsRequest);
                                        command.setSubscriberId(ncsRequest);
                                        command.execute();
                                    }
                                    break;
                                default:
                                    //do noting
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            terminate = true;
                        } catch (IOException e) {
                            logger.warn("NCS plugin IO exception.", e);
                        } catch (IllegalArgumentException e) {
                            sendDisconnect(-1, e.getMessage());
                        } catch (Exception t) {
                            logger.error("Something went wrong in the orchestrator.", t);
                            sendDisconnect(400, "transaction error.");
                        }
                    }

                    logger.debug("====== Orchestrator terminated. No more requests will be handled on this websocket");
                }
        );
    }

    public void sendDisconnect(Integer code, String message) {
        try {
            AbstractCommandTranslator.sendDisconnect( this, code, message);
        } catch (IOException e) {
            logger.error("failed to send response to client.", e);
        }
    }

    private void doQuery(Map<String, Object> ncsMessage) throws IOException {
        String message = (String)ncsMessage.getOrDefault("message", "noop");
        if (message.startsWith("query")) {
            handleQuery(NcsRequest.fromMap(ncsMessage));
            return;
        }

        if (message.startsWith("connect")) {
            handleConnect(NcsRequest.fromMap(ncsMessage));
        }

        if (message.startsWith("disconnect")) {
            handleDisconnect(NcsResponse.fromMap(ncsMessage));
        }
    }

    private void handleConnect(NcsRequest ncsRequest) throws IOException {
        setCodec(ncsRequest.getCodecName());
        setProductImprovements(ncsRequest.productImprovements);
        command = new StartSessionTranslator(this, ncsRequest);
        // execute will be called only when the subscriber Id comes in with the query_begin
    }

    private void handleDisconnect(NcsResponse ncsResponse) throws IOException {
        command = new EndSessionTranslator(this, ncsResponse);
        command.execute();
        cleanup();
    }

    private void handleQuery(NcsRequest ncsRequest) throws IOException {
        setCodec(ncsRequest.getCodecName());
        switch (ncsRequest.command) {
            case SpeechRecognitionTranslator.NCS_NAME: //Speech Recognition
                command = new SpeechRecognitionTranslator(this, ncsRequest);
                command.execute();
                break;

            case SpeechSynthesisTranslator.NCS_NAME: //Speech Synthesis
                command = new SpeechSynthesisTranslator(this, ncsRequest);
                command.execute();
                break;

            case InterpretationWithSpeechRecognitionTranslator.NCS_NAME: //Speech Recognition with NLU
                command = new InterpretationWithSpeechRecognitionTranslator(this, ncsRequest);
                command.execute();
                break;

            case InterpretationTranslator.NCS_NAME: //Text NLU
                command = new InterpretationTranslator(this, ncsRequest);
                command.execute();
                break;

            case UploadDynamicVocabularyTranslator.NCS_NAME: //Upload User Data
            case UploadDynamicVocabularyTranslator.INCREMENTAL_NCS_NAME: //Incremental Data Upload
                command = new UploadDynamicVocabularyTranslator(this, ncsRequest);
                command.execute();
                break;

            case DeleteDynamicVocabularyTranslator.NCS_NAME: //Delete All User Data
                command = new DeleteDynamicVocabularyTranslator(this, ncsRequest);
                command.execute();
                break;

            case UserResetTranslator.NCS_NAME: // Delete All User Data
                command = new UserResetTranslator(this, ncsRequest);
                command.execute();
                break;

            case SessionDeleteTranslator.NCS_NAME: // Delete session data
                command = new SessionDeleteTranslator(this, ncsRequest);
                command.execute();
                break;

            default:
                logger.warn("Command not implemented ({})", ncsRequest.command);
                sendDisconnect(-1, String.format("command '%s' is not supported", ncsRequest.command));
        }
    }

    public void cleanup() {
        if (command != null) {
            command.commandCleanup();
        }

        // client input buffers
        clientMessageBufferQueue.clear();
    }

    public void terminate() {
        boolean result = clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, "Orchestrator.terminate"));
        if(!result){
            logger.error("Failed to add map TERMINATE: Orchestrator.terminate to clientMessageBufferQueue");
        }
    }

    public void notify(String message) {
        boolean result = clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.NOTIFY, message));
        if(!result){
            logger.error("Failed to add NOTIFY to clientMessageBufferQueue");
        }
    }

    public boolean areModelImprovementsDefined(String featureName) {
        return productImprovements != null;
    }

    public boolean areModelImprovementsEnabled(String featureName) {
        if(productImprovements == null) {
            return false;
        }
        for(Object productImprovement : productImprovements) {
            if(productImprovement instanceof Map) {
                Map<String, Object> productImprovementJson = (Map<String, Object>) productImprovement;
                String feature = (String)productImprovementJson.getOrDefault("feature", "");
                if(feature.equalsIgnoreCase(featureName)) {
                    return (boolean)productImprovementJson.getOrDefault("enable", true);
                }
            }
        }
        return true;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }
}
