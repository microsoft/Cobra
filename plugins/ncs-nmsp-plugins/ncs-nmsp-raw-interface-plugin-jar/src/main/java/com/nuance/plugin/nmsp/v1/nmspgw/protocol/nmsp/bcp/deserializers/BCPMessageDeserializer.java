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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPMessageDeserializer {
    private static Logger logger = LogManager.getLogger(BCPMessageDeserializer.class);

    private BCPMessageDeserializer() {
    }

    public static NMSPMessage deserialize(short version, int command, ByteBuf message, Session session) throws CorruptedFrameException {
        byte[] sessionId;
        short transactionId;
        try {
            // Read the session id
            sessionId = new byte[Session.SESSION_ID_LENGTH];
            message.readBytes(sessionId);
            // Read the transaction id
            transactionId = message.readUnsignedByte();
        } catch (IndexOutOfBoundsException iobe) {
            // Log error
            throw new CorruptedFrameException("Invalid BCP message", iobe);
        }

        switch (command) {
            case BCPMessage.BCP_GENERATE_AUDIO:
                return BCPGenerateAudioDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_LOAD_GRAMMAR:
                return BCPLoadGrammarDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_RECOGNIZE:
                return BCPRecognizeDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_CANCEL:
                return BCPCancelDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_RETRIEVE_ARTIFACTS:
                return BCPRetrieveArtifactsDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_SET_PARAMS:
                return BCPSetParamsDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_GET_PARAMS:
                return BCPGetParamsDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_DATA:
                return BCPDataDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_BEGIN:
                return BCPBeginDeserializerRegistry.getBCPBeginDeserializer(version).deserialize(version, sessionId, transactionId, message, session);
            case BCPMessage.BCP_END:
                return BCPEndDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_LOAD_RESOURCE:
                return BCPLoadResourceDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_FREE_RESOURCE:
                return BCPFreeResourceDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_FREE_RESOURCE_ID:
                return BCPFreeResourceIdDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_INTERPRET:
                return BCPInterpretDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_RESOURCE_TUNNEL:
                return BCPResourceTunnelDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_TRANSCODE:
                return BCPTranscodeDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_LOG:
                return BCPLogDeserializer.deserialize(version, sessionId, transactionId, message);
            case BCPMessage.BCP_MDS_MESSAGE:
                return BCPMdsDeserializer.deserialize(version, sessionId, message);
            case BCPMessage.BCP_MDS_MESSAGE_RESPONSE:
                return BCPMdsResponseDeserializer.deserialize(version, sessionId, message);
            case BCPMessage.BCP_TTS_SERVICE_REQUEST:
                return BCPTTSServiceRequestDeserializer.deserialize(version, sessionId, transactionId, message);
            default:
                logger.warn("Invalid BCP message: " + command);
                throw new CorruptedFrameException("Invalid BCP message: " + command);
        }
    }

}
