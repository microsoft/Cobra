package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import com.nuance.plugin.ncs7.streaming.IStreamingEndpoint;
import com.nuance.plugin.ncs7.streaming.StreamingService;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpeechRecognitionTranslator extends AbstractCommandTranslator implements IStreamingEndpoint {
    private static final Logger logger = LogManager.getLogger();
    public static final String WEBAPI_NAME = "speechRecognition";
    public static final String NCS_NAME = "NMDP_ASR_CMD";
    public static final String WEBAPI_SESSION_PLUGIN_RECOGNITION_PARAMETERS = "ncs7-ws-plugin-recognition-parameters";

    private boolean wordStreaming = false;

    public SpeechRecognitionTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
    }

    @Override
    public void sendBinary(byte[] buffer) {
        logger.debug("======> Stream to webapi: {}", buffer.length);
        try {
            orchestrator.getWebApi().onBinary(orchestrator.getWebSocketSession(), buffer);
        } catch (IOException e) {
            logger.warn("SpeechReco streaming error", e);
        }
    }

    @Override
    public boolean isOpen() {
        return (orchestrator.getWebSocketSession().isOpen() && orchestrator.getWebApi().getActiveSession() != null);
    }

    @Override
    public void onStreamingDone() {
        JSONObject directive = new JSONObject();
        directive.put("name", "stop");

        JSONObject command = new JSONObject();
        command.put("name", WEBAPI_NAME);
        command.put("id", commandID);

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "directive");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("command", command);
        webApiRequest.put("directive", directive);

        try {
            sendToWebapi(orchestrator, webApiRequest.toString(), false);
        } catch (IOException e) {
            logger.debug("Failed to send STOP directive to webapi.", e);
        }
    }

    @Override
    public void execute() throws IOException {
        doRequest(ncsRequest);
        streamer = new StreamingService(orchestrator.clientAudioBytesBufferQueue, this, "speechreco");
        waitForResponse();
        streamer.terminate();
    }

    protected void doRequest(NcsRequest ncsRequest) throws IOException {
        setDeviceCodecAsClientData(ncsRequest, orchestrator);

        wordStreaming = ncsRequest.getResultDelivery();

        CombinedConfiguration webApiSessionConfig = orchestrator.getWebApi().getActiveSession().getSessionConfig();
        webApiSessionConfig.removeConfiguration(WEBAPI_SESSION_PLUGIN_RECOGNITION_PARAMETERS);
        final var modelImprovementsDefined = orchestrator.areModelImprovementsDefined("model-improvements");
        if (modelImprovementsDefined && !orchestrator.areModelImprovementsEnabled("model-improvements")) {
            var pluginRecognitionParams = new WebApiJSONConfiguration();
            pluginRecognitionParams.addProperty("kryptonSuppressCallRecording", true);
            pluginRecognitionParams.addProperty("kryptonDiscardSpeakerAdaptation", true);
            pluginRecognitionParams.addProperty("nluAasGrpcInputLoggingMode", "SUPPRESSED");
            webApiSessionConfig.addConfiguration(pluginRecognitionParams, WEBAPI_SESSION_PLUGIN_RECOGNITION_PARAMETERS);
        }

        JSONObject command = WebApiCommandsBuilder.buildSpeechRecoCommand(orchestrator, ncsRequest);
        command.put("id", getCommandID(ncsRequest));

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("command", command);

        orchestrator.setAudioId(ncsRequest.audio_id);
        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    public static void setDeviceCodecAsClientData(NcsRequest ncsRequest, Orchestrator orchestrator) {
        String clientDetailsKey = "clientDetails";
        CombinedConfiguration sessionConfig = orchestrator.getWebApi().getActiveSession().getSessionConfig();
        Integer deviceCodec = ncsRequest.getCodecId();
        if(deviceCodec != null) {
            Map<String, Object> oldDetails = getOldDetails(clientDetailsKey, sessionConfig);
            Map<String, Object> newDetailsMap = new HashMap<>();
            Map<String, Object> newClientDetailsMap = oldDetails.size() > 0 ? oldDetails : new HashMap<>();
            newClientDetailsMap.put("deviceCodec", deviceCodec.toString());
            newDetailsMap.put(clientDetailsKey, newClientDetailsMap);
            WebApiJSONConfiguration newDetails = new WebApiJSONConfiguration();
            newDetails.load(newDetailsMap);
            sessionConfig.addConfiguration(newDetails, "clientDetailsTemp");
        }
    }

    private static Map<String, Object> getOldDetails(String clientDetailsKey, CombinedConfiguration sessionConfig) {
        Map<String, Object> oldDetails = new HashMap<>();

        try {
            oldDetails = new WebApiJSONConfiguration(sessionConfig.configurationAt(clientDetailsKey)).constructMap();
        }
        catch (ConfigurationRuntimeException e) {
            logger.trace("Failed to read previous clientDetails before injecting deviceCodec {}", e.getMessage());
        }
        return oldDetails;
    }

    public static void removeDeviceCodecAsClientData(Orchestrator orchestrator) {
        CombinedConfiguration sessionConfig = orchestrator.getWebApi().getActiveSession().getSessionConfig();
        sessionConfig.removeConfiguration("clientDetailsTemp");
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isEvent("endOfSpeech")) {
            streamer.terminate(true);
            return new ExecutionResult(ExecutionStatus.SUCCESS, false);
        }

        if (webApiResponse.isFinal()) {
            removeDeviceCodecAsClientData(orchestrator);

            NcsResponse ncsResponse = NCSResponseBuilder.asrFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME, ncsRequest);
            if (ncsResponse != null) {
                respondToClient(orchestrator, NCSResponseBuilder.asrFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME, ncsRequest).toJson());
            }
            sendQueryEnd(orchestrator, webApiResponse);

            return doFinal(orchestrator, webApiResponse);
        } else {

            if (wordStreaming && webApiResponse.isSuccess())
            {
                NcsResponse ncsResponse = NCSResponseBuilder.asrFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME, ncsRequest);

                if (ncsResponse != null) {
                    respondToClient(orchestrator, ncsResponse.toJson());
                }
            }

            return new ExecutionResult(ExecutionStatus.SUCCESS, false);
        }
    }

    @Override
    public void commandCleanup() {
        if (streamer != null) {
            streamer.terminate(true);
        }
    }
}
