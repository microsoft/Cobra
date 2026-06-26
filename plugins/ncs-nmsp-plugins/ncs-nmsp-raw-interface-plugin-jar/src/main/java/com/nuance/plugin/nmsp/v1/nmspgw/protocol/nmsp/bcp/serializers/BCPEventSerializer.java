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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPEventMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class BCPEventSerializer
{
  private BCPEventSerializer()
  {
  }

  public static ByteBuf serialize(BCPEventMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    buffer.writeBytes(message.getSessionId());

    // Write the transaction id
    buffer.writeByte(message.getTransactionId());
    // Write the event code
    buffer.writeShort(message.getEventCode());
    // Write payload length
    buffer.writeInt(message.getPayload().length);
    // Write the payload
    buffer.writeBytes(message.getPayload());

    return buffer;
  }

}
