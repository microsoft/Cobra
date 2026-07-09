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

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.LoadResourceData;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadResourceMessage;
import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 */
public class BCPLoadResourceHandler {

    private BCPLoadResourceHandler() {
    }

    public static void handleMessage(Session session, BCPLoadResourceMessage message, ISessionListener sessionListener) {
//    session.setBcpVersion(message.getVersion());

        LoadResourceData loadResourceData = new LoadResourceData();
        loadResourceData.setUserName(message.getUserName());
        loadResourceData.setType(message.getResourceType());
        loadResourceData.setParameters(message.getParameters());
        if (message.getCodec() != Codec.UNKNOWN) {
            loadResourceData.setCodec(message.getCodec());
        } else { // If not present in the load resource message, use the session's codec
            loadResourceData.setCodec(session.getFromCodec());
        }
        loadResourceData.setDeferNSSLoading(message.getDeferNSSLoading());
        loadResourceData.setDictationCapabilities(message.getDictationCapabilities());
        loadResourceData.setRecoCapabilities(message.getRecoCapabilities());
        loadResourceData.setTtsCapabilities(message.getTtsCapabilities());
        loadResourceData.setClientAudioStreamingMaxIdleTimeMSec(message.getClientAudioStreamingMaxIdleTimeMSec());
    }

}
