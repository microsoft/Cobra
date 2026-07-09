package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadResourceCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPLoadResourceCompleteSerializerTest {

    @Test
    public void serializeBCPLoadResourceComplete() {
        BCPLoadResourceCompleteMessage bcpLoadResourceCompleteMessage = new BCPLoadResourceCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPLoadResourceCompleteSerializer.serialize(bcpLoadResourceCompleteMessage);
        assertNotNull(buffer);
    }
}