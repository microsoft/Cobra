/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRecognitionIntermediateResultsMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class BCPRecognitionIntermediateResultsSerializer
{
  private BCPRecognitionIntermediateResultsSerializer()
  {
  }

  public static ByteBuf serialize(BCPRecognitionIntermediateResultsMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    buffer.writeBytes(message.getSessionId());

    // Write the transaction id
    buffer.writeByte(message.getTransactionId());
    // Write the request id
    buffer.writeInt((int) message.getRequestId());
    // Write the completion cause
    buffer.writeShort(message.getCompletionCause());
    
    if(message.getPayload().length > 0) {
      // Write payload length
      buffer.writeInt(message.getPayload().length + 5);
      // Write the result type
      buffer.writeByte(message.getResultType());
      // Write the result length
      buffer.writeInt(message.getPayload().length);
      // Write the result
      buffer.writeBytes(message.getPayload());
    }
    else {
      // Write payload length
      buffer.writeInt(0);
    }

    return buffer;
  }

}
