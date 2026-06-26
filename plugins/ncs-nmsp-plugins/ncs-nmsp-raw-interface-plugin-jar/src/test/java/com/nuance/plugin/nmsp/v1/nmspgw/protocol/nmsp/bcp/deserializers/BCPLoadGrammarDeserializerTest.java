package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadGrammarMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BCPLoadGrammarDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <p>poolName</p>\n" +
                "  <g1>\n" +
                "    <i>myId</i>\n" +
                "    <d>5</d>\n" +
                "    <t>xml</t>\n" +
                "    <t>uri</t>\n" +
                "  </g1>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_LOAD_GRAMMAR, byteBuf, null);
        assertEquals(BCPMessage.BCP_LOAD_GRAMMAR, nmspMessage.getCommand());
    }
}
