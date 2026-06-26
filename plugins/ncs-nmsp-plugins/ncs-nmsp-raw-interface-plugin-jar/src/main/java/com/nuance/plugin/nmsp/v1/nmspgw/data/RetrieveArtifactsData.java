/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.data;

import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;

import java.util.Optional;

public class RetrieveArtifactsData {
  private final long audioId;
  private final long bufferSize;
  private final Optional<Long> bitPerSecond;
  private final String targetSessionId;
  private final IAudioListener audioListener;

  public RetrieveArtifactsData(
      final long audioId, 
      final long bufferSize, 
      final Optional<Long> bitPerSecond, 
      final String targetSessionId,
      final IAudioListener audioListener) {
    this.audioId = audioId;
    this.bufferSize = bufferSize;
    this.bitPerSecond = bitPerSecond;
    this.targetSessionId = targetSessionId;
    this.audioListener = audioListener;
  }
  public long getAudioId() {
    return audioId;
  }
  public long getBufferSize() {
    return bufferSize;
  }
  public Optional<Long> getBitPerSecond() {
    return bitPerSecond;
  }
  public IAudioListener getAudioListener() {
    return audioListener;
  }
  public String getTargetSessionId() {
    return targetSessionId;
  }
}
