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
import com.nuance.plugin.nmsp.v1.nmspgw.data.LoadGrammarData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadGrammarMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPLoadGrammarHandler
{

  private BCPLoadGrammarHandler()
  {
  }

  public static void handleMessage(Session session, BCPLoadGrammarMessage message, ISessionListener sessionListener)
  {
    LoadGrammarData loadGrammarData = new LoadGrammarData();
    loadGrammarData.setPoolName(message.getPoolName());
    loadGrammarData.setGrammars(message.getGrammars());

    NMSPSessionController.getInstance().loadGrammar(session, message.getTransactionId(), message.getRequestId(), message.getResourceId(), loadGrammarData, sessionListener);
  }

}
