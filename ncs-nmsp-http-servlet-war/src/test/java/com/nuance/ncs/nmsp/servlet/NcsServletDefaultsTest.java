package com.nuance.ncs.nmsp.servlet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NcsServletDefaultsTest {

    @Test(expected = IllegalStateException.class)
    public void construct() {
        new NcsServletDefaults();
    }

    @Test
    public void getClientCallLogEnabledMatchesConstant() {
        assertEquals(NcsServletDefaults.CLIENT_CALL_LOG_ENABLED, NcsServletDefaults.getClientCallLogEnabled());
    }

    @Test
    public void getAuthorizedCrossOriginsMatchesConstant() {
        assertEquals(NcsServletDefaults.AUTHORIZED_CROSS_ORIGINS, NcsServletDefaults.getAuthorizedCrossOrigins());
        assertNotNull(NcsServletDefaults.getAuthorizedCrossOrigins());
    }

    @Test
    public void getAuthorizedIpsMatchesConstant() {
        assertEquals(NcsServletDefaults.AUTHORIZED_IPS, NcsServletDefaults.getAuthorizedIps());
        assertNotNull(NcsServletDefaults.getAuthorizedIps());
    }
}
