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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxUnpacker;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxUnsupportedDataTypeException;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.buffer.ByteBuf;

/**
 * @author epigeon
 */
public class BCPBeginDeserializer implements IBCPBeginDeserializer {
    private static Logger logger = LogManager.getLogger(BCPBeginDeserializer.class);

    @Override
    public BCPBeginMessage deserialize(short version, byte[] sessionId, short transactionId,
                                       ByteBuf message, Session session) throws CorruptedFrameException {
        long pdxDataLength = readTotalPayloadLength(message);

        BCPBeginMessage bcpBegin = createBCPBeginMessage(version, sessionId, transactionId, message, pdxDataLength, session);

        return bcpBegin;
    }

    protected long readTotalPayloadLength(ByteBuf message) {
        return message.readUnsignedInt();
    }

    protected BCPBeginMessage createBCPBeginMessage(short version, byte[] sessionId, short transactionId,
                                                    ByteBuf message, long dataSize, Session session) throws CorruptedFrameException {
        BCPBeginMessage bcpBegin = new BCPBeginMessage(version, sessionId, transactionId);

        try {

            byte[] payload = new byte[(int) dataSize];
            // Read the payload
            message.readBytes(payload, 0, (int) dataSize);
            PdxDictionary queryBeginDict = parsePdxDictionary(payload);
            session.setPdxKeysAsciiEncoded(queryBeginDict.isKeysEncodedUsingAscii());
            bcpBegin.setQueryBegin(queryBeginDict);
            bcpBegin.setPayload(payload);
            bcpBegin.setUserId(session.getUserId());

        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP BEGIN message", iobe);
            throw new CorruptedFrameException("Invalid BCP BEGIN message");
        }
        return bcpBegin;
    }

    protected PdxDictionary parsePdxDictionary(byte[] payload)
            throws CorruptedFrameException {
        try {

            PdxDictionary queryBegin = PdxUnpacker.unpackDict(payload, 0);

            return queryBegin;
        } catch (PdxException e) {
            logger.warn("Invalid BCP BEGIN message payload: " + e.getMessage(), e);
            throw new CorruptedFrameException("Invalid BCP BEGIN message payload: " + e.getMessage());
        } catch (PdxUnsupportedDataTypeException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Invalid BCP BEGIN message payload: " + e.getMessage(), e);
            }
            throw new CorruptedFrameException("Invalid BCP BEGIN message payload: " + e.getMessage());
        }
    }

    @Override
    public Version getVersion() {
        return BCPDefaultParams.VERSION;
    }
}
