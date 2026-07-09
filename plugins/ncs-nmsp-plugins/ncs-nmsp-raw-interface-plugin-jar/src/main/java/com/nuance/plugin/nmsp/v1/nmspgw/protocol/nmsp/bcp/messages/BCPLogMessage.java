/**
 *
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;


/**
 * @author hgultekin
 *
 */
public class BCPLogMessage extends BCPMessage {
    private long requestId;

    /**
     * @param version
     * @param sessionId
     * @param transactionId
     */
    public BCPLogMessage(short version, byte[] sessionId, short transactionId) {
        super(version, sessionId, transactionId, BCPMessage.BCP_LOG);
        this.payload = new byte[0];
    }

    /**
     * @param requestId the requestId to set
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

    @Override
    public String toString() {
        return super.toString() + " requestId: " + requestId;
    }
}
