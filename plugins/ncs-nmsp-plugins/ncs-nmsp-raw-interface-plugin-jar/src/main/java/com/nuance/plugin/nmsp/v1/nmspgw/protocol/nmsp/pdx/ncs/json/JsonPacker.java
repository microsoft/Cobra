/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/// <summary>
/// Encodes C# objects into JSON.  The supported object types are: 
/// string, int, List&lt;object&gt;, Dictionary&lt;object&gt; byte[] and bool.
/// The &lt;object&gt; member of a collection may be any of the supported types, 
/// including another collection.
/// </summary>
public class JsonPacker
{
  private static final CharSequenceTranslator JSON_ESCAPER = makeTranslator();

  private final StringWriter sw;
  private final boolean prettyPrint;
  private final boolean packNonASCIIAsUnicodeEscape;

  // / <summary>
  // / Constructs a new PdxPacker to encode a pdx data block
  // / </summary>
  public JsonPacker()
  {
    this(true, true);
  }
  
  public JsonPacker(boolean prettyPrint)
  {
    this(prettyPrint, true);
  }
  
  public JsonPacker(final boolean prettyPrint, final boolean packNonASCIIAsUnicodeEscape)
  {
    this.sw = new StringWriter();
    this.prettyPrint = prettyPrint;
    this.packNonASCIIAsUnicodeEscape = packNonASCIIAsUnicodeEscape;
  }

  @Override
  public String toString()
  {
    return sw.toString();
  }

  // / <summary>
  // / Packs/encodes any of the supported objects.
  // / </summary>
  // / <param name="obj">The data to pack</param>
  // / <exception cref="PdxUnsupportedDataTypeException">Thrown if the data type presented for packing is not supported.</exception>
  @SuppressWarnings("unchecked")
  public void packDatum(Object obj) throws JsonUnsupportedDataTypeException
  {
    if (obj instanceof String)
    {
      packString((String) obj);
    }
    else if (obj instanceof Integer || obj instanceof Byte)
    {
      packNumber((Integer) obj);
    }
    else if (obj instanceof Double)
    {
      packDouble((Double) obj);
    }
    else if (obj instanceof HashMap<?, ?>)
    {
      packDictionary((Map<String, Object>)obj);
    }
    else if (obj instanceof List<?>)
    {
      packSequence((List<Object>) obj);
    }
    else if (obj instanceof JsonConst)
    {
      packConst((JsonConst) obj);
    }
    else if (obj instanceof byte[])
    {
      packByteString((byte[]) obj);
    }
    else if (obj instanceof Boolean)
    {
      packBool((Boolean) obj);
    }
    else if (obj == null)
    {
      packNull();
    }
    else
    {
      throw new JsonUnsupportedDataTypeException(obj.getClass().getName() + " with value " + obj.toString());
    }
  }

  private void pack(String v)
  {
    sw.write(v);
  }

  private void pack(char ch)
  {
    sw.write(ch);
  }

  // / <summary>
  // / Puts a null value into the stream
  // / </summary>
  public void packNull()
  {
    pack("null");
  }

  // / <summary>
  // / Puts a boolean value into the stream
  // / </summary>
  public void packBool(boolean b)
  {
    if (b)
    {
      pack("true");
    }
    else
    {
      pack("false");
    }
  }

  public void packConst(JsonConst c)
  {
    pack(c.toString());
  }

  // / <summary>
  // / Packs a dictionary
  // / </summary>
  // / <param name="dict"></param>
  public void packDictionary(Map<String, Object> dict) throws JsonUnsupportedDataTypeException
  {
    boolean first = true;
    if(prettyPrint) pack("{\r\n");
    else pack("{");
    
    for (Map.Entry<String, Object> entry : dict.entrySet())
    {
      if (!first)
      {
        if(prettyPrint) pack(",\r\n");
        else pack(",");
      }
      packString(entry.getKey());
      pack(":");
      packDatum(entry.getValue());
      first = false;
    }
    if(prettyPrint) pack("\r\n}\r\n");
    else pack("}");
  }

  // / <summary>
  // / Packs a List
  // / </summary>
  // / <param name="seq"></param>
  public void packSequence(List<Object> seq) throws JsonUnsupportedDataTypeException
  {
    boolean first = true;
    pack("[");
    for (Object obj : seq)
    {
      if (!first)
      {
        pack(",");
      }
      packDatum(obj);
      first = false;
    }
    pack("]");
  }

