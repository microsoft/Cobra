package com.nuance.cobra.api.globalregistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value Object for global registry "Company' element
 */
public class Company
{
    private Map<String, Application> applications;

    public Map<String, Application> getApplications()
    {
        if (applications == null)
        {
            return new ConcurrentHashMap<String, Application>();
        }
        return applications;
    }

    public void setApplications(Map<String, Application> applications)
    {
        this.applications = applications;
    }

    public void addApplication(String key, Application application)
    {
        if (applications == null)
        {
            applications = new ConcurrentHashMap<String, Application>();
        }
        applications.put(key, application);
    }

    public Application getApplication(String key)
    {
        if (null == applications)
        {
            return null;
        }
        return applications.get(key);
    }

}
