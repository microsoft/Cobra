package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.rpc.Status;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesResponse;

class AsrForgetMeGrpcResponseObserver
        extends ResponseObserver<DeleteSpeakerProfilesResponse> {
    protected Status asrResponseStatus;
    private final ForgetMeGrpcClient asrEventOwner;

    public AsrForgetMeGrpcResponseObserver(ForgetMeGrpcClient asrEventOwner) {
        this.asrEventOwner = asrEventOwner;
    }

    @Override
    public void onNext(DeleteSpeakerProfilesResponse response)
    {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(asrEventOwner.getSessionId(), true));

        asrResponseStatus = response.getStatus();

        if (asrEventOwner.isUserResetDone) {
            return;
        }

        var logLine = String.format("Received ASR ForgetMe gRPC response: %s", asrResponseStatus);

        int httpTransCode = asrResponseStatus.getHttpTransCode();
        if (httpTransCode == 200 || httpTransCode == 404)
        { // 404 means that the userId didn't have a speakerProfile.
            logger.debug(logLine);
        } else {
            logger.error(logLine);
        }
    }

    @Override
    public void errorHandler(Throwable e)
    {
        ForgetMeGrpcResponseObserver.errorHandler(e, asrEventOwner);
    }

    @Override
    protected void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode)
    {
        ForgetMeGrpcResponseObserver.completeTransaction(e, responseCode, asrEventOwner);
    }

    @Override
    public void onCompleted()
    {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(asrEventOwner.getSessionId(), true));

        if (asrEventOwner.isUserResetDone)
        {
            return;
        }

        logger.debug("onCompleted()");

        int statusCode = asrResponseStatus.getHttpTransCode();

        if (statusCode == 200 || statusCode == 404)
        { // Success
            asrEventOwner.startLogForgetRequest();
        } else if (statusCode == 400) { // Error: your client app sent a malformed or unsupported request.
            asrEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_MALFORMED_OR_UNSUPPORTED_REQUEST_ERROR);

        } else if (statusCode == 401 || statusCode == 403) { // Error: your client has not authenticated properly or permission was denied.
            asrEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_AUTH_OR_PERMISSION_ERROR);

        } else if (statusCode >= 500 && statusCode <= 511) { // Error: Internal error.
            asrEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_INTERNAL_ERROR);

        } else {
            asrEventOwner.sendErrorResponse(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
        }
    }

    @Override
    public boolean isDone()
    {
        return asrEventOwner.isUserResetDone;
    }

    @Override
    protected String getSessionId()
    {
        return asrEventOwner.getSessionId();
    }
}
