package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRecognitionIntermediateResultsMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPRecognitionIntermediateResultsSerializerTest {

    @Test
    public void serializeBCPRecognitionIntermediateResults() {
        BCPRecognitionIntermediateResultsMessage bcpRecognitionIntermediateResultsMessage = new BCPRecognitionIntermediateResultsMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPRecognitionIntermediateResultsSerializer.serialize(bcpRecognitionIntermediateResultsMessage);
        assertNotNull(buffer);
    }

    @Test
    public void serializeBCPRecognitionIntermediateResultsWithPayload() {
        BCPRecognitionIntermediateResultsMessage bcpRecognitionIntermediateResultsMessage = new BCPRecognitionIntermediateResultsMessage((short) 20, "test".getBytes(), (short) 1);
        bcpRecognitionIntermediateResultsMessage.setPayload("test".getBytes());
        ByteBuf buffer = BCPRecognitionIntermediateResultsSerializer.serialize(bcpRecognitionIntermediateResultsMessage);
        assertNotNull(buffer);
    }
}