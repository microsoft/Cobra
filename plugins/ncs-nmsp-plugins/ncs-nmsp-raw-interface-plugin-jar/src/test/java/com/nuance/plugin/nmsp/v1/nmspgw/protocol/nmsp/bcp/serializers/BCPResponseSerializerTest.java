package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPResponseMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPResponseSerializerTest {

    @Test
    public void serializeBCPResponse() {
        BCPResponseMessage bcpResponseMessage = new BCPResponseMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPResponseSerializer.serialize(bcpResponseMessage);
        assertNotNull(buffer);
    }
}