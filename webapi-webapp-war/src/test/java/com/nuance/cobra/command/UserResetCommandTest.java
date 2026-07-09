package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.UserResetClient;
import com.nuance.cobra.client.response.ResourcesResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({
        SessionManager.class, ClientFactory.class
})
public class UserResetCommandTest extends CobraTest {

    private final CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

    private Request request;

    @InjectMocks
    private ClientFactory mockClientFactory = mock(ClientFactory.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String userResetCommandString = "{\"type\":\"command\",\"sessionId\":\"123\",\"command\":{\"name\":\"userReset\",\"id\":\"3\"}}";

        request = Request.fromJson(userResetCommandString);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testExecuteImplementation(){
        Session mockSession = mock(Session.class);
        PowerMockito.when(mockSessionManager.getSession("123", true)).thenReturn(mockSession);

        testConfigs.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "myClientId");

        when(mockSession.getConfigWithMerge(any())).thenReturn(testConfigs);

        UserResetCommand command = new UserResetCommand(request, testConfigs, null);
        command.setClientFactory(mockClientFactory);

        ResponseHandler mockHandler = spy(mock(ResponseHandler.class));
        command.setResponseHandler(mockHandler);

        UserResetClient mockUserResetClient = mock(UserResetClient.class);
        PowerMockito.when(mockClientFactory.createUserResetClient(any(), any(), any()))
                .thenReturn(mockUserResetClient);

        command.executeImplementation();

        verify(mockUserResetClient).startUserReset();
    }

    @Test
    public void testExecuteImplementation_noClient(){
        Session mockSession = mock(Session.class);
        PowerMockito.when(mockSessionManager.getSession("123", true)).thenReturn(mockSession);

        when(mockSession.getConfigWithMerge(any())).thenReturn(testConfigs);

        UserResetCommand command = new UserResetCommand(request, testConfigs, null);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        command.setResponseHandler(mockResponseHandler);

        command.executeImplementation();

        ArgumentCaptor<ResponseResult> argument = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockResponseHandler).onResponse(argument.capture());
        ResponseResult responseResult = argument.getValue();

        assertTrue(responseResult.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, responseResult.getStatus());
    }

    @Test
    public void testExecuteImplementation_clientException(){
        Session mockSession = mock(Session.class);
        PowerMockito.when(mockSessionManager.getSession("123", true)).thenReturn(mockSession);

        testConfigs.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "myClientId");

        when(mockSession.getConfigWithMerge(any())).thenReturn(testConfigs);

        UserResetCommand command = new UserResetCommand(request, testConfigs, null);
        command.setClientFactory(mockClientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        command.setResponseHandler(mockResponseHandler);

        PowerMockito.when(mockClientFactory.createUserResetClient(any(), any(), any()))
                .thenThrow(new NullPointerException());

        command.executeImplementation();

        ArgumentCaptor<ResponseResult> argument = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockResponseHandler).onResponse(argument.capture());
        ResponseResult responseResult = argument.getValue();

        assertTrue(responseResult.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, responseResult.getStatus());
    }

    @Test
    public void testExecuteImplementation_noParams(){
        Session mockSession = mock(Session.class);
        PowerMockito.when(mockSessionManager.getSession("123", true)).thenReturn(mockSession);

        when(mockSession.getConfigWithMerge(any())).thenThrow(new IllegalArgumentException());

        UserResetCommand command = new UserResetCommand(request, testConfigs, null);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        command.setResponseHandler(mockResponseHandler);

        command.executeImplementation();

        ArgumentCaptor<ResponseResult> argument = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockResponseHandler).onResponse(argument.capture());
        ResponseResult responseResult = argument.getValue();

        assertTrue(responseResult.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, responseResult.getStatus());
    }

    @Test
    public void testFormatResponse() throws CobraException {
        UserResetCommand command = new UserResetCommand(request, testConfigs, null);
        ResourcesResponse response = new ResourcesResponse(testConfigs);

        String formattedResponse = command.formatResponse(response);

        assertNotNull(formattedResponse);

        JSONObject jsonResponse = new JSONObject(formattedResponse);

        assertEquals("success", jsonResponse.getJSONObject("result").getString("status"));
    }

    @Test
    public void testOnResponseImplementation(){
        UserResetCommand command = new UserResetCommand(request, testConfigs, null);
        command.client = mock(UserResetClient.class);
        assertTrue(command.onResponseImplementation(null));
    }
}