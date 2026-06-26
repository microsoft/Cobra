package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.google.gson.Gson;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages.PDXQueryResult;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import com.nuance.plugin.nmsp.v1.webapi.streaming.IStreamingEndpoint;
import com.nuance.plugin.nmsp.v1.webapi.streaming.StreamingService;
import io.prometheus.client.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.nuance.plugin.nmsp.v1.webapi.executor.SpeechRecognitionExecutor.removeDeviceCodecAsClientData;
import static com.nuance.plugin.nmsp.v1.webapi.executor.SpeechRecognitionExecutor.setDeviceCodecAsClientData;

public class InterpretationWithSpeechRecognitionExecutor extends Executor implements IStreamingEndpoint
{
    private static final Logger logger = LogManager.getLogger();

    private static final String PARAMETERS = "parameters";

    /**
     * Prometheus metrics for measuring the time between the time the stop is received to when the final results are available.
     */
    protected static final Histogram EOS_TO_COMPLETE_DURATION_HISTOGRAM =
            Histogram.build("webapi_ncs7_ndsp_asr_app_cmd_stop_to_complete_duration_seconds",
                            "NDSP_ASR_APP_CMD end of speech to complete duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer endOfSpeechToCompleteHistogramTimer;

    public InterpretationWithSpeechRecognitionExecutor(Session nmspSession, HashMap<String, Object> queryInfo, String webAPISessionID) {
        super(nmspSession, queryInfo, webAPISessionID);
    }

    public void execute() throws IOException {
        setDeviceCodecAsClientData(nmspSession);

        JSONArray commands = new JSONArray();

        JSONObject command = SpeechRecognitionExecutor.buildSpeechRecoCommand(queryInfo, nmspSession);
        command.put("id", commandID);

        command.getJSONObject(PARAMETERS).put("speechRecognitionVerboseResult", true);
        commands.put(command);

        command = InterpretationExecutor.buildInterpretationCommand(queryInfo, nmspSession);
        command.put("id", commandID);

        if(getCommandName().equalsIgnoreCase("DRAGON_NLU_ASR_CMD"))
        {
            command.getJSONObject(PARAMETERS).put("type", "text/plain");
            command.getJSONObject(PARAMETERS).put("data", "_(result.bestResult)_");
        } else
        {
            command.getJSONObject(PARAMETERS).put("type", "speechRecognitionVerboseResult");
            command.getJSONObject(PARAMETERS).put("data", "_(result.verboseResult)_");
        }
        commands.put(command);

        command = EndSessionExecutor.buildEndSessionCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        Gson gson = new Gson();
        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "commandChain");
        webApiRequest.put("sessionId", getWebAPISessionID());
        webApiRequest.put("commands", commands);
        webApiRequest.put("metadata", new JSONObject(gson.toJson(queryInfo)));

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, webApiRequest.toString(), false);

