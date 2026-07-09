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

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.ResourceType;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class BCPLoadResourceMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;
    private ResourceType resourceType = null;

    private long resourceId = 0;
    private String userName = "";
    private Vector<Parameter> parameters = new Vector<Parameter>();
    private Codec codec = Codec.UNKNOWN;
    private Utils.DeferNSSLoading deferNSSLoading = Utils.DeferNSSLoading.OFF;
    private Map<String, String> recoCapabilities = new HashMap<String, String>();
    private Map<String, String> ttsCapabilities = new HashMap<String, String>();
    private Map<String, String> dictationCapabilities = new HashMap<String, String>();
    private int clientAudioStreamingMaxIdleTimeMSec = 0;

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPLoadResourceMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_LOAD_RESOURCE);
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
     * @param userName
     *          the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param resourceType
     *          the resourceType to set
     */
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * @return the resourceType
     */
    public ResourceType getResourceType() {
        return resourceType;
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
     * @param codec the codec to set
     */
    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    /**
     * @return the codec
     */
    public Codec getCodec() {
        return codec;
    }

    public Utils.DeferNSSLoading getDeferNSSLoading() {
        return deferNSSLoading;
    }

    public void setDeferNSSLoading(Utils.DeferNSSLoading deferNSSLoading) {
        this.deferNSSLoading = deferNSSLoading;
    }

    /**
     * @param recoCapabilities the recoCapabilities to set
     */
    public void setRecoCapabilities(Map<String, String> recoCapabilities) {
        this.recoCapabilities.clear();
        this.recoCapabilities.putAll(recoCapabilities);
    }

    /**
     * @return the recoCapabilities
     */
    public Map<String, String> getRecoCapabilities() {
        return recoCapabilities;
    }

    /**
     * @param ttsCapabilities the ttsCapabilities to set
     */
    public void setTtsCapabilities(Map<String, String> ttsCapabilities) {
        this.ttsCapabilities.clear();
        this.ttsCapabilities.putAll(ttsCapabilities);
    }

    /**
     * @return the ttsCapabilities
     */
    public Map<String, String> getTtsCapabilities() {
        return ttsCapabilities;
    }

    /**
     * @param dictationCapabilities the dictationCapabilities to set
     */
    public void setDictationCapabilities(Map<String, String> dictationCapabilities) {
        this.dictationCapabilities.clear();
        this.dictationCapabilities.putAll(dictationCapabilities);
    }

    /**
     * @return the dictationCapabilities
     */
    public Map<String, String> getDictationCapabilities() {
        return dictationCapabilities;
    }

    /**
     * @param clientAudioStreamingMaxIdleTimeMSec
     *          the max idle time allowed for audio to be idle to set
     */
    public void setClientAudioStreamingMaxIdleTimeMSec(int clientAudioStreamingMaxIdleTimeMSec) {
        this.clientAudioStreamingMaxIdleTimeMSec = clientAudioStreamingMaxIdleTimeMSec;
    }

    /**
     * @return the clientAudioStreamingMaxIdleTimeMSec
     */
    public int getClientAudioStreamingMaxIdleTimeMSec() {
        return clientAudioStreamingMaxIdleTimeMSec;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " resid: " + resourceId + " type: " + resourceType.name();
    }
}
