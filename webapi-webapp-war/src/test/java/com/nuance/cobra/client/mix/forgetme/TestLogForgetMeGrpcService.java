package com.nuance.cobra.client.mix.forgetme;

import com.nuance.rpc.Status;
import com.nuance.rpc.StatusCode;
import com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc;
import com.nuance.rpc.fabric.forget.v1.ForgetRequest;
import com.nuance.rpc.fabric.forget.v1.ForgetResponse;
import io.grpc.stub.StreamObserver;

public class TestLogForgetMeGrpcService
        extends ForgetMeGrpc.ForgetMeImplBase {

    private BehaviorReturnStatus behaviorReturnStatus = BehaviorReturnStatus.OK;
    private io.grpc.Status error;

    StreamObserver<ForgetResponse> responseObserver;

    public enum BehaviorReturnStatus {
        OK,
        INTERNAL_ERROR,
        ON_ERROR,
        DO_NOTHING
    }

    @Override
    public void forget(ForgetRequest request, StreamObserver<ForgetResponse> responseObserver) {
        this.responseObserver = responseObserver;

        ForgetResponse response = null;

        if (behaviorReturnStatus == BehaviorReturnStatus.OK) {
            response = ForgetResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setStatusCode(StatusCode.OK)
                            .build())
                    .build();
        } else if (behaviorReturnStatus == BehaviorReturnStatus.INTERNAL_ERROR) {
            response = ForgetResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setStatusCode(StatusCode.INTERNAL_ERROR)
                            .build())
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

    public void sendOnNext(ForgetResponse response) {
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
}
