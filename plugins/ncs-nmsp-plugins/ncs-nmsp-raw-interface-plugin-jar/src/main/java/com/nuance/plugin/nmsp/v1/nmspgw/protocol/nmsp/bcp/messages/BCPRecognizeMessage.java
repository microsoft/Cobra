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
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;

import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class BCPRecognizeMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;
    private long audioId = -1;

    private long resourceId = 0;
    private long audioRangeBegin;
    private long audioRangeEnd;
    private String poolName = null;
    private Vector<Parameter> parameters = new Vector<Parameter>();
    private Vector<Grammar> grammars = new Vector<Grammar>();

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPRecognizeMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_RECOGNIZE);
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
     * @param audioId
     *          the audioId to set
     */
    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    /**
     * @return the audioId
     */
    public long getAudioId() {
        return audioId;
    }

    /**
     * @param audioRangeBegin
     *          the audioRangeBegin to set
     */
    public void setAudioRangeBegin(long audioRangeBegin) {
        this.audioRangeBegin = audioRangeBegin;
    }

    /**
     * @return the audioRangeBegin
     */
    public long getAudioRangeBegin() {
        return audioRangeBegin;
    }

    /**
     * @param audioRangeEnd
     *          the audioRangeEnd to set
     */
    public void setAudioRangeEnd(long audioRangeEnd) {
        this.audioRangeEnd = audioRangeEnd;
    }

    /**
     * @return the audioRangeEnd
     */
    public long getAudioRangeEnd() {
        return audioRangeEnd;
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
     * @param parameters
     *          the parameters to set
     */
    public void setParameters(Vector<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the parameters
     */
    public Vector<Parameter> getParameters() {
        return parameters;
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
        return super.toString() + " rid: " + requestId + " resid: " + resourceId + " audioId: " + audioId;
    }
}
