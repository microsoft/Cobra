package com.nuance.cobra.api.globalregistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value Object for global registry "Grammar' element
 */
public class Grammar
{
    private String currentVersion;
    private Map<String, String> versionURLMap;

    public String getCurrentVersion()
    {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion)
    {
        this.currentVersion = currentVersion;
    }

    public Map<String, String> getVersionURLMap()
    {
        if (versionURLMap == null)
        {
            return new ConcurrentHashMap<String, String>();
        }
        return versionURLMap;
    }

    public void setVersionURLMap(Map<String, String> versionURLMap)
    {
        this.versionURLMap = versionURLMap;
    }

    public void addVersionURL(String version, String url)
    {
        if (null == versionURLMap)
        {
            versionURLMap = new ConcurrentHashMap<String, String>();
        }
        versionURLMap.put(version, url);
    }

    public String getVersionURL(String key)
    {
        if (null == versionURLMap)
        {
            return null;
        }
        return versionURLMap.get(key);
    }
}