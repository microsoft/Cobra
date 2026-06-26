package com.nuance.cobra.utils.request;

import com.nuance.cobra.client.VocalizerPremiumVoiceModels;
import com.nuance.cobra.command.SpeechSynthesisCommand;
import com.nuance.cobra.session.WebApiConfig;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VOCSVoiceConfigUtil
{
    private static final Logger logger = LogManager.getLogger(VOCSVoiceConfigUtil.class);

    private VOCSVoiceConfigUtil()
    {
    }

    public static HierarchicalConfiguration<ImmutableNode> getVocsMapping(CombinedConfiguration params)
    {
        return params.configurationAt(WebApiConfig.Property.VOCS_MAPPING.key);
    }

    public static HierarchicalConfiguration<ImmutableNode> getVoiceConfig(CombinedConfiguration params)
    {
        try
        {
            return getVocsMapping(params).configurationAt(
                    params.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key));
        } catch(ConfigurationRuntimeException e)
        {
            return null;
        }
    }

    public static String getVocsUrl(CombinedConfiguration cc, String globalUrlKey)
    {
        String vocsURL = cc.getString(globalUrlKey);
        HierarchicalConfiguration<ImmutableNode> voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(cc);

        String perVoiceVocsURL = null;
        if (voiceConfig != null)
        {
            perVoiceVocsURL = voiceConfig.getString(WebApiConfig.Property.VOCS_URL.key);
        }
        if (perVoiceVocsURL != null)
        {
            vocsURL = perVoiceVocsURL;
        }

        if (vocsURL == null)
        {
            return null;
        }

        logger.debug("Using vocsURL: {}", vocsURL);
        return vocsURL;
    }

    public static HierarchicalConfiguration<ImmutableNode> getCodecConfig(HierarchicalConfiguration<ImmutableNode> voiceConfig,
            CombinedConfiguration params)
    {
        return voiceConfig.configurationAt(params.getString(
                WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key));
    }

    public static String getVoiceQuality(CombinedConfiguration params)
    {
        if (VocalizerPremiumVoiceModels.isPremiumVoiceModel(getVoiceModel(params)))
        {
            return "premium";
        }
        else
        {
            return "standard";
        }
    }

    public static String getVoiceQualityForReporting(CombinedConfiguration params)
    {
        String nvcVersion = getVoiceNvcVersion(params);

        if(nvcVersion != null && nvcVersion.equals("nvc3")) {
            return "neural";
        }

        return getVoiceQuality(params);
    }

    public static String getVoiceNvcVersion(CombinedConfiguration params) {
        HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(params);
        String nvcVersion = null;

        if(voiceConfig != null) {
            nvcVersion = voiceConfig.getString("nvcVersion");
        }
        return nvcVersion;
    }

    public static String getVoiceModel(CombinedConfiguration params)
    {
        try
        {
            HierarchicalConfiguration<ImmutableNode> voiceConfig = getVoiceConfig(params);
            if (null != voiceConfig)
            {
                HierarchicalConfiguration<ImmutableNode> codecConfig = getCodecConfig(voiceConfig, params);
                if (null != codecConfig)
                {
                    return codecConfig.getString("voice_model");
                }
            }
        }
        catch (ConfigurationRuntimeException e)
        {
            logger.debug("VOCS Voice mapping parameters cannot be parsed");
        }
        return null;
    }

    public static Map<String, Object> getResources(CombinedConfiguration params)
    {
        Map<String, Object> resources = new HashMap<>();
        HierarchicalConfiguration<ImmutableNode> voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(params);
        if(voiceConfig == null) return resources;

        LinkedHashMap<String, String>[] tuneableDataConfigs = (LinkedHashMap<String, String>[]) voiceConfig.getArray(LinkedHashMap.class, SpeechSynthesisCommand.TUNABLE_DATA);
        if (tuneableDataConfigs != null) resources.put(SpeechSynthesisCommand.TUNABLE_DATA, tuneableDataConfigs);

        LinkedHashMap<String, String>[] userDictionaryConfigs = (LinkedHashMap<String, String>[]) voiceConfig.getArray(LinkedHashMap.class, SpeechSynthesisCommand.USER_DICTIONARY);
        if (userDictionaryConfigs != null) resources.put(SpeechSynthesisCommand.USER_DICTIONARY, userDictionaryConfigs);

        return resources;
    }

    public static String getSpeechSynthesisRate(CombinedConfiguration config)
    {
        String speechSynthesisRateString = config.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key);
        if (speechSynthesisRateString == null)
        {
            return null;
        }

        validateNvcSpeakingRate(speechSynthesisRateString);
        return speechSynthesisRateString;
    }

    private static void validateNvcSpeakingRate(String speechSynthesisRateString)
    {
        double speechSynthesisRate;
        try
        {
            speechSynthesisRate = Double.parseDouble(speechSynthesisRateString);
        }
        catch (Exception e)
        {
            logger.warn("speechSynthesisRate \"{}\" is not a floating point number", speechSynthesisRateString);
            throw new IllegalArgumentException();
        }

        if (speechSynthesisRate < 0 || speechSynthesisRate > 2)
        {
            logger.warn("speechSynthesisRate \"{}\" is not within permitted range", speechSynthesisRateString);
            throw new IllegalArgumentException();
        }
    }

}
