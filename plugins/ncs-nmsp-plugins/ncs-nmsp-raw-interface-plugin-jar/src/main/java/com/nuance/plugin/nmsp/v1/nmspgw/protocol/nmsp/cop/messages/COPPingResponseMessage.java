/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages;


/**
 * @author epigeon
 * 
 */
public class COPPingResponseMessage extends COPMessage
{
  private long requestId;
  private long latencyEstimate;

  /**
   * @param version
   */
  public COPPingResponseMessage(short version)
  {
    super(version, COPMessage.COP_PING_RESPONSE);
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
   * @param latencyEstimate
   *          the latencyEstimate to set
   */
  public void setLatencyEstimate(long latencyEstimate)
  {
    this.latencyEstimate = latencyEstimate;
  }

  /**
   * @return the latencyEstimate
   */
  public long getLatencyEstimate()
  {
    return latencyEstimate;
  }

  public String toString() {
    return super.toString() + " rid: " + requestId + " latencyEstimate: " + latencyEstimate;
  }
}
