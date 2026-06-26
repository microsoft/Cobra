package com.nuance.cobra.test;


import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.testing.LoggingTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.mock;

@PrepareForTest({WebApiConfigManager.class, HttpClientBuilder.class})
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*","javax.security.*","java.util.regex.Pattern"})
public abstract class CobraTest extends LoggingTest
{

    private static final String SESSION_MANAGER_HOLDER_CLASSNAME =
            "com.nuance.cobra.session.SessionManager$SessionManagerHolder";


    private Class sessionManagerHolderClass;
    private SessionManager originalSessionManager;
    protected SessionManager mockSessionManager;


    @Mock
    protected CombinedConfiguration mockServerConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    protected Configuration mockServerConfig = CobraConfigurationUtils.getHighestConfig(mockServerConfigs);

    public void setUp() throws Exception
    {
        mockServerConfig.setProperty(WebApiConfig.Property.SECURITY_LOGGER_LOG_LEVEL.key, 15);
        mockServerConfig.setProperty(WebApiConfig.Property.SECURITY_LOGGER_LOG_LEVEL_NAME.key, "Security");
        mockServerConfig.setProperty(WebApiConfig.Property.SESSION_CLEAN_UP_SLEEP_TIME_MILLISECONDS.key,60000);
        mockServerConfig.setProperty(WebApiConfig.Property.SESSION_CLEAN_UP_SLEEP_TIME_MILLISECONDS.key,60000);
        mockServerConfig.setProperty(WebApiConfig.Property.EDF_BROKER_URI.key, WebApiConfig.Property.EDF_BROKER_URI.defaultValue);
        mockServerConfig.setProperty(WebApiConfig.Property.EDF_BROKER_CONNECT_URL.key,WebApiConfig.Property.EDF_BROKER_CONNECT_URL.defaultValue);
        mockServerConfig.setProperty(WebApiConfig.Property.EDF_BROKER_MEMORY_LIMIT_MB.key,WebApiConfig.Property.EDF_BROKER_MEMORY_LIMIT_MB.defaultValue);
        mockServerConfig.setProperty(WebApiConfig.Property.EDF_BROKER_STORAGE_LIMIT_GB.key,WebApiConfig.Property.EDF_BROKER_STORAGE_LIMIT_GB.defaultValue);
        mockServerConfig.setProperty(WebApiConfig.Property.EDF_BROKER_TEMP_STORAGE_LIMIT_GB.key,WebApiConfig.Property.EDF_BROKER_TEMP_STORAGE_LIMIT_GB.defaultValue);
        mockServerConfig.setProperty(WebApiConfig.Property.ENABLE_EDF_LOG_POSTER.key,true);
        Whitebox.setInternalState(WebApiConfigManager.class, "webApiConfig", mockServerConfigs);

        sessionManagerHolderClass = Class.forName(SESSION_MANAGER_HOLDER_CLASSNAME);

        mockSessionManager = mock(SessionManager.class);
        originalSessionManager = TestUtils.inject(sessionManagerHolderClass, "instance", mockSessionManager);

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(mockServerConfigs);
    }


    public void tearDown() throws Exception
    {
        TestUtils.inject(sessionManagerHolderClass, "instance", originalSessionManager);
    }

}
