package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class PdxDataTypesTest {

    @Test
    public void nmspFrameFields() {
        byte[] payload = {1, 2, 3};
        NmspFrame f = new NmspFrame((byte) 0x03, (byte) 0x01, 0x0002, payload);
        assertEquals((byte) 0x03, f.protocol);
        assertEquals((byte) 0x01, f.version);
        assertEquals(0x0002, f.command);
        assertSame(payload, f.payload);
    }

    @Test
    public void pdxQueryErrorGetters() {
        PdxQueryError e = new PdxQueryError(503, "Service Unavailable");
        assertEquals(503, e.getError());
        assertEquals("Service Unavailable", e.getDescription());
    }

    @Test
    public void pdxQueryResultGetters() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addUTF8String("key", "val");
        PdxQueryResult r = new PdxQueryResult(dict, true);
        assertSame(dict, r.getData());
        assertTrue(r.isFinal());

        PdxQueryResult r2 = new PdxQueryResult(null, false);
        assertNull(r2.getData());
        assertFalse(r2.isFinal());
    }

    @Test
    public void pdxQueryRetryGetters() {
        PdxQueryRetry r = new PdxQueryRetry("retryName", 4, "Try again");
        assertEquals("retryName", r.getName());
        assertEquals(4, r.getCause());
        assertEquals("Try again", r.getPrompt());
    }
}
