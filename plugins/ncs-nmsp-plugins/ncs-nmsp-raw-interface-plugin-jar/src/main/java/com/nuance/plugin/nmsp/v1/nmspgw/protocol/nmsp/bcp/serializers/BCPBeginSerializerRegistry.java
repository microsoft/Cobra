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
 * Date  : 2013-11-04
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.BCPDefaultParams;
import com.nuance.plugin.nmsp.v1.nmspgw.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;

public class BCPBeginSerializerRegistry {
  private static Logger logger = LogManager.getLogger();
  private static Hashtable<Version, IBCPBeginSerializer> bcpBeginSerializerResources = new Hashtable<Version, IBCPBeginSerializer>();

  public static IBCPBeginSerializer getBCPBeginSerializer(short version) {
    Version versionObj = new Version(version);
    if(!bcpBeginSerializerResources.containsKey(versionObj)) {
      return bcpBeginSerializerResources.get(BCPDefaultParams.VERSION);
    }
    return bcpBeginSerializerResources.get(versionObj);
  }
  
}
