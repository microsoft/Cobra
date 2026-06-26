package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGetParamsMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BCPGetParamsDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <noresponse/>\n" +
                "  <nmsp p=\"niceName\"/>\n" +
                "  <app p=\"niceName\"/>\n" +
                "  <ext p=\"niceName\"/>\n" +
                "  <nss p=\"niceName\"/>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_GET_PARAMS, byteBuf, null);
        assertEquals(BCPMessage.BCP_GET_PARAMS, nmspMessage.getCommand());
    }
}