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

import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Utterance;

import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class BCPGenerateAudioMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;
    private long audioId = -1;

    private long resourceId = 0;
    private long bufferSize = -1;
    private long bitPerSecond = -1;
    private boolean endOfSentence = false;
    private Vector<Utterance> utterances = new Vector<Utterance>();
    private Vector<Parameter> parameters = new Vector<Parameter>();

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPGenerateAudioMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_GENERATE_AUDIO);
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
     * @param bufferSize
     *          the bufferSize to set
     */
    public void setBufferSize(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @return the bufferSize
     */
    public long getBufferSize() {
        return bufferSize;
    }

    /**
     * @param endOfSentence
     *          the endOfSentence to set
     */
    public void setEndOfSentence(boolean endOfSentence) {
        this.endOfSentence = endOfSentence;
    }

    /**
     * @return the endOfSentence
     */
    public boolean isEndOfSentence() {
        return endOfSentence;
    }

    /**
     * @param utterances
     *          the utterances to set
     */
    public void setUtterances(Vector<Utterance> utterances) {
        this.utterances = utterances;
    }

    /**
     * @return the utterances
     */
    public Vector<Utterance> getUtterances() {
        return utterances;
    }

    /**
     * @return the parameters
     */
    public Vector<Parameter> getParameters() {
        return parameters;
    }

    public void setBitPerSecond(long bps) {
        this.bitPerSecond = bps;
    }

    public long getBitPerSecond() {
        return bitPerSecond;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " resid: " + resourceId + " audioId: " + audioId;
    }
}
