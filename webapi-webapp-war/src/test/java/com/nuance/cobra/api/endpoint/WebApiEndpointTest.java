package com.nuance.cobra.api.endpoint;

import com.google.api.client.util.ArrayMap;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestCommand;
import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.command.CommandFactory;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.testing.MetricsVerifier;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.error.listener.ErrorListener;
import com.nuance.cobra.utils.webapi.ResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletContextEvent;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class, SessionManager.class, CommandFactory.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*", "sun.net.*", "com.sun.*"})
public class WebApiEndpointTest extends CobraTest
{
    ResponseSender responseSender = mock(ResponseSender.class);
    Logger logger = mock(Logger.class);
    private CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration config = CobraConfigurationUtils.getHighestConfig(configs);
    private WebApiEndpoint endpoint;
    private com.nuance.cobra.session.Session session;
    private Session mockSession = mock(Session.class);

    private String startSession = "{" +
            "\"version\":\"1.0\"," +
            "\"type\":\"command\"," +
            "\"command\":" +
            "{\"name\":\"startSession\"," +
            "\"id\":\"1\"," +
            "\"parameters\":" +
            "{\"applicationKey\":\"companyName_appName\"," +
            "\"authentication\": {" +
            "\"type\": \"hashcode\"," +
            "\"data\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"" +
            "}," +
            "\"speechSynthesisCodec\":\"opus_wb\"," +
            "\"speechRecognitionCodec\":\"pcm_16_16k\"" +
            "}" +
            "}" +
            "}";

    private String speechReco = "{" +
            "\"type\": \"command\"," +
            "\"sessionId\": \"%s\"," +
            "\"command\": {" +
            "\"name\": \"speechRecognition\"," +
            "\"id\": \"2\"," +
            "\"parameters\": {" +
            "\"endPointDetection\": true," +
            "\"beginNoiseSampleFrames\": 10," +
            "\"voiceThreshold\": 4," +
            "\"startOfSpeechVoicedFrames\": 11," +
            "\"startOfSpeechHistoryFrames\": 20," +
            "\"endOfSpeechVoicedFrames\": 12," +
            "\"endOfSpeechHistoryFrames\": 40," +
            "\"stopOnEndOfSpeech\": true," +
            "\"wordStream\": true," +
            "\"startOfSpeechTimeoutSeconds\": 5," +
            "\"utteranceMaxTimeSeconds\": 30," +
            "\"activeDynamicVocabularySets\": [" +
            "\"some_id1\"," +
            "\"some_id2\"" +
            "]," +
            "\"statistics\": true" +
            "}" +
            "}" +
            "}";
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);

    @Before
    public void setUp(){
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfs);

        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_COMPANY_NAME.key, "nuance");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key, "testApplication");
        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "12351");
        config.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key, "testId");
        config.setProperty(WebApiConfig.Property.COMMAND_TIMEOUT_MILLISECONDS.key, 100);
        config.setProperty(WebApiConfig.Property.NMAID_KEY.key, "1234567890");
        config.setProperty(WebApiConfig.Property.NMSP_GATEWAY_HOST.key, "host");
        config.setProperty(WebApiConfig.Property.NMAID_ID.key, "testId");
        session = new com.nuance.cobra.session.Session("testId", configs);
        System.out.println(config.toString());
        PowerMockito.mockStatic(SessionManager.class);
        mockSessionManager = mock(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        when(mockSessionManager.getSession("testId", true)).thenReturn(session);
        endpoint = new WebApiEndpoint(responseSender);
        endpoint.activeSession = session;
    }

    @Test
    public void getActiveSessionTest() {
        assertNotNull(endpoint.getActiveSession());
        endpoint.activeSession = null;
        assertNull(endpoint.getActiveSession());
        assertNull(endpoint.getWebapiSession(""));
    }

    @Test
    public void onBinaryTest() {
        byte[] testBuffer = {2};

        assertNotNull(testBuffer);

        SessionManager mockSessionManager = mock(SessionManager.class);
        PowerMockito.mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        PowerMockito.when(SessionManager.getInstance().getSession(mockSession)).thenReturn(session);
        endpoint.onBinary(mockSession, testBuffer);

        endpoint.activeSession = session;
        endpoint.onBinary(mockSession, testBuffer);

        when(mockSessionManager.getSession(any(), anyBoolean())).thenReturn(session);

        endpoint.onBinary(mockSession, testBuffer);
    }

    @Test
    public void onOpenTest() throws CobraException {
        Map<String, List<String>> testParams = new ArrayMap<>();
        ArrayList<String> testList = new ArrayList<>();

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(configs);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(anyString())).thenReturn(configs);

        testList.add(new String(Base64.getEncoder().encode(startSession.getBytes())));
        testParams.put("authentication", testList);

        when(mockSession.getRequestParameterMap()).thenReturn(testParams);
        endpoint.activeSession = session;
        endpoint.onOpen(mockSession);

        testList.add("c3RhcnRTZXNzaW9u");
        testParams.remove("authentication");
        testParams.put("authentication", testList);
