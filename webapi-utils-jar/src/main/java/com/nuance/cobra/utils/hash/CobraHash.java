package com.nuance.cobra.utils.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CobraHash
{
    private CobraHash()
    {
        //hide utility class constructor.
    }

    public static String sha256(String stringToHash)
    {
        return digest("SHA-256", stringToHash);
    }

    public static String digest(String algorithm, String stringToHash)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
