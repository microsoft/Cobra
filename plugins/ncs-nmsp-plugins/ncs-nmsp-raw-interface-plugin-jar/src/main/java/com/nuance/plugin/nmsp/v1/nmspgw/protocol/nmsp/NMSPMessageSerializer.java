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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers.BCPMessageSerializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.serializers.COPMessageSerializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.serializers.VAPMessageSerializer;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class NMSPMessageSerializer {
    private static final Logger logger = LogManager.getLogger();

    private NMSPMessageSerializer() {
    }

    public static ByteBuf serialize(final Session session, final NMSPMessage message) {
        ByteBuf buffer = Unpooled.EMPTY_BUFFER;

        switch (message.getProtocol()) {
            case NMSPMessage.COP:
                buffer = COPMessageSerializer.serialize((COPMessage) message);
                break;
            case NMSPMessage.BCP:
                buffer = BCPMessageSerializer.serialize(session, (BCPMessage) message);
                break;
            case NMSPMessage.VAP:
                buffer = VAPMessageSerializer.serialize(session, (VAPMessage) message);
                break;
            case NMSPMessage.HANDSHAKEBYTE:
                buffer = Unpooled.buffer(NMSPMessage.headerSize).order(NMSPMessage.byteOrder);
                buffer.writerIndex(NMSPMessage.headerSize);
                break;
        }

        long payloadSize = (long) buffer.readableBytes() - NMSPMessage.headerSize;
        // Insert header (NMSP uses little-endian byte order)
        buffer.setByte(NMSPMessage.protocolIndex, message.getProtocol());
        buffer.setByte(NMSPMessage.versionIndex, message.getVersion());
        buffer.setShortLE(NMSPMessage.commandIndex, message.getCommand());
        buffer.setIntLE(NMSPMessage.lengthIndex, (int) payloadSize);

        logger.trace("Serialized a {} bytes message", buffer.readableBytes());
        return buffer;
    }
}
