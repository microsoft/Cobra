package com.nuance.cobra.client.mix.krypton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.SpeechRecognitionClients;
import com.nuance.cobra.client.krypton.KryptonUtils;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.*;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.nuance.rpc.asr.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KryptonGrpcClient extends SpeechRecoClient {

    public static final String RESPONSE_NOTIFICATIONS_KEY = "notifications";
    public static final String X_NUANCE_SUPPLEMENTAL_METRICS_RESULT_KEY = "x-nuance-supplemental-metrics-result";
    public static final String DATA_KEY = "data";

    protected enum State {
        INITIALIZING, // NOT ready to stream audio and to stop the reco
        RUNNING, // ready to stream audio and to stop the reco
        CANCELED, // ready to stream audio and to stop the reco
        COMPLETED
    }

    public static final String AUTH_MIX_BEARER_TOKEN = "mixBearerToken";
    public static final String AUTH_NO_AUTH = "no_auth";
    public static final String AUTH_PASSTHROUGH = "passthrough";

    public static final String URI = "uri";
    public static final String WEIGHT = "weight";
    public static final String REQUEST_TIMEOUT = "requestTimeout";

    private static final Logger logger = LogManager.getLogger();

    protected final CombinedConfiguration config;
    protected final KryptonGrpcRequestParams params;
    private final RecognizerGrpc.RecognizerStub stub;
    private final StreamObserver<RecognitionResponse> responseObserver = new KryptonGrpcResponseObserver(this);
    private final ResponseHandler responseHandler;
    private final ResponseErrorHandler errorHandler;
    protected NoiseLevel noiseLevel = new NoiseLevel(0, 0);
    protected boolean startOfSpeechWasDetected;
    protected boolean queueStopSpeechRecognition;
    protected State state = State.INITIALIZING;
    private StreamObserver<RecognitionRequest> requestObserver;
    private final Queue<AudioChunk> audioChunkBuffer = new LinkedList<>(); // buffer chunks if server is not ready to receive
    private int nbBufferedAudioChunks;
    private int nbSentAudioRequests;
    private Calendar timeAtWhichTheFirstAudioRequestWasSent;
    private Calendar timeAtWhichTheLastAudioRequestWasSent;
    private boolean finalResponseWasSent;
    private String language;
    private String topic;
    private final List<Map<String, String>> statisticsDlms = new ArrayList<>();
    private final List<Map<String, Object>> statisticsBuiltins = new ArrayList<>();

    private ScheduledFuture<?> scheduledFuture = null;
    private int silencePacketCount = 0;

    protected List<AudioNodeObserver> observers = new ArrayList<>();

    public KryptonGrpcClient(CombinedConfiguration config,
                             String sessionId,
                             KryptonGrpcRequestParams params,
                             RecognizerGrpc.RecognizerStub stub,
                             ResponseHandler responseHandler,
                             ResponseErrorHandler errorHandler,
                             HeaderEventListener interceptor) {

        super(sessionId);

        this.config = config;
        this.params = params;
        this.stub = stub;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        interceptor.setListener(header -> {
            var session = SessionManager.getInstance().getSession(getSessionId(), false);
            if (session == null) return;
            if (session.getCommand() == null) return;

            session.getCommand().getStatistics().addEngineResponse("grpcHeaders", header);

            MixUtils.checkIfRateLimited(header, logger);
        });
    }

    @Override
    public void startSpeechRecognition() {
        if (!KryptonUtils.isLanguageAllowed(config)) {
            sendErrorResponse(ExternalResponseErrorCode.KRYPTON_LANGUAGE_NOT_ALLOWED);
            return;
        }

        try {
            RecognitionRequest request = buildRequest();
            setStatistics(SpeechRecognitionClients.KRYPTON_GRPC.getValue(), language, topic, statisticsDlms, statisticsBuiltins);
            logger.debug("Sending Krypton gRPC request:\n{}", request);
            requestObserver = stub.recognize(responseObserver);
            requestObserver.onNext(request);
            onEngineReady(); // we do not have to wait for status code 100 before sending audio
            thwartKryptonNoAudioTimerWithSilence();
        } catch (Exception e) {
            state = State.COMPLETED;
            MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);

            if (e instanceof ExceptionWithExternalResponseErrorCode) {
                sendErrorResponse(((ExceptionWithExternalResponseErrorCode) e).getErrorCode());
            } else {
                sendErrorResponse(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_EXECUTE_ERROR);
            }
        }
    }

    protected void onEngineReady() {
        state = KryptonGrpcClient.State.RUNNING;
        synchronizedProcessAudio(null);

        var session = SessionManager.getInstance().getSession(getSessionId(), true);
        if (session != null && session.getCommand() != null) {
            session.getCommand().getStatistics().addEngineResponse("engineReady", Map.of("timestamp", LoggingUtils.getUTCFormattedTimestamp()));
        }

        if (queueStopSpeechRecognition) {
            queueStopSpeechRecognition = false;
            stopSpeechRecognition();
        }
    }

    private RecognitionRequest buildRequest() throws ExceptionWithExternalResponseErrorCode, CobraException {
        RecognitionInitMessage.Builder initMessage = RecognitionInitMessage.newBuilder()
                .setParameters(buildRequestInitParams());

        addResourcesToRequestInitMessageIfAny(initMessage);

        ImmutableMap<String, String> clientData = MixUtils.getClientDataMap(this.sessionId, config, Map.of("x-nuance-supplemental-metrics", "1"));
        if (logger.isTraceEnabled()) logger.trace("Adding clientData: {}", StringUtils.join(clientData));
        initMessage.putAllClientData(clientData);

        addUserId(initMessage);

        return RecognitionRequest.newBuilder()
                .setRecognitionInitMessage(initMessage)
                .build();
    }

    protected RecognitionParameters.Builder buildRequestInitParams() {
        language = KryptonUtils.getLanguage(config);

        var recognitionFlags = RecognitionFlags.newBuilder();
        params.setDiscardSpeakerAdaptation(recognitionFlags);
        recognitionFlags.setFilterProfanity(KryptonUtils.isProfanityFilterEnabled(config))
                        .setIncludeTokenization(true)
                        .setSuppressCallRecording(params.shouldMaskUserUtterance())
                        .setStallTimers(params.getStallTimers())
                        .setMaskLoadFailures(config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_MASK_LOAD_FAILURES.key));

        var recognitionParametersBuilder =
                RecognitionParameters.newBuilder()
                        .setLanguage(language)
                        .setAudioFormat(params.getAudioFormat())
                        .setResultType(params.getResultType())
                        .setRecognitionFlags(recognitionFlags);
