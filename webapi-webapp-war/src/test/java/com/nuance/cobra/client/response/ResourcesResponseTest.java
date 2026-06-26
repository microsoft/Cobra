package com.nuance.cobra.client.response;

import com.nuance.cobra.command.SpeechRecognitionCommand;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourcesResponseTest {

    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration testConfig = testConfigs.getConfiguration(0);
    private SessionManager mockSessionManager;
    private ResourcesResponse resourcesResponse;

    @Before
    public void setUp() throws Exception {
        testConfig.addProperty("vocsMapping.Evan.language", "en-us");
        testConfig.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key, "someRandomEngine");
        resourcesResponse = new ResourcesResponse(testConfigs);

    }

    @Test
    public void getResourcesTest() {
        JSONObject jsonCapabilities = new JSONObject(resourcesResponse.getResources());

        String name = jsonCapabilities
                .getJSONObject("speechSynthesis")
                .getJSONArray("voices")
                .getJSONObject(0)
                .get("name").toString();

        assertEquals("Evan", name);
    }

    @Test
    public void addCapabilityTest() {
        testConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key, "test2");
        resourcesResponse.addResource(SpeechRecognitionCommand.getResources(testConfigs));

        JSONObject resourcesJson = new JSONObject(resourcesResponse.getResources());

        assertEquals("test2" ,resourcesJson.getJSONObject("speechRecognition").get("speechRecognitionEngine"));
    }
}