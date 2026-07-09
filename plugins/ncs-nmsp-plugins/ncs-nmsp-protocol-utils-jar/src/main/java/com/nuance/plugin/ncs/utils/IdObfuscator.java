package com.nuance.plugin.ncs.utils;

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
 * Author: epigeon
 * Date  : Apr 29, 2015
 *
 * ---------------------------------------------------------------------------
 */

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class IdObfuscator {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final HashFunction hashFunction;

    public IdObfuscator(byte[] salt)
    {
        SecretKeySpec signingKey = new SecretKeySpec(salt, HMAC_SHA1_ALGORITHM);
        hashFunction = Hashing.hmacSha1(signingKey);
    }

    /**
     * Obfuscate the id and transform it into a UUID.
     * This method is thread safe at the cost of creating a new HMAC at every call.
     *
     * @throws NoSuchAlgorithmException If HmacSHA1 isn't available
     * @throws InvalidKeyException If the key isn't valid
     */
    public static UUID obfuscate(byte[] salt, final String id)
    {
        return new IdObfuscator(salt).obfuscateId(id);
    }

    /**
     * Obfuscate the id and transform it into a UUID.
     * This method is not thread safe, because it reuses the same HMAC for every call for performance reason.
     */
    public synchronized UUID obfuscateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        String newId;
        newId = java.net.URLEncoder.encode(id, StandardCharsets.UTF_8).replace("%", ""); // See GW-572

        HashCode hc = hashFunction.hashBytes(newId.getBytes());
        final byte[] rawHmac = hc.asBytes();

        final String intermediateId = bytesToHexString(rawHmac);

        return convertToUUID(intermediateId);
    }

    //
    // Private methods
    //
    private static String bytesToHexString(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++, j+=2) {
            out[j] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j+1] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    private static UUID convertToUUID(final String id) {
        return UUID.nameUUIDFromBytes(id.getBytes());
    }
}