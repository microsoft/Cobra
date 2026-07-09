package com.nuance.plugin.kryptonws.params;

import com.google.gson.GsonBuilder;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.config.ConfigHelpers;
import com.nuance.cobra.utils.config.ReverseOverrideCombiner;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.utils.KryptonWsTimeDesignation;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class KryptonWsParameters
{
    private static final Logger logger = LogManager.getLogger(KryptonWsParameters.class);

    private final WebApiJSONConfiguration createSessionSessionParameters = new WebApiJSONConfiguration();
    private final WebApiJSONConfiguration createSessionRecognitionParameters = new WebApiJSONConfiguration();

    private CombinedConfiguration configurations;

    public static final String KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT = "noInputTimeout";
    public static final String KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT = "recognitionTimeout";
    public static final String KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE = "utteranceEndSilence";
    public static final String KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION = "disableUtteranceDetection";
    public static final String KRYPTON_WS_PARAM_NBEST_LIST_LENGTH = "nBestListLength";
    public static final String KRYPTON_WS_PARAM_SENSITIVTY = "speechDetectionSensitivity";

    public static final String KEY_EQUALS_VALUE = "{} = {}";
    public static final String DISABLED = "disabled";

    public static final String PLUGIN_PARAMS_NAME = "pluginSettings";
    public static final String CREATE_SESSION_SESSION_PARAMS_NAME = "kryptonWsCreateSessionSessionParameters";
    public static final String CREATE_SESSION_RECOGNITION_PARAMS_NAME = "kryptonWsCreateSessionRecognitionParameters";
    public static final String RECOGNIZE_RECOGNITION_PARAMS_NAME = "kryptonWsRecognizeRecognitionParameters";
    public static final String INTERPRETATION_PARAMS_NAME = "kryptonWsInterpretationParameters";

    public KryptonWsParameters(JSONObject sessionParametersJson, JSONObject recognitionParametersJson)
    {
        logger.trace("new CombinedConfiguration");
        configurations = new CombinedConfiguration(new ReverseOverrideCombiner());

        if (sessionParametersJson == null)
        {
            sessionParametersJson = new JSONObject();
        }

        if (recognitionParametersJson == null)
        {
            recognitionParametersJson = new JSONObject();
        }

        createSessionSessionParameters.mergeMap(sessionParametersJson.toMap());

        setDefaultRecognitionParams(createSessionRecognitionParameters);
        createSessionRecognitionParameters.mergeMap(recognitionParametersJson.toMap());

        init();
    }

    private void init()
    {
        parseSessionParameters();

        parseRecognitionParameters(createSessionRecognitionParameters);

        configurations.addConfiguration(createSessionSessionParameters, CREATE_SESSION_SESSION_PARAMS_NAME);
        configurations.addConfiguration(createSessionRecognitionParameters, CREATE_SESSION_RECOGNITION_PARAMS_NAME);
    }

    private void setDefaultRecognitionParams(WebApiJSONConfiguration recognitionParams)
    {
        recognitionParams.setProperty("autoEnd", true);
        recognitionParams.setProperty("autoPunctuation", true);
        recognitionParams.setProperty("enablePartialResults", true);
        recognitionParams.setProperty(KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT, DISABLED);
        recognitionParams.setProperty(KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT, DISABLED);
        recognitionParams.setProperty("startRecognitionTimers", true);
        recognitionParams.setProperty(KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE, "500ms");
        recognitionParams.setProperty(KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION, false);
        recognitionParams.setProperty("enableProfanityFilter", false);
        //speechDomain
        //formattingType
        recognitionParams.setProperty("immutablePartialResults", false);
        recognitionParams.setProperty("suppressCallRecording", false);
        recognitionParams.setProperty("maskLoadFailures", false);
        recognitionParams.setProperty(KRYPTON_WS_PARAM_NBEST_LIST_LENGTH, 10);
    }

    public void afterCreateSessionAddConfigToWebapi(CombinedConfiguration webapiCombinedConfiguration)
    {
        webapiCombinedConfiguration.removeConfiguration(CREATE_SESSION_RECOGNITION_PARAMS_NAME);
        webapiCombinedConfiguration.removeConfiguration(CREATE_SESSION_SESSION_PARAMS_NAME);

        webapiCombinedConfiguration.addConfiguration(createSessionSessionParameters, CREATE_SESSION_SESSION_PARAMS_NAME);
        webapiCombinedConfiguration.addConfiguration(createSessionRecognitionParameters, CREATE_SESSION_RECOGNITION_PARAMS_NAME);
    }

    @SuppressWarnings("java:S125")
    private void parseSessionParameters()
    {
        processAudioFormat(createSessionSessionParameters);

        //Set noise level to 0 as we don't usually get silence from NSS due to SWIep
        //NoiseLevel noiseLevel = new NoiseLevel(40,15);
        //rc.put(Config.Property.NOISE_LEVEL, NoiseLevel.noiseLevelToMap(noiseLevel));

        var idleTimeout = createSessionSessionParameters.getString("idleTimeout", null);
        if (idleTimeout != null)
        {
            logger.debug("idleTimeout = {}", idleTimeout);
            createSessionSessionParameters.setProperty("sessionInactivityTimeoutSeconds",
                    KryptonWsTimeDesignation.getSecondsFromTimeDesignation(idleTimeout));
        }
    }

    public void interpretAddConfig(JSONObject kryptonWsInterpretParamsJson,
                                   CombinedConfiguration webapiCombinedConfiguration,
                                   KryptonWsSession kryptonWsSession)
    {
        webapiCombinedConfiguration.removeConfiguration(INTERPRETATION_PARAMS_NAME);
        configurations.removeConfiguration(INTERPRETATION_PARAMS_NAME);

        var interpretParameters = new WebApiJSONConfiguration();
        interpretParameters.load(kryptonWsInterpretParamsJson.toMap());
        parseInterpretationParameters(interpretParameters, kryptonWsInterpretParamsJson, kryptonWsSession);

        configurations.addConfiguration(interpretParameters, INTERPRETATION_PARAMS_NAME);
        webapiCombinedConfiguration.addConfiguration(interpretParameters, INTERPRETATION_PARAMS_NAME);
    }

    private void parseInterpretationParameters(WebApiJSONConfiguration interpretParameters, JSONObject kryptonWsInterpretParamsJson, KryptonWsSession kryptonWsSession)
    {
        if (interpretParameters.containsKey("inputType")) {
            String inputType = interpretParameters.getString("inputType");
            if(inputType.equalsIgnoreCase("application/x-nuance-krypton-result")) {
                inputType = "speechRecognitionVerboseResult";
            }
            interpretParameters.setProperty("type", inputType);
        }

        if (interpretParameters.containsKey("model")) {
            JSONObject semanticModel = kryptonWsSession.getSemanticModel();
            if(semanticModel.getString("id").equalsIgnoreCase(interpretParameters.getString("model"))) {
                interpretParameters.setProperty("semanticModelUrl", semanticModel.get("url"));
            }
        }

        final var inputKey = "input";
        if (kryptonWsInterpretParamsJson.has(inputKey)) {
            if(kryptonWsInterpretParamsJson.get(inputKey) instanceof String) {
                interpretParameters.setProperty("data", kryptonWsInterpretParamsJson.get(inputKey));
            } else {
                var gson = new GsonBuilder().create();
                interpretParameters.setProperty("data", new JSONObject(gson.toJson(kryptonWsInterpretParamsJson.get(inputKey))));
            }
        }

        if (interpretParameters.containsKey("sensitive")) {
            String sensitive = interpretParameters.getString("sensitive");
            if(sensitive.equalsIgnoreCase("suppress")) {
                interpretParameters.setProperty("sensitivity", "masked");
            }
        }

    }

    public void recognizeAddConfig(JSONObject recognizeRecognitionParametersJson,
            CombinedConfiguration webapiCombinedConfiguration)
    {
        webapiCombinedConfiguration.removeConfiguration(RECOGNIZE_RECOGNITION_PARAMS_NAME);
        configurations.removeConfiguration(RECOGNIZE_RECOGNITION_PARAMS_NAME);

        var recognizeRecognitionParameters = new WebApiJSONConfiguration();
        recognizeRecognitionParameters.load(recognizeRecognitionParametersJson.toMap());
        parseRecognitionParameters(recognizeRecognitionParameters);

        configurations.addConfiguration(recognizeRecognitionParameters, RECOGNIZE_RECOGNITION_PARAMS_NAME);
        webapiCombinedConfiguration.addConfiguration(recognizeRecognitionParameters, RECOGNIZE_RECOGNITION_PARAMS_NAME);
    }

    private void parseRecognitionParameters(WebApiJSONConfiguration recognitionParameters)
    {
        if (recognitionParameters.containsKey("audioFormat"))
        {
            processAudioFormat(recognitionParameters);
        }

        processBooleanParam(recognitionParameters, "enablePartialResults", "wordStream");

        if (recognitionParameters.containsKey(KRYPTON_WS_PARAM_NBEST_LIST_LENGTH))
        {
            var nBestListLength = recognitionParameters.getInt(KRYPTON_WS_PARAM_NBEST_LIST_LENGTH);
            if (nBestListLength < 1 || nBestListLength > 10)
            {
                throw new JSONException("Field nBestListLength must be within [1,10]");
            }
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_NBEST_LIST_LENGTH, nBestListLength);
            recognitionParameters.setProperty("maxTranscriptions", nBestListLength);
        }

        processBooleanParam(recognitionParameters, "enableProfanityFilter", "kryptonEnableProfanityFilter");

        processStringParam(recognitionParameters, "formattingType", "speechRecognitionFormattingType");

        processBooleanParam(recognitionParameters, "immutablePartialResults",
                "speechRecognitionImmutablePartialResults");

        parseUtteranceDetectionParams(recognitionParameters);
    }

    private void processAudioFormat(WebApiJSONConfiguration parameters)
    {
        var audioFormat = parameters.getString("audioFormat", "audio/L16;rate=8000");
        var audioCodec = AudioCodec.fromString(audioFormat)
                                      .getWebapiString();
        logger.debug("audioCodec = {}", audioCodec);
        parameters.setProperty("speechRecognitionCodec", audioCodec);
    }

    private void parseUtteranceDetectionParams(WebApiJSONConfiguration recognitionParameters)
    {
        if (recognitionParameters.containsKey(KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION))
        {
            var disableUtteranceDetection =
                    recognitionParameters.getBoolean(KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION);
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION, disableUtteranceDetection);
            recognitionParameters.setProperty("endPointDetection", !disableUtteranceDetection);
        }

        processBooleanParam(recognitionParameters, "autoEnd", "stopOnEndOfSpeech");

        if (recognitionParameters.containsKey(KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT))
        {
            var noInputTimeoutStringValue = recognitionParameters.getString(KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT);
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT, noInputTimeoutStringValue);
            if (!noInputTimeoutStringValue.equalsIgnoreCase(DISABLED))
            {
                int noInputTimeoutSeconds = verifyValidSecondsInput(noInputTimeoutStringValue, KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT);
                logger.debug("startOfSpeechTimeoutSeconds = {}", noInputTimeoutSeconds);
                recognitionParameters.setProperty("startOfSpeechTimeoutSeconds", noInputTimeoutSeconds);
            }
        }

        if (recognitionParameters.containsKey(KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT))
        {
            var recognitionTimeoutString = recognitionParameters.getString(KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT);
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT, recognitionTimeoutString);
            if (!recognitionTimeoutString.equalsIgnoreCase(DISABLED))
            {
                recognitionParameters.setProperty("utteranceMaxTimeSeconds",
                        verifyValidSecondsInput(recognitionTimeoutString, KRYPTON_WS_PARAM_RECOGNITION_TIMEOUT)); //recognitionTimeout is not exactly utteranceMaxTimeSeconds
            }
        }

        if (recognitionParameters.containsKey(KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE))
        {
            var utteranceEndSilence = recognitionParameters.getString(KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE);
            int utteranceEndSilenceMilliseconds = verifyValidMillisecondsInput(utteranceEndSilence, KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE);
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE, utteranceEndSilenceMilliseconds);
            recognitionParameters.setProperty("kryptonUtteranceEndSilenceMs", utteranceEndSilenceMilliseconds);
        }

        processBooleanParam(recognitionParameters, "suppressCallRecording", "kryptonSuppressCallRecording");

        if(recognitionParameters.containsKey(KRYPTON_WS_PARAM_SENSITIVTY))
        {
            var sensitivtyZeroToThousand = recognitionParameters.getInt(KRYPTON_WS_PARAM_SENSITIVTY);
            if (sensitivtyZeroToThousand < 0 || sensitivtyZeroToThousand > 1000)
            {
                throw new JSONException("speechDetectionSensitivity must be within [0,1000]");
            }
            logger.debug(KEY_EQUALS_VALUE, KRYPTON_WS_PARAM_SENSITIVTY, sensitivtyZeroToThousand);

            recognitionParameters.setProperty("kryptonSpeechDetectionSensitivity", sensitivtyZeroToThousand);
        }
    }

    private int verifyValidSecondsInput(String givenTime, String valueKey)
    {
        int toReturn = KryptonWsTimeDesignation.getSecondsFromTimeDesignation(givenTime);
        if (toReturn >= 0)
        {
            return toReturn;
        }
        else
        {
            throw new IllegalArgumentException(String.format("%s invalid input.", valueKey));
        }
    }

    private int verifyValidMillisecondsInput(String givenTime, String valueKey)
    {
        int toReturn = KryptonWsTimeDesignation.getMillisecondsFromTimeDesignation(givenTime);
        if (toReturn >= 0)
        {
            return toReturn;
        }
        else
        {
            throw new IllegalArgumentException(String.format("%s invalid input.", valueKey));
        }
    }

    private void processBooleanParam(WebApiJSONConfiguration parameters, String kryptonWsKey, String webapiKey)
    {
        if (parameters.containsKey(kryptonWsKey))
        {
            var kryptonValue = parameters.getBoolean(kryptonWsKey);
            logger.debug("{} = {} = {}", kryptonWsKey, webapiKey, kryptonValue);
            parameters.setProperty(webapiKey, kryptonValue);
        }
    }

    private void processStringParam(WebApiJSONConfiguration parameters, String kryptonWsKey, String webapiKey)
    {
        if (parameters.containsKey(kryptonWsKey))
        {
            var kryptonValue = parameters.getString(kryptonWsKey);
            logger.debug("{} = {} = {}", kryptonWsKey, webapiKey, kryptonValue);
            parameters.setProperty(webapiKey, kryptonValue);
        }
    }

    public void processThwartNoAudioTimer(KryptonWsSession kryptonWsSession, CombinedConfiguration webapiCombinedConfig)
    {
        int seconds = kryptonWsSession.getPluginConfig().getInt(KryptonWsConfig.Property.KRYPTON_WS_THWART_NO_AUDIO_TIMER_SECONDS.getKey());
        webapiCombinedConfig.getConfiguration(RECOGNIZE_RECOGNITION_PARAMS_NAME).setProperty("kryptonGrpcThwartNoAudioTimerSeconds", seconds);
    }

    public CombinedConfiguration getConfigurations()
    {
        return configurations;
    }

    public void clearCombinedConfig(String sessionId)
    {
        ConfigHelpers.clearCombinedConfiguration(configurations, sessionId);
        configurations = null;
    }

}
