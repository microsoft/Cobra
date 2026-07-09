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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPGetParamsMessage;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 * 
 */
public class BCPGetParamsSAXHandler extends NMSPSAXHandler
{
  private BCPGetParamsMessage message;

  public BCPGetParamsSAXHandler(BCPGetParamsMessage message)
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
          throw new SAXException("negative value not allowed for 'rid' in BCPGetParamsMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPGetParamsMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("nmsp"))
      {
        Parameter parameter = new Parameter(name, Parameter.Type.NMSP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("app"))
      {
        Parameter parameter = new Parameter(name, Parameter.Type.APP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("ext"))
      {
        Parameter parameter = new Parameter(name, Parameter.Type.EXT);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nss"))
      {
        Parameter parameter = new Parameter(name, Parameter.Type.NSS);
        message.getParameters().add(parameter);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPGetParamsMessage " + e.getMessage());
    }
  }
}
