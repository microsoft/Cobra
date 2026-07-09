package com.nuance.cobra.utils.response;

public class HttpAlarmSenderConfigException extends HttpAlarmSenderException {

    public HttpAlarmSenderConfigException(String message, String paramName) {
        super(buildMessage(message, paramName));
    }

    public HttpAlarmSenderConfigException(String message,  String paramName, Throwable cause) {
        super(buildMessage(message, paramName), cause);
    }

    private static String buildMessage(String message, String paramName) {
        return message + " Check parameter \'" + paramName + "\'.";
    }
}
