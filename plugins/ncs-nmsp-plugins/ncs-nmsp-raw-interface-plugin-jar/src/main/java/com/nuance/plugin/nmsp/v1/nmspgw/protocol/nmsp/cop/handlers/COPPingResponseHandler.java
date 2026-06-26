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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPPingResponseMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class COPPingResponseHandler
{

  private COPPingResponseHandler()
  {
  }

  public static void handleMessage(Session session, COPPingResponseMessage message)
  {
    session.getClient().pingResponse(session, message.getRequestId(), message.getLatencyEstimate());
  }
}
