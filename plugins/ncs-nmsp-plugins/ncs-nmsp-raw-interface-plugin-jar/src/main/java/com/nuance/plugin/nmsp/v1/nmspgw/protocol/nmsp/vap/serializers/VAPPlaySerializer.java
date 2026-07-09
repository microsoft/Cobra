/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;

/**
 * @author epigeon
 * 
 */
public class VAPPlaySerializer
{
  public static ByteBuf serialize(Session session, VAPPlayMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    // Write the audio id
    buffer.writeInt((int) message.getAudioId());

    String codec = session.getFromCodec().name().toLowerCase();
    if (codec.startsWith("speex") || codec.startsWith("opus")) {
      message.setAudio(addNmspNmtSizeHeader(message.getAudio()));
    }

    // Write the audio length
    buffer.writeInt(message.getAudio().length);
    // Write the audio
    buffer.writeBytes(message.getAudio());

    return buffer;
  }

  //Prefixes the 1-4 byte NMSP/NMT size header.
  private static byte[] addNmspNmtSizeHeader(byte[] buffer)
  {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    if(buffer.length < 0x80) {
      byteArrayOutputStream.write(buffer.length & 0x7F);
    }
    else if(buffer.length < 0x4000) {
      byteArrayOutputStream.write(((buffer.length >> 7) & 0x7F) | 0x80);
      byteArrayOutputStream.write(buffer.length & 0x7F);
    }
    else if(buffer.length < 0x200000) {
      byteArrayOutputStream.write(((buffer.length >> 14) & 0x7F) | 0x80);
      byteArrayOutputStream.write(((buffer.length >> 7) & 0x7F) | 0x80);
      byteArrayOutputStream.write(buffer.length & 0x7F);
    }
    else if(buffer.length < 0x10000000) {
      byteArrayOutputStream.write(((buffer.length >> 21) & 0x7F) | 0x80);
      byteArrayOutputStream.write(((buffer.length >> 14) & 0x7F) | 0x80);
      byteArrayOutputStream.write(((buffer.length >> 7) & 0x7F) | 0x80);
      byteArrayOutputStream.write(buffer.length & 0x7F);
    }
    else {
      return null;
    }
    byteArrayOutputStream.write(buffer, 0, buffer.length);
    return byteArrayOutputStream.toByteArray();
  }

}
