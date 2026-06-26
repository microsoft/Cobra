package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPDataSerializerTest {

    @Test
    public void serializeBCPData() {
        BCPDataMessage bcpDataMessage = new BCPDataMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPDataSerializer.serialize(bcpDataMessage);
        assertNotNull(buffer);
    }
}