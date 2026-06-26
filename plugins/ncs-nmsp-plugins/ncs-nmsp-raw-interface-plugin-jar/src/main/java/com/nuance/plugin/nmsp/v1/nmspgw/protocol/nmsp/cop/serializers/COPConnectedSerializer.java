/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectedMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class COPConnectedSerializer
{
  private COPConnectedSerializer()
  {
  }

  public static ByteBuf serialize(COPConnectedMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    // Write the session id
    buffer.writeBytes(message.getSessionId());

    ByteBuf payload = Unpooled.buffer(128).order(NMSPMessage.byteOrder);

    if (message.getPingInterval() != -1.0f)
    {
      payload.writeBytes(("\"" + Session.PING_INTERVAL_SECS_STRING + "=" + message.getPingInterval() + "\"").getBytes());
    }
    if (message.getPingMissedBeforeDisconnect() != -1)
    {
      payload.writeBytes(("\"" + Session.PING_MISSED_RESPONSES_BEFORE_DISCONNECT + "=" + message.getPingMissedBeforeDisconnect() + "\"").getBytes());
    }

    // Write payload length
    buffer.writeInt(payload.readableBytes());
    // Write payload
    buffer.writeBytes(payload);

    return buffer;
  }
}
