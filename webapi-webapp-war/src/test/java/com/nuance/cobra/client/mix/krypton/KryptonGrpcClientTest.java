package com.nuance.cobra.client.mix.krypton;

import com.google.protobuf.TextFormat;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.asr.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nuance.rpc.asr.v1.EnumResultType.FINAL;
import static com.nuance.rpc.asr.v1.EnumResultType.PARTIAL;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({SessionManager.class})
public class KryptonGrpcClientTest extends CobraTest {

    private static String SPEECH_RECOGNITION_CODEC = "pcm_16_16k";
    private static String SPEECH_RECOGNITION_LANGUAGE = "en-US";

    @ClassRule
    public static GrpcCleanupRule cleanupRule = new GrpcCleanupRule();

    private static String serverName = InProcessServerBuilder.generateName();
    private static TestKryptonGrpcService service = new TestKryptonGrpcService();
    private static ManagedChannel channel;

    private ResponseHandler mockResponseHandler;
    private ResponseErrorHandler mockErrorHandler;
    private KryptonGrpcClient client;
    private CombinedConfiguration configs;

    @BeforeClass
    public static void runsOnlyOnceBeforeTests() throws IOException {
        Server grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor() // directExecutor is fine for unit tests
                .addService(service)
                .build()
                .start();
        cleanupRule.register(grpcServer);
    }

