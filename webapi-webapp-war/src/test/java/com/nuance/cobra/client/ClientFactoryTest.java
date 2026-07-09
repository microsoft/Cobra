package com.nuance.cobra.client;

import com.nuance.cobra.client.azure.AzureASRClient;
import com.nuance.cobra.client.azure.AzureTokenProvider;
import com.nuance.cobra.client.mix.MixClientCredentialsManager;
import com.nuance.cobra.client.mix.forgetme.ForgetMeGrpcClient;
import com.nuance.cobra.client.mix.grpc.InterceptedManagedChannel;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.client.mix.krypton.KryptonGrpcClient;
import com.nuance.cobra.client.mix.nlu.NluAasGrpcClient;
import com.nuance.cobra.client.mix.nvc.NvcClient;
import com.nuance.cobra.client.nim.MrecNimClient;
import com.nuance.cobra.client.nim.VocalizerNimClient;
import com.nuance.cobra.session.*;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.testing.MetricsVerifier;
import com.nuance.cobra.utils.HealthCheckException;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MixClientCredentialsManager.class, ClientFactory.class, AzureTokenProvider.class,})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*","sun.net.*", "com.sun.*", "javafx.*"})
public class ClientFactoryTest extends CobraTest
{
    private String sessionId;
    private String commandId;

    private final Session mockSession = mock(Session.class);

