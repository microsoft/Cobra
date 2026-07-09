package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class NMSPSAXHandler extends DefaultHandler
{

  protected StringBuilder text;
  protected String name;
  protected String value;
  protected String weigth;

  public NMSPSAXHandler()
  {
    super();
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    text = new StringBuilder();
    weigth = null;
    for (int i = 0; i < attributes.getLength(); i++)
    {
      if (attributes.getQName(i).equalsIgnoreCase("p"))
      {
        name = attributes.getValue(i);
      }
      else if (attributes.getQName(i).equalsIgnoreCase("v"))
      {
        value = attributes.getValue(i);
      }
      else if (attributes.getQName(i).equalsIgnoreCase("w"))
      {
        weigth = attributes.getValue(i);
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    text.append(ch, start, length);
  }

  public abstract void endElement(String uri, String localName, String qName) throws SAXException;

}
