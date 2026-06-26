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

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * PdxToXml can convert PDX encoded data into a human readable XML format. 
 * The XML is only intended for visualizing the encoded data and should 
 * not be used to transport the data
 *
 */
public class PdxToXml
{

  // In order to maintain backward compatibility with core 7.2, allow the option to encode using ASCII instead of UTF-8.
  private static boolean encodeAsAscii = Boolean.parseBoolean(System.getenv("PDX_DICTIONARY_ASCII_ENCODING"));

  private Writer writer;
  
  public PdxToXml(Writer tw)
  {
    writer = tw;
  }
  
  public PdxToXml()
  {
    writer = new OutputStreamWriter(System.out);
  }
  
  public void write(PdxPacket packet) throws IOException, PdxException, PdxUnsupportedDataTypeException
  {
    writeDatum(this.writer, packet);
  }
  
  public void write(byte[] bs) throws IOException, PdxException, PdxUnsupportedDataTypeException
  {
    writeDatum(this.writer, new PdxPacket(bs, 0));
  }
  
  public static void writeDatum(Writer writer, PdxPacket packet) throws IOException, PdxException, PdxUnsupportedDataTypeException
  {
    switch (packet.getPdxClass())
    {
      case AsciiString:
        writeAsciiString(writer, packet);
        break;
      case Number:
        writeNumber(writer, packet);
        break;
      case Utf8String:
        writeUtf8String(writer, packet);
        break;
      case ByteString:
        writeByteString(writer, packet);
        break;
      case Null:
        writeNull(writer, packet);
        break;
      case Sequence:
        writeSequence(writer, packet);
        break;
      case Dictionary:
        writeDictionary(writer, packet);
        break;
      default:
        throw new PdxUnsupportedDataTypeException();
    }
  }
  
  public static void writeDictionary(Writer writer, PdxPacket packet) throws IOException, PdxException, PdxUnsupportedDataTypeException
  {
    if (packet.getPdxClass() != PdxClass.Dictionary)
    {
      throw new PdxException("Cannot Dump packet to dictionary");
    }

    writer.write("<DICT>");
  
    PdxPacketBlock block = new PdxPacketBlock(packet.getData(), packet.getPayloadOffset(), packet.getPayloadSize());
    while (block.moveNext())
    {
      if (encodeAsAscii || block.getCurrent().getPdxClass() == PdxClass.AsciiString) {
        writeAsciiString(writer, block.getCurrent());
      } else {
        writeUtf8String(writer, block.getCurrent());
      }
      if (!block.moveNext())
      {
        throw new PdxException("Invalid dictionary");
      }
      writeDatum(writer, block.getCurrent());
    }
    writeStringLine(writer, "</DICT>");
  }
  
  public static void writeSequence(Writer writer, PdxPacket packet) throws IOException, PdxException, PdxUnsupportedDataTypeException
  {
    if (packet.getPdxClass() != PdxClass.Sequence)
    {
      throw new PdxException("Cannot Dump packet to sequenece");
    }
  
    writer.write("<SEQ>");
  
    PdxPacketBlock block = new PdxPacketBlock(packet.getData(), packet.getPayloadOffset(), packet.getPayloadSize());
  
    while (block.moveNext())
    {
      writeDatum(writer, block.getCurrent());
    }
  
    writeStringLine(writer, "</SEQ>");
  }
  
  public static void writeNull(Writer writer, PdxPacket packet) throws IOException
  {
    writeStringLine(writer, "<NULL/>");
  }
  
  public static void writeUtf8String(Writer writer, PdxPacket packet) throws PdxException, IOException
  {
    if (packet.getPdxClass() != PdxClass.Utf8String)
    {
      throw new PdxException("Cannot Dump packet to Utf8 string");
    }
    writer.write("<UTF8>");
    writer.write(StringEscapeUtils.escapeXml(PdxUnpacker.unpackUtf8String(packet).toString()));
    writeStringLine(writer, "</UTF8>");
  }
  
  public static void writeAsciiString(Writer writer, PdxPacket packet) throws PdxException, IOException
  {
    if (packet.getPdxClass() != PdxClass.AsciiString)
    {
        throw new PdxException("Cannot Dump packet to Ascii string");
    }
    writer.write("<ASCII>");
    writer.write(StringEscapeUtils.escapeXml(PdxUnpacker.unpackAsciiString(packet).toString()));
    writeStringLine(writer, "</ASCII>");
  }
  
  public static void writeNumber(Writer writer, PdxPacket packet) throws IOException, PdxException
  {
    if (packet.getPdxClass() != PdxClass.Number)
    {
      throw new PdxException("Cannot Dump packet to number");
    }
    writer.write("<INT>");
    writer.write(StringEscapeUtils.escapeXml(PdxUnpacker.unpackNumber(packet).toString()));
    writeStringLine(writer, "</INT>");
  }
  
  public static void writeByteString(Writer writer, PdxPacket packet) throws PdxException, IOException
  {
    if (packet.getPdxClass() != PdxClass.ByteString)
    {
      throw new PdxException("Cannot Dump packet to byte string");
    }
    writer.write("<BYTES>");
    writer.write(StringEscapeUtils.escapeXml(PdxUnpacker.unpackByteString(packet).toString()));
    writeStringLine(writer, "</BYTES>");
  }
  
  private static void writeStringLine(Writer writer, String s) throws IOException
  {
    writer.write(s + "\n");
  }
}
