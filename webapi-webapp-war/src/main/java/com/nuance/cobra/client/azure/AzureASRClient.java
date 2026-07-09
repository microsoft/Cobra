package com.nuance.cobra.client.azure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.SpeechRecognitionClients;
import com.nuance.cobra.client.krypton.KryptonUtils;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.parseInt;

public class AzureASRClient extends SpeechRecoClient {
    private final ResponseHandler responseHandler;
    private static final Logger logger = LogManager.getLogger(AzureASRClient.class);
    private static final String CONFIDENCE = "Confidence";

    private SpeechConfig speechConfig;
    private SpeechRecognizer recognizer;
    private PushAudioInputStream pushStream;
    private AudioStreamFormat audioFormat;
    private NoiseLevel noiseLevel = new NoiseLevel(0, 0);

    private boolean isStopDirective = false;
    private boolean statisticsEventTriggered = false;
    private boolean hasStatistics;
    private boolean startOfSpeechWasDetected = false;
    private boolean endOfSpeechWasDetected = false;
    private boolean enableProfanity = false;
    private final AtomicBoolean finalResponseSent = new AtomicBoolean(false);
    private CombinedConfiguration cc;
    private String finalResultsJson = "";

    protected List<AudioNodeObserver> observers = new ArrayList<>();
    private String language;
    private String topic;
    private final List<Map<String, String>> statisticsDlms = new ArrayList<>();
    private final List<Map<String, Object>> statisticsBuiltins = new ArrayList<>();

    private int mcsftDelayProcessMs;

    private int nbSentAudioRequests;
    private Calendar timeAtWhichTheFirstAudioRequestWasSent;
    private Calendar timeAtWhichTheLastAudioRequestWasSent;

    // Enum to represent the state of the recognition process
    protected enum AzureASRState {
        INITIALIZING, // Not ready to stream audio or stop the recognition
        RUNNING, // Ready to stream audio and stop the recognition
        CANCELED, // Recognition process was canceled
        COMPLETED // Recognition process is completed
    }

    public enum RecognitionMode {
        INTERACTIVE("INTERACTIVE"),
        CONVERSATION("CONVERSATION"),
        DICTATION("DICTATION");

        private final String mode;

        RecognitionMode(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
        }

        public static RecognitionMode fromString(String mode) {
            for (RecognitionMode rm : RecognitionMode.values()) {
                if (rm.mode.equalsIgnoreCase(mode)) {
                    return rm;
                }
            }
            logger.error("Unsupported recognition mode:: {}", mode);
            throw new IllegalArgumentException("Unsupported recognition mode: " + mode);
        }
    }

    public enum BestResultFormat {
        LEXICAL("Lexical"),
        ITN("ITN"),
        MASKED_ITN("MaskedITN"),
        DISPLAY("Display");

        private final String value;

        BestResultFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static BestResultFormat fromString(String value) {
            for (BestResultFormat format : BestResultFormat.values()) {
                if (format.value.equalsIgnoreCase(value)) {
                    return format;
                }
            }
            logger.error("Invalid format type: {}", value);
            throw new IllegalArgumentException("Invalid format type: " + value);
        }
    }

    private volatile AzureASRState recognitionState = AzureASRState.INITIALIZING;

