/* 
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2015 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap;

public interface IGWAudioSink {
  void addAudioBuf(byte[] bufferIn, int offset, int length, boolean isLastBuf, boolean segmentEnding);

  void addSubAudioBuf(byte[] bufferIn, int offset, int length, boolean isLastBuf, boolean isAudioStart);
}

