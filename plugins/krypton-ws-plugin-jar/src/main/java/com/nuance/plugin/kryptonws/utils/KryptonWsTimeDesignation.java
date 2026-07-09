package com.nuance.plugin.kryptonws.utils;

import org.apache.commons.lang3.StringUtils;

public class KryptonWsTimeDesignation
{
    private KryptonWsTimeDesignation() { throw new IllegalStateException("Utility class!"); }

    public static int getSecondsFromTimeDesignation(String timeDesignation)
    {
        String noSuffix;
        if(StringUtils.endsWith(timeDesignation, "ms"))
        {
            noSuffix = StringUtils.stripEnd(timeDesignation, "ms");
            int milliseconds = Integer.parseInt(noSuffix);
            return Math.round(milliseconds / 1000.0f);
        }

        if(StringUtils.endsWith(timeDesignation, "s"))
        {
            noSuffix = StringUtils.stripEnd(timeDesignation, "s");
            return Integer.parseInt(noSuffix);
        }
        return -1;
    }

    public static int getMillisecondsFromTimeDesignation(String timeDesignation)
    {
        String noSuffix;
        if(StringUtils.endsWith(timeDesignation, "ms"))
        {
            noSuffix = StringUtils.stripEnd(timeDesignation, "ms");
            return Integer.parseInt(noSuffix);
        }

        if(StringUtils.endsWith(timeDesignation, "s"))
        {
            noSuffix = StringUtils.stripEnd(timeDesignation, "s");
            return Integer.parseInt(noSuffix) * 1000;
        }
        return -1;
    }
}
