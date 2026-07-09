package com.nuance.cobra.client;

import com.nuance.cobra.session.WebApiConfig;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NluClientUtilsTest
{

    @Test
    public void getInterpretationLanguage()
    {
        CombinedConfiguration config = mock(CombinedConfiguration.class);

        when(config.getString(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key)).thenReturn("eng-USA-interpretationLanguage");
        assertEquals("eng-USA-interpretationLanguage", NluClientUtils.getInterpretationLanguage(config));

        when(config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key)).thenReturn("eng-USA-language");
        assertEquals("eng-USA-language", NluClientUtils.getInterpretationLanguage(config));

        when(config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key)).thenReturn("en-US");
        BaseHierarchicalConfiguration languageToUrlMapping = new BaseHierarchicalConfiguration();
        when(config.configurationAt(WebApiConfig.Property.INTERPRETATION_LANGUAGE_MAPPING.key)).thenReturn(languageToUrlMapping);
        assertEquals("en-US", NluClientUtils.getInterpretationLanguage(config));

        languageToUrlMapping.setProperty("en-US", "eng-USA");
        assertEquals("eng-USA", NluClientUtils.getInterpretationLanguage(config));
    }
}