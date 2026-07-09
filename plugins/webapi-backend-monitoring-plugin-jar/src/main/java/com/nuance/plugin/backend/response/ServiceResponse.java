package com.nuance.plugin.backend.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceResponse {
    @JsonProperty("status")
    private String status = null;

    @JsonProperty("details")
    private String details = null;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatistics(String statistics) {
        this.details = statistics;
    }
}
