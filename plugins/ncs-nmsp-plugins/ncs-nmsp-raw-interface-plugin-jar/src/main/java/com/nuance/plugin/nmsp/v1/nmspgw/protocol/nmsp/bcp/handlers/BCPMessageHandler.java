/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.*;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class BCPMessageHandler {
    private static final Logger logger = LogManager.getLogger();

    public static void handleMessage(Session session, BCPMessage message, ISessionListener sessionListener) {
        switch (message.getCommand()) {
            case BCPMessage.BCP_GENERATE_AUDIO:
                BCPGenerateAudioHandler.handleMessage(session, (BCPGenerateAudioMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_TTS_SERVICE_REQUEST:
                //DEPRECATED AFAIK is part of the old protocol
                //BCPTTSServiceRequestHandler.handleMessage(session, (BCPTTSServiceRequestMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_LOAD_GRAMMAR:
                BCPLoadGrammarHandler.handleMessage(session, (BCPLoadGrammarMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_RECOGNIZE:
                BCPRecognizeHandler.handleMessage(session, (BCPRecognizeMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_CANCEL:
                BCPCancelHandler.handleMessage(session, (BCPCancelMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_RETRIEVE_ARTIFACTS:
                BCPRetrieveArtifactsHandler.handleMessage(session, (BCPRetrieveArtifactsMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_SET_PARAMS:
                BCPSetParamsHandler.handleMessage(session, (BCPSetParamsMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_GET_PARAMS:
                BCPGetParamsHandler.handleMessage(session, (BCPGetParamsMessage) message, sessionListener);
                break;
            // Cannot come from the client
            // case BCPMessage.BCP_END:
            // BCPEndHandler.handleMessage(session, (BCPEndMessage)message);
            // break;
            case BCPMessage.BCP_LOAD_RESOURCE:
                BCPLoadResourceHandler.handleMessage(session, (BCPLoadResourceMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_FREE_RESOURCE:
                BCPFreeResourceHandler.handleMessage(session, (BCPFreeResourceMessage) message);
                break;
            case BCPMessage.BCP_FREE_RESOURCE_ID:
                BCPFreeResourceIdHandler.handleMessage(session, (BCPFreeResourceIdMessage) message);
                break;
            case BCPMessage.BCP_INTERPRET:
                BCPInterpretHandler.handleMessage(session, (BCPInterpretMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_DATA:
                BCPDataHandler.handleMessage(session, (BCPDataMessage) message);
                break;
            case BCPMessage.BCP_BEGIN:
                // update the bcp version based on the received message..this must be done here,
                // before the calls to BCPBeginHandlerRegistry...as this determines the handler to be used
                BCPBeginHandler.handleMessage(session, (BCPBeginMessage) message);
                break;
            case BCPMessage.BCP_RESOURCE_TUNNEL:
                BCPResourceTunnelHandler.handleMessage(session, (BCPResourceTunnelMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_TRANSCODE:
                BCPTranscodeHandler.handleMessage(session, (BCPTranscodeMessage) message, sessionListener);
                break;
            case BCPMessage.BCP_LOG:
                BCPLogHandler.handleMessage(session, (BCPLogMessage) message);
                break;
            case BCPMessage.BCP_MDS_MESSAGE:
                BCPMdsMessageHandler.handleMessage(session, (BCPMdsMessage) message);
                break;
            case BCPMessage.BCP_MDS_MESSAGE_RESPONSE:
                BCPMdsMessageResponseHandler.handleMessage(session, (BCPMdsMessageResponse) message);
                break;
        }
    }
}
