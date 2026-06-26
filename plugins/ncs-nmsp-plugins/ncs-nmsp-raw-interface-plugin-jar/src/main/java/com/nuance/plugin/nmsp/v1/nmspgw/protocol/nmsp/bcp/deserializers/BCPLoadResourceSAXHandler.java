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

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.data.ResourceType;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadResourceMessage;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

import static com.nuance.plugin.nmsp.v1.nmspgw.data.ResourceType.*;

/**
 * @author epigeon
 */
public class BCPLoadResourceSAXHandler extends NMSPSAXHandler {
  private BCPLoadResourceMessage message;
  private Map<String, String> capabilities = new HashMap<String, String>();

  /**
   * 
   */
  public BCPLoadResourceSAXHandler(BCPLoadResourceMessage message)
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
          throw new SAXException("negative value not allowed for 'rid' in BCPLoadResourceMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPLoadResourceMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("username"))
      {
        message.setUserName(text.toString());
      }
      else if (qName.equalsIgnoreCase("oper"))
      {
        if(message.getResourceType() == null) {
          message.setResourceType(OPERATOR);
        }
        else {
          throw new SAXException("cannot specify more than one resource in BCPLoadResourceMessage");
        }
      }
      // else if(qName.equalsIgnoreCase("nr9")) {
      // }
      else if (qName.equalsIgnoreCase("reco"))
      {
        if (message.getResourceType() == TTS)
        {
          message.setResourceType(RECOGNITION_TTS);
        }
        else if(message.getResourceType() == null)
        {
          message.setResourceType(RECOGNITION);
        }
        else {
          throw new SAXException("cannot specify more than one resource in BCPLoadResourceMessage");
        }
        message.setRecoCapabilities(capabilities);
        capabilities.clear();
      }
      else if (qName.equalsIgnoreCase("tts"))
      {
        if (message.getResourceType() == RECOGNITION)
        {
          message.setResourceType(RECOGNITION_TTS);
        }
        else if(message.getResourceType() == null)
        {
          message.setResourceType(TTS);
        }
        else {
          throw new SAXException("cannot specify more than one resource in BCPLoadResourceMessage");
        }
        message.setTtsCapabilities(capabilities);
        capabilities.clear();
      }
      else if (qName.equalsIgnoreCase("dict"))
      {
        if(message.getResourceType() == null) {
          message.setResourceType(ResourceType.DICTATION);
        }
        else {
          throw new SAXException("cannot specify more than one resource in BCPLoadResourceMessage");
        }
        message.setDictationCapabilities(capabilities);
        capabilities.clear();
      }
      else if (qName.equalsIgnoreCase("sip"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.SIP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("sdp"))
      {
        Parameter parameter = new Parameter(name, value, Parameter.Type.SDP);
        message.getParameters().add(parameter);
      }
      else if (qName.equalsIgnoreCase("s2"))
      {
        message.setResourceType(S2);
      }
      else if(qName.equalsIgnoreCase("codec")) {
        intValue = Integer.parseInt(text.toString());
        Codec codec = Codec.fromNMAINCodecId(intValue);
        if(codec == Codec.UNKNOWN) {
          throw new SAXException("value not allowed for 'codec' in BCPLoadResourceMessage: " + text.toString());
        }
        message.setCodec(codec);
      }
      else if(qName.equalsIgnoreCase("capability")) {
        capabilities.put(name, value);
      }
      else if(qName.equalsIgnoreCase("deferloading")) {
        try {
          Utils.DeferNSSLoading deferNSSLoading = Utils.DeferNSSLoading.valueOf(text.toString());
          switch(deferNSSLoading) {
          case OFF:
            message.setDeferNSSLoading(Utils.DeferNSSLoading.OFF);
            break;
          case WAIT_AUDIO:
            message.setDeferNSSLoading(Utils.DeferNSSLoading.WAIT_AUDIO);
            break;
          case WAIT_SPEECH:
            message.setDeferNSSLoading(Utils.DeferNSSLoading.WAIT_SPEECH);
            break;
          }
        }
        catch(Exception e) {
          throw new SAXException("value not allowed for 'deferloading' in BCPLoadResourceMessage: " + text.toString(), e);
        }
      }
      else if (qName.equalsIgnoreCase("clientAudioStreamingMaxIdleTimeMSec")) {
        intValue = Integer.parseInt(text.toString());
        if (intValue <= 0) {
          throw new SAXException("value for 'clientAudioStreamingMaxIdleTimeMSec' must be greater than 0 in BCPLoadResourceMessage");
        }
        message.setClientAudioStreamingMaxIdleTimeMSec(intValue);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPLoadResourceMessage " + e.getMessage());
    }
  }
}
