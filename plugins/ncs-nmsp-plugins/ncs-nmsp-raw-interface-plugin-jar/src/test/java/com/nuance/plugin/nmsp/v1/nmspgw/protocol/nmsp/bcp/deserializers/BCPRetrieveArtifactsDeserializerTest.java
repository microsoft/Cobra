package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRetrieveArtifactsMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class BCPRetrieveArtifactsDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <tsid>5</tsid>\n" +
                "  <aid>5</aid>\n" +
                "  <bs>5</bs>\n" +
                "  <bps>5</bps>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_RETRIEVE_ARTIFACTS, byteBuf, null);
        assertEquals(BCPMessage.BCP_RETRIEVE_ARTIFACTS, nmspMessage.getCommand());
    }

    @Test(expected = CorruptedFrameException.class)
    public void test_invalidInput() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        String message = "<body>\n  <rid>9h8d9g17f3</rid>\n</body>";
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        BCPRetrieveArtifactsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), (short) 0, byteBuf);
    }
}