package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IErrorCode;


/**
 * Used to report an error condition associated with a documented error code in Cobra.
 *
 * Can be used by both base Cobra and plugins. Simpler than Cobra exception, which can carry a number of errors.
 * (but supports cause argument to capture and exception chain.)
 */
public class WebApiCommonException
        extends Exception
{
    private final int httpStatusCode;
    private final int errorCodeValue;


    public WebApiCommonException(IErrorCode error, Throwable cause, Object... args) {
        super(IErrorCode.formatInternalMessage(error, args), cause);
        this.httpStatusCode = error.getHttpStatusCode();
        this.errorCodeValue = error.getValue();
    }

    public WebApiCommonException(IErrorCode error, Object... args) {
        super(IErrorCode.formatInternalMessage(error, args));
        this.httpStatusCode = error.getHttpStatusCode();
        this.errorCodeValue = error.getValue();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public int getErrorCodeValue() {
        return errorCodeValue;
    }
}