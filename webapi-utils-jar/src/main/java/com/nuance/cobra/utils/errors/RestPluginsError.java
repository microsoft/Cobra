package com.nuance.cobra.utils.errors;

import com.nuance.plugin.v1.shareable.IErrorCode;

import javax.servlet.http.HttpServletResponse;


public enum RestPluginsError implements IErrorCode {

    UNKNOWN_ACTION(4001, HttpServletResponse.SC_BAD_REQUEST, "Unknown action in URI.",
            "Unsupported action in URI: %s"),

    JSON_PARSING_ERROR(4002, HttpServletResponse.SC_BAD_REQUEST, "JSON processing error while parsing request.",
            "JSON processing error while parsing request: %s" ),

    MISSING_FIELD(4003, HttpServletResponse.SC_BAD_REQUEST, "Missing mandatory field in request.",
            "Missing mandatory field in request: %s"),

    INVALID_CONTENT_TYPE(4004, HttpServletResponse.SC_BAD_REQUEST,
            "The content-type is invalid. Refer to RFC 2045 for allowable format.",
            "Could not parse content-type: %s"),

    UNSUPPORTED_CONTENT_TYPE(4005, HttpServletResponse.SC_BAD_REQUEST, "Unsupported content-type.",
            "Unsupported content-type (expected: %s, got: %s)"),

    UNSUPPORTED_URI(4006, HttpServletResponse.SC_BAD_REQUEST, "Unexpected URI format for plugin.",
            "Unexpected URI format: %s"),

    INVALID_TENANT_PATH_FORMAT(4009, HttpServletResponse.SC_BAD_REQUEST, "The tenant component in request URI does not match the configured tenant key regexp."),

    FAILED_TO_LOAD_TENANT_CONFIG(4010, HttpServletResponse.SC_NOT_FOUND,
            "Failed to load tenant configuration. Possible causes: tenant in the request URI not found, or an error (e.g. faulty tenant config file) prevents loading it."),

    UNAUTHORIZED(4011, HttpServletResponse.SC_UNAUTHORIZED, "Authentication error."),

    REQUEST_PATH_NOT_FOUND(4013, HttpServletResponse.SC_NOT_FOUND, "No configured plugin matched request path."),

    PLUGIN_NOT_INITIALIZED(5001, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Plugin was not successfully initialized and unable to serve request, please check logs."),

    METHOD_NOT_IMPLEMENTED(4014, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not implemented.",
            "Method not implemented: %s"),

    PROCESSING_ERROR_EXCEPTION(5008, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing request.",
            "Error while processing request: %s");


    private final ErrorCodeDefinition pluginErrorDefinition;


    RestPluginsError(int errorCode, int statusCode, String message, String internalMessageFormat)
    {
        pluginErrorDefinition = new ErrorCodeDefinition(errorCode, Severity.ERROR, message, internalMessageFormat, statusCode);
    }

    RestPluginsError(int errorCode, int statusCode, String message)
    {
        pluginErrorDefinition = new ErrorCodeDefinition(errorCode, Severity.ERROR, message, statusCode);
    }

    @Override
    public int getValue()
    {
        return pluginErrorDefinition.getValue();
    }

    @Override
    public int getHttpStatusCode()
    {
        return pluginErrorDefinition.getHttpStatusCode();
    }

    @Override
    public String getMessage()
    {
        return pluginErrorDefinition.getMessage();
    }

    @Override
    public int getSeverity() {
        return pluginErrorDefinition.getSeverity();
    }

    @Override
    public String getInternalErrorMessage() {
        return pluginErrorDefinition.getInternalErrorMessage();
    }

}
