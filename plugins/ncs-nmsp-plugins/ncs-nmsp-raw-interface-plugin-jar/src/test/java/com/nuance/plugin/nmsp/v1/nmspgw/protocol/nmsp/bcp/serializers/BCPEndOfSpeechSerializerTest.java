package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPEndOfSpeechMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPEndOfSpeechSerializerTest {

    @Test
    public void serializeBCPEndOfSpeech() {
        BCPEndOfSpeechMessage bcpEndOfSpeechMessage = new BCPEndOfSpeechMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPEndOfSpeechSerializer.serialize(bcpEndOfSpeechMessage);
        assertNotNull(buffer);
    }
}