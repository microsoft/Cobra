package com.nuance.plugin.backend.v1.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class RequestJson {

    @JsonProperty("generalConfig")
    public Map<String, Object> generalConfig = new HashMap<>();

    @JsonProperty("input")
    public Map<String, Object> input = new HashMap<>();

    @JsonProperty("voice")
    public Map<String, Object> voice = new HashMap<>();

    @JsonProperty("audioConfig")
    public Map<String, Object> audioConfig = new HashMap<>();

    @JsonProperty("clientMetaDataConfig")
    public Map<String, Object> clientMetaDataConfig = new HashMap<>();

    public static RequestJson fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return mapper.readValue(json, RequestJson.class);
    }

    public static RequestJson fromMap(Map<String, Object> json) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(json, RequestJson.class);
    }
}
