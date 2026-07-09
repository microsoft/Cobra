/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.Grammar;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPInterpretMessage;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;

/**
 * @author epigeon
 *
 */
public class BCPInterpretSAXHandler extends NMSPSAXHandler
{
  private BCPInterpretMessage message;

  public BCPInterpretSAXHandler(BCPInterpretMessage message)
  {
    this.message = message;
  }
  
  /* (non-Javadoc)
   * @see com.nuance.nvc.nmspgw.protocol.nmsp.NMSPSAXHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    int intValue;
    
    try
    {
      if (qName.equalsIgnoreCase("rid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'rid' in BCPInterpretMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPInterpretMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("text"))
      {
        String utf8Text;
        try
        {
          utf8Text = new String(text.toString().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
          utf8Text = text.toString();
        }
        message.setText(utf8Text);
      }
      else if (qName.equalsIgnoreCase("p"))
      {
        message.setPoolName(text.toString());
      }
      else if (qName.equalsIgnoreCase("uri"))
      {
        int grammarWeigth = -1;
        if (weigth != null)
        {
          try
          {
            grammarWeigth = Integer.parseInt(weigth);
            if(grammarWeigth < 0) {
              throw new SAXException("negative value not allowed for 'w' in BCPInterpretMessage");
            }
          }
          catch (Exception e)
          {
            throw new SAXException("Invalid grammar weigth in BCPInterpretMessage", e);
          }
        }
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Grammar grammar = new Grammar(name, Grammar.Type.URI, intValue, grammarWeigth);
        message.getGrammars().add(grammar);
      }
      else if (qName.equalsIgnoreCase("xml"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Grammar grammar = new Grammar(name, Grammar.Type.XML, intValue, 0);
        message.getGrammars().add(grammar);
      }
      else if (qName.toLowerCase().matches("i\\d+"))
      {
        int grammarWeigth = -1;
        if (weigth != null)
        {
          try
          {
            grammarWeigth = Integer.parseInt(weigth);
            if(grammarWeigth < 0) {
              throw new SAXException("negative value not allowed for 'w' in BCPInterpretMessage");
            }
          }
          catch (Exception e)
          {
            throw new SAXException("Invalid grammar weigth in BCPInterpretMessage", e);
          }
        }
        Grammar grammar = new Grammar(text.toString(), grammarWeigth);
        message.getGrammars().add(grammar);
      }
      else if (qName.equalsIgnoreCase("nmsp"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NMSP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("app"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.APP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("ext"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.EXT);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nss"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NSS);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("slog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nsslog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.NSSLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("gwlog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.GWLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("svsp"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPInterpretMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SVSP, intValue);
        message.getParameters().add(parameter);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPInterpretMessage " + e.getMessage());
    }
  }

}
