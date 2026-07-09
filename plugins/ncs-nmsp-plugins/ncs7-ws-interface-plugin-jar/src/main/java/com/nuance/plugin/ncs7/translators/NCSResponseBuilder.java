package com.nuance.plugin.ncs7.translators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.ncs.nmsp.lang.NCSQueryRetryAndErrorMessages;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NCSResponseBuilder {
    private static final Logger logger = LogManager.getLogger();

    private static final String QUERY_RESPONSE = "query_response";
    private static final String QUERY_ERROR = "query_error";

    NCSResponseBuilder() { throw new IllegalStateException("Utility class!"); }

    public static NcsResponse nluFinalResponseBuilder(Orchestrator orchestrator, WebApiResponse webApiResponse, String commandName) {
        NcsResponse ncsResponse = buildBasicResponse(orchestrator, webApiResponse, commandName);

        ncsResponse.final_response = 1;
        ncsResponse.result_format = "nlu_interpretation_results";
        ncsResponse.nlu_interpretation_results = new HashMap<String, Object>();

        if (webApiResponse.isSuccess()) {
            ncsResponse.message = QUERY_RESPONSE;
            ncsResponse.nlu_interpretation_results.put("final_response", 1);
            ncsResponse.nlu_interpretation_results.put("status", "success");
            ncsResponse.nlu_interpretation_results.put("payload_format", "nlu-base");
            ncsResponse.nlu_interpretation_results.put("payload_version", "1.0");
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("interpretations", convertToMixInterpretations(orchestrator, webApiResponse.result.get("interpretations")));
            payload.put("type", "nlu-1.0");
            ncsResponse.nlu_interpretation_results.put("payload", payload);

            return ncsResponse;
        } else {
            AbstractCommandTranslator.ExtractedErrors err = AbstractCommandTranslator.extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
            ncsResponse.message = QUERY_ERROR;
            ncsResponse.reason = err.message;
            ncsResponse.code = err.code;
            ncsResponse.nlu_interpretation_results.put("final_response", 1);
            ncsResponse.nlu_interpretation_results.put("status", "failure");
            ncsResponse.nlu_interpretation_results.put("reason", err.message);

            return ncsResponse;
        }
    }

    private static List<InterpretationConverter.MixInterpretation> convertToMixInterpretations(Orchestrator orchestrator, Object rawCobraInterpretations) {
        try {
            String cobraInterpretationsJson = new ObjectMapper()
                    .writeValueAsString(rawCobraInterpretations);

            List<InterpretationConverter.CobraInterpretation> cobraInterpretations = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(cobraInterpretationsJson, new TypeReference<>() {});

            boolean useNewConceptFormat = getUseNewConceptFormat(orchestrator);
            return InterpretationConverter.convertToMixInterpretations(useNewConceptFormat, cobraInterpretations);

        } catch (Exception e) {
            logger.error("Unable to convert Cobra interpretations to Mix interpretations.", e);
            return new ArrayList<>();
        }
    }

    private static boolean getUseNewConceptFormat(Orchestrator orchestrator)
    {
        try
        {
            return orchestrator.getWebApiFeatures().getConfigs(orchestrator.getAppId()).configurationAt("ncs7-ws-plugin").getBoolean("useNewConceptFormat");
        }
        catch (Exception e) {
            return false;
        }
    }

    public static NcsResponse asrFinalResponseBuilder(Orchestrator orchestrator, WebApiResponse webApiResponse, String commandName, NcsRequest ncsRequest)
    {
        boolean werePartialResultsEnabled = ncsRequest.getResultDelivery();

        NcsResponse ncsResponse = buildBasicResponse(orchestrator, webApiResponse, commandName);

        // If partial results were requested, a no input or no match results in a query_response
        boolean isNoMatchOrNoInput = webApiResponse.isNoInput() || webApiResponse.isNoMatch();

        if (webApiResponse.isSuccess())
        {
            ncsResponse = buildNcsASRResponse(orchestrator, webApiResponse, commandName);

            return ncsResponse;
        }
        else if (isNoMatchOrNoInput)
        {
            if(werePartialResultsEnabled) {
                ncsResponse.message = QUERY_RESPONSE;
                ncsResponse.final_response = 1;
                ncsResponse.cadence_regulatable_result = "completeRecognition";
                ncsResponse.result_format = "empty_final_response";

                return ncsResponse;
            }
            else {
                String dictationLanguage = ncsRequest.getASRLanguage();
                ncsResponse.reason = webApiResponse.isNoInput() ? NCSQueryRetryAndErrorMessages.getRecNoInputTimeoutPrompt(dictationLanguage) : NCSQueryRetryAndErrorMessages.getRecNoMatchPrompt(dictationLanguage);
                ncsResponse.message = QUERY_ERROR;
                ncsResponse.code = webApiResponse.isNoInput() ? 2 : 1;
                ncsResponse.final_response =  1;
                ncsResponse.session_id = webApiResponse.sessionId;

                return ncsResponse;
            }
        } else if (webApiResponse.isError()) {
            AbstractCommandTranslator.ExtractedErrors err = AbstractCommandTranslator.extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
            ncsResponse.reason = err.message;
            ncsResponse.message = QUERY_ERROR;
            ncsResponse.code = err.code;
            ncsResponse.final_response =  1;
            ncsResponse.session_id = webApiResponse.sessionId;

            return ncsResponse;
        } else {
            return null;
        }
    }

    private static NcsResponse buildNcsASRResponse(Orchestrator orchestrator, WebApiResponse webApiResponse, String commandName) {
        NcsResponse ncsResponse = buildBasicResponse(orchestrator, webApiResponse, commandName);

        ncsResponse.message = QUERY_RESPONSE;
        ncsResponse.transcriptions = (List) ((HashMap) webApiResponse.result.get("data")).get("transcriptions");
        ncsResponse.confidences =
                (List) ((HashMap) webApiResponse.result.get("data")).get("averageWeightedConfidences");
        ncsResponse.words = (List) ((HashMap) webApiResponse.result.get("data")).get("words");

        if (webApiResponse.isFinal()) {
            ncsResponse.final_response = 1;
            ncsResponse.cadence_regulatable_result = "completeRecognition";
        } else {
            ncsResponse.final_response = 0;
            ncsResponse.cadence_regulatable_result = "partialRecognition";
        }

        ncsResponse.status_code = 0;
        ncsResponse.prompt = "";
        ncsResponse.result_format = "rec_text_results";

        ncsResponse.audio_transfer_info = buildAudioTransferInfo(orchestrator);

        return ncsResponse;
    }

    @NotNull
    public static NcsResponse buildBasicResponse(Orchestrator orchestrator, WebApiResponse webApiResponse, String commandName) {
        NcsResponse ncsResponse = new NcsResponse();

        ncsResponse.result_type = commandName;
        ncsResponse.NMAS_PRFX_SESSION_ID = orchestrator.getSessionId();
        ncsResponse.NMAS_PRFX_TRANSACTION_ID = (String) webApiResponse.command.get("id");
        ncsResponse.transaction_id = Integer.parseInt((String) webApiResponse.command.get("id"));
        return ncsResponse;
    }

    private static Map<String, Object> buildAudioTransferInfo(Orchestrator orchestrator) {
        Map<String, Object> result = new HashMap<>();
        result.put("audio_id", orchestrator.getAudioId());
        result.put("nss_server", "webapi-plugin");
        result.put("start_time", orchestrator.getStartTime());
        result.put("end_time", orchestrator.getEndTime());

        result.put("packages", new LinkedList<Object>());

        return result;
    }
}
