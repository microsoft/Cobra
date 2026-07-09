/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.MdsMessageData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPMdsMessageHandler
{

  private BCPMdsMessageHandler()
  {
  }

  public static void handleMessage(Session session, BCPMdsMessage message)
  {
    MdsMessageData mdsMessageData = new MdsMessageData();
    mdsMessageData.setRequestId((int) message.getRequestId());
    mdsMessageData.setServiceId(message.getServiceId());
    mdsMessageData.setTo(message.getTo());
    mdsMessageData.setTtl(message.getTtl());
    mdsMessageData.setMessage(message.getMessage());
    
    NMSPSessionController.getInstance().sendMdsMessage(session, mdsMessageData);
  }

}
