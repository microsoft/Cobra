package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;


import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Utterance;

import java.util.Vector;

public class BCPTTSServiceRequestMessage extends BCPMessage {

    private long requestId = -1;
    private long audioId = -1;

    private Vector<Parameter> parameters = new Vector<Parameter>();
    private Vector<Utterance> utterances = new Vector<Utterance>();

    private long bitPerSecond;

    public BCPTTSServiceRequestMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_TTS_SERVICE_REQUEST);
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public void setBitPerSecond(long bps) {
        this.bitPerSecond = bps;
    }

    public long getBitPerSecond() {
        return bitPerSecond;
    }

    /**
     * @return the parameters
     */
    public Vector<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return super.toString() + " rid: " + requestId + " utterances: " + utterances.toString() + " parameters: "
                + parameters.toString();
    }

    public Vector<Utterance> getUtterances() {
        return utterances;
    }

    public void setUtterances(Vector<Utterance> utterances) {
        this.utterances = utterances;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }
}
