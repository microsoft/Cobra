package com.nuance.cobra.api.globalregistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CompanyTest {

    @Test
    public void setcompany(){
        Company company = new Company();
        Map<String, Application> data = new HashMap<String, Application>();
        data.put("Test", new Application());
        company.setApplications(data);

        company.addApplication("Test 1", new Application());

        assert(company.getApplication("Test") instanceof Application);

        company.setApplications(null);

        Assert.assertNull(company.getApplication("test"));
        Assert.assertNull(company.getApplications().get("test"));

        company.addApplication("Test 1", new Application());

        assert(company.getApplication("Test 1") instanceof Application);

        assert(company.getApplications().get("Test 1") instanceof Application);
    }
}
