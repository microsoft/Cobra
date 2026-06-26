package com.nuance.cobra.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.endpoint.WebApiEndpoint;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.logging.EdfFluentbitListener;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.EdfLogListener;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.logging.eventhub.EventHubListener;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.encryption.Encryptor;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.*;
import com.nuance.cobra.utils.response.EDF.*;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.ResponseHandlerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import com.nuance.plugin.v1.shareable.ICommand;
import io.prometheus.client.Histogram;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.*;

/**
 * The abstract definition of a command.
 */
public abstract class Command
        implements ResponseFormatter, ResponseReceiver, EDFResponse, ICommand
{
    private static final Logger logger = LogManager.getLogger(Command.class);
    private Encryptor encryptor = null;
    private UserInfo userInfo = null;
    private static ObjectMapper mapper;

    /**
     * Prometheus metrics for measuring command execution duration.
     */
    private static final Histogram COMMAND_DURATION_HISTOGRAM =
            Histogram.build("webapi_command_duration_seconds", "Command execution duration: ")
                     .labelNames("commandName")
                     .buckets(0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 20.0, 30.0, 40.0, 50.0)
                     .register();

    public enum CommandSensitivityType {
        OPEN,
        MASKED,
        ENCRYPTED;
    }

    boolean hasSentFinalResponse = false;
    String currentSessionID;
    Session currentSession;

    private ScheduledFuture<?> timerTask;

    ScheduledFuture<?> getTimerTask()
    {
        return timerTask;
    }

    /**
     * Error handling object
     */
    private ResponseErrorHandler errorHandler;

    protected ResponseErrorHandler getErrorHandler(Logger logger) {
        if (errorHandler == null) {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new ResponseHandlerErrorListener(getResponseHandler()));
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    public class EventStatistics
    {
        @JsonProperty("name")
        public String name;

        @JsonProperty("startTime")
        public Calendar startTime;

        public EventStatistics(String name, Calendar time)
        {
            this.name = name;
            startTime = time;
        }
    }

    public class DirectiveStatistics
    {
        @JsonProperty("name")
        public String name;

        @JsonProperty("origin")
        public String origin;

        @JsonProperty("startTime")
        public Calendar startTime;

        public DirectiveStatistics(String name, String origin, Calendar time)
        {
            this.name = name;
            this.origin = origin;
            startTime = time;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class StatisticsHostInfo
    {
        @JsonProperty("region")
        public String region;

        @JsonProperty("environment")
        public String environment;

        @JsonProperty("version")
        public String version;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class StatisticsChainInfo
    {
        @JsonProperty("id")
        public String id;

        @JsonProperty("index")
        public Integer index;

        @JsonProperty("isFinalCommand")
        public Boolean isFinalCommand;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class CommandStatistics
    {
        @JsonProperty("hostInfo")
        public StatisticsHostInfo hostInfo;

        @JsonProperty("dataLength")
        public Integer getDataLength()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(SpeechSynthesisCommand.class)) ||
                    getCommandName().equals(CommandFactory.getCommandName(InterpretationCommand.class)))
            {
                return dataLength;
            }
            else
            {
                return null;
            }
        }

        int dataLength;

        @JsonProperty("modelVersion")
        public String getModelVersion()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(SpeechRecognitionCommand.class)) ||
                getCommandName().equals(CommandFactory.getCommandName(InterpretationCommand.class)))
            {
                return modelVersion;
            }
            else
            {
                return null;
            }
        }

        public void setModelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
        }

        private String modelVersion;

        @JsonProperty("voiceQuality")
        public String getVoiceQuality()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(SpeechSynthesisCommand.class)))
            {
                return voiceQuality;
            }
            else
            {
                return null;
            }
        }

        public void setVoiceQuality(String voiceQuality) {
            this.voiceQuality = voiceQuality;
        }

        private String voiceQuality;

        @JsonProperty("speechSynthesisTuningBundle")
        public String getSpeechSynthesisTuningBundle()
        {
            if(getCommandName().equals(CommandFactory.getCommandName(SpeechSynthesisCommand.class)))
            {
                return speechSynthesisTuningBundle;
            }
            else
            {
                return null;
            }
        }

        public void setSpeechSynthesisTuningBundle(String speechSynthesisTuningBundle) {this.speechSynthesisTuningBundle =
                speechSynthesisTuningBundle;}

        private String speechSynthesisTuningBundle;

        @JsonProperty("voiceModel")
        public String getVoiceModel()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(SpeechSynthesisCommand.class)))
            {
                return voiceModel;
            }
            else
            {
                return null;
            }
        }

        public void setVoiceModel(String voiceModel) {
            this.voiceModel = voiceModel;
        }

        private String voiceModel;

        @JsonProperty("asrEngineMetrics")
        public Map<String, Map<String, Object>> asrEngineMetrics;

        @JsonProperty("engineRequests")
        public Map<String, Object> engineRequests;

        public void setEngineRequests(Map<String, Object> engineRequests)
        {
            this.engineRequests = engineRequests;
        }

        public void setAudioStreamingToEngineStatistics(int engineBufferedAudioChunks, int engineSentAudioChunks, Calendar engineFirstAudioChunk, Calendar engineLastAudioChunk)
        {
            this.engineBufferedAudioChunks = engineBufferedAudioChunks;
            this.engineSentAudioChunks = engineSentAudioChunks;
            this.engineFirstAudioChunk = engineFirstAudioChunk;
            this.engineLastAudioChunk = engineLastAudioChunk;
        }

        @JsonProperty("engineResponses")
        public Map<String, Object> engineResponses;
        //should contain debugging headers and info returned by the server, like the x-client-id

        public void addEngineResponse(String key, Object data)
        {
            if (this.engineResponses == null) {
                this.engineResponses = new HashMap<>();
            }
            this.engineResponses.put(key, data);
        }

        @JsonProperty("startTime")
        public Calendar startTime;

        @JsonProperty("endTime")
        public Calendar endTime;

        @JsonProperty("runTimeMs")
        public Long runTimeMs;

        @JsonProperty("firstIntermediateResultTime")
        public Calendar firstIntermediateResultTime;

        @JsonProperty("lastIntermediateResultTime")
        public Calendar lastIntermediateResultTime;

        @JsonProperty("firstAudioBufferReceivedTime")
        public Calendar firstAudioBufferReceivedTime;

        @JsonProperty("lastAudioBufferReceivedTime")
        public Calendar lastAudioBufferReceivedTime;

        @JsonProperty("firstAudioBufferSentTime")
        public Calendar firstAudioBufferSentTime;

        @JsonProperty("lastAudioBufferSentTime")
        public Calendar lastAudioBufferSentTime;

        @JsonProperty("mediaDuration")
        public Integer mediaDuration;

        @JsonProperty("mediaSize")
        public Integer mediaSize;

        @JsonProperty("engineBufferedAudioChunks")
        public Integer engineBufferedAudioChunks;

        @JsonProperty("engineSentAudioChunks")
        public Integer engineSentAudioChunks;

        @JsonProperty("engineFirstAudioChunk")
        public Calendar engineFirstAudioChunk;

        @JsonProperty("engineLastAudioChunk")
        public Calendar engineLastAudioChunk;

        @JsonProperty("directives")
        public List<DirectiveStatistics> directives;

        @JsonProperty("events")
        public List<EventStatistics> events;

        @JsonProperty("enableSpeechRecognitionSpeakerProfile")
        public boolean enableSpeechRecognitionSpeakerProfile;

        @JsonProperty("clientUserID")
        public String clientUserID;

        int wordCount;

        int packetsReceived;

        @JsonProperty("wordCount")
        public Integer getWordCount()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(UploadDynamicVocabularyCommand.class)))
            {
                return wordCount;
            }
            else
            {
                return null;
            }
        }

        @JsonProperty("packetsReceived")
        public Integer getPacketsReceived()
        {
            if (getCommandName().equals(CommandFactory.getCommandName(SpeechRecognitionCommand.class)))
            {
                return packetsReceived;
            }
            else
            {
                return null;
            }
        }

        @JsonProperty("chain")
        public StatisticsChainInfo chainInfo;

        @JsonProperty("metadata")
        public Map<String,Object> metadata;

        CommandStatistics()
        {
            directives = new ArrayList<>();
            events = new ArrayList<>();
        }

        public void setMediaDuration(int mediaDuration)
        {
            this.mediaDuration = mediaDuration;
        }

        public void setAsrEngineMetrics(Map<String, Map<String, Object>> asrEngineMetrics) {
            this.asrEngineMetrics = asrEngineMetrics;
        }
    }

    /**
     * The request associated with the current command
     */
    public final Request request;

    /**
     * The response handler to forward to the client
     */
    private ResponseHandler responseHandler;

    /**
     * The command statistics
     */
    protected CommandStatistics statistics;

    protected CombinedConfiguration commandConfigs;

    protected CommandChainer chainer;

    /**
     * Initializes the command object
     */
    protected Command(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        this.request = request;
        this.commandConfigs = commandConfigs;
        this.chainer = chainer;
        currentSessionID = request.sessionId;

        statistics = new CommandStatistics();
        var statisticsHostInfo = new StatisticsHostInfo();
        statisticsHostInfo.region = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.HOST_REGION.key);
        statisticsHostInfo.environment = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.HOST_ENVIRONMENT.key);
        statisticsHostInfo.version = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.API_VERSION.key);

        if(statisticsHostInfo.region != null || statisticsHostInfo.environment != null ||statisticsHostInfo.version != null)
        {
            statistics.hostInfo = statisticsHostInfo;
        }

        statistics.enableSpeechRecognitionSpeakerProfile = commandConfigs.getBoolean(WebApiConfig.Property.ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE.key, false) ||
                commandConfigs.getBoolean(WebApiConfig.Property.KRYPTON_GRPC_USE_USERID.key, false);
        if(statistics.enableSpeechRecognitionSpeakerProfile){
            statistics.clientUserID = commandConfigs.getString(WebApiConfig.Property.CLIENT_USER_ID.key);
        }

        if(chainer != null) {
            var statisticsChainInfo = new StatisticsChainInfo();
            statisticsChainInfo.id = chainer.getChainId();
            statisticsChainInfo.index = chainer.getCurrentCommandIndex();
            statisticsChainInfo.isFinalCommand = chainer.getCommandCount() == (chainer.getCurrentCommandIndex() + 1);
            statistics.chainInfo = statisticsChainInfo;
            statistics.metadata = statistics.metadata != null && !statistics.metadata.isEmpty() ? chainer.getChainMetadata() : request.metadata;
        }

        statistics.metadata = request.metadata != null && !request.metadata.isEmpty() ? request.metadata : statistics.metadata;
    }

    /**
     * Starts the command
     */
    public void execute() {
        statistics.startTime = getCurrentUTCTime();
        startIdleTimer();
        executeImplementation();
    }

    protected abstract void executeImplementation();

    /**
     * When the endpoint receives a directive from the user, this method
     * will be called on the active command
     *
     * @param requestDirective
     *         The directive received from the user
     */
    public void onDirective(RequestDirective requestDirective, boolean isFinal, boolean fromIdleTimerFiring, String origin) {
        statistics.directives.add(new DirectiveStatistics(requestDirective.getName().toString(), origin, getCurrentUTCTime()));

        if (!fromIdleTimerFiring)
        {
            startIdleTimer();
        }

        onDirectiveImplementation(requestDirective, isFinal);
    }

    public void onDirective(RequestDirective requestDirective, boolean isFinal, String origin) {
        onDirective(requestDirective, isFinal, false, origin);
    }

    /**
     * Default behaviour to not handle directives.
     * @param requestDirective Request containing the directive.
     * @param isFinal Whether or not the response is final.
     */
     protected void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal) {
         final ResponseResult result = new ResponseResult(ResponseResult.ResponseStatus.CANCELLED, true);
         responseHandler.onResponse(result);
     }

    /**
     * Terminates the current command.
     */
    protected void terminateCommand() {
        cancelIdleTimer();
        final var sessionManager = SessionManager.getInstance();
        final var session = sessionManager.getSession(request.sessionId, false);
        if (session != null) {
            session.setCommand(null);
        }
    }

    /**
     * When the endpoint receives audio from the user, this method
     * will be called on the active command
     *
     * @param audio
     *         The audio received from the user
     */
    public void onAudio(byte[] audio) {
        if (statistics.firstAudioBufferReceivedTime == null)
        {
            statistics.firstAudioBufferReceivedTime = getCurrentUTCTime();
        }
        statistics.lastAudioBufferReceivedTime = getCurrentUTCTime();

        //Track Total Number Of Audio Packets Received during Speech Reco
        ++statistics.packetsReceived;
        
        onAudioImplementation(audio);
    }

    /**
     * Default behaviour to ignore streamed audio
     * @param audio The audio received as a byte array
     */
    protected void onAudioImplementation(byte[] audio) {
        String simpleClassName = this.getClass().getSimpleName().replace("Command", "");
        logger.info("audio received FROM the client application during {}.", simpleClassName);
    }

    /**
     * Method invoked to track responses sent back if needed
     *
     * @param response
     *         The response sent back
     */
    public boolean onResponse(ResponseResult response)
    {
        statistics.endTime = getCurrentUTCTime();

        if(response.isFinal() && hasSentFinalResponse)
        {
            return false;
        }

        hasSentFinalResponse = response.isFinal();

        if (!response.isFinal())
        {
            if (statistics.firstIntermediateResultTime == null)
            {
                statistics.firstIntermediateResultTime = getCurrentUTCTime();
                response.setIsFirstIntermediateResponse(true);
            }
            statistics.lastIntermediateResultTime = getCurrentUTCTime();
        } else {
            if(chainer != null) {
                statistics.chainInfo.isFinalCommand |= response.getStatus() == ResponseResult.ResponseStatus.ERROR;
            }
            cancelIdleTimer();
            if (statistics.startTime != null && statistics.endTime != null) {
                statistics.runTimeMs = statistics.endTime.getTimeInMillis() - statistics.startTime.getTimeInMillis();
            }
            if (COMMAND_DURATION_HISTOGRAM != null && statistics.runTimeMs != null)
            {
                COMMAND_DURATION_HISTOGRAM.labels(request.requestCommand.name)
                                          .observe((statistics.runTimeMs) /
                                       (double) 1000);
            }
        }

        if (response.isFinal() &&
                request.requestCommand.parameters != null && // Possible because endSession doesn't have parameters
                request.requestCommand.parameters.containsKey(WebApiConfig.Property.STATISTICS.key) &&
                (boolean) request.requestCommand.parameters.get(WebApiConfig.Property.STATISTICS.key))
        {
            response.setStatistics(new ResponseStatistics(statistics));
        }

        updateMetrics(response);
        return onResponseImplementation(response);
    }

    private void updateMetrics(ResponseResult response) {
        String errorCodeStr = null;
        if(response.getStatus().equals(ResponseResult.ResponseStatus.ERROR))
        {
            List<ResponseCode> errors = response.getErrors();
            if (!errors.isEmpty()) {
                ResponseCode rc = errors.get(0);
                errorCodeStr = Integer.toString(rc.getCode());
            }

            WebApiEndpoint.commandErrorsCounter.labels(
                    MetricsConstants.nonNullLabelValue(request.tenantKey),
                    MetricsConstants.nonNullLabelValue(request.requestCommand.name),
                    MetricsConstants.nonNullLabelValue(errorCodeStr))
                    .inc();
        }
    }

    /**
     * Default behaviour to allow responses to be sent back
     * @param response The response that would be sent
     */
    protected boolean onResponseImplementation(ResponseResult response) {
        return true;
    }

    /**
     * Method invoked to track events sent back if needed
     *
     * @param event
     *         The event sent back
     */
    public boolean onEvent(ResponseEvent event)
    {
        statistics.events.add(new EventStatistics(event.getName(), getCurrentUTCTime()));
        return onEventImplementation(event);
    }

    /**
     * Callbacks when events are being sent back to the client (such as end of speech)
     * @param event Which event is being sent
     * @return Whether the event should be sent back to the client or not
     */
    protected boolean onEventImplementation(ResponseEvent event)
    {
        return true;
    }

    /**
     * Method invoked to track audio responses sent back if needed
     *
     * @param audio
     *         The audio sent back
     * @param isLastBuffer
     *         True if this is the last audio buffer
     */
    public boolean onAudioResponse(byte[] audio, boolean isLastBuffer)
    {
        if (statistics.firstAudioBufferSentTime == null)
        {
            statistics.firstAudioBufferSentTime = getCurrentUTCTime();
        }
        // We don't use the isLastBuffer boolean because its size is 0 and the response is stopped at the VocalizerNimClient
        statistics.lastAudioBufferSentTime = getCurrentUTCTime();
        return onAudioResponseImplementation(audio, isLastBuffer);
    }

    /**
     * Default behaviour is to not send audio
     * @param audio The audio meant to be sent back to the client
     * @param isLastBuffer Whether this is the last buffer or not
     */
    protected boolean onAudioResponseImplementation(byte[] audio, boolean isLastBuffer) {
        return false;
    }

    /**
     * Creates the appropriate response formatter
     *
     * @return The ResponseFormatter
     *
     * @see ResponseFormatter
     */
    public ResponseFormatter getResponseFormatter() {
        return new BaseResponseFormatter();
    }


    /**
     * Set the response handler for this command.
     *
     * @param responseHandler
     *         the response handler.
     */
    public void setResponseHandler(ResponseHandler responseHandler)
    {
        this.responseHandler = responseHandler;
    }

    /**
     * Get the response handler for this command.
     *
     * @return the response handler.
     */
    public ResponseHandler getResponseHandler()
    {
        return responseHandler;
    }

    /**
     * Gets the command identifier
     *
     * @return the command identifier
     */
    public String getCommandId()
    {
        return request.requestCommand.id;
    }

    /**
     * Gets the command name
     *
     * @return the command name
     */
    public String getCommandName()
    {
        return request.requestCommand.name;
    }

    /**
     * To accommodate unit tests
     *
     * @return the client factory
     */
    public ClientFactory getClientFactory()
    {
        return new ClientFactory();
    }

    /**
     * Helper method to get UTC time for statistics
     *
     * @return The UTC time stamp
     */
    private Calendar getCurrentUTCTime()
    {
        var timeZone = TimeZone.getTimeZone("UTC");
        return Calendar.getInstance(timeZone);
    }

    public String getSessionId()
    {
        return currentSessionID;
    }

    public CommandStatistics getStatistics()
    {
        return statistics;
    }

    Encryptor getEncryptor()
    {
        if (encryptor != null)
        {
            return encryptor;
        }

        //should never execute
        return new Encryptor();
    }

    public static ObjectMapper getMapper()
    {
        if (mapper == null)
        {
            mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        return mapper;
    }

    protected Object encryptJSONObject(Encryptor encryptor, Object object) {
        Object returnedObject = null;
        if(object instanceof Map) {
            Map<String, Object> processedObject = new HashMap<>();
            Map<String, Object> jsonObject = (Map) object;
            for (Object key : jsonObject.keySet()) {
                Object encryptedValue = encryptJSONObject(encryptor, jsonObject.get(key));
                processedObject.put((String)key, encryptedValue);
            }
            returnedObject = processedObject;
        } else if(object instanceof List) {
            List<Object> processedObject = new ArrayList<>();
            List<Object> jsonArray = (List) object;
            for (Object obj : jsonArray) {
                processedObject.add(encryptJSONObject(encryptor, obj));
            }
            returnedObject = processedObject;
        } else if(object instanceof String) {
            returnedObject = encryptor.processString((String)object);
        } else {
            returnedObject = object;
        }
        return returnedObject;
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        if (currentSession == null)
        {
            String sessionId = getSessionId();
            try
            {
                currentSession = SessionManager.getInstance().getSession(sessionId, false);
            }
            catch (NullPointerException e)
            {
                //Session does not exist
                logger.info("EDF formatting -- Session does not exist!");
                return getCommandLogNoSession();
            }
        }

        var requestHeader = getRequestHeader();

        if (null != currentSession) userInfo = new UserInfo(currentSession);

        var commandLog = new CommandLog();
        commandLog.requestHeader = requestHeader;

        var requestLog = new RequestLog();
        requestLog.command = getAPIName();

        requestLog.stepID = currentSession != null ? currentSession.getStepId() : 0;

        Optional<String> clientDeviceId = userInfo != null ? Optional.ofNullable(userInfo.clientDeviceId) : Optional.ofNullable(null);

        if ( clientDeviceId.isPresent())
        {
            var pt = Pattern.compile("[^a-zA-Z0-9\\.-]");
            var match= pt.matcher(clientDeviceId.get());
            if(match.find())
            {
                userInfo.clientDeviceId = "INVALID";
            }
        }
        requestLog.userInfo = userInfo;

        Map<String, Object> startSessionState = SessionManager.getInstance().getSessionEncryptionStateMap();

        List<String> paramKeysToBeRemoved = new ArrayList<>();
        paramKeysToBeRemoved.add(WebApiConfig.Property.VERIFICATION_CODE.key);
        paramKeysToBeRemoved.add(WebApiConfig.Property.AUTH_DATA.key);

        if (currentSession != null)
        {
            List<String> toRemove = currentSession.getSessionConfig().getList(String.class, WebApiConfig.Property.COMMAND_PARAMS_TO_REMOVE_FROM_LOGS.key);
            paramKeysToBeRemoved.addAll(toRemove);
        }

        Optional<Map<String, Object>> mapOptional = Optional.ofNullable(request.requestCommand.parameters);

        Optional<Object> sensitivityParam = getSensitivityParam(startSessionState, mapOptional);

        setEncryptorAndSensitivity(requestLog, sensitivityParam);

        for (String toRemove : paramKeysToBeRemoved) {
            request.requestCommand.parameters.remove(toRemove);
        }

        fillResponseCode((Response) response, requestLog);

        handleInvalidClientId();

        requestLog.params = request.requestCommand.parameters;

        requestLog.statistics = getStatistics();
        requestLog.sessionId = getSessionId();
        commandLog.session = currentSession;
        commandLog.request = requestLog;

        return commandLog;
    }

    private Optional<Object> getSensitivityParam(Map<String, Object> startSessionState, Optional<Map<String, Object>> mapOptional) {
        Optional<Object> sensitivityParam;
        if (startSessionState.get(getSessionId()) != null)
        {
            sensitivityParam = Optional.of(startSessionState.get(getSessionId()));
        }
        else
        {
            sensitivityParam = Optional.ofNullable(currentSession != null ? currentSession.getSessionConfig().getString(WebApiConfig.Property.SENSITIVITY.key) : null);
        }

        if (mapOptional.isPresent() && mapOptional.get().get(WebApiConfig.Property.SENSITIVITY.key) != null)
        {
            logger.debug("Sensitivity parameter passed for {}", getCommandName());
            //start session master switch

            if (Optional.ofNullable(mapOptional.get().get(WebApiConfig.Property.VERIFICATION_CODE.key)).isPresent())
            {
                startSessionState.put(getSessionId(), mapOptional.get().get(WebApiConfig.Property.SENSITIVITY.key));
                logger.debug("STARTSESSIONSTATE SET TO {}", startSessionState.get(getSessionId()));
            }
            else
            {
                sensitivityParam = Optional.of(mapOptional.get().get(WebApiConfig.Property.SENSITIVITY.key));
            }
        }
        return sensitivityParam;
    }

    private void setEncryptorAndSensitivity(RequestLog requestLog, Optional<Object> sensitivityParam) {
        if (sensitivityParam.isPresent())
        {
            if (encryptor == null)
            {
                encryptor = new Encryptor();
                var tenantPublicKey = currentSession.getSessionConfig().getString(WebApiConfig.Property.PUBLIC_KEY.key);
                var clientPublicKey = currentSession.getSessionConfig().getString(WebApiConfig.Property.CLIENT_PUBLIC_KEY.key);

                if(!clientPublicKey.isEmpty()) {
                    tenantPublicKey = clientPublicKey;
                }

                if (!tenantPublicKey.isEmpty()
                        && org.apache.commons.codec.binary.Base64.isBase64(tenantPublicKey))
                {
                    encryptor.setTenantPublicKey(Base64.getDecoder().decode(tenantPublicKey));
                }
            }

            switch (CommandSensitivityType.valueOf(sensitivityParam.get().toString().toUpperCase())) {
                case ENCRYPTED:
                    encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.ENCRYPT);
                    break;
                case MASKED:
                    encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.MASK);
                    break;
                case OPEN:
                default:
                    sensitivityParam = Optional.of(CommandSensitivityType.OPEN.name());
                    break;
            }
            requestLog.sensitivity = sensitivityParam.get().toString();
        }
        else
        {
            requestLog.sensitivity = CommandSensitivityType.OPEN.name();
        }
    }

    private RequestHeader getRequestHeader() {
        var requestHeader = new RequestHeader();

        requestHeader.tenant = request.tenantKey;
        requestHeader.clientID = commandConfigs.getInt(WebApiConfig.Property.EDF_CLIENT_ID.key);
        requestHeader.timeStamp = Long.toString(System.currentTimeMillis());
        requestHeader.sessionStartTime = currentSession != null ? Long.toString(currentSession.getStartSessionTime().getTimeInMillis()) : "";
        requestHeader.seqNum = currentSession != null ? currentSession.getStepId() : 0;
        requestHeader.edfVersion = CommandLog.EDF_VERSION;
        requestHeader.applicationID = CommandLog.WAPI_APPLICATION_ID;
        requestHeader.environment = commandConfigs.getString(WebApiConfig.Property.HOST_ENVIRONMENT.key);
        requestHeader.clientApplicationID = commandConfigs.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key);
        requestHeader.clientApplicationVersion = commandConfigs.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key);
        requestHeader.customerIDs = new JSONArray(); //Been a placeholder for a while
        requestHeader.customerIDsOld = new JSONArray(); //Been a placeholder for a while
        requestHeader.channel = commandConfigs.getString(WebApiConfig.Property.FLUENT_LOG_CHANNEL.key);
        return requestHeader;
    }

    private void fillResponseCode(Response response, RequestLog requestLog) {
        Optional<Response> responseOptional = Optional.ofNullable(response);
        if (responseOptional.isPresent())
        {
            ResponseResult.ResponseStatus status = responseOptional.get().getResult().getStatus();
            requestLog.outcome = status.getName();
            switch(status)
            {
                case SUCCESS:
                case NO_MATCH:
                case NO_INPUT:
                case CANCELLED:
                    requestLog.responseCode = status.getHttpEquivalent();
                    requestLog.httpEquivalentStatusCode = status.getHttpEquivalent();
                    break;

                case ERROR:
                    var responseCode = responseOptional.get().getResult().getErrors().get(0);
                    requestLog.responseCode = responseCode.getCode() + "";
                    requestLog.httpEquivalentStatusCode = responseCode.getHttpEquivalentStatusCode() + "";
                    break;

                default:
                    logger.error("Outcome not handled: {}", status);
            }
        }
    }

    private void handleInvalidClientId() {
        Map<String, Object> parameters = request.requestCommand.parameters;
        var clientDeviceId = Optional.ofNullable(parameters.get(WebApiConfig.Property.CLIENT_DEVICE_ID.key));

        if ( clientDeviceId.isPresent())
        {
            var pt = Pattern.compile("[^a-zA-Z0-9\\.-]");
            var match= pt.matcher(clientDeviceId.get().toString());
            if(match.find())
            {
                parameters.put(WebApiConfig.Property.CLIENT_DEVICE_ID.key, "INVALID");
            }
        }
    }

    /**
     * Modify variables of the Response object so that they play nicely with Jackson's ObjectMapper API.
     * @param response The response to modify
     * @return a Response without duplicate params.
     */
    Response prettyFormatResponse(Response response)
    {
        var requestCommand = response.getEdfCopy();
        requestCommand.parameters = null;
        response.setEdfCopy(requestCommand);
        return response;
    }

    public String getAPIName()
    {
        return request.requestCommand.name;
    }

    CommandLog getCommandLogNoSession()
    {
        var commandLog = new CommandLog();
        var requestLog = new RequestLog();
        requestLog.command = getAPIName();
        requestLog.statistics = getStatistics();
        requestLog.params.remove(WebApiConfig.Property.VERIFICATION_CODE.key);
        requestLog.params.remove(WebApiConfig.Property.AUTH_DATA.key);
        commandLog.request = requestLog;
        return commandLog;
    }

    protected void sendEvent(ResponseEvent event)
    {
        responseHandler.sendEvent(event);
    }

    protected void sendResult(ResponseResult result)
    {
        responseHandler.onResponse(result);
    }

    private void startIdleTimer() {
        var timeout = commandConfigs.getInt(WebApiConfig.Property.COMMAND_TIMEOUT_MILLISECONDS.key);
        logger.debug("startIdleTimer {}", timeout);
        cancelIdleTimer();
        if (timeout > 0) {
            timerTask = ScheduledTaskUtils.scheduleTask(() ->
            {
                logger.debug("IdleTimer firing for command: {}, session: {}", this.getClass().getName(), currentSessionID);
                cancel(true, "Idle command timer");

                getErrorHandler(logger).handleError(new ResponseResult(
                        ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_IDLE_FOR_TOO_LONG)));
            }, timeout, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelIdleTimer() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }

    public void cancel(boolean fromIdleTimerFiring, String origin)
    {
        var cancel = new RequestDirective();
        cancel.setName(RequestDirective.RequestDirectiveName.CANCEL);
        this.onDirective(cancel, false, fromIdleTimerFiring, origin);
    }

    public void cancel(String origin)
    {
        cancel(false, origin);
    }

    protected EdfLogForwarder getEDFLogForwarder(){
        var edfLogForwarder = new EdfLogForwarder();
        String commandChainLogType = commandConfigs.getString(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key);
        boolean sendToChain = (chainer != null && !commandChainLogType.equalsIgnoreCase(CommandChain.CommandChainLogType.PER_REQUEST.toString()));
        boolean sendToLogs = (chainer == null || !commandChainLogType.equalsIgnoreCase(CommandChain.CommandChainLogType.SUMMARY_ONLY.toString()));

        if(sendToChain) {
            edfLogForwarder.addListener((EdfLogListener) chainer);
        }

        if(sendToLogs) {
            if (commandConfigs.getBoolean(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key)) {
                edfLogForwarder.addListener(new EdfFluentbitListener());
            }
            if (commandConfigs.getBoolean(WebApiConfig.Property.ENABLE_EVENTHUB_LOG_POSTER.key)) {
                edfLogForwarder.addListener(new EventHubListener());
            }
        }
        return  edfLogForwarder;
    }

    protected EdfMessageQueueable getEdfMessageQueueable(CommandLog commandLog){
        if(commandConfigs.getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key).equalsIgnoreCase(NEAP_V1_LOG_FORMAT_NAME)){
            return new NeapLog(getCommandName(), commandLog, commandConfigs);
        }
        else if(commandConfigs.getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key).equalsIgnoreCase(NEAP_V2_LOG_FORMAT_NAME)){
            return new EventHubLog(getCommandName(), commandLog, commandConfigs);
        }
        logger.error("Fluent log format not defined properly. Refer to tenant config documentation for field fluentLogFormat");
        return null;
    }

    /**
     * Get active session, if any, based on request sessionId
     */
    protected Session getActiveSession()
    {
        return SessionManager.getInstance()
                .getSession(request.sessionId, false);
    }

    /**
     * Reset active command in session when the command is done
     */
    protected void resetSessionActiveCommand()
    {
        terminateCommand();
    }

    /**
     * Log request
     *
     * @param request
     *         The request sent by the client application
     */
    protected void logRequest(Request request, Logger logger)
    {
        try
        {
            // Get json
            String json = Request.toJson(request);

            if (!logger.isTraceEnabled())
            {
                // Mask 'data' in json parameter for PCI
                String regex = "(\"" + WebApiConfig.Property.COMMAND_DATA.key + "\":\")([^\"]*)(\")";
                json = json.replaceAll(regex, "$1-log level should be TRACE to see data-$3");
            }

            // Log
            logger.debug("logRequest - got request: {}", json);
        }
        catch (CobraException e)
        {
            logger.error("logRequest - can't log received request since can't get json from request - error: {}",
                    e.getMessage());
        }
    }

    @Override
    public String formatResponse(Object objectResponse)
            throws CobraException
    {
        logger.trace("Command formatResponse - sessionId: {}", request.sessionId);
        var response = Response.fromRequest(request);
        response.setResult((ResponseResult)objectResponse);

        int errorCode;
        try
        {
            errorCode = response.getErrors().get(0).getCode();
        } catch(Exception e)
        {
            errorCode = -1;
        }

        if (response.getResult().isFinal() &&
            errorCode != ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR.getValue()
            )
        {
            reportBillableEvent(response);
        }

        return response.toJson();
    }

    protected void reportBillableEvent(Response response) {
        var commandLog = formatLog(response);

        if(commandConfigs.getNumberOfConfigurations() == 0)
        {
            commandConfigs = WebApiConfigManager.getInstance();
        }
        getEDFLogForwarder().forwardLog(getEdfMessageQueueable(commandLog), commandConfigs);
    }
}
