/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.TranscodeData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 *
 */
public class BCPTranscodeHandler
{

  private BCPTranscodeHandler()
  {
  }
  
  public static void handleMessage(Session session, BCPTranscodeMessage message, ISessionListener sessionListener)
  {
    TranscodeData transcodeData = new TranscodeData();
    transcodeData.setAudioId(message.getAudioId());
    transcodeData.setCodec(message.getCodec());
    transcodeData.setTransportProtocol(message.getTransportProtocol());

    NMSPSessionController.getInstance().transcode(session, message.getTransactionId(), message.getRequestId(), transcodeData, session.getClientAudioListener(), sessionListener);
  }
}
