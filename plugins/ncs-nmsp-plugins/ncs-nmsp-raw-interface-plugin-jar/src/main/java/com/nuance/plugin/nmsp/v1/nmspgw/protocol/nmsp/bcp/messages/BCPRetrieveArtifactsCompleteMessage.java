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


import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

public class BCPRetrieveArtifactsCompleteMessage extends BCPMessage {
    private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 11;

    private final long requestId;
    private final long audioId;
    private final int completionCause;

    public BCPRetrieveArtifactsCompleteMessage(
            final short version,
            final byte[] sessionId,
            final short transactionId,
            final long requestId,
            final long audioId,
            final int completionCause) {
        super(version, sessionId, transactionId, BCPMessage.BCP_RETRIEVE_ARTIFACTS_COMPLETE);
        this.requestId = requestId;
        this.audioId = audioId;
        this.completionCause = completionCause;
    }

    public int getMessageLength() {
        return messageStaticLength;
    }

    public long getRequestId() {
        return requestId;
    }

    public long getAudioId() {
        return audioId;
    }

    public int getCompletionCause() {
        return completionCause;
    }

    @Override
    public String toString() {
        return super.toString() + " requestId: " + requestId + " audioId: " + audioId + " completionCause: " + completionCause;
    }
}
