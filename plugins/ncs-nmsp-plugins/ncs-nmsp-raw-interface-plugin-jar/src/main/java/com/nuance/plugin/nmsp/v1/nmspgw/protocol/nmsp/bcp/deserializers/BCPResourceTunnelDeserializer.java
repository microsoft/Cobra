/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPResourceTunnelMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPResourceTunnelDeserializer {
    private static Logger logger = LogManager.getLogger(BCPResourceTunnelDeserializer.class);

    private BCPResourceTunnelDeserializer() {
    }

    public static BCPResourceTunnelMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPResourceTunnelMessage bcpResourceTunnel = new BCPResourceTunnelMessage(version, sessionId, transactionId);

        try {
            // Read the resource id
            long resourceId = message.readUnsignedInt();
            bcpResourceTunnel.setResourceId(resourceId);
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            bcpResourceTunnel.setPayload(payload);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP RESOURCE TUNNEL message", iobe);
            throw new CorruptedFrameException("Invalid BCP RESOURCE TUNNEL message");
        }

        return bcpResourceTunnel;
    }
}
