package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.command.GetResourcesCommand.IResources;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import io.prometheus.client.Histogram;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * SpeechRecognition command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/speech-recognition-command.md
 * for documentation.
 *
 */

public class SpeechRecognitionCommand
        extends Command
        implements EDFResponse, AudioNodeObserver
{
    private static final Logger logger = LogManager.getLogger(SpeechRecognitionCommand.class);
    public static final String AUDIO_FILENAME_FORMAT = "%s_%s_%s_raw";

    private ClientFactory clientFactory = new ClientFactory();
    private List<SpeechRecoClient> speechRecoClient = new ArrayList<>();
    private AudioChunkerNode audioChunkerNode = null;
    private final Queue<AudioChunk> audioChunkBuffer = new LinkedList<>();

    private List<AudioNodeObserver> speechRecognitionTimersObservers = new ArrayList<>();

    /**
     * Used to track the state of the command.
     * It is introduced to solve issues related to the audio which continued to be sent to the pipeline
     * after a stop of other event. If needed for other purposes/features, the states could be further refined.
     */
    enum SpeechRecoCommandState {
        INITIAL,
        STREAMING_AUDIO,
        STOPPED,
        COMPLETED
    }

    private SpeechRecoCommandState commandState = SpeechRecoCommandState.INITIAL;

    private NoiseLevel noiseLevel = new NoiseLevel(0, 0);



    /**
     * Prometheus metrics for measuring end of speech to recognition complete of Speech Recognition command.
     * Will only be collected when endPointDetection (speech detection) is enabled.
     */
    protected static final Histogram EOS_TO_REC_COMPLETE_DURATION_HISTOGRAM =
            Histogram.build("webapi_eos_to_reco_complete_duration_seconds",
                    "Speech recognition end of speech to recognition complete duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer endOfSpeechToRecoCompleteHistogramTimer;

    /**
     * Prometheus metrics for measuring start of speech to first intermediate result duration of Speech Recognition command
     * Will only be collected when endPointDetection (speech detection) is enabled.
     */
    protected static final Histogram SOS_TO_FIRST_RESULT_DURATION_HISTOGRAM =
            Histogram.build("webapi_sos_to_first_result_duration_seconds",
                    "Speech recognition start of speech to first intermediate result duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer startOfSpeechToFirstResultHistogramTimer;

    /**
     * Prometheus metrics for measuring the time between the stop directive to to recognition complete
     * Can be used as  a substitute for eoa_to_reco complete when endpointing is not enabled
     */
    protected static final Histogram STOP_DIRECTIVE_TO_RECO_COMPLETE_DURATION_HISTOGRAM =
            Histogram.build("webapi_stop_to_reco_complete_duration_seconds",
                    "Speech recognition stop directive to recognition complete duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer stopDirectiveToRecoCompleteHistogramTimer;

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public SpeechRecognitionCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    // For testing
    @SuppressWarnings("unused")
    void setClientFactory(ClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(ResponseCode... error)
    {
        sendErrorResponse(true, error);
    }

    private void sendErrorResponse(boolean isFinal, ResponseCode... error)
    {
        getErrorHandler(logger).handleError(new SpeechRecoResponse(isFinal, null, error));
    }

    @Override
    public void executeImplementation()
    {
        logger.trace("SpeechRecognitionCommand.execute");
        final var sessionManager = SessionManager.getInstance();
        final var session = sessionManager.getSession(request.sessionId, true);
        logger.debug("SpeechRecognitionCommand.execute -- session: {}", session);
        if (null == session)
        {
            sendErrorResponse(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
            return;
        }
        session.setCommand(this);

        try
        {
            CombinedConfiguration requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);

            logger.trace("SpeechRecognitionCommand.execute -- creating speech reco client");

            List<DynamicVocabulary> dynamicVocabularies = session.retrieveActiveDynamicVocabularies(requestConfig);
            var mySpeechRecoClient = clientFactory.createSpeechRecoClient(requestConfig, session,
                    dynamicVocabularies, getResponseHandler());
            if (mySpeechRecoClient == null)
            {
                terminateCommand();
                return;
            }
            speechRecoClient.add(mySpeechRecoClient);

            var pipeBuilder = new AudioPipeBuilder(requestConfig, session);

            List<AudioNodeObserver> statisticsObservers = new ArrayList<>();
            statisticsObservers.add(this);
            statisticsObservers.add(mySpeechRecoClient);

            AudioNode statisticsCalculatorNode = pipeBuilder.buildStatisticsCalculatorNode(statisticsObservers);
            pipeBuilder.appendNode(statisticsCalculatorNode);

            var isSpeechDetectorEngine = requestConfig.getString(WebApiConfig.Property.SPEECH_DETECTOR.key).equals(SpeechDetectorTypes.ENGINE.getValue());
            var endPointingEnabled = requestConfig.getBoolean(WebApiConfig.Property.END_POINT_DETECTION.key);
            var wavDumperNode = getAudioNode(pipeBuilder, endPointingEnabled && !isSpeechDetectorEngine);
            pipeBuilder.appendNode(wavDumperNode);

            List<AudioNodeObserver> observers = new ArrayList<>(speechRecoClient);
            observers.add(this); //add speech reco command as an observer to handle speech timeout events.

            if (statisticsCalculatorNode != null) {
                observers.add(statisticsCalculatorNode);
            }

            if (endPointingEnabled && !isSpeechDetectorEngine) {
                boolean startRecognitionTimers = requestConfig.getBoolean(WebApiConfig.Property.START_RECOGNITION_TIMERS.key, true);
                AudioNode startOfSpeechTimeoutNode = pipeBuilder.buildTimeoutNode(
                        requestConfig.getInt(WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.key), SpeechRecoClient.START_SPEECH_TIMER_ID, observers, startRecognitionTimers);
                pipeBuilder.appendNode(startOfSpeechTimeoutNode);

                List<AudioNodeObserver> obs = new ArrayList<>(observers);
                obs.add(startOfSpeechTimeoutNode);
                speechRecognitionTimersObservers.add(startOfSpeechTimeoutNode);

                obs.add(wavDumperNode);
                startOfSpeechTimeoutNode.addObserver(Collections.singletonList(wavDumperNode));

                pipeBuilder.appendNode(pipeBuilder.buildSpeechDetectorNode(obs));
            }

            observers.add(wavDumperNode);

            AudioNode maxSpeechTimeoutNode = pipeBuilder.buildTimeoutNode(
                    requestConfig.getInt(WebApiConfig.Property.UTTERANCE_MAX_TIME_SECONDS.key), SpeechRecoClient.MAX_SPEECH_TIMER_ID, observers, true);
            pipeBuilder.appendNode(maxSpeechTimeoutNode);
            speechRecognitionTimersObservers.add(maxSpeechTimeoutNode);
            pipeBuilder.appendNode(pipeBuilder.createdMultipleDestination(speechRecoClient.toArray(new AudioDestination[speechRecoClient.size()])));

            audioChunkerNode = pipeBuilder.getPipe();

            sendBufferedAudio();

            logger.debug("SpeechRecognitionCommand.execute -- starting speech recognition");
            for (SpeechRecoClient client: speechRecoClient) {
                if (endPointingEnabled && isSpeechDetectorEngine) {
                    client.setObservers(observers);
                }
                client.startSpeechRecognition();
            }
            var fileName = String.format(Locale.US, AUDIO_FILENAME_FORMAT, getSessionId(), getCommandId(), session.getStepId());
            logger.debug("FILENAME : {}", fileName);

            setAudioNodeEncryption(session, fileName);

        }
        catch (IllegalArgumentException e)
        {
            logger.debug("SpeechRecognitionCommand.execute -- Exception", e);
            sendErrorResponse(ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR,
                    e.getMessage()));
        }
        catch (Exception e)
        {
            logger.error("SpeechRecognitionCommand.execute -- Exception", e);
            sendErrorResponse(ResponseCode.createResponseError(
                    ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_EXECUTE_ERROR));

        }
    }

    //Requires audioChunkerNode to be available
    private void sendBufferedAudio() {
        if(audioChunkBuffer.isEmpty())
        {
           return;
        }

        Iterator<AudioChunk> ite = audioChunkBuffer.iterator();
        while (ite.hasNext())
        {
            var bufferedAudioChunk = ite.next();
            logger.debug("Sending buffered audio chunk. | Offset={} | Energy={}", bufferedAudioChunk.getOffset(), bufferedAudioChunk.getEnergy());
            onAudioImplementation(bufferedAudioChunk.getBytes());
            ite.remove();
        }

        audioChunkBuffer.clear();
    }

    private void setAudioNodeEncryption(Session session, String fileName) {
        if (request.requestCommand.parameters.containsKey(WebApiConfig.Property.SENSITIVITY.key))
        {
            logger.debug("PUTTING FILENAME {} {}", fileName,request.requestCommand.parameters.get(WebApiConfig.Property.SENSITIVITY.key));
            WavDumperNode.getEncryptedFiles()
                    .put(fileName, request.requestCommand.parameters.get(WebApiConfig.Property.SENSITIVITY.key).toString());
        }
        else if (SessionManager.getInstance().getSessionEncryptionStateMap().containsKey(getSessionId()))
        {
            WavDumperNode.getEncryptedFiles()
                    .put(fileName, SessionManager.getInstance().getSessionEncryptionStateMap().get(getSessionId()).toString());
        }
        else if (!session.getSessionConfig().getString(WebApiConfig.Property.SENSITIVITY.key).equals("open"))
        {
            WavDumperNode.getEncryptedFiles()
                    .put(fileName, session.getSessionConfig().getString(WebApiConfig.Property.SENSITIVITY.key));
        }
    }

    private AudioNode getAudioNode(AudioPipeBuilder pipeBuilder, boolean endPointingEnabled) {
        AudioNode wavDumperNode = null;
        if (endPointingEnabled){
            wavDumperNode = pipeBuilder.buildEndpointingWavDumperNode(new ArrayList<>());
        } else {
            wavDumperNode = pipeBuilder.buildWavDumperNode(new ArrayList<>());
        }
        return wavDumperNode;
    }

    @Override
    public void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal)
    {
        logger.debug("SpeechRecognitionCommand.onDirectiveImplementation -- directive: {}", requestDirective.getName());
        if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.STOP))
        {
            onStopDirective(isFinal);
        }
        else if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.CANCEL))
        {
            onCancelDirective(isFinal);
        } else if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.START_RECOGNITION_TIMERS))
        {
            onStartRecognitionTimersDirective();
        }
    }

    private void onStopDirective(boolean isFinal)
    {
        try {
            for (SpeechRecoClient client : speechRecoClient) {
                client.stopSpeechRecognition();
            }

            stopDirectiveToRecoCompleteHistogramTimer = STOP_DIRECTIVE_TO_RECO_COMPLETE_DURATION_HISTOGRAM
                    .labels(getLanguageLabel(), MetricsConstants.nonNullLabelValue(request.tenantKey)).startTimer();

            commandState = SpeechRecoCommandState.STOPPED;
            if (null != audioChunkerNode) audioChunkerNode.finish();
        } catch (Exception e) {
            logger.info("Failed to stop speech recognition.", e);
            sendErrorResponse(isFinal, ResponseCode.createResponseError(
                    ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_STOP_DIRECTIVE_ERROR));
        }
    }

    private void onCancelDirective(boolean isFinal)
    {
        for (SpeechRecoClient client: speechRecoClient) {
            client.cancelSpeechRecognition(isFinal);
        }
        commandState = SpeechRecoCommandState.COMPLETED;
        terminateCommand();

        if(null != audioChunkerNode) audioChunkerNode.finish();
    }

    private void onStartRecognitionTimersDirective()
    {
        for (SpeechRecoClient client: speechRecoClient) {
            client.startRecognitionTimers();
        }

        for (AudioNodeObserver obs: speechRecognitionTimersObservers)
        {
            obs.onEvent(null, NodeEvent.TIMERS_START);
        }
    }

    @Override
    public void onAudioImplementation(byte[] audio) {
        if(audioChunkerNode == null)
        {
            logger.trace("Buffering {} bytes of audio while getting ready.", audio.length);
            audioChunkBuffer.add(new AudioChunk(audioChunkBuffer.size(), audio));
            return;
        }

        if (commandState == SpeechRecoCommandState.INITIAL) {
            commandState = SpeechRecoCommandState.STREAMING_AUDIO;
        }

        audioChunkerNode.onAudio(audio);
    }

    @Override
    public boolean onResponseImplementation(ResponseResult responseResult)
    {
        logger.trace("SpeechRecognitionCommand.onResponseImplementation");
        if (responseResult.isFinal())
        {
            if (endOfSpeechToRecoCompleteHistogramTimer != null) {
                endOfSpeechToRecoCompleteHistogramTimer.observeDuration();
            }

            if (stopDirectiveToRecoCompleteHistogramTimer != null) {
                stopDirectiveToRecoCompleteHistogramTimer.observeDuration();
            }
            terminateCommand();
            commandState = SpeechRecoCommandState.COMPLETED;
            logger.debug("Removing the command from the session, sessionId - {}", request.sessionId);
        } else {
            if (responseResult.getIsFirstIntermediateResponse() && startOfSpeechToFirstResultHistogramTimer != null) {
                startOfSpeechToFirstResultHistogramTimer.observeDuration();
            }
        }

        return true;
    }

    @Override
    public String formatResponse(Object clientResponse)
            throws CobraException
    {
        logger.trace("SpeechRecognitionCommand.formatResponse");

        if (clientResponse instanceof ResponseEvent)
        {
            final ResponseEvent cast = (ResponseEvent) clientResponse;

            final var response = Response.eventFromRequest(request, cast);
            return response.toJson();
        }

        final var response = Response.fromRequest(request);

        if (clientResponse instanceof ResponseResult)
        {
            ResponseResult cast = (ResponseResult) clientResponse;

            response.setResult(cast);
        }


        if (response.getResult().isFinal())
        {
            CommandLog speechRecognitionCommandLog;
            speechRecognitionCommandLog = formatLog(response);

            getEDFLogForwarder().forwardLog(getEdfMessageQueueable(speechRecognitionCommandLog), commandConfigs);
        }

        return response.toJson();
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        CommandLog baseResponse = super.formatLog(response);

        if (((Response)response).getResult() instanceof SpeechRecoResponse)
        {
            var responseCast = (Response) response;
            Response responseClone = null;
            SpeechRecoResponse speechRecoResponse ;


            try
            {
                Optional<SpeechRecoResponse.Data> speechRecoResponseData;

                if (!baseResponse.request.sensitivity.equalsIgnoreCase(CommandSensitivityType.OPEN.name()))
                {
                    responseClone = responseCast.clone();
                    speechRecoResponse = (SpeechRecoResponse) responseClone.getResult();
                    speechRecoResponseData = Optional.ofNullable(speechRecoResponse.getData());
                }
                else
                {
                    speechRecoResponse = (SpeechRecoResponse) responseCast.getResult();
                    speechRecoResponseData =
                            Optional.ofNullable(speechRecoResponse.getData());
                }

                encryptResponse(baseResponse, responseClone, speechRecoResponse, speechRecoResponseData);

                if (responseClone == null)
                {

                    if (!baseResponse.request.sensitivity.equalsIgnoreCase(CommandSensitivityType.MASKED.name()))
                    {
                        responseCast.fileName = String.format(Locale.US, AUDIO_FILENAME_FORMAT, getSessionId(), getCommandId(), baseResponse.request.stepID);
                    }

            /*
            We need to do this to pretty-print the EDF response without duplicate parameters
             */
                    var prettyFormatResponse = prettyFormatResponse(responseCast);
                    baseResponse.request.response = prettyFormatResponse;

                }
                else
                {
                    if (!baseResponse.request.sensitivity.equalsIgnoreCase(CommandSensitivityType.MASKED.name()))
                    {
                        responseClone.fileName = String.format(Locale.US, AUDIO_FILENAME_FORMAT, getSessionId(), getCommandId(), baseResponse.request.stepID);
                    }


            /*
            We need to do this to pretty-print the EDF response without duplicate parameters
             */
                    var prettyFormatResponse = prettyFormatResponse(responseClone);
                    baseResponse.request.response = prettyFormatResponse;
                }

                var baseResponseString = baseResponse.toJson(false);
                logger.debug("{}--EDF Response : {} ",getCommandName(), baseResponseString);

                return baseResponse;
            }
            catch (Exception e)
            {
                logger.info("Failed to format Speech Recognition EDF Response", e);
            }
            return baseResponse;
        }
        return baseResponse;
    }

    private void encryptResponse(CommandLog baseResponse, Response responseClone, SpeechRecoResponse speechRecoResponse, Optional<SpeechRecoResponse.Data> speechRecoResponseData) throws JsonProcessingException {
        switch (CommandSensitivityType.valueOf(baseResponse.request.sensitivity.toUpperCase()))
        {
            case OPEN:
                break;
            case MASKED:
            case ENCRYPTED:
                if (speechRecoResponseData.isPresent())
                {
                    var transcriptions = getEncryptor().processString(Arrays.toString(
                            speechRecoResponseData.get()
                                                  .getTranscriptions()));
                    var words = getEncryptor().processString(getMapper().writer()
                                                                           .writeValueAsString(
                                                                                   speechRecoResponseData.get()
                                                                                                         .getWords()));
                    var bestResult = getEncryptor().processString(speechRecoResponse.getBestResult());
                    speechRecoResponseData.get()
                                          .setEncryptedTranscriptions(transcriptions);
                    speechRecoResponseData.get()
                                          .setEncryptedWords(words);
                    speechRecoResponse.setData(speechRecoResponseData.get());
                    speechRecoResponse.setBestResult(bestResult);

                    logger.debug("SETTING FILENAME IN JSON " );

                    if (responseClone != null)
                    {
                        Map<String, Object> processedVerboseResults = new HashMap<>();
                        Map<String, Object> verboseResults = speechRecoResponse.getVerboseResult();

                        for (Map.Entry<String, Object> entry : verboseResults.entrySet()) {
                            String encryptedKey = getEncryptor().processString(entry.getKey());
                            Object encryptedValue = encryptJSONObject(getEncryptor(), verboseResults.get(entry.getKey()));
                            processedVerboseResults.put(encryptedKey, encryptedValue);
                        }

                        speechRecoResponse.setVerboseResult(processedVerboseResults);

                        responseClone.setResult(speechRecoResponse);
                    }

                }
                else
                {
                    logger.debug("No Speech Reco Response Data Present");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        switch (event)
        {
            case NEW_NOISE_LEVEL_CALCULATED:
                if(eventOwner instanceof SpeechDetector)
                {
                    noiseLevel = ((SpeechDetector) eventOwner).getCalculatedBackgroundNoiseLevel();
                }
                break;

            case TIMEOUT:
                onTimeout(eventOwner);
                break;
            case START_OF_SPEECH:
                startOfSpeechToFirstResultHistogramTimer = SOS_TO_FIRST_RESULT_DURATION_HISTOGRAM
                        .labels(getLanguageLabel(), MetricsConstants.nonNullLabelValue(request.tenantKey)).startTimer();
                sendEvent(new ResponseEvent("startOfSpeech"));
                break;
            case END_OF_SPEECH:
                sendEvent(new ResponseEvent("endOfSpeech"));
                endOfSpeechToRecoCompleteHistogramTimer = EOS_TO_REC_COMPLETE_DURATION_HISTOGRAM
                            .labels(getLanguageLabel(), MetricsConstants.nonNullLabelValue(request.tenantKey)).startTimer();
                commandState = SpeechRecoCommandState.STOPPED;
                break;

            case AUDIO_STATISTICS:
                if (eventOwner instanceof StatisticsCalculatorNode) {
                    Map<String, Object> audioStatistics = ((StatisticsCalculatorNode) eventOwner).getStatistics(StatisticsCalculatorNode.ALL_STATISTICS);
                    statistics.mediaDuration = (Integer)audioStatistics.get(StatisticsCalculatorNode.STATISTIC_MEDIA_DURATION);
                    statistics.mediaSize = (Integer)audioStatistics.get(StatisticsCalculatorNode.STATISTIC_MEDIA_SIZE);
                }
                break;
            default:
                break;
        }
    }


    private String getLanguageLabel() {
        String language = null;
        for (SpeechRecoClient client : speechRecoClient) {
            language = client.getLanguage();
        }
        return MetricsConstants.nonNullLabelValue(language);
    }

    public void onTimeout(AudioNode source) {

        if (source instanceof TimeoutNode) {
            String timerID = ((TimeoutNode) source).getTimerID();

            //This could probably be done better, but I have no other idea right now.
            if (timerID.equalsIgnoreCase(SpeechRecoClient.START_SPEECH_TIMER_ID))
            {
                var response = new SpeechRecoResponse(ResponseResult.ResponseStatus.NO_INPUT, noiseLevel);
                commandState = SpeechRecoCommandState.COMPLETED;
                sendResult(response);

                terminateCommand();
            }
            else if (timerID.equalsIgnoreCase(SpeechRecoClient.MAX_SPEECH_TIMER_ID))
            {
                logger.info("utterance reached maximum time");
                commandState = SpeechRecoCommandState.STOPPED;

                final var event = new ResponseEvent("maxSpeech");
                sendEvent(event);

                logger.trace("onTimeout(MAX_SPEECH_TIMER_ID), stopping recognition");
                for (SpeechRecoClient client: speechRecoClient) {
                    client.stopSpeechRecognition();
                }
            }
            else {
                logger.info("Command received timeout from unknown source: {}", timerID);
            }
        }
    }

    public static IResources getResources(CombinedConfiguration config){
        return new IResources() {
            @Override
            public String getName() {
                return CommandFactory.getCommandName(this.getClass());
            }

            @Override
            public Map<String, Object> getResourcesMap() {
                HashMap<String, Object> resourcesMap = new HashMap<>();

                resourcesMap.put("speechRecognitionEngine", config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key));

                resourcesMap.put("languageList", getLanguageList(config));
                resourcesMap.put("speechRecognitionCodec", config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key));
                return resourcesMap;
            }
        };
    }

    static List<String> getLanguageList(CombinedConfiguration config) {
        List<String> languageKeys = new ArrayList<>();
        HierarchicalConfiguration<ImmutableNode> langConfig;
        try {
            langConfig = config.configurationAt(WebApiConfig.Property.KRYPTON_LANGUAGE_TO_URL_MAPPING.key);
        } catch (ConfigurationRuntimeException cre){
            return languageKeys;
        }
        Iterator<String> languageIter = langConfig.getKeys();
        while(languageIter.hasNext()) {
            languageKeys.add(languageIter.next());
        }
        return languageKeys;
    }
}