//                .setNoInputTimeoutMs(...) // TODO bugged on the server side

        // Defaults to EnumUtteranceDetectionMode.SINGLE
        if(!config.getString(WebApiConfig.Property.SPEECH_DETECTOR.key).equals(SpeechDetectorTypes.ENGINE.getValue()) ||
            !config.getBoolean(WebApiConfig.Property.END_POINT_DETECTION.key)) {
            recognitionParametersBuilder.setUtteranceDetectionMode(EnumUtteranceDetectionMode.DISABLED);
        }
        else {
            recognitionParametersBuilder
                    .setNoInputTimeoutMs(config.getInt(WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.key, (int)WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.defaultValue) * 1000)
                    .setRecognitionTimeoutMs(config.getInt(WebApiConfig.Property.UTTERANCE_MAX_TIME_SECONDS.key, (int)WebApiConfig.Property.UTTERANCE_MAX_TIME_SECONDS.defaultValue) * 1000);
            params.setUtteranceEndSilenceMs(recognitionParametersBuilder);
            params.setSpeechDetectionSensitivty(recognitionParametersBuilder);
        }

        params.buildMaxHypotheses(recognitionParametersBuilder);
        params.buildSpeechDomain(recognitionParametersBuilder);
        params.buildFormattingType(recognitionParametersBuilder);

        topic = params.buildTopic(recognitionParametersBuilder);
        return recognitionParametersBuilder;
    }

    private void addResourcesToRequestInitMessageIfAny(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode, CobraException
    {
        buildResourceDomainLm(initMessage);

        List<Map<String, Object>> loadedBuiltins = params.addBuiltins(initMessage);
        statisticsBuiltins.addAll(loadedBuiltins);

        params.addWordsets(initMessage);

        params.setSpeakerProfile(initMessage);
    }

    private void addUserId(RecognitionInitMessage.Builder initMessage) {
        if (!MixUtils.shouldAddUserId(config, WebApiConfig.Property.KRYPTON_GRPC_USE_USERID, logger) &&
                !MixUtils.shouldAddUserId(config, WebApiConfig.Property.ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE, logger)) {
            return;
        }

        var userId = config.getString(WebApiConfig.Property.CLIENT_USER_ID.key);
        logger.trace("Adding user_id: {}", userId);
        initMessage.setUserId(userId);
    }

    private void buildResourceDomainLm(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode {
        List<Map<String, String>> loadedDlms = params.addDlmsPerLang(initMessage);

        if(loadedDlms.isEmpty()) {
            loadedDlms = params.addKryptonDlm(initMessage);
        }

        if(loadedDlms.isEmpty()) {
            loadedDlms = params.addDlmFromDialogModelConfig(initMessage);
        }

        List<Map<String, String>> dynVocLoadedDlms = params.addDlmFromDynamicVocabularies(initMessage);
        if (dynVocLoadedDlms != null && !dynVocLoadedDlms.isEmpty()) {
            loadedDlms.addAll(dynVocLoadedDlms);
        }

        statisticsDlms.addAll(loadedDlms);
    }

    @Override
    public void onAudio(AudioChunk audioChunk)
    {
        if(audioChunk.getBytes().length > 2)
        {
            cancelScheduledFuture();
        }

        synchronizedProcessAudio(audioChunk);
    }

    protected synchronized void synchronizedProcessAudio(AudioChunk audioChunk) {
        if (state != State.COMPLETED) {
            if (state == State.RUNNING) {
                // send all audio chunks that were buffered
                Iterator<AudioChunk> ite = audioChunkBuffer.iterator();
                while (ite.hasNext()) {
                    var bufferedAudioChunk = ite.next();
                    logger.debug("Sending buffered audio chunk. | Offset={} | Energy={}", bufferedAudioChunk.getOffset(), bufferedAudioChunk.getEnergy());
                    sendAudioRequest(bufferedAudioChunk);
                    ite.remove();
                }
                if (audioChunk != null) {
                    logger.debug("Sending audio chunk. | Offset={} | Energy={}", audioChunk.getOffset(), audioChunk.getEnergy());
                    sendAudioRequest(audioChunk);
                }
            } else if (audioChunk != null) {
                logger.debug("Buffering audio chunk. | Offset={} | Energy={}", audioChunk.getOffset(), audioChunk.getEnergy());
                addAudioChunkToBuffer(audioChunk);
            }
        } else {
            logger.debug("Attempt at processing audio was cancelled because Krypton-gRPC ASR request is completed. | audioChunk={}", audioChunk == null ? "null" : "offset:" + audioChunk.getOffset());
        }
    }

    private void addAudioChunkToBuffer(AudioChunk audioChunk) {
        audioChunkBuffer.add(audioChunk);
        nbBufferedAudioChunks++;
    }

    private void sendAudioRequest(AudioChunk audioChunk) {
        byte[] audioBytes = audioChunk.getBytes();
        var audio = ByteString.copyFrom(audioBytes);

        RecognitionRequest request = RecognitionRequest.newBuilder()
                .setAudio(audio)
                .build();

        refreshAudioStreamingStatistics();

        requestObserver.onNext(request);
        nbSentAudioRequests++;
    }

    private void refreshAudioStreamingStatistics() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        if (timeAtWhichTheFirstAudioRequestWasSent == null) {
            timeAtWhichTheFirstAudioRequestWasSent = currentTime;
        }
        timeAtWhichTheLastAudioRequestWasSent = currentTime;
    }

    @Override
    public void finish() { // AudioDestination.finish()
        // nothing to do here
        // doesn't mean the audio recording is finished
    }

    @Override
    public void stopSpeechRecognition() {
        if (state == State.INITIALIZING
                || (state == State.RUNNING && !audioChunkBuffer.isEmpty())) {
            queueStopSpeechRecognition = true;
        } else if (state == State.RUNNING) {
            state = State.COMPLETED;

            requestObserver.onCompleted(); // not to be confused with responseObserver.onCompleted() // TODO add timeout
        }
    }

    @Override
    public void cancelSpeechRecognition(boolean isFinal) { // isFinal is not used
        if (state != State.COMPLETED) {
            cancelRpc("Cancelled");
            state = State.COMPLETED;

            var response = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.CANCELLED, noiseLevel);
            sendResponse(response);
        } else {
            logger.info("Cannot cancel, already completed");
        }
    }

    @Override
    public void startRecognitionTimers()
    {
        logger.trace("startRecognitionTimers");
        StartTimersControlMessage startTimersControlMessage = StartTimersControlMessage.newBuilder().build();
        ControlMessage controlMessage = ControlMessage.newBuilder().setStartTimersMessage(startTimersControlMessage).build();
        RecognitionRequest request = RecognitionRequest.newBuilder().setControlMessage(controlMessage).build();
        logger.debug("Sending Krypton gRPC start timers request:\n{}", request);
        requestObserver.onNext(request);
    }

    protected synchronized void sendResponse(SpeechRecoResponse response) {
        if (!finalResponseWasSent) {
            if (response.isFinal()) {
                finalResponseWasSent = true;
                cancelRpc("sendResponse");
                ManagedChannelManager.managedChannelShutdownNow(config, (ManagedChannel) stub.getChannel());
                setAudioStreamingToEngineStatistics(nbBufferedAudioChunks, nbSentAudioRequests, timeAtWhichTheFirstAudioRequestWasSent, timeAtWhichTheLastAudioRequestWasSent);
                if(verboseResponseHasNotifications(response))
                {
                    var notifObject = response.getVerboseResult().get(RESPONSE_NOTIFICATIONS_KEY);
                    if(notifObject instanceof List) {
                        var notifs = (List<?>)notifObject;
                        List<Object> newNotifs = new ArrayList<>(notifs.size());
                        for(Object notifMap : notifs) {
                            var gson = new GsonBuilder().create();
                            var notif = new JSONObject(gson.toJson(notifMap));
                            newNotifs.add(convertNotification(notif).toMap());
                        }
                        response.getVerboseResult().put(RESPONSE_NOTIFICATIONS_KEY, newNotifs);
                    }
                }
            }
            responseHandler.onResponse(response);
        }
    }

    private boolean verboseResponseHasNotifications(SpeechRecoResponse response) {
        return response.getVerboseResult() != null && response.getVerboseResult().get(RESPONSE_NOTIFICATIONS_KEY) != null;
    }

    private JSONObject convertNotification(JSONObject notif) {
        if(notif.getInt("code") == 1004) {
            var metricsResult = convertNotificationKey(new JSONObject(notif.getJSONObject(DATA_KEY).getString(X_NUANCE_SUPPLEMENTAL_METRICS_RESULT_KEY)), X_NUANCE_SUPPLEMENTAL_METRICS_RESULT_KEY);
            notif.getJSONObject(DATA_KEY).put(X_NUANCE_SUPPLEMENTAL_METRICS_RESULT_KEY, metricsResult.toString());
        }
        return notif;
    }

    private Object convertNotificationKey(Object obj, String parentKey) {
        if(obj instanceof JSONObject) {
            var newNotif = new JSONObject();
            JSONObject jsonObj = ((JSONObject)obj);
            for (String key : jsonObj.keySet()) {
                newNotif.put(params.resourceIndexToIdMap.getOrDefault(key, key), convertNotificationKey(jsonObj.get(key), key));
            }
            return newNotif;
        }
        else if(obj instanceof JSONArray) {
            var newArray = new JSONArray();
            for(Object objInArray : ((JSONArray)obj)) {
                newArray.put(convertNotificationKey(objInArray, parentKey));
            }
            return newArray;
        }
        else {
            if (parentKey.equalsIgnoreCase("wordsets") && params.resourceIndexToIdMap.containsKey(obj)) {
                return params.resourceIndexToIdMap.get(obj);
            }
            return obj;
        }
    }

    protected synchronized void sendErrorResponse(ExternalResponseErrorCode errorCode) {
        if (!finalResponseWasSent) {
            finalResponseWasSent = true;
            cancelRpc("sendErrorResponse");
            ManagedChannelManager.managedChannelShutdownNow(config, (ManagedChannel) stub.getChannel());

            var response = new SpeechRecoResponse(true, noiseLevel, ResponseCode.createResponseError(errorCode));
            errorHandler.handleError(response);
        }
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        switch (event) {
            case NEW_NOISE_LEVEL_CALCULATED:
                if (eventOwner instanceof SpeechDetector) {
                    noiseLevel = ((SpeechDetector) eventOwner).getCalculatedBackgroundNoiseLevel();
                }
                break;
            case START_OF_SPEECH:
                logger.debug("onEvent() START_OF_SPEECH");
                // SOS events are sent to the client by the audio pipeline
                startOfSpeechWasDetected = true;
                break;
            case END_OF_SPEECH:
                logger.debug("onEvent() END_OF_SPEECH");
                // EOS events are sent to the client by the audio pipeline
                if (params.shouldStopOnEndOfSpeech()) {
                    stopSpeechRecognition();
                }
                break;
            case TIMEOUT:
                if (eventOwner instanceof TimeoutNode) {
                    String timerId = ((TimeoutNode) eventOwner).getTimerID();
                    logger.debug("onEvent() TIMEOUT {}", timerId);
                    if(timerId.equals(START_SPEECH_TIMER_ID)) {
                        state = State.COMPLETED;
                        finalResponseWasSent = true; // NoInput response was sent to the client by the audio pipeline
                        cancelRpc("TIMEOUT");
                        ManagedChannelManager.managedChannelShutdownNow(config, (ManagedChannel) stub.getChannel());
                    }
                    else if(timerId.equals(MAX_SPEECH_TIMER_ID)) {
                        stopSpeechRecognition();
                    }
                }
                break;
            default:
                logger.trace("Unhandled event {}", event);
                break;
        }
    }

    @Override
    public boolean setRecResultType(RecResultType recResultType) {
        throw new UnsupportedOperationException("setRecResultType in KryptonGrpcClient is unsupported"); // TODO add support?
    }

    @Override
    public float getMaxConfidence() {
        throw new UnsupportedOperationException("getMaxConfidence in KryptonGrpcClient is unsupported"); // TODO add support?
    }

    @Override
    public String getLanguage() {
        return language;
    }

    private void cancelRpc(String reason)
    {
        logger.trace("cancelRpc");
        cancelScheduledFuture();
        if (requestObserver == null) return;
        state = State.CANCELED;
        ((ClientCallStreamObserver<RecognitionRequest>) requestObserver).cancel(reason, null);
        ((KryptonGrpcResponseObserver) responseObserver).cancel();
    }

    private void thwartKryptonNoAudioTimerWithSilence()
    {
        int silenceDelaySeconds = config.getInt(WebApiConfig.Property.KRYPTON_GRPC_THWART_NO_AUDIO_TIMER_SECONDS.key);
        if(silenceDelaySeconds < 1)
        {
            return;
        }

        int audioFormatNumber = params.getAudioFormat().getAudioFormatUnionCase().getNumber();
        if(audioFormatNumber > 3 || //pcm, alaw, ulaw
           scheduledFuture != null)
        {
            return;    //opus, oggopus etc. Don't know what a silence value is for opus or oggopus
        }

        Runnable task = new ThreadContextRunnable()
        {
            @Override
            public void runWithContext() {
                if(++silencePacketCount <= 6)
                {
                    var silenceBytes = new byte[2];
                    silenceBytes[0] = (byte) 0xFF;
                    silenceBytes[1] = (byte) 0xFF;
                    logger.trace("Sending silence packet #{}", silencePacketCount);
                    onAudio(new AudioChunk(0, silenceBytes));
                } else
                {
                    cancelScheduledFuture();
                }
            }
        };
        scheduledFuture = ScheduledTaskUtils.scheduleWithFixedDelay(task, silenceDelaySeconds, silenceDelaySeconds, TimeUnit.SECONDS);
    }

    private void cancelScheduledFuture()
    {
        if(scheduledFuture == null)
        {
            return;
        }
        boolean cancelled = scheduledFuture.cancel(true);
        if (cancelled)
        {
            scheduledFuture = null;
        }
    }

    @Override
    public void setObservers(List<AudioNodeObserver> observers) {
        this.observers = observers;
    }
}
