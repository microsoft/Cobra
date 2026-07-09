/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPMdsDeserializer {
    private static Logger logger = LogManager.getLogger(NMSPClientManager.class);

    private BCPMdsDeserializer() {
    }

    public static BCPMdsMessage deserialize(short version, byte[] sessionId, ByteBuf message) throws CorruptedFrameException {
        BCPMdsMessage bcpMdsMessage = new BCPMdsMessage(version, sessionId);

        try {
            // Read the message info length
            long length = message.readUnsignedInt();
            byte[] messageInfo = new byte[(int) length];
            // Read the message info
            message.readBytes(messageInfo);
            BCPMdsSAXHandler handler = new BCPMdsSAXHandler(bcpMdsMessage);
            NMSPMessageDeserializer.parseXMLPayload(messageInfo, handler);
            // Read the message length
            length = message.readUnsignedInt();
            byte[] mdsMessage = new byte[(int) length];
            // Read the message
            message.readBytes(mdsMessage);
            bcpMdsMessage.setMessage(mdsMessage);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP MDS MESSAGE message", iobe);
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message");
        }

        // Validate mandatory parameters
        if (bcpMdsMessage.getRequestId() == -1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.REQUEST_ID_TAG + "'");
            }
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.REQUEST_ID_TAG + "'");
        }
        if (bcpMdsMessage.getServiceId() == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.SERVICE_ID_TAG + "'");
            }
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.SERVICE_ID_TAG + "'");
        }
        if (bcpMdsMessage.getTo() == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.TO_TAG + "'");
            }
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.TO_TAG + "'");
        }
        if (bcpMdsMessage.getTtl() == -1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.TTL_TAG + "'");
            }
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message: missing mandatory element '" + BCPMdsMessage.TTL_TAG + "'");
        }
        if (bcpMdsMessage.getMessage().length == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP MDS MESSAGE message: message is empty");
            }
            throw new CorruptedFrameException("Invalid BCP MDS MESSAGE message: message is empty");
        }

        return bcpMdsMessage;
    }
}
