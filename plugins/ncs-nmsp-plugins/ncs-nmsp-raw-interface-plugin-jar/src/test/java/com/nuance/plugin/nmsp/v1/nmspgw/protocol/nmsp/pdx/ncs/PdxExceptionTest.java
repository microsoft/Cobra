package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import org.junit.Test;

import static org.junit.Assert.*;

public class PdxExceptionTest {
    @Test
    public void testException() {
        Exception testException = new PdxException();
        assertNull(testException.getMessage());

        testException = new PdxException("test");
        assertEquals("test", testException.getMessage());
    }
}