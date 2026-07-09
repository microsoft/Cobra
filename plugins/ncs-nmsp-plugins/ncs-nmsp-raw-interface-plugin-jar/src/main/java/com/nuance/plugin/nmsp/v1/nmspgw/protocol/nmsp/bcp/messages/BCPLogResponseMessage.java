/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
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
public class BCPLogResponseMessage extends BCPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 7;

  private long requestId;
  private short statusCode;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPLogResponseMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_LOG_RESPONSE);
  }

  /**
   * @return
   */
  public int getMessageLength()
  {
    return messageStaticLength;
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
   * @param statusCode the statusCode to set
   */
  public void setStatusCode(short statusCode)
  {
    this.statusCode = statusCode;
  }

  /**
   * @return the statusCode
   */
  public short getStatusCode()
  {
    return statusCode;
  }
  
  @Override
  public String toString() {
    return super.toString() + " requestId: " + requestId + " status code: " + statusCode;
  }
}
