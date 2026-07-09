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
public class VAPEndOfSentenceMessage extends VAPMessage
{
  private static int messageStaticLength = NMSPMessage.headerSize + 4;

  /**
   * @param version
   */
  public VAPEndOfSentenceMessage(short version) {
    super(version, VAPMessage.VAP_END_OF_SENTENCE);
  }

  public int getMessageLength()
  {
    return messageStaticLength;
  }

}
