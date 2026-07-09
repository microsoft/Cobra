package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPTranscodeCompleteSerializerTest {

    @Test
    public void serializeBCPTranscodeComplete() {
        BCPTranscodeCompleteMessage bcpTranscodeCompleteMessage = new BCPTranscodeCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPTranscodeCompleteSerializer.serialize(bcpTranscodeCompleteMessage);
        assertNotNull(buffer);
    }
}