package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.rpc.LocalizedMessage;
import com.nuance.rpc.Status;
import com.nuance.rpc.StatusDetail;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesResponse;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

public class AsrForgetMeGrpcResponseObserverTest
        extends CobraTest {

    AsrForgetMeGrpcResponseObserver observer;
    ForgetMeGrpcClient mockClient;

    @Before
    public void setup() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        config.setProperty(WebApiConfig.Property.MIX_CLIENT_ID.key, "mixClientId");
        config.setProperty(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_SCOPE.key, "asr log tts");
        config.setProperty(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_FORM.key, "form");
        config.setProperty(WebApiConfig.Property.MIX_AUTH_URI.key, "fakeUri");
        config.setProperty(WebApiConfig.Property.MIX_CLIENT_SECRETS.key, List.of("fakeUri"));

        mockClient = mock(ForgetMeGrpcClient.class);
        when(mockClient.getCobraConfig()).thenReturn(configs);

        observer = new AsrForgetMeGrpcResponseObserver(mockClient);
    }

    @Test
    public void test_onNext() {
        Status status = Status.newBuilder()
                .setStatusCodeValue(200)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        DeleteSpeakerProfilesResponse response = DeleteSpeakerProfilesResponse.newBuilder()
                .setStatus(status)
                .build();

        startCapturingLogOutput(Level.DEBUG, ResponseObserver.class.getName());

        observer.onNext(response);

        assertEquals(observer.asrResponseStatus, status);
    }

    @Test
    public void test_onNext_Error() {
        Status status = Status.newBuilder()
                .setStatusCodeValue(500)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("Failure").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is not good").build())
                .build();
        DeleteSpeakerProfilesResponse response = DeleteSpeakerProfilesResponse.newBuilder()
                .setStatus(status)
                .build();

        observer.onNext(response);

        assertEquals(observer.asrResponseStatus, status);
    }

    @Test
    public void test_errorHandler() {

        CombinedConfiguration config = mockClient.getCobraConfig();
        mockClient = spy(mockClient);
        when(mockClient.getCobraConfig()).thenReturn(config);

        observer = new AsrForgetMeGrpcResponseObserver(mockClient);
        observer.errorHandler(new Exception("Resolver returned status code 404"));

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
    }

    @Test
    public void test_onCompleted() {
        Status status = Status.newBuilder()
                .setHttpTransCode(200)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).startLogForgetRequest();
    }

    @Test
    public void test_onCompleted_400() {
        Status status = Status.newBuilder()
                .setHttpTransCode(400)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_MALFORMED_OR_UNSUPPORTED_REQUEST_ERROR);
    }

    @Test
    public void test_onCompleted_401() {
        Status status = Status.newBuilder()
                .setHttpTransCode(401)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_AUTH_OR_PERMISSION_ERROR);
    }

    @Test
    public void test_onCompleted_403() {
        Status status = Status.newBuilder()
                .setHttpTransCode(403)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_AUTH_OR_PERMISSION_ERROR);
    }

    @Test
    public void test_onCompleted_500() {
        Status status = Status.newBuilder()
                .setHttpTransCode(500)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_INTERNAL_ERROR);
    }

    @Test
    public void test_onCompleted_511() {
        Status status = Status.newBuilder()
                .setHttpTransCode(511)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_INTERNAL_ERROR);
    }

    @Test
    public void test_onCompleted_870() {
        Status status = Status.newBuilder()
                .setHttpTransCode(870)
                .setStatusMessage(LocalizedMessage.newBuilder().setMessage("OK").build())
                .addStatusDetails(StatusDetail.newBuilder().setMessage("All is good").build())
                .build();
        observer.asrResponseStatus = status;
        observer.onCompleted();

        verify(mockClient, times(1)).sendErrorResponse(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
    }

    @Test
    public void test_isDone() {
        mockClient.isUserResetDone = false;
        assertFalse(observer.isDone());

        mockClient.isUserResetDone = true;
        assertTrue(observer.isDone());
    }
}
