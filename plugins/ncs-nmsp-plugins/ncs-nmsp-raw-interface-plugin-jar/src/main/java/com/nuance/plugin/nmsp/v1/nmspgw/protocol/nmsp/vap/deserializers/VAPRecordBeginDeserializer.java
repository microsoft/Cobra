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

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordBeginMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class VAPRecordBeginDeserializer {
    private static Logger logger = LogManager.getLogger(VAPRecordBeginDeserializer.class);

    private VAPRecordBeginDeserializer() {
    }

    public static VAPRecordBeginMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        VAPRecordBeginMessage vapRecordBegin = new VAPRecordBeginMessage(version);

        try {
            // Read the audio id
            long audioId = message.readUnsignedInt();
            vapRecordBegin.setAudioId(audioId);
            // Read the codec
            Codec codec = Codec.fromNMAINCodecId(message.readUnsignedShort());
            if (codec == Codec.UNKNOWN) {
                    logger.warn("Invalid Codec in VAP RECORD BEGIN message");
                throw new CorruptedFrameException("Invalid Codec in VAP RECORD BEGIN message");
            }
            vapRecordBegin.setCodec(codec);
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid VAP RECORD BEGIN message", iobe);
            throw new CorruptedFrameException("Invalid VAP RECORD BEGIN message");
        }

        return vapRecordBegin;
    }
}
