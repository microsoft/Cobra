package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.rpc.Status;
import com.nuance.rpc.StatusCode;
import com.nuance.rpc.fabric.forget.v1.ForgetResponse;

class LogForgetMeGrpcResponseObserver
        extends ResponseObserver<ForgetResponse> {
    protected Status logResponseStatus;
    private final ForgetMeGrpcClient logEventOwner;

    public LogForgetMeGrpcResponseObserver(ForgetMeGrpcClient logEventOwner) {
        this.logEventOwner = logEventOwner;
    }

    @Override
    public void onNext(ForgetResponse response)
    {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(logEventOwner.getSessionId(), true));

        logResponseStatus = response.getStatus();

        if (logEventOwner.isUserResetDone) {
            return;
        }

        var logLine = String.format("Received Log ForgetMe gRPC response: %s", logResponseStatus);

        StatusCode statusCode = logResponseStatus.getStatusCode();
        if (statusCode == StatusCode.OK)
        {
            logger.debug(logLine);
        } else
        {
            logger.error(logLine);
        }
    }

    @Override
    public void errorHandler(Throwable e)
    {
        ForgetMeGrpcResponseObserver.errorHandler(e, logEventOwner);
    }

    @Override
    protected void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode)
    {
        ForgetMeGrpcResponseObserver.completeTransaction(e, responseCode, logEventOwner);
    }

    @Override
    public void onCompleted()
    {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(logEventOwner.getSessionId(), true));

        if (logEventOwner.isUserResetDone)
        {
            return;
        }

        logger.debug("onCompleted()");

        StatusCode statusCode = logResponseStatus.getStatusCode();
        if(statusCode == StatusCode.OK)
        { // Success
            var response = new ResponseResult();
            logEventOwner.sendResponse(response);
        } else if (statusCode == StatusCode.INVALID_REQUEST)
        { // Error: your client app sent a malformed or unsupported request.
            logEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_MALFORMED_OR_UNSUPPORTED_REQUEST_ERROR);
        } else if (statusCode == StatusCode.NOT_AUTHORIZED || statusCode == StatusCode.PERMISSION_DENIED)
        { // Error: your client has not authenticated properly or permission was denied.
            logEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_AUTH_OR_PERMISSION_ERROR);
        } else if (statusCode == StatusCode.INTERNAL_ERROR)
        { // Error: Internal error.
            logEventOwner.sendErrorResponse(ExternalResponseErrorCode.FORGETME_GRPC_INTERNAL_ERROR);
        } else
        {
            logEventOwner.sendErrorResponse(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
        }
    }

    @Override
    public boolean isDone()
    {
        return logEventOwner.isUserResetDone;
    }

    @Override
    protected String getSessionId()
    {
        return logEventOwner.getSessionId();
    }
}
