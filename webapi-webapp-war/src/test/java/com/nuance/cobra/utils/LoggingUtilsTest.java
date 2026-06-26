package com.nuance.cobra.utils;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoggingUtilsTest
{
    CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

    @Test(expected = IllegalStateException.class)
    public void constructor()
    {
        new LoggingUtils();
    }

    @Test
    public void getStringPropertyValueForLogging()
    {
        Session session = new Session("123", cc);
        String rc = LoggingUtils.getStringPropertyValueForLogging(session, WebApiConfig.Property.EDF_ENVIRONMENT);
        assertEquals(WebApiConfig.Property.EDF_ENVIRONMENT.defaultValue, rc);
    }
}