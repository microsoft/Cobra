/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.data;

import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;

import java.util.Vector;

/**
 * @author epigeon
 * 
 */
public class GenerateAudioData
{
  private long audioId;
  private long bufferSize;
  private boolean endOfSentence;
  private Vector<Utterance> utterances;
  private IAudioListener audioListener;
  private Vector<Parameter> parameters;
  private long bitPerSecond = -1;

  /**
   * @return the audioId
   */
  public long getAudioId()
  {
    return audioId;
  }

  /**
   * @param audioId
   *          the audioId to set
   */
  public void setAudioId(long audioId)
  {
    this.audioId = audioId;
  }

  /**
   * @return the bufferSize
   */
  public long getBufferSize()
  {
    return bufferSize;
  }

  /**
   * @param bufferSize
   *          the bufferSize to set
   */
  public void setBufferSize(long bufferSize)
  {
    this.bufferSize = bufferSize;
  }

  /**
   * @return the endOfSentence
   */
  public boolean isEndOfSentence()
  {
    return endOfSentence;
  }

  /**
   * @param endOfSentence
   *          the endOfSentence to set
   */
  public void setEndOfSentence(boolean endOfSentence)
  {
    this.endOfSentence = endOfSentence;
  }

  /**
   * @return the utterances
   */
  public Vector<Utterance> getUtterances()
  {
    return utterances;
  }

  /**
   * @param utterances
   *          the utterances to set
   */
  public void setUtterances(Vector<Utterance> utterances)
  {
    this.utterances = utterances;
  }

  /**
   * @param audioListener the audioListener to set
   */
  public void setAudioListener(IAudioListener audioListener)
  {
    this.audioListener = audioListener;
  }

  /**
   * @return the audioListener
   */
  public IAudioListener getAudioListener()
  {
    return audioListener;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(Vector<Parameter> parameters)
  {
    this.parameters = parameters;
  }

  /**
   * @return the parameters
   */
  public Vector<Parameter> getParameters()
  {
    return parameters;
  }

  public long getBitPerSecond() {
    return bitPerSecond;
  }

  public void setBitPerSecond(long bitPerSecond) {
    this.bitPerSecond = bitPerSecond;
  }
}
