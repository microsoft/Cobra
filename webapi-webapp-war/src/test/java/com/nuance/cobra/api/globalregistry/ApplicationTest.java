package com.nuance.cobra.api.globalregistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ApplicationTest {
    private Map<String, ApplicationVersion> data;
    private Application application;

    @Test
    public void setApplicationVersion(){
        application = new Application();
        data = new HashMap<String, ApplicationVersion>();
        data.put("Test", new ApplicationVersion());
        application.setApplicationVersions(data);

        application.addApplicationVersion("Test 1", new ApplicationVersion());

        assert(application.getApplicationVersion("Test") instanceof ApplicationVersion);

        application.setApplicationVersions(null);

        Assert.assertNull(application.getApplicationVersion("test"));
        Assert.assertNull(application.getApplicationVersions().get("test"));

        application.addApplicationVersion("Test 1", new ApplicationVersion());

        assert(application.getApplicationVersion("Test 1") instanceof ApplicationVersion);

        assert(application.getApplicationVersions().get("Test 1") instanceof ApplicationVersion);
    }
}
