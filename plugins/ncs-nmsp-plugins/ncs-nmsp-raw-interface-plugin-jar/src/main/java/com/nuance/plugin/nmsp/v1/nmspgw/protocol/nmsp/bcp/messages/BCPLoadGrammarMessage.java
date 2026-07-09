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

import com.nuance.plugin.nmsp.v1.nmspgw.data.Grammar;

import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class BCPLoadGrammarMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;

    private long resourceId = 0;
    private String poolName = null;
    private Vector<Grammar> grammars = new Vector<Grammar>();

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPLoadGrammarMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_LOAD_GRAMMAR);
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

    /**
     * @param poolName
     *          the poolName to set
     */
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * @return the poolName
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * @param grammars
     *          the grammars to set
     */
    public void setGrammars(Vector<Grammar> grammars) {
        this.grammars = grammars;
    }

    /**
     * @return the grammars
     */
    public Vector<Grammar> getGrammars() {
        return grammars;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " resid: " + resourceId;
    }
}