    private ResponseHandler responseHandlerMock;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        responseHandlerMock = mock(ResponseHandler.class);
        sessionId = "testSessionId";
        commandId = "testCommandId";
        PowerMockito.mockStatic(MixClientCredentialsManager.class);
    }

    @After
    public void cleanUp() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void test_constructor() throws Exception
    {
        new ClientFactory();
    }

    private SpeechRecoClient test_createClient(String speechRecEngine) throws Exception
    {
        return test_createClient(speechRecEngine, null);
    }

    private SpeechRecoClient test_createClient(String speechRecEngine, String authMethod) throws Exception
    {
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = CobraConfigurationUtils.getHighestConfig(mockConfigs);

        if(authMethod != null)
        {
            mockConfig.setProperty(WebApiConfig.Property.KRYPTON_GRPC_AUTH_METHOD.key, authMethod);
        }

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key, speechRecEngine);
        mockConfig.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        mockConfig.setProperty(WebApiConfig.Property.UTTERANCE_MAX_TIME_SECONDS.key, 30);
        mockConfig.setProperty(WebApiConfig.Property.AUDIO_CAPTURE.key, 0);
        mockConfig.setProperty(WebApiConfig.Property.WAV_ENABLED.key, false);
        mockConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        mockConfig.setProperty(WebApiConfig.Property.ENABLE_EDF_LOG_POSTER.key, false);
        mockConfig.setProperty(WebApiConfig.Property.KRYPTON_GRPC_SERVER_URL.key, "http://host:443");
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "test-datapack");
        mockConfig.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_API_KEY.key, "dummySubscriptionKey");
        mockConfig.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.key, "dummyRegion");
        mockConfig.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "dummyKeyVaultClientId");
        when (mockSession.getSessionConfig()).thenReturn(mockConfigs);
        when (mockSession.getSessionId()).thenReturn(sessionId);

        // Mock the AzureTokenProvider and its getAccessToken method
        AzureTokenProvider mockTokenProvider = mock(AzureTokenProvider.class);
        when(mockTokenProvider.getAccessToken()).thenReturn("mockAccessToken");
        PowerMockito.whenNew(AzureTokenProvider.class).withAnyArguments().thenReturn(mockTokenProvider);

        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(mockConfigs);

        final SpeechRecoClient speechRecoClient = clientFactory.createSpeechRecoClient(mockConfigs, mockSession, new ArrayList<DynamicVocabulary>(), mockResponseHandler);
        return speechRecoClient;
    }

    @Test
    public void test_createMrecClient() throws Exception
    {
        SpeechRecoClient speechRecoClient = test_createClient(SpeechRecognitionClients.MREC_NIM.getValue());
        assertNotNull("speechRecoClient is not null", speechRecoClient);
        assertTrue("client created is an instance of MrecNimClient", speechRecoClient instanceof MrecNimClient);

        String speechRecEngine = "MrecNim";
        assertTrue(verifySpeechRecMetric(speechRecEngine));
    }

    @Test
    public void test_createAzureClient() throws Exception
    {
        SpeechRecoClient speechRecoClient = test_createClient(SpeechRecognitionClients.SPEECH_SERVICE_ASR.getValue());
        assertNotNull("speechRecoClient is not null", speechRecoClient);
        assertTrue("client created is an instance of AzureSpeechClient", speechRecoClient instanceof AzureASRClient);
    }

    @Test
    public void test_createKryptonGrpcClient() throws Exception
    {
        MixClientCredentialsManager.init();
        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn("abc1234");
        SpeechRecoClient speechRecoClient = test_createClient(SpeechRecognitionClients.KRYPTON_GRPC.getValue());
        assertNotNull("speechRecoClient is not null", speechRecoClient);
        assertTrue("client created is an instance of KryptonGrpcClient", speechRecoClient instanceof KryptonGrpcClient);
        String speechRecEngine = "Krypton-gRPC";
        assertTrue(verifySpeechRecMetric(speechRecEngine));
    }

    @Test
    public void test_createKryptonGrpcClientAuths() throws Exception
    {
        test_createKryptonGrpcClientAuth(NvcClient.AUTH_PASSTHROUGH);
        for (List<InterceptedManagedChannel> managedChannelList: ManagedChannelManager.getManagedChannelCache().values())
        {
            managedChannelList.get(0).getChannel().shutdown();
        }
        test_createKryptonGrpcClientAuth(NvcClient.AUTH_NO_AUTH);
    }

    public void test_createKryptonGrpcClientAuth(String authType) throws Exception
    {
        SpeechRecoClient speechRecoClient = test_createClient(SpeechRecognitionClients.KRYPTON_GRPC.getValue(), authType);
        assertNotNull("speechRecoClient is not null", speechRecoClient);
        assertTrue("client created is an instance of KryptonGrpcClient", speechRecoClient instanceof KryptonGrpcClient);
        String speechRecEngine = "Krypton-gRPC";
        assertTrue(verifySpeechRecMetric(speechRecEngine));
    }

    @Test
    public void test_createKryptonGrpcClientNull() throws Exception
    {
        MixClientCredentialsManager.init();
        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn(null);
        SpeechRecoClient speechRecoClient = test_createClient(SpeechRecognitionClients.KRYPTON_GRPC.getValue());
        assertNull(speechRecoClient);
    }

    private boolean verifySpeechRecMetric(String speechRecEngine) throws IOException, InterruptedException {
        String datapack = "test-datapack";

        return new MetricsVerifier().verifyMetric(
                "webapi_asr_requests_total",
                new String[]{MetricsConstants.ENGINE_LABEL, MetricsConstants.DATAPACK_LABEL},
                new String[]{speechRecEngine, datapack},
                1.0);
    }

    @Test
    public void test_createSpeechSynthesisClient() throws Exception
    {
        MixClientCredentialsManager.init();
        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn("abc1234");
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration mockConfig = (WebApiJSONConfiguration) CobraConfigurationUtils.getHighestConfig(mockConfigs);
        mockConfig.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nim.getValue());
        mockConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        String speech_synthesis_voice = "test-voice";
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, speech_synthesis_voice);
        when(mockSession.getSessionConfig()).thenReturn(mockConfigs);
        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);
        responseHandlerMock = mock(ResponseHandler.class);

        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(mockConfigs);

        final SpeechSynthesisClient speechSynthesisClient = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClient);
        assertTrue("client created is an instance of VocalizerNimClient", speechSynthesisClient instanceof VocalizerNimClient);

        mockConfig.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nvc.getValue());
        mockConfig.setProperty(WebApiConfig.Property.NVC_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.addProperty("vocsMapping.test-voice.language", "en-us");
        mockConfig.addProperty("vocsMapping.test-voice.nvcVersion", "nvc3");
        SpeechSynthesisClient speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientNvcTest);
        assertTrue("client created is an instance of NvcClient", speechSynthesisClientNvcTest instanceof NvcClient);

        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn(null);
        speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNull(speechSynthesisClientNvcTest);

        mockConfig.setProperty(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key, NvcClient.AUTH_NO_AUTH);
        mockConfig.addProperty("vocsMapping.test-voice.nvcVersion", "nvc2");
        speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientNvcTest);
        assertTrue("client created is an instance of NvcClient", speechSynthesisClientNvcTest instanceof NvcClient);

        mockConfig.setProperty(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key, NvcClient.AUTH_PASSTHROUGH);
        speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientNvcTest);
        assertTrue("client created is an instance of NvcClient", speechSynthesisClientNvcTest instanceof NvcClient);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "1234321");
        mockConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        mockConfig.setProperty(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key, NvcClient.AUTH_PASSTHROUGH);
        speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientNvcTest);
        assertTrue("client created is an instance of NvcClient", speechSynthesisClientNvcTest instanceof NvcClient);

        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        speechSynthesisClientNvcTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientNvcTest);
        assertTrue("client created is an instance of NvcClient", speechSynthesisClientNvcTest instanceof NvcClient);

        mockConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, true);
        final SpeechSynthesisClient speechSynthesisClientTest = clientFactory.createSpeechSynthesisClient(mockConfigs, sessionId, responseHandlerMock);
        assertNotNull("speechSynthesisClient is not null", speechSynthesisClientTest);

        String[] engines = {"NiM", "NVC"};
        for(String engine: engines) {
            assertTrue(new MetricsVerifier().verifyMetric(
                   "webapi_tts_requests_total",
                    new String[]{MetricsConstants.ENGINE_LABEL, MetricsConstants.VOICE_LABEL},
                    new String[]{engine, speech_synthesis_voice},
                    1.0));
        }

    }

    @Test
    public void test_createSessionClient() throws Exception
    {
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        mockConfig.setProperty(WebApiConfig.Property.NIM_NCS_SESSION_REQUIRED.key, true);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        final SessionClient sessionClient = clientFactory.createSessionClient(mockConfigs, sessionId);
        assertNotNull("sessionClient is not null", sessionClient);

        mockConfig.setProperty(WebApiConfig.Property.NIM_NCS_SESSION_REQUIRED.key, false);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        final SessionClient testSessionClient = clientFactory.createSessionClient(mockConfigs, sessionId);
        assertNotNull("sessionClient is not null", testSessionClient);
    }

    @Test
    public void test_interpretationClient(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        mockConfig.setProperty(WebApiConfig.Property.INTERPRETATION_BACKEND.key, InterpretationClients.NLU_GRPC.getValue());
        mockConfig.setProperty(WebApiConfig.Property.NLUAAS_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.NLUAAS_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN);
        MixClientCredentialsManager.init();
        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn("abc1234");
        InterpretationClient interpretationClient = clientFactory.createInterpretationClient(mockConfigs, mockSession.getSessionId(), new ArrayList<DynamicVocabulary>(), mockResponseHandler);
        assertNotNull("Interpretation client is not null", interpretationClient);
        assertTrue("client created is an instance of NluAasGrpcClient", interpretationClient instanceof NluAasGrpcClient);

        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn(null);
        interpretationClient = clientFactory.createInterpretationClient(mockConfigs, mockSession.getSessionId(), new ArrayList<DynamicVocabulary>(), mockResponseHandler);
        assertNull(interpretationClient);
    }

    @Test
    public void test_userResetClient_invalidClient(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.USER_RESET_BACKEND.key, "fake");

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        UserResetClient userResetClient = clientFactory.createUserResetClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNull("userReset client is not null", userResetClient);
    }

    @Test
    public void test_userResetClient_bearerToken(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.USER_RESET_BACKEND.key, UserResetClients.MIX.getValue());
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN);
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        MixClientCredentialsManager.init();
        when(MixClientCredentialsManager.getBearerToken(any())).thenReturn("abc1234");
        UserResetClient userResetClient = clientFactory.createUserResetClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNotNull("userReset client is not null", userResetClient);
        assertTrue("client created is an instance of ForgetMeGrpcClient", userResetClient instanceof ForgetMeGrpcClient);
    }

    @Test
    public void test_userResetClient_mixFailure(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.USER_RESET_BACKEND.key, UserResetClients.MIX.getValue());
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN);
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        UserResetClient userResetClient = clientFactory.createUserResetClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNull("userReset client is not null", userResetClient);
    }

    @Test
    public void test_userResetClient_passthrough(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.USER_RESET_BACKEND.key, UserResetClients.MIX.getValue());
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_PASSTHROUGH);
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_PASSTHROUGH);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        UserResetClient userResetClient = clientFactory.createUserResetClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNotNull("userReset client is not null", userResetClient);
        assertTrue("client created is an instance of ForgetMeGrpcClient", userResetClient instanceof ForgetMeGrpcClient);
    }

    @Test
    public void test_userResetClient_noAuth(){
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration mockConfig = mockConfigs.getConfiguration(0);
        mockConfig.setProperty(WebApiConfig.Property.USER_RESET_BACKEND.key, UserResetClients.MIX.getValue());
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_NO_AUTH);
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_SERVER_URL.key, "https://host:443");
        mockConfig.setProperty(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key, KryptonGrpcClient.AUTH_NO_AUTH);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        UserResetClient userResetClient = clientFactory.createUserResetClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNotNull("userReset client is not null", userResetClient);
        assertTrue("client created is an instance of ForgetMeGrpcClient", userResetClient instanceof ForgetMeGrpcClient);
    }

    @Test
    public void test_dynamicVocabClient()
    {
        final ClientFactory clientFactory = new ClientFactory();
        CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration mockConfig = mockConfigs.getConfiguration(0);

        final ResponseHandler mockResponseHandler = mock(ResponseHandler.class);

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);

        final DynamicVocabularyClient vocabClient = clientFactory.createDynamicVocabularyClient(mockConfigs, mockSession.getSessionId(), mockResponseHandler);
        assertNotNull("DynamicVocabularyClient client is not null", vocabClient);
    }

    @Test (expected = HealthCheckException.class)
    public void test_healthCheckClient() throws Exception{
        final ClientFactory clientFactory = new ClientFactory();
        String type = "NinaMobile";
        Map<String, Object> testNinaMap = new HashMap<String, Object>();
        testNinaMap.put("nmspGatewayHost", "Test");
        testNinaMap.put("nmspGatewayPort", 1000);

        final HealthCheckClient testClient = clientFactory.createHealthCheckClient(type, testNinaMap);
        assertNotNull("HealthCheckClient is not Null", testClient);

        type = "tcpSocket";
        Map<String, Object> testTcpMap = new HashMap<String, Object>();
        testTcpMap.put("endpointHost", "Test");
        testTcpMap.put("endpointPort", 1000);
        final HealthCheckClient testTcpClient = clientFactory.createHealthCheckClient(type, testTcpMap);
        assertNotNull("HealthCheckClient is not Null", testTcpClient);

        type = "default";
        final HealthCheckClient testDefaultClient = clientFactory.createHealthCheckClient(type, testTcpMap);
    }

    @Test
    public void testCustomHeaders() {
        final ClientFactory clientFactory = new ClientFactory();

        assertTrue(clientFactory.getCustomGrpcHeaders("sessionId", "nvcVersion").size() == 1);
        assertTrue(clientFactory.getCustomGrpcHeaders("sessionId", "").size() == 1);
        assertTrue(clientFactory.getCustomGrpcHeaders("sessionId", null).size() == 1);
        assertTrue(clientFactory.getCustomGrpcHeaders("sessionId", "nvc3").size() == 2);
        assertTrue(clientFactory.getCustomGrpcHeaders("sessionId", "nvc3").get("x-nuance-tts-neural").equalsIgnoreCase("true"));
    }
}
