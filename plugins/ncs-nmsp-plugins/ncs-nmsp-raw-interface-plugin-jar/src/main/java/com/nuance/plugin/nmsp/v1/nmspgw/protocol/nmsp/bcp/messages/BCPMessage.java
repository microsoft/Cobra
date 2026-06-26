/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 * 
 */
public class BCPMessage extends NMSPMessage
{
  // client commands
  public static final int BCP_GENERATE_AUDIO   = 0x0A00;
  public static final int BCP_LOAD_GRAMMAR     = 0x0A01;
  public static final int BCP_RECOGNIZE        = 0x0A02;
  public static final int BCP_CANCEL           = 0x0A03;
  public static final int BCP_RETRIEVE_ARTIFACTS = 0x0A04;
  public static final int BCP_SET_PARAMS       = 0x0A15;
  public static final int BCP_GET_PARAMS       = 0x0A17;
  public static final int BCP_END              = 0x0A26;
  public static final int BCP_LOAD_RESOURCE    = 0x0A27;
  public static final int BCP_FREE_RESOURCE    = 0x0A29;
  public static final int BCP_FREE_RESOURCE_ID = 0x0A30;
  public static final int BCP_INTERPRET        = 0x0A31;
  public static final int BCP_TRANSCODE        = 0x0A40;
  public static final int BCP_LOG              = 0x0A50;
  // server commands
  public static final int BCP_RESPONSE                  = 0x0A10;
  public static final int BCP_GENERATE_AUDIO_COMPLETE   = 0x0A11;
  public static final int BCP_START_OF_SPEECH           = 0x0A12;
  public static final int BCP_RECOGNITION_COMPLETE      = 0x0A13;
  public static final int BCP_RECOGNITION_INTERMRESULTS = 0x0A14;
  public static final int BCP_SET_PARAMS_COMPLETE       = 0x0A16;
  public static final int BCP_GET_PARAMS_COMPLETE       = 0x0A18;
  public static final int BCP_CANCEL_RESPONSE           = 0x0A20;
  public static final int BCP_RETRIEVE_ARTIFACTS_COMPLETE = 0x0A21;
  public static final int BCP_EVENT                     = 0x0A28;
  public static final int BCP_INTERPRETATION_COMPLETE   = 0x0A32;
  public static final int BCP_END_OF_SPEECH             = 0x0A34;
  public static final int BCP_LOAD_RESOURCE_COMPLETE    = 0x0A35;
  public static final int BCP_TRANSCODE_COMPLETE        = 0x0A41;
  public static final int BCP_LOG_RESPONSE              = 0x0A51;
  public static final int BCP_MDS_MESSAGE_RESPONSE      = 0x0A61;
  public static final int BCP_TTS_SERVICE_REQUEST       = 0x0A70;

  // 2-way commands
  public static final int BCP_DATA            = 0x0A19;
  public static final int BCP_BEGIN           = 0x0A25;
  public static final int BCP_RESOURCE_TUNNEL = 0x0A33;
  public static final int BCP_MDS_MESSAGE     = 0x0A60;

  public static final int BCP_INVALID_REQUEST_CODE      = 0x0258;
  public static final int BCP_RESOURCE_UNAVAILABLE_CODE = 0x0259;

  private byte[] sessionId = new byte[Session.SESSION_ID_LENGTH];
  private short transactionId;
  protected byte[] payload;

  // Supported versions
  public static final short BCP_MINIMUM_LEGACY_VERSION = 0x22;
  public static final short BCP_MINIMUM_VERSION = 0x23;
  public static final short BCP_MAXIMUM_VERSION = 0x23;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   * @param command
   */
  public BCPMessage(short version, byte[] sessionId, short transactionId, int command)
  {
    super(NMSPMessage.BCP, version, command);

    this.setSessionId(sessionId);
    this.transactionId = transactionId;
  }

  /**
   * @param sessionId
   *          the sessionId to set
   */
  public void setSessionId(byte[] sessionId)
  {
    this.sessionId = sessionId;
  }

  /**
   * @return the sessionId
   */
  public byte[] getSessionId()
  {
    return sessionId;
  }

  /**
   * @param transactionId
   *          the transactionId to set
   */
  public void setTransactionId(short transactionId)
  {
    this.transactionId = transactionId;
  }

