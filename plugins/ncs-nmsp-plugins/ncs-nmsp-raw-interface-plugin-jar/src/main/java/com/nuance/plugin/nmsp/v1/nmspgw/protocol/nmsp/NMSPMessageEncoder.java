/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@ChannelHandler.Sharable
public class NMSPMessageEncoder extends MessageToMessageEncoder<NMSPMessage> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void encode(ChannelHandlerContext ctx, NMSPMessage msg, List<Object> out) throws Exception {
        final Session session = (Session) ctx.channel().attr(io.netty.util.AttributeKey.valueOf("session")).get();

        // Encode and add to output list
        ByteBuf encoded = encode(session, msg);
        out.add(encoded);
    }

    public static ByteBuf encode(final Session session, final NMSPMessage message) {
        logger.debug("Encoding message: {}", message.toString());
        return NMSPMessageSerializer.serialize(session, message);
    }
}
