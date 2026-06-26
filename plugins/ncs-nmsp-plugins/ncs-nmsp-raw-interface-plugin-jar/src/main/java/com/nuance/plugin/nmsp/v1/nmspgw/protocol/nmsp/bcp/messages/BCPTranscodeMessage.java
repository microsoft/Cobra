/**
 *
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;


/**
 * @author epigeon
 *
 */
public class BCPTranscodeMessage extends BCPMessage {
    //mandatory elements
    private long requestId = -1;
    private long audioId = -1;
    private int codec = -1;
    private int transportProtocol = -1;

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPTranscodeMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_TRANSCODE);
    }

    /**
     * @return the requestId
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the audioId
     */
    public long getAudioId() {
        return audioId;
    }

    /**
     * @param audioId the audioId to set
     */
    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    /**
     * @return the codec
     */
    public int getCodec() {
        return codec;
    }

    /**
     * @param codec the codec to set
     */
    public void setCodec(int codec) {
        this.codec = codec;
    }

    /**
     * @return the transportProtocol
     */
    public int getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * @param transportProtocol the transportProtocol to set
     */
    public void setTransportProtocol(int transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " audioId: " + audioId + " codec: " + codec + " protocol: " + transportProtocol;
    }
}
