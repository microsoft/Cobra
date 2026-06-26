/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import org.xml.sax.SAXException;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPTranscodeMessage;

/**
 * @author epigeon
 *
 */
public class BCPTranscodeSAXHandler extends NMSPSAXHandler
{

  private BCPTranscodeMessage message;
  
  public BCPTranscodeSAXHandler(BCPTranscodeMessage bcpTranscode)
  {
    message = bcpTranscode;
  }

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
          throw new SAXException("negative value not allowed for 'rid' in BCPTranscodeMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("aid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'aid' in BCPTranscodeMessage");
        }
        message.setAudioId(intValue);
      }
      else if (qName.equalsIgnoreCase("codec"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'codec' in BCPTranscodeMessage");
        }
        message.setCodec(intValue);
      }
      else if (qName.equalsIgnoreCase("protocol"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'protocol' in BCPTranscodeMessage");
        }
        message.setTransportProtocol(intValue);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPTranscodeMessage " + e.getMessage());
    }
    
  }

}
