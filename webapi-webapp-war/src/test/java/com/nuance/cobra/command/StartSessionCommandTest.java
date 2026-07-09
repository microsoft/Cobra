package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.response.StartSessionResponse;
import com.nuance.cobra.session.*;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.cobra.utils.response.ResponseFormatter;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StartSessionCommandTest extends CobraTest
{
    private ResponseHandler mockResponseHandler;

    private Request request;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        mockResponseHandler = mock(ResponseHandler.class);
/*        String requestString = "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\", \"command\": { \"name\": \"endSession\", \"id\": \"1\" } }";


        request = Request.fromJson(requestString);*/

        Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key,"123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key,"DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key,"Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key,"4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key,"as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key,"appName");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key,"1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "MTIzNDU2Nzg5");
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key , VocalizerClients.nim.getValue());
        testConf.setProperty(WebApiConfig.Property.STUB_BACKEND.key ,false);
        testConf.setProperty(WebApiConfig.Property.COMMAND_PARAMS_TO_REMOVE_FROM_LOGS.key, new ArrayList<Object>());

        String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + "startSession" +
                "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"applicationKey\" , \"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\" } }}";
        request = Request.fromJson(requestString);

    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void test_constructor() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        assertEquals(command.request, request);
        assertEquals(command.commandConfigs, testConfs);
    }

    @Test(expected=NullPointerException.class)
    public void test_execute_nullSession() throws Exception
    {
        when(mockSessionManager.startSession(any(), any())).thenReturn(null);

        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        command.execute();
    }
    
    @Test
    public void test_execute_happy() throws Exception
    {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        WebApiJSONConfiguration requestConfig = (WebApiJSONConfiguration) cc.getConfiguration(0);

        requestConfig.setProperty(WebApiConfig.Property.NMAID_KEY.key, "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        requestConfig.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c");

        requestConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        final Session mockSession = mock(Session.class);
        when(mockSessionManager.startSession(any(), any())).thenReturn(mockSession);
        final Command mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn("mockCommandId");

        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final StartSessionCommand spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();
        
        SessionClient mockSessionClient = mock(SessionClient.class);
        when(mockClientFactory.createSessionClient(any(), any())).thenReturn(mockSessionClient);
        when(mockSession.getSessionConfig()).thenReturn(cc);
        when(mockSession.getConfigWithMerge(any())).thenReturn(cc);

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();
        
        
        verify(mockSession).setCommand(eq(spyCommand));

        verify(mockSessionManager).startSession(any(), any());
        
        verify(mockSessionClient).startSession(eq(mockResponseHandler), eq(cc));
    }
    
    @Test
    public void test_execute_throws_exception() throws Exception
    {

        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration requestConfig = CobraConfigurationUtils.getHighestConfig(cc);
        
        final Session mockSession = mock(Session.class);
        when(mockSessionManager.startSession(any(), any())).thenReturn(mockSession);
        doThrow(new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR, "an error occurred"))).when(mockSessionManager).startSession(request.requestCommand.parameters, testConfs);
        
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final StartSessionCommand spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();
        
        SessionClient mockSessionClient = mock(SessionClient.class);
        when(mockClientFactory.createSessionClient(any(), anyString())).thenReturn(mockSessionClient);
        when(mockSession.getSessionConfig()).thenReturn(cc);
        //doThrow(new InvalidConfigurationException("an error occured")).when(mockSessionClient).startSession(any(ResponseHandler.class), any(Config.class));
        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();
        
        verify(mockResponseHandler).onResponse(any());

        verify(mockSessionManager).startSession(any(), any());
        
    }

    @Test
    public void test_execute_invalid_request() throws Exception
    {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        WebApiJSONConfiguration requestConfig = (WebApiJSONConfiguration) cc.getConfiguration(0);

        requestConfig.setProperty(WebApiConfig.Property.NMAID_KEY.key, "b7635accbe5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        requestConfig.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c");

        requestConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        final Session mockSession = mock(Session.class);
        when(mockSessionManager.startSession(any(), any())).thenReturn(mockSession);
        final Command mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn("mockCommandId");

        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final StartSessionCommand spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();

        SessionClient mockSessionClient = mock(SessionClient.class);
        when(mockClientFactory.createSessionClient(any(), any())).thenReturn(mockSessionClient);
        when(mockSession.getSessionConfig()).thenReturn(cc);
        when(mockSession.getConfigWithMerge(any())).thenReturn(cc);

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();

        verify(spyCommand).sendErrorResponse(any());
    }

    @Test
    public void test_execute_invalidConfigurationException() throws Exception
    {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        WebApiJSONConfiguration requestConfig = (WebApiJSONConfiguration) cc.getConfiguration(0);

        requestConfig.setProperty(WebApiConfig.Property.NMAID_KEY.key, "b7635accbe5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        requestConfig.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c");

        requestConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        when(mockSessionManager.startSession(any(), any())).thenReturn(null);

        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final StartSessionCommand spyCommand = spy(command);

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();

        verify(spyCommand).sendErrorResponse(any());
    }

    @Test
    public void test_execute_illegalArgumentException() throws Exception
    {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        WebApiJSONConfiguration requestConfig = (WebApiJSONConfiguration) cc.getConfiguration(0);

        requestConfig.setProperty(WebApiConfig.Property.NMAID_KEY.key, "b7635accbe5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        requestConfig.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c");

        requestConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        when(mockSessionManager.startSession(any(), any())).thenThrow(new IllegalArgumentException());

        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final StartSessionCommand spyCommand = spy(command);

        spyCommand.setResponseHandler(mockResponseHandler);
        spyCommand.execute();

        verify(spyCommand).sendErrorResponse(any());
    }

    @Test(expected = NullPointerException.class)
    public void test_onDirective_null() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);

        command.onDirective(null, true, "tests");
    }


    @Test(expected = NullPointerException.class)
    public void test_onDirective_unknown() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);

        command.onDirective(new RequestDirective(), true, "tests");
    }

    @Test
    public void test_onAudio_null() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);

        command.onAudio(null);
    }

    @Test
    public void test_onAudio_empty() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);

        command.onAudio(new byte[0]);
    }

    @Test
    public void test_onAudio_happy() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);

        final byte[] happyBytes = new byte[] { 0, 0, 0, 0 };
        command.onAudio(happyBytes);
    }


    @Test
    public void test_getResponseFormatter() throws Exception
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        final ResponseFormatter formatter = command.getResponseFormatter();
        assertNotNull(formatter);
    }
    
    @Test
    public void test_formatResponse() throws TenantConfigRetrievalException, InvalidConfigurationException
    {
        StartSessionCommand command = spy(new StartSessionCommand(request, testConfs, null));
        PowerMockito.doReturn(new CommandLog()).when((Command)command).formatLog(any());
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";

        final Session mockSession = mock(Session.class);
        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSessionManager.getSession(anyString(), any(Boolean.class))).thenReturn(mockSession);

        JSONObject jsonObject = new JSONObject(resultString);
        StartSessionResponse startSessionResponse = StartSessionResponse.fromNiMResult(jsonObject);

        try
        {
            String result = command.formatResponse(startSessionResponse);
            String response = startSessionResponse.toJson();
            assertTrue(result.contains(response));
        }
        catch (CobraException unused)
        {
            //
        }

        CobraConfigurationUtils.clearCombinedConfiguration(testConfs, request.sessionId);
        try
        {
            String result = command.formatResponse(startSessionResponse);
            String response = startSessionResponse.toJson();
            assertTrue(result.contains(response));
        }
        catch (CobraException unused)
        {
            //
        }
    }

    @Test
    public void test_formatLog()
            throws JsonProcessingException
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        Response response = Response.fromRequest(request);
        response.setResult(new ResponseResult());
        String json =
                "{'type':'result','sessionId':'12345','command':{'name':'startSession','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        startCapturingLogOutput(Level.INFO, StartSessionCommand.class.getName());

        CommandLog commandLog = command.formatLog(response);

        assert (null != commandLog);
        assert (null != commandLog.toJson(false));
        assertNotNull(getCapturedLogOutput());
    }
    @Test
    public void test_formatLogException()
            throws JsonProcessingException
    {
        final StartSessionCommand command = new StartSessionCommand(request, testConfs, null);
        Response response = Response.fromRequest(request);
        response.setResult(new ResponseResult());
        String json =
                "{'type':'result','sessionId':'12345','command':{'name':'startSession','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        startCapturingLogOutput(Level.ERROR, StartSessionCommand.class.getName());

        CommandLog commandLog = command.formatLog(response);

        assertNotNull(getCapturedLogOutput());
    }
}
