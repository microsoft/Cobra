/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;


import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPEndOfSpeechMessage extends BCPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 11;

  private long requestId;
  private int requestState;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPEndOfSpeechMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_END_OF_SPEECH);
    this.payload = new byte[0];
  }

  public int getMessageLength()
  {
    return messageStaticLength + payload.length;
  }

  /**
   * @param requestId
   *          the requestId to set
   */
  public void setRequestId(long requestId)
  {
    this.requestId = requestId;
  }

  /**
   * @return the requestId
   */
  public long getRequestId()
  {
    return requestId;
  }

  /**
   * @param requestState
   *          the requestState to set
   */
  public void setRequestState(int requestState)
  {
    this.requestState = requestState;
  }

  /**
   * @return the requestState
   */
  public int getRequestState()
  {
    return requestState;
  }

  @Override
  public String toString() {
    return super.toString() + " requestId: " + requestId + " requestState: " + requestState + " " + new String(payload);
  }
}
