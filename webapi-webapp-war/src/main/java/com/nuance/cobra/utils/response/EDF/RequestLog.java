package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.command.Command;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/*
Created as a result of feedback from the EDF team
 */
public class RequestLog
{
    @JsonProperty("sessionID")
    public String sessionId;

    @JsonProperty("Command")
    public String command;

    @JsonProperty("stepID")
    public int stepID;

    @JsonProperty("outcome")
    public String outcome;

    @JsonProperty("responseCode")
    public String responseCode;

    @JsonProperty("httpEquivalentStatusCode")
    public String httpEquivalentStatusCode;

    @JsonProperty("sensitive")
    public String sensitivity;

    @JsonProperty("params")
    public Map<java.lang.String, Object> params = new HashedMap();

    @JsonProperty("userInfo")
    public UserInfo userInfo;

    // requestLog deserialization currently broken due to static inner class (Command Statistics),
    // CommandStatistics must be ignored
    @JsonIgnore
    public Command.CommandStatistics statistics;

    @JsonIgnore
    public void setStatistics(Command.CommandStatistics statistics)
    {
        this.statistics = statistics;
    }

    @JsonProperty("statistics")
    public Command.CommandStatistics getStatistics() {
        return statistics;
    }

    @JsonDeserialize(as = Response.class)
    @JsonProperty("response")
    public IResponse response;

    @JsonProperty("fileName")
    public String data = null;


    public String toJson(boolean prettyPrint) throws JsonProcessingException
    {
        if (prettyPrint)
        {
            return Command.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        else
        {

            return Command.getMapper().writer().writeValueAsString(this);
        }
    }
}
