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
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPSetParamsMessage;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 * 
 */
public class BCPSetParamsSAXHandler extends NMSPSAXHandler
{
  private BCPSetParamsMessage message;
  private boolean sendResponse = false;

  public BCPSetParamsSAXHandler(BCPSetParamsMessage message)
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
          throw new SAXException("negative value not allowed for 'rid' in BCPSetParamsMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPSetParamsMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("noresponse"))
      {
        message.setSendResponse(false);
      }
      else if (qName.equalsIgnoreCase("nmsp"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NMSP);
        message.getParameters().add(parameter);
        sendResponse = true;
      }
      else if (qName.equalsIgnoreCase("app"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.APP);
        message.getParameters().add(parameter);
        sendResponse = true;
      }
      else if (qName.equalsIgnoreCase("ext"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.EXT);
        message.getParameters().add(parameter);
        sendResponse = true;
      }
      else if (qName.equalsIgnoreCase("nss"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.NSS);
        message.getParameters().add(parameter);
        sendResponse = true;
      }
      else if (qName.equalsIgnoreCase("slog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPSetParamsMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("nsslog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPSetParamsMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.NSSLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("gwlog"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPSetParamsMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.GWLOG, intValue);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("svsp"))
      {
        intValue = Integer.parseInt(value);
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'binary payload index' in BCPSetParamsMessage");
        }
        Parameter parameter = new Parameter(name, Parameter.Type.SVSP, intValue);
        message.getParameters().add(parameter);
        sendResponse = true;
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPSetParamsMessage " + e.getMessage());
    }
  }

  public void endDocument() throws org.xml.sax.SAXException
  {
    if (!sendResponse)
    {
      message.setSendResponse(false);
    }
  }
}
