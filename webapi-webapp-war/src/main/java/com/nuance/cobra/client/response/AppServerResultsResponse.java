package com.nuance.cobra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.api.response.Views;
import org.json.JSONObject;

import java.util.Map;

public class AppServerResultsResponse
        extends ResponseResult
{
    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("appserver_results")
    private Map<String, Object> appServerResultsMap;

    public AppServerResultsResponse(ResponseStatus status, JSONObject appServerResultsJson, boolean isFinal)
    {
        super(status, isFinal);
        this.appServerResultsMap = appServerResultsJson.toMap();
    }
}
