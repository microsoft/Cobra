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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPFreeResourceIdMessage;

/**
 * @author epigeon
 * 
 */
public class BCPFreeResourceIdSAXHandler extends NMSPSAXHandler
{
  private BCPFreeResourceIdMessage message;
  private Long resourceId;
  private Long timeout;

  public BCPFreeResourceIdSAXHandler(BCPFreeResourceIdMessage message)
  {
    this.message = message;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    super.startElement(uri, localName, qName, attributes);

    if (qName.toLowerCase().startsWith("resid"))
    {
      resourceId = null;
      timeout = null;
    }
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    int requestId;

    try
    {
      if (qName.equalsIgnoreCase("rid"))
      {
        requestId = Integer.parseInt(text.toString());
        if(requestId < 0) {
          throw new SAXException("negative value not allowed for 'rid' in BCPFreeResourceIdMessage");
        }
        message.setRequestId(requestId);
      }
      else if (qName.equalsIgnoreCase("id"))
      {
        resourceId = new Long(text.toString());
        if(resourceId.longValue() < 0) {
          throw new SAXException("negative value not allowed for 'id' in BCPFreeResourceIdMessage");
        }
      }
      else if (qName.equalsIgnoreCase("timeout"))
      {
        timeout = new Long(text.toString());
        if(timeout.longValue() < 0) {
          throw new SAXException("negative value not allowed for 'timeout' in BCPFreeResourceIdMessage");
        }
      }
      else if (qName.toLowerCase().startsWith("resid"))
      {
        if(resourceId == null) {
          throw new SAXException("missing element 'id' in BCPFreeResourceIdMessage");
        }
        if(timeout == null) {
          throw new SAXException("missing element 'timeout' in BCPFreeResourceIdMessage");
        }
        message.getResources().put(resourceId, timeout);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPFreeResourceIdMessage " + e.getMessage());
    }
  }
}
