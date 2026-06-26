package com.nuance.cobra.testing;

import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class MetricsEndpointHelper extends HTTPServer {

    MetricsEndpointHelper() throws IOException {
        super(0);
    }

    int getBoundPort() {
        return server != null ? server.getAddress().getPort() : 0;
    }

    public static Pattern getPattern(String name, String[] labels) {
        return Pattern.compile(getPatternString(name, labels));
    }

    public static String getMetricsResponse() throws IOException, InterruptedException {
        HttpResponse<String> response = getMetricsHttpResponse();
        return (response != null) ? response.body() : "";
    }

    private static String getPatternString(String name, String[] labels) {
        String fpNumberRegex = "([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";

        return name + ".*" +
                String.join("=\"([^\"]*).*", labels) +
                "=\"([^\"]*)\".*}\\D+" +
                fpNumberRegex;
    }

    //For testing purposes
    private static HttpClient client;
    static void setClient(HttpClient client) {
        MetricsEndpointHelper.client = client;
    }

    private static HttpResponse<String> getMetricsHttpResponse() throws IOException, InterruptedException
    {
        HttpRequest request;
        try (MetricsEndpointHelper server = new MetricsEndpointHelper())
        {
            if (client == null)
            {
                client = HttpClient.newHttpClient();
            }

            request = HttpRequest.newBuilder()
                                 .uri(URI.create("http://localhost:" + server.getBoundPort()))
                                 .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}
