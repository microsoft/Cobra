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
public class COPDisconnectMessage extends COPMessage {
    public static final int DISCONNECT_REASON_NORMAL = 0;
    public static final int DISCONNECT_REASON_TIMEOUT = 1;
    public static final int DISCONNECT_REASON_SERVER_SHUTDOWN = 2;
    public static final int DISCONNECT_REASON_CRITICAL_ERROR = 3;
    public static final int DISCONNECT_REASON_PING_TIMEOUT = 4;
    public static final int DISCONNECT_REASON_SERVER_UNAVAILABLE = 5;
    public static final int DISCONNECT_REASON_NETWORK_EXCEPTION = 6;
    public static final int DISCONNECT_REASON_MAX_AUDIO = 7;
    public static final int DISCONNECT_REASON_UNKNOWN = 8;
    public static final int DISCONNECT_REASON_AUTHENTIFICATION_DATA_ERROR = 9;

    private static int messageStaticLength = NMSPMessage.headerSize + 6;

    private int reasonCode;
    private byte[] data;

    /**
     * @param version
     */
    public COPDisconnectMessage(short version) {
        super(version, COPMessage.COP_DISCONNECT);
        this.data = new byte[0];
    }

    public int getMessageLength() {
        return messageStaticLength + data.length;
    }

    /**
     * @param reasonCode
     *          the reasonCode to set
     */
    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    /**
     * @return the reasonCode
     */
    public int getReasonCode() {
        return reasonCode;
    }

    /**
     * @param data
     *          the data to set
     */
    public void setData(byte[] data) {
        if (data != null) {
            this.data = data;
        }
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return super.toString() + " reason: " + reasonCode;
    }
}
