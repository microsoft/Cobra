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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.*;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class VAPMessageSerializer
{
  private VAPMessageSerializer()
  {
  }

  public static ByteBuf serialize(Session session, VAPMessage message)
  {
    switch (message.getCommand())
    {
      case VAPMessage.VAP_PLAY:
        return VAPPlaySerializer.serialize(session, (VAPPlayMessage) message);
      case VAPMessage.VAP_PLAY_BEGIN:
        return VAPPlayBeginSerializer.serialize((VAPPlayBeginMessage) message);
      case VAPMessage.VAP_PLAY_END:
        return VAPPlayEndSerializer.serialize((VAPPlayEndMessage) message);
      case VAPMessage.VAP_END_OF_SENTENCE:
        return VAPEndOfSentenceSerializer.serialize((VAPEndOfSentenceMessage) message);
        // case VAPMessage.VAP_BACKUP_RESPONSE:
        // break;
        // case VAPMessage.VAP_RECOVER_RESPONSE:
        // break;
      default:
        return Unpooled.EMPTY_BUFFER;
    }

  }
}
