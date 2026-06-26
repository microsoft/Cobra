package com.nuance.plugin.kryptonws.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KryptonWsEvent
        extends KryptonWsMessage
{
    private static Logger logger = LogManager.getLogger(KryptonWsEvent.class);

    public enum EVENT{
        END_OF_INPUT("StartOfSpeech"),
        PARTIAL_RESULT("PartialResult"),
        FINAL_RESULT("FinalResult"),
        END_OF_SESSION("EndOfSession"),
        START_OF_SPEECH("StartOfSpeech");

        public final String eventString;

        EVENT(String eventString){
            this.eventString = eventString;
        }
    }

    @JsonProperty("event")
    String event;

    @JsonProperty("reason")
    String reason;

    @JsonProperty("metrics")
    Map<String, Object> metrics;

    @JsonProperty("completion_cause")
    COMPLETION_CAUSE completionCause;

    @JsonProperty("data")
    Map<String, Object> data;

    KryptonWsEvent(){}

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
