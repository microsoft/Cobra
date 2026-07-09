package com.nuance.cobra.utils;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class WebAPIRateLimiterTest {
    private WebAPIRateLimiter testLimiter;

    @Before
    public void setUp(){
        testLimiter = new WebAPIRateLimiter(1);
    }

    @Test
    public void releaseTest(){
        assertNotNull(testLimiter);

        testLimiter.release("testTenant");
        testLimiter.release(1);
    }

    @Test
    public void checkIfRateLimitExceededTest() throws CobraException {
        try {
            testLimiter.checkIfRateLimitExceeded("testTenant", 0);
            fail();
        }catch (CobraException e) {}
    }

    @Test
    public void garbageCollectTest(){
        HashMap<String, Session> testMap = new HashMap<>();
        CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration testConfig = testConfigs.getConfiguration(0);
        testConfig.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "testKey");
        Session testSession = new Session("testId", testConfigs);
        assertNotNull(testSession);
        assertNotNull(testMap);

        testMap.put("firstSession", testSession);
        assertEquals(testSession, testMap.get("firstSession"));

        testLimiter.garbageCollect(testMap);
    }


}