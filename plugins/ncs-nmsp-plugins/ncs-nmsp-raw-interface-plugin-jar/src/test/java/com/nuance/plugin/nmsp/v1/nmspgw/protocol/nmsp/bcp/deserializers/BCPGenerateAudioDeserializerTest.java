package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGenerateAudioMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class BCPGenerateAudioDeserializerTest {

    @Test
    public void test_basicDeserialization() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String fullMessage = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <p1>\n" +
                "    <d>5</d>\n" +
                "    <t>txt</t>\n" +
                "    <t>uri</t>\n" +
                "    <t>ssml</t>\n" +
                "  </p1>\n" +
                "  <aid>5</aid>\n" +
                "  <bs>5</bs>\n" +
                "  <bps>5</bps>\n" +
                "  <eos/>\n" +
                "  <nmsp/>\n" +
                "  <nss/>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(fullMessage.length());
        byteBuf.writeBytes(fullMessage.getBytes());
        byte[] extraBytes = new byte[5];
        new java.security.SecureRandom().nextBytes(extraBytes);
        byteBuf.writeIntLE(extraBytes.length);
        byteBuf.writeBytes(extraBytes);
        NMSPMessage message = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_GENERATE_AUDIO, byteBuf, null);
        assertEquals(BCPMessage.BCP_GENERATE_AUDIO, message.getCommand());
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_overflow() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>\n" +
                "    <id>5</id>\n" +
                "    <timeout>5</timeout>\n" +
                "  </resid>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length()+100);
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_GENERATE_AUDIO, byteBuf, null);
        assertEquals(BCPMessage.BCP_FREE_RESOURCE_ID, nmspMessage.getCommand());
    }
}