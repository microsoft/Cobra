/* 
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2016 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: bertrand_braschi
 * Date  : Jan 25, 2016
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class AudioHandlingSpec {

  private final AudioGenerationPolicy audioGenerationPolicy;
  private final SameCodecMGWPolicy sameCodecMGWPolicy;
  private final AudioContainerType audioContainerType;

  public AudioHandlingSpec(final AudioGenerationPolicy audioGenerationPolicy, final SameCodecMGWPolicy sameCodecMGWPolicy, final AudioContainerType audioContainerType) {
    this.audioGenerationPolicy = audioGenerationPolicy;
    this.sameCodecMGWPolicy = sameCodecMGWPolicy;
    this.audioContainerType = audioContainerType;
  }

  public AudioGenerationPolicy getAudioGenerationPolicy() {
    return audioGenerationPolicy;
  }

  public SameCodecMGWPolicy getSameCodecMGWPolicy() {
    return sameCodecMGWPolicy;
  }

  public AudioContainerType getAudioContainerType() {
    return audioContainerType;
  }

  @Override
  public String toString() {
    return reflectionToString(this, SIMPLE_STYLE);
  }
}
