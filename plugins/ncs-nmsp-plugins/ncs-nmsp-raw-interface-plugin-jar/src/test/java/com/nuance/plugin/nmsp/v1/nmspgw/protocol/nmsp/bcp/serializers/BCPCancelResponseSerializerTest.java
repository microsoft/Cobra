package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPCancelResponseMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPCancelResponseSerializerTest {

    @Test
    public void serializeBCPCancelResponse() {
        BCPCancelResponseMessage bcpCancelResponseMessage = new BCPCancelResponseMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPCancelResponseSerializer.serialize(bcpCancelResponseMessage);
        assertNotNull(buffer);
    }
}