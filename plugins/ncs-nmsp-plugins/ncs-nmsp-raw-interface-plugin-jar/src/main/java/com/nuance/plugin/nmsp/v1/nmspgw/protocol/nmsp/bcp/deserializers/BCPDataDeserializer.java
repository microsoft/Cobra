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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPDataDeserializer {
    private static Logger logger = LogManager.getLogger(BCPDataDeserializer.class);

    private BCPDataDeserializer() {
    }

    public static BCPDataMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPDataMessage bcpData = new BCPDataMessage(version, sessionId, transactionId);

        try {
            // Read the request id
            long requestId = message.readUnsignedInt();
            bcpData.setRequestId(requestId);
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            bcpData.setPayload(payload);
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid BCP DATA message", iobe);
            throw new CorruptedFrameException("Invalid BCP DATA message");
        }

        return bcpData;
    }
}
