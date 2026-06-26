package com.nuance.cobra.utils.errors;

import com.nuance.plugin.v1.shareable.IErrorCode;

/**
 * Used to implement error codes. Error codes are defined in enums and implement the IErrorCode interface.
 * Error codes can be grouped in error code collections (e.g. common error codes, REST plugins error codes,
 * WebApi websockets API error codes). One enum (implementing IErrorCode) is defined for each collection.
 * This class can be used for the implementation of an error codes collection.
 */
public class ErrorCodeDefinition implements IErrorCode {

    private final int errorCode;
    private final int statusCode;
    private final String message;
    private final Severity severity;
    private final String internalMessageFormat;
    private final String documentation;

    public ErrorCodeDefinition(int errorCode, Severity severity, String message, String internalMessageFormat, int statusCode, String documentation) {
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.message = message;
        this.internalMessageFormat = internalMessageFormat;
        this.severity = severity;
        this.documentation = documentation;
    }

    public ErrorCodeDefinition(int errorCode, Severity severity, String message, String internalMessageFormat, int statusCode) {
        this(errorCode, severity, message, internalMessageFormat, statusCode, null);
    }

    public ErrorCodeDefinition(int errorCode, Severity severity, String message, int statusCode) {
        this(errorCode, severity, message, null, statusCode);
    }

    public ErrorCodeDefinition(int errorCode, Severity severity, String message, String internalMessageFormat) {
        this(errorCode, severity, message, internalMessageFormat, 0);
    }

    public ErrorCodeDefinition(int errorCode, Severity severity, String message) {
        this(errorCode, severity, message, null, 0);
    }

    @Override
    public int getValue() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getSeverity() {
        return severity.getIntValue();
    }

    @Override
    public String getInternalErrorMessage() {
        return internalMessageFormat;
    }

    @Override
    public int getHttpStatusCode() {
        return statusCode;
    }
}
