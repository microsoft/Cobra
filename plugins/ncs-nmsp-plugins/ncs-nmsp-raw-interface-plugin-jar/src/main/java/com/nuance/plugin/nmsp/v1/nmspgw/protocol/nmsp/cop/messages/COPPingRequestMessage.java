/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages;


import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;

/**
 * @author epigeon
 *
 */
public class COPPingRequestMessage extends COPMessage {
    private static int messageStaticLength = NMSPMessage.headerSize + 8;

    private long pingRequestId;
    private long previousRoundTripTime;

    /**
     * @param version
     */
    public COPPingRequestMessage(short version) {
        super(version, COPMessage.COP_PING_REQUEST);
    }

    public int getMessageLength() {
        return messageStaticLength;
    }

    /**
     * @param pingRequestId
     *          the pingRequestId to set
     */
    public void setPingRequestId(long pingRequestId) {
        this.pingRequestId = pingRequestId;
    }

    /**
     * @return the pingRequestId
     */
    public long getPingRequestId() {
        return pingRequestId;
    }

    /**
     * @param previousRoundTripTime
     *          the previousRoundTripTime to set
     */
    public void setPreviousRoundTripTime(long previousRoundTripTime) {
        this.previousRoundTripTime = previousRoundTripTime;
    }

    /**
     * @return the previousRoundTripTime
     */
    public long getPreviousRoundTripTime() {
        return previousRoundTripTime;
    }

}
