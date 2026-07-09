package com.nuance.ncs.nmsp.util;

public class EnvUtil
{
    private EnvUtil() {
        //utility class.
    }

    public static String getOrDefault(String key, String defaultValue)
    {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    @SuppressWarnings("java:S1172")
    public static String useDefault(String key, String defaultValue)
    {
        return defaultValue;
    }
}
