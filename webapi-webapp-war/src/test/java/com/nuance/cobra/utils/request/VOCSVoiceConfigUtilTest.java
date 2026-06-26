package com.nuance.cobra.utils.request;

import com.nuance.cobra.client.VocalizerPremiumVoiceModels;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({ WebApiConfigManager.class })
public class VOCSVoiceConfigUtilTest
{
    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConfig = CobraConfigurationUtils.getHighestConfig(testConfigs);

    @Before
    public void setUp()
            throws Exception
    {
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.name", "Ava");
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.speechSynthesisCodec", "pcm_16_8k");
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.pcm_16_8k.voice_model",
                VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue());
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.pcm_16_8k.output_type", "0");
        testConfig.addProperty("data", "test");
        testConfig.addProperty("type", "text");
        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "carol");

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance())
                    .thenReturn(testConfigs);

    }


    @Test
    public void getVocsMappingTest()
    {
        HierarchicalConfiguration vocsMapping = VOCSVoiceConfigUtil.getVocsMapping(testConfigs);
        assertTrue(vocsMapping != null);
        assertEquals("Ava",vocsMapping.configurationAt(testConfig.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key))
                                .getString("name"));
    }

    @Test
    public void getVoiceConfigTest()
    {
        HierarchicalConfiguration voiceConfig = VOCSVoiceConfigUtil.getVoiceConfig(testConfigs);
        assertTrue(voiceConfig != null);
        assertEquals("Ava",voiceConfig.getString("name"));
    }

    @Test
    public void getVoiceQualityTest()
    {

        //Test voiceQuality = "standard"
        testConfig.setProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.pcm_16_8k.voice_model", "xyzzzz");
        assertEquals("standard",VOCSVoiceConfigUtil.getVoiceQuality(testConfigs));

        //Test voiceQuality = "premium"
        testConfig.setProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.pcm_16_8k.voice_model",
                VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue());
        assertEquals("premium",VOCSVoiceConfigUtil.getVoiceQuality(testConfigs));
    }

    @Test
    public void getVoiceModelTest()
    {
        testConfig.setProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.pcm_16_8k.voice_model",
                VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue());
        assertEquals(VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue(), VOCSVoiceConfigUtil.getVoiceModel(testConfigs));

        testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        testConfig = CobraConfigurationUtils.getHighestConfig(testConfigs);
        testConfig.setProperty(WebApiConfig.Property.VOCS_MAPPING.key + "xxx",
                VocalizerPremiumVoiceModels.XPREMIUM_HIGH.getValue());
        assertEquals(null, VOCSVoiceConfigUtil.getVoiceModel(testConfigs));
    }

    @Test
    public void testVoiceMappingParsingError()
    {
          testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "xyz");
          assertEquals("standard", VOCSVoiceConfigUtil.getVoiceQuality(testConfigs));
    }

    @Test
    public void testGetVocsUrl()
    {
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.name", "Ava");

        String testVal = "https://myTestUrl:443";
        testConfig.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".carol.vocsURL", testVal);
        assertEquals(VOCSVoiceConfigUtil.getVocsUrl(testConfigs, WebApiConfig.Property.VOCS_URL.key), testVal);

        CombinedConfiguration emptyTestConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        assertNull(VOCSVoiceConfigUtil.getVocsUrl(emptyTestConfigs, WebApiConfig.Property.VOCS_URL.key));
    }

    @Test
    public void testGetSpeechSynthesisRate()
    {
        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "1.0");
        assertEquals("1.0", VOCSVoiceConfigUtil.getSpeechSynthesisRate(testConfigs));

        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "0");
        assertEquals("0", VOCSVoiceConfigUtil.getSpeechSynthesisRate(testConfigs));

        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "2.0");
        assertEquals("2.0", VOCSVoiceConfigUtil.getSpeechSynthesisRate(testConfigs));

        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "hello");
        try
        {
            VOCSVoiceConfigUtil.getSpeechSynthesisRate(testConfigs);
            fail();
        }
        catch (IllegalArgumentException e)
        { /*Success*/ }

        testConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_RATE.key, "2.1");
        try
        {
            VOCSVoiceConfigUtil.getSpeechSynthesisRate(testConfigs);
            fail();
        }
        catch (IllegalArgumentException e)
        { /*Success*/ }
    }

}
