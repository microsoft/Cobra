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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRetrieveArtifactsMessage;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class BCPRetrieveArtifactsDeserializer {
  private static Logger logger = LogManager.getLogger(BCPRetrieveArtifactsDeserializer.class);

    private BCPRetrieveArtifactsDeserializer() {
    }

    public static BCPRetrieveArtifactsMessage deserialize(final short version, final byte[] sessionId, final short transactionId, final ByteBuf message) throws CorruptedFrameException {
        final BCPRetrieveArtifactsMessage bcpRetrieveArtifacts = new BCPRetrieveArtifactsMessage(version, sessionId, transactionId);

        try {
            // Read the payload's length
            final long length = message.readUnsignedInt();
            final byte[] payload = new byte[(int) length];
            // Read the payload
            message.readBytes(payload);
            final BCPRetrieveArtifactsSAXHandler handler = new BCPRetrieveArtifactsSAXHandler(bcpRetrieveArtifacts);
            NMSPMessageDeserializer.parseXMLPayload(payload, handler);
        } catch (final IndexOutOfBoundsException iobe) {
            logger.warn("Invalid BCP  message", iobe);
            throw new CorruptedFrameException("Invalid BCP message");
        }

        checkMandatoryParameterPresence(bcpRetrieveArtifacts);

        return bcpRetrieveArtifacts;
    }

    private static void checkMandatoryParameterPresence(
            final BCPRetrieveArtifactsMessage bcpRetrieveArtifacts) throws CorruptedFrameException {
        try {
            Objects.requireNonNull(bcpRetrieveArtifacts.getRequestId(), "Invalid BCPRetrieveArtifactsMessage: missing requestId");
            Objects.requireNonNull(bcpRetrieveArtifacts.getAudioId(), "Invalid BCPRetrieveArtifactsMessage: missing audioId");
            Objects.requireNonNull(bcpRetrieveArtifacts.getResourceId(), "Invalid BCPRetrieveArtifactsMessage: missing resourceId");
            Objects.requireNonNull(bcpRetrieveArtifacts.getTargetSessionId(), "Invalid BCPRetrieveArtifactsMessage: missing targetSessionId");
        } catch (final NullPointerException npe) {
            final String message = "Invalid BCPRetrieveArtifactsMessage: " + npe.getMessage();
            logger.warn(message, npe);
            throw new CorruptedFrameException(message);
        }
    }
}
