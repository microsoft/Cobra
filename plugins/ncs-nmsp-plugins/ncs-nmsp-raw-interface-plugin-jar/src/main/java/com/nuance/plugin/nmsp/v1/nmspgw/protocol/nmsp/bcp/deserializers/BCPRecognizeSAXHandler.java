/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.Grammar;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRecognizeMessage;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 * 
 */
public class BCPRecognizeSAXHandler extends NMSPSAXHandler
{
  private BCPRecognizeMessage message;

  public BCPRecognizeSAXHandler(BCPRecognizeMessage message)
  {
    this.message = message;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    int intValue;
    
    try
    {
      if (qName.equalsIgnoreCase("rid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'rid' in BCPRecognizeMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPRecognizeMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("aid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'aid' in BCPRecognizeMessage");
        }
        message.setAudioId(intValue);
      }
      else if (qName.equalsIgnoreCase("arb"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'arb' in BCPRecognizeMessage");
        }
        message.setAudioRangeBegin(intValue);
      }
      else if (qName.equalsIgnoreCase("are"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'are' in BCPRecognizeMessage");
        }
        message.setAudioRangeEnd(intValue);
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
              throw new SAXException("negative value not allowed for 'w' in BCPRecognizeMessage");
            }
          }
          catch (Exception e)
          {
            throw new SAXException("Invalid grammar weigth in BCPRecognizeMessage", e);
          }
        }
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Grammar grammar = new Grammar(name, Grammar.Type.URI, intValue, grammarWeigth);
        message.getGrammars().add(grammar);
      }
      else if (qName.equalsIgnoreCase("xml"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Grammar grammar = new Grammar(name, Grammar.Type.XML, intValue, -1);
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
              throw new SAXException("negative value not allowed for 'w' in BCPRecognizeMessage");
            }
          }
          catch (Exception e)
          {
            throw new SAXException("Invalid grammar weigth in BCPRecognizeMessage", e);
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
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nsslog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.NSSLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("gwlog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.GWLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("svsp"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPRecognizeMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SVSP, intValue);
        message.getParameters().add(parameter);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPRecognizeMessage " + e.getMessage());
    }
  }

}
