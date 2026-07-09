package com.nuance.cobra.utils.metrics;

/***
 * Defined constants for metrics.
 */
public class MetricsConstants
{
    MetricsConstants() { throw new IllegalStateException("Utility class!"); }

    // The following can be used as keys to store and retrieve the corresponding value in request/session/etc attributes.
    // They can be used to collect
    public static final String FUNCTION_LABEL_ATTRIBUTE_KEY = "com.nuance.metrics.function_label";
    public static final String ACTION_LABEL_ATTRIBUTE_KEY = "com.nuance.metrics.action_label";
    public static final String TENANT_LABEL_ATTRIBUTE_KEY = "com.nuance.metrics.tenant_label";
    public static final String COMMAND_LABEL_ATTRIBUTE_KEY = "com.nuance.metrics.command_label";

    // Label value to use whent he value could not be determined (e.g. tenant/action/command does not exist)
    public static final String UNDEFINED_LABEL_VALUE = "<undefined>";

    // identifies the tenant (e.g. companyName_appName)
    public static final String TENANT_LABEL = "tenant";

    // identifies the function (e.g. recommendation-engine, logging-service)
    public static final String FUNCTION_LABEL = "function";

    // identifies the command on the websockets API
    public static final String COMMAND_LABEL = "command";

    // identifies the action in pseudo-REST APIs. (e.g. action in http://<endpoint>/<path>/<action>)
    public static final String API_ACTION_LABEL = "action";

    // identifies the HTTP method
    public static final String HTTP_METHOD_LABEL = "method";

    // identifies the resource, in true REST APIs. (e.g. http://<endpoint>/<path>/<resource>)
    public static final String API_RESOURCE_LABEL = "resource";

    // identifies status code for HTTP or other APIs
    public static final String STATUS_CODE_LABEL = "status_code";

    // identifies the engine
    public static final String ENGINE_LABEL = "engine";

    // identifies the datapack
    public static final String DATAPACK_LABEL = "datapack";

    // identifies the voice
    public static final String VOICE_LABEL = "voice";

    // identifies the language
    public static final String LANGUAGE_LABEL = "language";

    // error_code label
    public static final String ERROR_CODE_LABEL = "error_code";


    /**
     * Return the label if it is not null, or the predefined constant for undefined label values otherwise.
     */
    public static String nonNullLabelValue(String labelValue) {
        return (labelValue != null) ? labelValue : UNDEFINED_LABEL_VALUE;
    }
}
