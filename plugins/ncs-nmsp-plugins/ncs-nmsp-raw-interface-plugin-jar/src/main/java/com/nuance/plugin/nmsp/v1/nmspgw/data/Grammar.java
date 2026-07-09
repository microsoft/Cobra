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

public class Grammar
{
  private static final String CONTENT_TYPE_URI = "text/uri-list";
  private static final String CONTENT_TYPE_XML = "application/srgs+xml";
  
  private byte[] grammar = null;
  private String name = null;
  private Grammar.Type type = null;
  private int weight = -1;
  private int index = -1;
  private String id = null;
  private String contentType = null;

  public enum Type
  {
    XML, URI, ID;
    
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public Grammar()
  {
  }

  public Grammar(byte[] grammar, Grammar.Type type, int weight)
  {
    this.grammar = grammar;
    this.weight = weight;
    setType(type);
  }

  public Grammar(String name, Grammar.Type type, int index, int weight)
  {
    this.name = name;
    this.index = index;
    this.weight = weight;
    setType(type);
  }
  
  public Grammar(String id, int weight)
  {
    this.id = id;
    this.weight = weight;
    this.type = Type.ID;
  }

  /**
   * @param grammar
   *          the grammar to set
   */
  public void setGrammar(byte[] grammar)
  {
    this.grammar = grammar;
  }

  /**
   * @return the grammar
   */
  public byte[] getGrammar()
  {
    return grammar;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(Grammar.Type type)
  {
    this.type = type;
    setContentType();
  }

  /**
   * @return the type
   */
  public Grammar.Type getType()
  {
    return type;
  }

  /**
   * @return the content type set from the type
   */
  public String getContentType()
  {
    return contentType;
  }

  /**
   * @param weight
   *          the weight to set
   */
  public void setWeight(int weight)
  {
    if(weight >= 0) {
      this.weight = weight;
    }
  }

  /**
   * @return the weight
   */
  public int getWeight()
  {
    return weight;
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
   * @param id
   *          the id to set
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId()
  {
    return id;
  }

  public static Grammar.Type convertToType(String type)
  {
    if (type.equalsIgnoreCase("xml"))
    {
      return Grammar.Type.XML;
    }
    else if (type.equalsIgnoreCase("uri"))
    {
      return Grammar.Type.URI;
    }
    else if (type.equalsIgnoreCase("id"))
    {
      return Grammar.Type.ID;
    }
    else
    {
      return null;
    }
  }
  
  private void setContentType()
  {
    if (type == Type.URI)
    {
      contentType = CONTENT_TYPE_URI;
    }
    else if (type == Type.XML)
    {
      contentType = CONTENT_TYPE_XML;
    }
    else
    {
      contentType = null;
    }
  }
  
  @Override
  public String toString()
  {
    if(this.name != null) {
      if(this.grammar != null) {
        return "<type=" + this.type + " name=" + this.name + " weight=" + this.weight + " content=" + new String(this.grammar) + "/>";
      }
      else {
        return "<type=" + this.type + " name=" + this.name + " weight=" + this.weight + " content=/>";
      }
    }
    else {
      return "<type=" + this.type + " id=" + this.id + " weight=" + this.weight + "/>";
    }
  }
}
