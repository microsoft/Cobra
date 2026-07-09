/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.client;

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * Defines the interface for receiving audio
 * 
 * @author epigeon
 *
 */
public interface IAudioListener
{
  /**
   * @param session
   * @param requestId
   * @param audioId
   * @param codec
   */
  void audioReceivedStart(Session session, long requestId, long audioId, Codec codec);
  
  /**
   * @param session
   * @param requestId
   * @param audioId
   * @param codec
   * @param audio
   * @param endOfSpeech
   * @param segmentEnding
   */
  void audioReceived(Session session, long requestId, long audioId, Codec codec, byte[] audio, int startOfSpeechMs, boolean endOfSpeech, boolean segmentEnding);

  void subAudioReceived(Session session, long requestId, long audioId, Codec codec, byte[] audio, int startOfSpeechMs, boolean endOfSpeech, boolean isAudioStart);
}
