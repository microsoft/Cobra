package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.client.response.ResourcesResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeleteDynamicVocabularyCommandTest
        extends CobraTest {

    private final CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

    private Request request;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String deleteDynamicVocabularyCommandString = "{\"type\":\"command\",\"sessionId\":\"123\",\"command\":{\"name\":\"deleteDynamicVocabulary\",\"id\":\"3\"}}";

        request = Request.fromJson(deleteDynamicVocabularyCommandString);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testExecuteImplementation(){
        Session mockSession = mock(Session.class);
        PowerMockito.when(mockSessionManager.getSession("123", true)).thenReturn(mockSession);

        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);

        ResponseHandler mockHandler = spy(mock(ResponseHandler.class));
        command.setResponseHandler(mockHandler);

        command.executeImplementation();

        verify(mockHandler, times(1)).onResponse(any());
    }

    @Test
    public void testFormatResponse() throws CobraException {
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        ResourcesResponse response = new ResourcesResponse(testConfigs);

        String formattedResponse = command.formatResponse(response);

        assertNotNull(formattedResponse);

        JSONObject jsonResponse = new JSONObject(formattedResponse);

        assertEquals("success", jsonResponse.getJSONObject("result").getString("status"));
    }

    @Test
    public void testGetResponseFormatter(){
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        assertNotNull(command.getResponseFormatter());
    }

    @Test
    public void testOnResponseImplementation(){
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        assertTrue(command.onResponseImplementation(null));
    }

    @Test
    public void testOnAudioResponseImplementation(){
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        assertFalse(command.onAudioResponseImplementation(new byte[0], false));
    }

    @Test
    public void testOnDirectiveImplementation(){
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        command.setResponseHandler(mock(ResponseHandler.class));
        command.onDirectiveImplementation(new RequestDirective(), false);
        assertNotNull(command);
    }

    @Test
    public void testOnAudioImplementation(){
        DeleteDynamicVocabularyCommand command = new DeleteDynamicVocabularyCommand(request, testConfigs, null);
        command.onAudioImplementation(new byte[0]);
        assertNotNull(command);
    }
}