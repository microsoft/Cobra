package com.nuance.plugin.ncs7.translators;

import com.google.gson.Gson;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import com.nuance.plugin.ncs7.streaming.IStreamingEndpoint;
import com.nuance.plugin.ncs7.streaming.StreamingService;
import io.prometheus.client.Histogram;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static com.nuance.plugin.ncs7.translators.SpeechRecognitionTranslator.*;

public class InterpretationWithSpeechRecognitionTranslator extends InterpretationTranslator implements IStreamingEndpoint {
    private static final Logger logger = LogManager.getLogger();
    public static final String NCS_NAME = "NDSP_ASR_APP_CMD";
    NcsResponse ncsASRResponse = new NcsResponse();

    /**
     * Prometheus metrics for measuring the time between the time the stop is received to when the final results are available.
     */
    protected static final Histogram EOS_TO_COMPLETE_DURATION_HISTOGRAM =
            Histogram.build("webapi_ncs7_ndsp_asr_app_cmd_stop_to_complete_duration_seconds",
                            "NDSP_ASR_APP_CMD end of speech to complete duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer endOfSpeechToCompleteHistogramTimer;

    public InterpretationWithSpeechRecognitionTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
    }

    public void sendBinary(byte[] buffer) {
        logger.debug("======> Stream to webapi: {}", buffer.length);
        try {
            orchestrator.getWebApi().onBinary(orchestrator.getWebSocketSession(), buffer);
        } catch (IOException e) {
            logger.warn("Interpretation with SpeechReco streaming error", e);
        }
    }

    @Override
    public boolean isOpen() {
        return (orchestrator.getWebSocketSession().isOpen() && orchestrator.getWebApi().getActiveSession() != null);
    }

    @Override
    public void onStreamingDone() {
        String tenant;
        try {
            tenant = orchestrator.getWebApiFeatures().getConfigs().getString("applicationKey");
        }
        catch (Exception e)
        {
            tenant = "INVALID";
        }
        endOfSpeechToCompleteHistogramTimer = EOS_TO_COMPLETE_DURATION_HISTOGRAM
                .labels(ncsRequest.getASRLanguage(), MetricsConstants.nonNullLabelValue(tenant)).startTimer();

        JSONObject directive = new JSONObject();
        directive.put("name", "stop");

        JSONObject command = new JSONObject();
        command.put("name", WebApiCommandsBuilder.SPEECH_RECO_WEBAPI_NAME);
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
        streamer = new StreamingService(orchestrator.clientAudioBytesBufferQueue, this, "interpretationspeechreco");
        waitForResponse();
        streamer.terminate();
    }

    @Override
    protected void doRequest(NcsRequest ncsRequest) throws IOException {
        setDeviceCodecAsClientData(ncsRequest, orchestrator);

        JSONArray commands = new JSONArray();
        JSONObject command = WebApiCommandsBuilder.buildSpeechRecoCommand(orchestrator, ncsRequest);

        command.put("id", getCommandID(ncsRequest));
        final String parametersKey = "parameters";
        command.getJSONObject(parametersKey).put("speechRecognitionVerboseResult", true);
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
        commands.put(command);

        command = WebApiCommandsBuilder.buildInterpretationCommand(orchestrator, ncsRequest);
        command.put("id", getCommandID(ncsRequest));
        command.getJSONObject(parametersKey).put("data", "_(result.verboseResult)_");
        command.getJSONObject(parametersKey).put("type", "speechRecognitionVerboseResult");
        command.getJSONObject(parametersKey).put("interpretationVerboseResult", true);
        commands.put(command);

        Gson gson = new Gson();
        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "commandChain");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("commands", commands);
        webApiRequest.put("metadata", new JSONObject(gson.toJson(ncsRequest)));

        orchestrator.setAudioId(ncsRequest.audio_id);
        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    @Override
    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.command.getOrDefault("name", "NAN").equals(WebApiCommandsBuilder.INTERPRETATION_WEBAPI_NAME)) {
            return handleInterpretationResults(webApiResponse);
        } else {
            return handleSpeechRecognitionResults(webApiResponse);
        }
    }

    private ExecutionResult handleSpeechRecognitionResults(WebApiResponse webApiResponse) throws IOException {
        NcsResponse ncsResponse;
        if(webApiResponse.isFinal()) {
            removeDeviceCodecAsClientData(orchestrator);
        }

        if (webApiResponse.isEvent("endOfSpeech")) {
            streamer.terminate(true);
            return new ExecutionResult(ExecutionStatus.SUCCESS, false);
        }
        if(webApiResponse.isError()){
            ncsResponse = NCSResponseBuilder.nluFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME);
            if (ncsResponse != null) {
                respondToClient(orchestrator, ncsResponse.toJson());
            }
            sendQueryEnd(orchestrator, webApiResponse, true);
            return doFinal(orchestrator, webApiResponse);
        }
        else if(webApiResponse.isResult()) {
            boolean isFinal = webApiResponse.isNoInput() || webApiResponse.isNoMatch();
            ncsResponse = NCSResponseBuilder.asrFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME, ncsRequest);
            ncsASRResponse = ncsResponse;
            ncsResponse.final_response = !isFinal ? 0 : 1;
            respondToClient(orchestrator, ncsResponse.toJson());
            if(isFinal) {
                sendQueryEnd(orchestrator, webApiResponse);
                return doFinal(orchestrator, webApiResponse);
            }
        }
        return new ExecutionResult(ExecutionStatus.SUCCESS, false);
    }

    private ExecutionResult handleInterpretationResults(WebApiResponse webApiResponse) throws IOException {
        NcsResponse ncsResponse;
        if(endOfSpeechToCompleteHistogramTimer != null) {
            endOfSpeechToCompleteHistogramTimer.observeDuration();
        }
        ncsResponse = NCSResponseBuilder.nluFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME);
        if (webApiResponse.isSuccess()) {
            ncsResponse.audio_transfer_info = ncsASRResponse.audio_transfer_info;
            ncsResponse.cadence_regulatable_result = ncsASRResponse.cadence_regulatable_result;
            ncsResponse.prompt = ncsASRResponse.prompt;
            ncsResponse.status_code = ncsASRResponse.status_code;
        }
        respondToClient(orchestrator, ncsResponse.toJson());
        sendQueryEnd(orchestrator, webApiResponse);

        return doFinal(orchestrator, webApiResponse);
    }
}
