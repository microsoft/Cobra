package com.nuance.cobra.utils.response;

public class HttpAlarmSenderRequestFailedException extends HttpAlarmSenderException {

    private int statusCode;

    public HttpAlarmSenderRequestFailedException(int statusCode) {
        super("Alarm over HTTP failed with response code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
