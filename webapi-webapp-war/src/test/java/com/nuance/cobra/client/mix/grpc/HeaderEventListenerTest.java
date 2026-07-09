package com.nuance.cobra.client.mix.grpc;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HeaderEventListenerTest {

    public static class TestListener implements HeaderEventListener.HeaderListener {
        Map<String, Object> resultHeader;

        public Map<String, Object> getResultHeader() {
            return resultHeader;
        }

        @Override
        public void newHeader(Map<String, Object> header) {
            resultHeader = header;
        }
    }

    @Test
    public void setListener() {
        TestListener listener = new TestListener();

        Map<String, Object> testHeader = new HashMap<>();
        testHeader.put("test", "value");

        HeaderEventListener testListener = new HeaderEventListener();
        testListener.setListener(listener);

        testListener.newHeader(testHeader);

        assertEquals(testHeader, listener.getResultHeader());
    }
}