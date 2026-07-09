/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;


import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 *
 */
public class BCPTranscodeCompleteMessage extends BCPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 15;

  private long requestId;
  private long audioId;
  private int completionCause;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPTranscodeCompleteMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_TRANSCODE_COMPLETE);
    this.payload = new byte[0];
  }

  public int getMessageLength()
  {
    return messageStaticLength + payload.length;
  }

  /**
   * @return the requestId
   */
  public long getRequestId()
  {
    return requestId;
  }

  /**
   * @param requestId the requestId to set
   */
  public void setRequestId(long requestId)
  {
    this.requestId = requestId;
  }

  /**
   * @return the audioId
   */
  public long getAudioId()
  {
    return audioId;
  }

  /**
   * @param audioId the audioId to set
   */
  public void setAudioId(long audioId)
  {
    this.audioId = audioId;
  }

  /**
   * @return the completionCause
   */
  public int getCompletionCause()
  {
    return completionCause;
  }

  /**
   * @param completionCause the completionCause to set
   */
  public void setCompletionCause(int completionCause)
  {
    this.completionCause = completionCause;
  }

  @Override
  public String toString() {
    return super.toString() + " requestId: " + requestId + " audioId: " + audioId + " completionCause: " + completionCause;
  }
}
