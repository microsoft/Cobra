package com.nuance.cobra.utils.response;

public class HttpAlarmSenderException extends Exception {

    public HttpAlarmSenderException (String message) {
        super(message);
    }

    public HttpAlarmSenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
