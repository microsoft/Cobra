package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGenerateAudioCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPGenerateAudioCompleteSerializerTest {

    @Test
    public void serializeBCPGenerateAudioComplete() {
        BCPGenerateAudioCompleteMessage bcpGenerateAudioCompleteMessage = new BCPGenerateAudioCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPGenerateAudioCompleteSerializer.serialize(bcpGenerateAudioCompleteMessage);
        assertNotNull(buffer);
    }
}