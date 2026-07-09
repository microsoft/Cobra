/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;

public class ByteHelper
{
  public static byte[] getBytes(boolean x)
  {
    return new byte[] { (byte) (x ? 0xFF : 0) };
  }

  public static byte[] getBytes(char c)
  {
    return new byte[] { (byte) (c & 0xff), (byte) (c >> 8 & 0xff) };
  }

  public static byte[] getBytes(double x)
  {
    return getBytes(Double.doubleToRawLongBits(x));
  }

  public static byte[] getBytes(short x)
  {
    return new byte[] { (byte) x, (byte) (x >>> 8) };
  }

  public static byte[] getBytes(int x)
  {
    return new byte[] { (byte) x, (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24) };
  }

  public static byte[] getBytes(long x)
  {
    return new byte[] { (byte) x, (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24), (byte) (x >>> 32), (byte) (x >>> 40), (byte) (x >>> 48), (byte) (x >>> 56) };
  }

  public static byte[] getBytes(float x)
  {
    return getBytes(Float.floatToRawIntBits(x));
  }

  public static byte[] getBytes(String x) throws UnsupportedEncodingException
  {
    return x.getBytes(StandardCharsets.UTF_8);
  }

  public static long doubleToInt64Bits(double x)
  {
    return Double.doubleToRawLongBits(x);
  }

  public static double int64BitsToDouble(long x)
  {
    return x;
  }

  private static void checkIndex(final int nbByteRequired, final byte[] bytes, final int index) throws Exception
  {
    if (null == bytes)
    {
      throw new NullPointerException("bytes cannot be null");
    }
    if (index < 0 || bytes.length < index + nbByteRequired)
    {
      final String msg = String.format("Invalid index=%d for byte array of lenght=%d for data type requiring %d bytes. Please provide an index between 0 and %d included.", 
          index, bytes.length, nbByteRequired, bytes.length - nbByteRequired);
      throw new Exception(msg);
    }
  }
  
  public static boolean toBoolean(byte[] bytes, int index) throws Exception
  {
    checkIndex(1, bytes, index);
    return bytes[index] != 0;
  }

  public static char toChar(byte[] bytes, int index) throws Exception
  {
    checkIndex(2, bytes, index);
    return (char) ((0xff & bytes[index]) << 8 | (0xff & bytes[index + 1]));
  }

  public static double toDouble(byte[] bytes, int index) throws Exception
  {
    checkIndex(8, bytes, index);
    return Double.longBitsToDouble(toInt64(bytes, index));
  }

  public static short toInt16(byte[] bytes, int index) throws Exception
  {
    checkIndex(2, bytes, index);
    return (short) ((0xff & bytes[index]) << 8 | (0xff & bytes[index + 1]));
  }

  public static int toInt32(byte[] bytes, int index) throws Exception
  {
    checkIndex(4, bytes, index);
    return ((0xff & bytes[index]) << 24 | (0xff & bytes[index + 1]) << 16 | (0xff & bytes[index + 2]) << 8 | (0xff & bytes[index + 3]));
  }

  public static long toInt64(byte[] bytes, int index) throws Exception
  {
    checkIndex(8, bytes, index);
    return ((long) (0xff & bytes[index]) << 56 | (long) (0xff & bytes[index + 1]) << 48 | (long) (0xff & bytes[index + 2]) << 40 | (long) (0xff & bytes[index + 3]) << 32
        | (long) (0xff & bytes[index + 4]) << 24 | (long) (0xff & bytes[index + 5]) << 16 | (long) (0xff & bytes[index + 6]) << 8 | (long) (0xff & bytes[index + 7]));
  }

  public static float toSingle(byte[] bytes, int index) throws Exception
  {
    checkIndex(4, bytes, index);
    return Float.intBitsToFloat(toInt32(bytes, index));
  }

  public static String toString(byte[] bytes) throws Exception
  {
    if (bytes == null)
    {
      throw new NullPointerException("bytes cannot be null");
    }
    return new String(bytes);
  }

  public static byte[] decompress(byte[] compressedData) throws Exception
  {
    Inflater decompressor = new Inflater();
    decompressor.setInput(compressedData);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
    byte[] buf = new byte[1024];
    while (!decompressor.finished())
    {
      int count = decompressor.inflate(buf);
      bos.write(buf, 0, count);
    }
    bos.close();
    return bos.toByteArray();
  }

  public static byte[] reverse(byte[] bytes)
  {
    int size = bytes.length;
    byte[] buf = new byte[size];
    for (int i = 0; i < size; i++)
    {
      buf[i] = bytes[size - i - 1];
    }
    return buf;
  }
}
