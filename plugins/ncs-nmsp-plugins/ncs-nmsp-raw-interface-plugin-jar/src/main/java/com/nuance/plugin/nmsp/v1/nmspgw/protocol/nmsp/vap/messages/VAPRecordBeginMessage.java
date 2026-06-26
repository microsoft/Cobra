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


import com.nuance.plugin.ncs.utils.ICodecResource.Codec;

/**
 * @author epigeon
 * 
 */
public class VAPRecordBeginMessage extends VAPMessage
{
  //mandatory elements
  private Codec codec = null;

  /**
   * @param version
   */
  public VAPRecordBeginMessage(short version)
  {
    super(version, VAPMessage.VAP_RECORD_BEGIN);
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
    return super.toString() + " codec: " + codec.name();
  }
}
