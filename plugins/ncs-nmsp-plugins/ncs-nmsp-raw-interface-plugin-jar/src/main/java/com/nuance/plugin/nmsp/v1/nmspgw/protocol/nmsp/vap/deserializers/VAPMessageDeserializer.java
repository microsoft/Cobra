/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class VAPMessageDeserializer {
    private static Logger logger = LogManager.getLogger(VAPMessageDeserializer.class);

    private VAPMessageDeserializer() {
    }

    public static NMSPMessage deserialize(short version, int command, ByteBuf message, Session session) throws CorruptedFrameException {
        switch (command) {
            case VAPMessage.VAP_RECORD_END:
                return VAPRecordEndDeserializer.deserialize(version, message);
            case VAPMessage.VAP_RECORD_BEGIN:
                return VAPRecordBeginDeserializer.deserialize(version, message);
            case VAPMessage.VAP_RECORD:
                return VAPRecordDeserializer.deserialize(version, message, session);
            // case VAPMessage.VAP_BACKUP_REQUEST:
            // return VAPBackupRequestDeserializer.deserialize(version, message);
            // case VAPMessage.VAP_RECOVER_REQUEST:
            // return VAPRecoverRequestDeserializer.deserialize(version, message);
            case VAPMessage.VAP_RELEASE:
                return VAPReleaseDeserializer.deserialize(version, message);
            default:
                    logger.warn("Invalid VAP message: " + command);
                throw new CorruptedFrameException("Invalid VAP message: " + command);
        }
    }
}
