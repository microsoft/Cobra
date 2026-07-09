/* 
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2015 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: jfgosse
 * Date  : Mar 30, 2015
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.storage.utils;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

public final class VersionConverter {

  public static BigInteger fromVersionStringToBigInteger(final String version) {
    return StringUtils.isBlank(version) ? null : new BigInteger(version);
  }

}
