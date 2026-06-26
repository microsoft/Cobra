package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.mix.MixUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgetMeGrpcResponseObserver
{
    protected static final Logger logger =  LogManager.getLogger(ForgetMeGrpcResponseObserver.class);

    ForgetMeGrpcResponseObserver() { throw new IllegalStateException("Utility class!"); }

    public static void errorHandler(Throwable e, ForgetMeGrpcClient eventOwner)
    {
        logger.debug("onError()", e);
        completeTransaction(e, ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR, eventOwner);
    }

    protected static void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode, ForgetMeGrpcClient eventOwner)
    {
        MixUtils.handleMixThrowable(logger, ForgetMeGrpcResponseObserver.class.getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
        MixUtils.invalidateTokenIfUnauthorized(logger, e, eventOwner.getCobraConfig());

        eventOwner.sendErrorResponse(responseCode);
    }

}
