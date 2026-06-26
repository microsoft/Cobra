package com.nuance.plugin.kryptonws.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.utils.webapi.WebApiResponse;

import java.util.List;
import java.util.Map;

public class KryptonWsMessage
{

    public enum COMPLETION_CAUSE {

        // possible causes "SUCCCESS|ERROR|NO-INPUT-TIMEOUT| RECOGNITION-TIMEOUT|END-OF-STREAM|NO-MATCH"
        SUCCESS("SUCCESS"),
        ERROR("ERROR"),
        NO_INPUT_TIMEOUT("NO-INPUT-TIMEOUT"),
        RECOGNITION_TIMEOUT("RECOGNITION-TIMEOUT"),
        END_OF_STREAM("END-OF-STREAM"),
        NO_MATCH("NO-MATCH");

        public final String cause;

        COMPLETION_CAUSE(String cause) {
            this.cause = cause;
        }

        static COMPLETION_CAUSE fromResponse(WebApiResponse response){
            switch (((String)response.result.get("status")).toLowerCase()){
                case "error":
                    return ERROR;
                case "cancelled":
                    return END_OF_STREAM;
                case "noinput":
                    return NO_INPUT_TIMEOUT;
                case "nomatch":
                    return NO_MATCH;
                case "timeout": // i don't think webapi sends this
                    return RECOGNITION_TIMEOUT;
                case "success":
                default:
                    return SUCCESS;
            }
        }
    }

    public enum STATUS {

        COMPLETE("complete"),
        IN_PROGRESS("in-progress"),
        FAILED("failed"),
        NO_ACTION("no-action");

        public final String statusString;

        STATUS(String stat) {
            this.statusString = stat;
        }

    }

    protected static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("method")
    String method;

    @JsonProperty("version")
    String version;

    @JsonProperty("sessionId")
    String sessionId;

    @JsonProperty("requestId")
    String requestId;

    @JsonProperty("interpretationData")
    Map<String, Object> interpretationData;

    @JsonProperty("interpretationMetrics")
    Map<String, Object> interpretationMetrics;

    @JsonProperty("interpretationStatus")
    String interpretationStatus;

    @JsonProperty("interpretationErrors")
    List<Map<String, Object>> interpretationErrors;
}
