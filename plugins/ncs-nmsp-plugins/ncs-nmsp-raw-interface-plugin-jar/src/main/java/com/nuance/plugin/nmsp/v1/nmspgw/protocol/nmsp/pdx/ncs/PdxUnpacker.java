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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.utils.ByteHelper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Decodes a block of PDX encoded datums
/// </summary>
public class PdxUnpacker
{
  // In order to maintain backward compatibility with core 7.2, allow the option to encode using ASCII instead of UTF-8.
  private static boolean encodeAsAscii = Boolean.parseBoolean(System.getenv("PDX_DICTIONARY_ASCII_ENCODING"));

  /**
   * Unpacks a single PDX datum (string, List, Dictionary, number, byte[]) from the tip of the block
   * 
   * @param buffer
   * @return
   * @throws PdxUnsupportedDataTypeException
   * @throws PdxException
   */
  public static Object unpack(byte[] buffer) throws PdxUnsupportedDataTypeException, PdxException
  {
    PdxPacket packet = new PdxPacket(buffer, 0);
    return unpackDatum(packet);
  }

  /**
   * Unpacks a single PDX datum (string, List, Dictionary, number, byte[]) from a specified offset within the block
   * 
   * @param buffer
   * @param offset
   * @return
   * @throws PdxUnsupportedDataTypeException
   * @throws PdxException
   */
  public static Object unpack(byte[] buffer, int offset) throws PdxUnsupportedDataTypeException, PdxException
  {
    PdxPacket packet = new PdxPacket(buffer, offset);
    return unpackDatum(packet);
  }

  /**
   * Unpacks a Dictionary
   * 
   * @param buffer
   * @param offset
   * @return
   * @throws PdxUnsupportedDataTypeException
   * @throws PdxException
   */
  public static PdxDictionary unpackDict(byte[] buffer, int offset) throws PdxUnsupportedDataTypeException, PdxException
  {
    return (PdxDictionary) unpack(buffer, offset);
  }

  /**
   * Unpacks a packets PDX datum (string, List, Dictionary, number, byte[])
   * 
   * @param packet
   * @return
   * @throws PdxUnsupportedDataTypeException
   * @throws PdxException
   */
  public static Object unpackDatum(PdxPacket packet) throws PdxUnsupportedDataTypeException, PdxException
  {
    switch (packet.getPdxClass())
    {
      case AsciiString:
        return unpackAsciiString(packet);
      case Number:
        return unpackNumber(packet);
      case Utf8String:
        return unpackUtf8String(packet);
      case ByteString:
        return unpackByteString(packet);
      case Null:
        return unpackNull(packet);
      case Sequence:
        return unpackSequence(packet);
      case Dictionary:
        return unpackDictionary(packet);
      case Double:
        return unpackDouble(packet);
      case Boolean:
        return unpackBoolean(packet);
      default:
        throw new PdxUnsupportedDataTypeException();
    }
  }

  /**
   * Unpacks a Dictionary
   * 
   * @param packet
   * @return
   * @throws PdxException
   * @throws PdxUnsupportedDataTypeException
   */
  public static Object unpackDictionary(PdxPacket packet) throws PdxException, PdxUnsupportedDataTypeException
  {
    if (packet.getPdxClass() != PdxClass.Dictionary)
    {
      throw new PdxException("Cannot unpack packet to dictionary");
    }

    PdxDictionary dic = new PdxDictionary();
    PdxPacketBlock block = new PdxPacketBlock(packet.getData(), packet.getPayloadOffset(), packet.getPayloadSize());
    String k;

    while (block.moveNext())
    {
      if (encodeAsAscii || block.getCurrent().getPdxClass() == PdxClass.AsciiString) {
        k = (String) unpackAsciiString(block.getCurrent());
        dic.setKeysEncodedUsingAscii(true);
      } else {
        k = (String) unpackUtf8String(block.getCurrent());
      }
      if (!block.moveNext())
      {
        throw new PdxException("Invalid dictionary");
      }
      Object v = unpackDatum(block.getCurrent());
      dic.put(k, v);
    }
    return dic;
  }

  /**
   * Unpacks a Sequence
   * 
   * @param packet
   * @return
   * @throws PdxException
   * @throws PdxUnsupportedDataTypeException
   */
  public static Object unpackSequence(PdxPacket packet) throws PdxException, PdxUnsupportedDataTypeException
  {
    if (packet.getPdxClass() != PdxClass.Sequence)
    {
      throw new PdxException("Cannot unpack packet to sequenece");
    }

    List<Object> seq = new ArrayList<Object>();
    PdxPacketBlock block = new PdxPacketBlock(packet.getData(), packet.getPayloadOffset(), packet.getPayloadSize());

    while (block.moveNext())
    {
      seq.add(unpackDatum(block.getCurrent()));
    }
    return seq;
  }

