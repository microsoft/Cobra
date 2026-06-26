package com.nuance.cobra.client.mix.nvc;

import com.google.protobuf.ByteString;
import com.nuance.rpc.tts.v1.SynthesizerGrpc;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class TestNvcService extends SynthesizerGrpc.SynthesizerImplBase {
    private Behavior behavior = Behavior.SUCCESS;
    private boolean simulateLongProcessing;

    // mocks
    List<SynthesizerProto.SynthesisResource> resources;

    public enum Behavior {
        SUCCESS,
        INTERNAL_SERVER_ERROR_500,
        ERROR
    }

    @Override
    public void synthesize(SynthesizerProto.SynthesisRequest request, StreamObserver<SynthesizerProto.SynthesisResponse> responseObserver) {
        switch (behavior) {
            case SUCCESS:
                success(request, responseObserver);
                break;
            case INTERNAL_SERVER_ERROR_500:
                internalServerError500(request, responseObserver);
                break;
            case ERROR:
                error(request, responseObserver);
                break;
        }
    }

    private void success(SynthesizerProto.SynthesisRequest request, StreamObserver<SynthesizerProto.SynthesisResponse> responseObserver) {
        resources = request.getInput().getResourcesList();

        ByteString audio = ByteString.copyFrom(new byte[0]);

        SynthesizerProto.SynthesisResponse audioResponse = SynthesizerProto.SynthesisResponse.newBuilder()
                .setAudio(audio)
                .build();

        responseObserver.onNext(audioResponse);

        if (request.hasEventParams() && request.getEventParams().getSendLogEvents()) {
            SynthesizerProto.SynthesisResponse eventResponse = SynthesizerProto.SynthesisResponse.newBuilder()
                    .setEvents(SynthesizerProto.Events.newBuilder()
                            .addEvents(SynthesizerProto.Event.newBuilder()
                                    .setName("NVOCsynd")
                                    .putValues("DURS", "844")))
                    .build();

            responseObserver.onNext(eventResponse);
        }

        SynthesizerProto.SynthesisResponse statusResponse = SynthesizerProto.SynthesisResponse.newBuilder()
                .setStatus(SynthesizerProto.Status.newBuilder()
                        .setCode(200)
                        .setMessage("OK")
                        .build())
                .build();

        if (!simulateLongProcessing) {
            responseObserver.onNext(statusResponse);
            responseObserver.onCompleted();
        }
    }

    private void internalServerError500(SynthesizerProto.SynthesisRequest request, StreamObserver<SynthesizerProto.SynthesisResponse> responseObserver) {
        SynthesizerProto.SynthesisResponse statusResponse = SynthesizerProto.SynthesisResponse.newBuilder()
                .setStatus(SynthesizerProto.Status.newBuilder()
                        .setCode(500)
                        .setMessage("Internal Server Error")
                        .build())
                .build();

        responseObserver.onNext(statusResponse);

        responseObserver.onCompleted();
    }

    private void error(SynthesizerProto.SynthesisRequest request, StreamObserver<SynthesizerProto.SynthesisResponse> responseObserver) {
        responseObserver.onError(new Exception("Expected Error"));
    }

    public void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }

    public void setSimulateLongProcessing(boolean simulateLongProcessing) {
        this.simulateLongProcessing = simulateLongProcessing;
    }

    public void resetMocks() {
        resources = null;
    }

    public List<SynthesizerProto.SynthesisResource> getResources() {
        return resources;
    }
}
