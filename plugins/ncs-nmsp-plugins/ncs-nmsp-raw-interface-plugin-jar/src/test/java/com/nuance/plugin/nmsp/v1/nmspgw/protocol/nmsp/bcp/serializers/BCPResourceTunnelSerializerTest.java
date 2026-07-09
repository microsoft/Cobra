package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPResourceTunnelMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPResourceTunnelSerializerTest {

    @Test
    public void serializeBCPResourceTunnel() {
        BCPResourceTunnelMessage bcpResourceTunnelMessage = new BCPResourceTunnelMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPResourceTunnelSerializer.serialize(bcpResourceTunnelMessage);
        assertNotNull(buffer);
    }
}