    public AzureASRClient(String sessionId, ResponseHandler responseHandler, CombinedConfiguration config, List<DynamicVocabulary> dynamicVocabularies, AzureTokenProvider tokenProvider) {
        super(sessionId);
        int initialSilenceTimeoutMs = config.getInt(WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.key, (Integer) WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.defaultValue);
        String endSilenceTimeoutMs = config.getString(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_END_SILENCE_TIMEOUT_MS.key, (String) WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_END_SILENCE_TIMEOUT_MS.defaultValue);
        String segmentationSilenceTimeoutMs = config.getString(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_SEGMENTATION_SILENCE_TIMEOUT_MS.key, (String) WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_SEGMENTATION_SILENCE_TIMEOUT_MS.defaultValue);
        String speechRecognitionCodec = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, (String) WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.defaultValue);
        String speechRecoLanguage = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, (String) WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.defaultValue);
        String recognitionModeString = config.getString(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_MODE.key, (String) WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_MODE.defaultValue);
        RecognitionMode recognitionMode = RecognitionMode.fromString(recognitionModeString);
        String outputFormat = config.getString(WebApiConfig.Property.MCSFT_SPEECH_OUTPUT_FORMAT.key, (String) WebApiConfig.Property.MCSFT_SPEECH_OUTPUT_FORMAT.defaultValue);
        String serviceRegion = config.getString(WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.key, (String) WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.defaultValue);
        String resourceId = config.getString(WebApiConfig.Property.AZURE_SPEECH_SERVICE_RESOURCE_ID.key, (String)WebApiConfig.Property.AZURE_SPEECH_SERVICE_RESOURCE_ID.defaultValue);
        String sensitivity = config.getString(WebApiConfig.Property.SENSITIVITY.key, (String)WebApiConfig.Property.SENSITIVITY.defaultValue);
        hasStatistics = config.getBoolean(WebApiConfig.Property.STATISTICS.key, (Boolean)WebApiConfig.Property.STATISTICS.defaultValue);
        mcsftDelayProcessMs = config.getInt(WebApiConfig.Property.MCSFT_SPEECH_PROCESS_DELAY_MS.key, (Integer) WebApiConfig.Property.MCSFT_SPEECH_PROCESS_DELAY_MS.defaultValue);
        AudioCodec speechRecoCodec = AudioCodec.fromString(speechRecognitionCodec);
        int sampleRate = speechRecoCodec.getSampleRate();

        boolean speechRecognitionEnableProfanity = config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key, (Boolean) WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.defaultValue);
        boolean kryptonRecognitionEnableProfanity = config.getBoolean(WebApiConfig.Property.KRYPTON_ENABLE_PROFANITY_FILTER.key, (Boolean) WebApiConfig.Property.KRYPTON_ENABLE_PROFANITY_FILTER.defaultValue);

        // Set speech recognition audio format
        switch (sampleRate) {
            case 8000:
                audioFormat = AudioStreamFormat.getWaveFormatPCM(8000L, (short) 16, (short) 1);
                break;
            case 16000:
                audioFormat = AudioStreamFormat.getWaveFormatPCM(16000L, (short) 16, (short) 1);
                break;
            case 22050:
                audioFormat = AudioStreamFormat.getWaveFormatPCM(22050L, (short) 16, (short) 1);
                break;
            case 24000:
                audioFormat = AudioStreamFormat.getWaveFormatPCM(24000L, (short) 16, (short) 1);
                break;
            default:
                logger.error("Unsupported sample rate: {}", sampleRate);
                throw new IllegalArgumentException("Unsupported sample rate: " + sampleRate);
        }
        logger.info("Setting language: {}", speechRecoLanguage);

        this.responseHandler = responseHandler;

        String accessToken = tokenProvider.getAccessToken();
        String authorizationToken = "aad#" + resourceId + "#" + accessToken;

        this.speechConfig = SpeechConfig.fromAuthorizationToken(authorizationToken, serviceRegion);

        setAudioLogging(sensitivity);
        setProfanityOptions(speechRecognitionEnableProfanity, kryptonRecognitionEnableProfanity);
        setOutputFormat(outputFormat);
        setTimeoutProperties(initialSilenceTimeoutMs, endSilenceTimeoutMs, segmentationSilenceTimeoutMs);

        this.speechConfig.setSpeechRecognitionLanguage(speechRecoLanguage);
        this.speechConfig.setProperty(PropertyId.SpeechServiceConnection_RecoMode, recognitionMode.getMode());

        this.pushStream =  AudioInputStream.createPushStream(audioFormat);
        this.recognizer = new SpeechRecognizer(speechConfig, AudioConfig.fromStreamInput(pushStream));

        // Populating phrase list and add to recognizer to improve accuracy of speech recognition
        List<String> phraseList = createPhraseList(dynamicVocabularies);
        if(!phraseList.isEmpty()){
            PhraseListGrammar phraseListGrammar = PhraseListGrammar.fromRecognizer(recognizer);
            for (String phrase : phraseList) {
                phraseListGrammar.addPhrase(phrase);
            }
        }

        this.cc = config;

        language = KryptonUtils.getLanguage(config);
        topic = KryptonUtils.getTopic(config);
        if (topic == null) {
            topic = "";
        }

        recognizer.startContinuousRecognitionAsync();
        this.setupSpeechRecognitionEventListeners();
    }


    private void setAudioLogging(String sensitivity) {
        if (sensitivity.equalsIgnoreCase(Command.CommandSensitivityType.OPEN.name())) {
            this.speechConfig.enableAudioLogging();
        } else {
            this.speechConfig.setProperty(PropertyId.SpeechServiceConnection_EnableAudioLogging, "false");
        }
        logger.info("The audio logging value is set to: {}", speechConfig.getProperty(PropertyId.SpeechServiceConnection_EnableAudioLogging));
    }

    private void setProfanityOptions(boolean speechRecognitionEnableProfanity, boolean kryptonRecognitionEnableProfanity) {
        if (speechRecognitionEnableProfanity || kryptonRecognitionEnableProfanity) {
            logger.info("Masking the data because profanity is set to true...");
            enableProfanity = true;
            this.speechConfig.setProfanity(ProfanityOption.Masked);
            this.speechConfig.setProperty(PropertyId.SpeechServiceResponse_ProfanityOption, "Masked");
        }
    }

    private void setOutputFormat(String outputFormat) {
        if (outputFormat.equalsIgnoreCase("detailed")) {
            this.speechConfig.setOutputFormat(OutputFormat.Detailed);
        } else {
            this.speechConfig.setOutputFormat(OutputFormat.Simple);
        }
    }

    private void setTimeoutProperties(int initialSilenceTimeoutMs, String endSilenceTimeoutMs, String segmentationSilenceTimeoutMs) {
        if (checkPositiveNumber(endSilenceTimeoutMs)) {
            this.speechConfig.setProperty(PropertyId.SpeechServiceConnection_EndSilenceTimeoutMs, endSilenceTimeoutMs);
        } else {
            logger.info("Invalid end silence timeout value: {}", endSilenceTimeoutMs);
        }

        if (checkPositiveNumber(String.valueOf(initialSilenceTimeoutMs))) {
            logger.info("Setting initial silence timeout to {}", initialSilenceTimeoutMs * 1000);
            speechConfig.setProperty(PropertyId.SpeechServiceConnection_InitialSilenceTimeoutMs, String.valueOf(initialSilenceTimeoutMs * 1000));
            speechConfig.setProperty(PropertyId.Conversation_Initial_Silence_Timeout, String.valueOf(initialSilenceTimeoutMs * 1000));
        } else {
            logger.info("Initial silence timeout value is unlimited, meaning 60 seconds");
            speechConfig.setProperty(PropertyId.SpeechServiceConnection_InitialSilenceTimeoutMs, "60000");
            speechConfig.setProperty(PropertyId.Conversation_Initial_Silence_Timeout, "60000");
        }

        if (checkPositiveNumber(segmentationSilenceTimeoutMs)) {
            this.speechConfig.setProperty(PropertyId.Speech_SegmentationSilenceTimeoutMs, segmentationSilenceTimeoutMs);
        } else {
            logger.info("Invalid segmentation timeout value: {}", segmentationSilenceTimeoutMs);
        }
    }

    @Override
    public void startSpeechRecognition() {
        logger.info("Azure Cog Client starting speech reco...");
        recognitionState = AzureASRState.RUNNING;
        setStatistics(SpeechRecognitionClients.SPEECH_SERVICE_ASR.getValue(), language, topic, statisticsDlms, statisticsBuiltins);
        onEngineReady();
    }

    @Override
    public void stopSpeechRecognition() {
        isStopDirective = true;
        stopRecognition();
    }

    @Override
    public void cancelSpeechRecognition(boolean isFinal) { //isFinal is always true
        synchronized (finalResponseSent) {
            if (finalResponseSent.compareAndSet(false, true)) {
                stopRecognition();
                recognitionState = AzureASRState.CANCELED;
                responseHandler.onResponse(new SpeechRecoResponse(isFinal, ResponseResult.ResponseStatus.CANCELLED, noiseLevel));
            }
        }
    }

    @Override
    public void startRecognitionTimers() {
        // Not implemented for microsoft asr
    }

    @Override
    public boolean setRecResultType(RecResultType recResultType) {
        return false;
    }

    @Override
    public float getMaxConfidence() {
        return 1000.0f;
    }

    @Override
    public void onAudio(AudioChunk chunk) {
        if (chunk == null) {
            logger.trace("Received null AudioChunk in onAudio method.");
            return;
        }
        try {
            if(recognitionState != AzureASRState.COMPLETED) {
                logger.info("Sending audio chunk to pushStream...");
                sendAudioRequest(chunk);
            }
        } catch (Exception e) {
            logger.trace("Exception occurred while writing to pushStream: {}", e.getMessage(), e);
        }
    }

    @Override
    public void finish() {
        // Not implemented for microsoft asr
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        switch(event){
            case NEW_NOISE_LEVEL_CALCULATED:
                noiseLevel = ((SpeechDetector) eventOwner).getCalculatedBackgroundNoiseLevel();
                break;
            case START_OF_SPEECH:
                logger.info("Cobra start of speech detected");
                startOfSpeechWasDetected = true;
                break;
            case END_OF_SPEECH:
                endOfSpeechWasDetected = true;
                // Add a delay before stopping recognition for microsoft to process the remaining chunks of audio...
                logger.info("adding small delay to process all audio from microsoft delay value is: {}", mcsftDelayProcessMs);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopRecognition();
                    }
                }, mcsftDelayProcessMs);
                break;
            case TIMEOUT:
                handleTimeout(eventOwner);
                break;
            case AUDIO_STATISTICS:
                handleAudioStatistics(eventOwner);
                break;
            default:
                logger.info("Unhandled event {}", event);
                break;
        }
    }

    private void handleTimeout(AudioNode eventOwner) {
        if (eventOwner instanceof TimeoutNode) {
            String timerId = ((TimeoutNode) eventOwner).getTimerID();
            logger.debug("onEvent() TIMEOUT {}", timerId);
            if(timerId.equals(START_SPEECH_TIMER_ID)) {
                recognitionState = AzureASRState.COMPLETED;
                finalResponseSent.set(true); // NoInput response was sent to the client by the audio pipeline
            }
            else if(timerId.equals(MAX_SPEECH_TIMER_ID)) {
                logger.info("MAX_SPEECH_TIMER_ID");
                stopSpeechRecognition();
            }
        }
    }

    private void handleAudioStatistics(AudioNode eventOwner) {
        statisticsEventTriggered = true;
        if (eventOwner instanceof StatisticsCalculatorNode && hasStatistics) {
            Map<String, Object> audioStatistics = ((StatisticsCalculatorNode) eventOwner).getStatistics(StatisticsCalculatorNode.ALL_STATISTICS);
            Session session = SessionManager.getInstance().getSession(getSessionId(), false);
            if (session == null) {
                logger.info("Session is null.");
                return;
            }
            processAudioStatistics(audioStatistics, session);
            handleFinalResults();
        }
    }

    private void processAudioStatistics(Map<String, Object> audioStatistics, Session session) {
        logger.info("Adding media duration / media size statistics if possible...");
        if (!audioStatistics.isEmpty()) {
            Integer mediaDuration = (Integer) audioStatistics.get(StatisticsCalculatorNode.STATISTIC_MEDIA_DURATION);
            Integer mediaSize = (Integer) audioStatistics.get(StatisticsCalculatorNode.STATISTIC_MEDIA_SIZE);

            Command command = session.getCommand();
            logger.info("The command statistic: {}", audioStatistics);
            if (command != null) {
                updateCommandStatistics(command.getStatistics(), mediaDuration, mediaSize);
            }
        }
    }

    private void updateCommandStatistics(Command.CommandStatistics commandStatistics, Integer mediaDuration, Integer mediaSize) {
        if (commandStatistics != null) {
            if (mediaDuration != null) {
                commandStatistics.mediaDuration = mediaDuration;
            }
            if (mediaSize != null) {
                commandStatistics.mediaSize = mediaSize;
            }
        }
    }

    private void handleFinalResults() {
        logger.info("Final results json: {}", finalResultsJson);
        if (!finalResultsJson.isEmpty()) {
            synchronized (finalResponseSent) {
                logger.info("In the process of sending final results, finalResponseSent value is: {}", finalResponseSent.get());
                if (finalResponseSent.compareAndSet(false, true)) {
                    logger.info("In the statistics event listeners... sending final results: {}", finalResultsJson);
                    SpeechRecoResponse finalResponse = createSpeechRecoResponseFromJson(finalResultsJson);
                    responseHandler.onResponse(finalResponse);
                    cleanUpResources();
                }
            }
        }
    }


    private boolean checkPositiveNumber(String num){
        int value = parseInt(num);
        return value > 0;
    }

    // Handle the output of the speech recognition process.
    private void setupSpeechRecognitionEventListeners() {
        recognizer.speechStartDetected.addEventListener((s, e) ->
                logger.info("Speech recognition start detected by microsoft cog services...")
        );

        recognizer.speechEndDetected.addEventListener((s, e) ->
                logger.info("Speech recognition end detected...")
        );

        // Final results
        recognizer.recognized.addEventListener((s, e) -> handleFinalResults(e));

        // Partial results
        recognizer.recognizing.addEventListener((s, e) -> handlePartialResults(e));

        recognizer.canceled.addEventListener((s, e) -> handleCanceledResults(e));

        recognizer.sessionStarted.addEventListener((s, e) -> {
                    logger.info("Speech recognition session started");
                    logger.info("My session id: {}", e.getSessionId());
                }
        );

        recognizer.sessionStopped.addEventListener((s, e) -> {
                    logger.info("Speech recognition session stopped");
                    //If stop directive activates quickly, no audio stream inside the recognizer and no final response sent, send a no input as final response.
                    synchronized (finalResponseSent) {
                        if(finalResponseSent.compareAndSet(false, true)) {
                            logger.info("Sending final response in session stopped event listener..");
                            SpeechRecoResponse finalResponse;
                            if(startOfSpeechWasDetected){
                                finalResponse = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_MATCH, noiseLevel);
                            } else {
                                finalResponse = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_INPUT, noiseLevel);
                            }
                            responseHandler.onResponse(finalResponse);
                            cleanUpResources();
                        }
                    }
                }
        );

    }

    private void handleFinalResults(SpeechRecognitionEventArgs e) {
        logger.info("We are in the Handle Final results");
        logger.info("In the final results, the reason is: {}", e.getResult().getReason());
        if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
            finalResultsJson = e.getResult().getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult);
            if(hasStatistics){
                setAudioStreamingToEngineStatistics(0, nbSentAudioRequests, timeAtWhichTheFirstAudioRequestWasSent, timeAtWhichTheLastAudioRequestWasSent);
            }
            if(!startOfSpeechWasDetected && !endOfSpeechWasDetected && !isStopDirective) {
                return;
            }
            logger.info("The hasStatistics value is : {}", hasStatistics);
            logger.info("The statisticsEventTriggered value is : {}", statisticsEventTriggered);
            synchronized (finalResponseSent) {
                if((!hasStatistics || statisticsEventTriggered) && finalResponseSent.compareAndSet(false, true)) {
                    logger.info("Detailed final results: {}", finalResultsJson);
                    logger.info("Sending final response..");
                    SpeechRecoResponse finalResponse = createSpeechRecoResponseFromJson(finalResultsJson);
                    logger.info("Speech recognition response: {} ", finalResponse);
                    responseHandler.onResponse(finalResponse);
                    cleanUpResources();
                }
            }
        } else if (e.getResult().getReason() == ResultReason.NoMatch){
            handleNoMatch();
        }
    }

    private void handlePartialResults(SpeechRecognitionEventArgs e) {
        logger.info("We are in the partial event listener..");
        logger.info("The reason in the partial results event listener is: {}", e.getResult().getReason());
        if (e.getResult().getReason() == ResultReason.RecognizingSpeech) {
            String partialText = e.getResult().getText();
            logger.info("Partial recognition: {}", partialText);
        }   else if (e.getResult().getReason() == ResultReason.NoMatch) {
            logger.info("NOMATCH: Speech could not be recognized.");
        }

        boolean shouldSendPartialResults = cc.getBoolean(WebApiConfig.Property.WORD_STREAM.key, (Boolean) WebApiConfig.Property.WORD_STREAM.defaultValue);
        if(shouldSendPartialResults){
            SpeechRecoResponse partialResponse = new SpeechRecoResponse(false, ResponseResult.ResponseStatus.SUCCESS, noiseLevel);
            responseHandler.onResponse(partialResponse);
        }
    }

    private void handleCanceledResults(SpeechRecognitionCanceledEventArgs e) {
        logger.info("We are in the cancelled event listener..");
        logger.info("The reason in the cancelled event listener is: {}", e.getResult().getReason());
        if (e.getResult().getReason() == ResultReason.Canceled) {
            synchronized (finalResponseSent) {
                if (finalResponseSent.compareAndSet(false, true)) {
                    SpeechRecoResponse canceledResponse = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.CANCELLED, noiseLevel);
                    // Handle TIMEOUT or CANCELED events
                    CancellationReason reason = e.getReason();
                    logger.warn("Recognition canceled: {}", reason);
                    if (reason == CancellationReason.Error) {
                        logger.error("Cancellation error: {}", e.getErrorCode());
                        String errorMessage = "Recognition canceled due to error: " + e.getErrorCode();
                        logger.error("ERROR MSG: {}", errorMessage);
                        responseHandler.onResponse(canceledResponse);
                        recognitionState = AzureASRState.CANCELED;
                    }
                    cleanUpResources();
                }

            }
        }

    }

    private void handleNoMatch( ) {
        synchronized (finalResponseSent) {
            logger.info("No speech could be recognized for final results...");
            if (finalResponseSent.compareAndSet(false, true)) {
                SpeechRecoResponse finalResponseNoMatch = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_MATCH, noiseLevel);
                responseHandler.onResponse(finalResponseNoMatch);
                cleanUpResources();
            }
        }
    }

    private SpeechRecoResponse createSpeechRecoResponseFromJson(String jsonText) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(jsonText);
        } catch (IOException e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
            return null;
        }

        JsonNode nBestArray = rootNode.path("NBest");

        JsonNode bestResultNode = nBestArray.get(0);
        String bestResultFormatConfig = cc.getString(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_BEST_RESULT_FORMAT.key, (String) WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_BEST_RESULT_FORMAT.defaultValue);
        if(enableProfanity) {
            bestResultFormatConfig = BestResultFormat.MASKED_ITN.getValue();
        }
        BestResultFormat bestResultFormat = BestResultFormat.fromString(bestResultFormatConfig);
        String bestResultText = bestResultNode.path(bestResultFormat.getValue()).asText();
        double bestResultConfidence = bestResultNode.path(CONFIDENCE).asDouble();
        logger.info("The best result confidence is: {}", bestResultConfidence);
        double confidenceThreshold = cc.getDouble(WebApiConfig.Property.MICROSOFT_SPEECH_CONFIDENCE_THRESHOLD.key, (Double) WebApiConfig.Property.MICROSOFT_SPEECH_CONFIDENCE_THRESHOLD.defaultValue);
        logger.info("The confidenceThreshold is: {}", confidenceThreshold);
        if (startOfSpeechWasDetected && (bestResultText.isEmpty() || bestResultConfidence < confidenceThreshold)) {
            logger.info("There is no match...");
            return new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_MATCH, noiseLevel);
        } else if (!startOfSpeechWasDetected && bestResultText.isEmpty()) {
            return new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_INPUT, noiseLevel);
        }

        SpeechRecoResponse.Data data = extractDataFromJson(rootNode);

        SpeechRecoResponse finalResponse = new SpeechRecoResponse(true, data, noiseLevel);
        finalResponse.setBestResult(bestResultText);

        boolean hasVerboseResult = cc.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.key, (Boolean)WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.defaultValue);
        if(hasVerboseResult){
            // convert JSON string to Map
            try{
                Map<String, Object> verboseMicrosoftFinalResults = mapper.readValue(jsonText, new TypeReference<>() {});
                finalResponse.setVerboseResult(verboseMicrosoftFinalResults);
            }catch (IOException e) {
                logger.error("Error converting JSON to Map: {}", e.getMessage(), e);
            }
        }

        return finalResponse;
    }

    private SpeechRecoResponse.Data extractDataFromJson(JsonNode rootNode) {
        int maxSize = cc.getInt(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key, (int) WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.defaultValue);
        int numTranscriptions = (maxSize <= 0) ? 1 : maxSize;

        String[] lexicalArray = new String[numTranscriptions];
        int[] overallConfidenceArray = new int[numTranscriptions];
        SpeechRecoResponse.Word[][] wordsArray = new SpeechRecoResponse.Word[numTranscriptions][];

        JsonNode nBestArray = rootNode.path("NBest");
        JsonNode bestResultNode = nBestArray.get(0);
        int topConfidence = (int) (bestResultNode.path(CONFIDENCE).asDouble() * 1000);

        for (int i = 0; i < numTranscriptions; i++) {
            JsonNode resultNode = nBestArray.get(i);
            String resultFormat = resultNode.path(BestResultFormat.LEXICAL.getValue()).asText();
            if(enableProfanity){
                resultFormat = resultNode.path(BestResultFormat.MASKED_ITN.getValue()).asText();
            }

            JsonNode wordsNode = resultNode.path("Words");
            int numWords = wordsNode.size();
            SpeechRecoResponse.Word[] words = new SpeechRecoResponse.Word[numWords];

            double sumConfidences = 0;
            for (int j = 0; j < numWords; j++) {
                JsonNode wordNode = wordsNode.get(j);
                String wordText = wordNode.path("Word").asText();
                int wordConfidenceInt = (int)(wordNode.path(CONFIDENCE).asDouble() * 1000);
                sumConfidences += wordConfidenceInt;
                words[j] = new SpeechRecoResponse.Word(wordText, wordConfidenceInt);
            }

            lexicalArray[i] = resultFormat;

            // Calculate the average weighted confidence
            int averageWeightedConfidence = (int) (sumConfidences / numWords);
            overallConfidenceArray[i] = averageWeightedConfidence;

            wordsArray[i] = words;
        }

        return new SpeechRecoResponse.Data(
                lexicalArray,
                overallConfidenceArray,
                topConfidence,
                wordsArray,
                maxSize
        );
    }

    private void cleanUpResources() {
        try {
            if (recognizer != null) {
                recognizer.close();
            }
            if (speechConfig != null) {
                speechConfig.close();
            }
            if (pushStream != null) {
                pushStream.close();
            }
        } catch (Exception e) {
            logger.error("Error occurred while closing resources: {}", e.getMessage(), e);
        }
    }

    private void stopRecognition() {
        logger.info("Stopping recognition to generate final results from the event listener.");
        recognizer.stopContinuousRecognitionAsync();
        recognitionState = AzureASRState.COMPLETED;
    }

    public NoiseLevel getNoiseLevel(){
        return noiseLevel;
    }

    @Override
    public void setObservers(List<AudioNodeObserver> observers) {
        this.observers = observers;
    }

    protected void onEngineReady() {
        var session = SessionManager.getInstance().getSession(getSessionId(), true);
        if (session != null && session.getCommand() != null) {
            session.getCommand().getStatistics().addEngineResponse("engineReady", Map.of("timestamp", LoggingUtils.getUTCFormattedTimestamp()));
        }
    }

    public static List<String> createPhraseList(List<DynamicVocabulary> dynamicVocabularies) {
        List<String> phraseList = new ArrayList<>();
        if(dynamicVocabularies != null) {
            for (DynamicVocabulary vocab : dynamicVocabularies) {
                if (vocab.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET) {
                    Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body = vocab.getBody();
                    for (Map.Entry<String, ArrayList<DynamicVocabulary.Vocabulary>> entry : body.entrySet()) {
                        for (DynamicVocabulary.Vocabulary vocabulary : entry.getValue()) {
                            phraseList.add(vocabulary.getLiteral());
                        }
                    }
                }
            }
        }

        return phraseList;
    }

    private synchronized void sendAudioRequest(AudioChunk chunk) {
        try {
            if (pushStream != null) {
                pushStream.write(chunk.getBytes());
                logger.info("Pushing bytes to stream, size: {}", chunk.getBytes().length);
                nbSentAudioRequests++;
                refreshAudioStreamingStatistics();
            }
        } catch (Exception e) {
            logger.trace("Exception occurred while writing to pushStream: {}", e.getMessage(), e);
        }
    }

    private void refreshAudioStreamingStatistics() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        if (timeAtWhichTheFirstAudioRequestWasSent == null) {
            timeAtWhichTheFirstAudioRequestWasSent = currentTime;
        }
        timeAtWhichTheLastAudioRequestWasSent = currentTime;
    }
}
