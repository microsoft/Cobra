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
public class BCPLoadResourceCompleteMessage extends BCPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 11;

  private long requestId;
  private long resourceId;
  private int completionCause;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPLoadResourceCompleteMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_LOAD_RESOURCE_COMPLETE);
  }

  public int getMessageLength()
  {
    return messageStaticLength;
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
   * @param resourceId
   *          the resourceId to set
   */
  public void setResourceId(long resourceId)
  {
    this.resourceId = resourceId;
  }

  /**
   * @return the resourceId
   */
  public long getResourceId()
  {
    return resourceId;
  }

  /**
   * @param completionCause
   *          the completionCause to set
   */
  public void setCompletionCause(int completionCause)
  {
    this.completionCause = completionCause;
  }

  /**
   * @return the completionCause
   */
  public int getCompletionCause()
  {
    return completionCause;
  }

  @Override
  public String toString() {
    return super.toString() + " requestId: " + requestId + " resourceId:" + resourceId + " completionCause: " + completionCause;
  }
}
