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


/**
 * @author epigeon
 * 
 */
public class BCPFreeResourceMessage extends BCPMessage
{
  private long resourceId = 0;
  private long timeout;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPFreeResourceMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_FREE_RESOURCE);
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
   * @param timeout
   *          the timeout to set
   */
  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

  /**
   * @return the timeout
   */
  public long getTimeout()
  {
    return timeout;
  }

  @Override
  public String toString() {
    return super.toString() + " resourceId: " + resourceId + " timeout: " + timeout;
  }
}
