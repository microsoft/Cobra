package com.nuance.cobra.client.mix.grpc;

import io.grpc.Metadata;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HeaderClientInterceptorTest {

    @Test
    public void testInterceptCall() {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-client-request-id", "fakeSessionId");
        HeaderClientInterceptor headerClientInterceptor = new HeaderClientInterceptor(customHeaders);
        Metadata headers = new Metadata();
        headerClientInterceptor.insertCustomHeaders(headers);

        assertEquals("Metadata(x-client-request-id=fakeSessionId)", headers.toString());
    }
}