package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.*;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPEventSerializerTest {

    @Test
    public void serializeBCPEvent() {
        BCPEventMessage bcpEventMessage = new BCPEventMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPEventSerializer.serialize(bcpEventMessage);
        assertNotNull(buffer);
    }
}