package com.nuance.plugin.backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.plugin.backend.HttpMonitoringPlugin;

import java.util.Map;

public class MonitoringResponse {
    @JsonProperty("logTrackId")
    private final String trackingId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("format")
    private final String version = HttpMonitoringPlugin.FORMAT_VERSION;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class StatisticsHostInfo
    {
        @JsonProperty("region")
        public String region;

        @JsonProperty("environment")
        public String environment;

        @JsonProperty("version")
        public String version;
    }

    @JsonProperty("hostInfo")
    private StatisticsHostInfo hostInfo;

    @JsonProperty("services")
    private Map<String, ServiceResponse> services = null;

    public MonitoringResponse(String trackingID) {
        this.trackingId = trackingID;
    }

    public String toJson() throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        return mapper.writeValueAsString(this);
    }

    public void setServices(Map<String, ServiceResponse> services) {
        this.services = services;
    }

    public void setStatisticsHostInfo(String region, String environment, String version) {
        if (region != null ||
                environment != null ||
                version != null) {
            hostInfo = new StatisticsHostInfo();

            hostInfo.region = region;
            hostInfo.environment = environment;
            hostInfo.version = version;
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
