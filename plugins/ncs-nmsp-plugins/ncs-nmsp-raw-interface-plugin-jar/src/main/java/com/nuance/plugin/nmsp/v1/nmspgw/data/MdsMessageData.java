/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.data;

/**
 * @author epigeon
 *
 */
public class MdsMessageData
{
  private int requestId;
  private String serviceId;
  private String to;
  private byte[] message;
  private long ttl;
  
  /**
   * @return the requestId
   */
  public int getRequestId()
  {
    return requestId;
  }
  
  /**
   * @param requestId the requestId to set
   */
  public void setRequestId(int requestId)
  {
    this.requestId = requestId;
  }
  
  /**
   * @return the serviceId
   */
  public String getServiceId()
  {
    return serviceId;
  }
  
  /**
   * @param serviceId the serviceId to set
   */
  public void setServiceId(String serviceId)
  {
    this.serviceId = serviceId;
  }
  
  /**
   * @return the message
   */
  public byte[] getMessage()
  {
    return message;
  }
  
  /**
   * @param message the message to set
   */
  public void setMessage(byte[] message)
  {
    this.message = message;
  }
  
  /**
   * @return the ttl
   */
  public long getTtl()
  {
    return ttl;
  }
  
  /**
   * @param ttl the ttl to set
   */
  public void setTtl(long ttl)
  {
    this.ttl = ttl;
  }

  /**
   * @param to the to to set
   */
  public void setTo(String to)
  {
    this.to = to;
  }

  /**
   * @return the to
   */
  public String getTo()
  {
    return to;
  }
  
}
