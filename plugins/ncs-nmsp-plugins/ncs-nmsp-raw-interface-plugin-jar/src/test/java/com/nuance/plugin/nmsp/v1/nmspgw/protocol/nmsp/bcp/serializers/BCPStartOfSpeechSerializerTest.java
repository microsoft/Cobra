package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPStartOfSpeechMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPStartOfSpeechSerializerTest {

    @Test
    public void serializeBCPStartOfSpeech() {
        BCPStartOfSpeechMessage bcpStartOfSpeechMessage = new BCPStartOfSpeechMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPStartOfSpeechSerializer.serialize(bcpStartOfSpeechMessage);
        assertNotNull(buffer);
    }
}