package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.junit.Test;

import static org.junit.Assert.*;

public class BCPBeginMessageTest {

    private BCPBeginMessage bcpBeginMessage = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
    private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 5;

    @Test
    public void testMessageLength() {
        assertEquals(bcpBeginMessage.getMessageLength(), messageStaticLength + bcpBeginMessage.payload.length);
    }

    @Test
    public void testQueryBegin() {
        PdxDictionary testDictionary = new PdxDictionary();
        bcpBeginMessage.setQueryBegin(testDictionary);
        assertEquals(bcpBeginMessage.getQueryBeginInfo(), testDictionary);
        assertNull(bcpBeginMessage.getQueryBeginUserId());
    }

    @Test
    public void testRemoteParentEventId() {
        String remoteParentEventId = "test";
        bcpBeginMessage.setRemoteParentEventId(remoteParentEventId);
        assertEquals(bcpBeginMessage.getRemoteParentEventId(), remoteParentEventId);
    }

    @Test
    public void testUserId() {
        String userId = "test";
        bcpBeginMessage.setUserId(userId);
        assertEquals(bcpBeginMessage.getUserId(), userId);
    }

    @Test
    public void testNmaid() {
        String nmaid = "test";
        bcpBeginMessage.setNmaid(nmaid);
        assertEquals(bcpBeginMessage.getNmaid(), nmaid);
    }

    @Test
    public void testToStringQuery() {
        PdxDictionary testDictionary = new PdxDictionary();
        bcpBeginMessage.setQueryBegin(testDictionary);
        assertNotNull(bcpBeginMessage.toString());
    }

    @Test
    public void testToString() {
        bcpBeginMessage.setQueryBegin(null);
        assertNotNull(bcpBeginMessage.toString());
    }
}