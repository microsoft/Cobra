/**
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved. 
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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMdsMessage;

/**
 * @author epigeon
 *
 */
public class BCPMdsSAXHandler extends NMSPSAXHandler
{
  private BCPMdsMessage message;
  
  public BCPMdsSAXHandler(BCPMdsMessage bcpMdsMessage)
  {
    message = bcpMdsMessage;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    long longValue;
    
    try
    {
      if (qName.equalsIgnoreCase(BCPMdsMessage.REQUEST_ID_TAG))
      {
        longValue = Long.parseLong(text.toString());
        if(longValue < 0) {
          throw new SAXException("negative value not allowed for '" + BCPMdsMessage.REQUEST_ID_TAG + "' in BCPMdsMessage");
        }
        message.setRequestId(longValue);
      }
      else if (qName.equalsIgnoreCase(BCPMdsMessage.SERVICE_ID_TAG))
      {
        message.setServiceId(text.toString());
      }
      else if (qName.equalsIgnoreCase(BCPMdsMessage.TO_TAG))
      {
        message.setTo(text.toString());
      }
      else if (qName.equalsIgnoreCase(BCPMdsMessage.TTL_TAG))
      {
        longValue = Long.parseLong(text.toString());
        if(longValue < 0) {
          throw new SAXException("negative value not allowed for '" + BCPMdsMessage.TTL_TAG + "' in BCPMdsMessage");
        }
        message.setTtl(longValue);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPMdsMessage " + e.getMessage());
    }
  }

}
