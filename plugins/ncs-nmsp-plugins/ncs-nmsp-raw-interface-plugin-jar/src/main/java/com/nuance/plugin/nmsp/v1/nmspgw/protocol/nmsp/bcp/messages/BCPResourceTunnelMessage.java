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

/**
 * @author epigeon
 *
 */
public class BCPResourceTunnelMessage extends BCPMessage {
    private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 9;

    private long resourceId = 0;

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPResourceTunnelMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_RESOURCE_TUNNEL);
        this.payload = new byte[0];
    }

    public int getMessageLength() {
        return messageStaticLength + payload.length;
    }

    /**
     * @param resourceId
     *          the resourceId to set
     */
    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * @return the resourceId
     */
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + " resid: " + resourceId;
    }
}
