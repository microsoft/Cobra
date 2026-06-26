package com.nuance.cobra.client.mix.krypton;

import com.nuance.rpc.asr.v1.*;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestKryptonGrpcService extends RecognizerGrpc.RecognizerImplBase {

    private boolean returnError;
    private BehaviorReturnStatus100 returnStatus100;
    private List<Result> intermediateResults;
    private Result finalResult;
    private int finalStatusCode;
    private String finalStatusMessage;
    private String finalStatusDetails;

    private boolean simulateLongProcessing;

    private StreamObserver<RecognitionResponse> responseObserver;

    // mocks
    private boolean onCompletedWasCalled;
    private List<String> resourceDomainLmUri = new ArrayList<>();
    private List<EnumWeight> resourceDomainLmWeight_enum = new ArrayList<>();
    private List<Float> resourceDomainLmWeight_float = new ArrayList<>();
    private String resourceInlineWordset;
    private String resourceBuiltin;
    private EnumWeight resourceWeight_enum;
    private Float resourceWeight_float;
    private boolean speakerProfileAdded = false;
    private final ByteArrayOutputStream audio = new ByteArrayOutputStream();

    public enum BehaviorReturnStatus100 {
        NEVER,
        AFTER_INIT_MESSAGE,
        MANUAL
    }

    @Override
    public StreamObserver<RecognitionRequest> recognize(StreamObserver<RecognitionResponse> responseObserver) {
        this.responseObserver = responseObserver;

        return new StreamObserver<>() {
            @Override
            public void onNext(RecognitionRequest request) {
                // mocks
                if (request.hasRecognitionInitMessage()) {
                    for (RecognitionResource res : request.getRecognitionInitMessage().getResourcesList()) {
                        if (res.hasExternalReference() && res.getExternalReference().getType().getNumber() == 3 ) {
                            resourceDomainLmUri.add(res.getExternalReference().getUri());
                            resourceDomainLmWeight_enum.add(res.getWeightEnum());
                            resourceDomainLmWeight_float.add(res.getWeightValue());
                        } else {
                            resourceInlineWordset = res.getInlineWordset();
                            resourceBuiltin = res.getBuiltin();
                            resourceWeight_enum = res.getWeightEnum();
                            resourceWeight_float = res.getWeightValue();
                        }
                        if(res.getExternalReference().getType().getNumber() == 4){
                            speakerProfileAdded = true;
                        }
                    }
                } else if (request.getAudio().size() > 0) {
                    try {
                        audio.write(request.getAudio().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (returnError) {
                    responseObserver.onError(new Exception("This is expected."));

                } else if (request.hasRecognitionInitMessage() && returnStatus100 == BehaviorReturnStatus100.AFTER_INIT_MESSAGE) {
                    returnStatus100();

                } else if (request.hasRecognitionInitMessage() && returnStatus100 == BehaviorReturnStatus100.MANUAL) {
                    // do nothing

                } else {
                    if (intermediateResults != null) {
                        for (Result intermediateResult : intermediateResults) {
                            RecognitionResponse intermediateResultResponse = RecognitionResponse.newBuilder()
                                    .setResult(intermediateResult)
                                    .build();
                            responseObserver.onNext(intermediateResultResponse);
                        }
                    }
                    if (!simulateLongProcessing) {
                        TestKryptonGrpcService.this.onCompleted(responseObserver);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onCompleted() {
                onCompletedWasCalled = true;

                TestKryptonGrpcService.this.onCompleted(responseObserver);
            }
        };
    }

    private void onCompleted(StreamObserver<RecognitionResponse> responseObserver) {
        if (finalResult != null) {
            RecognitionResponse finalResultResponse = RecognitionResponse.newBuilder()
                    .setResult(finalResult)
                    .build();
            responseObserver.onNext(finalResultResponse);
        }

        RecognitionResponse finalStatusResponse = RecognitionResponse.newBuilder()
                .setStatus(Status.newBuilder()
                        .setCode(finalStatusCode)
                        .setMessage(finalStatusMessage)
                        .setDetails(finalStatusDetails))
                .build();
        responseObserver.onNext(finalStatusResponse);
        responseObserver.onCompleted();
    }

    public void setBehavior(boolean returnError,
                            BehaviorReturnStatus100 returnStatus100,
                            List<Result> intermediateResults,
                            Result finalResult,
                            int finalStatusCode,
                            String finalStatusMessage,
                            boolean simulateLongProcessing) {

        this.returnError = returnError;
        this.returnStatus100 = returnStatus100;
        this.intermediateResults = intermediateResults;
        this.finalResult = finalResult;
        this.finalStatusCode = finalStatusCode;
        this.finalStatusMessage = finalStatusMessage;
        this.finalStatusDetails = "";
        this.simulateLongProcessing = simulateLongProcessing;
    }

    public void setBehavior(boolean returnError,
                            BehaviorReturnStatus100 returnStatus100,
                            List<Result> intermediateResults,
                            Result finalResult,
                            int finalStatusCode,
                            String finalStatusMessage,
                            String finalStatusDetails,
                            boolean simulateLongProcessing) {

        this.returnError = returnError;
        this.returnStatus100 = returnStatus100;
        this.intermediateResults = intermediateResults;
        this.finalResult = finalResult;
        this.finalStatusCode = finalStatusCode;
        this.finalStatusMessage = finalStatusMessage;
        this.finalStatusDetails = finalStatusDetails;
        this.simulateLongProcessing = simulateLongProcessing;
    }

    public void returnStatus100() {
        RecognitionResponse statusResponse100 = RecognitionResponse.newBuilder()
                .setStatus(Status.newBuilder()
                        .setCode(100)
                        .setMessage("Continue"))
                .build();
        responseObserver.onNext(statusResponse100);
    }

    public void resetMocks() {
        onCompletedWasCalled = false;
        resourceDomainLmUri = new ArrayList<>();
        resourceDomainLmWeight_enum = new ArrayList<>();
        resourceDomainLmWeight_float = new ArrayList<>();
        resourceInlineWordset = null;
        resourceBuiltin = null;
        resourceWeight_enum = null;
        resourceWeight_float = null;
        speakerProfileAdded = false;
        audio.reset();
    }

    public boolean onCompletedWasCalled() {
        return onCompletedWasCalled;
    }

    public String getResourceDomainLmUri() {
        return getResourceDomainLmUri(0);
    }

    public String getResourceDomainLmUri(int index) {
        return resourceDomainLmUri.get(index);
    }

    public EnumWeight getResourceDomainLmWeight_enum() {
        return getResourceDomainLmWeight_enum(0);
    }

    public EnumWeight getResourceDomainLmWeight_enum(int index) {
        return resourceDomainLmWeight_enum.get(index);
    }

    public float getResourceDomainLmWeight_float() {
        return getResourceDomainLmWeight_float(0);
    }

    public float getResourceDomainLmWeight_float(int index) {
        return resourceDomainLmWeight_float.get(index);
    }

    public String getResourceInlineWordset() {
        return resourceInlineWordset;
    }

    public String getResourceBuiltin() {
        return resourceBuiltin;
    }

    public EnumWeight getResourceWeight_enum() {
        return resourceWeight_enum;
    }

    public Float getResourceWeight_float() {
        return resourceWeight_float;
    }

    public boolean getSpeakerProfileAdded(){
        return speakerProfileAdded;
    }

    public byte[] getAudio() {
        return audio.toByteArray();
    }
}
