package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLogResponseMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPLogResponseSerializerTest {

    @Test
    public void serializeBCPLogResponse() {
        BCPLogResponseMessage bcpLogResponseMessage = new BCPLogResponseMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPLogResponseSerializer.serialize(bcpLogResponseMessage);
        assertNotNull(buffer);
    }
}