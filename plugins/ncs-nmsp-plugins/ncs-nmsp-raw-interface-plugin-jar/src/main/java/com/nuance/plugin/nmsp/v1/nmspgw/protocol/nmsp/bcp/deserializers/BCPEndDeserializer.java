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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPEndMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class BCPEndDeserializer {
    private static Logger logger = LogManager.getLogger(BCPEndMessage.class);
    private BCPEndDeserializer() {
    }

    public static BCPEndMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPEndMessage bcpEnd = new BCPEndMessage(version, sessionId, transactionId);

        try {
            // Read the disconnection timeout
            long disconnectionTimeout = message.readUnsignedInt();
            bcpEnd.setDisconnectionTimeout(disconnectionTimeout);
            long transactionInfoLength = 0;
            if (message.isReadable()) {
                transactionInfoLength = message.readUnsignedInt();
            }
            if (transactionInfoLength > 0) {
                byte[] transactionInfoBytes = new byte[(int) transactionInfoLength];
                message.readBytes(transactionInfoBytes);
                BCPEndSAXHandler handler = new BCPEndSAXHandler(bcpEnd);
                NMSPMessageDeserializer.parseXMLPayload(transactionInfoBytes, handler);
            } else {
                bcpEnd.setStatus(Utils.Status.DONT_REPORT);
            }
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Invalid BCP END message", e);
            throw new CorruptedFrameException("Invalid BCP END message " + e.getMessage());
        }

        return bcpEnd;
    }
}
