/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeCompleteMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 *
 */
public class BCPTranscodeCompleteSerializer
{

  private BCPTranscodeCompleteSerializer()
  {
  }

  public static ByteBuf serialize(BCPTranscodeCompleteMessage message)
  {
    ByteBuf buffer = Unpooled.buffer(message.getMessageLength()).order(NMSPMessage.byteOrder);

    // Skip header size, the header is added in NMSPMessageSerializer
    buffer.setIndex(0, NMSPMessage.headerSize);
    buffer.writeBytes(message.getSessionId());

    // Write the transaction id
    buffer.writeByte(message.getTransactionId());
    // Write the request id
    buffer.writeInt((int) message.getRequestId());
    // Write the audio id
    buffer.writeInt((int) message.getAudioId());
    // Write the completion cause
    buffer.writeShort(message.getCompletionCause());
    // Write payload length
    buffer.writeInt(message.getPayload().length);
    // Write the payload
    buffer.writeBytes(message.getPayload());
    
    return buffer;
  }

}
