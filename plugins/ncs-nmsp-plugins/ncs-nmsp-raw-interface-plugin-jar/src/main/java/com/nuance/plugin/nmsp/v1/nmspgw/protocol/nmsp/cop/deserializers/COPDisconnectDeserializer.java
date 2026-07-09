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

import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPDisconnectMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 *
 */
public class COPDisconnectDeserializer {
    private static Logger logger = LogManager.getLogger(NMSPClientManager.class);

    private COPDisconnectDeserializer() {
    }

    public static COPDisconnectMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        COPDisconnectMessage copDisconnect = new COPDisconnectMessage(version);

        try {
            // Read the reason code
            int reasonCode = message.readUnsignedShort();
            copDisconnect.setReasonCode(reasonCode);

            // The NMSP SDK doesn't seem to send the data length
            if (message.readableBytes() >= 4) {
                // Read the data's length
                long length = message.readUnsignedInt();
                if (message.readableBytes() >= length) {
                    byte[] data = new byte[(int) length];
                    // Read the data
                    message.readBytes(data);
                    copDisconnect.setData(data);
                }
            }

        } catch (IndexOutOfBoundsException iobe) {
                logger.warn("Invalid COP DISCONNECT message", iobe);
            throw new CorruptedFrameException("Invalid COP DISCONNECT message");
        }

        return copDisconnect;
    }

}
