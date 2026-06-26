/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages;


/**
 * @author epigeon
 * 
 */
public class COPConfirmMessage extends COPMessage
{
  //mandatory elements
  private byte[] encryptedSessionId = null;

  /**
   * @param version
   */
  public COPConfirmMessage(short version)
  {
    super(version, COPMessage.COP_CONFIRM);
  }

  /**
   * @param encryptedSessionId
   *          the encryptedSessionId to set
   */
  public void setEncryptedSessionId(byte[] encryptedSessionId)
  {
    this.encryptedSessionId = encryptedSessionId;
  }

  /**
   * @return the encryptedSessionId
   */
  public byte[] getEncryptedSessionId()
  {
    return encryptedSessionId;
  }

}
