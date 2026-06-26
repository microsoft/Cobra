/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.data;

import java.util.Objects;

public class Parameter
{
  private String name;
  private String value;
  private Parameter.Type type;
  private int index = -1;

  public enum Type
  {
    XML, URI, NMSP, APP, NSS, SLOG, NSSLOG, GWLOG, SVSP, EXT, SIP, SDP;
    
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public Parameter(String name, String value, Parameter.Type type)
  {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public Parameter(String name, Parameter.Type type)
  {
    this.name = name;
    this.value = null;
    this.type = type;
  }

  public Parameter(String name, Parameter.Type type, int index)
  {
    this.name = name;
    this.value = null;
    this.type = type;
    this.index = index;
  }

  /**
   * Returns the name of the parameter.
   * 
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the value of the parameter.
   * 
   * @return
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * Returns the value of the parameter.
   * 
   * @return
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Returns the type of the parameter.
   * 
   * @return
   */
  public Parameter.Type getType()
  {
    return type;
  }

  /**
   * @param index
   *          the index to set
   */
  public void setIndex(int index)
  {
    if(index >= 0) {
      this.index = index;
    }
  }

  /**
   * @return the index
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Converts a String to a parameter type. Returns null if the String doesn't correspond to any type.
   * 
   * @param type
   * @return
   */
  public static Parameter.Type convertToType(String type)
  {
    if (type.equalsIgnoreCase("xml"))
    {
      return Parameter.Type.XML;
    }
    else if (type.equalsIgnoreCase("uri"))
    {
      return Parameter.Type.URI;
    }
    else if (type.equalsIgnoreCase("nmsp"))
    {
      return Parameter.Type.NMSP;
    }
    else if (type.equalsIgnoreCase("app"))
    {
      return Parameter.Type.APP;
    }
    else if (type.equalsIgnoreCase("nss"))
    {
      return Parameter.Type.NSS;
    }
    else if (type.equalsIgnoreCase("slog"))
    {
      return Parameter.Type.SLOG;
    }
    else if (type.equalsIgnoreCase("nsslog"))
    {
      return Parameter.Type.NSSLOG;
    }
    else if (type.equalsIgnoreCase("gwlog"))
    {
      return Parameter.Type.GWLOG;
    }
    else if (type.equalsIgnoreCase("svsp"))
    {
      return Parameter.Type.SVSP;
    }
    else if (type.equalsIgnoreCase("ext"))
    {
      return Parameter.Type.EXT;
    }
    else if (type.equalsIgnoreCase("sip"))
    {
      return Parameter.Type.SIP;
    }
    else if (type.equalsIgnoreCase("sdp"))
    {
      return Parameter.Type.SDP;
    }
    else
    {
      return null;
    }
  }
  
  @Override
  public boolean equals(Object param){
    if (param == null) return false;
    if (param == this) return true;
    if (this.getClass() != param.getClass())return false;
    Parameter otherParam = (Parameter)param;
    if (this.type != otherParam.type) return false;
    if (this.name != null) {
      if (!this.name.equals(otherParam.name)) return false;
    }
    else { // this.name == null
      if(otherParam.name != null) return false;
    }
    if (this.value != null) {
      if (!this.value.equals(otherParam.value)) return false;
    }
    else { // this.value == null
      if(otherParam.value != null) return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
      return Objects.hash(name, value, type);
  }
  
  @Override
  public String toString()
  {
    return "<" + this.type + " p=" + this.name + " v=" + this.value + "/>";
  }
}
