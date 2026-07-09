/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved. 
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
public class BCPMdsMessage extends BCPMessage
{
  public static final String MDS_TAG        = "mds";
  public static final String REQUEST_ID_TAG = "requestid";
  public static final String SERVICE_ID_TAG = "serviceid";
  public static final String TO_TAG         = "to";
  public static final String TTL_TAG        = "ttl";

  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 9;

  //mandatory elements
  private long requestId = -1;
  private String serviceId = null;
  private String to = null;
  private long ttl = -1;  
  private byte[] message;

  /**
   * @param version
   * @param sessionId
   */
  public BCPMdsMessage(short version, byte[] sessionId)
  {
    super(version, sessionId, (short) 0, BCPMessage.BCP_MDS_MESSAGE);
    this.message = new byte[0];
  }
  
  public int getMessageLength()
  {
    return messageStaticLength + getMessageInfo().length() + message.length;
  }

  public String getMessageInfo()
  {
    String messageInfo = "<" + MDS_TAG + ">";
    messageInfo += "<" + REQUEST_ID_TAG +">" + requestId + "</" + REQUEST_ID_TAG + ">";
    messageInfo += "<" + SERVICE_ID_TAG + ">" + serviceId + "</" + SERVICE_ID_TAG + ">";
    messageInfo += "<" + TO_TAG + ">" + to + "</" + TO_TAG + ">";
    messageInfo += "<" + TTL_TAG + ">" + ttl + "</" + TTL_TAG + ">";
    messageInfo += "</" + MDS_TAG + ">";
    
    return messageInfo;
  }
  
  /**
   * @param message
   *          the message to set
   */
  public void setMessage(byte[] message)
  {
    if (message != null)
    {
      this.message = message;
    }
  }

  /**
   * @return the message
   */
  public byte[] getMessage()
  {
    return message;
  }

  /**
   * @param serviceId the serviceId to set
   */
  public void setServiceId(String serviceId)
  {
    this.serviceId = serviceId;
  }

  /**
   * @return the serviceId
   */
  public String getServiceId()
  {
    return serviceId;
  }

  /**
   * @param ttl the ttl to set
   */
  public void setTtl(long ttl)
  {
    this.ttl = ttl;
  }

  /**
   * @return the ttl
   */
  public long getTtl()
  {
    return ttl;
  }

  /**
   * @param requestId the requestId to set
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

  @Override
  public String toString() {
    return super.toString() + " rid: " + requestId + " serviceId: " + serviceId + " to: " + to + " ttl: " + ttl;
  }
}
