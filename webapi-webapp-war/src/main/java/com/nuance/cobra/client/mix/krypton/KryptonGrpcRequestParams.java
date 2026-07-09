package com.nuance.cobra.client.mix.krypton;

import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.krypton.KryptonUtils;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.DynamicVocabularyDb;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.rpc.asr.v1.*;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.nuance.cobra.client.mix.krypton.KryptonGrpcClient.*;

public class KryptonGrpcRequestParams {

    private final Logger logger;

    private final CombinedConfiguration config;
    private final List<DynamicVocabulary> dynamicVocabularies;

    protected Map<String, String> resourceIndexToIdMap = new HashMap<>();

    public enum DomainLmWeightType {
        ENUM,
        FLOAT
    }

    private Map<String, EnumWeight> domainLmWeightMappingStringToEnum = new ImmutableMap.Builder<String, EnumWeight>()
            .put("default", EnumWeight.DEFAULT_WEIGHT)
            .put("lowest", EnumWeight.LOWEST)
            .put("low", EnumWeight.LOW)
            .put("medium", EnumWeight.MEDIUM)
            .put("high", EnumWeight.HIGH)
            .put("highest", EnumWeight.HIGHEST)
            .build();

    public KryptonGrpcRequestParams(CombinedConfiguration config,
                                    Logger logger) {

        this(config, null, logger);
    }

    public KryptonGrpcRequestParams(CombinedConfiguration config,
                                    List<DynamicVocabulary> dynamicVocabularies,
                                    Logger logger) {

        this.config = config;
        this.dynamicVocabularies = dynamicVocabularies;
        this.logger = logger;
    }

    public String getUrl()
    {
        return KryptonUtils.getKryptonUrl(config, WebApiConfig.Property.KRYPTON_GRPC_SERVER_URL);
    }

