package com.nuance.cobra.api.response;

import javax.annotation.Nullable;

/**
 * @api {display_none} display_none Internal Error codes
 * @apiName InternalResponseErrorCode
 * @apiGroup Errors
 * @apiVersion ${project.version}
 * @apiDescription This is the list of error codes which are internal to the application and not being sent to client, for all commands. Error codes and error messages are found in the error object of a result response,
 * for example "errors": [ {"code": 1908, "message": "Global registry malformed url."} ]
 * @apiError (Errors) 1908 Global registry malformed URL.
 * @apiError (Errors) 1909 Resource database can not be fetched.
 * @apiError (Errors) 1910 Resource database not valid.
 * @apiError (Errors) 1911 Global registry unknown exception.
 * @apiError (Errors) 2001 The monitoring service failed to establish connection with one or more of the back ends.
 * @apiError (Errors) 2100 Invalid log posting endpoint found in tenant configuration.
 * @apiError (Errors) 2101 Unable to post to EDF.
 * @apiError (Errors) 2102 Invalid project ID found in tenant configuration.
 * @apiError (Errors) 2103 Invalid client ID found in tenant configuration.
 * @apiError (Errors) 2104 EDF Message queue restart
 *
 */
@java.lang.SuppressWarnings({"java:S107", "java:S1192"})
public enum InternalResponseErrorCode
        implements ResponseErrorCode
{
    //Severity is based on NAGIOS severity:
    // 0: OK
    // 1: WARNING
    // 2: ERROR
    // 3: UNKNOWN

    GLOBAL_REGISTRY_URL_MALFORMED(1908, "Global registry malformed URL.",
            "Global registry URL is malformed, please configure correct host and port values in webapi config file",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Couldn't reach the global registry.",
            "2.0",
            "Global Registry Configuration",
            "Check webapi logs and server config. Validate that global registry is available and reachable.",
           true),
    GLOBAL_REGISTRY_CANNOT_BE_FETCHED(1909, "Resource database can not be fetched",
            "Global registry can not be fetched due to connection/read timeout",
            "If > 50 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Couldn't get information from the global registry.",
            "2.0",
            "Global registry is down/ Server connection issue/ Read time out",
            "Check webapi/global registry logs.",
           false),
    GLOBAL_REGISTRY_CANNOT_BE_PARSED(1910, "Resource database not valid",
            "Global registry can not be parsed due to improper format",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Couldn't get information from the global registry.",
            "2.0",
            "Global registry data",
            "Check webapi/global registry logs.",
           true),
    GLOBAL_REGISTRY_UNKNOWN_EXCEPTION(1911, "Global registry unknown exception",
            "Global registry unknown exception",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Failed to get information from the global registry.",
            "2.0",
            "TBD",
            "Check webapi/global registry logs.",
           false),
    GLOBAL_REGISTRY_HTTP_RESPONSE_ERROR(1912, "Global registry http response error",
            "Global registry http response error: [%s] [%s]",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Failed to get information from the global registry.",
            "2.6",
            "WebAPI",
            "Check webapi logs and server configuration. Check global registry logs.",
           false),

    MONITORING_CHECK_FAILED(2001, "Monitoring failed for service: [%s] of type: [%s]",
            "The monitoring service failed to establish connection with one or more of the back ends.",
            "Any (> 0) alarms outside of maintenance windows should be investigated. Check monitored address(es) in webapi/config/services.json.",
            2,
            "A service that webapi depends on is unreachable.",
            "1.5.2",
            "Monitoring service",
            "Any (> 0) alarms outside of maintenance windows should be investigated. Check monitored address(es) in webapi/config/services.json.",
           false),

    DISK_USAGE_LIMIT_REACHED(2002, "Disk is full.", "Not able to dump audio and EDF binary data may be lost",
            "Any (> 0) alarms should be investigated.",
            1,
            "Disk is full.",
            "",
            "WebAPI writes audio received to disk for logging",
            "",
           true),
    DISK_USAGE_VALID(2003, "Disk usage limit now valid", "Now continuing to dump audio and EDF binary data.",
            "Never sent.",
            2,
            "Deprecated",
            "",
            "",
            "",
           false),

    INVALID_LOG_POSTING_ENDPOINT(2100, "Invalid log posting endpoint found in tenant configuration.",
            "Tenant Configuration does not contain, or contains a malformed, url endpoint for either the data or binary EDF API.",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            2,
            "Webapi couldn't log activity.",
            "2.0",
            "Tenant Configuration",
            "Validate tenant configuration. Check for edf endpoint configuration.",
           true),
    EDF_LOG_POSTING_FAILURE(2101, "Unable to post to EDF.",
            "WAPI failed to post a log to EDF (received status code other than 200 or 302/307)",
            "If > 50 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Webapi couldn't log activity.",
            "2.0",
            "Logging",
            "Check webapi logs.",
           true),
    INVALID_PROJECT_ID(2102, "Unable to post to EDF.", "Invalid project ID found in tenant configuration.",
            "If > 50 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Deprecated.",
            "2.0",
            "Tenant Configuration",
            "Check webapi logs. Validate tenant configuration.",
           true),
    INVALID_CLIENT_ID(2103,"Unable to post to EDF.", "Invalid client ID found in tenant configuration.",
            "If > 50 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            1,
            "Logging endpoint improperly configured.",
            "2.0",
            "Tenant Configuration",
            "Check webapi logs. Validate tenant configuration.",
           true),
    MESSAGE_QUEUE_RESTART(2104,"The messaging queue is restarting due to an exception",
            "Possible corruption of the KahaDB. Please check the webapi logs for this instance.",
            "If > 50 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            2,
            "Webapi couldn't log activity.",
            "2.1.0",
            "/usr/local/Nuance/webapi/nina-webapi-base-n/activemq-data",
            "Check webapi logs.",
           true),

    FLUENTBIT_LOG_POSTING_FAILED(2200, "Failed posting logs to fluentbit", "Failed posting logs to fluentbit",
            "Never sent.",
            2,
            "Deprecated",
            "",
            "",
            "",
           false),
    FLUENTBIT_LOG_POSTING_ERROR(2201, "Error while posting logs to fluentbit",
            "An exception occurred while posting logs to fluentbit: [%s]",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            2,
            "Webapi couldn't log activity.",
            "",
            "Logging",
            "Check webapi logs.",
           true),
    FLUENTBIT_NETWORK_ERROR(2202, "Network/Connection failure while posting logs to fluentbit",
            "Network/Connection failure while posting logs to fluentbit: [%s], however fluentbit may retry sending the logs",
            "Never sent.",
            1,
            "Deprecated",
            "",
            "",
            "",
            false),
    FLUENTBIT_JSON_ERROR(2203, "Error while posting logs to fluentbit",
            "An exception occurred while posting logs to fluentbit: [%s]",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            2,
            "Webapi couldn't log activity.",
            "",
            "Logging",
            "Check webapi logs.",
            false),
    EVENTHUB_POST_ERROR(2204, "Error while posting logs to eventhub",
            "An exception occurred while posting logs to eventhub: [%s]",
            "If > 25 per hour for a customer, check error messages in webapi diagnostic logs and report back to SolArch/R&D.",
            2,
            "Webapi couldn't log activity.",
            "",
            "Logging",
            "Check webapi logs.",
            false);

    private final int code;
    private final String message;
    private final String internalErrorMessage;
    private final String documentation;
    private final int severity;
    private final String nocRecommendation;
    private final String troubleshootTips;
    private final String area;
    private final String version;
    private final boolean isPotentialSecurityThreat;

    InternalResponseErrorCode(int code, String message, String internalErrorMessage, String nocRecommendation, int severity, String documentation, String version, String area, String troubleshootTips, boolean isPotentialSecurityThreat) {
        this.code = code;
        this.severity = severity;
        this.message = message;
        this.internalErrorMessage = internalErrorMessage;
        this.documentation = documentation;
        this.nocRecommendation = nocRecommendation;
        this.area = area;
        this.version = version;
        this.troubleshootTips = troubleshootTips;
        this.isPotentialSecurityThreat = isPotentialSecurityThreat;
    }

    public int getValue() { return code; }

    public String getMessage() { return message; }

    public int getSeverity() { return severity; }

    public String getInternalErrorMessage() { return internalErrorMessage; }

    @Nullable
    @Override
    public String getNOCRecommendation() { return nocRecommendation; }

    @Nullable
    @Override
    public String getTroubleshoot() { return troubleshootTips; }

    @Nullable
    @Override
    public String getAreaOfIssue() { return area; }

    @Nullable
    @Override
    public String getVersion() { return version; }

    @Nullable
    @Override
    public String getDocumentation() { return documentation; }

    @Override
    public boolean isPotentialSecurityThreat() {
        return isPotentialSecurityThreat;
    }
}
