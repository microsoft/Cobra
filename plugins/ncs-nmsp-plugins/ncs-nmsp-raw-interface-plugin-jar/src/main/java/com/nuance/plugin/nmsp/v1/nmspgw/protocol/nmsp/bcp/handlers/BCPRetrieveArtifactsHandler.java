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
import com.nuance.plugin.nmsp.v1.nmspgw.data.RetrieveArtifactsData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRetrieveArtifactsMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

import java.util.Optional;

public class BCPRetrieveArtifactsHandler
{

  private BCPRetrieveArtifactsHandler()
  {
  }

  public static void handleMessage(final Session session, final BCPRetrieveArtifactsMessage message, final ISessionListener sessionListener) {
    final RetrieveArtifactsData retrieveArtifactsData =
            new RetrieveArtifactsData(
                    message.getAudioId(),
                    getBufferSize(session, message),
                    message.getBitPerSecond(),
                    message.getTargetSessionId(),
                    session.getClientAudioListener());

    NMSPSessionController.getInstance().retrieveArtifacts(session, message.getTransactionId(), message.getRequestId(), message.getResourceId(), retrieveArtifactsData, sessionListener);
  }
  
  private static long getBufferSize(final Session session, final BCPRetrieveArtifactsMessage message) {
    final Optional<Long> bufferSize = message.getBufferSize();
    if(bufferSize.isPresent()) {
      return bufferSize.get();
    }
    final int copConnectBufferSize = session.getBufferSize();
    return copConnectBufferSize;
  }
}
