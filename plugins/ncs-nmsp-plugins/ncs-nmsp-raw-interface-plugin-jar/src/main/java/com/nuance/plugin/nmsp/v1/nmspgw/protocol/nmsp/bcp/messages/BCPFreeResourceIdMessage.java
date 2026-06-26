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

import java.util.Hashtable;


/**
 * @author epigeon
 * 
 */
public class BCPFreeResourceIdMessage extends BCPMessage
{
  //mandatory elements
  private long requestId = -1;
  
  private Hashtable<Long, Long> resources; // <ResourceId, Timeout>

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPFreeResourceIdMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_FREE_RESOURCE_ID);
    this.payload = new byte[0];

    this.setResources(new Hashtable<Long, Long>());
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
   * @param resources
   *          the resources to set
   */
  public void setResources(Hashtable<Long, Long> resources)
  {
    this.resources = resources;
  }

  /**
   * @return the resources
   */
  public Hashtable<Long, Long> getResources()
  {
    return resources;
  }

  @Override
  public String toString() {
    return super.toString() + " rid: " + requestId;
  }
}
