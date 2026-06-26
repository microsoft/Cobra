package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.nle.NLEConverter;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiInterpretationCommandBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.nuance.plugin.kryptonws.response.KryptonWsMessage.STATUS.COMPLETE;
import static com.nuance.plugin.kryptonws.response.KryptonWsMessage.STATUS.FAILED;
import static com.nuance.plugin.kryptonws.utils.KryptonWsSessionObjectsHelper.WORDSET_PKG_TYPE;

public class KryptonWsInterpretMethod
        extends KryptonWsMethod
{
    private static final Logger logger = LogManager.getLogger(KryptonWsInterpretMethod.class);
    public static final String NAME = "Interpret";
    public static final String BEST_INTERPRETATION = "bestInterpretation";
    public static final String LITERAL = "literal";
    public static final String VERBOSE_RESULT = "verboseResult";
    public static final String METADATA = "metadata";
    public static final String SUPPLEMENTAL_METRICS = "x-nuance-supplemental-metrics-result";
    public static final String MODEL_LANGUAGE = "modelLanguage";

    protected KryptonWsInterpretMethod(JSONObject kryptonWsMethod, KryptonWsSession kryptonWsSession)
    {
        super(kryptonWsMethod, kryptonWsSession);
        logger.debug("KryptonWsInterpretMethod constructor");
    }

    @Override
    public void process()
    {
        process(this);
    }

    public void process(KryptonWsMethod methodToReceiveReply)
    {
        super.process();

        if(kryptonWsSession.getSemanticModel() == null) {
            kryptonWsResponseBuilder.addError("No semantic model loaded", null);
            sendResponse(FAILED);
            return;
        }

        var webapiCombinedConfig = kryptonWsSession.getWebapiSessionConfig();

        kryptonWsSession.getKryptonWsParameters().interpretAddConfig(methodJson, webapiCombinedConfig, kryptonWsSession);

        var wapiSessionId = kryptonWsSession.getWebapiSessionId();
        var wapiRequestId = Integer.toString(kryptonWsSession.getNextWebapiStepId());
        var wapiCommandBuilder = new WebapiInterpretationCommandBuilder(wapiSessionId, wapiRequestId);

        if (methodJson.has(KryptonWsRecognizeMethod.USE_WORD_SETS_KEY))
        {
            var useWordSets = methodJson.getJSONArray(KryptonWsRecognizeMethod.USE_WORD_SETS_KEY);
            JSONArray filteredUseWordSets = new JSONArray();

            for(Object wordSetObject : useWordSets) {
                String wordSetId = (String)wordSetObject;
                if(!kryptonWsSession.getWordSetIds().contains(wordSetId)) {
                    kryptonWsResponseBuilder.addError("Wordset ID not found", wordSetId);
                    sendResponse(FAILED);
                    return;
                } else {
                    addWordset(filteredUseWordSets, wordSetId, WORDSET_PKG_TYPE);
                }
            }

            if (filteredUseWordSets.length() > 0)
            {
                wapiCommandBuilder.activeDynamicVocabularySet(filteredUseWordSets.toList().toArray(new String[0]));
            }
        }

        wapiCommandBuilder.setParameters(kryptonWsSession.getKryptonWsParameters().getConfigurations());
        wapiCommandBuilder.statistics(true);
        wapiCommandBuilder.interpretationVerboseResult(true);

        var wapiCommandJson = wapiCommandBuilder.buildRequest();
        var wapiCommandString = wapiCommandJson.toString();

        sendToWebapi(wapiCommandString, methodToReceiveReply);
    }

    @Override
    public void processWebapiResponse(String webapiResponseString)
    {
        logger.info("Got webapi response: {}", webapiResponseString);

        WebApiResponse webApiResponse;

        try
        {
            webApiResponse = WebApiResponse.fromJson(webapiResponseString);
        }
        catch (JsonProcessingException e)
        {
            var errorMessage = "Unable to parse webapi response json";
            logger.error("{}: {}", errorMessage, webapiResponseString, e);
            sendErrorResponse(errorMessage, null);
            cleanup();
            return;
        }

        if (webApiResponse.command == null || webApiResponse.command.get("name").equals("interpretation")) {
            processResults(webApiResponse, webapiResponseString);
        }
    }

    protected void processResults(WebApiResponse webApiResponse, String wapiResponseString) {
        if(areMetricsEnabled) {
            kryptonWsResponseBuilder.setMetrics(buildInterpretationMetrics(webApiResponse, startTime));
        }

        if (webApiResponse.isSuccess())
        {
            var nleInterpretations = NLEConverter.convertWebapiInterpretationToNle(webApiResponse, false);

            kryptonWsResponseBuilder.setStatus(COMPLETE).setData(nleInterpretations.toMap());

            sendCompleteResponse();
        }
        else if (webApiResponse.isError())
        {
            sendErrorResponseFromWebapiResponseObject(webApiResponse);
        }
        else if (webApiResponse.isCanceled())
        {
            sendErrorResponse("Interpret was cancelled.", null);
        }
        else if (webApiResponse.isNoMatch())
        {
            sendErrorResponse("Interpret resulted in a no match.", null);
        }
        cleanup();
    }

    static Map<String, Object> buildInterpretationMetrics(WebApiResponse webApiResponse, Calendar interpretationStartTime) {
        var metrics = buildCommonMetrics(interpretationStartTime, getCurrentUTCTime());

        metrics.put("status", webApiResponse.isSuccess() ? "complete" : "failed");
        String completionCause;
        if(webApiResponse.isSuccess()) {
            completionCause = "SUCCESS";
            if (((String) ((Map<String, Object>) webApiResponse.result.get("bestInterpretation")).get("intent")).equalsIgnoreCase("NO_MATCH")) {
                completionCause = "NO-MATCH";
            }
        } else {
            completionCause = "ERROR";
        }
        metrics.put("completionCause",  completionCause);

        List<Map<?,?>> pipeline = new LinkedList<>();
        List<Map<?,?>> tokenizer = new LinkedList<>();

        if(webApiResponse.result.containsKey("interpretations")) {
            for (Map<String, Object> interpretation : (List<Map<String, Object>>) webApiResponse.result.get("interpretations")) {
                Map<String, Object> result = new HashMap<>();
                result.put("intention", interpretation.get("intent"));
                result.put("confidence", interpretation.get("confidence"));
                result.put("origin", interpretation.get("origin"));

                if(interpretation.containsKey("concepts")) {
                    List<Map<?, ?>> mentions = new LinkedList<>();
                    for (Map<String, Object> concept : (List<Map<String, Object>>) interpretation.get("concepts")) {
                        Map<String, Object> mention = new HashMap<>();
                        mention.put("concept", concept.get("concept"));
                        mention.put("value", concept.get("literal"));
                        mentions.add(mention);
                    }
                    result.put("mentions", mentions);
                }
                pipeline.add(result);
                tokenizer.add(Map.of("mff", interpretation.get("literal")));
            }

            putPipelineMetrics(webApiResponse, metrics, pipeline);
            metrics.put("tokenizer", Map.of("results", tokenizer));
            putEngineMetrics(webApiResponse, metrics);
        }

        parseVerboseResults(webApiResponse, metrics);

        return metrics;
    }

    private static void putPipelineMetrics(WebApiResponse webApiResponse, Map<String, Object> metrics, List<Map<?,?>> pipeline) {
        Map<String, Object> pipelineMetrics = new HashMap<>();
        pipelineMetrics.put("results", pipeline);

        String inputTextualRepresentation = getInputTextualRepresentation(webApiResponse);
        if (inputTextualRepresentation != null) {
            pipelineMetrics.put("input", inputTextualRepresentation);
        }

        pipelineMetrics.put("start", ((List<?>) metrics.get("timestamps")).get(0));

        metrics.put("pipeline", pipelineMetrics);
    }

    private static void putEngineMetrics(WebApiResponse webApiResponse, Map<String, Object> metrics) {
        String semanticModelLanguage = getSemanticModelLanguage(webApiResponse);
        if (semanticModelLanguage != null) {
            metrics.put("engine", Map.of("language", semanticModelLanguage));
        }
    }

    private static String getInputTextualRepresentation(WebApiResponse webApiResponse) {
        try {
            if (webApiResponse.result.containsKey(BEST_INTERPRETATION)) {
                Map<String, Object> bestInterpretation = (Map<String, Object>) webApiResponse.result.get(BEST_INTERPRETATION);

                if (bestInterpretation.containsKey(LITERAL)) {
                    return (String) bestInterpretation.get(LITERAL);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to read NLU input from Cobra result.", e);
        }
        return null;
    }

    /**
     * We cannot use 'statistics.engineRequests.language' because it is not an 'ISO 3+3 language code'
     * We have to use 'verboseResult.metadata.x-nuance-supplemental-metrics-result.modelLanguage' because it is
     */
    private static String getSemanticModelLanguage(WebApiResponse webApiResponse) {
        try {
            if (webApiResponse.result.containsKey(VERBOSE_RESULT)) {
                Map<String, Object> verboseResult = (Map<String, Object>) webApiResponse.result.get(VERBOSE_RESULT);

                if (verboseResult.containsKey(METADATA)) {
                    Map<String, Object> metadata = (Map<String, Object>) verboseResult.get(METADATA);

                    if (metadata.containsKey(SUPPLEMENTAL_METRICS)) {
                        Map<String, Object> supplementalMetrics = (Map<String, Object>) metadata.get(SUPPLEMENTAL_METRICS);

                        if (supplementalMetrics.containsKey(MODEL_LANGUAGE)) {
                            return (String) supplementalMetrics.get(MODEL_LANGUAGE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to read NLU language from Cobra result.", e);
        }
        return null;
    }

    private static void parseVerboseResults(WebApiResponse webApiResponse, Map<String, Object> metrics) {
        try {
            final var verboseResultKey = "verboseResult";
            final var metadataKey = "metadata";
            final var metricsKey = "x-nuance-supplemental-metrics-result";
            if(webApiResponse.result.containsKey(verboseResultKey)) {
                var verboseResults = (Map<String, Object>) webApiResponse.result.get(verboseResultKey);
                if(verboseResults.containsKey(metadataKey) && (verboseResults.get(metadataKey) instanceof Map)) {
                    var metadata =  (Map<String, Object>) verboseResults.get(metadataKey);
                    if(metadata.containsKey(metricsKey)) {
                        ((Map<String, Object>) metadata.get(metricsKey)).remove("tokens");
                        metrics.put(metadataKey, metadata);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to parse NLU verbose result from Cobra response.", e);
        }
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    public static class Builder
    {
        private KryptonWsSession kryptonWsSession;

        private String method; // type: String, required: Yes, default: none
        private String version; // type: String, required: No, default: 1.0
        private String sessionId; // type: String, required: No, default: none
        private String requestId; // type: String, required: No, default: random UUID
        private String model; // type: String, required: Yes, default: none
        private JSONArray useWordSets; // type: String array, required: No, default: empty
        private String inputType; // type: String, required: No, default: text/plain
        private Object input; // type: String, required: Yes, default: none
        private String responseType; // type: String, required: No, default: application/json, Valid values are "suppress" and "off"
        private String sensitive; // type: String, required: No, default: configurable

        public Builder setKryptonWsSession(KryptonWsSession kryptonWsSession)
        {
            this.kryptonWsSession = kryptonWsSession;
            return this;
        }

        public Builder setMethod(String method)
        {
            this.method = method;
            return this;
        }

        public Builder setVersion(String version)
        {
            this.version = version;
            return this;
        }

        public Builder setSessionId(String sessionId)
        {
            this.sessionId = sessionId;
            return this;
        }

        public Builder setRequestId(String requestId)
        {
            this.requestId = requestId;
            return this;
        }

        public Builder setModel(String model)
        {
            this.model = model;
            return this;
        }

        public Builder setUseWordSets(JSONArray useWordSets)
        {
            this.useWordSets = useWordSets;
            return this;
        }

        public Builder setInputType(String inputType)
        {
            this.inputType = inputType;
            return this;
        }

        public Builder setInput(Object input)
        {
            this.input = input;
            return this;
        }

        public Builder setResponseType(String responseType)
        {
            this.responseType = responseType;
            return this;
        }

        public Builder setSensitive(String sensitive)
        {
            this.sensitive = sensitive;
            return this;
        }

        public KryptonWsInterpretMethod build()
        {
            JSONObject nleInterpretMethodJson = new JSONObject()
                    .put("method", method) // type: String, required: Yes, default: none
                    .put("version", version) // type: String, required: No, default: 1.0
                    .put("sessionId", sessionId) // type: String, required: No, default: none
                    .put("requestId", requestId) // type: String, required: No, default: random UUID
                    .put("model", model) // type: String, required: Yes, default: none
                    .put("useWordSets", useWordSets) // type: String array, required: No, default: empty
                    .put("inputType", inputType) // type: String, required: No, default: text/plain
                    .put("input", input) // type: String, required: Yes, default: none
                    .put("responseType", responseType) // type: String, required: No, default: application/json, Valid values are "suppress" and "off"
                    .put("sensitive", sensitive); // type: String, required: No, default: configurable

            return new KryptonWsInterpretMethod(nleInterpretMethodJson, kryptonWsSession);
        }
    }
}
