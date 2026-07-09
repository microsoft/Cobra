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


/**
 * @author epigeon
 *
 */
public class BCPCancelMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;
    private long requestIdToCancel = -1;

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPCancelMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_CANCEL);
    }

    /**
     * @param requestId
     *          the requestId to set
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the requestId
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * @param requestIdToCancel
     *          the requestIdToCancel to set
     */
    public void setRequestIdToCancel(long requestIdToCancel) {
        this.requestIdToCancel = requestIdToCancel;
    }

    /**
     * @return the requestIdToCancel
     */
    public long getRequestIdToCancel() {
        return requestIdToCancel;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " reqIdToCancel: " + requestIdToCancel;
    }
}
