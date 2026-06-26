package com.nuance.cobra.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ToolsTest {

    @Test
    public void getJettyBase() {
        assertTrue(Tools.getJettyPath().contains("webapi-utils-jar"));
    }

    @Test
    public void reMap() {
        double result = Tools.reMap(100, 0, 1000, 0, 1);
        assertEquals(0.1, result, 0.1);
    }
}