/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.data;

/**
 * @author epigeon
 *
 */
public class TranscodeData
{
  private long audioId;
  private int codec;
  private int transportProtocol;
  /**
   * @return the audioId
   */
  public long getAudioId()
  {
    return audioId;
  }
  /**
   * @param audioId the audioId to set
   */
  public void setAudioId(long audioId)
  {
    this.audioId = audioId;
  }
  /**
   * @return the codec
   */
  public int getCodec()
  {
    return codec;
  }
  /**
   * @param codec the codec to set
   */
  public void setCodec(int codec)
  {
    this.codec = codec;
  }
  /**
   * @return the transportProtocol
   */
  public int getTransportProtocol()
  {
    return transportProtocol;
  }
  /**
   * @param transportProtocol the transportProtocol to set
   */
  public void setTransportProtocol(int transportProtocol)
  {
    this.transportProtocol = transportProtocol;
  }
  
}
