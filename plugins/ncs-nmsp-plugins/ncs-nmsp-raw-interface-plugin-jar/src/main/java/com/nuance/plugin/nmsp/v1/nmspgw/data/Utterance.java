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

public class Utterance
{
  public static final String CONTENT_TYPE_URI  = "text/uri-list";
  public static final String CONTENT_TYPE_HTML  = "text/html";
  public static final String CONTENT_TYPE_SSML = "application/synthesis+ssml";
  public static final String CONTENT_TYPE_TEXT = "text/plain";
  
  private byte[] text = null;
  private Utterance.Type type = null;
  private String contentType = null;
  private int index = -1;

  public enum Type
  {
    TEXT, URI, SSML;
    
    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public Utterance()
  {
  }

  public Utterance(byte[] text, Utterance.Type type)
  {
    this.text = text;
    setType(type);
  }

  /**
   * @param text
   *          the text to set
   */
  public void setText(byte[] text)
  {
    this.text = text;
  }

  /**
   * @return the text
   */
  public byte[] getText()
  {
    return text;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(Utterance.Type type)
  {
    this.type = type;
    setContentType();
  }

  /**
   * @return the type
   */
  public Utterance.Type getType()
  {
    return type;
  }

  /**
   * @return the content type
   */
  public String getContentType()
  {
    return contentType;
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

  public static Utterance.Type convertToType(String type)
  {
    if (type.equalsIgnoreCase("txt"))
    {
      return Utterance.Type.TEXT;
    }
    else if (type.equalsIgnoreCase("uri"))
    {
      return Utterance.Type.URI;
    }
    else if (type.equalsIgnoreCase("ssml"))
    {
      return Utterance.Type.SSML;
    }
    else
    {
      return null;
    }
  }
  
  private void setContentType()
  {
    if (type == Type.SSML)
    {
      contentType = CONTENT_TYPE_SSML;
    }
    else if (type == Type.TEXT)
    {
      contentType = CONTENT_TYPE_TEXT;
    }
    else if (type == Type.URI)
    {
      contentType = CONTENT_TYPE_URI;
    }
    else
    {
      contentType = null;
    }
  }
  
  @Override
  public String toString()
  {
    if(this.text != null) {
      return "<type=" + this.type + " text=" + new String(this.text) + "/>";
    }
    else {
      return "<type=" + this.type + " text=/>";
    }
  }
}
