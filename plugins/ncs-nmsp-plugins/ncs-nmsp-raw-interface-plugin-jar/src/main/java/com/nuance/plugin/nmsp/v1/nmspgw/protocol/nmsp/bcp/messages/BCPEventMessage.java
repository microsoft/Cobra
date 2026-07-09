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
public class BCPEventMessage extends BCPMessage
{
  public static final int EVENT_CODE_CLIENT_DISCONNECTED            = 0x0000;
  public static final int EVENT_CODE_CLIENT_SOCKET_SHUTDOWN         = 0x0001;
  public static final int EVENT_CODE_CLIENT_PING_TIMEOUT            = 0x0002;
  public static final int EVENT_CODE_CLIENT_IDLE_TIMEOUT            = 0x0003;
  public static final int EVENT_CODE_NSS_DISCONNECT                 = 0x0004;
  public static final int EVENT_CODE_NSS_ERROR                      = 0x0005;
  public static final int EVENT_CODE_INTERNAL_ERROR                 = 0x0006;
  public static final int EVENT_CODE_APPSERVER_DISCONNECT           = 0x0007;
  public static final int EVENT_CODE_APPSERVER_ERROR                = 0x0008;
  public static final int EVENT_CODE_CLIENT_STARTED_NEW_TRANSACTION = 0x0009;
  public static final int EVENT_CODE_RESOURCE_UNLOADED              = 0x000A;

  private static int messageStaticLength = NMSPMessage.headerSize + Session.SESSION_ID_LENGTH + 7;

  private int eventCode;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPEventMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_EVENT);
    this.payload = new byte[0];
  }

  public int getMessageLength()
  {
    return messageStaticLength + payload.length;
  }

  /**
   * @param eventCode
   *          the eventCode to set
   */
  public void setEventCode(int eventCode)
  {
    this.eventCode = eventCode;
  }

  /**
   * @return the eventCode
   */
  public int getEventCode()
  {
    return eventCode;
  }

  @Override
  public String toString() {
    return super.toString() + " eventCode: " + eventCode;
  }
}
