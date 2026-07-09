package com.nuance.cobra.utils.encryption;

import java.security.SecureRandom;

public class Utils
{
    private Utils() { throw new IllegalStateException("Utility class!"); }

    private static SecureRandom secureRandom;

    public static SecureRandom getSecureRandom()
    {
        if (secureRandom == null)
        {
            secureRandom = new SecureRandom();
        }
        return secureRandom;
    }

    public static int getRandomIndex(int size)
    {
        SecureRandom secureRandom = getSecureRandom();
        return secureRandom.nextInt(size);
    }

    public static String getRandomString(int length)
    {
        //Write it this way to please Veracode
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom secureRandom = getSecureRandom();
        return secureRandom.ints(length, 0, chars.length()).mapToObj(chars::charAt)
                           .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

}
