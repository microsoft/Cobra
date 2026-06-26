package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPSetParamsMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BCPSetParamsDeserializerTest {

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
                "  <nmsp p=\"niceName\" v=\"12\"/>\n" +
                "  <app p=\"niceName\" v=\"12\"/>\n" +
                "  <ext p=\"niceName\" v=\"12\"/>\n" +
                "  <nss p=\"niceName\" v=\"12\"/>\n" +
                "  <svsp p=\"fakeName\" v=\"412\"/>\n" +
                "  <slog p=\"fakeName\" v=\"412\"/>\n" +
                "  <nsslog p=\"fakeName\" v=\"412\"/>\n" +
                "  <gwlog p=\"fakeName\" v=\"412\"/>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_SET_PARAMS, byteBuf, null);
        assertEquals(BCPMessage.BCP_SET_PARAMS, nmspMessage.getCommand());
    }
}