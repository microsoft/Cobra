package com.nuance.cobra.client.mix.nlu;

import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.nlu.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NluAasGrpcClientTest
        extends CobraTest {
    @ClassRule
    public static GrpcCleanupRule cleanupRule = new GrpcCleanupRule();

    private static String serverName = InProcessServerBuilder.generateName();
    private static TestNluAasGrpcService service = new TestNluAasGrpcService();
    private static ManagedChannel channel;

    private ResponseHandler mockResponseHandler;
    private ResponseErrorHandler mockErrorHandler;
    private NluAasGrpcClient client;

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

        service.resetMocks();
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.OK_200);

        mockResponseHandler = mock(ResponseHandler.class);
        mockErrorHandler = mock(ResponseErrorHandler.class);
        client = new NluAasGrpcClient(new NluAasGrpcParams("test-session-id", buildCobraConfig()), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);

        Command mockCommand = Mockito.mock(Command.class);
        Mockito.when(mockSession.getCommand()).thenReturn(mockCommand);
        Mockito.when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));

        Command.CommandStatistics mockCommandStatistics = Mockito.mock(Command.CommandStatistics.class);
        Mockito.when(mockCommand.getStatistics()).thenReturn(mockCommandStatistics);
    }

    private static RuntimeGrpc.RuntimeStub buildStub() {
        // gRPC channel (client<->server)
        // we build a new channel each time because they are shut down after each request
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor() // directExecutor is fine for unit tests
                .build();

        // gRPC stub
        return RuntimeGrpc.newStub(channel);
    }

    @After
    public void runAfterEachTests() throws InterruptedException {
        if(channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
            while(!channel.awaitTermination(2, TimeUnit.SECONDS)) {
                Thread.sleep(500);
            }
        }
    }

    private static CombinedConfiguration buildCobraConfig() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "This is a test.");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "test-semantic-model-id");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");

        return configs;
    }

    @Test
    public void testInterpretation_200_OK() {
        client.startInterpretation();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertEquals(37, response.getInterpretations()[0].getConfidence());
        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testInterpretation_dynamicVocabulary() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "This is a test.");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "test-semantic-model-id");
        config.setProperty(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key, "eng-USA");

        // prepare dyn voc - begin
        DynamicVocabulary.Vocabulary voc = new DynamicVocabulary.Vocabulary();
        voc.setLiteral("jf");
        voc.addSpoken("J F");
        ArrayList<DynamicVocabulary.Vocabulary> vocList = new ArrayList<>();
        vocList.add(voc);

        Map<String, ArrayList<DynamicVocabulary.Vocabulary>> dynVocBody = new HashMap<>();
        dynVocBody.put("SUPERHERO", vocList);

        DynamicVocabulary dynVoc = new DynamicVocabulary("test", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, dynVocBody);
        ArrayList<DynamicVocabulary> dynVocList = new ArrayList<>();
        dynVocList.add(dynVoc);
        // prepare dyn voc - end

        client = new NluAasGrpcClient(new NluAasGrpcParams("test-session-id", configs, dynVocList), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        client.startInterpretation();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());

        List<String> inlineWordsets = service.getInlineWordsets();
        assertEquals(1, inlineWordsets.size());
        assertEquals("{\"SUPERHERO\":[{\"literal\":\"jf\",\"spoken\":[\"J F\"]}]}", inlineWordsets.get(0));

        //assertTrue(channel.isShutdown());
    }

    @Test
    public void testInterpretation_maxInterpretations() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.NLUAAS_GRPC_MAX_INTERPRETATIONS.key, 3);
        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "This is a test.");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "test-semantic-model-id");
        config.setProperty(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key, "eng-USA");

        client = new NluAasGrpcClient(new NluAasGrpcParams("test-session-id", configs), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        client.startInterpretation();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());

        int maxInterpretations = service.getMaxInterpretations();
        assertEquals(3, maxInterpretations);
    }

    @Test
    public void testInterpretation_500_internalServerError() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.INTERNAL_SERVER_ERROR_500);
        client.startInterpretation();

        verifyError();
    }

    @Test
    public void testInterpretation_onError() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.ON_ERROR);
        client.startInterpretation();

        verifyError();
    }

    @Test
    public void testInterpretation_onError_resourceNotFound() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.ON_ERROR);
        service.setError(io.grpc.Status.fromCode(io.grpc.Status.Code.INVALID_ARGUMENT)
                .withDescription("Request rejected: Resolver returned status code 404 resolving"));

        client.startInterpretation();

        InterpretationResponse response = verifyError();

        assertEquals(1, response.getErrors().size());
        assertEquals(1814, response.getErrors().get(0).getCode());
    }

    @Test
    public void testInterpretation_onStop() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.DO_NOTHING);
        client.startInterpretation();
        client.stopInterpretation();

        service.sendOnNext(null);
        service.sendOnCompleted();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());
    }

    @Test
    public void testInterpretation_onCancel() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.DO_NOTHING);
        client.startInterpretation();
        client.cancelInterpretation(true);

        service.setError(io.grpc.Status.fromCode(Status.Code.CANCELLED)
                .withDescription("Canceled"));

        service.sendOnError();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.CANCELLED, response.getStatus());
    }

    @Test
    public void testInterpretation_invalidCommand_semanticModelNotSpecified() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "This is a test.");
