/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;

import java.util.Optional;


public class BCPRetrieveArtifactsMessage extends BCPMessage {
    private Long requestId;
    private Long audioId;

    private Long resourceId;

    private String targetSessionId;

    private Optional<Long> bufferSize = Optional.empty();
    private Optional<Long> bitPerSecond = Optional.empty();

    public BCPRetrieveArtifactsMessage(final short version, final byte[] sessionId, final short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_RETRIEVE_ARTIFACTS);
    }

    public void setRequestId(final Long requestId) {
        this.requestId = requestId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setResourceId(final Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setAudioId(final Long audioId) {
        this.audioId = audioId;
    }

    public Long getAudioId() {
        return audioId;
    }

    public void setBufferSize(final Optional<Long> bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Optional<Long> getBufferSize() {
        return bufferSize;
    }

    public void setBitPerSecond(final Optional<Long> bitPerSecond) {
        this.bitPerSecond = bitPerSecond;
    }

    public Optional<Long> getBitPerSecond() {
        return bitPerSecond;
    }

    public String getTargetSessionId() {
        return targetSessionId;
    }

    public void setTargetSessionId(final String targetSessionId) {
        this.targetSessionId = targetSessionId;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " resid: " + resourceId + " audioId: " + audioId + " targetSessionId: " + targetSessionId + " bitPerSecond: " + bitPerSecond;
    }
}
