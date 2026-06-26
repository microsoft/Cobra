package com.nuance.cobra.client.mix.forgetme;

import com.nuance.rpc.Status;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesRequest;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesResponse;
import com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc;
import io.grpc.stub.StreamObserver;

public class TestAsrForgetMeGrpcService
        extends ForgetMeGrpc.ForgetMeImplBase {

    private BehaviorReturnStatus behaviorReturnStatus = BehaviorReturnStatus.OK_200;
    private io.grpc.Status error;

    StreamObserver<DeleteSpeakerProfilesResponse> responseObserver;

    public enum BehaviorReturnStatus {
        OK_200,
        INTERNAL_SERVER_ERROR_500,
        ON_ERROR,
        DO_NOTHING
    }

    @Override
    public void deleteSpeakerProfiles(DeleteSpeakerProfilesRequest request, StreamObserver<DeleteSpeakerProfilesResponse> responseObserver) {
        this.responseObserver = responseObserver;

        DeleteSpeakerProfilesResponse response = null;

        if (behaviorReturnStatus == BehaviorReturnStatus.OK_200) {
            response = DeleteSpeakerProfilesResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setHttpTransCode(200)
                            .build())
                    .build();
        } else if (behaviorReturnStatus == BehaviorReturnStatus.INTERNAL_SERVER_ERROR_500) {
            response = DeleteSpeakerProfilesResponse.newBuilder()
                    .setStatus(Status.newBuilder()
                            .setHttpTransCode(500)
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

    public void sendOnNext(DeleteSpeakerProfilesResponse response) {
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
