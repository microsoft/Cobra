package com.nuance.cobra.utils.metrics;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricsConstantsTest {

    @Test
    public void testNonNullLabelValue() {

        final String VALUE = "value";

        assertEquals(VALUE, MetricsConstants.nonNullLabelValue(VALUE));
        assertEquals(MetricsConstants.UNDEFINED_LABEL_VALUE, MetricsConstants.nonNullLabelValue(null));
    }

}