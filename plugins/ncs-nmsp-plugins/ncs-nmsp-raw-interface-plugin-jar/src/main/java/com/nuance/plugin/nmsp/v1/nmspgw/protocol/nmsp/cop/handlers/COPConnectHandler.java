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

import com.google.common.base.Strings;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioContainerType;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioGenerationPolicy;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioHandlingSpec;
import com.nuance.plugin.nmsp.v1.nmspgw.SameCodecMGWPolicy;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Vector;

/**
 * @author epigeon
 */
public class COPConnectHandler {
    private static final Logger logger = LogManager.getLogger();

    private COPConnectHandler() {
    }

    public static void handleMessage(Session session, COPConnectMessage message) {
        // Store connect info in the session
        session.setBufferSize(message.getBufferSize());
        session.setBuildNumber(message.getBuildNumber());
        session.setDeviceType(message.getDeviceType());
        session.setFromCodec(message.getToCodec()); // Session has from device codec, COPCONNECT has to server codec
        session.setNmaid(message.getNmaid());
        session.setToCodec(message.getFromCodec()); // Session has to device codec, COPCONNECT has from server codec
        session.setParent(message.getParent());
        session.setConnectToMds(message.isConnectToMds());
        if (!Strings.isNullOrEmpty(message.getUserId())) {
            session.setUserId(message.getUserId());
        }
        session.setDeviceId(message.getDeviceId());
        session.setCopVersion(message.getVersion());
        Vector<Parameter> parameters = message.getParameters();

        AudioContainerType audioContainerType = message.getAudioContainerType();
        if (audioContainerType != null) {
            if (audioContainerType == AudioContainerType.ogg) {
                final AudioHandlingSpec outAudioHandlingSpec = new AudioHandlingSpec(AudioGenerationPolicy.preferMGW, SameCodecMGWPolicy.skipGW, AudioContainerType.ogg);
                session.setOutAudioHandlingSpec(outAudioHandlingSpec);
                session.setOggContainer(true);
            }
        }

        for (Parameter param : parameters) {
            switch (param.getType()) {
                case APP:
                    break;
                case NMSP:
                    if (param.getName().equalsIgnoreCase(Session.PING_INTERVAL_SECS_STRING)) {
                        session.setPingIntervalSecs(Integer.parseInt(param.getValue()));
                    } else if (param.getName().equalsIgnoreCase(Session.PING_MISSED_RESPONSES_BEFORE_DISCONNECT)) {
                        session.setMissedPingResponsesBeforeDisconnect(Integer.parseInt(param.getValue()));
                    } else if (param.getName().equalsIgnoreCase(Session.SESSION_TIMEOUT_SECS_PARAM_NAME)) {
                        session.setSessionTimeoutSecs(Long.parseLong(param.getValue()));
                    }
                    break;
                case NSS:
                    session.getResourceParameters().add(param);
                    break;
            }
        }

        session.getClient().connect(session);
    }
}
