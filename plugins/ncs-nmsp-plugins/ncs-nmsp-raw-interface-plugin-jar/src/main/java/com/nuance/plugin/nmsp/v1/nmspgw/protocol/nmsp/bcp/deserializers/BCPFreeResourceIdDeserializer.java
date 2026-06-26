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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPFreeResourceIdMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPFreeResourceIdDeserializer {
    private static Logger logger = LogManager.getLogger(BCPFreeResourceIdDeserializer.class);

    private BCPFreeResourceIdDeserializer() {
    }

    public static BCPFreeResourceIdMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPFreeResourceIdMessage bcpFreeResourceId = new BCPFreeResourceIdMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            BCPFreeResourceIdSAXHandler handler = new BCPFreeResourceIdSAXHandler(bcpFreeResourceId);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP FREE RESOURCE ID message", iobe);
            throw new CorruptedFrameException("Invalid BCP FREE RESOURCE ID message");
        }

        // Validate mandatory parameters
        if (bcpFreeResourceId.getRequestId() == -1) {
            logger.warn("Invalid BCP FREE RESOURCE ID message: missing mandatory element 'request id'");
            throw new CorruptedFrameException("Invalid BCP FREE RESOURCE ID message: missing mandatory element 'request id'");
        }

        return bcpFreeResourceId;
    }
}
