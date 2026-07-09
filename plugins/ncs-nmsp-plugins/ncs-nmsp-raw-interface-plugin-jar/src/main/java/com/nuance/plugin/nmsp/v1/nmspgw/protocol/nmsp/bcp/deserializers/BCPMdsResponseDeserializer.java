/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessageResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPMdsResponseDeserializer {
    private static Logger logger = LogManager.getLogger(BCPMdsResponseDeserializer.class);

    private BCPMdsResponseDeserializer() {
    }

    public static BCPMdsMessageResponse deserialize(short version, byte[] sessionId, ByteBuf message) throws CorruptedFrameException {
        BCPMdsMessageResponse bcpMdsResponseMessage = new BCPMdsMessageResponse(version, sessionId);

        try {
            // Read the request id
            long requestId = message.readUnsignedInt();
            bcpMdsResponseMessage.setRequestId(requestId);
            int statusCode = message.readUnsignedShort();
            bcpMdsResponseMessage.setStatusCode((short) statusCode);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP MDS RESPONSE MESSAGE message", iobe);
            throw new CorruptedFrameException("Invalid BCP MDS RESPONSE MESSAGE message");
        }

        return bcpMdsResponseMessage;
    }
}
