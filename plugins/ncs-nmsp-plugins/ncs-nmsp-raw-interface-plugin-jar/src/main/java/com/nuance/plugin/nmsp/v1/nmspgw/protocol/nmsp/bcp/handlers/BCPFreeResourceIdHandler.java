/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPFreeResourceIdMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author epigeon
 * 
 */
public class BCPFreeResourceIdHandler
{

  private BCPFreeResourceIdHandler()
  {
  }

  public static void handleMessage(Session session, BCPFreeResourceIdMessage message)
  {
    Hashtable<Long, Long> resources = message.getResources();
    Enumeration<Long> resourceIds = resources.keys();
    while (resourceIds.hasMoreElements())
    {
      Long resourceId = resourceIds.nextElement();
      NMSPSessionController.getInstance().freeResource(session, message.getTransactionId(), resourceId, resources.get(resourceId));
    }
  }

}
