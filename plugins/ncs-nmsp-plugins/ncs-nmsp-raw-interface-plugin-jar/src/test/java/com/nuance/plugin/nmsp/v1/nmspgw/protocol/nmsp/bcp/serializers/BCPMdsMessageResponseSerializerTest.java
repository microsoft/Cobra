package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessageResponse;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPMdsMessageResponseSerializerTest {

    @Test
    public void serializeBCPMdsMessageResponse() {
        BCPMdsMessageResponse bcpMdsMessageResponse = new BCPMdsMessageResponse((short) 20, "test".getBytes());
        ByteBuf buffer = BCPMdsMessageResponseSerializer.serialize(bcpMdsMessageResponse);
        assertNotNull(buffer);
    }
}