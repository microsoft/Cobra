package com.nuance.cobra.testing;

import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsEndpointHelperTest {

    @Test
    public void getBoundPortTest() throws IOException {
        MetricsEndpointHelper metricsEndpointHelper = new MetricsEndpointHelper();
        int port = metricsEndpointHelper.getBoundPort();
        assertTrue(port >= 0);
    }

    @Test
    public void getPatternTest() {
        Pattern pattern = MetricsEndpointHelper.getPattern("test_pattern",
                new String[]{"label_one", "label_two"});
        String expected = "test_pattern.*label_one=\"([^\"]*).*label_two=\"([^\"]*)\".*}\\D+([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
        assertEquals(expected, pattern.toString());
    }

    @Test
    public void getMetricsResponse() throws IOException, InterruptedException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("mock response body");
        MetricsEndpointHelper.setClient(mockClient);
        String response = MetricsEndpointHelper.getMetricsResponse();
        assertEquals("mock response body", response);
    }
}
