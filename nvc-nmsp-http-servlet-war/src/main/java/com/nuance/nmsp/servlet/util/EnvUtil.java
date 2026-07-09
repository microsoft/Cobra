package com.nuance.nmsp.servlet.util;

public class EnvUtil
{
    EnvUtil() { throw new IllegalStateException("Utility class!"); }

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
