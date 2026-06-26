package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BCPMdsDeserializerTest {

    @Test
    public void test_basic() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        byte[] sessionId = new byte[16];
        int transactionId = 5;
        String message = "<body>\n" +
                "  <requestid>5</requestid>\n" +
                "  <serviceid>service1</serviceid>\n" +
                "  <to>you</to>\n" +
                "  <ttl>5</ttl>\n" +
                "</body>";
        String actualMessage = "Hi mom";
        byteBuf.writeBytes(sessionId);
        byteBuf.writeByte(transactionId);
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeIntLE(actualMessage.length());
        byteBuf.writeBytes(actualMessage.getBytes());
        NMSPMessage nmspMessage = BCPMessageDeserializer.deserialize((short)0, BCPMessage.BCP_MDS_MESSAGE, byteBuf, null);
        assertEquals(BCPMessage.BCP_MDS_MESSAGE, nmspMessage.getCommand());
    }

    @Test
    public void testMalformedRequest() throws CorruptedFrameException {
        ByteBuf byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        String message = "<body>\n  <requestid>5</requestid>\n</body>";
        String actualMessage = "Hi mom";
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeIntLE(actualMessage.length());
        byteBuf.writeBytes(actualMessage.getBytes());
        try{
            BCPMdsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), byteBuf);
            fail();
        } catch (CorruptedFrameException ignored) {
        }

        byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        message = "<body>\n  <requestid>5</requestid>\n  <serviceid>myServ</serviceid>\n</body>";
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeIntLE(actualMessage.length());
        byteBuf.writeBytes(actualMessage.getBytes());
        try{
            BCPMdsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), byteBuf);
            fail();
        } catch (CorruptedFrameException ignored) {
        }

        byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        message = "<body>\n  <requestid>5</requestid>\n  <serviceid>myServ</serviceid>\n  <to>you</to>\n</body>";
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeIntLE(actualMessage.length());
        byteBuf.writeBytes(actualMessage.getBytes());
        try{
            BCPMdsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), byteBuf);
            fail();
        } catch (CorruptedFrameException ignored) {
        }

        byteBuf = Unpooled.buffer(1024).order(ByteOrder.LITTLE_ENDIAN);
        message = "<body>\n  <requestid>5</requestid>\n  <serviceid>myServ</serviceid>\n  <to>you</to>\n  <ttl>5</ttl>\n</body>";
        byteBuf.writeIntLE(message.length());
        byteBuf.writeBytes(message.getBytes());
        byteBuf.writeIntLE(0);
//        byteBuf.writeBytes("".getBytes());
        try{
            BCPMdsDeserializer.deserialize((short) 0, "djih89ud1".getBytes(), byteBuf);
            fail();
        } catch (CorruptedFrameException ignored) {
        }
    }
}