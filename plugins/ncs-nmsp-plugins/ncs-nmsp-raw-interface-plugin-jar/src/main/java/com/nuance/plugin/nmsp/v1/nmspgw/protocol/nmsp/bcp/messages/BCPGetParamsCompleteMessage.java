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
public class BCPGetParamsCompleteMessage extends BCPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 11;

  private long requestId;
  private int statusCode;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPGetParamsCompleteMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_GET_PARAMS_COMPLETE);
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
   * @param statusCode
   *          the statusCode to set
   */
  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  /**
   * @return the statusCode
   */
  public int getStatusCode()
  {
    return statusCode;
  }

  @Override
  public String toString() {
    return super.toString() + " requestId: " + requestId + " statusCode: " + statusCode;
  }
}
