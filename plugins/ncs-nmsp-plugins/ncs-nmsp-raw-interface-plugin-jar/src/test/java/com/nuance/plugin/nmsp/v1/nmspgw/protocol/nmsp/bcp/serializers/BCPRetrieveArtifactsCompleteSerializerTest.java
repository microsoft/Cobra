package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRetrieveArtifactsCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPRetrieveArtifactsCompleteSerializerTest {

    @Test
    public void serializeBCPRetrieveArtifactsComplete() {
        BCPRetrieveArtifactsCompleteMessage bcpRetrieveArtifactsCompleteMessage = new BCPRetrieveArtifactsCompleteMessage((short) 20, "test".getBytes(), (short) 1, 1L, 1L, 1);
        ByteBuf buffer = BCPRetrieveArtifactsCompleteSerializer.serialize(bcpRetrieveArtifactsCompleteMessage);
        assertNotNull(buffer);
    }
}