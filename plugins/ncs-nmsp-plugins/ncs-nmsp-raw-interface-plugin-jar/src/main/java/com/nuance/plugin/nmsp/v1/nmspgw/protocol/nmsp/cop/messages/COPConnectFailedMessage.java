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
public class COPConnectFailedMessage extends COPMessage {
    public static final int CONNECT_FAILURE_BUSY = 0;
    public static final int CONNECT_FAILURE_SERVER_UNAVAILABLE = 1;
    public static final int CONNECT_FAILURE_UNKNOWN = 2;
    public static final int CONNECT_FAILURE_UNKNOWN_COMMAND = 3;
    public static final int CONNECT_FAILURE_RECORD_CODEC_UNSUPPORTED = 4;
    public static final int CONNECT_FAILURE_PLAY_CODEC_UNSUPPORTED = 5;
    public static final int CONNECT_FAILURE_PLAY_RECORD_CODECS_UNSUPPORTED = 6;
    public static final int CONNECT_FAILURE_FORBIDDEN = 403;
    public static final int CONNECT_FAILURE_NOT_FOUND = 404;
    public static final int CONNECT_FAILURE_PRE_CONDITION_FAILED = 412;

    private static int messageStaticLength = NMSPMessage.headerSize + 6;

    private int reasonCode;
    private String reasonText = "";

    /**
     * @param version
     */
    public COPConnectFailedMessage(short version) {
        super(version, COPMessage.COP_CONNECT_FAILED);
    }

    public int getMessageLength() {
        return messageStaticLength + reasonText.getBytes().length;
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
     * @param reasonText
     *          the reasonText to set
     */
    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    /**
     * @return the reasonText
     */
    public String getReasonText() {
        return reasonText;
    }

}
