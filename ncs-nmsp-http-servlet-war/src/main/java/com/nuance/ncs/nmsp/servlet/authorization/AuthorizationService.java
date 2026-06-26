package com.nuance.ncs.nmsp.servlet.authorization;

import java.util.regex.Pattern;

public class AuthorizationService
{
    AuthorizationService()
    {
        throw new IllegalStateException("Utility class!");
    }

    public static boolean authorizeAppIdAndKey(String whitelist, String appId, byte[] appKey)
    {
		if (!validateAppIdAppKey(appId, appKey))
		{
			return false;
		}

        if (whitelist != null)
        {
            for (String pattern : whitelist.split(","))
            {
                if (Pattern.matches(pattern, appId))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean authorizeGwIp(String whitelist, String ipAddr)
    {
		if (ipAddr == null)
		{
			return false;
		}

        if (whitelist != null)
        {
            for (String pattern : whitelist.split(","))
            {
                if (Pattern.matches(pattern, ipAddr))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean authorizeCrossOrigin(String whitelist, String origin)
    {
		if (origin == null)
		{
			return false;
		}

        if (whitelist != null)
        {
            for (String pattern : whitelist.split(","))
            {
                if (Pattern.matches(pattern, origin))
                {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean validateAppIdAppKey(String appId, byte[] appKey)
    {
        return appId != null && appKey != null && appId.length() != 0 && (appKey.length % 2 == 0);
    }
}