package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ForgetMeGrpcClientTest extends CobraTest {

    @ClassRule
    public static GrpcCleanupRule cleanupRule = new GrpcCleanupRule();

    private static String serverName = InProcessServerBuilder.generateName();
    private static TestAsrForgetMeGrpcService asrForgetMeGrpcService = new TestAsrForgetMeGrpcService();
    private static TestLogForgetMeGrpcService logForgetMeGrpcService = new TestLogForgetMeGrpcService();
    private static ManagedChannel channel;

    private ResponseHandler mockResponseHandler;
    private ResponseErrorHandler mockErrorHandler;
    private ForgetMeGrpcClient client;

    @BeforeClass
    public static void runsOnlyOnceBeforeTests() throws IOException {
        Server grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor() // directExecutor is fine for unit tests
                .addService(asrForgetMeGrpcService)
                .addService(logForgetMeGrpcService)
                .build()
                .start();
        cleanupRule.register(grpcServer);
    }

    @Before
    public void runsBeforeEachTest() throws Exception {
        super.setUp(); // required so that CobraTest.setUp() is called

        asrForgetMeGrpcService.setBehavior(TestAsrForgetMeGrpcService.BehaviorReturnStatus.OK_200);
        logForgetMeGrpcService.setBehavior(TestLogForgetMeGrpcService.BehaviorReturnStatus.OK);

        mockResponseHandler = mock(ResponseHandler.class);
        mockErrorHandler = mock(ResponseErrorHandler.class);
        client = new ForgetMeGrpcClient("mySessionId", buildCobraConfig(), buildAsrForgetMeStub(), buildLogForgetMeStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);

        Command mockCommand = Mockito.mock(Command.class);
        Mockito.when(mockSession.getCommand()).thenReturn(mockCommand);
        Mockito.when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));

        Command.CommandStatistics mockCommandStatistics = Mockito.mock(Command.CommandStatistics.class);
        Mockito.when(mockCommand.getStatistics()).thenReturn(mockCommandStatistics);
    }

    private static com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub buildAsrForgetMeStub() {
        // gRPC channel (client<->server)
        // we build a new channel each time because they are shut down after each request
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor() // directExecutor is fine for unit tests
                .build();

        // gRPC stub
        return com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.newStub(channel);
    }

    private static com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub buildLogForgetMeStub() {
        // gRPC channel (client<->server)
        // we build a new channel each time because they are shut down after each request
        channel = InProcessChannelBuilder.forName(serverName)
                                         .directExecutor() // directExecutor is fine for unit tests
                                         .build();

        // gRPC stub
        return com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.newStub(channel);
    }

    private static CombinedConfiguration buildCobraConfig() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");
        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "myUserId");

        return configs;
    }

    @Test
    public void test_startUserReset() {
        client.startUserReset();

        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<ResponseResult> captor = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        ResponseResult response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.SUCCESS, response.getStatus());
    }

    @Test(expected = NullPointerException.class)
    public void test_startUserReset_exception() {
        client = new ForgetMeGrpcClient("mySessionId", buildCobraConfig(), null, null, mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        client.startUserReset();
    }

    @Test
    public void testInterpretation_500_internalServerError() {
        asrForgetMeGrpcService.setBehavior(TestAsrForgetMeGrpcService.BehaviorReturnStatus.INTERNAL_SERVER_ERROR_500);
        client.startUserReset();

        verifyError();
    }

    @Test
    public void testInterpretation_onError() {
        asrForgetMeGrpcService.setBehavior(TestAsrForgetMeGrpcService.BehaviorReturnStatus.ON_ERROR);
        client.startUserReset();

        verifyError();
    }

    private ResponseResult verifyError() {
        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, times(1)).handleError(any());

        ArgumentCaptor<ResponseResult> captor = ArgumentCaptor.forClass(ResponseResult.class);
        verify(mockErrorHandler).handleError(captor.capture());
        ResponseResult response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, response.getStatus());

        //assertTrue(channel.isShutdown());

        return response;
    }

    @Test
    public void testLogInternalError() {
        logForgetMeGrpcService.setBehavior(TestLogForgetMeGrpcService.BehaviorReturnStatus.INTERNAL_ERROR);
        client.startUserReset();

        verifyError();
    }
}
