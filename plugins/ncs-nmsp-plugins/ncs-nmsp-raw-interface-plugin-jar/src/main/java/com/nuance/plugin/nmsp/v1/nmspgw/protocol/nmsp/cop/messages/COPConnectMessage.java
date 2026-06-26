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

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioContainerType;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;

import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class COPConnectMessage extends COPMessage {
    public static final short CONNECTION_TYPE_NMSP = 0;
    public static final short CONNECTION_TYPE_APP = 1;

    // mandatory elements
    private Codec fromCodec = null;
    private Codec toCodec = null;
    private String nmaid = null;
    private String parent = null;
    private String userId = null;
    // optional elements
    private String deviceId = null;
    private byte[] connectData = null;
    private String sessionId = null;
    private short deviceType;
    private String reconnectSessionId = null;
    private String buildNumber = null;
    private int bufferSize = 0; // 0 means no limit
    private boolean disableCallLogging = false;
    private boolean connectToMds = false;
    private Vector<Parameter> parameters = new Vector<Parameter>();

    private AudioContainerType audioContainerType = null;

    /**
     * @param version
     */
    public COPConnectMessage(short version) {
        super(version, COPMessage.COP_CONNECT);
    }

    public AudioContainerType getAudioContainerType() {
        return audioContainerType;
    }

    public void setAudioContainerType(AudioContainerType audioContainerType) {
        this.audioContainerType = audioContainerType;
    }

    /**
     * @param connectData
     *          the connectData to set
     */
    public void setConnectData(byte[] connectData) {
        this.connectData = connectData;
    }

    /**
     * @return the connectData
     */
    public byte[] getConnectData() {
        return connectData;
    }

    /**
     * @return the fromCodec
     */
    public Codec getFromCodec() {
        return fromCodec;
    }

    /**
     * @param fromCodec
     *          the fromCodec to set
     */
    public void setFromCodec(Codec fromCodec) {
        this.fromCodec = fromCodec;
    }

    /**
     * @return the toCodec
     */
    public Codec getToCodec() {
        return toCodec;
    }

    /**
     * @param toCodec
     *          the toCodec to set
     */
    public void setToCodec(Codec toCodec) {
        this.toCodec = toCodec;
    }

    /**
     * @return the nmaid
     */
    public String getNmaid() {
        return nmaid;
    }

    /**
     * @param nmaid
     *          the nmaid to set
     */
    public void setNmaid(String nmaid) {
        this.nmaid = nmaid;
    }

    /**
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * @param parent
     *          the parent to set
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId
     *          the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the deviceType
     */
    public short getDeviceType() {
        return deviceType;
    }

    /**
     * @param deviceType
     *          the deviceType to set
     */
    public void setDeviceType(short deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * @return the reconnectSessionId
     */
    public String getReconnectSessionId() {
        return reconnectSessionId;
    }

    /**
     * @param reconnectSessionId
     *          the reconnectSessionId to set
     */
    public void setReconnectSessionId(String reconnectSessionId) {
        this.reconnectSessionId = reconnectSessionId;
    }

    /**
     * @return the buildNumber
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber
     *          the buildNumber to set
     */
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize
     *          the bufferSize to set
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *          the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return the deviceId
     */
    public String getDeviceId() {
        return deviceId;
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

    public void setDisableCallLogging(boolean disableCallLogging) {
        this.disableCallLogging = disableCallLogging;
    }

    public boolean isDisableCallLogging() {
        return disableCallLogging;
    }

    public boolean isConnectToMds() {
        return connectToMds;
    }

    public void setConnectToMds(boolean connectToMds) {
        this.connectToMds = connectToMds;
    }

    @Override
    public String toString() {
        return super.toString() + " fromCodec: " + fromCodec.name() + " toCodec: " + toCodec.name() + " nmaid: " + nmaid;
    }
}
