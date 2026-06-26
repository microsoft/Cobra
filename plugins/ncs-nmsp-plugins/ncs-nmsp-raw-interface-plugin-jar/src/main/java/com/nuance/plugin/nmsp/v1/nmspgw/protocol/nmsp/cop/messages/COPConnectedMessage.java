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
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 *
 */
public class COPConnectedMessage extends COPMessage {
    private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 4;

    private byte[] sessionId = null;
    private float pingInterval = -1.0f;
    private int pingMissedBeforeDisconnect = -1;

    /**
     * @param version
     * @param sessionId
     */
    public COPConnectedMessage(short version, byte[] sessionId) {
        super(version, COPMessage.COP_CONNECTED);
        this.sessionId = sessionId;
    }

    public int getMessageLength() {
        int length = messageStaticLength;
        if (pingInterval != -1.0f) {
            length = +Session.PING_INTERVAL_SECS_STRING.getBytes().length + 1 + Float.toString(pingInterval).getBytes().length;
        }
        if (pingMissedBeforeDisconnect != -1) {
            length = +Session.PING_MISSED_RESPONSES_BEFORE_DISCONNECT.getBytes().length + 1 + Integer.toString(pingMissedBeforeDisconnect).getBytes().length;
        }

        return length;
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
     * @param pingInterval
     *          the pingInterval to set
     */
    public void setPingInterval(float pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * @return the pingInterval
     */
    public float getPingInterval() {
        return pingInterval;
    }

    /**
     * @param pingMissedBeforeDisconnect
     *          the pingMissedBeforeDisconnect to set
     */
    public void setPingMissedBeforeDisconnect(int pingMissedBeforeDisconnect) {
        this.pingMissedBeforeDisconnect = pingMissedBeforeDisconnect;
    }

    /**
     * @return the pingMissedBeforeDisconnect
     */
    public int getPingMissedBeforeDisconnect() {
        return pingMissedBeforeDisconnect;
    }

}