//        endpoint.onOpen(mockSession);

        assertEquals(new String(Base64.getEncoder().encode(startSession.getBytes())), testList.get(0));


    }

    @Test
    public void onPongTest() throws IOException {
        PongMessage mockPongMessage = mock(PongMessage.class);
        WebApiEndpoint endpointSpy = Mockito.spy(endpoint);
        endpointSpy.onPong(mockSession, mockPongMessage);
        verify(endpointSpy, times(1)).onPong(mockSession, mockPongMessage);
    }

    @Test
    public void onInternalErrorTest() throws IOException, InterruptedException {
        ResponseSender responseSender = mock(ResponseSender.class);
        Logger logger = mock(Logger.class);

        WebApiEndpoint endpoint = new WebApiEndpoint(responseSender);

        Session mockSession = mock(Session.class);
        ResponseCode mockError = mock(ResponseCode.class);
        Request mockRequest = mock(Request.class);
        ResponseErrorHandler mockResponseErrorHandler = mock(ResponseErrorHandler.class);
        RequestCommand requestCommand = new RequestCommand();

        // make sure the tenant name is unique from other test cases, otherwise the test may fail.
        String TENANT_NAME = "test-tenant-onInternalErrorTest";
        String REQUEST_COMMAND = "test-command";

        mockRequest.tenantKey = TENANT_NAME;
        requestCommand.name = REQUEST_COMMAND;
        mockRequest.requestCommand = requestCommand;

        int ERROR_CODE_VALUE = 4242;
        when (mockError.getCode()).thenReturn(ERROR_CODE_VALUE);

        endpoint.errorHandler = mockResponseErrorHandler;

        doNothing().when(mockResponseErrorHandler).addListener(any(ErrorListener.class));
        doNothing().when(mockResponseErrorHandler).handleError(any(IResponse.class));

        endpoint.onInternalError(mockSession, mockError, mockRequest);
        assertTrue(new MetricsVerifier().verifyMetric("webapi_command_errors_total",
                new String[]{MetricsConstants.TENANT_LABEL, MetricsConstants.COMMAND_LABEL, MetricsConstants.ERROR_CODE_LABEL},
                new String[]{TENANT_NAME, REQUEST_COMMAND, Integer.toString(ERROR_CODE_VALUE)},
                1.0));
    }


    @Test
    public void onError() {
        WebApiEndpoint endpoint = new WebApiEndpoint(responseSender);
        Session mockSession = mock(Session.class);
        Throwable mockThrowable = mock(Throwable.class);

        //Nullable throwable
        endpoint.onError(mockSession, null);

        //Null message
        when(mockThrowable.getMessage()).thenReturn(null);
        endpoint.onError(mockSession, mockThrowable);

        //Success
        when(mockThrowable.getMessage()).thenReturn("test message");
        endpoint.onError(mockSession, mockThrowable);

        //Success
        when(mockThrowable.getMessage()).thenReturn("Timeout on Read");
        endpoint.onError(mockSession, mockThrowable);

        //Success
        when(mockThrowable.getMessage()).thenReturn("Broken pipe");
        endpoint.onError(mockSession, mockThrowable);

        //Success
        mockThrowable = mock(IOException.class);
        when(mockThrowable.getMessage()).thenReturn("Broken pipe");
        endpoint.onError(mockSession, mockThrowable);
    }

    @Test
    public void closeWebsocketTest() throws IOException {
        when(mockSession.isOpen()).thenReturn(true);
        endpoint.closeWebsocket(mockSession, "");
        verify(mockSession, times(1)).close(any(CloseReason.class));
    }

    @Test
    public void onCloseTest() throws CobraException {
        WebApiEndpoint endpoint = new WebApiEndpoint(responseSender);
        Session mockSession = mock(Session.class);
        mockStatic(WebApiConfigManager.class);
        config.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key, null);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant("companyName_appName")).thenReturn(configs);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(configs);
        CloseReason mockReason = mock(CloseReason.class);

        //reason null
        endpoint.onClose(mockSession, null);

        assertNotNull(endpoint);
        assertNull(endpoint.getActiveSession());

        endpoint.activeSession = session;

        assertNotNull(endpoint.getActiveSession());
        //reason null getReasonPhrase
        when(mockReason.getReasonPhrase()).thenReturn(null);
        endpoint.onClose(mockSession, mockReason);

        when(mockReason.getReasonPhrase()).thenReturn("testPhrase");
        //reason null getCloseCode
        when(mockReason.getCloseCode()).thenReturn(CloseReason.CloseCodes.SERVICE_RESTART);
        endpoint.onClose(mockSession, mockReason);

        //working one
        com.nuance.cobra.session.Session mockWebApiSession = mock(com.nuance.cobra.session.Session.class);
        SessionManager mockSessionManager = mock(SessionManager.class);
        PowerMockito.mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        when(mockSessionManager.getSession(any())).thenReturn(session);
        endpoint.onClose(mockSession, mockReason);
    }

    @Test
    public void getConfigsTest() throws Exception
    {
        final String sessionId = "testSessionId";
        final CombinedConfiguration mockSessionConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration mockSessionConfig = mockSessionConfigs.getConfiguration(0);
        WebApiEndpoint endpoint = new WebApiEndpoint(responseSender);
        mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(any())).thenReturn(mockSessionConfigs);

        CombinedConfiguration c1 = endpoint.getConfigs("blah");
        assertEquals(mockSessionConfigs, c1);

        CombinedConfiguration injectedConfig = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        WebApiEndpoint endpoint2 = new WebApiEndpoint(responseSender, injectedConfig);
        CombinedConfiguration c2 = endpoint2.getConfigs(null);
        assertEquals(injectedConfig.getConfiguration(0), c2.getConfiguration(0));

        final com.nuance.cobra.session.Session
                session = new com.nuance.cobra.session.Session(sessionId, mockSessionConfigs);
        endpoint.activeSession = session;
        CombinedConfiguration c3 = endpoint.getConfigs(null);
        assertEquals(mockSessionConfigs, c3);
    }

    @Test
    public void setWebsocketMaximumsTest(){
        endpoint.setWebsocketMaximums(mockSession);

        when(mockSessionManager.getSession(mockSession)).thenReturn(null);
        assertNotNull(endpoint.activeSession);

        endpoint.setWebsocketMaximums(mockSession);

        PowerMockito.mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        PowerMockito.when(mockSessionManager.getSession(mockSession)).thenReturn(session);

        endpoint.setWebsocketMaximums(mockSession);
    }

    @Test
    public void initContext() {
        ServletContextEvent mockContext = mock(ServletContextEvent.class);
        WebApiEndpoint.contextInitialized(mockContext);
        WebApiEndpoint.contextDestroyed(mockContext);
    }

    @Test
    public void handleDirectiveSessionEnded()
            throws IOException
    {
        WebApiEndpoint endpointSpy = Mockito.spy(endpoint);
        String message = "{ \"type\": \"directive\", \"command\": { \"name\": \"speechRecognition\", \"id\": \"3\" }, \"directive\": { \"name\": \"stop\" }, \"sessionId\": \"testId\" }";
        endpointSpy.activeSession.setEndSessionTime(Calendar.getInstance());
        endpointSpy.onMessage(mockSession, message);
        verify(endpointSpy, times(0)).onInternalError(any(), (ResponseCode) any(), any());
    }

    @Test
    public void closeWebsocketException()
            throws IOException
    {
        when(mockSession.isOpen()).thenThrow(new IllegalStateException());
        endpoint.closeWebsocket(mockSession, "");
        verify(mockSession, times(0)).close(any(CloseReason.class));
    }

}
