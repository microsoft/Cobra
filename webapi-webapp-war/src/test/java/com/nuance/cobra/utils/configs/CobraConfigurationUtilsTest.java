package com.nuance.cobra.utils.configs;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.config.ReverseOverrideCombiner;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class})
@PowerMockIgnore({"sun.net.*", "com.sun.*", "javax.xml.*", "org.apache.xerces.*", "org.w3c.dom.*", "org.xml.sax.*", "javax.net.ssl.*", "javax.management.*"})
public class CobraConfigurationUtilsTest
{
    final static LoggerContext loggingContext = (LoggerContext) LogManager.getContext(false);
    final static Configuration loggingConfig = loggingContext.getConfiguration();

    @Before
    public void setUp() {
        LoggerConfig loggerConfig = loggingConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.ALL);
    }

    @Test(expected = IllegalStateException.class)
    public void constructorTest()
    {
        new CobraConfigurationUtils();
    }

    @Test
    public void lookupTenantConfigAndAddParams()
            throws CobraException
    {
        CombinedConfiguration defaults = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

        HashMap<String, Object> params = new HashMap<>();
        params.put("testKey", "testValue");

        CombinedConfiguration cc = CobraConfigurationUtils.lookupTenantConfigAndAddParams("test**Tenant", params);
        assertEquals("testValue", cc.getString("testKey"));
        assertEquals(WebApiConfig.Property.MIX_AUTH_URI.defaultValue, cc.getString(WebApiConfig.Property.MIX_AUTH_URI.key));



        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(any())).thenReturn(defaults);

        cc = CobraConfigurationUtils.lookupTenantConfigAndAddParams("testTenant", params);

        assertEquals("testValue", cc.getString("testKey"));
        assertEquals(WebApiConfig.Property.MIX_AUTH_URI.defaultValue, cc.getString(WebApiConfig.Property.MIX_AUTH_URI.key));
    }

    @Test
    public void setPropertyTest()
    {
        CombinedConfiguration empty = new CombinedConfiguration(new ReverseOverrideCombiner());
        CobraConfigurationUtils.setProperty(empty, "nullKey", "test");
        String value = empty.getString("nullKey");
        assertNull(value);
    }
}