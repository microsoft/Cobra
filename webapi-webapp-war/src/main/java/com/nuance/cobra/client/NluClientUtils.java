package com.nuance.cobra.client;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class NluClientUtils
{
    NluClientUtils() { throw new IllegalStateException("Utility class!"); }

    public static String getInterpretationLanguage(CombinedConfiguration config)
    {
        // SPEECH_RECOGNITION_LANGUAGE literally just is "language"
        String language = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key);

        if (language == null || language.isBlank()) {
            language = config.getString(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key);
            return language;
        }

        HierarchicalConfiguration<ImmutableNode> interpretationLanguageMap =
                CobraConfigurationUtils.configurationAtOrNull(config, WebApiConfig.Property.INTERPRETATION_LANGUAGE_MAPPING.key);
        if (interpretationLanguageMap == null)
        {
            //if interpretationLanguageMap is not found
            return language;
        }

        String internalLanguageCode = interpretationLanguageMap.getString(language);
        if (internalLanguageCode != null)
        {
            //return the mapped code
            return internalLanguageCode;
        }
        return language;
    }
}
