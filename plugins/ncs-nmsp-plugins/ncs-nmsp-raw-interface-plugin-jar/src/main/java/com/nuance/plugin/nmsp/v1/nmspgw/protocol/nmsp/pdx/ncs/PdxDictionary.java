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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json.*;

/**
 * Representation of a PDX dictionary object
 */
@SuppressWarnings("serial")
public class PdxDictionary extends HashMap<String, Object>
{
  private static final ObjectMapper mapper = new ObjectMapper();
  private boolean keysEncodedUsingAscii;
  /**
   * Default constructor  
   */
  public PdxDictionary()
  {
    super();
  }

  /**
   * Copy constructor  
   */
  public PdxDictionary(PdxDictionary cp)
  {
    super();
    for (Map.Entry<String, Object> entry : cp.entrySet())
    {
      this.put(entry.getKey(), copyObject(entry.getValue()));
    }
  }

  private Object copyObject(Object obj)
  {
    if (obj instanceof String || 
        obj instanceof Integer || 
        obj instanceof Byte ||
        obj instanceof JsonConst ||
        obj instanceof Boolean)
    {
      return obj;
    }
    if (obj instanceof PdxDictionary)
    {
      return new PdxDictionary((PdxDictionary) obj);
    }
    if (obj instanceof List<?>)
    {
      return copyList((List<?>) obj);
    }
    if (obj instanceof byte[])
    {
      return copyByteArray((byte[]) obj);
    }

    return obj;
  }

  private List<Object> copyList(List<?> listToCopy)
  {
    List<Object> copyList = new ArrayList<Object>(listToCopy.size());
    for (Object obj : listToCopy)
    {
      copyList.add(copyObject(obj));
    }
    return copyList;
  }

  private byte[] copyByteArray(byte[] arrayToCopy)
  {
    byte[] copyArray = new byte[arrayToCopy.length];
    for (int i = 0; i < arrayToCopy.length; i++)
    {
      copyArray[i] = arrayToCopy[i];
    }
    return copyArray;
  }

  /**
   * Convert a Json encoded string into a PdxDictionary  
   */
  public static PdxDictionary fromJson(String s) throws JsonException, JsonUnsupportedDataTypeException
  {
    final PdxDictionary rv = (PdxDictionary) JsonUnpacker.unpack(s);
    return rv;
  }

  /**
   * Create a Json string representation of the PdxDictionary  
   * NOTE: whereas toString() returns a Json string with escaped encoding, toJsonString() will return a JSON string with UTF8 encoding (if needed)
   */
  public String toJsonString() throws Exception
  {
    try
    {
      return mapper.writeValueAsString(this);
    } 
    catch (Exception e) 
    {
      throw new Exception("Error creating Json", e);
    }
  }
  
  /**
   * Return the Json encoded string representing this PdxDictionary  
   */
  @Override
  public String toString()
  {
    JsonPacker jpack = new JsonPacker();
    try
    {
      jpack.packDictionary(this);
    }
    catch (JsonUnsupportedDataTypeException e)
    {
      return "Exception: " + e.getMessage();
    }
    return jpack.toString();
  }

  @Override
  public boolean equals(Object otherObj)
  {
    if (otherObj == null)
    {
      return false;
    }

    if (this == otherObj)
    {
      return true;
    }

    if (!(otherObj instanceof PdxDictionary))
    {
      return false;
    }

    PdxDictionary otherDict = (PdxDictionary) otherObj;
    if (this.size() != otherDict.size())
    {
      return false;
    }

    for (Map.Entry<String, Object> entry : this.entrySet())
    {
      if (!otherDict.containsKey(entry.getKey()))
      {
        return false;
      }
      if (!dictObjectEquals(entry.getValue(), otherDict.get(entry.getKey())))
      {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 0;
    for (Object obj : this.values())
    {
      hash += hashCode(obj);
    }
    return hash;
  }

  private static int hashCode(Object obj)
  {
    if (obj instanceof String)
    {
      return ((String) obj).hashCode();
    }
    if (obj instanceof Integer)
    {
      return ((Integer) obj).hashCode();
    }
    if (obj instanceof Byte)
    {
      return ((Byte) obj).hashCode();
    }
    else if (obj instanceof PdxDictionary)
    {
      return ((PdxDictionary) obj).hashCode();
    }
    else if (obj instanceof List<?>)
    {
      int hash = 0;
      for (Object subObj : (List<?>) obj)
      {
        hash += hashCode(subObj);
      }
      return hash;
    }
    else if (obj instanceof byte[])
    {
      return Arrays.hashCode(((byte[]) obj));
    }
    if (obj instanceof Boolean)
    {
      return ((Boolean) obj).hashCode();
    }
    if (obj instanceof JsonConst)
    {
      return obj.hashCode();
    }

    return 0;
  }

  private static boolean dictObjectEquals(Object obj1, Object obj2)
  {
    if (obj1 == obj2)
    {
      return true;
    }
    if (obj1 instanceof String)
    {
      if (!(obj2 instanceof String))
      {
        return false;
      }
      return obj1.equals(obj2);
    }
    if (obj1 instanceof Integer || obj1 instanceof Byte)
    {
      if (!(obj2 instanceof Integer || obj2 instanceof Byte))
      {
        return false;
      }
      return ((Integer) obj1).equals(obj2);
    }
    if (obj1 instanceof Double)
    {
      if (!(obj2 instanceof Double))
      {
        return false;
      }
      return ((Double) obj1).equals(obj2);
    }
    if (obj1 instanceof PdxDictionary)
    {
      if (!(obj2 instanceof PdxDictionary))
      {
        return false;
      }
      return obj1.equals(obj2);
    }
    if (obj1 instanceof List<?>)
    {
      if (!(obj2 instanceof List<?>))
      {
        return false;
      }
      return listEquals((List<?>) obj1, (List<?>) obj2);
    }
    if (obj1 instanceof byte[])
    {
      if (!(obj2 instanceof byte[]))
      {
        return false;
      }
      return byteArrayEquals((byte[]) obj1, (byte[]) obj2);
    }
    if (obj1 instanceof Boolean)
    {
      if (!(obj2 instanceof Boolean))
      {
        return false;
      }
      return obj1.equals(obj2);
    }
    if (obj1 instanceof JsonConst)
    {
      if (!(obj2 instanceof JsonConst))
      {
        return false;
      }
      return obj1.equals(obj2);
    }
    return false;
  }

  private static boolean listEquals(List<?> list1, List<?> list2)
  {
    if (list1.size() != list2.size())
    {
      return false;
    }
    for (int i = 0; i < list1.size(); i++)
    {
      if (list1.get(i) instanceof byte[] && list2.get(i) instanceof byte[])
      {
        return byteArrayEquals((byte[]) list1.get(i), (byte[]) list2.get(i));
      }
      if (!list1.get(i).equals(list2.get(i)))
      {
        return false;
      }
    }

    return true;
  }

  private static boolean byteArrayEquals(byte[] list1, byte[] list2)
  {
    return Arrays.equals(list1, list2);
  }

  public boolean isKeysEncodedUsingAscii() {
    return keysEncodedUsingAscii;
  }

  void setKeysEncodedUsingAscii(boolean keysEncodedUsingAscii) {
    this.keysEncodedUsingAscii = keysEncodedUsingAscii;
  }
}
