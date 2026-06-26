/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPDisconnectMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class COPDisconnectHandler
{

  private COPDisconnectHandler()
  {
  }

  public static void handleMessage(Session session, COPDisconnectMessage message)
  {
    session.setDisconnectionReason("client sent COP_DISCONNECT");
    NMSPSessionController.getInstance().clientDisconnected(session, message.getReasonCode());
  }
}
