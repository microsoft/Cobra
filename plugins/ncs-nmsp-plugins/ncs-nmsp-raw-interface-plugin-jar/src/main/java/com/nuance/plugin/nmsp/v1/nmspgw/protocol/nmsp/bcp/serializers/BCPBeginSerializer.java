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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.Optional;

/**
 * @author epigeon
 * 
 */
public class BCPBeginSerializer implements IBCPBeginSerializer
{
  @Override
  public ByteBuf serialize(final Session session, final BCPBeginMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    buffer.writeBytes(message.getSessionId());

    // Write the transaction id
    buffer.writeByte(message.getTransactionId());
    // Write payload length
    buffer.writeInt(message.getPayload().length);
    // Write the payload
    buffer.writeBytes(message.getPayload());
    // Write optionalPayload length
    String optionalPayload = buildOptionalParameters(session, message);
    byte[] optionalPayloadBytes = optionalPayload.getBytes();
    buffer.writeInt(optionalPayloadBytes.length);
    // Write the optionalPayload
    buffer.writeBytes(optionalPayloadBytes);
    return buffer;
  }

  protected String buildOptionalParameters(final Session session, final BCPBeginMessage message)
  {
    String optionalPayload = "";
    if(message.getRemoteParentEventId() != null) 
    {
      optionalPayload += "<remoteParentId>" + message.getRemoteParentEventId() + "</remoteParentId>";
    }

    optionalPayload += "<nmaid>" + message.getNmaid() + "</nmaid>";
    try {
      optionalPayload += "<obfuscatedUserId>" + session.getObfuscatedUserId() + "</obfuscatedUserId>";
      optionalPayload += "<storageDeviceId>" + session.getDeviceId() + "</storageDeviceId>";
    }
    catch(IOException e) {
      optionalPayload += "<obfuscatedUserId>INVALID</obfuscatedUserId>";
      optionalPayload += "<storageDeviceId>INVALID</storageDeviceId>";
    }
    optionalPayload += maxMessageSizeXMLElement(session.getDerogatoryMaxMessageSizeForCurrentNMAID());
    return optionalPayload;
  }

  private String maxMessageSizeXMLElement(final Optional<Integer> derogatoryMaxMessageSizeForCurrentNMAID) 
  {
    if (derogatoryMaxMessageSizeForCurrentNMAID.isPresent())
    {
      return "<maxMessageSize>" + derogatoryMaxMessageSizeForCurrentNMAID.get() + "</maxMessageSize>";
    }
    return "";
  }

  @Override
  public Version getVersion() {
    return BCPDefaultParams.VERSION;
  }

}
