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

import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Utterance;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGenerateAudioMessage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 * 
 */
public class BCPGenerateAudioSAXHandler extends NMSPSAXHandler
{
  private BCPGenerateAudioMessage message;
  private Utterance utterance;

  public BCPGenerateAudioSAXHandler(BCPGenerateAudioMessage message)
  {
    this.message = message;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    super.startElement(uri, localName, qName, attributes);
    if (qName.toLowerCase().matches("p\\d+"))
    {
      this.utterance = new Utterance();
    }
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
          throw new SAXException("negative value not allowed for 'rid' in BCPGenerateAudioMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPGenerateAudioMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("aid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'aid' in BCPGenerateAudioMessage");
        }
        message.setAudioId(intValue);
      }
      else if (qName.equalsIgnoreCase("bs"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'bs' in BCPGenerateAudioMessage");
        }
        message.setBufferSize(intValue);
      }
      else if (qName.equalsIgnoreCase("eos"))
      {
        message.setEndOfSentence(true);
      }
      else if (qName.equalsIgnoreCase("d"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'd' in BCPGenerateAudioMessage");
        }
        utterance.setIndex(intValue);
      }
      else if (qName.equalsIgnoreCase("t"))
      {
        Utterance.Type type = Utterance.convertToType(text.toString());
        if (type != null)
        {
          utterance.setType(type);
        }
        else
        {
          throw new SAXException("invalid utterance type in BCPGenerateAudioMessage");
        }
      }
      else if (qName.toLowerCase().matches("p\\d+"))
      {
        message.getUtterances().add(utterance);
      }
      // Generate audio now supports NMSP parameter to be able to set the SecureLog param
      else if (qName.equalsIgnoreCase("nmsp"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NMSP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nss"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NSS);
        message.getParameters().add(parameter);
      }
      else if(qName.equalsIgnoreCase("bps")) {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'bps' in BCPGenerateAudioMessage");
        }
        message.setBitPerSecond(intValue);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPGenerateAudioMessage " + e.getMessage());
    }
  }
}
