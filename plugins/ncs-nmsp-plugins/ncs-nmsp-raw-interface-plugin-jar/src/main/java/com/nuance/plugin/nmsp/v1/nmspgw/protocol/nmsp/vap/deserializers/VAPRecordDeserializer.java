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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * @author epigeon
 *
 */
public class VAPRecordDeserializer {
    private static Logger logger = LogManager.getLogger(VAPRecordDeserializer.class);

    private VAPRecordDeserializer() {
    }

    public static VAPRecordMessage deserialize(short version, ByteBuf message, Session session) throws CorruptedFrameException {
        VAPRecordMessage vapRecord = new VAPRecordMessage(version);

        try {
            // Read the audio id
            long audioId = message.readUnsignedInt();
            vapRecord.setAudioId(audioId);

            // Read the audio length
            long length = message.readUnsignedInt();

            String codec = session.getFromCodec().name().toLowerCase();
            final byte[] streamAudio = new byte[(int) length];
            message.readBytes(streamAudio);

            //This seems to be an undocumented behaviour.
            // For SPEEX and OPUS the client library adds extra bytes to the stream. The extra bytes containing the frame size.
            if (codec.startsWith("opus") || codec.startsWith("speex")) {
                ByteBuffer bytes = ByteBuffer.wrap(streamAudio);
                while (bytes.hasRemaining()) {
                    int size = 0;
                    int partialSize;

                    do {
                        byte sizeByte = bytes.get();
                        partialSize = Byte.toUnsignedInt(sizeByte);
                        size = size << 7 | (sizeByte & 127);
                    } while (partialSize >= 128 && size < 268435456);

                    logger.trace("Extracting {} frame of size {} from packet", codec, size);
                    byte[] opusFrame = new byte[size];
                    bytes.get(opusFrame);
                    vapRecord.addAudio(opusFrame);
                }
            } else {
                vapRecord.addAudio(streamAudio);
            }
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid VAP RECORD message", iobe);
            throw new CorruptedFrameException("Invalid VAP RECORD message");
        }

        return vapRecord;
    }
}
