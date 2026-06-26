package com.nuance.cobra.api.response;

public enum InternalGenericResponseCode
    implements GenericResponseCode
{
    LOG_POSTING_RESUMED(3000, 0, "EDF Log Posting Resumed.",
            "Disk is not full, log poster resuming successfully.");

    private final int code;
    private final String message;
    private final String internalErrorMessage;
    private final int severity;

    InternalGenericResponseCode(int code, int severity, String message, String internalErrorMessage)
    {
        this.code = code;
        this.severity = severity;
        this.message = message;
        this.internalErrorMessage = internalErrorMessage;
    }

    public int getValue()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    public int getSeverity()
    {
        return severity;
    }

    public String getInternalErrorMessage()
    {
        return internalErrorMessage;
    }

}
