package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONArray;

public class RequestHeader
{
    @JsonProperty("timestamp")
    public String timeStamp;

    @JsonProperty("seq_num")
    public Integer seqNum;

    @JsonProperty("EDFVersion")
    public String edfVersion;

    @JsonProperty("ClientID")
    public int clientID;

    @JsonProperty("ApplicationID")
    public String applicationID;

    @JsonProperty("Environment")
    public String environment;

    @JsonProperty("ClientApplicationID")
    public String clientApplicationID;

    @JsonProperty("ClientApplicationVersion")
    public String clientApplicationVersion;

    @JsonIgnore
    @JsonProperty("CustomerIDs")
    public JSONArray customerIDs;

    @JsonIgnore
    @JsonProperty("CustomerIDsOld")
    public JSONArray customerIDsOld;

    @JsonProperty("Channel")
    public String channel;

    @JsonProperty("Tenant")
    public String tenant;

    @JsonProperty("SessionStartTime")
    public String sessionStartTime;

    public RequestHeader()
    {

    }
}
