package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.DynamicVocabularyClient;
import com.nuance.cobra.client.response.DynamicVocabularyResponse;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.testing.LoggingTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggingTest.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class UploadDynamicVocabularyCommandTest
        extends CobraTest
{
    private ResponseHandler mockResponseHandler;
    private Request request;
    private Session mockSession;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        mockResponseHandler = mock(ResponseHandler.class);

        String requestString = "{ \"type\": \"command\", \"sessionId\": \"233005b4-ee80-40e1-a2c9-9b31bfaf82ff\", \"command\": { \"name\": \"uploadDynamicVocabulary\", \"id\": \"1\", \"parameters\": { \"dynamicVocabularies\": [{ \"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset\", \"body\": { \"place-name\": [{ \"literal\": \"Los Angeles\", \"spoken\": [\"l a\", \"los angeles\"] }, { \"literal\": \"San Francisco\" }], \"hockey-team\": [{ \"literal\": \"Montreal Canadiens\", \"spoken\": [\"montreal canadiens\", \"habs\"] }, { \"literal\": \"Boston Bruins\" }] } }] } } }";
        request = Request.fromJson(requestString);
        mockSession = mock(Session.class);
        when(mockSessionManager.getSession(any(), anyBoolean())).thenReturn(mockSession);

        Configuration config = CobraConfigurationUtils.getHighestConfig(mockServerConfigs);
        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key,"123");
        config.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key,"DV_123");
        config.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key,"Linux");
        config.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key,"4.14");
        config.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key,"as23213df");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key,"appName");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key,"1");
        config.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "MTIzNDU2Nzg5");

        when(mockSession.getSessionConfig()).thenReturn(mockServerConfigs);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testConstructor() throws Exception
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        assertEquals(command.request, request);
        assertEquals(command.commandConfigs, mockServerConfigs);
    }

    @Test(expected=NullPointerException.class)
    public void test_execute_nullSession() throws Exception
    {
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(null);

        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        command.execute();

        verify(mockResponseHandler).onResponse(any());
    }

    @Test
    public void test_execute_happy() throws Exception
    {
        CombinedConfiguration requestConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration requestConfig = CobraConfigurationUtils.getHighestConfig(requestConfigs);
        final Command mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn("mockCommandId");

        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, requestConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        ClientFactory mockClientFactory = mock(ClientFactory.class);
        doReturn(mockClientFactory).when(spyCommand).getClientFactory();
        DynamicVocabularyClient mockDynamicVocabularyClient = mock(DynamicVocabularyClient.class);
        when(mockSession.getSessionConfig()).thenReturn(requestConfigs);
        when(mockSession.getConfigWithMerge(any())).thenReturn(requestConfigs);
        when(mockClientFactory.createDynamicVocabularyClient(any(CombinedConfiguration.class), anyString(), any())).thenReturn(mockDynamicVocabularyClient);

        HashMap<String, Object> vocabularyHashMap = new HashMap<>();
        vocabularyHashMap.put("id","b");
        vocabularyHashMap.put("type", "application/x-nuance-wordset");
        HashMap body = new HashMap();
        List<Map> vocabs = new ArrayList<>();
        HashMap vone = new HashMap();
        DynamicVocabulary.Vocabulary dynamicVocabulary = mock(DynamicVocabulary.Vocabulary.class);
        dynamicVocabulary.setLiteral("bob");
        vone.put("literal","bob");
        vocabs.add(vone);
        List arrayList = new ArrayList();
        arrayList.add(mock(DynamicVocabulary.Vocabulary.class));
        body.put("vOne",vocabs);
        vocabularyHashMap.put("body",body);
        ArrayList<String> spokenList = new ArrayList<>();
        spokenList.add("Powow");
        DynamicVocabulary mockVocabulary = new DynamicVocabulary("id", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET,new HashMap<>());
        requestConfig.setProperty(WebApiConfig.Property.DYNAMIC_VOCABULARIES.key, vocabularyHashMap);
        spyCommand.setResponseHandler(mockResponseHandler);

        spyCommand.execute();

        verify(mockSession).setCommand(eq(spyCommand));

        verify(mockDynamicVocabularyClient).uploadDynamicVocabulary();
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_execute_config_throws_exeption()
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        spyCommand.setResponseHandler(mockResponseHandler);

        when(mockSession.getConfigWithMerge(any())).thenThrow(new IllegalArgumentException("Error"));

        spyCommand.execute();

        verify(mockResponseHandler).onResponse(any());

    }

    @Test
    public void test_on_response_receiver_happy()
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        final ResponseResult result = new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, true);
        spyCommand.setResponseHandler(mockResponseHandler);

        boolean status = spyCommand.onResponse(result);

        verify(mockSession).setCommand(null);
        assertTrue(status);

    }

    @Test
    public void test_on_response_receiver_happy_not_final()
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        final ResponseResult result = new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, false);
        spyCommand.setResponseHandler(mockResponseHandler);

        boolean status = spyCommand.onResponse(result);

        assertTrue(status);

    }

    @Test
    public void test_on_response_receiver_happy_no_session()
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        final ResponseResult result = new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, false);
        spyCommand.setResponseHandler(mockResponseHandler);

        when(mockSessionManager.getSession(any(), anyBoolean())).thenReturn(null);
        boolean status = spyCommand.onResponse(result);


        verify(mockSession, Mockito.times(0)).setCommand(null);
        assertTrue(status);

    }

    @Test
    public void test_on_response_receiver_exception()
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        final ResponseResult result = new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, true);
        spyCommand.setResponseHandler(mockResponseHandler);


        doThrow(new NullPointerException()).when(mockSession).setCommand(null);
        boolean status = spyCommand.onResponse(result);

        verify(mockSession).setCommand(null);
        assertTrue(status);

    }


    @Test
    public void test_on_format_formatter_happy()
            throws CobraException
    {
        final UploadDynamicVocabularyCommand command = new UploadDynamicVocabularyCommand(request, mockServerConfigs, null);
        final UploadDynamicVocabularyCommand spyCommand = spy(command);
        final ResponseResult result = new DynamicVocabularyResponse(ResponseResult.ResponseStatus.SUCCESS, true);

        Response expectedResponse = Response.fromRequest(request);
        expectedResponse.setSessionId(request.sessionId);
        expectedResponse.setResult(result);

        String responseReceived = spyCommand.formatResponse(result);

        assertTrue(expectedResponse.toJson().equals(responseReceived));

    }
}
