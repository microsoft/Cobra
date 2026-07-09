package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGetParamsCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPGetParamsCompleteSerializerTest {

    @Test
    public void serializeBCPGenerateAudioComplete() {
        BCPGetParamsCompleteMessage bcpGetParamsCompleteMessage = new BCPGetParamsCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPGetParamsCompleteSerializer.serialize(bcpGetParamsCompleteMessage);
        assertNotNull(buffer);
    }
}