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

public class JsonConst
{
  private String value;

  public JsonConst(String s)
  {
    value = s;
  }

  @Override
  public String toString()
  {
    return value;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (this == obj)
    {
      return true;
    }

    if (!(obj instanceof JsonConst))
    {
      return false;
    }

    JsonConst otherConst = (JsonConst) obj;
    if (value == null && otherConst.value == null || value != null && value.equals(otherConst.value))
    {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 0;
    if (value != null)
    {
      hash += value.hashCode();
    }
    return hash;
  }
}
