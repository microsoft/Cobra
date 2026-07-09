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

import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPSetParamsMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class BCPSetParamsDeserializer {
    private static Logger logger = LogManager.getLogger(NMSPClientManager.class);

    private BCPSetParamsDeserializer() {
    }

    public static BCPSetParamsMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPSetParamsMessage bcpSetParams = new BCPSetParamsMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            BCPSetParamsSAXHandler handler = new BCPSetParamsSAXHandler(bcpSetParams);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
            int index = 1; // The primary payload index is 0
            while (message.isReadable()) {
                // Read the binary payload's length
                length = message.readUnsignedInt();
                payload = new byte[(int) length];
                message.readBytes(payload);
                // Insert the binary data in the proper parameter based on the index...
                for (Parameter param : bcpSetParams.getParameters()) {
                    if (param.getIndex() == index) {
                        param.setValue(new String(payload));
                        break;
                    }
                }
                index++;
            }
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP SET PARAMS message", iobe);
            throw new CorruptedFrameException("Invalid BCP SET PARAMS message");
        }

        // Validate mandatory parameters
        if (bcpSetParams.getRequestId() == -1) {
            logger.warn("Invalid BCP SET PARAMS message: missing mandatory element 'request id'");
            throw new CorruptedFrameException("Invalid BCP SET PARAMS message: missing mandatory element 'request id'");
        }

        return bcpSetParams;
    }
}
