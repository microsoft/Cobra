/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages;


import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;

/**
 * @author epigeon
 * 
 */
public class VAPPlayMessage extends VAPMessage
{
  private static final int messageStaticLength = NMSPMessage.headerSize + 8;

  private byte[] audio;

  /**
   * @param version
   */
  public VAPPlayMessage(short version)
  {
    super(version, VAPMessage.VAP_PLAY);
    this.audio = new byte[0];
  }

  public int getMessageLength()
  {
    return messageStaticLength + audio.length;
  }

  /**
   * @param audio
   *          the audio to set
   */
  public void setAudio(byte[] audio)
  {
    if (audio != null)
    {
      this.audio = audio;
    }
  }

  /**
   * @return the audio
   */
  public byte[] getAudio()
  {
    return audio;
  }

  public String toString() {
    if(audio != null) {
      return super.toString() + " audio length: " + audio.length + " bytes";
    }
    else {
      return super.toString() + " audio length: 0 byte";
    }
  }
}
