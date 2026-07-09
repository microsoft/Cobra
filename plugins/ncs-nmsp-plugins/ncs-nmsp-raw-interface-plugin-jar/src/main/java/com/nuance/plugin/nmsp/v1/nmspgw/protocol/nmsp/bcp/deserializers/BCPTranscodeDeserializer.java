/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPTranscodeDeserializer {
    private static Logger logger = LogManager.getLogger(BCPTranscodeDeserializer.class);

    private BCPTranscodeDeserializer() {
    }

    public static BCPTranscodeMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPTranscodeMessage bcpTranscode = new BCPTranscodeMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            BCPTranscodeSAXHandler handler = new BCPTranscodeSAXHandler(bcpTranscode);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP TRANSCODE AUDIO message", iobe);
            throw new CorruptedFrameException("Invalid BCP TRANSCODE AUDIO message");
        }

        // Validate mandatory parameters
        if (bcpTranscode.getRequestId() == -1) {
            logger.warn("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'request id'");
            throw new CorruptedFrameException("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'request id'");
        }
        if (bcpTranscode.getAudioId() == -1) {
            logger.warn("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'audio id'");
            throw new CorruptedFrameException("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'audio id'");
        }
        if (bcpTranscode.getCodec() == -1) {
            logger.warn("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'codec'");
            throw new CorruptedFrameException("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'codec'");
        }
        if (bcpTranscode.getTransportProtocol() == -1) {
            logger.warn("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'protocol'");
            throw new CorruptedFrameException("Invalid BCP TRANSCODE AUDIO message: missing mandatory element 'protocol'");
        }

        return bcpTranscode;
    }
}
