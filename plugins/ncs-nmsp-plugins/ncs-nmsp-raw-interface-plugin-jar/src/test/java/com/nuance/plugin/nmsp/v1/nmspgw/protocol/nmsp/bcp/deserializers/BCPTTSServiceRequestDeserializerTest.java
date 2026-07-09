package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTTSServiceRequestMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class BCPTTSServiceRequestDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
       String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <p1>\n" +
                "    <d>5</d>\n" +
                "    <t>txt</t>\n" +
                "  </p1>\n" +
                "  <aid>5</aid>\n" +
                "  <bps>5</bps>\n" +
                "  <xml p=\"myName\" v=\"6\">5</xml>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byte[] extraBytes = new byte[5];
        new java.security.SecureRandom().nextBytes(extraBytes);
        byteBuf.writeIntLE(extraBytes.length);
        byteBuf.writeBytes(extraBytes);        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_TTS_SERVICE_REQUEST, byteBuf, null);
        assertEquals(BCPMessage.BCP_TTS_SERVICE_REQUEST, nmspMessage.getCommand());
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_invalidText() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <text>";
        String text = "new text?";
        String endMessage = "</text>\n" +
                "</body>";
        byteBuf.writeIntLE(message.length() + text.length() + endMessage.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeBytes(text.getBytes(StandardCharsets.UTF_16BE));
        byteBuf.writeBytes(endMessage.getBytes());
        BCPInterpretDeserializer.deserialize((short)0, "djih89ud1".getBytes(), (short)0, byteBuf);
    }
}
