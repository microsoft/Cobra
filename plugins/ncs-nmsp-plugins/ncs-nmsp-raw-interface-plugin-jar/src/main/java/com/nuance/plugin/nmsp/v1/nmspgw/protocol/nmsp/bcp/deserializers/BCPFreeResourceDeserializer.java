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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPFreeResourceMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPFreeResourceDeserializer {
    private static Logger logger = LogManager.getLogger(BCPFreeResourceDeserializer.class);

    private BCPFreeResourceDeserializer() {
    }

    public static BCPFreeResourceMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPFreeResourceMessage bcpFreeResource = new BCPFreeResourceMessage(version, sessionId, transactionId);

        try {
            // Read the timeout
            long timeout = message.readUnsignedInt();
            bcpFreeResource.setTimeout(timeout);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP FREE RESOURCE message", iobe);
            throw new CorruptedFrameException("Invalid BCP FREE RESOURCE message");
        }

        return bcpFreeResource;
    }
}
