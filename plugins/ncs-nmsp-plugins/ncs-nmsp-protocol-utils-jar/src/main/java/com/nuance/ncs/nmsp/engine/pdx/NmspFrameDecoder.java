package com.nuance.ncs.nmsp.engine.pdx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Netty 4 ByteToMessageDecoder for the NMSP wire protocol.
 * Header: [protocol:1][version:1][command:2 LE][payloadLen:4 LE] = 8 bytes total
 */
public class NmspFrameDecoder extends ByteToMessageDecoder {
    private static final Logger log = LogManager.getLogger(NmspFrameDecoder.class);
    private static final int HEADER_SIZE = 8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < HEADER_SIZE) return;

        in.markReaderIndex();
        byte protocol = in.readByte();
        byte version = in.readByte();
        int command = in.readShortLE() & 0xFFFF;
        long payloadLen = in.readUnsignedIntLE();

        if (payloadLen > Integer.MAX_VALUE) {
            log.error("NMSP frame payload too large: {}", payloadLen);
            ctx.close();
            return;
        }
        int len = (int) payloadLen;
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }

        byte[] payload = new byte[len];
        in.readBytes(payload);
        out.add(new NmspFrame(protocol, version, command, payload));
    }
}
