package com.nuance.cobra.api.globalregistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value Object for global registry "Application' element
 */
public class Application
{
    private Map<String, ApplicationVersion> applicationVersions;

    public Map<String, ApplicationVersion> getApplicationVersions()
    {
        if (applicationVersions == null)
        {
            return new ConcurrentHashMap<String, ApplicationVersion>();
        }
        return applicationVersions;
    }

    public void setApplicationVersions(Map<String, ApplicationVersion> appVersions)
    {
        this.applicationVersions = appVersions;
    }

    public void addApplicationVersion(String key, ApplicationVersion appVersion)
    {
        if (applicationVersions == null)
        {
            applicationVersions = new ConcurrentHashMap<String, ApplicationVersion>();
        }
        applicationVersions.put(key, appVersion);
    }

    public ApplicationVersion getApplicationVersion(String key)
    {
        if (null == applicationVersions)
        {
            return null;
        }
        return applicationVersions.get(key);
    }
}