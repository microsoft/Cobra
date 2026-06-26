package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Utterance;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTTSServiceRequestMessage;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BCPTTSServiceRequestSAXHandler extends NMSPSAXHandler {
  private BCPTTSServiceRequestMessage message;
  private Utterance utterance;

  public BCPTTSServiceRequestSAXHandler(BCPTTSServiceRequestMessage message) {
    this.message = message;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    super.startElement(uri, localName, qName, attributes);
    if (StringUtils.isNotEmpty(qName) && qName.toLowerCase().matches("p\\d+"))
    {
      this.utterance = new Utterance();
    }
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    int intValue;

    try {
      if ("rid".equalsIgnoreCase(qName)) {
        intValue = Integer.parseInt(text.toString());
        if (intValue < 0) {
          throw new SAXException("negative value not allowed for 'rid' in BCPTTSServiceRequestMessage");
        }
        message.setRequestId(intValue);
      } else if ("xml".equalsIgnoreCase(qName)) {
        
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPTTSServiceRequestMessage");
        }
        Parameter parameter = new Parameter(name, value, Parameter.Type.XML);
        parameter.setIndex(intValue);
        message.getParameters().add(parameter);
      }
      else if ("aid".equalsIgnoreCase(qName))
      {
        intValue = Integer.parseInt(text.toString());
        if (intValue < 0)
        {
          throw new SAXException("negative value not allowed for 'aid' in BCPTTSServiceRequestMessage");
        }
        message.setAudioId(intValue);
      }
      else if("bps".equalsIgnoreCase(qName)) {
          intValue = Integer.parseInt(text.toString());
          if(intValue < 0) {
            throw new SAXException("negative value not allowed for 'bps' in BCPTTSServiceRequestMessage");
          }
          message.setBitPerSecond(intValue);
        }
      else if ("d".equalsIgnoreCase(qName))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'd' in BCPTTSServiceRequestMessage");
        }
        utterance.setIndex(intValue);
      }
      else if ("t".equalsIgnoreCase(qName))
      {
        Utterance.Type type = Utterance.convertToType(text.toString());
        if (type != null)
        {
          utterance.setType(type);
        }
        else
        {
          throw new SAXException("invalid utterance type in BCPTTSServiceRequestMessage");
        }
      }
      else if (StringUtils.isNotEmpty(qName) && qName.toLowerCase().matches("p\\d+"))
      {
        message.getUtterances().add(utterance);
      }

    } catch (NumberFormatException e) {
      throw new SAXException("Invalid XML payload in BCPTTSServiceRequestMessage " + e.getMessage());
    }
  }
}
