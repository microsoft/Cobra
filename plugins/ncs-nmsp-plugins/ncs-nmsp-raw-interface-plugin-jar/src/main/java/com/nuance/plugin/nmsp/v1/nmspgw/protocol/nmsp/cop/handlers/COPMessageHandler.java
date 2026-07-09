/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.*;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 */
public class COPMessageHandler {

    private COPMessageHandler() {
    }

    public static void handleMessage(Session session, COPMessage message) {
        switch (message.getCommand()) {
            case COPMessage.COP_CONNECT:
                COPConnectHandler.handleMessage(session, (COPConnectMessage) message);
                break;
            case COPMessage.COP_CONFIRM:
                COPConfirmHandler.handleMessage(session, (COPConfirmMessage) message);
                break;
            case COPMessage.COP_DISCONNECT:
                COPDisconnectHandler.handleMessage(session, (COPDisconnectMessage) message);
                break;
            case COPMessage.COP_PING_RESPONSE:
                COPPingResponseHandler.handleMessage(session, (COPPingResponseMessage) message);
                break;
            // Route request is no longer supported
            // case COPMessage.COP_ROUTE_REQUEST:
            // break;
        }
    }
}
