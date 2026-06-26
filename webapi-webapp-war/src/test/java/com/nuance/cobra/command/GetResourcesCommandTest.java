package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.response.ResourcesResponse;
import com.nuance.cobra.session.*;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class, SessionManager.class, ResourcesResponse.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class GetResourcesCommandTest {

    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConfig = testConfigs.getConfiguration(0);
    private Request request;

    @Before
    public void setUp() throws Exception {
        String getResourcesCommandString = "{\"type\":\"command\",\"sessionId\":\"123\",\"command\":{\"name\":\"getResources\",\"id\":\"3\"}}\"";

        testConfig.setProperty(WebApiConfig.Property.HOST_REGION.key, "en-us");
        testConfig.setProperty(WebApiConfig.Property.HOST_ENVIRONMENT.key, "someEnv");
        testConfig.setProperty(WebApiConfig.Property.API_VERSION.key, "2.14.0");
        testConfig.addProperty("vocsMapping.Evan.language", "en-us");
        testConfig.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConfig.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nvc.getValue());
        testConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key, "someRandomEngine");


        request = Request.fromJson(getResourcesCommandString);
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);

    }

    @Test
    public void testExecuteImplementationThrowException(){
        GetResourcesCommand testCapabilitiesCommand = new GetResourcesCommand(request, testConfigs, null);

        assertNotNull(testCapabilitiesCommand);

        testCapabilitiesCommand.executeImplementation();
    }

    @Test
    public void testExecuteImplementation(){
        PowerMockito.mockStatic(SessionManager.class);
        SessionManager mockManager = mock(SessionManager.class);
        Session mockSession = mock(Session.class);

        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockManager);

        assertNotNull(SessionManager.getInstance());

        PowerMockito.when(mockManager.getSession("123", true)).thenReturn(mockSession);

        assertEquals(mockSession, mockManager.getSession("123", true));

        PowerMockito.when(mockSession.getSessionConfig()).thenReturn(testConfigs);

        GetResourcesCommand testCapabilitiesCommand = new GetResourcesCommand(request, testConfigs, null);

        testCapabilitiesCommand.executeImplementation();

    }

    @Test
    public void testFormatResponse() throws CobraException {

        GetResourcesCommand testCapabilitiesCommand = new GetResourcesCommand(request, testConfigs, null);
        ResourcesResponse response = new ResourcesResponse(testConfigs);

        String formattedResponse = testCapabilitiesCommand.formatResponse(response);

        assertNotNull(formattedResponse);

        JSONObject jsonResponse = new JSONObject(formattedResponse);

        assertEquals("success", jsonResponse.getJSONObject("result").getString("status"));

    }

    @Test
    public void testGetResponseFormatter(){
        GetResourcesCommand testCapabilitiesCommand = new GetResourcesCommand(request, testConfigs, null);
        assertNotNull(testCapabilitiesCommand.getResponseFormatter());
    }
}