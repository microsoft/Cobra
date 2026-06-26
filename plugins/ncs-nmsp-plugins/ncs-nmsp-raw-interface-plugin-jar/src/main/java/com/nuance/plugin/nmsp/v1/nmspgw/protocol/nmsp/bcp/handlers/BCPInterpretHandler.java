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

import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.InterpretData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPInterpretMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPInterpretHandler
{

  private BCPInterpretHandler()
  {
  }

  public static void handleMessage(Session session, BCPInterpretMessage message, ISessionListener sessionListener)
  {
    InterpretData interpretData = new InterpretData();
    interpretData.setText(message.getText());
    interpretData.setPoolName(message.getPoolName());
    interpretData.setParameters(message.getParameters());
    interpretData.setGrammars(message.getGrammars());

    NMSPSessionController.getInstance().interpret(session, message.getTransactionId(), message.getRequestId(), message.getResourceId(), interpretData, sessionListener);
  }

}
