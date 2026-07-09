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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConfirmMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class COPConfirmDeserializer {
    private static Logger logger = LogManager.getLogger(COPConfirmDeserializer.class);

    private COPConfirmDeserializer() {
    }

    public static COPConfirmMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        COPConfirmMessage copConfirm = new COPConfirmMessage(version);

        try {
            // Read the session id's length
            long length = message.readUnsignedInt();
            byte[] sessionId = new byte[(int) length];
            // Read the encrypted session id
            message.readBytes(sessionId);
            copConfirm.setEncryptedSessionId(sessionId);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid COP CONFIRM message", iobe);
            throw new CorruptedFrameException("Invalid COP CONFIRM message");
        }

        // Validate mandatory parameters
        if (copConfirm.getEncryptedSessionId() == null) {
            logger.warn("Invalid COP CONFIRM message: missing mandatory element 'encrypted session id'");
            throw new CorruptedFrameException("Invalid COP CONFIRM message: missing mandatory element 'encrypted session id'");
        }

        return copConfirm;
    }
}
