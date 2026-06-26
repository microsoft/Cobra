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

import com.nuance.plugin.nmsp.v1.nmspgw.data.Grammar;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadGrammarMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class BCPLoadGrammarDeserializer {
    private static Logger logger = LogManager.getLogger(BCPLoadGrammarDeserializer.class);

    private BCPLoadGrammarDeserializer() {
    }

    public static BCPLoadGrammarMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPLoadGrammarMessage bcpLoadGrammar = new BCPLoadGrammarMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            BCPLoadGrammarSAXHandler handler = new BCPLoadGrammarSAXHandler(bcpLoadGrammar);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
            int index = 1; // The primary payload index is 0
            while (message.isReadable()) {
                // Read the binary payload's length
                length = message.readUnsignedInt();
                payload = new byte[(int) length];
                message.readBytes(payload);
                // Insert the binary data in the proper grammar based on the index...
                for (Grammar grammar : bcpLoadGrammar.getGrammars()) {
                    if (grammar.getIndex() == index) {
                        grammar.setGrammar(payload);
                        break;
                    }
                }
                index++;
            }
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP LOAD GRAMMAR message", iobe);
            throw new CorruptedFrameException("Invalid BCP LOAD GRAMMAR message");
        }

        // Validate mandatory parameters
        if (bcpLoadGrammar.getRequestId() == -1) {
            logger.warn("Invalid BCP LOAD GRAMMAR message: missing mandatory element 'request id'");
            throw new CorruptedFrameException("Invalid BCP LOAD GRAMMAR message: missing mandatory element 'request id'");
        }

        return bcpLoadGrammar;
    }
}
