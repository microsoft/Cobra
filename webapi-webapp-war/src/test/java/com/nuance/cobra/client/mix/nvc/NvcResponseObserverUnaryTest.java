package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.tts.v1.SynthesizerGrpc;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NvcResponseObserverUnaryTest {

    @Test
    public void testOnCompleted_unknownError() {
        NvcClient mockClient = mock(NvcClient.class);
        mockClient = spy(mockClient);

        NvcResponseObserverUnary responseObserver = new NvcResponseObserverUnary(mockClient);

        SynthesizerProto.Status status = SynthesizerProto.Status.newBuilder()
                .setCode(400)
                .setDetails("details")
                .build();

        SynthesizerProto.UnarySynthesisResponse response = SynthesizerProto.UnarySynthesisResponse.newBuilder()
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        verify(mockClient, times(1)).sendError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testOnCompleted_unknownModule() {
        NvcClient mockClient = mock(NvcClient.class);
        mockClient = spy(mockClient);

        NvcResponseObserverStreaming responseObserver = new NvcResponseObserverStreaming(mockClient);

        SynthesizerProto.Status status = SynthesizerProto.Status.newBuilder()
                .setCode(400)
                .setDetails("TTS_E_MODULE_NOT_FOUND")
                .build();

        SynthesizerProto.SynthesisResponse response = SynthesizerProto.SynthesisResponse.newBuilder()
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        verify(mockClient, times(1)).sendError(ExternalResponseErrorCode.INVALID_SPEECHSYNTHESIS_VOICE_ERROR);
    }

    @Test
    public void testOnCompleted_rejectedResource() {
        NvcClient mockClient = mock(NvcClient.class);
        mockClient = spy(mockClient);

        NvcResponseObserverStreaming responseObserver = new NvcResponseObserverStreaming(mockClient);

        SynthesizerProto.Status status = SynthesizerProto.Status.newBuilder()
                .setCode(400)
                .setDetails("Rejected URI")
                .build();

        SynthesizerProto.SynthesisResponse response = SynthesizerProto.SynthesisResponse.newBuilder()
                .setStatus(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        verify(mockClient, times(1)).sendError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_REJECTED_RESOURCE);
    }

    @Test
    public void getSessionIdTest() {
        NvcRequestParams mockParams = mock(NvcRequestParams.class);
        SynthesizerGrpc.SynthesizerStub stub = SynthesizerGrpc.newStub(mock(io.grpc.Channel.class));
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        ResponseErrorHandler mockErrorHandler = mock(ResponseErrorHandler.class);
        HeaderEventListener mockInterceptor = mock(HeaderEventListener.class);

        NvcClient testClient = new NvcClient("testID", mockParams, stub, mockResponseHandler, mockErrorHandler, mockInterceptor);
        NvcResponseObserverStreaming testObserver = new NvcResponseObserverStreaming(testClient);

        assertEquals("testID", testObserver.getSessionId());
    }
}
