package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.nle.NLEConverter;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.*;
import com.nuance.plugin.kryptonws.response.KryptonWsEvent;
import com.nuance.plugin.kryptonws.response.KryptonWsEventBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage.STATUS;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.utils.KryptonWsSessionObjectsHelper;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.nuance.plugin.kryptonws.response.KryptonWsMessage.STATUS.*;
import static com.nuance.plugin.kryptonws.utils.KryptonWsSessionObjectsHelper.WORDSET_NLU_PKG_TYPE;

public class KryptonWsRecognizeMethod
        extends KryptonWsMethod
{
    private static final Logger logger = LogManager.getLogger(KryptonWsRecognizeMethod.class);
    public static final String VERBOSE_RESULT = "verboseResult";
    public static final String DATA_PACK = "dataPack";

    private ScheduledFuture<?> recognitionTimerFuture = null;

    public static final String NAME = "Recognize";
    public static final String LANGUAGE = "language";
    protected static final String USE_WORD_SETS_KEY = "useWordSets";

    public static final String SPEECH_RECOGNITION = "speechRecognition";

    public static final String START_OF_SPEECH = "startOfSpeech";

    public static final String RECOGNITION_PARAMATERS = "recognitionParameters";

    public final AtomicBoolean recognitionWasStopped = new AtomicBoolean(false);

    private WebApiResponse lastReceivedPartialResult;
    private KryptonWsEventBuilder recognizeFinalResultBuilder;

    protected Calendar interpretationStartTime;

    public KryptonWsRecognizeMethod(JSONObject kryptonWsMethod, KryptonWsSession kryptonWsSession)
    {
        super(kryptonWsMethod, kryptonWsSession);
        logger.debug("KryptonWsRecognizeMethod constructor");
    }

    @Override
    public void process()
    {
        try
        {
            super.process();

            CombinedConfiguration webapiCombinedConfig = kryptonWsSession.getWebapiSessionConfig();

            JSONObject kryptonWsRecoParamsJson = methodJson.optJSONObject(RECOGNITION_PARAMATERS);
            kryptonWsSession.getKryptonWsParameters().recognizeAddConfig(kryptonWsRecoParamsJson, webapiCombinedConfig);

            String wapiSessionId = kryptonWsSession.getWebapiSessionId();
            String wapiRequestId = Integer.toString(kryptonWsSession.getNextWebapiStepId());
            WebapiSpeechRecognitionCommandBuilder wapiCommandBuilder = new WebapiSpeechRecognitionCommandBuilder(wapiSessionId, wapiRequestId);

            setLanguageAndTopic(wapiCommandBuilder);

            wapiCommandBuilder.setParameters(kryptonWsSession.getKryptonWsParameters().getConfigurations());
            wapiCommandBuilder.speechRecognitionVerboseResult(true);
            wapiCommandBuilder.statistics(areMetricsEnabled);

            JSONArray useWordSets = getWordSets();
            if (useWordSets == null) {
                sendResponse(FAILED);
                return;
            }

            // Set dlms and weights in the session config based on loaded objects
            KryptonWsSessionObjectsHelper sessionHelper = new KryptonWsSessionObjectsHelper(kryptonWsSession);
            JSONObject dlmWeights = methodJson.optJSONObject("domainLmWeights");
            if(dlmWeights == null) {
                dlmWeights = new JSONObject();
            }
            sessionHelper.setSessionObjectsForSpeechRecognition(dlmWeights.toMap());

            JSONObject builtinWeights = methodJson.optJSONObject("builtinWeights");
            JSONArray usedBuiltins = new JSONArray();
            WebapiUploadDynamicVocabularyCommandBuilder vocabularyCommandBuilder = null;
            if(builtinWeights != null && !builtinWeights.keySet().isEmpty()) {
                vocabularyCommandBuilder = new WebapiUploadDynamicVocabularyCommandBuilder(wapiSessionId, wapiRequestId);
                JSONArray builtins = getBuiltins(builtinWeights, usedBuiltins);
                vocabularyCommandBuilder.dynamicVocabularies(builtins);
            }
            else {
                builtinWeights = new JSONObject();
            }

            if(areMetricsEnabled) {
                Map<String, Object> metrics = buildCommonMetrics();
                metrics.put(LANGUAGE, kryptonWsSession.getCreateSessionLanguagePack().getString(LANGUAGE));
                metrics.put("wordsets", useWordSets.toList().toArray(new String[0]));
                metrics.put("weights", dlmWeights.toMap());
                metrics.put("builtins", builtinWeights.toMap());
                kryptonWsResponseBuilder.setMetrics(metrics);
            }

            setActiveDynamicVocabularies(wapiCommandBuilder, useWordSets, usedBuiltins);

            if (webapiCombinedConfig.getBoolean(KryptonWsStartRecognitionTimersMethod.PARAM_NAME))
            {
                startRecognitionTimer();
            } else
            {
                //Thwart no-audio timer when startRecognitionTimers: false
                kryptonWsSession.getKryptonWsParameters().processThwartNoAudioTimer(kryptonWsSession, webapiCombinedConfig);
            }

            sendInitialResponse(true);

            JSONObject wapiCommandJson = createWapiCommand(wapiCommandBuilder, vocabularyCommandBuilder);
            String wapiCommandString = wapiCommandJson.toString();

            sendToWebapi(wapiCommandString);
        }
        catch(ConversionException | IllegalArgumentException | JSONException e) {
            kryptonWsResponseBuilder.addError(e.getMessage(), null);
            sendResponse(FAILED);
        }
        catch (Exception e)
        {
            logger.error("Error while parsing KryptonWs Recognize method", e);

            sendInitialResponse(false);
        }
    }

    private JSONArray getBuiltins(JSONObject builtinWeights, JSONArray usedBuiltins) {
        JSONArray builtins = new JSONArray();
        for(Map.Entry<String, Object> entry : builtinWeights.toMap().entrySet()) {
            JSONObject builtin = new JSONObject();
            String vocabId = entry.getKey();
            builtin.put("id", vocabId);
            builtin.put("builtin", entry.getKey());
            builtin.put("weight", entry.getValue());
            builtin.put("type", "application/x-nuance-builtin");
            usedBuiltins.put(vocabId);
            builtins.put(builtin);
        }
        return builtins;
    }

    private JSONArray getWordSets() {
        JSONArray filteredUseWordSets = new JSONArray();
        if (methodJson.has(USE_WORD_SETS_KEY))
        {
            JSONArray useWordSets = methodJson.getJSONArray(USE_WORD_SETS_KEY);

            for(Object wordSetObject : useWordSets) {
                String wordSetId = (String)wordSetObject;
                if(!kryptonWsSession.getWordSetIds().contains(wordSetId)) {
                    kryptonWsResponseBuilder.addError("Wordset ID not found", wordSetId);
                    return null;
                } else {
                    addWordset(filteredUseWordSets, wordSetId, WORDSET_NLU_PKG_TYPE);
                }
            }
        }

        return filteredUseWordSets;
    }

    private JSONObject createWapiCommand(WebapiSpeechRecognitionCommandBuilder wapiCommandBuilder, WebapiUploadDynamicVocabularyCommandBuilder vocabularyCommandBuilder) {
        if(vocabularyCommandBuilder != null) {
            var commandChainBuilder = new WebapiCommandChainBuilder();
            commandChainBuilder.addCommand(vocabularyCommandBuilder);
            commandChainBuilder.addCommand(wapiCommandBuilder);
            return commandChainBuilder.buildRequest();
        }
        else {
            return wapiCommandBuilder.buildRequest();
        }
    }

    private void setActiveDynamicVocabularies(WebapiSpeechRecognitionCommandBuilder wapiCommandBuilder, JSONArray useWordSets, JSONArray usedBuiltins) {
        if (useWordSets.length() > 0 || usedBuiltins.length() > 0)
        {
            for(Object builtin : usedBuiltins) {
                useWordSets.put(builtin);
            }
            wapiCommandBuilder.activeDynamicVocabularySets(useWordSets.toList().toArray(new String[0]));
        }
    }

    void setLanguageAndTopic(WebapiSpeechRecognitionCommandBuilder webapiSpeechRecognitionCommandBuilder)
    {
        JSONObject languagePack = kryptonWsSession.getCreateSessionLanguagePack();
        webapiSpeechRecognitionCommandBuilder.language(languagePack.getString(LANGUAGE));

        String topic = languagePack.optString("topic", null);
        if(topic != null)
        {
            webapiSpeechRecognitionCommandBuilder.topic(topic);
        }
    }

    @Override
    protected void onAudioImplementation(byte[] audio)
    {
        logger.trace("Recognize received {} audio bytes", audio.length);
        sendAudioToWebapi(audio);
    }

    @Override
    public void processWebapiResponse(String wapiResponseString)
    {
        logger.info("Got webapi response: {}", wapiResponseString);

        WebApiResponse wapiResponse;

        try
        {
            wapiResponse = WebApiResponse.fromJson(wapiResponseString);
        }
        catch (JsonProcessingException e)
        {
            String errorMessage = "Unable to parse webapi response json";
            logger.error("{}: {}", errorMessage, wapiResponseString);
            sendErrorResponse(errorMessage, null);
            cleanup();
            return;
        }

        if (wapiResponse.command == null || wapiResponse.command.get("name").equals(SPEECH_RECOGNITION))
        {
            if (wapiResponse.isResult()) {
                processSpeechRecoResults(wapiResponse);
            } else if (wapiResponse.isEvent(START_OF_SPEECH)) {
                KryptonWsEventBuilder wsEventBuilder = KryptonWsEventBuilder.buildEventForMethod(KryptonWsEvent.EVENT.START_OF_SPEECH, this);
                addEventMetrics(wapiResponse, wsEventBuilder, false);
                kryptonWsSession.getKryptonWsResponseSender().sendResponse(wsEventBuilder.build().toJson());
            }
        }
        else if (wapiResponse.command.get("name").equals("interpretation") &&
                wapiResponse.isResult() && wapiResponse.isFinal())
        {
            KryptonWsEventBuilder kryptonWsEventBuilder = populateRecognizeEventWithInterpretationResult(wapiResponseString, wapiResponse, recognizeFinalResultBuilder, areMetricsEnabled, interpretationStartTime);
            String kryptonWsEventString = kryptonWsEventBuilder.build().toJson();
            kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsEventString);
            sendCompleteResponse();
            cleanup();
        }
        else if (wapiResponse.command.get("name").equals("uploadDynamicVocabulary") &&
                !wapiResponse.isSuccess()) {
            logger.error("Failed to set builtins {}", wapiResponse.getErrors());
        }
    }

    static KryptonWsEventBuilder populateRecognizeEventWithInterpretationResult(String wapiResponseString,
                                                                                WebApiResponse wapiResponse,
                                                                                KryptonWsEventBuilder recognizeFinalResultBuilder,
                                                                                boolean areMetricsEnabled,
                                                                                Calendar interpretationStartTime)
    {
        if (wapiResponse.isSuccess())
        {
            JSONObject nleInterpretations = NLEConverter.convertWebapiInterpretationToNle(wapiResponse, true);

            recognizeFinalResultBuilder.setInterpretationStatus(COMPLETE.statusString)
                    .setInterpretationData(nleInterpretations.toMap());

            // If NLU intent is no match, make asr final completion cause no match
            if(isNluIntentionNoMatch(wapiResponse)){
                recognizeFinalResultBuilder.setCause(KryptonWsMessage.COMPLETION_CAUSE.NO_MATCH);
            }

            if(areMetricsEnabled) {
                recognizeFinalResultBuilder.setInterpretationMetrics(KryptonWsInterpretMethod.buildInterpretationMetrics(wapiResponse, interpretationStartTime));
            }
        }
        else if (wapiResponse.isCanceled()) // not supposed to ever happen
        {
            Map<String, Object> errors = new HashMap<>();
            errors.put("message", "Command was cancelled.");

            recognizeFinalResultBuilder.setInterpretationStatus(FAILED.statusString)
                    .setInterpretationErrors(List.of(errors));
        }
        else if (wapiResponse.isError())
        {
            recognizeFinalResultBuilder.setInterpretationStatus(FAILED.statusString)
                    .setInterpretationErrors(wapiResponse.getErrors());
        }
        return recognizeFinalResultBuilder;
    }

    private void processSpeechRecoResults(WebApiResponse wapiResponse) {
        JSONObject kryptonWsRecoParamsJson = methodJson.optJSONObject(RECOGNITION_PARAMATERS);

        if (wapiResponse.isSuccess())
        {
            processSpeechRecoSuccessResponse(wapiResponse);
        }
        else if (wapiResponse.isError())
        {
            stopRecognitionTimer();
            sendErrorResponseFromWebapiResponseObject(wapiResponse);
            cleanup();
        }
        else if (wapiResponse.isCanceled())
        {
            stopRecognitionTimer();
            kryptonWsResponseBuilder.setCompletionCause(KryptonWsMessage.COMPLETION_CAUSE.ERROR);
            sendCompleteResponse();
            cleanup();
        }
        else if (wapiResponse.isNoMatch())
        {
            sendNonSuccessFinalResultEvent(KryptonWsMessage.COMPLETION_CAUSE.NO_MATCH, wapiResponse);
            stopRecognitionTimer();
            sendCompleteResponse();
            cleanup();
        }
        else if (kryptonWsRecoParamsJson.has("recognitionTimeout")){
            sendNonSuccessFinalResultEvent(KryptonWsMessage.COMPLETION_CAUSE.RECOGNITION_TIMEOUT, wapiResponse);
            stopRecognitionTimer();
            sendCompleteResponse();
            cleanup();
        }
        else if (wapiResponse.isNoInput())
        {
            boolean autoEnd = kryptonWsRecoParamsJson.optBoolean("autoEnd", true);
            if (autoEnd) {
                sendNonSuccessFinalResultEvent(KryptonWsMessage.COMPLETION_CAUSE.NO_INPUT_TIMEOUT, wapiResponse);
                stopRecognitionTimer();
                sendCompleteResponse();
                cleanup();
            }
        }
    }

    private void processSpeechRecoSuccessResponse(WebApiResponse wapiResponse) {
        KryptonWsEventBuilder kryptonWsEventBuilder = new KryptonWsEventBuilder()
                .setCause(KryptonWsMessage.COMPLETION_CAUSE.SUCCESS)
                .setObjectsFromWebapiResponse(wapiResponse, wapiResponse.isFinal())
                .setStatus(getWapiResponseStatus(wapiResponse))
                .setUtteranceComplete(wapiResponse.isFinal())
                .setEvent(getWapiEventStatus(wapiResponse))
                .setMethod("Event")
                .setRequestId(this.getKryptonWsRequestId())
                .setSessionId(this.getKryptonWsSessionId())
                .setVersion("1.0");

        addEventMetrics(wapiResponse, kryptonWsEventBuilder, true);

        if (!wapiResponse.isFinal())
        {
            lastReceivedPartialResult = wapiResponse;
            String kryptonWsEventString = kryptonWsEventBuilder.build().toJson();
            kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsEventString);
        }
        else
        {
            processSpeechRecoFinalResponse(wapiResponse, kryptonWsEventBuilder);
        }
    }

    private void processSpeechRecoFinalResponse(WebApiResponse wapiResponse, KryptonWsEventBuilder kryptonWsEventBuilder) {
        stopRecognitionTimer();

        JSONObject semanticModel = kryptonWsSession.getSemanticModel();

        if (semanticModel != null) // if should Interpret
        {
            recognizeFinalResultBuilder = kryptonWsEventBuilder;

            KryptonWsInterpretMethod interpretMethod = createInterpretMethod(kryptonWsSession, this, wapiResponse, semanticModel);
            interpretationStartTime = getCurrentUTCTime();
            interpretMethod.process(this);
        }
        else
        {
            addEventMetrics(wapiResponse, kryptonWsEventBuilder, true);
            String kryptonWsEventString = kryptonWsEventBuilder.build().toJson();
            kryptonWsSession.getKryptonWsResponseSender().sendResponse(kryptonWsEventString);
            sendCompleteResponse();
            cleanup();
        }
    }

    static KryptonWsInterpretMethod createInterpretMethod(KryptonWsSession kryptonWsSession,
                                                          KryptonWsRecognizeMethod recognizeMethod,
                                                          WebApiResponse wapiResponse,
                                                          JSONObject semanticModel)
    {
        KryptonWsInterpretMethod.Builder interpretMethodBuilder = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId(kryptonWsSession.getWebapiSessionId())
                .setRequestId(recognizeMethod.kryptonWsRequestId)
                .setModel(semanticModel.getString("id"))
                .setInputType("application/x-nuance-krypton-result")
                .setInput(wapiResponse.result.get(VERBOSE_RESULT))
                .setResponseType("application/plain")
                .setSensitive("off");

        if (recognizeMethod.methodJson.has(USE_WORD_SETS_KEY))
        {
            interpretMethodBuilder.setUseWordSets(recognizeMethod.methodJson.getJSONArray(USE_WORD_SETS_KEY));
        }

        return interpretMethodBuilder.build();
    }

    private void sendNonSuccessFinalResultEvent(KryptonWsMessage.COMPLETION_CAUSE completionCause, WebApiResponse wapiResponse)
    {
        KryptonWsEventBuilder finalResultEventBuilder = new KryptonWsEventBuilder();

        if (lastReceivedPartialResult != null && lastReceivedPartialResult.isSuccess())
        {
            finalResultEventBuilder.setObjectsFromWebapiResponse(lastReceivedPartialResult, true);
        }
        finalResultEventBuilder.setCause(completionCause)
                .setEvent(KryptonWsEvent.EVENT.FINAL_RESULT)
                .setMethod("Event")
                .setRequestId(this.getKryptonWsRequestId())
                .setSessionId(this.getKryptonWsSessionId())
                .setStatus(STATUS.COMPLETE)
                .setUtteranceComplete(true)
                .setVersion("1.0");

        addEventMetrics(wapiResponse, finalResultEventBuilder, true);

        kryptonWsSession.getKryptonWsResponseSender().sendResponse(finalResultEventBuilder.build().toJson());
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    STATUS startRecognitionTimer()
    {
        return startRecognitionTimer(false);
    }

    STATUS startRecognitionTimer(boolean sendStartRecognitionTimersDirective)
    {
        //Uses clock time, instead of sample time

        logger.debug("startRecognitionTimer");
        if (recognitionTimerFuture != null)
        {
            return NO_ACTION;
        }

        Runnable task = () -> {
            // if recognition was NOT stopped
            if (recognitionWasStopped.compareAndSet(false, true))
            {
                logger.debug("Recognition Timer Expired!");
                WebapiStopDirectiveBuilder webapiStopDirectiveBuilder = new WebapiStopDirectiveBuilder(kryptonWsSession.getWebapiSessionId(), kryptonWsSession.getWebapiSession().getCommand().getCommandId(), SPEECH_RECOGNITION);
                JSONObject webapiStopDirectiveJson = webapiStopDirectiveBuilder.buildRequest();
                sendToWebapi(webapiStopDirectiveJson.toString());
            }
        };

        int recognitionTimeout = kryptonWsSession.getWebapiSessionConfig().getInt("utteranceMaxTimeSeconds");

        recognitionTimerFuture = ScheduledTaskUtils.scheduleTask(task, recognitionTimeout, TimeUnit.SECONDS);

        if(sendStartRecognitionTimersDirective)
        {
            String webapiSessionId = kryptonWsSession.getWebapiSessionId();
            ISession webapiSession = kryptonWsSession.getWebapiSession();
            ICommand command = webapiSession.getCommand();
            if(command != null)
            {
                String commandId = command.getCommandId();
                WebapiStartRecognitionTimersDirectiveBuilder webapiStartRecognitionTimersDirectiveBuilder =
                        new WebapiStartRecognitionTimersDirectiveBuilder(webapiSessionId, commandId,
                                SPEECH_RECOGNITION);
                JSONObject webapiStartRecognitionTimersDirectiveJson =
                        webapiStartRecognitionTimersDirectiveBuilder.buildRequest();
                sendToWebapi(webapiStartRecognitionTimersDirectiveJson.toString());
            }
        }

        return COMPLETE;
    }

    public void stopRecognitionTimer()
    {
        logger.debug("stopRecognitionTimer");
        if (recognitionTimerFuture != null)
        {
            recognitionTimerFuture.cancel(false);
            recognitionTimerFuture = null;
        }
    }

    KryptonWsMessage.STATUS getWapiResponseStatus(WebApiResponse wapiResponse) {
        return wapiResponse.isFinal() ?
                KryptonWsMessage.STATUS.COMPLETE :
                KryptonWsMessage.STATUS.IN_PROGRESS;
    }

    KryptonWsEvent.EVENT getWapiEventStatus(WebApiResponse wapiResponse) {
        return wapiResponse.isFinal() ?
                KryptonWsEvent.EVENT.FINAL_RESULT :
                KryptonWsEvent.EVENT.PARTIAL_RESULT;
    }

    void addEventMetrics(WebApiResponse webApiResponse, KryptonWsEventBuilder kryptonWsEventBuilder, boolean isResult) {
        if(areMetricsEnabled) {
            Map<String, Object> metrics = buildCommonMetrics();
            if(isResult) {
                metrics.put("media", kryptonWsSession.getKryptonWsParameters().getConfigurations().getString("speechRecognitionCodec"));
                parseStatistics(webApiResponse, metrics);
                parseNotifications(webApiResponse, metrics);

                Map<?, ?> languagePackMetric = getLanguagePackMetric(webApiResponse);
                if (!languagePackMetric.isEmpty()) {
                    metrics.put("versions", Map.of("languagePack", languagePackMetric));
                }
            }
            kryptonWsEventBuilder.setMetrics(metrics);
        }
    }

    private Map<?, ?> getLanguagePackMetric(WebApiResponse webApiResponse) {
        try {
            if (webApiResponse.result.containsKey(VERBOSE_RESULT)) {
                Map<?, ?> verboseResult = (Map<?, ?>) webApiResponse.result.get(VERBOSE_RESULT);

                if (verboseResult.containsKey(DATA_PACK)) {
                    return (Map<?, ?>) verboseResult.get(DATA_PACK);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to parse recognition data pack metrics from Cobra response.", e);
        }
        return new HashMap<>();
    }

    void parseStatistics(WebApiResponse webApiResponse, Map<String, Object> metrics) {
        if(!webApiResponse.result.containsKey("statistics")) {
            return;
        }

        try {
            var statistics = new JSONObject((LinkedHashMap) webApiResponse.result.get("statistics"));
            if (statistics.has("firstAudioBufferReceivedTime")) {
                DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                var firstAudioDate = formatter.parse(statistics.getString("firstAudioBufferReceivedTime"));
                if (statistics.has("firstIntermediateResultTime")) {
                    Date firstResult = formatter.parse(statistics.getString("firstIntermediateResultTime"));
                    metrics.put("firstAudioToResult", firstResult.getTime() - firstAudioDate.getTime());
                }
                if (statistics.has("events")) {
                    var events = statistics.getJSONArray("events");
                    for (var i = 0; i < events.length(); i++) {
                        var event = events.getJSONObject(i);
                        if (event.getString("name").equals(START_OF_SPEECH)) {
                            Date startOfSpeechTime = formatter.parse(event.getString("startTime"));
                            metrics.put("firstAudioToStartOfSpeech", startOfSpeechTime.getTime() - firstAudioDate.getTime());
                        }
                    }
                }
            }
        } catch (ParseException e) {
            logger.error("Couldn't parse webapi response's statistics");
        }
    }

    void parseNotifications(WebApiResponse webApiResponse, Map<String, Object> metrics) {
        try{
            for(var notification : new JSONObject((LinkedHashMap) webApiResponse.result.get(VERBOSE_RESULT)).getJSONArray("notifications")) {
                if(notification instanceof JSONObject &&
                    ((JSONObject) notification).getInt("code") == 1004) {
                    var data = new JSONObject(((JSONObject) notification).getJSONObject("data").getString("x-nuance-supplemental-metrics-result")).getJSONObject("metrics");
                    var weights = data.optJSONObject("weights");
                    if(weights != null) {
                        parseNotificationMetrics("dlm", "effectiveWeights", weights, metrics);
                        parseNotificationMetrics("builtin", "effectiveBuiltins", weights, metrics);
                        parseNotificationMetrics("wordset", "effectiveWordsetWeights", weights, metrics);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Couldn't parse webapi response verbose results' notifications");
        }
    }

    public void parseNotificationMetrics(String key, String metricsKey, JSONObject source, Map<String, Object> metrics) {
        Object value = source.opt(key);
        if(value != null) {
            if(value instanceof JSONObject) {
                metrics.put(metricsKey, ((JSONObject)value).toMap());
            } else if(value instanceof JSONArray) {
                metrics.put(metricsKey, ((JSONArray)value).toList());
            } else {
                metrics.put(metricsKey, value);
            }
        }
    }

    private static boolean isNluIntentionNoMatch(WebApiResponse webApiResponse){
        return ((String) ((Map<String, Object>) webApiResponse.result.get("bestInterpretation")).get("intent")).equalsIgnoreCase("NO_MATCH");
    }
}
