/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

public class PdxPacketBlock
{
  private byte[] data;
  private int offset;
  private int itr;
  private int len;
  private PdxPacket current;
  
  public PdxPacketBlock(byte[] buffer, int offset, int len)
  {
    this.data = buffer;
    this.offset = offset;
    this.len = len;
    this.itr = -1;
    this.current = null;
  }
  
  public int getEndOffset()
  {
    return offset + len;
  }
  
  public PdxPacket getCurrent()
  {
    return current;
  }
  
  public void reset()
  {
    itr = -1;
  }
  
  public boolean checkEnd()
  {
    if (itr >= getEndOffset())
    {
      current = null;
      return true;
    }
    return false;
  }
  
  public boolean moveNext() throws PdxUnsupportedDataTypeException, PdxException
  {
    if (checkEnd())
    {
      return false;
    }
    if (itr == -1)
    {
      itr = offset;
    }
    else
    {
      itr += current.getSize();
    }
  
    if (checkEnd())
    {
      return false;
    }
    else
    {
      current = new PdxPacket(data, itr);
      return true;
    }
  }
}
