package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.command.SpeechSynthesisCommand;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.request.VOCSVoiceConfigUtil;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NvcRequestParams {

    private static final Logger logger = LogManager.getLogger();

    private final CombinedConfiguration config;

    public NvcRequestParams(CombinedConfiguration config) {

        this.config = config;
    }

    public CombinedConfiguration getConfig()
    {
        return config;
    }

    public String getApplicationKey()
    {
        return config.getString(WebApiConfig.Property.APPLICATION_KEY.key);
    }

    public String getUrl()
    {
        return VOCSVoiceConfigUtil.getVocsUrl(config, WebApiConfig.Property.NVC_GRPC_SERVER_URL.key);
    }

    public boolean shouldMaskUserUtterance()
    {
        String sensitivity = config.getString(WebApiConfig.Property.SENSITIVITY.key);
        if (sensitivity == null) return true;
        return !sensitivity.equals("open");
    }

    public String getVoiceName()
    {
        HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
        String rc = voiceConfig.getString("name");
        logger.debug("Returning {} as voice name", rc);
        return rc;
    }

    public String getVoiceModel()
    {
        HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
        HierarchicalConfiguration codecConfig = VOCSVoiceConfigUtil.getCodecConfig(voiceConfig, config);
        return codecConfig.getString("voice_model");
    }

    public String getVoiceLanguage()
    {
        HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
        return voiceConfig.getString("language");
    }

    public String getVoiceNvcVersion()
    {
        return VOCSVoiceConfigUtil.getVoiceNvcVersion(config);
    }

    public String getInputType() {
        return config.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key);
    }

    public String getInputBody() {
        return config.getString(WebApiConfig.Property.COMMAND_DATA.key);
    }

    public AudioCodec getCodec() {
        return AudioCodec.fromString(config.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key));
    }

    public SynthesizerProto.AudioFormat getAudioFormat()
    {
        AudioCodec speechSynthesisCodec = AudioCodec.fromString(config.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key));

        switch (speechSynthesisCodec.getEncoding())
        {
            case PCM:
            case SPEEX:
                return buildPcmAudioFormat(speechSynthesisCodec.getSampleRate());
            case OPUS:
                return buildPureOpusAudioFormat(speechSynthesisCodec.getSampleRate()); // is NOT NuanceTts.OggOpus, it is NuanceTts.Opus
            default:
                throw new IllegalArgumentException(String.format("Audio format '%s' is unsupported.", speechSynthesisCodec));
        }
    }

    private SynthesizerProto.AudioFormat buildPcmAudioFormat(int sampleRateHz) {
        return SynthesizerProto.AudioFormat.newBuilder()
                .setPcm(SynthesizerProto.PCM.newBuilder()
                        .setSampleRateHz(sampleRateHz))
                .build();
    }

    private SynthesizerProto.AudioFormat buildPureOpusAudioFormat(int sampleRateHz) { // is NOT NuanceTts.OggOpus, it is NuanceTts.Opus
        return SynthesizerProto.AudioFormat.newBuilder()
                .setOpus(SynthesizerProto.Opus.newBuilder()
                        .setSampleRateHz(sampleRateHz)
                        .setVbr(getVbr())
                        .setMaxFrameDurationMs(getMaxFrameDurationMs())
                )
                .build();
    }

    int getMaxFrameDurationMs()
    {
        int rc;

        try
        {
            HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
            HierarchicalConfiguration codecConfig = VOCSVoiceConfigUtil.getCodecConfig(voiceConfig, config);
            rc = codecConfig.getInt("maxFrameDuration");
        }
        catch (Exception e)
        {
            rc = 60;
            logger.info("Could not interpret maxFrameDuration setting, setting maxFrameDuration = {}", rc);
        }

        return rc;
    }

    SynthesizerProto.EnumVariableBitrate getVbr()
    {
        SynthesizerProto.EnumVariableBitrate rc;

        try
        {
            HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
            HierarchicalConfiguration codecConfig = VOCSVoiceConfigUtil.getCodecConfig(voiceConfig, config);
            String vbrString = codecConfig.getString("vbr");

            switch (vbrString)
            {
                case "on":
                    rc = SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_ON;
                    break;
                case "constrained":
                    rc = SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_CONSTRAINED;
                    break;
                case "off":
                default:
                    rc = SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF;
            }
        } catch (Exception e)
        {
            logger.info("Could not interpret vbr setting, turning off vbr.");
            rc = SynthesizerProto.EnumVariableBitrate.VARIABLE_BITRATE_OFF;
        }

        return rc;
    }

    public List<SynthesizerProto.SynthesisResource.Builder> getSynthesisResources() throws ExceptionWithExternalResponseErrorCode {
        List<SynthesizerProto.SynthesisResource.Builder> resources = new ArrayList<>();

        try {
            // voice config
            HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(config);
            addTunableDataToSynthesisResources(resources, voiceConfig);
            addUserDictionariesToSynthesisResources(resources, voiceConfig);

            // tuning bundle
            addTuningBundleToSynthesisResources(getTuningBundle(), resources);

        } catch (ExceptionWithExternalResponseErrorCode e) {
            throw e;
        } catch (Exception e) {
            throw new ExceptionWithExternalResponseErrorCode("Error while reading NVC TTSaaS resources.", e, ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_EXECUTE_ERROR);
        }
        return resources;
    }

    private void addTunableDataToSynthesisResources(List<SynthesizerProto.SynthesisResource.Builder> resources, HierarchicalConfiguration<ImmutableNode> voiceConfig) {
        List<Map> tunableDataList = voiceConfig.getList(Map.class, SpeechSynthesisCommand.TUNABLE_DATA, Collections.emptyList());
        for (Map<String, String> tunableData : tunableDataList) {
            switch (tunableData.get("content_type")) {
                case "application/x-vocalizer-activeprompt-db":
                    resources.add(SynthesizerProto.SynthesisResource.newBuilder()
                            .setType(SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB)
                            .setUri(tunableData.get("uri")));
                    break;
                case "application/x-vocalizer-rettt+text":
                    resources.add(SynthesizerProto.SynthesisResource.newBuilder()
                            .setType(SynthesizerProto.EnumResourceType.TEXT_USER_RULESET)
                            .setUri(tunableData.get("uri")));
                    break;
                default:
                    logger.error("Tenant configuration at 'vocsMapping.<voice>.tuneable_data[content_type]' is invalid. The current invalid 'tuneable_data' will be ignored.");
            }
        }
        try {
            if (tunableDataList.isEmpty() && voiceConfig.configurationAt(SpeechSynthesisCommand.TUNABLE_DATA) != null) {
                logger.error("Tenant configuration at 'vocsMapping.<voice>.tuneable_data' is supposed to be an array of 'tuneable_data'. The current invalid 'tuneable_data' will be ignored.");
            }
        } catch (Exception ignored) {
            // configurationAt throws an exception if key doesn't exist
        }
    }

    private void addUserDictionariesToSynthesisResources(List<SynthesizerProto.SynthesisResource.Builder> resources, HierarchicalConfiguration<ImmutableNode> voiceConfig) {
        List<Map> userDictionaries = voiceConfig.getList(Map.class, SpeechSynthesisCommand.USER_DICTIONARY, Collections.emptyList());
        for (Map<String, String> userDictionary : userDictionaries) {
            if (userDictionary.get("content_type").equalsIgnoreCase("application/edct-bin-dictionary")) {
                resources.add(SynthesizerProto.SynthesisResource.newBuilder()
                        .setType(SynthesizerProto.EnumResourceType.USER_DICTIONARY)
                        .setUri(userDictionary.get("uri")));
            }
            else {
                    logger.error("Tenant configuration at 'vocsMapping.<voice>.user_dictionary[content_type]' is invalid. The current invalid 'user_dictionary' will be ignored.");
            }
        }
        try {
            if (userDictionaries.isEmpty() && voiceConfig.configurationAt(SpeechSynthesisCommand.USER_DICTIONARY) != null) {
                logger.error("Tenant configuration at 'vocsMapping.<voice>.user_dictionary' is supposed to be an array of 'user_dictionary'. The current invalid 'user_dictionary' will be ignored.");
            }
        } catch (Exception ignored) {
            // configurationAt throws an exception if key doesn't exist
        }
    }

    /**
     * Returns an empty List if there's no tuning bundle.
     */
    private List<Map> getTuningBundle() throws ExceptionWithExternalResponseErrorCode {
        String speechSynthesisTuningBundleName = config.getString((WebApiConfig.Property.SPEECH_SYNTHESIS_TUNING_BUNDLE.key));

        if (speechSynthesisTuningBundleName == null || speechSynthesisTuningBundleName.isBlank()) {
            logger.debug("No speechSynthesisTuningBundle specified.");
            return Collections.emptyList();
        }

        HierarchicalConfiguration speechSynthesisTuningBundles;
        try {
            speechSynthesisTuningBundles = config.configurationAt(WebApiConfig.Property.SPEECH_SYNTHESIS_TUNING_BUNDLES.key);
        } catch (Exception e) {
            // configurationAt throws an exception if 'key' doesn't exist
            logger.info("Trying to use a speechSynthesisTuningBundle but tenant configuration contains no speechSynthesisTuningBundles map to choose from.", e);
            return Collections.emptyList();
        }

        List<Map> speechSynthesisTuningBundle;
        try {
            speechSynthesisTuningBundle = speechSynthesisTuningBundles.getList(Map.class, speechSynthesisTuningBundleName, Collections.emptyList());
        } catch (Exception e) {
            // getList throws an exception if 'key' maps to an object that is not compatible with a list of the specified class
            throw new ExceptionWithExternalResponseErrorCode("Tenant configuration at 'speechSynthesisTuningBundles.<tuning_bundle>' is supposed to be an array of JSON objects. The current invalid 'tuning_bundle' will be ignored.", e, ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLE_SYNTAX_ERROR);
        }

        if (speechSynthesisTuningBundle.isEmpty()) {
            throw new ExceptionWithExternalResponseErrorCode("Trying to use a speechSynthesisTuningBundle but tenant configuration defines no such speechSynthesisTuningBundle.", ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_TUNINGBUNDLE_ERROR);
        } else {
            return speechSynthesisTuningBundle;
        }
    }

    private void addTuningBundleToSynthesisResources(List<Map> tuningBundle, List<SynthesizerProto.SynthesisResource.Builder> synthesisResources) {
        for (Map<String, String> tuningResource : tuningBundle) {
            SynthesizerProto.EnumResourceType type = null;
            switch (tuningResource.get("content_type")) {
                case "application/x-vocalizer-rettt+text":
                    type = SynthesizerProto.EnumResourceType.TEXT_USER_RULESET;
                    break;
                case "application/edct-bin-dictionary":
                    type = SynthesizerProto.EnumResourceType.USER_DICTIONARY;
                    break;
                case "application/x-vocalizer-activeprompt-db":
                    type = SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB;
                    break;
                case "application/x-vocalizer-activeprompt-db;mode=automatic":
                    type = SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB_AUTO;
                    break;
                case "application/sdct-bin-dictionary":
                    type = SynthesizerProto.EnumResourceType.SYSTEM_DICTIONARY;
                    break;
                default:
                    logger.error("Tuning bundle resource at 'speechSynthesisTuningBundles.<tuning_bundle>[tuning_resource]' is invalid. The current invalid 'tuning_resource' will be ignored.");
            }
            if(type != null) {
                synthesisResources.add(SynthesizerProto.SynthesisResource.newBuilder()
                        .setType(type)
                        .setUri(tuningResource.get("uri")));
            }
        }
    }

    public SynthesizerProto.AudioParameters getAudioParameters() throws ExceptionWithExternalResponseErrorCode{
        SynthesizerProto.AudioParameters.Builder audioParams = SynthesizerProto.AudioParameters.newBuilder()
                .setAudioFormat(getAudioFormat());

        if (hasSpeakingRateFactor()) {
            audioParams.setSpeakingRateFactor(getSpeakingRateFactor());
        }

        if (hasAudioChunkDurationMs()) {
            audioParams.setAudioChunkDurationMs(getAudioChunkDurationMs());
        }

        if (hasTargetAudioLengthMs()) {
            audioParams.setTargetAudioLengthMs(getTargetAudioLengthMs());
        }

        setDisableEarlyEmission(audioParams);

        return audioParams.build();
    }

    public boolean hasAudioChunkDurationMs() {
        int duration = getAudioChunkDurationMs();
        return duration > 0 && duration <= 60000;
    }

    public boolean isAudioChunkDisabled() {
        int duration = getAudioChunkDurationMs();
        return duration == 0;
    }

    public int getAudioChunkDurationMs() {
        return config.getInt(WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.key, (int)WebApiConfig.Property.SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS.defaultValue);
    }

    public boolean hasTargetAudioLengthMs() {
        int duration = getTargetAudioLengthMs();
        return duration >= 0;
    }

    public int getTargetAudioLengthMs() {
        return config.getInt(WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.key, (int)WebApiConfig.Property.SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS.defaultValue);
    }

    public void setDisableEarlyEmission(SynthesizerProto.AudioParameters.Builder audioParams) {
        String key = WebApiConfig.Property.SPEECH_SYNTHESIS_DISABLE_EARLY_EMISSIONS.key;
        if(config.containsKey(key)) {
            audioParams.setDisableEarlyEmission(config.getBoolean(key));
        }
    }

    public boolean hasSpeakingRateFactor() {
        return config.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key) != null;
    }

    public float getSpeakingRateFactor() throws ExceptionWithExternalResponseErrorCode {

//      "slow": 0.5f
//      "normal": 1.0f
//      "fast": 2.0f

        try {
            String speakingRate = VOCSVoiceConfigUtil.getSpeechSynthesisRate(config);
            return Float.parseFloat(speakingRate);

        } catch (IllegalArgumentException e) {
            throw new ExceptionWithExternalResponseErrorCode(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key + " argument value is invalid.", e, ExternalResponseErrorCode.SPEECH_SYNTHESIS_RATE_ERROR);
        }
    }

    public boolean isStatisticsEnabled() {
        return config.getBoolean(WebApiConfig.Property.STATISTICS.key);
    }

    public void assertValidity() throws ExceptionWithExternalResponseErrorCode {
        if (hasSpeakingRateFactor()) {
            float speakingRateFactor = getSpeakingRateFactor();

            if (speakingRateFactor < 0.0f || speakingRateFactor > 2.0f) {
                throw new ExceptionWithExternalResponseErrorCode("Mix TTS as a service gRPC request: speakingRateFactor value is out of allowed range.", ExternalResponseErrorCode.SPEECH_SYNTHESIS_RATE_ERROR);
            }
        }
    }
}
