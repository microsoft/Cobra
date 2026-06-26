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

import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordEndMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class VAPRecordEndDeserializer {
    private static Logger logger = LogManager.getLogger(NMSPClientManager.class);

    private VAPRecordEndDeserializer() {
    }

    public static VAPRecordEndMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        VAPRecordEndMessage vapRecordEnd = new VAPRecordEndMessage(version);

        try {
            // Read the audio id
            long audioId = message.readUnsignedInt();
            vapRecordEnd.setAudioId(audioId);
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid VAP RECORD END message", iobe);
            throw new CorruptedFrameException("Invalid VAP RECORD END message");
        }

        return vapRecordEnd;
    }
}
