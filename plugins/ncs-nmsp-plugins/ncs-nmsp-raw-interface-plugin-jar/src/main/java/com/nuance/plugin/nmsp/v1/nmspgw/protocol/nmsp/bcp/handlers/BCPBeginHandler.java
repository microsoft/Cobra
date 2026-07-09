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

import com.google.common.base.Strings;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.CommandBeginKeys;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BCPBeginHandler {
    private static final Logger logger = LogManager.getLogger();

    public static void handleMessage(final Session session, final BCPBeginMessage message) {
        session.setBcpVersion(message.getVersion());

        if (!Strings.isNullOrEmpty(message.getQueryBeginUserId())) {
            session.setUserId(message.getQueryBeginUserId()); // Use the Query Begin user id only if the session doesn't already have one
            session.setDeviceId(message.getQueryBeginUserId());
        }

        String subscriberId = (String)message.getQueryBeginInfo().get("subscriberId");
        if (!Strings.isNullOrEmpty(subscriberId)) {
            session.setSubscriberId(subscriberId);
        }

        String phoneModel = (String)message.getQueryBeginInfo().get("phone_model");
        if (!Strings.isNullOrEmpty(phoneModel)) {
            session.setPhoneModel(phoneModel);
        }

        String deviceModel = (String)message.getQueryBeginInfo().get("deviceModel");
        if (!Strings.isNullOrEmpty(deviceModel)) {
            session.setDeviceModel(deviceModel);
        }

        String commandName = (String) message.getQueryBeginInfo().get(CommandBeginKeys.COMMAND.getValue());
        session.setCommandName(commandName);

        String dictationLanguage = (String) message.getQueryBeginInfo().get("dictation_language");
        if (!Strings.isNullOrEmpty(dictationLanguage)) {
            session.setDictationLanguage(dictationLanguage);
        }
        session.putBcpMessagePdxDicts(message.getQueryBeginInfo());

        session.getWebAPIOrchestrator().onBCPBegin(session, message);
    }

    public static Version getVersion() {
        return BCPDefaultParams.VERSION;
    }
}
