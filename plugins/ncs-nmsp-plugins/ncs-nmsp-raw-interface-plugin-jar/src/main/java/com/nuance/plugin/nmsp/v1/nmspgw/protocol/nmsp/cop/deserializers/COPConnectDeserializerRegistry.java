/*
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2013 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: patrick_filion
 * Date  : 2013-10-30
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.COPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;

import java.util.Hashtable;

public class COPConnectDeserializerRegistry {
    private static Hashtable<Version, ICOPConnectDeserializer> copConnectDeserializerResources = new Hashtable<Version, ICOPConnectDeserializer>();

    public static ICOPConnectDeserializer getCOPMessageDeserializer(short version) {
        if (!copConnectDeserializerResources.containsKey(new Version(version))) {
            return copConnectDeserializerResources.get(COPDefaultParams.VERSION);
        }
        return copConnectDeserializerResources.get(new Version(version));
    }

    public static void findCOPConnectDeserializerResource() {
        COPConnectDeserializer copDeserializerResource = new COPConnectDeserializer();
        COPConnectDeserializerRegistry.copConnectDeserializerResources.put(copDeserializerResource.getVersion(), copDeserializerResource);
    }
}

