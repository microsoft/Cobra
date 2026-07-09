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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPReleaseMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class VAPReleaseDeserializer {
    private static Logger logger = LogManager.getLogger(VAPReleaseDeserializer.class);

    private VAPReleaseDeserializer() {
    }

    public static VAPReleaseMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        VAPReleaseMessage vapRelease = new VAPReleaseMessage(version);

        try {
            byte[] sessionId = new byte[Session.SESSION_ID_LENGTH];
            // Read the session id
            message.readBytes(sessionId);
            vapRelease.setSessionId(sessionId);
            // Read the audio id
            long audioId = message.readUnsignedInt();
            vapRelease.setAudioId(audioId);
            byte[] requestSessionId = new byte[Session.SESSION_ID_LENGTH];
            // Read the session id
            message.readBytes(requestSessionId);
            vapRelease.setRequestSessionId(requestSessionId);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid VAP RELEASE message", iobe);
            throw new CorruptedFrameException("Invalid VAP RELEASE message");
        }

        return vapRelease;
    }
}
