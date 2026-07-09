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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordEndMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class VAPRecordEndHandler {
    private static final Logger logger = LogManager.getLogger();

    private VAPRecordEndHandler() {
    }

    public static void handleMessage(Session session, VAPRecordEndMessage message) {
        long audioId = message.getAudioId();

        session.stopVapRecordTimer();
        session.getWebAPIOrchestrator().onRecordEnd(audioId);
    }

}
