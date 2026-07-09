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

/**
 * Pdx encoding class values.  Each PDX packet contains one of these values to indicate its contents.
 * 
 */
public enum PdxClass
{
  Number(0xC0), AsciiString(0x16), Utf8String(0xC1), ByteString(0x04), Null(0x05), Sequence(0x10), Dictionary(0xE0), Double(0x06), Boolean(0x07);

  private final int value;

  private PdxClass(int value)
  {
    this.value = value;
  }

  public int getValue()
  {
    return value;
  }

  public static PdxClass createPdxClassFromByte(byte b)
  {
    for (PdxClass pdxClass : PdxClass.values())
    {
      if (pdxClass.getValue() == (b & 0xFF))
      {
        return pdxClass;
      }
    }
    return null;
  }
}
