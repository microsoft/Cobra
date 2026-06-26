package com.nuance.ncs.nmsp.engine.pdx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class NmspFrameDecoderTest {

    private static ByteBuf buildFrame(byte protocol, byte version, int command, byte[] payload) {
        ByteBuf buf = Unpooled.buffer(8 + payload.length);
        buf.writeByte(protocol);
        buf.writeByte(version);
        buf.writeShortLE(command);
        buf.writeIntLE(payload.length);
        buf.writeBytes(payload);
        return buf;
    }

    @Test
    public void decodeCompleteFrame() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        byte[] payload = {1, 2, 3, 4};
        ch.writeInbound(buildFrame((byte) 0x03, (byte) 0x01, 0x0002, payload));

        NmspFrame frame = ch.readInbound();
        assertNotNull(frame);
        assertEquals((byte) 0x03, frame.protocol);
        assertEquals((byte) 0x01, frame.version);
        assertEquals(0x0002, frame.command);
        assertArrayEquals(payload, frame.payload);
    }

    @Test
    public void partialHeaderNotDecoded() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        ByteBuf partial = Unpooled.buffer(4);
        partial.writeByte(0x03);
        partial.writeByte(0x01);
        partial.writeShortLE(0x0002);
        ch.writeInbound(partial);
        assertNull(ch.readInbound()); // not enough bytes
    }

    @Test
    public void partialPayloadNotDecoded() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        // write header saying payload=10 bytes, but only send 5
        ByteBuf buf = Unpooled.buffer(13);
        buf.writeByte(0x02);
        buf.writeByte(0x01);
        buf.writeShortLE(0x0001);
        buf.writeIntLE(10);
        buf.writeBytes(new byte[5]);
        ch.writeInbound(buf);
        assertNull(ch.readInbound());
    }

    @Test
    public void emptyPayload() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        ch.writeInbound(buildFrame((byte) 0x01, (byte) 0x01, 0x0200, new byte[0]));
        NmspFrame frame = ch.readInbound();
        assertNotNull(frame);
        assertEquals(0, frame.payload.length);
    }

    @Test
    public void multipleFrames() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(buildFrame((byte) 0x03, (byte) 0x01, 0x0002, new byte[]{10}));
        buf.writeBytes(buildFrame((byte) 0x02, (byte) 0x01, 0x7201, new byte[]{20, 21}));
        ch.writeInbound(buf);
        NmspFrame f1 = ch.readInbound();
        NmspFrame f2 = ch.readInbound();
        assertNotNull(f1);
        assertNotNull(f2);
        assertEquals((byte) 0x03, f1.protocol);
        assertEquals((byte) 0x02, f2.protocol);
    }

    @Test
    public void oversizedFrameClosesChannel() {
        EmbeddedChannel ch = new EmbeddedChannel(new NmspFrameDecoder());
        // payloadLen > Integer.MAX_VALUE: use Long value encoded in 4 bytes as unsigned
        // We need payloadLen > 2^31. Encode 0xFFFFFFFF as unsigned int LE
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeByte(0x03);
        buf.writeByte(0x01);
        buf.writeShortLE(0x0002);
        // write 0xFFFFFFFF as LE int (treated as unsigned > Integer.MAX_VALUE)
        buf.writeByte(0xFF);
        buf.writeByte(0xFF);
        buf.writeByte(0xFF);
        buf.writeByte(0xFF);
        ch.writeInbound(buf);
        assertFalse(ch.isActive());
    }
}
