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
import com.nuance.plugin.ncs.utils.ICodecResource.Codec;

/**
 * @author epigeon
 * 
 */
public class VAPPlayBeginMessage extends VAPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + 6;

  private Codec codec;

  /**
   * @param version
   */
  public VAPPlayBeginMessage(short version)
  {
    super(version, VAPMessage.VAP_PLAY_BEGIN);
  }

  public int getMessageLength()
  {
    return messageStaticLength;
  }

  /**
   * @param codec
   *          the codec to set
   */
  public void setCodec(Codec codec)
  {
    this.codec = codec;
  }

  /**
   * @return the codec
   */
  public Codec getCodec()
  {
    return codec;
  }

  public String toString() {
    return super.toString() + " codec: " + codec;
  }
}
