package com.nuance.plugin.kryptonws.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KryptonWsResponse
        extends KryptonWsMessage
{

    private static Logger logger = LogManager.getLogger(KryptonWsResponse.class);

    @JsonProperty("responseTo")
    String responseTo;

    @JsonProperty("status")
    String status;

    @JsonProperty("completionCause")
    String cause;

    @JsonProperty("errors")
    List<Map<String, String>> errors;

    @JsonProperty("sessionObjects")
    List<Object> sessionObjects;

    @JsonProperty("metrics")
    Map<String, Object> metrics;

    @JsonProperty("objects")
    List<Object> objects;

    @JsonProperty("clientData")
    Map<String, Object> clientData;

    @JsonProperty("data")
    Map<String, Object> data;

    KryptonWsResponse(){}

    public String toJson()
    {
        String rc = "";
        try
        {
            rc = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e)
        {
            logger.warn("Caught JsonProcessing exception while converting KryptonWsResponse: ", e);
        }
        return rc;
    }
}
