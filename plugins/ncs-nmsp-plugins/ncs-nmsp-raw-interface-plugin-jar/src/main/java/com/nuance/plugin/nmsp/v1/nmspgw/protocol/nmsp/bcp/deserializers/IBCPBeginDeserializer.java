/* 
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: patrick_filion
 * Date  : 2013-10-31
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

public interface IBCPBeginDeserializer {

  public abstract Version getVersion();
  
  public abstract BCPBeginMessage deserialize(short version, byte[] sessionId, short transactionId,
      ByteBuf message, Session session) throws CorruptedFrameException;

}
