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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.session.Session;

import java.util.HashMap;

/**
 * @author epigeon
 *
 */
public class BCPBeginMessage extends BCPMessage {
    private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 5;

    private static final String UID = "uid";

    private HashMap<String, Object> queryBeginInfo;
    private String remoteParentEventId = null;
    private String userId = "";
    private String nmaid = "";

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPBeginMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_BEGIN);
        this.payload = new byte[0];
    }

    public int getMessageLength() {
        return messageStaticLength + payload.length;
    }

    /**
     * @param queryBegin the queryBegin to set
     */
    public void setQueryBegin(PdxDictionary queryBegin) {
        this.queryBeginInfo = queryBegin;
    }

    /**
     * @return the queryBeginInfo
     */
    public HashMap<String, Object> getQueryBeginInfo() {
        return queryBeginInfo;
    }

    /**
     * @param remoteParentEventId the remoteParentEventId to set
     */
    public void setRemoteParentEventId(String remoteParentEventId) {
        this.remoteParentEventId = remoteParentEventId;
    }

    /**
     * @return the remoteParentEventId
     */
    public String getRemoteParentEventId() {
        return remoteParentEventId;
    }

    /**
     * @return the queryBeginUserId
     */
    public String getQueryBeginUserId() {
        return (String) queryBeginInfo.get(UID);
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param nmaid the nmaid to set
     */
    public void setNmaid(String nmaid) {
        this.nmaid = nmaid;
    }

    /**
     * @return the nmaid
     */
    public String getNmaid() {
        return nmaid;
    }

    @Override
    public String toString() {
        if (queryBeginInfo != null) {
            String queryBeginInfoStr = queryBeginInfo.toString();
            queryBeginInfoStr = queryBeginInfoStr.replace(userId, "");
            return super.toString() + " QueryBegin: " + queryBeginInfoStr;
        } else {
            return super.toString();
        }
    }
}
