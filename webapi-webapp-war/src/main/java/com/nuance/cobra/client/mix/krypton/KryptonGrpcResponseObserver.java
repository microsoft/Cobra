package com.nuance.cobra.client.mix.krypton;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.cobra.utils.audio.AudioNodeObserver;
import com.nuance.cobra.utils.krypton.KryptonConverter;
import com.nuance.rpc.asr.v1.*;
import io.grpc.StatusRuntimeException;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class KryptonGrpcResponseObserver extends ResponseObserver<RecognitionResponse> {
    private final KryptonGrpcClient eventOwner;

    private final LinkedList<Status> statuses = new LinkedList<>();
    private final LinkedList<Result> results = new LinkedList<>();
    public KryptonGrpcResponseObserver(KryptonGrpcClient eventOwner) {
        this.eventOwner = eventOwner;
    }

    @Override
    public void onNext(RecognitionResponse response) {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        if (eventOwner.state == KryptonGrpcClient.State.CANCELED) return;

        switch (response.getResponseUnionCase()) {
            case STATUS:
                onNextStatus(response);
                break;

            case RESULT:
                onNextResult(response);
                break;

            case START_OF_SPEECH:
                notifyObservers(AudioNodeObserver.NodeEvent.START_OF_SPEECH);
                logger.trace("Received start_of_speech");
                break;

            default:
                logger.warn("Unrecognized response {}", response.getResponseUnionCase());
        }
    }

    private void onNextStatus(RecognitionResponse response) {
        var status = response.getStatus();
        statuses.add(status);

        int statusCode = status.getCode(); // HTTP status
        String message = status.getMessage();
        String messageDetails = status.getDetails();

        var logFormat = "Received Krypton gRPC response, Type=STATUS, StatusCode={}, Message={}, MessageDetails={}";

        if ( ! (statusCode >= 100 && statusCode <= 299)) {
            Level level = Level.ERROR;
            if (statusCode == 408) { level = Level.DEBUG; }
            logger.log(level, logFormat, statusCode, message, messageDetails);
            onCompleted();
        } else if (statusCode == 204) {
            logger.debug(logFormat, statusCode, message, messageDetails);
            buildAndSendFinalSpeechRecoResponse();
        } else {
            logger.debug(logFormat, statusCode, message, messageDetails);
        }
    }

    private void onNextResult(RecognitionResponse response) {
        var result = response.getResult();
        results.add(result);

        EnumResultType resultType = result.getResultType();
        var resultString = maskIfNecessary(result);
        logger.debug("Received Krypton gRPC response, Type=RESULT, ResultType={}, Result={}",
                resultType, resultString);

        switch (result.getResultTypeValue()) {
            case EnumResultType.PARTIAL_VALUE:
            case EnumResultType.IMMUTABLE_PARTIAL_VALUE:
                if (canSendIntermediateResult()) {
                    var speechRecoResponse = new SpeechRecoResponse(false, buildRecoData(result), eventOwner.noiseLevel);
                    if (eventOwner.config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.key)) {
                        speechRecoResponse.setVerboseResult(KryptonConverter.getVerboseResultFromGRPC(result));
                    }

                    if (eventOwner.config.getBoolean(WebApiConfig.Property.ENGINE_RAW_RESULT.key)) {
                        speechRecoResponse.setRawResult(KryptonConverter.getVerboseResultFromGRPC(result)); // As of now, crypton gRPC raw is the same as standard verbose.
                    }
                    eventOwner.sendResponse(speechRecoResponse);
                } else {
                    logger.debug("Discarding partial result because endpointing is activated and start of speech was not detected.");
                }
                break;
            case EnumResultType.FINAL_VALUE:
                notifyObservers(AudioNodeObserver.NodeEvent.END_OF_SPEECH);
                eventOwner.state = KryptonGrpcClient.State.COMPLETED;
                buildAndSendFinalSpeechRecoResponse();
                break;
            default:
                logger.warn("Unrecognized result type {}", result.getResultTypeValue());
        }
    }

    @Override
    public boolean isDone() {
        return eventOwner.state == KryptonGrpcClient.State.CANCELED;
    }

    @Override
    public void errorHandler(Throwable e)
    {
        if (e instanceof StatusRuntimeException)
        {
            var statusRuntimeException = (StatusRuntimeException) e;
            if (statusRuntimeException.getStatus().getCode() == io.grpc.Status.Code.CANCELLED)
            {
                logger.info("Krypton gRPC cancelled: {}", statusRuntimeException.getMessage());
                return;
            }
        }

        logger.debug("Krypton Grpc client received an onError() event.", e);
        completeTransaction(e, ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
    }

    @Override
    protected void completeTransaction(Throwable e, ExternalResponseErrorCode speechRecognitionCommandGenericError) {
        eventOwner.state = KryptonGrpcClient.State.COMPLETED;
        MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
        MixUtils.invalidateTokenIfUnauthorized(logger, e, eventOwner.config);
        eventOwner.sendErrorResponse(speechRecognitionCommandGenericError);
    }

    @Override
    public void onCompleted() {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        if (eventOwner.state == KryptonGrpcClient.State.CANCELED
                || !eventOwner.hasActiveSession()) return;

        logger.debug("onCompleted()");
        eventOwner.state = KryptonGrpcClient.State.COMPLETED;

        var lastStatus = statuses.getLast();

        SpeechRecoResponse speechRecoResponse;

        try {
            switch (lastStatus.getCode()) {
                case 200: // Success
                    // won't ever hit this execution point because the gRPC call was cancelled during buildAndSendFinalSpeechRecoResponse() right after the final speech reco result was received in onNext()
                    logger.info("Krypton gRPC onCompleted() was hit with status code 200 after the final result.");
                    speechRecoResponse = new SpeechRecoResponse(true, eventOwner.params.isEndpointingActivated() ? noInputOrNoMatch() : ResponseResult.ResponseStatus.NO_INPUT, eventOwner.noiseLevel);
                    eventOwner.sendResponse(speechRecoResponse);
                    break;

                case 204: // No result
                case 404: // No speech
                    speechRecoResponse = new SpeechRecoResponse(true, eventOwner.params.isEndpointingActivated() ? noInputOrNoMatch() : ResponseResult.ResponseStatus.NO_INPUT, eventOwner.noiseLevel);
                    eventOwner.sendResponse(speechRecoResponse);
                    break;
                case 408: // Audio timeout
                    speechRecoResponse = new SpeechRecoResponse(true, ResponseResult.ResponseStatus.NO_INPUT, eventOwner.noiseLevel);
                    eventOwner.sendResponse(speechRecoResponse);
                    break;
                case 502: // Resource error
                    eventOwner.sendErrorResponse(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_ERROR_502);
                    break;

                case 400: // Bad request
                    if (lastStatus.getDetails().contains("no artifact for URN")) {
                        eventOwner.sendErrorResponse(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_NOT_FOUND_ERROR);
                        break;
                    } else if (lastStatus.getDetails().contains("invalid language requested")) {
                        eventOwner.sendErrorResponse(ExternalResponseErrorCode.KRYPTON_GRPC_RESOURCE_LANGUAGE_ERROR);
                    }
                    logger.error("Krypton gRPC client returned a non 200 code.Code: {}", lastStatus.getCode());
                    eventOwner.sendErrorResponse(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
                    break;
                case 409: // Conflict
                case 410: // Not recognizing
                case 413: // Too much speech
                case 500: // Internal server error
                case 503: // Service unavailable
                    logger.error("Krypton gRPC client returned a non 200 code.Code: {}", lastStatus.getCode());
                    eventOwner.sendErrorResponse(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_GENERIC_ERROR);
                    break;

                default:
                    logger.error("Unsupported Krypton gRPC response status code. Code {}", lastStatus.getCode());
                    eventOwner.sendErrorResponse(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_EXECUTE_ERROR);
                    break;
            }
        } catch (Exception e) {
            MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.SPEECH_RECOGNITION_COMMAND_EXECUTE_ERROR);
        }
    }

    private void buildAndSendFinalSpeechRecoResponse()
    {
        SpeechRecoResponse speechRecoResponse;
        if (!results.isEmpty() && hasNonRejectedHypotheses(results.getLast())) {
            speechRecoResponse = new SpeechRecoResponse(true, buildRecoData(results.getLast()), eventOwner.noiseLevel);
            if (eventOwner.config.getBoolean(WebApiConfig.Property.ENGINE_RAW_RESULT.key)) {
                speechRecoResponse.setRawResult(KryptonConverter.getVerboseResultFromGRPC(results.getLast())); // As of now, crypton gRPC raw is the same as standard verbose.
            }

            if (eventOwner.config.getBoolean(WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.key)) {
                speechRecoResponse.setVerboseResult(KryptonConverter.getVerboseResultFromGRPC(results.getLast()));
            }
        } else {
            speechRecoResponse = new SpeechRecoResponse(true, eventOwner.params.isEndpointingActivated() ? noInputOrNoMatch() : ResponseResult.ResponseStatus.NO_MATCH, eventOwner.noiseLevel);
        }

        eventOwner.sendResponse(speechRecoResponse);
    }

    private boolean hasNonRejectedHypotheses(Result result) {
        for (Hypothesis hypothesis : result.getHypothesesList()) {
            if (!hypothesis.getRejected()) {
                return true;
            }
        }
        return false;
    }

    private SpeechRecoResponse.Data buildRecoData(Result result) {
        List<Hypothesis> hypotheses = new ArrayList<>();

        // keep only non-rejected hypotheses
        for (Hypothesis hypothesis : result.getHypothesesList()) {
            if (!hypothesis.getRejected()) {
                hypotheses.add(hypothesis);
            }
        }

        int hSize = hypotheses.size();
        var transcriptions = new String[hSize];
        var confidences = new int[hSize];
        var words2D = new SpeechRecoResponse.Word[hSize][];

        for (var h = 0; h < hSize; h++) {
            var hypothesis = hypotheses.get(h);
            transcriptions[h] = hypothesis.getFormattedText();
            confidences[h] = Math.round(hypothesis.getAverageConfidence() * 1000);

            int wSize = hypothesis.getWordsList().size();
            var words1D = new SpeechRecoResponse.Word[wSize];

            for (var w = 0; w < wSize; w++) {
                var word = hypothesis.getWords(w);
                words1D[w] = new SpeechRecoResponse.Word(word.getText(), Math.round(word.getConfidence() * 1000));
            }
            words2D[h] = words1D;
        }
        return new SpeechRecoResponse.Data(transcriptions, confidences, Math.round(hypotheses.get(0).getConfidence() * 1000), words2D, eventOwner.config.getInt(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key));
    }

    private String maskIfNecessary(Result result) {
        return eventOwner.params.shouldMaskUserUtterance() ? "<masked>" : result.toString();
    }

    private boolean canSendIntermediateResult() {
        return !eventOwner.params.isEndpointingActivated() || eventOwner.startOfSpeechWasDetected;
    }

    private ResponseResult.ResponseStatus noInputOrNoMatch() {
        return eventOwner.startOfSpeechWasDetected ? ResponseResult.ResponseStatus.NO_MATCH : ResponseResult.ResponseStatus.NO_INPUT;
    }

    public void cancel() {
        //should cleanup stream here.
    }

    @Override
    protected String getSessionId() {
        return eventOwner.getSessionId();
    }

    private void notifyObservers(AudioNodeObserver.NodeEvent event) {
        for (AudioNodeObserver obs: eventOwner.observers) {
            if (obs != null) { //ignore null observers
                obs.onEvent(null, event);
            }
        }
    }
} //class KryptonGrpcResponseObserver