    public EnumResultType getResultType() {
        if (config.getBoolean(WebApiConfig.Property.WORD_STREAM.key)) {
            var immutablePartialResults = config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_IMMUTABLE_PARTIAL_RESULTS.key);
            if(immutablePartialResults) {
                return EnumResultType.IMMUTABLE_PARTIAL;
            }
            return EnumResultType.PARTIAL;
        } else {
            return EnumResultType.FINAL;
        }
    }
    Map<String, String> addDlm(RecognitionInitMessage.Builder initMessage, String uri, String weight, String resourceName) throws ExceptionWithExternalResponseErrorCode
    {
        return addDlm(initMessage, uri, weight, getRequestTimeoutFromConfig(WebApiConfig.Property.KRYPTON_GRPC_DLM_RESOURCE_REQUEST_TIMEOUT_MS), resourceName);
    }

    Map<String, String> addDlm(RecognitionInitMessage.Builder initMessage, String uri, String weight, Integer requestTimeout, String resourceName) throws ExceptionWithExternalResponseErrorCode {
        if(uri == null) {
            throw new ExceptionWithExternalResponseErrorCode("Dlm uri is invalid because it is null", ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_DOMAIN_LM_URI);
        }

        Map<String, String> loadedDlm = new HashMap<>();
        loadedDlm.put(URI, uri);

        var resourceReferenceBuilder = ResourceReference.newBuilder()
                .setType(EnumResourceType.DOMAIN_LM)
                .setRequestTimeoutMs(requestTimeout)
                .setUri(uri);
        addResourceHeaders(resourceReferenceBuilder);

        RecognitionResource.Builder resource = RecognitionResource.newBuilder()
                .setExternalReference(resourceReferenceBuilder);

        if(weight != null) {
            var newWeight = convertWeight(weight);
            if (newWeight instanceof Float) {
                loadedDlm.put(WEIGHT, Float.toString((Float) newWeight));
                resource.setWeightValue((Float) newWeight);
            } else if (newWeight instanceof EnumWeight) {
                loadedDlm.put(WEIGHT, weight);
                resource.setWeightEnum((EnumWeight) newWeight);
            }
            logger.debug("Added domainLM weight: {} uri: {}", weight, uri);
        }
        addResource(initMessage, resource, resourceName);

        return loadedDlm;
    }

    private void addResource(RecognitionInitMessage.Builder initMessage, RecognitionResource.Builder resource, String resourceName) {
        resourceIndexToIdMap.put(Integer.toString(resourceIndexToIdMap.size() + 1), resourceName);
        initMessage.addResources(resource);
    }

    @java.lang.SuppressWarnings("java:S3740")
    List<Map<String, String>> addDlmsPerLang(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode {
        try {
            HierarchicalConfiguration<ImmutableNode> domainLmsPerLang = config.configurationAt(WebApiConfig.Property.KRYPTON_DOMAIN_LMS_PER_LANGUAGE.key);
            List<Map> domainLms = domainLmsPerLang.getList(Map.class, KryptonUtils.getLanguage(config));

            if(domainLms == null) {
                return new ArrayList<>();
            }

            List<Map<String, String>> loadedDlms = new ArrayList<>();
            for (Map domainLm : domainLms) {
                var uri = domainLm.get(URI).toString();
                String weight = domainLm.get(WEIGHT) != null ? domainLm.get(WEIGHT).toString() : null;
                Integer requestTimeout = getDlmRequestTimeout(domainLm);
                var id = domainLm.get("id") != null ? domainLm.get("id").toString() : WebApiConfig.Property.KRYPTON_DOMAIN_LMS_PER_LANGUAGE.key;
                loadedDlms.add(addDlm(initMessage, uri, weight, requestTimeout, id));
            }
            return loadedDlms;
        }
        catch (ConfigurationRuntimeException e) {
            return new ArrayList<>();
        }
    }

    public Integer getDlmRequestTimeout(Map dlm)
    {
        Integer timeout = null;

        if (dlm.containsKey(REQUEST_TIMEOUT))
        {
            Object timeoutObj = dlm.get(REQUEST_TIMEOUT);

            if (timeoutObj instanceof Integer)
            {
                timeout = (Integer) timeoutObj;
            }
            else if (timeoutObj instanceof String)
            {
                try
                {
                    timeout = Integer.parseInt((String) timeoutObj);
                }
                catch (NumberFormatException e)
                {
                    logger.warn("Unable to parse speech recognition resource 'requestTimeout'", e);
                }
            }
        }
        if (timeout == null)
        {
            timeout = getRequestTimeoutFromConfig(WebApiConfig.Property.KRYPTON_GRPC_DLM_RESOURCE_REQUEST_TIMEOUT_MS);
        }
        return timeout;
    }

    /**
     * returns the specified resource timeout property value from config, or fallback the common resource property
     * (for backward compatibility)
     */
    Integer getRequestTimeoutFromConfig(WebApiConfig.Property property) {
        return config.getInt(config.containsKey(property.key)
                ? property.key : WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key);
    }


    List<Map<String, String>> addKryptonDlm(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode {
        var uri = config.getString(WebApiConfig.Property.KRYPTON_DOMAIN_LM_URL.key);
        var weight = config.getString(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key);
        List<Map<String, String>> loadedDlms = new ArrayList<>();
        if(uri != null) {
            loadedDlms.add(addDlm(initMessage, uri, weight, WebApiConfig.Property.KRYPTON_DOMAIN_LM_URL.key));
        }
        return loadedDlms;
    }

    List<Map<String, String>> addDlmFromDialogModelConfig(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode {
        List<Map<String, String>> loadedDlms = new ArrayList<>();
        var contextTag = config.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key);
        if (!contextTag.equals(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.defaultValue))
        {
            var uri = String.format("urn:nuance-mix:tag:model/%s/mix.asr?=language=%s", contextTag, KryptonUtils.getLanguage(config));
            var weight = config.getString(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key);
            loadedDlms.add(addDlm(initMessage, uri, weight, contextTag));
        }
        return loadedDlms;
    }

    public List<Map<String, String>> addDlmFromDynamicVocabularies(RecognitionInitMessage.Builder initMessage) throws ExceptionWithExternalResponseErrorCode {
        List<Map<String, String>> loadedDlms = new ArrayList<>();
        if(dynamicVocabularies != null && !dynamicVocabularies.isEmpty())
        {
            loadedDlms = setVocabularyDLMs(initMessage);
            return loadedDlms;
        }
        return loadedDlms;
    }

    public List<Map<String, String>> setVocabularyDLMs(RecognitionInitMessage.Builder initMessage)
            throws ExceptionWithExternalResponseErrorCode
    {
        List<Map<String, String>> loadedDlms = new ArrayList<>();
        for (DynamicVocabulary dynVoc : dynamicVocabularies)
        {
            if(dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_DOMAINLM)
            {
                var uri = dynVoc.getUri();
                var weight = dynVoc.getWeight().toString();
                var id = dynVoc.getId();
                loadedDlms.add(addDlm(initMessage, uri, weight, id));
                logger.trace("Added DLM from dynamic vocabulary - uri: {}", uri);
            }
        }
        return loadedDlms;
    }

    public List<Map<String, Object>> addBuiltins(RecognitionInitMessage.Builder initMessage)
            throws CobraException, ExceptionWithExternalResponseErrorCode
    {
        List<Map> builtins = config.getList(Map.class, WebApiConfig.Property.SPEECH_RECOGNITION_BUILTINS.key);
        List<Map<String, Object>> loadedBuiltins = new ArrayList<>();

        if(builtins != null && !builtins.isEmpty()) {
            setConfigBuiltins(initMessage, builtins, loadedBuiltins);
        }

        if(dynamicVocabularies != null && !dynamicVocabularies.isEmpty())
        {
            setVocabularyBuiltins(initMessage, loadedBuiltins);
        }

        return loadedBuiltins;
    }

    public void setConfigBuiltins(RecognitionInitMessage.Builder initMessage, List<Map> builtins, List<Map<String, Object>> loadedBuiltins)
            throws ExceptionWithExternalResponseErrorCode
    {
        for (Map<String, Object> builtin : builtins) {
            var builtinName = builtin.get("builtin");
            RecognitionResource.Builder resource = RecognitionResource.newBuilder().setBuiltin(builtinName.toString());

            var weight = builtin.get("weight");
            if (weight != null) {
                var newWeight = convertWeight(weight.toString());
                if (newWeight instanceof Float) {
                    resource.setWeightValue((Float) newWeight);
                } else if (newWeight instanceof EnumWeight) {
                    resource.setWeightEnum((EnumWeight) newWeight);
                }
                logger.trace("Added builtin - weight: {}, name: {}", newWeight, builtinName);
                weight = newWeight;
            }

            addResource(initMessage, resource, builtinName.toString());
            addBuiltinToLoadedBuiltins(builtinName.toString(), weight, loadedBuiltins);
            logger.trace("Added builtin - name: {}", builtinName);
        }
    }

    public void setVocabularyBuiltins(RecognitionInitMessage.Builder initMessage, List<Map<String, Object>> loadedBuiltins)
            throws ExceptionWithExternalResponseErrorCode
    {
        for (DynamicVocabulary dynVoc : dynamicVocabularies)
        {
            if(dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN)
            {
                var builtin = dynVoc.getBuiltin();
                var weight = dynVoc.getWeight();
                var resource = RecognitionResource.newBuilder();
                resource.setBuiltin(builtin);

                if (weight != null) {
                    var newWeight = setBuiltinWeight(resource, weight);
                    logger.trace("Added builtin - weight: {}, name: {}", newWeight, builtin);
                    weight = newWeight;
                }

                addResource(initMessage, resource, builtin);
                addBuiltinToLoadedBuiltins(builtin, weight, loadedBuiltins);
                logger.trace("Added builtin - name: {}", builtin);
            }
        }
    }

    public Object setBuiltinWeight(RecognitionResource.Builder resource, Object weight)
            throws ExceptionWithExternalResponseErrorCode
    {
        var newWeight = convertWeight(weight.toString());
        if (newWeight instanceof Float) {
            resource.setWeightValue((Float) newWeight);
        } else if (newWeight instanceof EnumWeight) {
            resource.setWeightEnum((EnumWeight) newWeight);
        }
        return newWeight;
    }

    public Object convertWeight(String weight)
            throws ExceptionWithExternalResponseErrorCode
    {
        if (NumberUtils.isCreatable(weight)) {
            var weightValue = Float.parseFloat(weight);
            if (weightValue < 0) {
                throw new ExceptionWithExternalResponseErrorCode("Weight is invalid because it is < 0", ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_DOMAIN_LM_WEIGHT);
            } else if (weightValue > 1000) {
                throw new ExceptionWithExternalResponseErrorCode("Weight is invalid because it is > 1000", ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_DOMAIN_LM_WEIGHT);
            }
            if (weightValue > 1) {
                weightValue /= 1000.0f;
            }
            return weightValue;
        } else {
            var enumWeight = domainLmWeightMappingStringToEnum.get(weight.toLowerCase());
            if (enumWeight == null) {
                throw new ExceptionWithExternalResponseErrorCode(String.format("Weight is invalid because it is %s", weight), ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_DOMAIN_LM_WEIGHT);
            }
            return enumWeight;
        }
    }

    public void addBuiltinToLoadedBuiltins(String name, Object weight, List<Map<String, Object>> loadedBuiltins)
    {
        Map<String, Object> loadedBuiltin = new HashMap<>();
        loadedBuiltin.put("name", name);
        if (weight != null) loadedBuiltin.put("weight", weight);
        loadedBuiltins.add(loadedBuiltin);
    }

    public void addWordsets(RecognitionInitMessage.Builder initMessage)
            throws CobraException
    {
        loadPersistedWordsets(initMessage);

        if(dynamicVocabularies == null)
        {
            return;
        }

        for (DynamicVocabulary dynVoc : dynamicVocabularies)
        {
            if (dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET)
            {
                addResource(initMessage, RecognitionResource.newBuilder().setInlineWordset(dynVoc.getJsonBody()), dynVoc.getId());
            }
            if(dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET_PKG)
            {
                int requestTimeout = getRequestTimeoutFromConfig(WebApiConfig.Property.KRYPTON_GRPC_WORDSET_RESOURCE_REQUEST_TIMEOUT_MS);
                addResource(initMessage, RecognitionResource.newBuilder()
                                                            .setExternalReference(ResourceReference.newBuilder()
                                                                                                   .setUri(dynVoc.getUri())
                                                                                                   .setRequestTimeoutMs(requestTimeout)
                                                                                                   .setType(EnumResourceType.COMPILED_WORDSET)),
                        dynVoc.getId());
            }
        }
    }

    void setSpeakerProfile(RecognitionInitMessage.Builder initMessage)
            throws CobraException
    {
        if (config.getBoolean(WebApiConfig.Property.KRYPTON_GRPC_USE_USERID.key, false) ||
                config.getBoolean(WebApiConfig.Property.ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE.key, false)) {

            initMessage.addResources(RecognitionResource.newBuilder()
                    .setExternalReference(ResourceReference.newBuilder()
                            .setRequestTimeoutMs(getRequestTimeoutFromConfig(
                                    WebApiConfig.Property.KRYPTON_GRPC_SPEAKER_PROFILE_RESOURCE_REQUEST_TIMEOUT_MS))
                            .setType(EnumResourceType.SPEAKER_PROFILE)));
        }
    }


    void loadPersistedWordsets(RecognitionInitMessage.Builder initMessage)
    {
        if(!config.getBoolean(WebApiConfig.Property.LOAD_PERSISTED_WORDSETS.key)) { return; }

        Set<String> conceptNames = DynamicVocabularyDb.getPersistedWordsets(config);
        if(conceptNames.isEmpty()) { return; }

        for (String conceptName : conceptNames)
        {
            DynamicVocabulary dynamicVocab = DynamicVocabularyDb.getWordset(conceptName, config);
            if(dynamicVocab == null)
            {
                logger.warn("Couldn't get {} wordset, despite being listed in set", conceptName);
                continue;
            }
            try
            {
                addResource(initMessage, RecognitionResource.newBuilder().setInlineWordset(dynamicVocab.getJsonBody()), dynamicVocab.getId());
            } catch (CobraException e)
            {
                logger.warn("Couldn't activate {} wordset", conceptName);
            }
        }
    }

    public boolean shouldStopOnEndOfSpeech() {
        return config.getBoolean(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key);
    }

    public AudioFormat getAudioFormat() {
        var speechRecognitionCodec = AudioCodec.fromString(config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key));
        return AudioFormat.newBuilder()
                .setPcm(PCM.newBuilder()
                        .setSampleRateHz(speechRecognitionCodec.getSampleRate())).build();
    }

    public boolean isEndpointingActivated() {
        return config.getBoolean(WebApiConfig.Property.END_POINT_DETECTION.key);
    }

    public boolean shouldMaskUserUtterance()
    {
        //Explict setting of suppress_call_recording
        if(config.getProperty(WebApiConfig.Property.KRYPTON_SUPPRESS_CALL_RECORDING.key) != null)
        {
            var suppressCallRecording = config.getBoolean(WebApiConfig.Property.KRYPTON_SUPPRESS_CALL_RECORDING.key);
            logger.trace("suppress_call_recording = {}", suppressCallRecording);
            return suppressCallRecording;
        }

        //Implicit setting via sensitivity
        var sensitivity = config.getString(WebApiConfig.Property.SENSITIVITY.key);
        if (sensitivity == null) return true;
        return !sensitivity.equals("open");
    }

    public void buildMaxHypotheses(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        var maxHypotheses = config.getInt(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key);
        if (maxHypotheses < 1 || maxHypotheses > 10) return;
        recognitionParametersBuilder.setMaxHypotheses(maxHypotheses);
    }

    public void buildSpeechDomain(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        var speechDomain = config.getString(WebApiConfig.Property.SPEECH_DOMAIN.key);
        if (speechDomain == null) return;
        recognitionParametersBuilder.setSpeechDomain(speechDomain);
    }

    public void buildFormattingType(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        var formattingType = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_FORMATTING_TYPE.key);
        if (formattingType == null) return;
        var formatting =
                Formatting.newBuilder()
                        .setScheme(formattingType)
                        .build();
        recognitionParametersBuilder.setFormatting(formatting);
    }

    public String buildTopic(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        String topic = KryptonUtils.getTopic(config);
        if (topic == null) return "";
        recognitionParametersBuilder.setTopic(topic);
        return topic;
    }

    public void addResourceHeaders(ResourceReference.Builder resource)
    {
        try
        {
            HierarchicalConfiguration<ImmutableNode> resourceHeaders = config.configurationAt(WebApiConfig.Property.KRYPTON_GRPC_RESOURCE_HEADERS.key);
            Iterator<String> resourceHeaderKeys = resourceHeaders.getKeys();
            while(resourceHeaderKeys.hasNext())
            {
                String header = resourceHeaderKeys.next();
                var value = resourceHeaders.getString(header);
                logger.debug("Adding Krypton gRPC Resource Header:  {}: {}", header, value);
                resource.putHeaders(header, value);
            }
        } catch(Exception e)
        {
            //Just move on.
        }
    }

    public void setUtteranceEndSilenceMs(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        if (recognitionParametersBuilder.getUtteranceDetectionMode() == EnumUtteranceDetectionMode.DISABLED)
        {
            //2021.04.27 14:34:49.293 [grpc-default-executor-0][xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx] [No TenantID]
            //ERROR com.nuance.cobra.client.mix.krypton.KryptonGrpcResponseObserver - Received Krypton gRPC response,
            // Type=STATUS, StatusCode=400, Message=Bad request, MessageDetails=disabled utterance detection is incompatible with utteranceEndSilence > 0
            return;
        }

        var uttEndSilenceMs = config.getInt(WebApiConfig.Property.KRYPTON_UTTERANCE_END_SILENCE_MS.key);
        if(uttEndSilenceMs > 0)
        {
            logger.trace("utterance_end_silence = {}", uttEndSilenceMs);
            recognitionParametersBuilder.setUtteranceEndSilenceMs(uttEndSilenceMs);
        }
    }

    public void setSpeechDetectionSensitivty(RecognitionParameters.Builder recognitionParametersBuilder)
    {
        var speechDetectionSensitivty = config.getInt(WebApiConfig.Property.KRYPTON_SPEECH_DETECTION_SENSITIVITY.key);
        var speechDetectionSensitivtyFloat = (float) speechDetectionSensitivty / 1000f;
        if(speechDetectionSensitivtyFloat >= 0 && speechDetectionSensitivtyFloat <= 1.0)
        {
            logger.trace("speech_detection_sensitivty = {}", speechDetectionSensitivtyFloat);
            recognitionParametersBuilder.setSpeechDetectionSensitivity(speechDetectionSensitivtyFloat);
        }
    }

    public void setDiscardSpeakerAdaptation(RecognitionFlags.Builder recognitionFlagsBuilder)
    {
        if(config.getProperty(WebApiConfig.Property.KRYPTON_DISCARD_SPEAKER_ADAPTATION.key) == null)
        {
            return;
        }

        var discardSpeakerAdaptation = config.getBoolean(WebApiConfig.Property.KRYPTON_DISCARD_SPEAKER_ADAPTATION.key);
        logger.trace("discard_speaker_adaptation = {}", discardSpeakerAdaptation);
        recognitionFlagsBuilder.setDiscardSpeakerAdaptation(discardSpeakerAdaptation);
    }

    public boolean getStallTimers()
    {
        return !config.getBoolean(WebApiConfig.Property.START_RECOGNITION_TIMERS.key, true);
    }

}
