package com.nuance.cobra.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SpeechSynthesisClient;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.command.GetResourcesCommand.IResources;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.audio.WavDumperNode;
import com.nuance.cobra.utils.config.ConfigHelpers;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.request.VOCSVoiceConfigUtil;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import io.prometheus.client.Histogram;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * SpeechRecognition command. This command will start a speech synthesis command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/speech-recognition-command.md
 * for documentation.
 *
 */
public class SpeechSynthesisCommand
        extends Command
        implements EDFResponse
{
    protected static final Logger logger = LogManager.getLogger(SpeechSynthesisCommand.class);
    private SpeechSynthesisClient speechSynthesisClient = null;

    public static final String TUNABLE_DATA = "tuneable_data";
    public static final String USER_DICTIONARY = "user_dictionary";

    protected byte[] audioBytes = null;

    /**
     * Prometheus metrics for measuring the time between the time the command is received to when the first results are available.
     */
    protected static final Histogram COMMAND_TO_FIRST_AUDIO_DURATION_HISTOGRAM =
            Histogram.build("webapi_tts_command_to_first_audio_duration_seconds",
                            "Speech synthesis command received to first audio duration.")
                    .labelNames(MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                    .register();
    protected Histogram.Timer commandToFirstAudioDurationHistogramTimer;

    @JsonPropertyOrder({"name","voice"})
    public static class VoiceObject{
        private String name;
        private String language;
        private String voiceAge;
        private String voiceGender;
        private List<VoiceDescription> voices;

        @JsonView(Views.Visible.class)
        @JsonProperty("voice")
        public List<VoiceDescription> getVoices(){
            return this.voices;
        }

        @JsonView(Views.Visible.class)
        @JsonProperty("language")
        public String getLanguage(){
            return this.language;
        }

        @JsonView(Views.Visible.class)
        @JsonProperty("voice_age")
        public String getVoiceAge(){
            return this.voiceAge;
        }

        @JsonView(Views.Visible.class)
        @JsonProperty("voice_gender")
        public String getVoiceGender(){
            return this.voiceGender;
        }

        @JsonView(Views.Visible.class)
        @JsonProperty("name")
        public String getName(){
            return this.name;
        }

        VoiceObject(String name){
            this.name = name;
            this.voices = new ArrayList<>();
        }

        public VoiceObject(String name, List<VoiceDescription> voices){
            this.name = name;
            this.voices = voices;
        }

        public void setLanguage(String language){
            this.language = language;
        }

        public void setVoiceAge(String voiceAge){
            this.voiceAge = voiceAge;
        }

        public void setVoiceGender(String voiceGender){
            this.voiceGender = voiceGender;
        }

        void addVoiceDescription(VoiceDescription vd){
            this.voices.add(vd);
        }
    }

    @JsonPropertyOrder({"codec","voice_model"})
    public static class VoiceDescription{
        private String codec;
        private String voiceModel;

        @JsonView(Views.Visible.class)
        @JsonProperty("codec")
        public String getCodec(){
            return this.codec;
        }

        @JsonView(Views.Visible.class)
        @JsonProperty("voice_model")
        public String getVoiceModel(){
            return this.voiceModel;
        }

        VoiceDescription(String codec, String voiceModel){
            this.codec = codec;
            this.voiceModel = voiceModel;
        }

    }

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public SpeechSynthesisCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    /**
     * Log response to send
     */
    protected void logResponse(ResponseResult responseResult)
    {
        try
        {
            var responseResultString = responseResult.toJson();
            logger.debug("send result: {}", responseResultString);
        }
        catch (Exception e)
        {
            logger.warn("can't log response to send since can't get json from response - error: {}", e.getMessage());
        }
    }

    /**
     * Starts the command
     */
    @Override
    public void executeImplementation()
    {
        // Log
        logRequest(request, logger);
        // Get current session
        Session session = null;
        CombinedConfiguration requestConfig;
        if (getSessionId()!=null)
        {
            session = getActiveSession();
            if (session == null)
            {
                sendErrorResponse(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR);
                return;
            }

            try
            {
                requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);
            }
            catch (IllegalArgumentException e)
            {
                sendErrorResponse(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR, e.getMessage());
                return;
            }
            if(!checkTTSTypeSSML(requestConfig))
            {
                sendErrorResponse(ExternalResponseErrorCode.TTS_TYPE_SSML_ERROR);
                return;
            }
            session.setCommand(this);
        }
        else
        {
            requestConfig = CobraConfigurationUtils.lookupTenantConfigAndAddParams(request.tenantKey, request.requestCommand.parameters);
            if(!checkTTSTypeSSML(requestConfig))
            {
                sendErrorResponse(ExternalResponseErrorCode.TTS_TYPE_SSML_ERROR);
                return;
            }
        }

        String voice = getVoice(requestConfig);
        commandToFirstAudioDurationHistogramTimer = COMMAND_TO_FIRST_AUDIO_DURATION_HISTOGRAM
                    .labels(voice, MetricsConstants.nonNullLabelValue(request.tenantKey)).startTimer();

        // Get all parameters for the command

        statistics.dataLength = ( requestConfig.getString(WebApiConfig.Property.COMMAND_DATA.key)).length();
        addVoiceQualityAndModelToStatistics(session, requestConfig);

        // Creation of the speech synthesis client
        var clientFactory = new ClientFactory();
        speechSynthesisClient = clientFactory.createSpeechSynthesisClient(
                requestConfig,
                request.sessionId,
                getResponseHandler()
        );

        if(speechSynthesisClient == null)
        {
            terminateCommand();
            return;
        }

        // Start speech synthesis
        speechSynthesisClient.startSpeechSynthesis();
    }

    @NotNull
    protected String getVoice(CombinedConfiguration requestConfig) {
        HierarchicalConfiguration<ImmutableNode> voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(requestConfig);
        String voice = null;
        if (null != voiceConfig) {
            voice = voiceConfig.getString("name");
        }
        if(voice == null) {
            voice = MetricsConstants.UNDEFINED_LABEL_VALUE;
        }
        return voice;
    }

    void addVoiceQualityAndModelToStatistics(Session session, CombinedConfiguration requestConfig)
    {
        if (session == null)
        {
            return;
        }

        VocalizerClients client;
        try
        {
            client = VocalizerClients.valueOf(session.getSessionConfig().getString(WebApiConfig.Property.VOCALIZER.key));
        } catch (Exception e)
        {
            return;
        }

        if (client == VocalizerClients.nvc)
        {
            statistics.setVoiceQuality(VOCSVoiceConfigUtil.getVoiceQualityForReporting(requestConfig));
            statistics.setVoiceModel(VOCSVoiceConfigUtil.getVoiceModel(requestConfig));
        }
    }

    /**
     * This method enforces 'ssml' TTS type to be enabled explicitly from tenant config.
     *
     * @param requestConfig The command configuration combined with the tenant config
     *
     * @return True if request allows ssml
     */
    boolean checkTTSTypeSSML(Configuration requestConfig)
    {
        if (null == requestConfig)
        {
            return false;
        }
        return null == requestConfig.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key) ||
               !requestConfig.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key)
                             .equalsIgnoreCase("ssml") ||
               requestConfig.getBoolean(WebApiConfig.Property.ALLOW_SSML.key);
    }

    /**
     * When the endpoint receives a directive from the client application, this method
     * will be called on the active command
     *
     * @param requestDirective
     *         The directive received from the client application
     */
    @Override
    public void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal)
    {
        if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.STOP)) {
            try {
                speechSynthesisClient.stopSpeechSynthesis();
            } catch (Exception e) {
                sendErrorResponse(ExternalResponseErrorCode.CANNOT_EXECUTE_DIRECTIVE_ERROR);
            }
        }
        else if(requestDirective.getName().equals(RequestDirective.RequestDirectiveName.CANCEL)) {
            speechSynthesisClient.cancelSpeechSynthesis(isFinal);
            terminateCommand();
        }
    }

    /**
     * Check if the command is done then reset active command
     *
     * @param response
     *          The formatted response received from the NIM client
     */
    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        if(commandConfigs.getBoolean(WebApiConfig.Property.TTS_AUDIO_CAPTURE.key) && response.isFinal()) {
            try {
                String basePath = WavDumperNode.getBasePath(commandConfigs);
                String tenant = commandConfigs.getString(WebApiConfig.Property.APPLICATION_KEY.key);
                String sessionId = currentSessionID;
                String commandId = getCommandId();
                String codec = commandConfigs.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key);

                LocalDate localDate = LocalDate.now();
                String delimiter = "/";
                basePath = basePath + "data/synthesisAudio/" + tenant + delimiter +
                        localDate.getYear() + delimiter + String.format("%02d", localDate.getMonthValue()) + localDate.getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.US) + delimiter + String.format("%02d", localDate.getDayOfMonth()) + delimiter + String.format("%02d", GregorianCalendar.getInstance()
                        .get(Calendar.HOUR_OF_DAY));

                Files.createDirectories(Paths.get(basePath));
                Files.write(Paths.get(basePath + delimiter + sessionId + "-" + commandId + "-" + codec), audioBytes);
            } catch (Exception e) {
                logger.error("Couldn't capture outgoing audio: {}", e.getMessage());
            }
        }

        // Log
        logResponse(response);
        // Reset active command if the command is done, make sure first that speechSynthesisClient exists, won't be
        // the case if this is an error parsing the request.
        if (speechSynthesisClient != null && speechSynthesisClient.isSpeechSynthesisDone())
        {
            resetSessionActiveCommand();
        }
        return true;
    }

    @Override
    public boolean onAudioResponseImplementation(byte[] audio, boolean isFinalBuffer)
    {
        if(commandConfigs.getBoolean(WebApiConfig.Property.TTS_AUDIO_CAPTURE.key)) {
            int audioSize = audio.length;
            if (this.audioBytes == null) {
                this.audioBytes = new byte[audioSize];
                System.arraycopy(audio, 0, this.audioBytes, 0, audioSize);
            } else {
                byte[] tmp = new byte[this.audioBytes.length + audioSize];
                System.arraycopy(this.audioBytes, 0, tmp, 0, this.audioBytes.length);
                System.arraycopy(audio, 0, tmp, this.audioBytes.length, audioSize);
                this.audioBytes = tmp;
            }
        }

        if (commandToFirstAudioDurationHistogramTimer != null) {
            commandToFirstAudioDurationHistogramTimer.observeDuration();
            commandToFirstAudioDurationHistogramTimer = null;
        }
        return true;
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        CommandLog baseResponse = super.formatLog(response);
        switch (CommandSensitivityType.valueOf(baseResponse.request.sensitivity.toUpperCase()))
        {
            case MASKED:
            case ENCRYPTED:
                logger.info("sensitivity is set to sensitivity, encrypting!");
                var dataString = baseResponse.request.params.get(WebApiConfig.Property.COMMAND_DATA.key).toString();
                dataString = getEncryptor().processString(dataString);
                logger.info("Encrypted string is {}", dataString);
                baseResponse.request.params.put(WebApiConfig.Property.COMMAND_DATA.key, dataString);
                break;
            default:
                break;
        }

        baseResponse.request.statistics = getStatistics();

        /*
        We need to do this to pretty-print the EDF response without duplicate parameters
         */
        baseResponse.request.response = prettyFormatResponse((Response)response);
        baseResponse.request.sessionId = getSessionId();

        try {
            var baseResponseString = baseResponse.toJson(false);
            logger.debug("{}--EDF Response : {} ", getCommandName(), baseResponseString);
        } catch (JsonProcessingException e) {
            logger.trace("Failed to log", e);
        }

        return baseResponse;
    }

    private static List<VoiceObject> getVoicesFromTenant(CombinedConfiguration config){
        List<VoiceObject> listOfVoices = new ArrayList<>();
        HierarchicalConfiguration<ImmutableNode> vocsConfig;

        try {
             vocsConfig = config.configurationAt("vocsMapping");
        }catch (ConfigurationRuntimeException cre){
            logger.error("There was an error fetching vocs mapping. {} ", cre.getMessage());
            return listOfVoices;
        }

        Set<String> uniqueKeys = ConfigHelpers.extractRootKeys(vocsConfig);

        for (String voice: uniqueKeys) {
            var voiceObject = getVoiceObject(vocsConfig, voice);
            listOfVoices.add(voiceObject);
        }

        return listOfVoices;
    }

    private static VoiceObject getVoiceObject(HierarchicalConfiguration<ImmutableNode> vocsConfig, String voice) {
        HierarchicalConfiguration<ImmutableNode> voiceDescription = vocsConfig.configurationAt(voice);
        var voiceObject = new VoiceObject(voice);

        if(voiceDescription.containsKey("language")){
            voiceObject.setLanguage(voiceDescription.getString("language"));
        }

        if(voiceDescription.containsKey("voice_age")){
            voiceObject.setVoiceAge(voiceDescription.getString("voice_age"));
        }

        if(voiceDescription.containsKey("voice_gender")){
            voiceObject.setVoiceGender(voiceDescription.getString("voice_gender"));
        }

        for (String modelKey: ConfigHelpers.extractRootKeys(voiceDescription)) {
            List<HierarchicalConfiguration<ImmutableNode>> keyDescriptions = voiceDescription.configurationsAt(modelKey);
            for (HierarchicalConfiguration<ImmutableNode> description : keyDescriptions) {
                if (ConfigHelpers.extractRootKeys(description).contains("voice_model")) {
                    VoiceDescription voiceDescriptionObject;
                    voiceDescriptionObject = new VoiceDescription(modelKey, voiceDescription.configurationAt(modelKey).getString("voice_model"));
                    voiceObject.addVoiceDescription(voiceDescriptionObject);
                }
            }
        }
        return voiceObject;
    }

    public static IResources getResources(CombinedConfiguration config){
        return new IResources() {
            @Override
            public String getName() {
                return CommandFactory.getCommandName(this.getClass());
            }

            @Override
            public Map<String, Object> getResourcesMap() {
                HashMap<String, Object> map = new HashMap<>();

                map.put("voices", getVoicesFromTenant(config));

                map.put("vocalizer", config.getString(WebApiConfig.Property.VOCALIZER.key));

                return map;
            }
        };
    }

    /**
     * Send error response
     *
     * @param errorCode
     *         The error code to build the response
     * @param args
     *         A list of strings (might be none) to build the response
     */
    protected void sendErrorResponse(ExternalResponseErrorCode errorCode, String... args)
    {
        var error = ResponseCode.createResponseError(errorCode, args);
        getErrorHandler(logger).handleError(new SpeechSynthesisResponse(error));
    }
}
