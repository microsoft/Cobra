/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers;

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioContainerType;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.COPSAXException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 *
 */
public class COPConnectSAXHandler extends NMSPSAXHandler {
    private final COPConnectMessage message;

    public COPConnectSAXHandler(COPConnectMessage message) {
        this.message = message;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        int intValue;

        try {
            if (qName.equalsIgnoreCase("tsc")) {
                intValue = Integer.parseInt(text.toString());
                // Allow invalid Codec to be backward compatible
//        if(intValue < 0) {
//          throw new SAXException("negative value not allowed for 'tsc' in COPConnectMessage");
//        }
                Codec codec = Codec.fromNMAINCodecId(intValue);
//        if(codec == Codec.UNKNOWN) {
//          throw new SAXException("value not allowed for 'tsc' in COPConnectMessage: " + text.toString());
//        }
                message.setToCodec(codec);
            } else if (qName.equalsIgnoreCase("fsc")) {
                intValue = Integer.parseInt(text.toString());
                // Allow invalid Codec to be backward compatible
//        if(intValue < 0) {
//          throw new SAXException("negative value not allowed for 'fsc' in COPConnectMessage");
//        }
                Codec codec = Codec.fromNMAINCodecId(intValue);
//        if(codec == Codec.UNKNOWN) {
//          throw new SAXException("value not allowed for 'fsc' in COPConnectMessage: " + text.toString());
//        }
                message.setFromCodec(codec);
            } else if (qName.equalsIgnoreCase("nmaid")) {
                message.setNmaid(text.toString());
            } else if (qName.equalsIgnoreCase("rootlog")) {
                message.setParent(text.toString());
            } else if (qName.equalsIgnoreCase("disablecalllogging")) {
                message.setDisableCallLogging(true);
            } else if (qName.equalsIgnoreCase("connecttomds")) {
                message.setConnectToMds(true);
            } else if (qName.equalsIgnoreCase("s")) {
                if (!text.toString().isEmpty()) {
                    message.setSessionId(text.toString());
                }
            } else if (qName.equalsIgnoreCase("t")) {
                short shortValue = Short.parseShort(text.toString());
                if (shortValue < 0) {
                    throw new COPSAXException("negative value not allowed for 't' in COPConnectMessage");
                }
                message.setDeviceType(shortValue);
            } else if (qName.equalsIgnoreCase("r")) {
                if (!text.toString().isEmpty()) {
                    message.setReconnectSessionId(text.toString());
                }
            } else if (qName.equalsIgnoreCase("b")) {
                message.setBuildNumber(text.toString());
            } else if (qName.equalsIgnoreCase("bs")) {
                intValue = Integer.parseInt(text.toString());
                if (intValue < 0) {
                    throw new COPSAXException("negative value not allowed for 'bs' in COPConnectMessage");
                }
                message.setBufferSize(intValue);
            } else if (qName.equalsIgnoreCase("uid")) {
                final String uid = text.toString();
                message.setUserId(uid);
            }
            // Un-documented, might need to support it in the future
            //else if (qName.equalsIgnoreCase("deviceid"))
            //{
            //  final String deviceId = text.toString();
            //  message.setDeviceId(deviceId);
            //}
            else if (qName.equalsIgnoreCase("app")) {
                Parameter parameter = new Parameter(name, value, Parameter.Type.APP);
                message.getParameters().add(parameter);
            } else if (qName.equalsIgnoreCase("nmsp")) {
                Parameter parameter = new Parameter(name, value, Parameter.Type.NMSP);
                message.getParameters().add(parameter);
            } else if (qName.equalsIgnoreCase("nss")) {
                Parameter parameter = new Parameter(name, value, Parameter.Type.NSS);
                message.getParameters().add(parameter);
            } else if ("audio_container_type".equalsIgnoreCase(qName)) {
                //When the client's connect Json does not contain the key audio_container_type, WSP sets the default value of None for this property
                if (!text.toString().equals("None") && text.length() != 0) {
                    if (text.toString().equalsIgnoreCase(AudioContainerType.ogg.toString())) {
                        message.setAudioContainerType(AudioContainerType.valueOf(text.toString().toLowerCase()));
                    } else {
                        throw new COPSAXException("Param error,Can't recognize this audio_container_type");
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new COPSAXException("Invalid XML payload in COPConnectMessage " + e.getMessage());
        }
    }
}
