package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.InterpretationClient;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({
        InterpretationCommandTest.class, SessionManager.class, ClientFactory.class,
        Request.class
})
public class InterpretationCommandTest
        extends CobraTest
{
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
    private InterpretationCommand interpretationCommand;

    private Session mockSession;
    private Request request;

    @Mock
    ClientFactory testClientFactory;

    @InjectMocks
    private ClientFactory mockClientFactory = mock(ClientFactory.class);


    private InterpretationClient dummyInterpretationClient = new InterpretationClient("")
    {
        @Override
        public void startInterpretation()
        {
            // intentionally left empty
        }

        @Override
        public void stopInterpretation()
        {
            // intentionally left empty
        }

        @Override
        public void cancelInterpretation(boolean isFinal)
        {
            // intentionally left empty
        }
    };


    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();

        mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key, "DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key, "Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key, "4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key, "as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key, "appName");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "MTIzNDU2Nzg5");

        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());

        // Build interpretation request
        final String jsonReq =
                "{'type': 'command', 'sessionId': '12345', 'command': {'name': 'interpretation', 'id': '1', " +
                "'parameters': {'data': 'Can I buy tulips and roses', 'type': 'text/plain'}}}";
        request = Request.fromJson(jsonReq.replaceAll("'", "\""));

        // need to set data in mockServerConfigs since merge config is mocked
        mockServerConfig.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "Can I buy tulips and roses");

        interpretationCommand = new InterpretationCommand(request, testConfs, null);

        when(mockSession.getConfigWithMerge(any())).thenReturn(mockServerConfigs);
        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSession.getSessionId()).thenReturn(request.sessionId);

        when(mockSessionManager.getSession(request.sessionId, true)).thenReturn(mockSession);
        when(mockSessionManager.getSession(request.sessionId, false)).thenReturn(mockSession);
    }

    @Test
    public void testExecuteHappy()
            throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        InterpretationClient mockInterpretationClient = mock(InterpretationClient.class);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(mockInterpretationClient);

        interpretationCommand.setClientFactory(mockClientFactory);

        interpretationCommand.execute();
        verify(mockInterpretationClient).startInterpretation();
    }

    @Test
    public void testExecuteWithDebugLogs() throws Exception
    {
       startCapturingLogOutput(Level.DEBUG, InterpretationCommand.class.getName());

        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);interpretationCommand.setClientFactory(mockClientFactory);

        interpretationCommand.execute();
        String capturedOutput = this.getCapturedLogOutput();
        assertTrue(capturedOutput.contains("log level should be TRACE to see data"));
        assertFalse(capturedOutput.contains("tulips and roses"));
    }

    @Test
    public void testExecuteWithTraceLogs() throws Exception
    {
        startCapturingLogOutput(Level.TRACE, InterpretationCommand.class.getName());

        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);

        interpretationCommand.setClientFactory(mockClientFactory);

        interpretationCommand.execute();
        assertTrue(getCapturedLogOutput().contains("tulips and roses"));
    }

    @Test
    public void testExecuteNullSession() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        // return null for session (purpose of this test)
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(null);

        interpretationCommand.setClientFactory(mockClientFactory);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        interpretationCommand.executeImplementation();

        ArgumentCaptor<InterpretationResponse> argument = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(argument.capture());
        InterpretationResponse interpretationResponseVerify = argument.getValue();

        assertTrue(interpretationResponseVerify.isFinal());
        assertTrue(interpretationResponseVerify.getStatus() == ResponseResult.ResponseStatus.ERROR);
    }

    @Test
    public void testLogRequestError()
            throws Exception
    {
        mockStatic(Request.class);

        PowerMockito.when(Request.toJson(any()))
                    .thenThrow(new CobraException(
                            ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR,
                                    "Exception thrown from mock class")));

        startCapturingLogOutput(Level.INFO, InterpretationCommand.class.getName());
        interpretationCommand.logRequest(request, interpretationCommand.logger);
        assertTrue(getCapturedLogOutput().contains("can't log received request"));
    }

    @Test
    public void testOnAudioImplementation() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        startCapturingLogOutput(Level.INFO, Command.class.getName());

        interpretationCommand.onAudioImplementation(new byte[]{});

        String capturedLog = getCapturedLogOutput();
        assertTrue(capturedLog.toLowerCase().contains("audio received from"));
        assertTrue(capturedLog.toLowerCase().contains("interpretation"));
    }

    @Test
    public void testOnAudioResponseImplementation() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        assertFalse(interpretationCommand.onAudioResponseImplementation(new byte[]{}, true));
    }

    @Test
    public void testOnResponseImplementation() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);
        interpretationCommand.setClientFactory(mockClientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        InterpretationResponse mockResponse = mock(InterpretationResponse.class);
        when(mockResponse.toJson()).thenReturn("dummyResponse");

        interpretationCommand.execute();

        boolean status = interpretationCommand.onResponseImplementation(mockResponse);
        assertTrue(status);
    }

    @Test
    public void testOnResponseImplementationWithDebugLogs() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);
        interpretationCommand.setClientFactory(mockClientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        startCapturingLogOutput(Level.DEBUG, InterpretationCommand.class.getName());

        InterpretationResponse mockResponse = mock(InterpretationResponse.class);
        when(mockResponse.toJson()).thenReturn("dummyResponseJson");

        interpretationCommand.execute();

        boolean status = interpretationCommand.onResponseImplementation(mockResponse);
        assertTrue(status);
        assertTrue(getCapturedLogOutput().contains("dummyResponseJson"));
    }


    @Test
    public void testOnResponseImplementationWithDebugLogsThrowsException() throws Exception
    {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(dummyInterpretationClient);
        interpretationCommand.setClientFactory(mockClientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        startCapturingLogOutput(Level.DEBUG, InterpretationCommand.class.getName());

        InterpretationResponse mockResponse = mock(InterpretationResponse.class);
        when(mockResponse.toJson()).thenThrow(new CobraException(
                ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR,
                        "Exception thrown from mock class")));

        interpretationCommand.execute();

        boolean status = interpretationCommand.onResponseImplementation(mockResponse);
        assertTrue(status);
        assertTrue(getCapturedLogOutput().contains("can't log response"));
    }

    @Test
    public void testOnDirectiveStop() throws Exception
    {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        InterpretationClient mockInterpretationClient = mock(InterpretationClient.class);
        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(mockInterpretationClient);

        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.STOP);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        interpretationCommand.setClientFactory(mockClientFactory);

        // Call methods
        interpretationCommand.execute();
        interpretationCommand.onDirective(requestDirective, true, "tests");

        verify(mockInterpretationClient).startInterpretation();
        verify(mockInterpretationClient).stopInterpretation();
    }

    @Test
    public void testOnDirectiveCancel() throws Exception
    {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        InterpretationClient mockInterpretationClient = mock(InterpretationClient.class);
        PowerMockito.when(mockClientFactory.createInterpretationClient(any(), any(), any(), any()))
                    .thenReturn(mockInterpretationClient);

        interpretationCommand.setClientFactory(mockClientFactory);

        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.CANCEL);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        interpretationCommand.setResponseHandler(mockResponseHandler);

        // Call methods
        interpretationCommand.execute();
        interpretationCommand.onDirective(requestDirective, true, "tests");

        verify(mockInterpretationClient).startInterpretation();
        verify(mockInterpretationClient).cancelInterpretation(true);
    }

    @Test
    public void testGetResponseFormatter()
    {
        // Call method and check result
        assertTrue(interpretationCommand.getResponseFormatter() instanceof BaseResponseFormatter);
    }

    @Test
    public void testFormatResponse() throws CobraException
    {
        // Call method
        ResponseResult r = new ResponseResult();
        String formattedResponse = interpretationCommand.formatResponse(r);

        String json = "{'type':'result','sessionId':'12345','command':{'name':'interpretation','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        // Check result
        assertEquals(formattedResponse, json);
    }
}
