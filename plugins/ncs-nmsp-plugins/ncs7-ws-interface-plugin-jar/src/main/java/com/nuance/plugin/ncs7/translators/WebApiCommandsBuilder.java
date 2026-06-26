package com.nuance.plugin.ncs7.translators;

import com.nuance.plugin.ncs7.Ncs7InterfaceConfig;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class WebApiCommandsBuilder {
    private static final Logger logger = LogManager.getLogger();
    public static final String PARAMETERS = "parameters";
    public static final String SPEECH_RECOGNITION_CODEC = "speechRecognitionCodec";
    public static final String TOPIC = "topic";
    public static final String SPEECH_DOMAIN = "speechDomain";

    private WebApiCommandsBuilder() {
    }

    public static JSONObject buildBasicCommand(String commandName, String sessionId, String commandId) {
        JSONObject parameters = new JSONObject();
        JSONObject command = new JSONObject();
        command.put(PARAMETERS, parameters);
        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("sessionId", sessionId);
        webApiRequest.put("command", command);

        command.put("name", commandName);
        command.put("id", commandId);

        return webApiRequest;
    }
    public static final String SPEECH_RECO_WEBAPI_NAME = "speechRecognition";
    public static JSONObject buildSpeechRecoCommand(Orchestrator orchestrator, NcsRequest ncsRequest) throws IOException {
        JSONObject parameters = new JSONObject();
        setSensitivityToOpenBasedOnModelImprovements(orchestrator, parameters);
        parameters.put("endPointDetection", false);
        if (ncsRequest.utterance_detection_silence_duration != null) {
            parameters.put("endPointDetection", true); //enable endpointing if we do any endpointing settings
            parameters.put("endOfSpeechVoicedFrames", 5);
            parameters.put("endOfSpeechHistoryFrames", ncsRequest.getEndOfSpeechVoicedFrames());
        }

        if (ncsRequest.dictation_type != null) {
            setTopicAndSpeechDomainBasedOnDictationTypeAndDictationLanguage(orchestrator, ncsRequest, parameters);
        }

        if(!StringUtils.isEmpty(ncsRequest.context_tag)) {
            parameters.put("dialogModelApplicationVersion", ncsRequest.context_tag);
            setTopicAndSpeechDomainBasedOnContextTagAndLanguage(orchestrator, ncsRequest, parameters);
        }

        setCodec(orchestrator, ncsRequest, parameters);

        if (!orchestrator.getDynamicVocabulary().isEmpty()) {
            parameters.put("activeDynamicVocabularySets", new String[]{UploadDynamicVocabularyTranslator.VOCABULARY_ID});
        }

        parameters.put("language", ncsRequest.getASRLanguage());
        parameters.put("wordStream", ncsRequest.getResultDelivery());
        parameters.put("loadPersistedWordsets", true);

        JSONObject command = new JSONObject();
        command.put(PARAMETERS, parameters);
        command.put("name", SPEECH_RECO_WEBAPI_NAME);

        return command;
    }

    private static void setTopicAndSpeechDomainBasedOnDictationTypeAndDictationLanguage(Orchestrator orchestrator, NcsRequest ncsRequest, JSONObject parameters) {
        try {
            HierarchicalConfiguration<ImmutableNode> pluginConfigs = orchestrator.getWebApi().getActiveSession().getSessionConfig().configurationAt(Ncs7InterfaceConfig.Property.DICTATION_TYPE_TO_TOPIC_MAP.getKey());
            if (pluginConfigs == null) {
                logger.error("Failed to get dictationTypeToTopicMap.");
            } else {
                HierarchicalConfiguration<ImmutableNode> dictationTypeConf = pluginConfigs.configurationAt(ncsRequest.dictation_type);
                if (dictationTypeConf != null) {
                    HierarchicalConfiguration<ImmutableNode> dictationConf = dictationTypeConf.configurationAt(ncsRequest.getASRLanguage());
                    if (dictationConf != null) {
                        parameters.put(TOPIC, dictationConf.getString(TOPIC));
                        parameters.put(SPEECH_DOMAIN, dictationConf.getString(SPEECH_DOMAIN));
                    } else {
                        logger.error("Failed to map dictation_language.");
                    }
                } else {
                    logger.error("Failed to map dictation_type.");
                }
            }
        } catch (ConfigurationRuntimeException e) {
            logger.debug("Client requested dictation_type='{}' but the '{}' doesn't exist. Will use default topic and speech domain.", ncsRequest.dictation_type, Ncs7InterfaceConfig.Property.DICTATION_TYPE_TO_TOPIC_MAP.getKey());
        }
    }

    private static void setTopicAndSpeechDomainBasedOnContextTagAndLanguage(Orchestrator orchestrator, NcsRequest ncsRequest, JSONObject parameters) {
        try {
            HierarchicalConfiguration<ImmutableNode> pluginConfigs = orchestrator.getWebApi().getActiveSession().getSessionConfig().configurationAt(Ncs7InterfaceConfig.Property.CONTEXT_TAG_TO_TOPIC_MAP.getKey());
            if (pluginConfigs == null) {
                logger.error("Failed to get contextTagToTopicMap.");
            } else {
                HierarchicalConfiguration<ImmutableNode> dictationTypeConf = pluginConfigs.configurationAt(ncsRequest.context_tag);
                if (dictationTypeConf != null) {
                    HierarchicalConfiguration<ImmutableNode> dictationConf = dictationTypeConf.configurationAt(ncsRequest.language);
                    if (dictationConf != null) {
                        parameters.put(TOPIC, dictationConf.getString(TOPIC));
                        parameters.put(SPEECH_DOMAIN, dictationConf.getString(SPEECH_DOMAIN));
                    } else {
                        logger.error("Failed to map language.");
                    }
                } else {
                    logger.error("Failed to map context_tag.");
                }
            }
        } catch (ConfigurationRuntimeException e) {
            logger.debug("Client requested context_tag='{}' but the '{}' doesn't exist. Will use default topic and speech domain.", ncsRequest.context_tag, Ncs7InterfaceConfig.Property.CONTEXT_TAG_TO_TOPIC_MAP.getKey());
        }
    }

    private static void setCodec(Orchestrator orchestrator, NcsRequest ncsRequest, JSONObject parameters) throws IOException {
        if (orchestrator.getCodec() == null) {
            parameters.put(SPEECH_RECOGNITION_CODEC, ncsRequest.getWebAPICodecName(orchestrator.getCodec()));
        } else {
            parameters.put(SPEECH_RECOGNITION_CODEC, orchestrator.getCodec());
        }

        if (ncsRequest.getWebAPICodecName(ncsRequest.getCodecName()) == null) {
            parameters.put(SPEECH_RECOGNITION_CODEC, ncsRequest.getWebAPICodecName(orchestrator.getCodec()));
        } else {
            parameters.put(SPEECH_RECOGNITION_CODEC, ncsRequest.getWebAPICodecName(ncsRequest.getCodecName()));
        }
    }

    public static final String INTERPRETATION_WEBAPI_NAME = "interpretation";
    public static JSONObject buildInterpretationCommand(Orchestrator orchestrator, NcsRequest ncsRequest) {
        JSONObject parameters = new JSONObject();
        setSensitivityToOpenBasedOnModelImprovements(orchestrator, parameters);

        if (!orchestrator.getDynamicVocabulary().isEmpty()) {
            parameters.put("activeDynamicVocabularySets", new String[]{UploadDynamicVocabularyTranslator.VOCABULARY_ID});
        }

        parameters.put("dialogModelApplicationVersion", ncsRequest.context_tag);
        parameters.put("language", ncsRequest.language);
        parameters.put("loadPersistedWordsets", true);

        parameters.put("maxInterpretations", 1);
        var nluNbestInterpretations = (int) ncsRequest.dictionary.getOrDefault("nlu_nbest_interpretations",0);
        if(nluNbestInterpretations == 1){
            parameters.put("maxInterpretations", 10);
        }

        JSONObject command = new JSONObject();
        command.put(PARAMETERS, parameters);
        command.put("name", INTERPRETATION_WEBAPI_NAME);

        return command;
    }

    private static void setSensitivityToOpenBasedOnModelImprovements(Orchestrator orchestrator, JSONObject parameters){
        final var modelImprovementsDefined = orchestrator.areModelImprovementsDefined("model-improvements");
        if (modelImprovementsDefined && orchestrator.areModelImprovementsEnabled("model-improvements")) {
            parameters.put("sensitivity", "open");
        }
    }
}