    @Before
    public void runsBeforeEachTest() throws Exception {
        super.setUp(); // required so that CobraTest.setUp() is called

        //mock the session objects
        var mockStatistics = mock(Command.CommandStatistics.class);
        var mockCommand = mock(Command.class);
        var mockSession = mock(Session.class);
        PowerMockito.mockStatic(SessionManager.class);
        var sessionManagerHolderClass = Class.forName("com.nuance.cobra.session.SessionManager$SessionManagerHolder");
        var mockSessionManager = mock(SessionManager.class);
        var originalSessionManager = TestUtils.inject(sessionManagerHolderClass, "instance", mockSessionManager);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));
        when(mockCommand.getStatistics()).thenReturn(mockStatistics);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        //

        service.resetMocks();
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                0,
                "",
                false);

        mockResponseHandler = mock(ResponseHandler.class);
        mockErrorHandler = mock(ResponseErrorHandler.class);
        configs = buildCobraConfig();
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(buildCobraConfig(), mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
    }

    private static RecognizerGrpc.RecognizerStub buildStub() {
        // gRPC channel (client<->server)
        // we build a new channel each time because they are shut down after each asr request
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor() // directExecutor is fine for unit tests
                .build();

        // gRPC stub
        return RecognizerGrpc.newStub(channel);
    }

    private static CombinedConfiguration buildCobraConfig() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        // start session command
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, SPEECH_RECOGNITION_CODEC);

        // speech recognition command
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, SPEECH_RECOGNITION_LANGUAGE);

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "KryptonGrpcClientTest");

        return configs;
    }

    private Result buildResult_normal(EnumResultType resultType) { // NuanceAsrResult.EnumResultType.FINAL
        return Result.newBuilder()
                .setResultType(resultType)
                .addHypotheses(Hypothesis.newBuilder()
                                         .addWords(Word.newBuilder()
                                         .setText("hi")))
                .build();
    }

    private void setServiceBehaviour_normal(boolean simulateLongProcessing) {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                simulateLongProcessing);
    }

    @Test
    public void testStartSpeechRecognition() throws Exception {
        setServiceBehaviour_normal(false);

        var mockStatistics = mock(Command.CommandStatistics.class);
        var mockCommand = mock(Command.class);
        var mockSession = mock(Session.class);
        PowerMockito.mockStatic(SessionManager.class);
        var sessionManagerHolderClass = Class.forName("com.nuance.cobra.session.SessionManager$SessionManagerHolder");
        var mockSessionManager = mock(SessionManager.class);
        var originalSessionManager = TestUtils.inject(sessionManagerHolderClass, "instance", mockSessionManager);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));
        when(mockCommand.getStatistics()).thenReturn(mockStatistics);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        TestUtils.inject(sessionManagerHolderClass, "instance", originalSessionManager);

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_maskUserUtterance() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.SENSITIVITY.key, "masked");
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_resource_domainLm_enumWeight() {
        String contextTag = "context-tag";

        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, contextTag);
        config.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key, "high");
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals(String.format("urn:nuance-mix:tag:model/%s/mix.asr?=language=%s", contextTag, SPEECH_RECOGNITION_LANGUAGE), service.getResourceDomainLmUri());
        assertEquals(EnumWeight.HIGH, service.getResourceDomainLmWeight_enum());
    }

    @Test
    public void testStartSpeechRecognition_resource_speaker_Profile() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);

        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_USE_USERID.key, true);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
        assertTrue( service.getSpeakerProfileAdded());
    }

    @Test
    public void testStartSpeechRecognition_resource_domainLm_floatWeight() {
        String contextTag = "context-tag";

        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, contextTag);
        config.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key, 340);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals(String.format("urn:nuance-mix:tag:model/%s/mix.asr?=language=%s", contextTag, SPEECH_RECOGNITION_LANGUAGE), service.getResourceDomainLmUri());
        assertEquals(0.340, service.getResourceDomainLmWeight_float(),0.0000001);
    }

    @Test
    public void testStartSpeechRecognition_resource_domainLm_invalidWeight() {
        String contextTag = "context-tag";

        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, contextTag);
        config.setProperty(WebApiConfig.Property.KRYPTON_DOMAIN_LM_WEIGHT.key, 1500);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyError(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_DOMAIN_LM_WEIGHT);
    }

    @Test
    public void testStartSpeechRecognition_resource_domainLm_dynamicVocabularies() {
        String contextTag = "context-tag";

        List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
        DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_DOMAINLM, "urn:nuance-mix:tag:model/"+contextTag+"/mix.asr?=language=eng-USA", null, "lowest");
        dynamicVocabularies.add(dynamicVocabulary);

        CombinedConfiguration configs = buildCobraConfig();
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, dynamicVocabularies, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals(String.format("urn:nuance-mix:tag:model/%s/mix.asr?=language=eng-USA", contextTag), service.getResourceDomainLmUri());
        assertEquals(EnumWeight.LOWEST, service.getResourceDomainLmWeight_enum());
    }

    @Test
    public void testStartSpeechRecognition_resource_inlineWordset() {
        DynamicVocabulary.Vocabulary vocab = new DynamicVocabulary.Vocabulary();
        vocab.setLiteral("literal");
        vocab.addSpoken("spoken");
        ArrayList<DynamicVocabulary.Vocabulary> vocabs = new ArrayList<>();
        vocabs.add(vocab);
        Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body = new HashMap<>();
        body.put("key", vocabs);
        List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
        dynamicVocabularies.add(new DynamicVocabulary("id", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, body));

        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(buildCobraConfig(), dynamicVocabularies, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals("{\"key\":[{\"literal\":\"literal\",\"spoken\":[\"spoken\"]}]}", service.getResourceInlineWordset());
    }

    @Test
    public void testStartSpeechRecognition_resource_builtins_enum() {
        List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
        DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN, null, "TEMPERATURE", "lowest");
        dynamicVocabularies.add(dynamicVocabulary);

        CombinedConfiguration configs = buildCobraConfig();
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(buildCobraConfig(), dynamicVocabularies, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals("TEMPERATURE", service.getResourceBuiltin());
        assertEquals("LOWEST", service.getResourceWeight_enum().toString());
    }

    @Test
    public void testStartSpeechRecognition_resource_builtins_float() {
        List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
        DynamicVocabulary dynamicVocabulary = new DynamicVocabulary("test123", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_BUILTIN, null, "TEMPERATURE", 0.05);
        dynamicVocabularies.add(dynamicVocabulary);

        CombinedConfiguration configs = buildCobraConfig();
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(buildCobraConfig(), dynamicVocabularies, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        assertEquals("TEMPERATURE", service.getResourceBuiltin());
        assertEquals(0.05F, service.getResourceWeight_float(), 0.00001);
    }

    @Test
    public void testStartSpeechRecognition_max_Hypotheses() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.addProperty(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key, 5);

        client = new KryptonGrpcClient(client.config, "", new KryptonGrpcRequestParams(configs, new ArrayList<>(), mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_max_Hypotheses_invalid() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.addProperty(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key, 12);

        client = new KryptonGrpcClient(client.config, "", new KryptonGrpcRequestParams(configs, new ArrayList<>(), mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_speechDomain() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.addProperty(WebApiConfig.Property.SPEECH_DOMAIN.key, "travel");

        client = new KryptonGrpcClient(client.config, "", new KryptonGrpcRequestParams(configs, new ArrayList<>(), mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }


    @Test
    public void testStartSpeechRecognition_formattingType() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.addProperty(WebApiConfig.Property.SPEECH_RECOGNITION_FORMATTING_TYPE.key, "all_as_words");

        client = new KryptonGrpcClient(client.config, "", new KryptonGrpcRequestParams(configs, new ArrayList<>(), mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        setServiceBehaviour_normal(false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_rejectedHypothesis_endpointingActivated_startOfSpeechDetected() {
        Result finalResult_rejected = Result.newBuilder()
                .setResultType(FINAL)
                .addHypotheses(Hypothesis.newBuilder()
                        .setRejected(true))
                .build();

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                finalResult_rejected,
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_rejectedHypothesis_endpointingActivated_butStartOfSpeechNotDetected() {
        Result finalResult_rejected = Result.newBuilder()
                .setResultType(FINAL)
                .addHypotheses(Hypothesis.newBuilder()
                        .setRejected(true))
                .build();

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                finalResult_rejected,
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_rejectedHypothesis_endpointingDeactivated_startOfSpeechNotDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        Result finalResult_rejected = Result.newBuilder()
                .setResultType(FINAL)
                .addHypotheses(Hypothesis.newBuilder()
                        .setRejected(true))
                .build();

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                finalResult_rejected,
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_rejectedHypothesis_endpointingDeactivated_butStartOfSpeechDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        Result finalResult_rejected = Result.newBuilder()
                .setResultType(FINAL)
                .addHypotheses(Hypothesis.newBuilder()
                        .setRejected(true))
                .build();

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                finalResult_rejected,
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_1intermediateResult() {
        List<Result> intermediateResults = new ArrayList<>();
        intermediateResults.add(buildResult_normal(PARTIAL));

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                intermediateResults,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);

        verify(mockResponseHandler, times(2)).onResponse(captor.capture());
        verify(mockErrorHandler, never()).handleError(any());

        List<SpeechRecoResponse> responses = captor.getAllValues();
        SpeechRecoResponse intermediateResponse = responses.get(0);
        SpeechRecoResponse finalResponse = responses.get(1);

        assertFalse(intermediateResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, intermediateResponse.getStatus());

        assertTrue(finalResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, finalResponse.getStatus());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testStartSpeechRecognition_1intermediateResult_butNoStartOfSpeech() {
        List<Result> intermediateResults = new ArrayList<>();
        intermediateResults.add(buildResult_normal(PARTIAL));

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                intermediateResults,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);

        verify(mockResponseHandler, times(1)).onResponse(captor.capture());
        verify(mockErrorHandler, never()).handleError(any());

        List<SpeechRecoResponse> responses = captor.getAllValues();
        SpeechRecoResponse finalResponse = responses.get(0);

        assertTrue(finalResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, finalResponse.getStatus());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testStartSpeechRecognition_1intermediateResult_butNoStartOfSpeech_butNoEndpointDetection() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        List<Result> intermediateResults = new ArrayList<>();
        intermediateResults.add(buildResult_normal(PARTIAL));

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                intermediateResults,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        // the same behaviour as testStartSpeechRecognition_1intermediateResult is expected

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);

        verify(mockResponseHandler, times(2)).onResponse(captor.capture());
        verify(mockErrorHandler, never()).handleError(any());

        List<SpeechRecoResponse> responses = captor.getAllValues();
        SpeechRecoResponse intermediateResponse = responses.get(0);
        SpeechRecoResponse finalResponse = responses.get(1);

        assertFalse(intermediateResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, intermediateResponse.getStatus());

        assertTrue(finalResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, finalResponse.getStatus());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testStartSpeechRecognition_2intermediateResults() {
        List<Result> intermediateResults = new ArrayList<>();
        intermediateResults.add(buildResult_normal(PARTIAL));
        intermediateResults.add(buildResult_normal(PARTIAL));

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                intermediateResults,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);

        verify(mockResponseHandler, times(3)).onResponse(captor.capture());
        verify(mockErrorHandler, never()).handleError(any());

        List<SpeechRecoResponse> responses = captor.getAllValues();
        SpeechRecoResponse intermediateResponse1 = responses.get(0);
        SpeechRecoResponse intermediateResponse2 = responses.get(1);
        SpeechRecoResponse finalResponse = responses.get(2);

        assertFalse(intermediateResponse1.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, intermediateResponse1.getStatus());

        assertFalse(intermediateResponse2.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, intermediateResponse2.getStatus());

        assertTrue(finalResponse.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, finalResponse.getStatus());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testStartSpeechRecognition_audioChunkBuffering() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.MANUAL,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        byte[] audio = {1, 2, 3};

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, audio));
//        service.returnStatus100(); // in this test, we send audio before we get status code 100

        // make sure audio was sent to the engine before receiving status code 100
        byte[] receivedAudio = service.getAudio();
        assertArrayEquals(receivedAudio, audio);

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_finish() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.finish();

        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        //assertFalse(channel.isShutdown());
    }

    @Test
    public void testStartSpeechRecognition_finish_stop() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.finish();
        client.stopSpeechRecognition();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_finish_cancel() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.finish();
        client.cancelSpeechRecognition(true);

        verifyResponse(ResponseResult.ResponseStatus.CANCELLED);
    }

    @Test
    public void testStartSpeechRecognition_stop() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.stopSpeechRecognition();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_stop_beforeGettingStatus100() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.MANUAL,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.stopSpeechRecognition();
//        service.returnStatus100();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_stop_finish() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.stopSpeechRecognition();
        client.finish();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechRecognition_cancel() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.cancelSpeechRecognition(true);

        verifyResponse(ResponseResult.ResponseStatus.CANCELLED);
    }

    @Test
    public void testStartSpeechRecognition_cancel_finish() {
        setServiceBehaviour_normal(true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.cancelSpeechRecognition(true);
        client.finish();

        verifyResponse(ResponseResult.ResponseStatus.CANCELLED);
    }

    @Test
    public void testStartSpeechRecognition_clientBug() {
        setServiceBehaviour_normal(false);
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        client = new KryptonGrpcClient(configs, "", null, buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener()); // params = null (this is the client fake bug)

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_EXECUTE_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_serverBug_noFinalResult() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null, // <--- this is the server fake bug
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verify(mockResponseHandler, never()).onAudio(any(), anyBoolean());
        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        // A warning will be logged if this ever happens. Then the request will time out.
    }

    @Test
    public void testStartSpeechRecognition_serverOnError() {
        service.setBehavior(true,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                0,
                "",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_204_noResult_endpointingActivated_startOfSpeechDetected() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                204,
                "No result",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_204_noResult_endpointingActivated_butStartOfSpeechNotDetected() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                204,
                "No result",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_204_noResult_endpointingDeactivated_startOfSpeechNotDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                204,
                "No result",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_204_noResult_endpointingDeactivated_butStartOfSpeechDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                204,
                "No result",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_400_badRequest() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                400,
                "Bad request",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_400_artifactNotFound() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                400,
                "Bad request",
                "no artifact for URN",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_NOT_FOUND_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_400_invalidLanguage() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                400,
                "Bad request",
                "invalid language requested",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_LANGUAGE_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_404_noSpeech_endpointingActivated_startOfSpeechDetected() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                404,
                "No speech",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_MATCH);
    }

    @Test
    public void testStartSpeechRecognition_404_noSpeech_endpointingActivated_butStartOfSpeechNotDetected() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                404,
                "No speech",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_404_noSpeech_endpointingDeactivated_startOfSpeechNotDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                404,
                "No speech",
                false);

        client.startSpeechRecognition();
        // start of speech not detected
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_404_noSpeech_endpointingDeactivated_butStartOfSpeechDetected() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                404,
                "No speech",
                false);

        client.startSpeechRecognition();
        client.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_408_audioTimeout() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                408,
                "Audio timeout",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.NO_INPUT);
    }

    @Test
    public void testStartSpeechRecognition_409_conflict() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                409,
                "Conflict",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_410_notRecognizing() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                410,
                "Not recognizing",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_413_tooMuchSpeech() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                null,
                413,
                "Too much speech",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_500_internalServerError() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                500,
                "Internal server error",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_502_resourceError() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                502,
                "Resource error",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_502);
    }

    @Test
    public void testStartSpeechRecognition_503_serviceUnavailable() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.NEVER,
                null,
                null,
                503,
                "Service unavailable",
                false);

        client.startSpeechRecognition();

        verifyError(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechRecognition_unsupportedResponseCode_afterReceivingFinalResult() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                123,
                "Unsupported status",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        // Despite receiving an unsupported response code, a valid final result was still received, resulting in a success response being sent to the client.
    }

    private void verifyResponse(ResponseResult.ResponseStatus responseStatus) {
        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        SpeechRecoResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(responseStatus, response.getStatus());

        //assertTrue(channel.isShutdown());
    }

    private void verifyError(ExternalResponseErrorCode errorCode) {
        verify(mockResponseHandler, never()).onAudio(any(), anyBoolean());
        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, times(1)).handleError(any());

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(mockErrorHandler).handleError(captor.capture());
        SpeechRecoResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, response.getStatus());

        List<ResponseCode> errors = response.getErrors();
        assertEquals(1, errors.size());

        ResponseCode error = errors.get(0);
        ResponseCode expectedError = ResponseCode.createResponseError(errorCode);

        assertEquals(expectedError, error);

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testOnEvent_newNoiseLevelCalculated() {
        SpeechDetector mockSpeechDetector = mock(SpeechDetector.class);
        when(mockSpeechDetector.getCalculatedBackgroundNoiseLevel()).thenReturn(new NoiseLevel(1, 2));

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onEvent(mockSpeechDetector, AudioNodeObserver.NodeEvent.NEW_NOISE_LEVEL_CALCULATED);
        client.onAudio(new AudioChunk(0, new byte[0]));

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        SpeechRecoResponse response = captor.getValue();
        NoiseLevel noiseLevel = response.getNoiseLevel();

        assertEquals(1, noiseLevel.getMean(), 0);
        assertEquals(2, noiseLevel.getStandardDeviation(), 0);

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testOnEvent_stopOnEndOfSpeech_on() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key, true);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.onEvent(null, AudioNodeObserver.NodeEvent.END_OF_SPEECH);

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testOnEvent_stopOnEndOfSpeech_off() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));
        client.onEvent(null, AudioNodeObserver.NodeEvent.END_OF_SPEECH);

        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        //assertFalse(channel.isShutdown());
    }

    @Test
    public void testOnEvent_timeout_SOS() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        TimeoutNode mockTimeoutNode = mock(TimeoutNode.class);
        when(mockTimeoutNode.getTimerID()).thenReturn(SpeechRecoClient.START_SPEECH_TIMER_ID);
        client.onEvent(mockTimeoutNode, AudioNodeObserver.NodeEvent.TIMEOUT);

        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testOnEvent_timeout_maxSpeech() {
        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                buildResult_normal(FINAL),
                200,
                "OK",
                true);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        TimeoutNode mockTimeoutNode = mock(TimeoutNode.class);
        when(mockTimeoutNode.getTimerID()).thenReturn(SpeechRecoClient.MAX_SPEECH_TIMER_ID);
        client.onEvent(mockTimeoutNode, AudioNodeObserver.NodeEvent.TIMEOUT);

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    public void testBuildRecoData_generic(String kryptonGrpcResult, String expectedCobraResult) throws TextFormat.ParseException, CobraException {
        Result.Builder resultBuilder = Result.newBuilder();
        TextFormat.getParser().merge(kryptonGrpcResult, resultBuilder);
        Result finalResult = resultBuilder.build();

        service.setBehavior(false,
                TestKryptonGrpcService.BehaviorReturnStatus100.AFTER_INIT_MESSAGE,
                null,
                finalResult,
                200,
                "OK",
                false);

        client.startSpeechRecognition();
        client.onAudio(new AudioChunk(0, new byte[0]));

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        SpeechRecoResponse response = captor.getValue();

        assertEquals(expectedCobraResult, response.toJson());
    }

    @Test
    public void testBuildRecoData() throws TextFormat.ParseException, CobraException {
        String kryptonGrpcResult =
                "abs_end_ms: 2720\n" +
                "utterance_info {\n" +
                "  duration_ms: 2720\n" +
                "  dsp {\n" +
                "    snr_estimate_db: 33.0\n" +
                "    level: 16385.0\n" +
                "    num_channels: 1\n" +
                "    initial_silence_ms: 20\n" +
                "    initial_energy: -78.2016\n" +
                "    final_energy: -31.902\n" +
                "    mean_energy: 99.4649\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  confidence: 0.164\n" +
                "  average_confidence: 0.568\n" +
                "  formatted_text: \"This is a unit test utterance\"\n" +
                "  minimally_formatted_text: \"This is a unit test utterance\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.917\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.886\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"unit\"\n" +
                "    confidence: 0.547\n" +
                "    start_ms: 400\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.767\n" +
                "    start_ms: 860\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"utterance\"\n" +
                "    confidence: 0.161\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1840\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.663\n" +
                "  formatted_text: \"This is a test utterance\"\n" +
                "  minimally_formatted_text: \"This is a test utterance\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.896\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.861\n" +
                "    start_ms: 260\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 340\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.838\n" +
                "    start_ms: 760\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"utterance\"\n" +
                "    confidence: 0.158\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1840\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.501\n" +
                "  formatted_text: \"This is a unit test Arends\"\n" +
                "  minimally_formatted_text: \"This is a unit test Arends\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.917\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.886\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"unit\"\n" +
                "    confidence: 0.507\n" +
                "    start_ms: 400\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.707\n" +
                "    start_ms: 860\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"Arends\"\n" +
                "    confidence: 0.053\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1880\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.619\n" +
                "  formatted_text: \"This is a unit test around\"\n" +
                "  minimally_formatted_text: \"This is a unit test around\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.917\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.886\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"unit\"\n" +
                "    confidence: 0.573\n" +
                "    start_ms: 400\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.816\n" +
                "    start_ms: 860\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"around\"\n" +
                "    confidence: 0.261\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1820\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.363\n" +
                "  formatted_text: \"This is a unit tests utterance\"\n" +
                "  minimally_formatted_text: \"This is a unit tests utterance\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.917\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.833\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"unit\"\n" +
                "    confidence: 0.377\n" +
                "    start_ms: 400\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"tests\"\n" +
                "    confidence: 0.099\n" +
                "    start_ms: 860\n" +
                "    end_ms: 1220\n" +
                "    silence_after_word_ms: 160\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"utterance\"\n" +
                "    confidence: 0.129\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1840\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.287\n" +
                "  formatted_text: \"This is a euro to Tess Arends\"\n" +
                "  minimally_formatted_text: \"This is a euro to Tess Arends\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.949\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.896\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.77\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"euro\"\n" +
                "    confidence: 0.11\n" +
                "    start_ms: 400\n" +
                "    end_ms: 660\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"to\"\n" +
                "    confidence: 0.111\n" +
                "    start_ms: 660\n" +
                "    end_ms: 740\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"Tess\"\n" +
                "    confidence: 0.105\n" +
                "    start_ms: 740\n" +
                "    end_ms: 1060\n" +
                "    silence_after_word_ms: 320\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"Arends\"\n" +
                "    confidence: 0.097\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1880\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.46\n" +
                "  formatted_text: \"This is a euro test utterance\"\n" +
                "  minimally_formatted_text: \"This is a euro test utterance\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.952\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.902\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"a\"\n" +
                "    confidence: 0.863\n" +
                "    start_ms: 260\n" +
                "    end_ms: 400\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"euro\"\n" +
                "    confidence: 0.167\n" +
                "    start_ms: 400\n" +
                "    end_ms: 740\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.639\n" +
                "    start_ms: 740\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"utterance\"\n" +
                "    confidence: 0.143\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1840\n" +
                "  }\n" +
                "}\n" +
                "hypotheses {\n" +
                "  average_confidence: 0.457\n" +
                "  formatted_text: \"This is the unit test utterance\"\n" +
                "  minimally_formatted_text: \"This is the unit test utterance\"\n" +
                "  words {\n" +
                "    text: \"This\"\n" +
                "    confidence: 0.929\n" +
                "    start_ms: 20\n" +
                "    end_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"is\"\n" +
                "    confidence: 0.857\n" +
                "    start_ms: 100\n" +
                "    end_ms: 260\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"the\"\n" +
                "    confidence: 0.241\n" +
                "    start_ms: 260\n" +
                "    end_ms: 360\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"unit\"\n" +
                "    confidence: 0.393\n" +
                "    start_ms: 360\n" +
                "    end_ms: 760\n" +
                "    silence_after_word_ms: 100\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"test\"\n" +
                "    confidence: 0.718\n" +
                "    start_ms: 860\n" +
                "    end_ms: 1200\n" +
                "    silence_after_word_ms: 180\n" +
                "  }\n" +
                "  words {\n" +
                "    text: \"utterance\"\n" +
                "    confidence: 0.153\n" +
                "    start_ms: 1380\n" +
                "    end_ms: 1840\n" +
                "  }\n" +
                "}\n";

        String expectedCobraResult = "{\"final\":true,\"status\":\"success\",\"data\":{\"transcriptions\":[\"This is a unit test utterance\",\"This is a test utterance\",\"This is a unit test Arends\",\"This is a unit test around\",\"This is a unit tests utterance\",\"This is a euro to Tess Arends\",\"This is a euro test utterance\",\"This is the unit test utterance\"],\"confidence\":164,\"averageWeightedConfidences\":[568,663,501,619,363,287,460,457],\"words\":[[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":917.0,\"word\":\"is\"},{\"confidence\":886.0,\"word\":\"a\"},{\"confidence\":547.0,\"word\":\"unit\"},{\"confidence\":767.0,\"word\":\"test\"},{\"confidence\":161.0,\"word\":\"utterance\"}],[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":896.0,\"word\":\"is\"},{\"confidence\":861.0,\"word\":\"a\"},{\"confidence\":838.0,\"word\":\"test\"},{\"confidence\":158.0,\"word\":\"utterance\"}],[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":917.0,\"word\":\"is\"},{\"confidence\":886.0,\"word\":\"a\"},{\"confidence\":507.0,\"word\":\"unit\"},{\"confidence\":707.0,\"word\":\"test\"},{\"confidence\":53.0,\"word\":\"Arends\"}],[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":917.0,\"word\":\"is\"},{\"confidence\":886.0,\"word\":\"a\"},{\"confidence\":573.0,\"word\":\"unit\"},{\"confidence\":816.0,\"word\":\"test\"},{\"confidence\":261.0,\"word\":\"around\"}],[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":917.0,\"word\":\"is\"},{\"confidence\":833.0,\"word\":\"a\"},{\"confidence\":377.0,\"word\":\"unit\"},{\"confidence\":99.0,\"word\":\"tests\"},{\"confidence\":129.0,\"word\":\"utterance\"}],[{\"confidence\":949.0,\"word\":\"This\"},{\"confidence\":896.0,\"word\":\"is\"},{\"confidence\":770.0,\"word\":\"a\"},{\"confidence\":110.0,\"word\":\"euro\"},{\"confidence\":111.0,\"word\":\"to\"},{\"confidence\":105.0,\"word\":\"Tess\"},{\"confidence\":97.0,\"word\":\"Arends\"}],[{\"confidence\":952.0,\"word\":\"This\"},{\"confidence\":902.0,\"word\":\"is\"},{\"confidence\":863.0,\"word\":\"a\"},{\"confidence\":167.0,\"word\":\"euro\"},{\"confidence\":639.0,\"word\":\"test\"},{\"confidence\":143.0,\"word\":\"utterance\"}],[{\"confidence\":929.0,\"word\":\"This\"},{\"confidence\":857.0,\"word\":\"is\"},{\"confidence\":241.0,\"word\":\"the\"},{\"confidence\":393.0,\"word\":\"unit\"},{\"confidence\":718.0,\"word\":\"test\"},{\"confidence\":153.0,\"word\":\"utterance\"}]]},\"bestResult\":\"This is a unit test utterance\",\"noiseLevel\":{\"mean\":0.0,\"standardDeviation\":0.0}}";

        testBuildRecoData_generic(kryptonGrpcResult, expectedCobraResult);
    }

    @Test
    public void testSendResponseNotifications() {
        JSONObject metrics = new JSONObject();
        metrics.put("weights", new JSONObject("{\"1\":5}"));
        metrics.put("array", new JSONArray("[\"2\"]"));
        metrics.put("wordsets", new JSONArray("[\"2\"]"));
        List<Map<String, Object>> notifications = new ArrayList<>();
        notifications.add(Map.of("code", 1001));
        notifications.add(Map.of("code", 1004, "data", Map.of("x-nuance-supplemental-metrics-result", metrics.toString())));
        Map<String, Object> verboseResult = new HashMap<>();
        verboseResult.put("notifications", notifications);
        SpeechRecoResponse response = mock(SpeechRecoResponse.class);
        when(response.isFinal()).thenReturn(true);
        when(response.getVerboseResult()).thenReturn(verboseResult);
        KryptonGrpcRequestParams mockParams = mock(KryptonGrpcRequestParams.class);
        mockParams.resourceIndexToIdMap = Map.of("1", "resource1", "2", "resource2");
        client = new KryptonGrpcClient(buildCobraConfig(), "", mockParams, buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        client.sendResponse(response);
        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(mockResponseHandler, times(1)).onResponse(captor.capture());
        SpeechRecoResponse actualResponse = captor.getValue();
        assertNotNull(actualResponse);
        assertEquals("{notifications=[{code=1001}, {code=1004, data={x-nuance-supplemental-metrics-result={\"wordsets\":[\"resource2\"],\"array\":[\"2\"],\"weights\":{\"resource1\":5}}}}]}", actualResponse.getVerboseResult().toString());
    }

    @Test
    public void testThwartNoAudioTimers() throws Exception
    {
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);
        config.setProperty(WebApiConfig.Property.KRYPTON_GRPC_THWART_NO_AUDIO_TIMER_SECONDS.key, 1);

        setServiceBehaviour_normal(false);

        var mockStatistics = mock(Command.CommandStatistics.class);
        var mockCommand = mock(Command.class);
        var mockSession = mock(Session.class);
        PowerMockito.mockStatic(SessionManager.class);
        var sessionManagerHolderClass = Class.forName("com.nuance.cobra.session.SessionManager$SessionManagerHolder");
        var mockSessionManager = mock(SessionManager.class);
        var originalSessionManager = TestUtils.inject(sessionManagerHolderClass, "instance", mockSessionManager);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));
        when(mockCommand.getStatistics()).thenReturn(mockStatistics);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);

        client.startSpeechRecognition();
        client.startRecognitionTimers();
        Thread.sleep(2000);
        byte[] audio = new byte[2];
        audio[0] = (byte) 0xFF;
        audio[1] = (byte) 0xFF;
        client.onAudio(new AudioChunk(0, audio));

        TestUtils.inject(sessionManagerHolderClass, "instance", originalSessionManager);

        assertNotNull(client.getLanguage());

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testOnEngineReady() {
        setServiceBehaviour_normal(true);

        client.queueStopSpeechRecognition = true;
        client.startSpeechRecognition();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testEngineSpeechDetector() {
        CombinedConfiguration configs = buildCobraConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, SpeechDetectorTypes.ENGINE.getValue());
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, true);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        RecognitionParameters.Builder builder = client.buildRequestInitParams();

        assertEquals(EnumUtteranceDetectionMode.SINGLE, builder.getUtteranceDetectionMode());

        configs = buildCobraConfig();
        config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, SpeechDetectorTypes.ENGINE.getValue());
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        builder = client.buildRequestInitParams();

        assertEquals(EnumUtteranceDetectionMode.DISABLED, builder.getUtteranceDetectionMode());

        configs = buildCobraConfig();
        config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, SpeechDetectorTypes.ADAPTIVE.getValue());
        config.setProperty(WebApiConfig.Property.END_POINT_DETECTION.key, true);
        client = new KryptonGrpcClient(configs, "", new KryptonGrpcRequestParams(configs, mock(Logger.class)), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        builder = client.buildRequestInitParams();

        assertEquals(EnumUtteranceDetectionMode.DISABLED, builder.getUtteranceDetectionMode());
    }

    @Test
    public void testSetObservers() {
        List<AudioNodeObserver> observers = new ArrayList<>();
        client.setObservers(observers);

        assertEquals(observers, client.observers);
    }
}
