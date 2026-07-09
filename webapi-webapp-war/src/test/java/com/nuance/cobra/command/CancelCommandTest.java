package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CancelCommandTest extends CobraTest {
    private ResponseHandler mockResponseHandler;

    private Session mockSession;
    private Request request;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);

    @Before
    public void setUp() throws Exception{
        super.setUp();

        mockResponseHandler = mock(ResponseHandler.class);
        String requestString = "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\", \"command\": { \"name\": \"cancelCommand\", \"id\": \"1\" } }";

        request = Request.fromJson(requestString);
        mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key,"123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key,"DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key,"Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key,"4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key,"as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key,"appName");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key,"1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "MTIzNDU2Nzg5");

        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void test_constructor() throws Exception
    {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        assertNotNull(command);
    }

    @Test
    public void test_execute_Happy() throws Exception
    {
        when(mockSessionManager.endSession(anyString())).thenReturn(false);
        final Session mockSession = mock(Session.class);
        doReturn(mockSession).when(mockSessionManager).getSession(anyString(), any(Boolean.class));

        testConf.setProperty(WebApiConfig.Property.COMMAND_TIMEOUT_MILLISECONDS.key, -1);

        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        when(mockSession.getCommand()).thenReturn(command);
        Command spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();
        SessionClient mockSessionClient = mock(SessionClient.class);
        doReturn(mockSessionClient).when(mockClientFactory).createSessionClient(any(), anyString());

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();

        assertNotNull(mockSession);
        assertNull(command.getTimerTask());
    }

    @Test
    public void test_onDirectiveImplementation() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        CancelCommandCommand spyCommand = spy(command);
        spyCommand.onDirectiveImplementation(null, false);
        verify(spyCommand).onDirectiveImplementation(any(), anyBoolean());
        verifyNoMoreInteractions(spyCommand);
    }

    @Test
    public void test_onAudioImplementation() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        CancelCommandCommand spyCommand = spy(command);
        spyCommand.onAudioImplementation(null);
        verify(spyCommand).onAudioImplementation(any());
        verifyNoMoreInteractions(spyCommand);
    }

    @Test
    public void test_onResponseImplementation() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        assertTrue(command.onResponseImplementation(null));
    }

    @Test
    public void test_onAudioResponseImplementation() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        assertFalse(command.onAudioResponseImplementation(null, false));
    }

    @Test
    public void test_getResponseFormatter() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        assertNotNull(command.getResponseFormatter());
    }

    @Test
    public void test_sendErrorResponse() {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        CancelCommandCommand spyCommand = spy(command);
        ResponseErrorHandler mockHandler = mock(ResponseErrorHandler.class);
        Mockito.doReturn(mockHandler).when(spyCommand).getErrorHandler(any());
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(null);
        spyCommand.executeImplementation();
        verify(spyCommand).getErrorHandler(any());
    }

    @Test
    public void test_formatResponse() throws CobraException {
        final CancelCommandCommand command = new CancelCommandCommand(request, testConfs, null);
        ResponseResult response = new ResponseResult();
        assertEquals("{\"type\":\"result\",\"sessionId\":\"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\",\"command\":{\"name\":\"cancelCommand\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"success\"}}", command.formatResponse(response));
    }
}
