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
 * Date  : 2013-10-31
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;

import java.util.Hashtable;

public class BCPBeginDeserializerRegistry {
    private static Hashtable<Version, IBCPBeginDeserializer> bcpBeginDeserializerResources = new Hashtable<>();

    public static void findBCPBeginDeserializerResource() {
        BCPBeginDeserializer deserializer = new BCPBeginDeserializer();
        BCPBeginDeserializerRegistry.bcpBeginDeserializerResources.put(deserializer.getVersion(), deserializer);
    }

    public static IBCPBeginDeserializer getBCPBeginDeserializer(short version) {
        Version versionObj = new Version(version);
        if (!bcpBeginDeserializerResources.containsKey(versionObj)) {
            return bcpBeginDeserializerResources.get(BCPDefaultParams.VERSION);
        }
        return bcpBeginDeserializerResources.get(versionObj);
    }

}

