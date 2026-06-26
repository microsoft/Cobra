package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.client.response.EndSessionResponse;
import com.nuance.cobra.session.*;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseFormatter;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.reflect.Whitebox;

import java.util.Calendar;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class EndSessionCommandTest extends CobraTest
{
    private ResponseHandler mockResponseHandler;

    private Session mockSession;
    private Request request;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        mockResponseHandler = mock(ResponseHandler.class);
        String requestString = "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\", \"command\": { \"name\": \"endSession\", \"id\": \"1\" } }";

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
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        assertNotNull(command);
    }


    //TODO: need to uncomment this unit test, must use a lot of mock to make it work
    /*@Test
    public void test_execute_endReturnsTrue() throws Exception
    {
        when(mockSessionManager.endSession(anyString())).thenReturn(true);

        final EndSessionCommand command = new EndSessionCommand(request);
        command.setResponseHandler(mockResponseHandler);
        command.execute();

        final String sessionId = request.sessionId;
        verify(mockSessionManager).endSession(eq(sessionId));

        verify(mockResponseHandler).onResponse(eq("EndSession is executed"));

    }*/

    
    @Test
    public void test_execute_Happy() throws Exception
    {
        when(mockSessionManager.endSession(anyString())).thenReturn(false);
        final Session mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(mockSession);
        

        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        Command spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();
        SessionClient mockSessionClient = mock(SessionClient.class);
        when(mockClientFactory.createSessionClient(any(), anyString())).thenReturn(mockSessionClient);

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();

        final String sessionId = request.sessionId;
        verify(mockSessionClient).endSession(any(ResponseHandler.class), any());
        
        verify(mockSession).setCommand(eq(spyCommand));
        
    }
    
    @Test
    public void test_execute_session_null() throws Exception
    {
        when(mockSessionManager.endSession(anyString())).thenReturn(false);
        
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(null);

        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        command.setResponseHandler(mockResponseHandler);
        command.execute();
        
        ArgumentCaptor<EndSessionResponse> argument = ArgumentCaptor.forClass(EndSessionResponse.class);
        
        
        verify(mockResponseHandler).onResponse(argument.capture());
        EndSessionResponse endSessionResponseVerify = argument.getValue();

        assertTrue(endSessionResponseVerify.isFinal());
        assertTrue(endSessionResponseVerify.getStatus() == ResponseResult.ResponseStatus.ERROR);
        
    }

    @Test
    public void test_formatResponse() throws TenantConfigRetrievalException, InvalidConfigurationException
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(mockSession);

        final Response mockResponse = mock(Response.class);
        final ResponseResult mockResponseResult = mock(ResponseResult.class);

        when(mockResponse.getResult()).thenReturn(mockResponseResult);
        when(mockResponseResult.getStatus()).thenReturn(ResponseResult.ResponseStatus.SUCCESS);

        EndSessionResponse endSessionResponse = new EndSessionResponse(ResponseResult.ResponseStatus.SUCCESS, true);

        try
        {
            String result = command.formatResponse(endSessionResponse);
            String response = endSessionResponse.toJson();
            assertTrue(result.contains(response));
        }
        catch (CobraException e)
        {
            fail();
        }
    }
    
    @Test
    public void test_onResponse() throws TenantConfigRetrievalException, InvalidConfigurationException
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        
        final Session mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(mockSession);

        when(mockSessionManager.endSession(anyString())).thenReturn(true);
        EndSessionResponse endSessionResponse = new EndSessionResponse(ResponseResult.ResponseStatus.SUCCESS, true);
        command.setResponseHandler(mockResponseHandler);
        boolean sendResponse = command.onResponse(endSessionResponse);
        verify(mockSessionManager).endSession(eq(request.sessionId));
        assertTrue(sendResponse);
    }

    @Test
    public void test_sessionEnded()
    {
        final EndSessionCommand endSessionCommand = new EndSessionCommand(request, testConfs, null);

        final Session mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(mockSession);

        EndSessionResponse endSessionResponse = new EndSessionResponse(ResponseResult.ResponseStatus.SUCCESS, true);
        endSessionCommand.setResponseHandler(mockResponseHandler);

        Whitebox.setInternalState(endSessionCommand, "sessionIsAlreadyEnded", true);
        when(mockSessionManager.endSession(anyString())).thenReturn(false);

        endSessionCommand.onResponse(endSessionResponse);
        verify(mockSessionManager, times(0)).endSession(eq(request.sessionId));

        Whitebox.setInternalState(endSessionCommand, "sessionIsAlreadyEnded", true);
        when(mockSessionManager.endSession(anyString())).thenReturn(true);

        endSessionCommand.onResponse(endSessionResponse);
        verify(mockSessionManager, times(0)).endSession(eq(request.sessionId));

        Whitebox.setInternalState(endSessionCommand, "sessionIsAlreadyEnded", false);
        when(mockSessionManager.endSession(anyString())).thenReturn(false);

        endSessionCommand.hasSentFinalResponse = false;
        endSessionCommand.onResponse(endSessionResponse);
        verify(mockSessionManager, times(1)).endSession(eq(request.sessionId));

        Whitebox.setInternalState(endSessionCommand, "sessionIsAlreadyEnded", false);
        when(mockSessionManager.endSession(anyString())).thenReturn(true);

        endSessionCommand.hasSentFinalResponse = false;
        endSessionCommand.onResponse(endSessionResponse);
        verify(mockSessionManager, times(2)).endSession(eq(request.sessionId));
    }
    
    
    @Test(expected = NullPointerException.class)
    public void test_onDirective_null() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        command.onDirective(null, true, "tests");
    }



    @Test(expected = NullPointerException.class)
    public void test_onDirective_unknown() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        command.onDirective(new RequestDirective(), true, "tests");
    }

    @Test
    public void test_onAudio_null() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        command.onAudio(null);
    }

    @Test
    public void test_onAudio_empty() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        command.onAudio(new byte[0]);
    }

    @Test
    public void test_onAudio_happy() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);

        final byte[] happyBytes = new byte[] { 0, 0, 0, 0 };
        command.onAudio(happyBytes);
    }


    @Test
    public void test_getResponseFormatter() throws Exception
    {
        final EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        final ResponseFormatter formatter = command.getResponseFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void test_createEndSessionRequest() throws Exception {
        Request req = EndSessionCommand.createEndSessionRequest("testId");
        assertEquals("testId", req.sessionId);
    }
}