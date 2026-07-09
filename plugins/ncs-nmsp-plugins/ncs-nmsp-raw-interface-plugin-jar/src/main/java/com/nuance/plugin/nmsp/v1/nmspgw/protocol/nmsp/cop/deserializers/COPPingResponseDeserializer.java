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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPPingResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class COPPingResponseDeserializer {
    private static Logger logger = LogManager.getLogger(COPPingResponseDeserializer.class);

    private COPPingResponseDeserializer() {
    }

    public static COPPingResponseMessage deserialize(short version, ByteBuf message) throws CorruptedFrameException {
        COPPingResponseMessage copPingResponse = new COPPingResponseMessage(version);

        try {
            // Read the request id
            long requestId = message.readUnsignedInt();
            copPingResponse.setRequestId(requestId);
            // Read the estimated latency
            long latencyEstimate = message.readUnsignedInt();
            copPingResponse.setLatencyEstimate(latencyEstimate);
        } catch (IndexOutOfBoundsException iobe) {
            logger.warn("Invalid COP PING RESPONSE message", iobe);
            throw new CorruptedFrameException("Invalid COP PING RESPONSE message");
        }

        return copPingResponse;
    }

}
