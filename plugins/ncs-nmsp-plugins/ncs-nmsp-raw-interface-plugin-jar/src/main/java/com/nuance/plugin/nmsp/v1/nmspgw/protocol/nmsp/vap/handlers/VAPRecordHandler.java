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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPRecordMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 */
public class VAPRecordHandler {
    private VAPRecordHandler() {
    }

    public static void handleMessage(Session session, VAPRecordMessage message) {
        session.getWebAPIOrchestrator().setAudioId(message.getAudioId());

        PDXDictionary audioPacketInfo = new PDXDictionary();
        audioPacketInfo.addInteger("bytes", message.getPackageSize());
        session.getWebAPIOrchestrator().setAudioPacketInfo(audioPacketInfo);

        for (byte[] audio: message.getAudio()) {
            session.getWebAPIOrchestrator().audioBytes(audio);
        }
    }
}
