package com.nuance.cobra.client.mix.nlu;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.nlu.v1.InterpretResponse;
import com.nuance.rpc.nlu.v1.InterpretResult;
import com.nuance.rpc.nlu.v1.RuntimeGrpc;
import com.nuance.rpc.nlu.v1.Status;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NluAasGrpcResponseObserverTest extends CobraTest {

    @ClassRule
    public static GrpcCleanupRule cleanupRule = new GrpcCleanupRule();

    private static String serverName = InProcessServerBuilder.generateName();
    private static TestNluAasGrpcService service = new TestNluAasGrpcService();
    private static ManagedChannel channel;

    private NluAasGrpcClient client;
    private ResponseHandler mockResponseHandler;
    private ResponseErrorHandler mockErrorHandler;

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

    private static CombinedConfiguration buildCobraConfig() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "This is a test.");
        config.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "test-semantic-model-id");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");
        config.setProperty(WebApiConfig.Property.INTERPRETATION_VERBOSE_RESULT.key, true);

        return configs;
    }

    @Test
    public void testInterpretation_formattedLiteral_ExceptionWithExternalResponse(){
        CombinedConfiguration config = buildCobraConfig();
        NluAasGrpcParams nluAasGrpcParamsMock = spy(new NluAasGrpcParams("test-session-id", config));
        client = new NluAasGrpcClient(nluAasGrpcParamsMock, buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        try {
            when(nluAasGrpcParamsMock.getInput()).thenThrow(ExceptionWithExternalResponseErrorCode.class);
        } catch (ExceptionWithExternalResponseErrorCode e) {
            e.printStackTrace();
        }

        NluAasGrpcResponseObserver observerMock = spy(new NluAasGrpcResponseObserver(client));
        InterpretResponse responseMock = InterpretResponse.newBuilder().setStatus(Status.newBuilder().setCode(200).build()).build();

        startCapturingLogOutput(Level.ERROR, ResponseObserver.class.getName());

        observerMock.onNext(responseMock);
        observerMock.onCompleted();

        String expectedErrorMessage = "Failed to retrieve INTERPRETATION_USE_FULLY_FORMATTED_LITERAL";
        String capturedOutput = this.getCapturedLogOutput();

        Assert.assertTrue(capturedOutput.contains(expectedErrorMessage));
    }

    @Test
    public void getSessionIdTest() {
        NluAasGrpcParams mockParams = new NluAasGrpcParams("testID", mock(CombinedConfiguration.class));
        RuntimeGrpc.RuntimeStub stub = RuntimeGrpc.newStub(mock(io.grpc.Channel.class));
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        ResponseErrorHandler mockErrorHandler = mock(ResponseErrorHandler.class);
        HeaderEventListener mockInterceptor = mock(HeaderEventListener.class);

        NluAasGrpcClient testClient = new NluAasGrpcClient(mockParams, stub, mockResponseHandler, mockErrorHandler, mockInterceptor);
        NluAasGrpcResponseObserver testObserver = new NluAasGrpcResponseObserver(testClient);

        assertEquals("testID", testObserver.getSessionId());
    }

    @Test
    public void testInterpretation_onCompleted() throws InvalidProtocolBufferException, CobraException {
        String grpcString = "{ \"literal\": \"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\", \"interpretations\": [{ \"singleIntentInterpretation\": { \"intent\": \"tv_search\", \"confidence\": 0.9449202, \"origin\": \"STATISTICAL\", \"entities\": { \"tv_air_date\": { \"entities\": [{ \"textRange\": { \"startIndex\": 30, \"endIndex\": 42 }, \"confidence\": 0.82713646, \"origin\": \"STATISTICAL\", \"structValue\": { \"nuance_CALENDARX\": { \"nuance_CALENDAR_RANGE\": { \"nuance_CALENDAR_RANGE_START\": { \"nuance_CALENDAR\": { \"nuance_DATE\": { \"nuance_DATE_REL\": { \"nuance_STEP\": \"day\", \"nuance_INCREMENT\": 1.0 } }, \"nuance_TIME\": { \"nuance_TIME_ABS\": { \"nuance_HOUR\": 5.0, \"nuance_AMPM\": \"am\", \"nuance_MODIFIER\": \"fuzzy\" } } } }, \"nuance_CALENDAR_RANGE_END\": { \"nuance_CALENDAR\": { \"nuance_DATE\": { \"nuance_DATE_REL\": { \"nuance_STEP\": \"day\", \"nuance_INCREMENT\": 1.0 } }, \"nuance_TIME\": { \"nuance_TIME_ABS\": { \"nuance_HOUR\": 11.0, \"nuance_MINUTE\": 59.0, \"nuance_AMPM\": \"am\", \"nuance_MODIFIER\": \"fuzzy\" } } } } } } }, \"literal\": \"demain matin\", \"formattedLiteral\": \"demain matin\", \"formattedTextRange\": { \"startIndex\": 30, \"endIndex\": 42 } }, { \"textRange\": { \"startIndex\": 51, \"endIndex\": 57 }, \"confidence\": 0.82601124, \"origin\": \"STATISTICAL\", \"stringValue\": \"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\", \"literal\": \"à 9h30\", \"formattedLiteral\": \"à 9h30\", \"formattedTextRange\": { \"startIndex\": 51, \"endIndex\": 57 } }] }, \"tv_query\": { \"entities\": [{ \"textRange\": { \"startIndex\": 20, \"endIndex\": 29 }, \"confidence\": 0.8014085, \"origin\": \"STATISTICAL\", \"stringValue\": \"Desperado\", \"literal\": \"Desperado\", \"formattedLiteral\": \"Desperado\", \"formattedTextRange\": { \"startIndex\": 20, \"endIndex\": 29 } }] }, \"tv_station\": { \"entities\": [{ \"textRange\": { \"startIndex\": 47, \"endIndex\": 50 }, \"confidence\": 0.5830483, \"origin\": \"STATISTICAL\", \"stringValue\": \"tf1\", \"literal\": \"TF1\", \"formattedLiteral\": \"TF1\", \"formattedTextRange\": { \"startIndex\": 47, \"endIndex\": 50 } }] }, \"tv_service\": { \"entities\": [{ \"textRange\": { \"startIndex\": 8, \"endIndex\": 19 }, \"confidence\": 0.60814315, \"origin\": \"STATISTICAL\", \"stringValue\": \"record\", \"literal\": \"enregistrer\", \"formattedLiteral\": \"enregistrer\", \"formattedTextRange\": { \"startIndex\": 8, \"endIndex\": 19 } }] } } } }], \"formattedLiteral\": \"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\"}";

        CombinedConfiguration config = buildCobraConfig();
        NluAasGrpcParams nluAasGrpcParamsMock = spy(new NluAasGrpcParams("test-session-id", config));
        client = new NluAasGrpcClient(nluAasGrpcParamsMock, buildStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        try {
            when(nluAasGrpcParamsMock.getInput()).thenThrow(ExceptionWithExternalResponseErrorCode.class);
        } catch (ExceptionWithExternalResponseErrorCode e) {
            e.printStackTrace();
        }

        NluAasGrpcResponseObserver observerMock = spy(new NluAasGrpcResponseObserver(client));
        InterpretResult.Builder builder = InterpretResult.newBuilder();
        JsonFormat.parser().merge(grpcString, builder);
        InterpretResult resultMock = builder.build();
        InterpretResponse responseMock = InterpretResponse.newBuilder().setResult(resultMock).setStatus(Status.newBuilder().setCode(200).build()).build();

        observerMock.onNext(responseMock);
        observerMock.onCompleted();

        ArgumentCaptor<ResponseResult> argumentCaptor = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockResponseHandler, times(1)).onResponse(argumentCaptor.capture());
        assertEquals("{\"final\":true,\"status\":\"success\",\"interpretations\":[{\"concepts\":[{\"concepts\":[],\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concepts\":[],\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concepts\":[],\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concepts\":[],\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concepts\":[],\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"}],\"bestInterpretation\":{\"concepts\":[{\"concepts\":[],\"concept\":\"tv_air_date\",\"value\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":11.0}}}},\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\",\"nuance_HOUR\":5.0}}}}}}},\"literal\":\"demain matin\",\"begin\":30,\"end\":42},{\"concepts\":[],\"concept\":\"tv_air_date\",\"value\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"begin\":51,\"end\":57},{\"concepts\":[],\"concept\":\"tv_query\",\"value\":\"Desperado\",\"literal\":\"Desperado\",\"begin\":20,\"end\":29},{\"concepts\":[],\"concept\":\"tv_station\",\"value\":\"tf1\",\"literal\":\"TF1\",\"begin\":47,\"end\":50},{\"concepts\":[],\"concept\":\"tv_service\",\"value\":\"record\",\"literal\":\"enregistrer\",\"begin\":8,\"end\":19}],\"confidence\":94,\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"intent\":\"tv_search\",\"origin\":\"STATISTICAL\"},\"verboseResult\":{\"literal\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\",\"interpretations\":[{\"singleIntentInterpretation\":{\"intent\":\"tv_search\",\"confidence\":0.9449202,\"origin\":\"STATISTICAL\",\"entities\":{\"tv_air_date\":{\"entities\":[{\"textRange\":{\"startIndex\":30.0,\"endIndex\":42.0},\"confidence\":0.82713646,\"origin\":\"STATISTICAL\",\"entities\":{},\"structValue\":{\"nuance_CALENDARX\":{\"nuance_CALENDAR_RANGE\":{\"nuance_CALENDAR_RANGE_START\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":5.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}},\"nuance_CALENDAR_RANGE_END\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_REL\":{\"nuance_STEP\":\"day\",\"nuance_INCREMENT\":1.0}},\"nuance_TIME\":{\"nuance_TIME_ABS\":{\"nuance_HOUR\":11.0,\"nuance_MINUTE\":59.0,\"nuance_AMPM\":\"am\",\"nuance_MODIFIER\":\"fuzzy\"}}}}}}},\"literal\":\"demain matin\",\"sensitive\":false,\"formattedLiteral\":\"demain matin\",\"formattedTextRange\":{\"startIndex\":30.0,\"endIndex\":42.0},\"metadata\":{}},{\"textRange\":{\"startIndex\":51.0,\"endIndex\":57.0},\"confidence\":0.82601124,\"origin\":\"STATISTICAL\",\"entities\":{},\"stringValue\":\"{\\\"nuance_CALENDARX\\\":\\\"{\\\"nuance_CALENDAR\\\":{\\\"nuance_TIME\\\":{\\\"nuance_TIME_ABS\\\":{\\\"nuance_AMPM\\\":\\\"am\\\",\\\"nuance_MINUTE\\\":30,\\\"nuance_HOUR\\\":9}}}}\\\"}\",\"literal\":\"à 9h30\",\"sensitive\":false,\"formattedLiteral\":\"à 9h30\",\"formattedTextRange\":{\"startIndex\":51.0,\"endIndex\":57.0},\"metadata\":{}}]},\"tv_query\":{\"entities\":[{\"textRange\":{\"startIndex\":20.0,\"endIndex\":29.0},\"confidence\":0.8014085,\"origin\":\"STATISTICAL\",\"entities\":{},\"stringValue\":\"Desperado\",\"literal\":\"Desperado\",\"sensitive\":false,\"formattedLiteral\":\"Desperado\",\"formattedTextRange\":{\"startIndex\":20.0,\"endIndex\":29.0},\"metadata\":{}}]},\"tv_station\":{\"entities\":[{\"textRange\":{\"startIndex\":47.0,\"endIndex\":50.0},\"confidence\":0.5830483,\"origin\":\"STATISTICAL\",\"entities\":{},\"stringValue\":\"tf1\",\"literal\":\"TF1\",\"sensitive\":false,\"formattedLiteral\":\"TF1\",\"formattedTextRange\":{\"startIndex\":47.0,\"endIndex\":50.0},\"metadata\":{}}]},\"tv_service\":{\"entities\":[{\"textRange\":{\"startIndex\":8.0,\"endIndex\":19.0},\"confidence\":0.60814315,\"origin\":\"STATISTICAL\",\"entities\":{},\"stringValue\":\"record\",\"literal\":\"enregistrer\",\"sensitive\":false,\"formattedLiteral\":\"enregistrer\",\"formattedTextRange\":{\"startIndex\":8.0,\"endIndex\":19.0},\"metadata\":{}}]}},\"metadata\":{}}}],\"sensitive\":false,\"formattedLiteral\":\"Peux-tu enregistrer Desperado demain matin sur TF1 à 9h30\"}}", argumentCaptor.getValue().toJson());

    }

}
