/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class COPMessageDeserializer {
    private static final Logger logger = LogManager.getLogger();

    private COPMessageDeserializer() {
    }

    public static COPMessage deserialize(short version, int command, ByteBuf message) throws CorruptedFrameException {
        switch (command) {
            case COPMessage.COP_CONNECT:
                return COPConnectDeserializerRegistry.getCOPMessageDeserializer(version).deserialize(version, message);
            case COPMessage.COP_PING_RESPONSE:
                return COPPingResponseDeserializer.deserialize(version, message);
            case COPMessage.COP_CONFIRM:
                return COPConfirmDeserializer.deserialize(version, message);
            case COPMessage.COP_DISCONNECT:
                return COPDisconnectDeserializer.deserialize(version, message);
            default:
                logger.warn("Invalid COP message: {}", command);
                throw new CorruptedFrameException("Invalid COP message: " + command);
        }
    }
}
