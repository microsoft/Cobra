package com.nuance.cobra.testing;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MetricsVerifier extends AbstractMetricsVerifier {

    @Override
    public void doMatch(Map<String, String> labelValuePair, double actualVal, double expectedVal) {
        assertTrue(expectedVal <= actualVal);
    }
}