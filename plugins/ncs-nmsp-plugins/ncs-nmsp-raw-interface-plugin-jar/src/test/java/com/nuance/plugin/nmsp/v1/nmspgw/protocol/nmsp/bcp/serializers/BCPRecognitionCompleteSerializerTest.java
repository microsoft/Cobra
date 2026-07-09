package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRecognitionCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPRecognitionCompleteSerializerTest {

    @Test
    public void serializeBCPRecognitionComplete() {
        BCPRecognitionCompleteMessage bcpRecognitionCompleteMessage = new BCPRecognitionCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPRecognitionCompleteSerializer.serialize(bcpRecognitionCompleteMessage);
        assertNotNull(buffer);
    }

    @Test
    public void serializeBCPRecognitionCompleteWithPayload() {
        BCPRecognitionCompleteMessage bcpRecognitionCompleteMessage = new BCPRecognitionCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        bcpRecognitionCompleteMessage.setPayload("test".getBytes());
        ByteBuf buffer = BCPRecognitionCompleteSerializer.serialize(bcpRecognitionCompleteMessage);
        assertNotNull(buffer);
    }
}