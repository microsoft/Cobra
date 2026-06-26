package com.nuance.plugin.ncs.utils;

import com.nuance.cobra.utils.hash.CobraHash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Base64;

public class StringObfuscator {

    private static final Logger logger = LogManager.getLogger();

    private StringObfuscator() {}

    public static String getObfuscatedString(HashType hashType, String salt, String value) throws IOException {
        return getObfuscatedString(hashType, salt, value, false);
    }

    public static String getObfuscatedString(HashType hashType, String salt, String value, boolean doubleHash) throws IOException {
        if(value == null) {
            return value;
        }
        switch(hashType) {
            case HASH_TYPE_NONE:
                return value;
            case HASH_TYPE_NCS:
                if(salt == null || salt.isBlank()) {
                    logger.error("NCS hash missing secret");
                    return null;
                }
                return hashString(Base64.getDecoder().decode(salt), value, doubleHash);
            case HASH_TYPE_SHA256:
                return CobraHash.sha256(value);
        }
        logger.error("Unsupported hash type: {}", hashType);
        return value;
    }

    protected static String hashString(byte[] salt, String value, boolean doubleHash) throws IOException {
        try {
            /*
              Yes it is weird, but ncs-ws needs to hash it twice.

              From Peter Freshman.
              User, Device, and Subscriber ID’s get hashed in two locations in NCS – the Web Socket Proxy and the NCS Gateway (I don’t recall if HTTP Servlets do any hashing). Both components use the same hashing algorithm.

              What this means is:
              - Clients using NMSP/PDX get their ID’s hashed 1x
              - Clients using WebSocket API get their ID’s hashed 2x
             */
            String firstHash = IdObfuscator.obfuscate(salt, value).toString();
            if(doubleHash) {
                return IdObfuscator.obfuscate(salt, firstHash).toString();
            }
            else {
                return firstHash;
            }
        } catch (Exception e) {
            logger.error("Couldn't produce id hash.");
        }
        throw new IOException("Couldn't produce id hash.");
    }

}
