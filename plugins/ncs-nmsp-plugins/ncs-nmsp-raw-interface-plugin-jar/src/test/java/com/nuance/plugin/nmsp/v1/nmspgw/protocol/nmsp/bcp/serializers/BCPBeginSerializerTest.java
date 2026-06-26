package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BCPBeginSerializerTest {

    @Test
    public void serializeBCPBegin() {
        BCPBeginMessage bcpBeginMessage = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        bcpBeginMessage.setRemoteParentEventId("test");
        Session session = mock(Session.class);
        BCPBeginSerializer bcpBeginSerializer = new BCPBeginSerializer();
        ByteBuf buffer = bcpBeginSerializer.serialize(session, bcpBeginMessage);
        assertNotNull(buffer);
    }

    @Test
    public void serializeBCPBeginMaxMessage() {
        BCPBeginMessage bcpBeginMessage = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        bcpBeginMessage.setRemoteParentEventId("test");
        Session session = mock(Session.class);
        when(session.getDerogatoryMaxMessageSizeForCurrentNMAID()).thenReturn(Optional.of(1));
        BCPBeginSerializer bcpBeginSerializer = new BCPBeginSerializer();
        ByteBuf buffer = bcpBeginSerializer.serialize(session, bcpBeginMessage);
        assertNotNull(buffer);
    }

    @Test
    public void testGetVersion() {
        BCPBeginSerializer bcpBeginSerializer = new BCPBeginSerializer();
        assertNotNull(bcpBeginSerializer.getVersion());
    }
}