        if (!isTerminated) {
            streamer = StreamingService.StreamingServiceBuilder.build(nmspSession.getWebAPIOrchestrator().getAudioBufferQueue(), this, commandID);
        }
    }

    @Override
    public void cancelCommand() {
        sendDirective("cancel", SpeechRecognitionExecutor.WEBAPI_SPEECH_RECO_CMD);
        isTerminated = true;
        terminateStreaming();
    }

    public void onResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isChainResult()) {
            return;
        }

        if (webApiResponse.isForCommand(InterpretationExecutor.WEBAPI_INTERPRETATION_CMD)) {
            if(endOfSpeechToCompleteHistogramTimer != null) {
                endOfSpeechToCompleteHistogramTimer.observeDuration();
            }
            doInterpretationResponse(webApiResponse);
        }

        if (webApiResponse.isForCommand(SpeechRecognitionExecutor.WEBAPI_SPEECH_RECO_CMD)) {
            doSpeechRecoResponse(webApiResponse);
        }
    }

    @SuppressWarnings("java:S3776")
    void doSpeechRecoResponse(WebApiResponse webApiResponse) throws IOException
    {
        if (webApiResponse.isResult()) {
            boolean partialResultsEnabled = SpeechRecognitionExecutor.isPartialResultEnabled((PdxDictionary) queryInfo.get("dict"));

            if (webApiResponse.isSuccess()) {
                if (webApiResponse.isFinal()) {
                    terminateStreaming();
                    if(!getCommandName().equalsIgnoreCase("DRAGON_NLU_ASR_CMD"))
                    {
                        removeDeviceCodecAsClientData(nmspSession.getWebAPIOrchestrator());
                        doFinalASR(webApiResponse);
                    }
                } else if (partialResultsEnabled) {
                    sendPartial(webApiResponse);
                }
            }

            //No match without streaming
            if (webApiResponse.isNoMatch() && !partialResultsEnabled)
            {
                endWebapiSessionWithFinal(ResponseUtils.buildRetry(queryInfo, nmspSession, "NoMatch", "No match.", ResponseCodes.CompletionCause.NOMATCH));
                return;
            }

            //No match with streaming OR no input
            if (webApiResponse.isNoMatch() || webApiResponse.isNoInput()) {
                sendEmptyResponse();
            }

            if (webApiResponse.isError()) {
                ExtractedErrors errors = extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
                nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, errors.message, errors.code));
            }

            if (webApiResponse.isCanceled()) {
                if (!isTerminated) {
                    nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Operation canceled", ResponseCodes.CompletionCause.CANCELLED));
                } else {
                    endWebapiSessionWithFinal(null); //end webapi session
                }
            }
        }
    }

    private void doInterpretationResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isResult()) {
            if (webApiResponse.isSuccess() && webApiResponse.isFinal()) {
                doFinal(webApiResponse);
                nmspSession.getWebAPIOrchestrator().commandDone(nmspSession, true);
            }

            if (webApiResponse.isError()) {
                ExtractedErrors errors = extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
                nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, errors.message, errors.code));
            }

            if (webApiResponse.isCanceled()) {
                if (!isTerminated) {
                    nmspSession.getWebAPIOrchestrator().sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Operation canceled", ResponseCodes.CompletionCause.CANCELLED));
                } else {
                    endWebapiSessionWithFinal(null); //end webapi session
                }
            }
        }
    }

    private void sendEmptyResponse() {
        endWebapiSessionWithFinal(NMSPResponseBuilder.buildPdxASREmptyResponseResult(nmspSession, getCommandName()).toByteArray());
    }

    private void sendPartial(WebApiResponse webApiResponse) throws IOException {
        nmspClientHandler.sendData(nmspSession, nmspSession.getWebAPIOrchestrator().getTransactionID(), nmspSession.getLastPingRequest(),
                NMSPResponseBuilder.buildPdxASRResult(webApiResponse, queryInfo, nmspSession, getCommandName(), false, 0, false).toByteArray());
    }

    private void doFinalASR(WebApiResponse webApiResponse) throws IOException {
        PDXQueryResult response = NMSPResponseBuilder.buildPdxASRResult(webApiResponse, queryInfo, nmspSession, getCommandName(), true, 0, false);
        response.addInteger("final_response", 0);

        nmspClientHandler.sendData(nmspSession, nmspSession.getWebAPIOrchestrator().getTransactionID(), nmspSession.getLastPingRequest(),
                response.toByteArray());
    }

    protected void doFinal(WebApiResponse webApiResponse)  throws IOException {
        PDXQueryResult response = NMSPResponseBuilder.buildPdxNLUResult(webApiResponse, queryInfo, nmspSession, getCommandName());
        response.addDictionary("audio_transfer_info", nmspSession.getWebAPIOrchestrator().getAudioInfo());

        endWebapiSessionWithFinal(response.toByteArray());
    }

    @Override
    public void onBinary(byte[] data) {
        //there should be no Binary form webapi durin ASR
    }

    @Override
    public void onClose(CloseReason closeReason)
    {
        //Do nothin
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
    public void onStreamingDone() {
        String tenant = nmspSession.getNmaid();
        String dictLanguage = nmspSession.getDictationLanguage();
        if(dictLanguage == null) {
            dictLanguage = "<not defined>";
            logger.error("Couldn't find dictationLanguage");
        }
        endOfSpeechToCompleteHistogramTimer = EOS_TO_COMPLETE_DURATION_HISTOGRAM
                .labels(dictLanguage, MetricsConstants.nonNullLabelValue(tenant)).startTimer();
        sendDirective("stop", SpeechRecognitionExecutor.WEBAPI_SPEECH_RECO_CMD);
        isTerminated = true;
        terminateStreaming();
    }

    @Override
    public boolean isOpen() {
        return nmspSession.getWebAPIOrchestrator().getWebapiSocketSession().isOpen();
    }

}
