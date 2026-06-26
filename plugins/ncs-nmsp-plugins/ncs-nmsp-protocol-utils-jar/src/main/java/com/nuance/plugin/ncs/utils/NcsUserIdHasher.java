package com.nuance.plugin.ncs.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.util.Base64;

public class NcsUserIdHasher
{
    public static void main(String[] args)
            throws IOException
    {
        final Logger logger = LogManager.getLogger(NcsUserIdHasher.class);
        Configurator.setLevel(LogManager.getLogger(NcsUserIdHasher.class).getName(), Level.ALL);

        final byte[] salt = {(byte)0xe4, (byte)0x36, (byte)0x18, (byte)0xcc, (byte)0x4a, (byte)0xdc, (byte)0x3b, (byte)0x93, (byte)0xbb, (byte)0xe1, (byte)0x8a, (byte)0x03, (byte)0x4d, (byte)0x2e, (byte)0x53, (byte)0xf6, (byte)0x06, (byte)0xe7, (byte)0xd0, (byte)0xc2};

        boolean doubleHash = true;
        String toHash;

        if(args.length < 2)
        {
            logger.error("Usage: $cmd <string to hash> single|double");
            return;
        }

        if(args[args.length-1].equals("single"))
        {
            doubleHash = false;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < args.length - 1; i++)
        {
            stringBuilder.append(args[i]);
        }
        toHash = stringBuilder.toString();

        String rc = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, Base64.getEncoder().encodeToString(salt), toHash, doubleHash);

        String times = doubleHash ? "double" : "single";

        logger.info("\"{}\" NCS {} hashes to: {}", toHash, times, rc);
    }
}
