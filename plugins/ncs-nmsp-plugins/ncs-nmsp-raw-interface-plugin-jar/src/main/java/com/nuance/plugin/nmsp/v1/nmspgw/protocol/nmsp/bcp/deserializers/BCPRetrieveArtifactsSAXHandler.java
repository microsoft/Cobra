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

import java.util.Optional;

import org.xml.sax.SAXException;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPRetrieveArtifactsMessage;

public class BCPRetrieveArtifactsSAXHandler extends NMSPSAXHandler
{
  private final BCPRetrieveArtifactsMessage message;

  public BCPRetrieveArtifactsSAXHandler(final BCPRetrieveArtifactsMessage message)
  {
    this.message = message;
  }

  public void endElement(final String uri, final String localName, final String qName) throws SAXException
  {
    try
    {
      if (qName.equalsIgnoreCase("rid"))
      {
        message.setRequestId(Long.parseLong(text.toString()));
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        message.setResourceId(Long.parseLong(text.toString()));
      }
      else if (qName.equalsIgnoreCase("tsid"))
      {
        message.setTargetSessionId(text.toString());
      }
      else if (qName.equalsIgnoreCase("aid"))
      {
        message.setAudioId(Long.parseLong(text.toString()));
      }
      else if (qName.equalsIgnoreCase("bs"))
      {
        message.setBufferSize(Optional.of(Long.parseLong(text.toString())));
      }
      else if(qName.equalsIgnoreCase("bps")) {
        message.setBitPerSecond(Optional.of(Long.parseLong(text.toString())));
      }
    }
    catch (final NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPRetrieveArtifactsMessage " + e.getMessage());
    }
  }
}
