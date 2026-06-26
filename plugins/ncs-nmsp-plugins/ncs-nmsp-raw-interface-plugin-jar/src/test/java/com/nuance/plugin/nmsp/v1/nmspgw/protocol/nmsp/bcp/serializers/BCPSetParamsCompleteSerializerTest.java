package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPSetParamsCompleteMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BCPSetParamsCompleteSerializerTest {

    @Test
    public void serializeBCPSetParamsComplete() {
        BCPSetParamsCompleteMessage bcpSetParamsCompleteMessage = new BCPSetParamsCompleteMessage((short) 20, "test".getBytes(), (short) 1);
        ByteBuf buffer = BCPSetParamsCompleteSerializer.serialize(bcpSetParamsCompleteMessage);
        assertNotNull(buffer);
    }
}