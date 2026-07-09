/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.data;

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class LoadResourceData
{
  private String userName;
  private ResourceType resourceType;
  private Vector<Parameter> parameters;
  private Codec codec = Codec.UNKNOWN;
  private Utils.DeferNSSLoading deferNSSLoading = Utils.DeferNSSLoading.OFF;
  private Map<String, String> recoCapabilities = new HashMap<String, String>();
  private Map<String, String> ttsCapabilities = new HashMap<String,String>();
  private Map<String, String> dictationCapabilities = new HashMap<String,String>();

  private int clientAudioStreamingMaxIdleTimeMSec = 0;
  
  /**
   * @return the userName
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * @param userName
   *          the userName to set
   */
  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  /**
   * @return the resourceType
   */
  public ResourceType getType()
  {
    return resourceType;
  }

  /**
   * @param resourceType
   *          the resourceType to set
   */
  public void setType(ResourceType resourceType)
  {
    this.resourceType = resourceType;
  }

  /**
   * @return the parameters
   */
  public Vector<Parameter> getParameters()
  {
    return parameters;
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(Vector<Parameter> parameters)
  {
    this.parameters = parameters;
  }

  /**
   * @param codec the codec to set
   */
  public void setCodec(Codec codec)
  {
    this.codec = codec;
  }

  /**
   * @return the codec
   */
  public Codec getCodec()
  {
    return codec;
  }

  public Utils.DeferNSSLoading getDeferNSSLoading() {
    return deferNSSLoading;
  }

  public void setDeferNSSLoading(Utils.DeferNSSLoading deferNSSLoading) {
    this.deferNSSLoading = deferNSSLoading;
  }

  /**
   * @param recoCapabilities the recoCapabilities to set
   */
  public void setRecoCapabilities(Map<String, String> recoCapabilities)
  {
    this.recoCapabilities = recoCapabilities;
  }

  /**
   * @return the recoCapabilities
   */
  public Map<String, String> getRecoCapabilities()
  {
    return recoCapabilities;
  }

  /**
   * @param ttsCapabilities the ttsCapabilities to set
   */
  public void setTtsCapabilities(Map<String, String> ttsCapabilities)
  {
    this.ttsCapabilities = ttsCapabilities;
  }

  /**
   * @return the ttsCapabilities
   */
  public Map<String, String> getTtsCapabilities()
  {
    return ttsCapabilities;
  }

  /**
   * @param dictationCapabilities the dictationCapabilities to set
   */
  public void setDictationCapabilities(Map<String, String> dictationCapabilities)
  {
    this.dictationCapabilities = dictationCapabilities;
  }

  /**
   * @return the dictationCapabilities
   */
  public Map<String, String> getDictationCapabilities()
  {
    return dictationCapabilities;
  }

  /**
   * @return the clientAudioStreamingMaxIdleTimeMSec
   */
  public int getClientAudioStreamingMaxIdleTimeMSec() {
    return clientAudioStreamingMaxIdleTimeMSec;
  }

  /**
   * @param clientAudioStreamingMaxIdleTimeMSec
   *          the clientAudioStreamingMaxIdleTimeMSec to set
   */
  public void setClientAudioStreamingMaxIdleTimeMSec(int clientAudioStreamingMaxIdleTimeMSec) {
    this.clientAudioStreamingMaxIdleTimeMSec = clientAudioStreamingMaxIdleTimeMSec;
  }

}