  /**
   * Unpacks a Null
   * 
   * @param packet
   * @return
   */
  public static Object unpackNull(PdxPacket packet)
  {
    return null;
  }

  /**
   * Unpacks a UTF8 String
   * 
   * @param packet
   * @return
   * @throws PdxException
   */
  public static Object unpackUtf8String(PdxPacket packet) throws PdxException
  {
    if (packet.getPdxClass() != PdxClass.Utf8String)
    {
      throw new PdxException("Cannot unpack packet to Utf8 string");
    }
    return unpackString(StandardCharsets.UTF_8, packet);
  }

  /**
   * Unpacks an ASCII String
   * 
   * @param packet
   * @return
   * @throws PdxException
   */
  // TODO: check the Ascii conversion here
  public static Object unpackAsciiString(PdxPacket packet) throws PdxException
  {
    if (packet.getPdxClass() != PdxClass.AsciiString)
    {
      throw new PdxException("Cannot unpack packet to Ascii string");
    }
    return unpackString(StandardCharsets.US_ASCII, packet);
  }
  
  private static Object unpackString(Charset charset, PdxPacket packet) throws PdxException
  {
    try
    {
      byte[] bytesToUse = new byte[packet.getPayloadSize()];
      System.arraycopy(packet.getData(), packet.getPayloadOffset(), bytesToUse, 0, packet.getPayloadSize());
      return new String(bytesToUse, charset);
    }
    catch (Exception e)
    {
      throw new PdxException("Could not decode with charset " + charset, e);
    }
  }

  /**
   * Unpacks a Number
   * 
   * @param packet
   * @return
   * @throws PdxException
   */
  public static Object unpackNumber(PdxPacket packet) throws PdxException
  {
    if (packet.getPdxClass() != PdxClass.Number)
    {
      throw new PdxException("Cannot unpack packet to number");
    }

    int i = 0;
    try
    {
      int size = packet.getPayloadSize();
      if (size == 1)
      {
        i = (byte)(packet.getData()[packet.getPayloadOffset()] & 0xFF);
      }
      else if (size == 2)
      {
        i = (short)(((packet.getData()[packet.getPayloadOffset()+1] & 0xFF) << 8) + 
            (packet.getData()[packet.getPayloadOffset()] & 0xFF));
      }
      else
      {
        i = (((packet.getData()[packet.getPayloadOffset()+3] & 0xFF) << 24) + 
            ((packet.getData()[packet.getPayloadOffset()+2] & 0xFF) << 16) + 
            ((packet.getData()[packet.getPayloadOffset()+1] & 0xFF) << 8) + 
            (packet.getData()[packet.getPayloadOffset()] & 0xFF));
      }
    }
    catch (Exception e)
    {
      throw new PdxException(e);
    }

    return i;
  }
  
  public static Object unpackDouble(PdxPacket packet) throws PdxException
  {
    double unpackedDouble;
    if (packet.getPdxClass() != PdxClass.Double)
    {
      throw new PdxException("Cannot unpack packet to double");
    }
    byte[] bytesToUse = new byte[packet.getPayloadSize()];
    System.arraycopy(packet.getData(), packet.getPayloadOffset(), bytesToUse, 0, packet.getPayloadSize());
    try
    {
      unpackedDouble = ByteHelper.toDouble(ByteHelper.reverse(bytesToUse), 0);
    }
    catch (Exception e)
    {
      throw new PdxException("Cannot unpack bytes as double");
    }
    return unpackedDouble;
  }
  
  
  /**
   * Unpacks a byte String
   * 
   * @param packet
   * @return
   * @throws PdxException
   */
  public static Object unpackByteString(PdxPacket packet) throws PdxException
  {
    if (packet.getPdxClass() != PdxClass.ByteString)
    {
      throw new PdxException("Cannot unpack packet to byte string");
    }
    byte[] bs = new byte[packet.getPayloadSize()];
    System.arraycopy(packet.getData(), packet.getPayloadOffset(), bs, 0, packet.getPayloadSize());
    return bs;
  }
  
  
  public static Object unpackBoolean(PdxPacket packet) throws PdxException {
    boolean unpackedBoolean;
    if (packet.getPdxClass() != PdxClass.Boolean)
    {
      throw new PdxException("Cannot unpack packet to boolean");
    }
    byte[] bytesToUse = new byte[packet.getPayloadSize()];
    System.arraycopy(packet.getData(), packet.getPayloadOffset(), bytesToUse, 0, packet.getPayloadSize());
    try
    {
      unpackedBoolean = ByteHelper.toBoolean(bytesToUse, 0);
    }
    catch (Exception e)
    {
      throw new PdxException("Cannot unpack bytes as boolean");
    }
    return new Boolean(unpackedBoolean);
  }
}
