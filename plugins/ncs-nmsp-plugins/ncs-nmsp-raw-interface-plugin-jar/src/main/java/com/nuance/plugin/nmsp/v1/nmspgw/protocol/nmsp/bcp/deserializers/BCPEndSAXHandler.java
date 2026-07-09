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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPEndMessage;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 *
 */
public class BCPEndSAXHandler extends NMSPSAXHandler
{
  private BCPEndMessage message;
  
  public BCPEndSAXHandler(BCPEndMessage bcpEndMessage)
  {
    message = bcpEndMessage;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {    
    if (qName.equalsIgnoreCase(BCPEndMessage.STATUS_TAG))
    {
      String status = text.toString().toUpperCase();
      switch(status) {
      case "PASS":
        message.setStatus(Utils.Status.PASS);
        break;
      case "FAIL":
        message.setStatus(Utils.Status.FAIL);
        break;
      case "ABORT":
        message.setStatus(Utils.Status.ABORT);
        break;
      default:
        throw new SAXException("invalid value for '" + BCPEndMessage.STATUS_TAG + "' in BCPEndMessage");
      }
    }
    else if (qName.equalsIgnoreCase(BCPEndMessage.REPORT_LATENCY_TAG))
    {
      message.setReportLatency(Boolean.parseBoolean(text.toString()));
    }
  }

}
