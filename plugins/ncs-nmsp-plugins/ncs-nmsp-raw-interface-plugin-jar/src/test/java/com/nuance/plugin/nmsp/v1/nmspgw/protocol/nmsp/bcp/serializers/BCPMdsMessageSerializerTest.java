package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPMdsMessageSerializerTest {

    @Test
    public void serializeBCPMdsMessage() {
        BCPMdsMessage bcpMdsMessage = new BCPMdsMessage((short) 20, "test".getBytes());
        ByteBuf buffer = BCPMdsMessageSerializer.serialize(bcpMdsMessage);
        assertNotNull(buffer);
    }
}