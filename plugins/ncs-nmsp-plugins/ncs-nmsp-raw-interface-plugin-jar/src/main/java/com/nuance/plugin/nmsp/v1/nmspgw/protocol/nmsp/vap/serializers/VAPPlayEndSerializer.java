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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayEndMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class VAPPlayEndSerializer
{

  public static ByteBuf serialize(VAPPlayEndMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    // Write the audio id
    buffer.writeInt((int) message.getAudioId());

    return buffer;
  }

}
