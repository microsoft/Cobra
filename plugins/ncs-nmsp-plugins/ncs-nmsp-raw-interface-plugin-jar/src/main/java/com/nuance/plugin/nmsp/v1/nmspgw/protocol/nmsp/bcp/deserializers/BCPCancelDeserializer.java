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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPCancelMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class BCPCancelDeserializer {
    private static Logger logger = LogManager.getLogger(BCPCancelDeserializer.class);

    private BCPCancelDeserializer() {
    }

    public static BCPCancelMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPCancelMessage bcpCancel = new BCPCancelMessage(version, sessionId, transactionId);

        try {
            // Read the request id
            long requestId = message.readUnsignedInt();
            bcpCancel.setRequestId(requestId);
            // Read the request id to cancel
            long requestIdToCancel = message.readUnsignedInt();
            bcpCancel.setRequestIdToCancel(requestIdToCancel);
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid BCP CANCEL message", iobe);
            throw new CorruptedFrameException("Invalid BCP CANCEL message");
        }

        return bcpCancel;
    }
}
