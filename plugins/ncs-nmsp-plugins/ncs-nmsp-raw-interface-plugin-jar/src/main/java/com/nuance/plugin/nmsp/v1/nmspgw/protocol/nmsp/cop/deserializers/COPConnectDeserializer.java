/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.COPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class COPConnectDeserializer implements ICOPConnectDeserializer {
    private static Logger logger = LogManager.getLogger(COPConnectDeserializer.class);

    @Override
    public COPConnectMessage deserialize(short version, ByteBuf message)
            throws CorruptedFrameException {
        COPConnectMessage copConnect = new COPConnectMessage(version);

        validateCOPConnectPayLoad(message);

        while (message.isReadable()) {
            parseCOPData(message, copConnect);
        }

        validateMandatoryParameters(copConnect);

        return copConnect;
    }

    protected void parseCOPData(ByteBuf message, COPConnectMessage copConnect) throws CorruptedFrameException {

        long length;
        short connectDataType;

        try {
            // Read the data length
            length = message.readUnsignedInt() - 1;
            // Read the type
            connectDataType = message.readUnsignedByte();
        } catch (IndexOutOfBoundsException iobe) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid COP CONNECT message", iobe);
            }
            throw new CorruptedFrameException("Invalid COP CONNECT message");
        }

        byte[] payload = new byte[(int) length];
        try {
            // Read the data
            message.readBytes(payload, 0, (int) length);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid COP CONNECT message");
            throw new CorruptedFrameException("Invalid COP CONNECT message", iobe);
        }

        switch (connectDataType) {
            case COPConnectMessage.CONNECTION_TYPE_NMSP:
                COPConnectSAXHandler handler = new COPConnectSAXHandler(copConnect);
                NMSPMessageDeserializer.parseXMLPayload(payload, handler);
                break;
            case COPConnectMessage.CONNECTION_TYPE_APP:
                copConnect.setConnectData(payload);
                break;
            default:
                logger.warn("Invalid COP CONNECT message: invalid connect data type");
                throw new CorruptedFrameException("Invalid COP CONNECT message: invalid connect data type");
        }
    }

    protected void validateMandatoryParameters(COPConnectMessage copConnect)
            throws CorruptedFrameException {
        if (copConnect.getFromCodec() == null) {
            logger.warn("Invalid COP CONNECT message: missing mandatory element <fsc>");
            throw new CorruptedFrameException("Invalid COP CONNECT message: missing mandatory element <fsc>");
        }
        if (copConnect.getToCodec() == null) {
            logger.warn("Invalid COP CONNECT message: missing mandatory element <tsc>");
            throw new CorruptedFrameException("Invalid COP CONNECT message: missing mandatory element <tsc>");
        }
        if (copConnect.getNmaid() == null) {
            logger.warn("Invalid COP CONNECT message: missing mandatory element <nmaid>");
            throw new CorruptedFrameException("Invalid COP CONNECT message: missing mandatory element <nmaid>");
        }
//    if (copConnect.getUserId() == null) {
//      if (logger.isWarnEnabled()) {
//        logger.warn("Invalid COP CONNECT message: missing mandatory element <uid>");
//      }
//      throw new CorruptedFrameException("Invalid COP CONNECT message: missing mandatory element <uid>");
//    }
    }

    protected void validateCOPConnectPayLoad(ByteBuf message)
            throws CorruptedFrameException {
        if (message.readableBytes() <= 0) {
            logger.warn("Invalid COP CONNECT message: empty message");
            throw new CorruptedFrameException("Invalid COP CONNECT message: empty message");
        }
    }

    @Override
    public Version getVersion() {
        return COPDefaultParams.VERSION;
    }
}
