package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class BCPBeginSerializerRegistryTest {

    @Test
    public void testGetBCPBeginSerializer() {
        assertNull(BCPBeginSerializerRegistry.getBCPBeginSerializer((short) 1));
    }
}