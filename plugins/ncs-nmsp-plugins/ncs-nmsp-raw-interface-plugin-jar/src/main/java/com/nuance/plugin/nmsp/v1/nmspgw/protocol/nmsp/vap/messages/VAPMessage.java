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
public class VAPMessage extends NMSPMessage
{
  // client commands
  public static final int VAP_RECORD_END       = 0x0100;
  public static final int VAP_RECORD_BEGIN     = 0x0101;
  public static final int VAP_RECORD           = 0x0201;
//  public static final int VAP_BACKUP_REQUEST   = 0x0440;
//  public static final int VAP_RECOVER_REQUEST  = 0x0442;
  public static final int VAP_RELEASE          = 0x0444;
  // server commands
  public static final int VAP_PLAY             = 0x0200;
  public static final int VAP_PLAY_BEGIN       = 0x0210;
  public static final int VAP_PLAY_END         = 0x0400;
  public static final int VAP_END_OF_SENTENCE  = 0x0401;
//  public static final int VAP_BACKUP_RESPONSE  = 0x0441;
//  public static final int VAP_RECOVER_RESPONSE = 0x0443;

  // Supported versions
  public static final short VAP_MINIMUM_VERSION = 0x12;
  public static final short VAP_MAXIMUM_VERSION = 0x13;

  //mandatory elements
  private long audioId = -1;

  /**
   * @param version
   */
  public VAPMessage(short version, int command)
  {
    super(NMSPMessage.VAP, version, command);
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
   * @return the audioId
   */
  public long getAudioId()
  {
    return audioId;
  }
  
  public String toString() {
    return commandToString(getCommand()) + " protocolVersion: " + getVersion() + " audioID: " + audioId;
  }

  public static String commandToString(int command) {
    switch(command) {
      case VAP_RECORD_END:
        return "VAP_RECORD_END";
      case VAP_RECORD_BEGIN:
        return "VAP_RECORD_BEGIN";
      case VAP_RECORD:
        return "VAP_RECORD";
      case VAP_RELEASE:
        return "VAP_RELEASE";
      case VAP_PLAY:
        return "VAP_PLAY";
      case VAP_PLAY_BEGIN:
        return "VAP_PLAY_BEGIN";
      case VAP_PLAY_END:
        return "VAP_PLAY_END";
      case VAP_END_OF_SENTENCE:
        return "VAP_END_OF_SENTENCE";
      default:
        return "UNKNOWN_COMMAND";
    }
  }
}
