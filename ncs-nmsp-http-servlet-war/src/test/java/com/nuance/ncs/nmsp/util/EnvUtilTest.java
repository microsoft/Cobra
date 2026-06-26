package com.nuance.ncs.nmsp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvUtilTest {

    @Test
    public void getOrDefault() {
        assertEquals("test", EnvUtil.useDefault("notUsed", "test"));
        assertEquals("test", EnvUtil.getOrDefault("notUsed", "test"));

    }
}