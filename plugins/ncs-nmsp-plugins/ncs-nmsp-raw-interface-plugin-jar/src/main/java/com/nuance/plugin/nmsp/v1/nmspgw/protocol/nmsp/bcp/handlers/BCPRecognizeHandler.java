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
import com.nuance.plugin.nmsp.v1.nmspgw.data.RecognizeData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRecognizeMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPRecognizeHandler
{

  private BCPRecognizeHandler()
  {
  }

  public static void handleMessage(Session session, BCPRecognizeMessage message, ISessionListener sessionListener)
  {
    RecognizeData recognizeData = new RecognizeData();
    recognizeData.setAudioId(message.getAudioId());
    recognizeData.setPoolName(message.getPoolName());
    recognizeData.setParameters(message.getParameters());
    recognizeData.setGrammars(message.getGrammars());

    NMSPSessionController.getInstance().recognize(session, message.getTransactionId(), message.getRequestId(), message.getResourceId(), recognizeData, sessionListener);
  }

}
