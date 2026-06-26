/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages;


/**
 * @author epigeon
 *
 */
public class VAPReleaseMessage extends VAPMessage {
    private byte[] sessionId = null;
    private byte[] requestSessionId = null;

    /**
     * @param version
     */
    public VAPReleaseMessage(short version) {
        super(version, VAPMessage.VAP_RELEASE);
    }

    /**
     * @param sessionId
     *          the sessionId to set
     */
    public void setSessionId(byte[] sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the sessionId
     */
    public byte[] getSessionId() {
        return sessionId;
    }

    /**
     * @param requestSessionId
     *          the requestSessionId to set
     */
    public void setRequestSessionId(byte[] requestSessionId) {
        this.requestSessionId = requestSessionId;
    }

    /**
     * @return the requestSessionId
     */
    public byte[] getRequestSessionId() {
        return requestSessionId;
    }

}
