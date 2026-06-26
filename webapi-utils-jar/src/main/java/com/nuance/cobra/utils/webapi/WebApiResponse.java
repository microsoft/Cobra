package com.nuance.cobra.utils.webapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebApiResponse {
    @JsonProperty("type")
    public String type;

    @JsonProperty("sessionId")
    public String sessionId;

    @JsonProperty("command")
    public HashedMap command;

    @JsonProperty("result")
    public HashedMap result;

    @JsonProperty("event")
    public HashedMap event;

    @JsonProperty("directive")
    public HashedMap directive;

    public static WebApiResponse fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, WebApiResponse.class);
    }

    public boolean isFinal() {
        if (result != null) {
            return (Boolean) result.get("final");
        } else {
            return false;
        }
    }

    public boolean isSuccess() {
        if (result != null) {
            return ((String) result.get("status")).equalsIgnoreCase("success");
        } else {
            return false;
        }
    }

    public boolean isError() {
        if (result != null) {
            return ((String) result.get("status")).equalsIgnoreCase("error");
        } else {
            return false;
        }
    }

    public boolean isCanceled() {
        if (result != null) {
            return ((String) result.get("status")).equalsIgnoreCase("cancelled");
        } else {
            return false;
        }
    }

    public boolean isNoInput() {
        if (result != null) {
            return ((String) result.get("status")).equalsIgnoreCase("noInput");
        } else {
            return false;
        }
    }

    public boolean isNoMatch() {
        if (result != null) {
            return ((String) result.get("status")).equalsIgnoreCase("noMatch");
        } else {
            return false;
        }
    }

    public boolean isEvent(String eventName) {
        if (type.equalsIgnoreCase("event") && (event != null)) {
            return eventName.equalsIgnoreCase((String) event.get("name"));
        } else {
            return false;
        }
    }

    public boolean isForCommand(String commandName) {
        return command.getOrDefault("name", "NAN").equals(commandName);
    }

    public boolean isResult() {
        return type.equalsIgnoreCase("result");
    }

    public boolean isChainResult() {
        return type.equalsIgnoreCase("commandChainResult");
    }

    public String getErrorsAsString() {
        StringBuilder response = new StringBuilder();

        if (isSuccess()) {
            return null;
        }

        for (HashMap<String, Object> error : ((ArrayList<HashMap<String, Object>>) result.get("errors"))) {
            int code = (int) error.get("code");
            response.append(code).append(": ");
            response.append((String)error.get("message")).append(" ");
        }

        return result.toString();
    }

    public List<Map<String, Object>> getErrors()
    {
        return (List<Map<String, Object>>) result.get("errors");
    }

    public JSONObject getResult() {
        return new JSONObject(result);
    }

    public boolean hasVerboseResult() {
        return getResult().optJSONObject("verboseResult") != null;
    }

    public JSONObject getVerboseResult() {
        return getResult().getJSONObject("verboseResult");
    }

    public JSONArray getInterpretations() {
        return getResult().optJSONArray("interpretations");
    }
}
