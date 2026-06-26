package com.nuance.plugin.backend.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.plugin.backend.HttpMonitoringPlugin;
import com.nuance.plugin.backend.response.MonitoringResponse.StatisticsHostInfo;
import org.junit.Test;

import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MonitoringResponseTest {

    @Test
    public void toJson() throws JsonProcessingException {
        String logTrackId = "123456789";
        String status = "Testing";
        String version  = HttpMonitoringPlugin.FORMAT_VERSION;
        String region = "Canada";
        String environment = "Environment Test";

        ServiceResponse serviceResponse = new ServiceResponse();
        Map<String, ServiceResponse> services = new HashMap<>();
        serviceResponse.setStatus("test");
        services.put("test", serviceResponse);

        MonitoringResponse monitoringResponseTest = new MonitoringResponse(logTrackId);

        monitoringResponseTest.setServices(services);

        monitoringResponseTest.setStatus(status);

        monitoringResponseTest.setStatisticsHostInfo(region, environment, version);

        assertEquals(("{" +
                        "\"logTrackId\":\"123456789\"," +
                        "\"status\":\"Testing\"," +
                        "\"format\":\"1.0\"," +
                        "\"hostInfo\":{" +
                        "\"region\":\"Canada\","+
                        "\"environment\":\"Environment Test\","+
                        "\"version\":\"1.0\""+
                        "},"+
                        "\"services\":{" +
                        "\"test\":{" +
                        "\"status\":\"test\"" +
                        "}}}\n").trim(), monitoringResponseTest.toJson()
                    );

    }
}