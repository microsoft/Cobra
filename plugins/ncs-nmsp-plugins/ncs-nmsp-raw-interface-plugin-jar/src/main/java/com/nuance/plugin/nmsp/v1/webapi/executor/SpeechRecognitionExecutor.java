package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.Tools;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.nmsp.v1.webapi.streaming.IStreamingEndpoint;
import com.nuance.plugin.nmsp.v1.webapi.streaming.StreamingService;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpeechRecognitionExecutor extends Executor implements IStreamingEndpoint {
    private static final Logger logger = LogManager.getLogger();
    public static final String DICTATION_TYPE = "dictation_type";
    public static final String CONTEXT_TAG = "context_tag";
    public static final String TOPIC = "topic";
    public static final String SPEECH_DOMAIN = "speechDomain";
    private double globalConfidenceTuning = Tools.reMap(0, -100, 100, -1, 1);
    private boolean confidenceOverwrite = false;
    HierarchicalConfiguration<ImmutableNode> speechRecognitionLanguageMap;
    BaseHierarchicalConfiguration sessionConfigs = null;

    public SpeechRecognitionExecutor(Session nmspSession, HashMap<String, Object> queryInfo, String webAPISessionID) {
        super(nmspSession, queryInfo, webAPISessionID);
        try {
            sessionConfigs = nmspSession.getWebAPIOrchestrator().getSessionConfigs();

            confidenceOverwrite = PdxInterfaceConfig.getPluginConfMap(nmspSession.getWebAPIOrchestrator().getWebApiFeatures().getConfigs()).getBoolean(
                    PdxInterfaceConfig.Property.OVERWRITE_WORD_CONFIDENCE.getKey());

            if (sessionConfigs != null && sessionConfigs.containsKey(PdxInterfaceConfig.Property.CONFIDENCE_DYNAMIC_RANGE_LANGUAGE_TUNING.getKey())) {
                speechRecognitionLanguageMap = sessionConfigs.configurationAt(PdxInterfaceConfig.Property.CONFIDENCE_DYNAMIC_RANGE_LANGUAGE_TUNING.getKey());
            }

            globalConfidenceTuning = PdxInterfaceConfig.getPluginConfMap(nmspSession.getWebAPIOrchestrator().getWebApiFeatures().getConfigs()).getInt(
                    PdxInterfaceConfig.Property.CONFIDENCE_DYNAMIC_RANGE_TUNING.getKey());
            globalConfidenceTuning = Tools.reMap(globalConfidenceTuning, -100, 100, -1, 1);

            BigDecimal bd = BigDecimal.valueOf(globalConfidenceTuning);
            bd = bd.round(new MathContext(3));
            globalConfidenceTuning = bd.doubleValue();

        } catch (IOException e) {
            logger.warn("Failed to read plugin configs, using default maxRecognitionResults");
        }
    }

    public static final String WEBAPI_SPEECH_RECO_CMD = "speechRecognition";

    private static void setPdxDefaultIfUnset(String paramName, int pdxDefault, JSONObject parameters, CombinedConfiguration sessionConfig)
    {
        int paramValueFromConfig = sessionConfig.getInt(paramName);
        if(paramValueFromConfig == -1) //unset
        {
            parameters.put(paramName, pdxDefault);
        }
    }

    public static JSONObject buildSpeechRecoCommand(Map<String, Object> queryInfo, Session nmspSession) throws IOException {
        JSONObject parameters = new JSONObject();
        CombinedConfiguration sessionConfig = nmspSession.getWebAPIOrchestrator().getSessionConfigs();

        setPdxDefaultIfUnset("maxTranscriptions", PdxInterfaceConfig.MAX_TRANSCRIPTIONS, parameters, sessionConfig);
        parameters.put("speechRecognitionCodec", nmspSession.getFromCodec().getWebAPIString());
        parameters.put("endPointDetection", false); //disable endpoint detection
        parameters.put("language", nmspSession.getDictationLanguage());

        PdxDictionary dict = (PdxDictionary) queryInfo.get("dict");
        parameters.put("wordStream", isPartialResultEnabled(dict)); //set word streaming

        if (queryInfo.get(DICTATION_TYPE) != null) {
            setTopicAndSpeechDomainBasedOnDictationTypeAndDictationLanguage(queryInfo, nmspSession, parameters, sessionConfig);
        }

        if(queryInfo.containsKey(CONTEXT_TAG)) {
            parameters.put("dialogModelApplicationVersion", queryInfo.get(CONTEXT_TAG));
            setTopicAndSpeechDomainBasedOnContextTagAndDictationLanguage(queryInfo, nmspSession, parameters, sessionConfig);
        }

        var enableProfanityFiltering = (int) dict.getOrDefault("enable_profanity_filtering", 0);
        if (enableProfanityFiltering == 1) {
            parameters.put("speechRecognitionEnableProfanityFilter", true);
        }

        parameters.put("speechRecognitionVerboseResult", true);

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_SPEECH_RECO_CMD);

        return command;
    }

    private static void setTopicAndSpeechDomainBasedOnDictationTypeAndDictationLanguage(Map<String, Object> queryInfo, Session nmspSession, JSONObject parameters, CombinedConfiguration sessionConfig) {
        try {
            HierarchicalConfiguration<ImmutableNode> pluginConfigs = sessionConfig.configurationAt(PdxInterfaceConfig.Property.DICTATION_TYPE_TO_TOPIC_MAP.getKey());
            if (pluginConfigs == null) {
                logger.error("Failed to get dictationTypeToTopicMap.");
            } else {
                HierarchicalConfiguration<ImmutableNode> dictationTypeConfig = pluginConfigs.configurationAt((String) queryInfo.get(DICTATION_TYPE));
                putTopicAndSpeechDomain(nmspSession, dictationTypeConfig, parameters, "Failed to map dictation_type.");
            }
        } catch (ConfigurationRuntimeException e) {
            logger.debug("Client requested dictation_type='{}' but the '{}' doesn't exist. Will use default topic and speech domain.", queryInfo.get(DICTATION_TYPE), PdxInterfaceConfig.Property.DICTATION_TYPE_TO_TOPIC_MAP.getKey());
        }
    }

    private static void setTopicAndSpeechDomainBasedOnContextTagAndDictationLanguage(Map<String, Object> queryInfo, Session nmspSession, JSONObject parameters, CombinedConfiguration sessionConfig) {
        try {
            HierarchicalConfiguration<ImmutableNode> pluginConfigs = sessionConfig.configurationAt(PdxInterfaceConfig.Property.CONTEXT_TAG_TO_TOPIC_MAP.getKey());
            if (pluginConfigs == null) {
                logger.error("Failed to get contextTagToTopicMap.");
            } else {
                HierarchicalConfiguration<ImmutableNode> contextTagConfig = pluginConfigs.configurationAt((String) queryInfo.get(CONTEXT_TAG));
                putTopicAndSpeechDomain(nmspSession, contextTagConfig, parameters, "Failed to map context_tag.");
            }
        } catch (ConfigurationRuntimeException e) {
            logger.debug("Client requested context_tag='{}' but the '{}' doesn't exist. Will use default topic and speech domain.", queryInfo.get(CONTEXT_TAG), PdxInterfaceConfig.Property.CONTEXT_TAG_TO_TOPIC_MAP.getKey());
        }
    }

    private static void putTopicAndSpeechDomain(Session nmspSession, HierarchicalConfiguration<ImmutableNode> config, JSONObject parameters, String error2)
    {
        if (nmspSession.getDictationLanguage() != null && config != null) {
            HierarchicalConfiguration<ImmutableNode> dictationLanguageConfig = config.configurationAt(nmspSession.getDictationLanguage());
            if (dictationLanguageConfig != null) {
                parameters.put(TOPIC, dictationLanguageConfig.getString(TOPIC));
                parameters.put(SPEECH_DOMAIN, dictationLanguageConfig.getString(SPEECH_DOMAIN));
            } else {
                logger.error("Failed to map dictation_language.");
            }
        } else {
            logger.error(error2);
        }
    }

    public static boolean isPartialResultEnabled(PdxDictionary dict) {
        boolean result = false;
        if (dict.get("enable_intermediate_response") != null) {
            // Deprecated parameter, overwritten by intermediate_response_mode
            result = (int) dict.get("enable_intermediate_response") != 0;
        }
        if (dict.get("intermediate_response_mode") != null) {
            /** Possible values are
             * - UtteranceDetectionWithCompleteRecognition
             * - UtteranceDetectionWithPartialRecognition (not for customers, but documented)
             * - NoUtteranceDetectionWithPartialRecognition
             */
            String intermediateResponseMode = dict.get("intermediate_response_mode").toString().toLowerCase();
            result = !intermediateResponseMode.equalsIgnoreCase("UtteranceDetectionWithCompleteRecognition");
        }
        return result;
    }

    public void execute() throws IOException {
        setDeviceCodecAsClientData(nmspSession);

        JSONArray commands = new JSONArray();

        JSONObject command = buildSpeechRecoCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        command = EndSessionExecutor.buildEndSessionCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "commandChain");
        webApiRequest.put("sessionId", getWebAPISessionID());
        webApiRequest.put("commands", commands);

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, webApiRequest.toString(), false);

        if (!isTerminated) {
            streamer = StreamingService.StreamingServiceBuilder.build(nmspSession.getWebAPIOrchestrator().getAudioBufferQueue(), this, commandID);
        }
    }

    @Override
    public void cancelCommand() {
        terminateStreaming();
        sendDirective("cancel", WEBAPI_SPEECH_RECO_CMD);
        isTerminated = true;
    }

    public void onResponse(WebApiResponse responseJson) throws IOException
    {
        if (responseJson.isResult() && responseJson.isForCommand(WEBAPI_SPEECH_RECO_CMD)) {
            if (responseJson.isSuccess()) {
                handleSuccessWebapiResponse(responseJson);
            }

            boolean partialResultsEnabled = SpeechRecognitionExecutor.isPartialResultEnabled((PdxDictionary) queryInfo.get("dict"));
            //No match without streaming
            if (responseJson.isNoMatch() && !partialResultsEnabled)
            {
                endWebapiSessionWithFinal(ResponseUtils.buildRetry(queryInfo, nmspSession, "NoMatch", "No match.", ResponseCodes.CompletionCause.NOMATCH));
                return;
            }

            //No match with streaming OR no input
            if (responseJson.isNoMatch() || responseJson.isNoInput()) {
                sendEmptyResponse();
            }

            if (responseJson.isError()) {
                ExtractedErrors errors = extractWebApiErrors((ArrayList<HashMap<String, Object>>) responseJson.result.get("errors"));
                nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, errors.message, errors.code));
            }

            if (responseJson.isCanceled() && !isTerminated) {
                nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Operation canceled", ResponseCodes.CompletionCause.CANCELLED));
            }
        }
    }

    private void handleSuccessWebapiResponse(WebApiResponse responseJson) throws IOException {
        if (responseJson.isFinal()) {
            removeDeviceCodecAsClientData(nmspSession.getWebAPIOrchestrator());
            doFinal(responseJson);
            terminateStreaming();
        } else if (isPartialResultEnabled((PdxDictionary) queryInfo.get("dict"))) {
            sendPartial(responseJson);
        }
    }

    private void sendEmptyResponse() {
        endWebapiSessionWithFinal(NMSPResponseBuilder.buildPdxASREmptyResponseResult(nmspSession, getCommandName()).toByteArray());
    }

    private void sendPartial(WebApiResponse responseJson) throws IOException {
        nmspClientHandler.sendData(nmspSession, nmspSession.getWebAPIOrchestrator().getTransactionID(), nmspSession.getLastPingRequest(),
                NMSPResponseBuilder.buildPdxASRResult(responseJson, queryInfo, nmspSession, getCommandName(), false, getConfidenceBias(nmspSession.getDictationLanguage()), confidenceOverwrite).toByteArray());
    }

    protected void doFinal(WebApiResponse responseJson) throws IOException {
        endWebapiSessionWithFinal(NMSPResponseBuilder.buildPdxASRResult(responseJson, queryInfo, nmspSession, getCommandName(), true, getConfidenceBias(nmspSession.getDictationLanguage()), confidenceOverwrite).toByteArray());
    }

    @Override
    public void onBinary(byte[] data) {
        //there should be no Binary form webapi during ASR
    }

    @Override
    public void onClose(CloseReason closeReason) {
        // Nothing to do
    }

    @Override
    public void sendBinary(byte[] buffer) {
        try {
            nmspSession.getWebAPIOrchestrator().streamToWebApi(buffer, false);
        } catch (IOException e) {
            logger.warn("Failed to stream to webapi", e);
        }
    }

    @Override
    public boolean isOpen() {
        return nmspSession.getWebAPIOrchestrator().getWebapiSocketSession().isOpen();
    }

    @Override
    public void onStreamingDone() {
        sendDirective("stop", WEBAPI_SPEECH_RECO_CMD);
        isTerminated = true;
        terminateStreaming();
    }

    private double getConfidenceBias(String language) {
        try
        {
            if (sessionConfigs == null) {
                return globalConfidenceTuning;
            }

            if (speechRecognitionLanguageMap == null)
            {
                //if speechRecognitionLanguageMap is not found
                return globalConfidenceTuning;
            }

            if (speechRecognitionLanguageMap.containsKey(language)) {
                int langConfidence = speechRecognitionLanguageMap.getInt(language, 0);

                BigDecimal bd = BigDecimal.valueOf(Tools.reMap(langConfidence, -100, 100, -1, 1));
                bd = bd.round(new MathContext(3));
                return bd.doubleValue();
            } else {
                return globalConfidenceTuning;
            }

        } catch(Exception e) {
            return globalConfidenceTuning;
        }
    }

    public static void setDeviceCodecAsClientData(Session nmspSession) {
        String clientDetailsKey = "clientDetails";
        CombinedConfiguration sessionConfig = nmspSession.getWebAPIOrchestrator().getWebApiEndpoint().getActiveSession().getSessionConfig();
        try {
            ICodecResource.Codec deviceCodec = nmspSession.getFromCodec();
            if(deviceCodec != null) {
                Map<String, Object> oldDetails = getOldDetails(clientDetailsKey, sessionConfig);
                Map<String, Object> newDetailsMap = new HashMap<>();
                Map<String, Object> newClientDetailsMap = oldDetails.size() > 0 ? oldDetails : new HashMap<>();
                newClientDetailsMap.put("deviceCodec", String.valueOf(deviceCodec.getNmaidCode()));
                newDetailsMap.put(clientDetailsKey, newClientDetailsMap);
                WebApiJSONConfiguration newDetails = new WebApiJSONConfiguration();
                newDetails.load(newDetailsMap);
                sessionConfig.addConfiguration(newDetails, "clientDetailsTemp");
            }
        } catch (Exception e) {
            logger.info("Failed to read previous clientDetails before injecting deviceCodec {}", e.getMessage());
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

    public static void removeDeviceCodecAsClientData(WebAPIOrchestrator orchestrator) {
        CombinedConfiguration sessionConfig = orchestrator.getWebApiEndpoint().getActiveSession().getSessionConfig();
        sessionConfig.removeConfiguration("clientDetailsTemp");
    }
}
