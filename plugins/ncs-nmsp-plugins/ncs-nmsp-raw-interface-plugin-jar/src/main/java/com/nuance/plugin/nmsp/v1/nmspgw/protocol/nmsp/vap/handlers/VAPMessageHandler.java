/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.handlers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.*;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 */
public class VAPMessageHandler {

    public static void handleMessage(Session session, VAPMessage message) {
        switch (message.getCommand()) {
            case VAPMessage.VAP_RECORD_END:
                VAPRecordEndHandler.handleMessage(session, (VAPRecordEndMessage) message);
                break;
            case VAPMessage.VAP_RECORD_BEGIN:
                VAPRecordBeginHandler.handleMessage(session, (VAPRecordBeginMessage) message);
                break;
            case VAPMessage.VAP_RECORD:
                VAPRecordHandler.handleMessage(session, (VAPRecordMessage) message);
                break;
            // case VAPMessage.VAP_BACKUP_REQUEST:
            // VAPBackupRequestHandler.handleMessage(session, (VAPBackupRequestMessage)message);
            // break;
            // case VAPMessage.VAP_RECOVER_REQUEST:
            // VAPRecoverRequestHandler.handleMessage(session, (VAPRecoverRequestMessage)message);
            // break;
            case VAPMessage.VAP_RELEASE:
                VAPReleaseHandler.handleMessage(session, (VAPReleaseMessage) message);
                break;
        }
    }
}