  /**
   * @return the transactionId
   */
  public short getTransactionId()
  {
    return transactionId;
  }

  public String getCommandName()
  {
    return commandToString(getCommand());
  }
  
  @Override
  public String toString()
  {
    return commandToString(getCommand()) + " protocolVersion: " + getVersion() + " transactionID: " + transactionId;
  }
  
  public static String commandToString(int command)
  {
    switch(command) {
      case BCP_GENERATE_AUDIO:
        return "BCP_GENERATE_AUDIO";
      case BCP_LOAD_GRAMMAR:
        return "BCP_LOAD_GRAMMAR";
      case BCP_RECOGNIZE:
        return "BCP_RECOGNIZE";
      case BCP_CANCEL:
        return "BCP_CANCEL";
      case BCP_RETRIEVE_ARTIFACTS:
        return "BCP_RETRIEVE_ARTIFACTS";
      case BCP_SET_PARAMS:
        return "BCP_SET_PARAMS";
      case BCP_GET_PARAMS:
        return "BCP_GET_PARAMS";
      case BCP_END:
        return "BCP_END";
      case BCP_LOAD_RESOURCE:
        return "BCP_LOAD_RESOURCE";
      case BCP_FREE_RESOURCE:
        return "BCP_FREE_RESOURCE";
      case BCP_FREE_RESOURCE_ID:
        return "BCP_FREE_RESOURCE_ID";
      case BCP_INTERPRET:
        return "BCP_INTERPRET";
      case BCP_TRANSCODE:
        return "BCP_TRANSCODE";
      case BCP_LOG:
        return "BCP_LOG";
      // server commands
      case BCP_RESPONSE:
        return "BCP_RESPONSE";
      case BCP_GENERATE_AUDIO_COMPLETE:
        return "BCP_GENERATE_AUDIO_COMPLETE";
      case BCP_START_OF_SPEECH:
        return "BCP_START_OF_SPEECH";
      case BCP_RECOGNITION_COMPLETE:
        return "BCP_RECOGNITION_COMPLETE";
      case BCP_RECOGNITION_INTERMRESULTS:
        return "BCP_RECOGNITION_INTERMRESULTS";
      case BCP_SET_PARAMS_COMPLETE:
        return "BCP_SET_PARAMS_COMPLETE";
      case BCP_GET_PARAMS_COMPLETE:
        return "BCP_GET_PARAMS_COMPLETE";
      case BCP_CANCEL_RESPONSE:
        return "BCP_CANCEL_RESPONSE";
      case BCP_RETRIEVE_ARTIFACTS_COMPLETE:
        return "BCP_RETRIEVE_ARTIFACTS_COMPLETE";       
      case BCP_EVENT:
        return "BCP_EVENT";
      case BCP_INTERPRETATION_COMPLETE:
        return "BCP_INTERPRETATION_COMPLETE";
      case BCP_END_OF_SPEECH:
        return "BCP_END_OF_SPEECH";
      case BCP_LOAD_RESOURCE_COMPLETE:
        return "BCP_LOAD_RESOURCE_COMPLETE";
      case BCP_TRANSCODE_COMPLETE:
        return "BCP_TRANSCODE_COMPLETE";
      case BCP_LOG_RESPONSE:
        return "BCP_LOG_RESPONSE";
      case BCP_MDS_MESSAGE_RESPONSE:
        return "BCP_MDS_MESSAGE_RESPONSE";
      // 2-way commands
      case BCP_DATA:
        return "BCP_DATA";
      case BCP_BEGIN:
        return "BCP_BEGIN";
      case BCP_RESOURCE_TUNNEL:
        return "BCP_RESOURCE_TUNNEL";
      case BCP_MDS_MESSAGE:
        return "BCP_MDS_MESSAGE";
      case BCP_TTS_SERVICE_REQUEST:
          return "BCP_TTS_SERVICE_REQUEST";
      default:
        return "UNKNOWN_COMMAND";
    }
  }
  
  public final void setPayload(final byte[] payload)
  {
    if (payload != null)
    {
      this.payload = payload;
    }
  }

  public final byte[] getPayload()
  {
    return payload;
  }
  

  public final int getPayloadLength()
  {
    return payload == null ? 0 : payload.length;
  }
}
