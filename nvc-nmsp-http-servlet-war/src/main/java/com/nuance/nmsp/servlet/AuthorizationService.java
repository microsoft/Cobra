package com.nuance.nmsp.servlet;

import com.nuance.nmsp.servlet.util.EnvUtil;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import java.util.regex.Pattern;

public class AuthorizationService
{

    private static final Logger log = ESAPI.getLogger(AuthorizationService.class);

    private final String whitelist = EnvUtil.useDefault("WHITELIST", ".*");

    public AuthorizationInfo authorize(String appId, String appKeyString)
            throws IllegalAccessException
    {
        log.info(Logger.EVENT_UNSPECIFIED, "Authorization [" + Thread.currentThread()
                                           .getName() + "] appId:" + appId + " appKeyString:" + appKeyString);

        validateAppIdAppKeyString(appId, appKeyString);

        if (whitelist != null)
        {
            for (String pattern : whitelist.split(","))
            {
                if (Pattern.matches(pattern, appId))
                {
                    return new AuthorizationInfo(appId, appKeyString);
                }
            }
        }
        throw new IllegalAccessException(
                "This application ID is not authorized to use this service. Please contact customer support.");
    }

    protected void validateAppIdAppKeyString(String appId, String appKeyString)
            throws IllegalAccessException
    {
        if (appId == null || appKeyString == null || appId.length() == 0 || (appKeyString.length() % 2 != 0))
        {
            throw new IllegalAccessException("Unauthorized, invalid application id or key.");
        }
    }

}