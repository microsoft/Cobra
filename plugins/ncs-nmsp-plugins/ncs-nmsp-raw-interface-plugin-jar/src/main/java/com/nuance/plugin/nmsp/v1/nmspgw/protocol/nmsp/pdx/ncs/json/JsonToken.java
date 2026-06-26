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

public class JsonToken
{
  private JsonTokenType type;
  private Object object;

  public JsonToken(JsonTokenType type, Object object)
  {
    this.type = type;
    this.object = object;
  }

  public JsonTokenType getType()
  {
    return type;
  }

  public void setType(JsonTokenType type)
  {
    this.type = type;
  }

  public Object getObject()
  {
    return object;
  }

  public void setObject(Object object)
  {
    this.object = object;
  }
}
