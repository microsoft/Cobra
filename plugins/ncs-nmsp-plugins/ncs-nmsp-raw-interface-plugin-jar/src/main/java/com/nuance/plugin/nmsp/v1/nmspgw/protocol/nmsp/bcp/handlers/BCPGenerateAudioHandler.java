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
import com.nuance.plugin.nmsp.v1.nmspgw.data.GenerateAudioData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGenerateAudioMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPGenerateAudioHandler
{

  private BCPGenerateAudioHandler()
  {
  }

  public static void handleMessage(Session session, BCPGenerateAudioMessage message, ISessionListener sessionListener)
  {
    GenerateAudioData generateAudioData = new GenerateAudioData();
    generateAudioData.setAudioId(message.getAudioId());
    if(message.getBufferSize() == -1) {
      // Use buffer size sent in COP CONNECT
      generateAudioData.setBufferSize(session.getBufferSize());
    }
    else {
      // Use the one in BCP GENERATE AUDIO
      generateAudioData.setBufferSize(message.getBufferSize());
    }
    generateAudioData.setEndOfSentence(message.isEndOfSentence());
    generateAudioData.setUtterances(message.getUtterances());
    generateAudioData.setAudioListener(session.getClientAudioListener());
    generateAudioData.setParameters(message.getParameters());
    generateAudioData.setBitPerSecond(message.getBitPerSecond());

    NMSPSessionController.getInstance().generateAudio(session, message.getTransactionId(), message.getRequestId(), message.getResourceId(), generateAudioData, sessionListener);
  }

}
