package com.nuance.cobra.client.mix.nvc;

import com.google.protobuf.ByteString;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.rpc.tts.v1.SynthesizerProto;

import java.util.Map;

abstract class NvcResponseObserver<T> extends ResponseObserver<T> {
    protected final NvcClient nvcClient;
    protected SynthesizerProto.Status status;

    protected int mediaDurationMillis;

    public NvcResponseObserver(NvcClient nvcClient) {
        this.nvcClient = nvcClient;
    }

    protected void onAudio(ByteString audio) {
        int audioSize = audio.size();
        byte[] audioBytes = audio.toByteArray();

        logger.debug("Received NVC response, Type=AUDIO, Size={}", audioSize);

        nvcClient.sendAudio(audioBytes);
    }

    protected void onEvent(SynthesizerProto.Events events) {
        int eventCount = events.getEventsCount();

        logger.debug("Received NVC response, Type=EVENTS, EventCount={}, Events={}", eventCount, events);

        // update mediaDurationMillis // TODO add error handling
        for (SynthesizerProto.Event event : events.getEventsList()) {
            if (event.getName().equalsIgnoreCase("NVOCsynd")) {
                Map<String, String> eventValues = event.getValuesMap();

                if (eventValues.containsKey("DURS")) {
                    mediaDurationMillis = Integer.parseInt(eventValues.get("DURS"));
                    nvcClient.setResponseStatisticsMediaDurationMillis(mediaDurationMillis);
                }
            }
        }
    }

    protected void onStatus(SynthesizerProto.Status responseStatus) {
        status = responseStatus;
        int statusCode = status.getCode(); // HTTP status
        String message = status.getMessage();
        String messageDetails = status.getDetails();

        if (statusCode == 200) {
            logger.debug("Received NVC response, Type=STATUS, StatusCode={}, Message={}, MessageDetails={}", statusCode, message, messageDetails);
        } else {
            logger.error("Received NVC response, Type=STATUS, StatusCode={}, Message={}, MessageDetails={}", statusCode, message, messageDetails);
        }
    }

    // Example: io.grpc.StatusRuntimeException: UNAUTHENTICATED: {"error":{"code":401,"status":"Unauthorized","reason":"Token is expired","message":"Access credentials are invalid"}
    @Override
    public void errorHandler(Throwable e) { // when onError is called, it is not followed by a call to onCompleted
        completeTransaction(e, ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_GENERIC_ERROR);
    }

    protected void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode) {
        MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
        MixUtils.invalidateTokenIfUnauthorized(logger, e, nvcClient.params.getConfig());
        nvcClient.sendError(responseCode);
    }

    protected boolean isDone() {
        return nvcClient.isSpeechSynthesisDone;
    }

    /**
     * onCompleted is called once the server is done processing the request, UNLESS onError was called
     */
    @Override
    public void onCompleted() {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        if (status.getCode() == 200) {
            nvcClient.sendResponse(ResponseResult.ResponseStatus.SUCCESS);
        } else {
            logger.error("NVC operation completed with error: {}, code: {}, details: {}.", status.getMessage(), status.getCode(), status.getDetails());
            String details = status.getDetails();
            if(details.contains("TTS_E_MODULE_NOT_FOUND")) {
                nvcClient.sendError(ExternalResponseErrorCode.INVALID_SPEECHSYNTHESIS_VOICE_ERROR);
            }
            else if(details.contains("Rejected URI")) {
                nvcClient.sendError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_REJECTED_RESOURCE);
            }
            else {
                nvcClient.sendError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_GENERIC_ERROR);
            }
        }
    }

    @Override
    protected String getSessionId() {
        return nvcClient.getSessionId();
    }
}
