package com.nuance.cobra.client.krypton;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;

public class KryptonUtils
{
    private static final Logger logger = LogManager.getLogger(KryptonUtils.class);

    private KryptonUtils()
    {
    }

    public static String getKryptonUrl(CombinedConfiguration config, WebApiConfig.Property urlProperty)
    {
        String kryptonUrl = config.getString(urlProperty.key);

        try
        {
            String language = getLanguage(config);
            HierarchicalConfiguration<ImmutableNode> languageToUrlMapping =
                    config.configurationAt(WebApiConfig.Property.KRYPTON_LANGUAGE_TO_URL_MAPPING.key);
            String kryptonUrlFromMapping = languageToUrlMapping.getString(language);
            if (kryptonUrlFromMapping != null)
            {
                kryptonUrl = kryptonUrlFromMapping;
            }
        }
        catch (Exception e)
        {
            //Do nothing. Just use urlProperty
        }

        logger.debug("Using Krypton URL: {}", kryptonUrl);

        return kryptonUrl;
    }

    public static String getLanguage(CombinedConfiguration config)
    {
        String speechRecoLanguage = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key);
        if (speechRecoLanguage == null)
        {
            //Try to fall back to kryptonLanguagePack
            speechRecoLanguage = config.getString(WebApiConfig.Property.KRYPTON_LANGUAGE_PACK.key);
            return speechRecoLanguage;
        }

        HierarchicalConfiguration<ImmutableNode> speechRecognitionLanguageMap =
                CobraConfigurationUtils.configurationAtOrNull(config,
                        WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE_MAPPING.key);
        if (speechRecognitionLanguageMap == null)
        {
            //if speechRecognitionLanguageMap is not found
            return speechRecoLanguage;
        }

        String internalLanguageCode = speechRecognitionLanguageMap.getString(speechRecoLanguage);
        if (internalLanguageCode != null)
        {
            //return the mapped code
            return internalLanguageCode;
        }
        return speechRecoLanguage;
    }

    public static boolean isLanguageAllowed(CombinedConfiguration config)
    {
        if (!config.getBoolean(WebApiConfig.Property.KRYPTON_RESTRICT_LANGUAGES.key))
        {
            return true;
        }

        String language = getLanguage(config);
        if (language.equals(config.getString(WebApiConfig.Property.KRYPTON_LANGUAGE_PACK.key)))
        {
            return true;
        }

        try
        {
            HierarchicalConfiguration<ImmutableNode> languageToUrlMapping =
                    config.configurationAt(WebApiConfig.Property.KRYPTON_LANGUAGE_TO_URL_MAPPING.key);
            if (languageToUrlMapping.containsKey(language))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            //Swallow the exception and return false.
        }

        return false;
    }

    public static String getLanguageForMetrics(CombinedConfiguration config)
    {
        if (!isLanguageAllowed(config))
        {
            return null;
        }

        return getLanguage(config);
    }

    public static boolean isProfanityFilterEnabled(CombinedConfiguration config)
    {
        var enableProfanityFilterSource = config.getSource(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key);
        var defaultConfig = config.getConfiguration(WebApiConfigManager.CONFIG_NAME_DEFAULTS);
        if(defaultConfig != enableProfanityFilterSource) {
            return config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key);
        }

        if (config.containsKey(WebApiConfig.Property.KRYPTON_ENABLE_PROFANITY_FILTER.key))
        {
            return config.getBoolean(WebApiConfig.Property.KRYPTON_ENABLE_PROFANITY_FILTER.key);
        }
        return WebApiConfigManager.getInstance()
                                  .getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key);
    }

    public static String getTopic(CombinedConfiguration config)
    {
        String topic = config.getString(WebApiConfig.Property.TOPIC.key);
        if (topic != null)
        {
            return topic;
        }
        topic = config.getString(WebApiConfig.Property.KRYPTON_TOPIC.key);
        return topic;
    }

}