//        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION, "test-semantic-model-id");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");
        client = new NluAasGrpcClient(new NluAasGrpcParams("test-session-id", configs), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        client.startInterpretation();

        verifyError();
    }

    private InterpretationResponse verifyError() {
        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, times(1)).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockErrorHandler).handleError(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, response.getStatus());

        //assertTrue(channel.isShutdown());

        return response;
    }

    @Test
    public void testInterpretation_verboseResult() {
        service.setBehavior(TestNluAasGrpcService.BehaviorReturnStatus.DO_NOTHING);
        CombinedConfiguration config = buildCobraConfig();
        config.setProperty(WebApiConfig.Property.INTERPRETATION_VERBOSE_RESULT.key, true);
        client = new NluAasGrpcClient(new NluAasGrpcParams("test-session-id", config), buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        client.startInterpretation();

        service.sendOnNext(InterpretResponse.newBuilder().putMetadata("meta1", Any.pack(StringValue.newBuilder().setValue("value1").build())).setStatus(com.nuance.rpc.nlu.v1.Status.newBuilder().setCode(200).build()).setResult(InterpretResult.newBuilder().build()).build());
        service.sendOnCompleted();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<InterpretationResponse> captor = ArgumentCaptor.forClass(InterpretationResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        InterpretationResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());
    }

    @Test
    public void testParseEntities() throws InvalidProtocolBufferException {
        var entitiesListBuilder = SingleIntentEntityList.newBuilder();
        var entity = SingleIntentEntity.newBuilder();
        Map<String, Value> structValueMap = new HashMap<>();
        ListValue.Builder arrayVal = ListValue.newBuilder();
        arrayVal.addValues(Value.newBuilder().setBoolValue(false).build());
        arrayVal.addValues(Value.newBuilder().setNumberValue(5).build());
        arrayVal.addValues(Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build());

        Map<String, Value> subStructValueMap = new HashMap<>();
        subStructValueMap.put("value", Value.newBuilder().setStringValue("false").build());
        arrayVal.addValues(Value.newBuilder().setStructValue(Struct.newBuilder().putAllFields(subStructValueMap).build()).build());

        structValueMap.put("array", Value.newBuilder().setListValue(arrayVal.build()).build());
        entity.setStructValue(Struct.newBuilder().putAllFields(structValueMap).build());

        var subEntity = SingleIntentEntity.newBuilder();

        entity.putEntities("subEntity", SingleIntentEntityList.newBuilder().addEntities(subEntity.build()).build());
        entitiesListBuilder.addEntities(entity.build());

        var result = client.parseEntities(false, "entityName", entitiesListBuilder.build());

        assertEquals(1, result.size());
    }

    @Test
    public void testParseEntitiesEmptyList() {
        var entitiesForName = SingleIntentEntityList.newBuilder().build();
        var result = client.parseEntities(false, "entityName", entitiesForName);

        assertEquals(0, result.size());
    }
}