  // / <summary>
  // / Packs a byte array
  // / </summary>
  // / <param name="v"></param>
  public void packByteString(byte[] v) throws JsonUnsupportedDataTypeException
  {
    boolean first = true;
    pack("[");
    for (byte b : v)
    {
      int n = b;
      if (!first)
      {
        pack(",");
      }
      packDatum(n);
      first = false;
    }
    if(prettyPrint) pack("]\r\n");
    else pack("]");
  }

  // / <summary>
  // / Packs a number
  // / </summary>
  // / <param name="val"></param>
  public void packNumber(int val)
  {
    pack(Integer.toString(val));
  }

  public void packDouble(double val)
  {
    pack(Double.toString(val));
  }
  
  // / <summary>
  // / Packs a string
  // / </summary>
  // / <param name="s"></param>
  public void packString(String str)
  {
    pack('"');
    
    if (packNonASCIIAsUnicodeEscape) 
    {
      packAndEscapeNonASCIIAsUnicodeEscape(str);
    }
    else
    {
      packAndEscapeNonBasicMultilingualPlaneAsUnicodeEscape(str);
    }
    
    pack('"');
  }

  private void packAndEscapeNonASCIIAsUnicodeEscape(final String str) 
  {
    final int n = str.length();
    for (int i = 0; i < n; i++)
    {
      final char ch = str.charAt(i);
      switch (ch)
      {
        case '\n':
          pack("\\n");
          continue;
        case '\r':
          pack("\\r");
          continue;
        case '\t':
          pack("\\t");
          continue;
        case '"':
          pack("\\\"");
          continue;
        case '\\':
          pack("\\\\");
          continue;
        case '\f':
          pack("\\f");
          continue;
        case '\b':
          pack("\\b");
          continue;
      }
 
      if (ch >= 32 && ch <= 126)
      {
        pack(ch);
        continue;
      }
      
      // Default, turn into a \\uXXXX sequence
      pack("\\u");
      pack(StringUtils.leftPad(Integer.toHexString(ch), 4, "0"));
    }
  }

  private void packAndEscapeNonBasicMultilingualPlaneAsUnicodeEscape(final String str) 
  {
    try 
    {
      JSON_ESCAPER.translate(str, sw);
    } 
    catch (final IOException e) 
    {
      throw new RuntimeException("Failed to transform '" + str + "' to a JSON string", e);
    }
  }
  
  private static CharSequenceTranslator makeTranslator() {
    return new AggregateTranslator(
        new LookupTranslator(
                  new String[][] {
                        {"\"", "\\\""},
                        {"\\", "\\\\"},
                        {"\u0000", "\\u0000"},
                        {"\u0001", "\\u0001"},
                        {"\u0002", "\\u0002"},
                        {"\u0003", "\\u0003"},
                        {"\u0004", "\\u0004"},
                        {"\u0005", "\\u0005"},
                        {"\u0006", "\\u0006"},
                        {"\u0007", "\\u0007"},
                        {"\u0008", "\\b"},
                        {"\u0009", "\\t"},
                        {"\n", "\\n"},
                        {"\u000b", "\\u000b"},
                        {"\u000c", "\\f"},
                        {"\r", "\\r"},
                        {"\u000e", "\\u000e"},
                        {"\u000f", "\\u000f"},
                        {"\u0010", "\\u0010"},
                        {"\u0011", "\\u0011"},
                        {"\u0012", "\\u0012"},
                        {"\u0013", "\\u0013"},
                        {"\u0014", "\\u0014"},
                        {"\u0015", "\\u0015"},
                        {"\u0016", "\\u0016"},
                        {"\u0017", "\\u0017"},
                        {"\u0018", "\\u0018"},
                        {"\u0019", "\\u0019"},
                        {"\u001a", "\\u001a"},
                        {"\u001b", "\\u001b"},
                        {"\u001c", "\\u001c"},
                        {"\u001d", "\\u001d"},
                        {"\u001e", "\\u001e"},
                        {"\u001f", "\\u001f"},
                  }),
        JavaUnicodeEscaper.between(Character.MIN_SUPPLEMENTARY_CODE_POINT, Character.MAX_CODE_POINT)
    );
  }
}
