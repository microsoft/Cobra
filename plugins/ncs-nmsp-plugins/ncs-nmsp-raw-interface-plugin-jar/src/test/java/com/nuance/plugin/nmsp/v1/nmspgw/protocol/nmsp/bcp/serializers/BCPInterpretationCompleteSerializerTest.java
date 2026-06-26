package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPInterpretationCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPInterpretationCompleteSerializerTest {

    @Test
    public void serializeBCPInterpretationComplete() {
        BCPInterpretationCompleteMessage bcpInterpretationCompleteMessage = new BCPInterpretationCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPInterpretationCompleteSerializer.serialize(bcpInterpretationCompleteMessage);
        assertNotNull(buffer);
    }

    @Test
    public void serializeBCPInterpretationCompleteWithPayload() {
        BCPInterpretationCompleteMessage bcpInterpretationCompleteMessage = new BCPInterpretationCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        bcpInterpretationCompleteMessage.setPayload("test".getBytes());
        ByteBuf buffer = BCPInterpretationCompleteSerializer.serialize(bcpInterpretationCompleteMessage);
        assertNotNull(buffer);
    }
}