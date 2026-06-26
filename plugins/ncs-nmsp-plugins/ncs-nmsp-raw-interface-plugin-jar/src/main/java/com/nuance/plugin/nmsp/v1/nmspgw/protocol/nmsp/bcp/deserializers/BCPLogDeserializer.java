/**
 *
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLogMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author hgultekin
 *
 */
public class BCPLogDeserializer {
    private static Logger logger = LogManager.getLogger(BCPLogDeserializer.class);

    private BCPLogDeserializer() {
    }

    public static BCPLogMessage deserialize(short version, byte[] sessionId, short transactionId, ByteBuf message) throws CorruptedFrameException {
        BCPLogMessage bcpLog = new BCPLogMessage(version, sessionId, transactionId);

        try {
            // Read the request id
            long requestId = message.readUnsignedInt();
            bcpLog.setRequestId(requestId);
            // Read the payload's length
            long length = message.readUnsignedInt();
            byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            bcpLog.setPayload(payload);
        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid BCP LOG message", iobe);
            throw new CorruptedFrameException("Invalid BCP LOG message");
        }

        return bcpLog;
    }
}
