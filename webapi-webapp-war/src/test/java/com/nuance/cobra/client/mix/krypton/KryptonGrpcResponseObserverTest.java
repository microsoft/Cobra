package com.nuance.cobra.client.mix.krypton;

import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.utils.audio.AudioNodeObserver;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.asr.v1.RecognitionResponse;
import com.nuance.rpc.asr.v1.RecognizerGrpc;
import com.nuance.rpc.asr.v1.StartOfSpeech;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KryptonGrpcResponseObserverTest {

    @Test
    public void onCompleted() {
        CombinedConfiguration mockConfig = mock (CombinedConfiguration.class);
        KryptonGrpcRequestParams mockParams = mock (KryptonGrpcRequestParams.class);
        RecognizerGrpc.RecognizerStub stub = RecognizerGrpc.newStub(mock(io.grpc.Channel.class));
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        ResponseErrorHandler mockErrorHandler = mock(ResponseErrorHandler.class);
        HeaderEventListener mockInterceptor = mock(HeaderEventListener.class);

        KryptonGrpcClient testClient = new KryptonGrpcClient(mockConfig, "testID", mockParams, stub, mockResponseHandler, mockErrorHandler, mockInterceptor);
        KryptonGrpcResponseObserver testObserver = new KryptonGrpcResponseObserver(testClient);

        testObserver.onCompleted();
    }

    @Test
    public void getSessionIdTest() {
        CombinedConfiguration mockConfig = mock (CombinedConfiguration.class);
        KryptonGrpcRequestParams mockParams = mock (KryptonGrpcRequestParams.class);
        RecognizerGrpc.RecognizerStub stub = RecognizerGrpc.newStub(mock(io.grpc.Channel.class));
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        ResponseErrorHandler mockErrorHandler = mock(ResponseErrorHandler.class);
        HeaderEventListener mockInterceptor = mock(HeaderEventListener.class);

        KryptonGrpcClient testClient = new KryptonGrpcClient(mockConfig, "testID", mockParams, stub, mockResponseHandler, mockErrorHandler, mockInterceptor);
        KryptonGrpcResponseObserver testObserver = new KryptonGrpcResponseObserver(testClient);

        assertEquals("testID", testObserver.getSessionId());
    }

    @Test
    public void testStartSpeechEvent() {
        CombinedConfiguration mockConfig = mock (CombinedConfiguration.class);
        KryptonGrpcRequestParams mockParams = mock (KryptonGrpcRequestParams.class);
        RecognizerGrpc.RecognizerStub stub = RecognizerGrpc.newStub(mock(io.grpc.Channel.class));
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        ResponseErrorHandler mockErrorHandler = mock(ResponseErrorHandler.class);
        HeaderEventListener mockInterceptor = mock(HeaderEventListener.class);

        KryptonGrpcClient testClient = new KryptonGrpcClient(mockConfig, "testID", mockParams, stub, mockResponseHandler, mockErrorHandler, mockInterceptor);
        List<AudioNodeObserver> observers = new ArrayList<>();
        AudioNodeObserver mockAudioNodeObserver = mock(AudioNodeObserver.class);
        observers.add(mockAudioNodeObserver);
        testClient.setObservers(observers);

        KryptonGrpcResponseObserver testObserver = new KryptonGrpcResponseObserver(testClient);

        RecognitionResponse.Builder builder = RecognitionResponse.newBuilder();
        builder.setStartOfSpeech(StartOfSpeech.newBuilder().build());
        testObserver.onNext(builder.build());

        verify(mockAudioNodeObserver).onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
    }
}