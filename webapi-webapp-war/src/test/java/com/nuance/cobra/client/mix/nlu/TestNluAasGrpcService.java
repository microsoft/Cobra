package com.nuance.cobra.client.mix.nlu;

import com.nuance.rpc.nlu.v1.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class TestNluAasGrpcService
        extends RuntimeGrpc.RuntimeImplBase {

    private BehaviorReturnStatus behaviorReturnStatus = BehaviorReturnStatus.OK_200;
    private io.grpc.Status error;

    StreamObserver<InterpretResponse> responseObserver;

    // mocks
    private List<String> inlineWordsets = new ArrayList<>();
    private int maxInterpretations = -1;

    public enum BehaviorReturnStatus {
        OK_200,
        INTERNAL_SERVER_ERROR_500,
        ON_ERROR,
        DO_NOTHING
    }

    @Override
    public void interpret(InterpretRequest request, StreamObserver<InterpretResponse> responseObserver) {
        this.responseObserver = responseObserver;

        InterpretResponse response = null;

        for (InterpretationResource res : request.getResourcesList()) {
            if (res.getResourceUnionCase().equals(InterpretationResource.ResourceUnionCase.INLINE_WORDSET)) {
                inlineWordsets.add(res.getInlineWordset());
            }
        }

        maxInterpretations = request.getParameters().getMaxInterpretations();

        if (behaviorReturnStatus == BehaviorReturnStatus.OK_200) {
            response = InterpretResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(200))
                    .setResult(InterpretResult.newBuilder()
                            .addInterpretations(Interpretation.newBuilder()
                                    .setSingleIntentInterpretation(SingleIntentInterpretation.newBuilder()
                                            .setIntent("TEST-INTENT")
                                            .setConfidence(0.37f)
                                            .putEntities("TEST-ENTITY", SingleIntentEntityList.newBuilder()
                                                    .addEntities(SingleIntentEntity.newBuilder()
                                                            .setStringValue("test-value"))
                                                    .build()))))
                    .build();
        } else if (behaviorReturnStatus == BehaviorReturnStatus.INTERNAL_SERVER_ERROR_500) {
            response = InterpretResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setCode(500))
                    .build();
        }

        if (behaviorReturnStatus == BehaviorReturnStatus.ON_ERROR) {
            sendOnError();

        } else if (behaviorReturnStatus != BehaviorReturnStatus.DO_NOTHING) {
            sendOnNext(response);
            sendOnCompleted();
        }
    }

    public void sendOnError() {
        responseObserver.onError(error.asException());
    }

    public void sendOnNext(InterpretResponse response) {
        responseObserver.onNext(response);
    }

    public void sendOnCompleted() {
        responseObserver.onCompleted();
    }

    public void setBehavior(BehaviorReturnStatus behaviorReturnStatus) {
        this.behaviorReturnStatus = behaviorReturnStatus;
    }

    public void setError(io.grpc.Status error) {
        this.error = error;
    }

    public void resetMocks() {
        inlineWordsets = new ArrayList<>();
        maxInterpretations = -1;
    }

    public List<String> getInlineWordsets() {
        return inlineWordsets;
    }

    public int getMaxInterpretations() {
        return maxInterpretations;
    }
}
