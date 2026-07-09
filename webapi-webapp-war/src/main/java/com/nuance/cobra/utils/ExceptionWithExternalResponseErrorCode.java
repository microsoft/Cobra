package com.nuance.cobra.utils;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;

public class ExceptionWithExternalResponseErrorCode extends Exception {

    private ExternalResponseErrorCode errorCode;

    public ExceptionWithExternalResponseErrorCode(String message, ExternalResponseErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExceptionWithExternalResponseErrorCode(String message, Throwable cause, ExternalResponseErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ExternalResponseErrorCode getErrorCode() {
        return errorCode;
    }
}
