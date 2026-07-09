/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.deserializers.VAPMessageDeserializer;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author epigeon
 */
public class NMSPMessageDeserializer {
    private static Logger logger = LogManager.getLogger(NMSPMessageDeserializer.class);

    private NMSPMessageDeserializer() {
    }

    public static NMSPMessage deserialize(short protocol, short version, int command, ByteBuf message, Session session) throws CorruptedFrameException {
        logger.debug(">====== Deserializing a {} bytes message", +message.readableBytes());

        switch (protocol) {
            case NMSPMessage.COP:
                return COPMessageDeserializer.deserialize(version, command, message);
            case NMSPMessage.BCP:
                return BCPMessageDeserializer.deserialize(version, command, message, session);
            case NMSPMessage.VAP:
                return VAPMessageDeserializer.deserialize(version, command, message, session);
            default:
                logger.warn("Invalid protocol: {}", protocol);
                throw new CorruptedFrameException("Invalid protocol: " + protocol);
        }
    }

    public static void parseXMLPayload(byte[] payload, NMSPSAXHandler handler) throws CorruptedFrameException {
        final var is = new InputSource(new ByteArrayInputStream(payload));
        is.setEncoding("UTF-8");

        try {
            //https://docs.oracle.com/en/java/javase/11/security/java-api-xml-processing-jaxp-security-guide.html

            //https://community.veracode.com/s/article/Java-Remediation-Guidance-for-XXE
            //A common mistake occurs when creating a new DocumentBuilder or Transformer
            //object without declaring and instantiating a new instance of DocumentBuilderFactory
            //or TransformerFactory first. While this is not wrong, this is not something the
            //static engine can determine as a remediation and will require a mitigation

            var saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            var saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(is, handler);
        } catch (final SAXException e) {
            logger.warn("Invalid XML message payload", e);
            throw new CorruptedFrameException(e.toString(), e);
        } catch (final IOException | ParserConfigurationException e) {
            logger.warn("Failed to parse XML message payload", e);
            throw new CorruptedFrameException(e.toString(), e);
        }
    }

}
