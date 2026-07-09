package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadResourceMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BCPLoadResourceDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        new java.security.SecureRandom().nextBytes(sessionId);
        int transactionId = 5;
        String message = "<body>\n" +
                "  <rid>5</rid>\n" +
                "  <resid>5</resid>\n" +
                "  <username>myUsername</username>\n" +
                "  <oper/>\n" +
                "  <sip p=\"niceName\" v=\"12\"/>\n" +
                "  <sdp p=\"niceName\" v=\"12\"/>\n" +
                "  <s2/>\n" +
                "  <codec>2</codec>\n" +
                "  <capability p=\"niceName\" v=\"12\"/>\n" +
                "  <deferloading>OFF</deferloading>\n" +
                "  <deferloading>WAIT_AUDIO</deferloading>\n" +
                "  <deferloading>WAIT_SPEECH</deferloading>\n" +
                "  <clientAudioStreamingMaxIdleTimeMSec>500</clientAudioStreamingMaxIdleTimeMSec>\n" +
                "</body>";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_LOAD_RESOURCE, byteBuf, null);
        assertEquals(BCPMessage.BCP_LOAD_RESOURCE, nmspMessage.getCommand());
    }
}
