package com.nuance.cobra.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.Tools;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.plugin.v1.IWebApiCommandPlugin;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import com.nuance.plugin.v1.shareable.edf.IEdfMessage;
import com.nuance.plugin.v1.shareable.edf.IEdfV2Message;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*", "jdk.xml.*", "java.xml.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "javax.management.*", "org.w3c.dom.*"})
@PrepareForTest({WebApiConfigManager.class, PluginServiceLoader.class, Tools.class})
public class PluginLoaderTest {

    private final String tenantName = "test";
    private final String testFolder = "./target/";
    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private WebApiJSONConfiguration testConfig = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(testConfigs);

    static final String LOG_ID = "465435321";
    static final String API_NAME = "the-api";
    static final String LOG_CUSTOMER = "789465";
    static final String LOG_SESSION_ID = "059dd2e1-a1db-43f7-a2ef-de06d4146530";


    PluginServiceLoader<IWebApiCommandPlugin> mockServiceLoader;

    @Before
    public void initTest() throws CobraException {
        mockStatic(WebApiConfigManager.class);
        mockStatic(Tools.class);

        testConfig.setProperty(WebApiConfig.Property.TENANT_VALIDATION_REGEX.key, "*");
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(tenantName)).thenReturn(testConfigs);
        PowerMockito.when(WebApiConfigManager.getConfigForTenant(tenantName)).thenReturn(testConfig);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);
    }


    @Test
    public void loadCommandPlugins() throws IOException {
        mockStatic(PluginServiceLoader.class);

        //service loader mock
        mockServiceLoader = PowerMockito.mock(PluginServiceLoader.class);

        PowerMockito.when(PluginServiceLoader.load(ArgumentMatchers.eq(IWebApiCommandPlugin.class), ArgumentMatchers.any(ClassLoader.class))).thenReturn(mockServiceLoader);

        mockProviders(new IWebApiCommandPlugin() {
            @Override
            public List<String> getProvidedCommands() {
                return null;
            }

            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {
                System.out.println("test");
            }
        });
        //done with service loader

        PowerMockito.when(Tools.getJettyPath()).thenReturn(testFolder);
        new File(testFolder + "/plugins").mkdirs();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(testFolder + "/plugins/testPlugin2.jar"), StandardCharsets.UTF_8))) {
            writer.write("something");
        }

        List<HashMap<String, Object>> tenantPlugins = new ArrayList<>();
        HashMap<String, Object> plugin1 = new LinkedHashMap<>();
        plugin1.put("jarName", "testPlugin1");
        HashMap<String, Object> plugin1Configs = new LinkedHashMap<>();
        plugin1.put("settings", plugin1Configs);
        tenantPlugins.add(plugin1);

        plugin1 = new LinkedHashMap<>();
        plugin1.put("jarName", "testPlugin2.jar");
        plugin1Configs = new LinkedHashMap<>();
        plugin1Configs.put("testConfig", "testValue");
        plugin1.put("settings", plugin1Configs);
        tenantPlugins.add(plugin1);

        testConfig.setProperty(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key, tenantPlugins);

        List<WebApiPlugin<IWebApiCommandPlugin>> result = PluginLoader.loadCommandPlugins(tenantName);
        assertTrue(result.isEmpty());
    }

    protected void mockProviders(final IWebApiCommandPlugin... providers) {
        PowerMockito.when(mockServiceLoader.iterator()).thenAnswer(new Answer<Iterator<IWebApiCommandPlugin>>() {

            @Override
            public Iterator<IWebApiCommandPlugin> answer(InvocationOnMock invocation) throws Throwable {
                return Arrays.asList(providers).iterator();
            }
        });
    }

    @Test
    public void loadRawPlugins() throws IOException {
        List<WebApiPlugin<IWebApiRawPlugin>> result = PluginLoader.loadRawPlugins(tenantName);
        assertTrue(result.isEmpty());
    }

    @Test
    public void buildPluginConfigs() throws CobraException {
        String pluginName = "testPlugin";

        List<HashMap<String, Object>> tenantPlugins = new ArrayList<>();
        HashMap<String, Object> plugin1 = new LinkedHashMap<>();
        plugin1.put("jarName", "notFound");
        HashMap<String, Object> plugin1Configs = new LinkedHashMap<>();
        plugin1.put("settings", plugin1Configs);
        tenantPlugins.add(plugin1);

        plugin1 = new LinkedHashMap<>();
        plugin1.put("jarName", pluginName);
        plugin1Configs = new LinkedHashMap<>();
        plugin1Configs.put("testConfig", "testValue");
        plugin1.put("settings", plugin1Configs);
        tenantPlugins.add(plugin1);

        testConfig.setProperty(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key, tenantPlugins);
        CombinedConfiguration conf = PluginLoader.buildPluginConfigs(tenantName, pluginName);
        assertNotNull(conf);
        assertEquals("testValue", conf.getString("testConfig"));
    }

    @Test
    public void buildPluginConfigsEmpty() throws CobraException {
        CombinedConfiguration conf = PluginLoader.buildPluginConfigs(tenantName, "testPlugin");
        assertNotNull(conf);
    }

    @Test(expected = IOException.class)
    public void buildWebApiFeaturesTestException()
            throws CobraException, IOException
    {
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(tenantName)).thenThrow(new CobraException(
                ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, tenantName)));
        IWebApiFeatures features = PluginLoader.buildWebApiFeatures(tenantName, "pluginName");
        assertNotNull(features);
        CombinedConfiguration cc = features.getConfigs(tenantName);
    }

    @Test
    public void buildWebApiFeaturesTest() throws IOException {

        IWebApiFeatures features = PluginLoader.buildWebApiFeatures(tenantName, "pluginName");
        assertNotNull(features);

        List<HashMap<String, Object>> tenantPlugins = new ArrayList<>();
        testConfig.setProperty(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key, tenantPlugins);
        testConfig.setProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, "true");

        CombinedConfiguration tenantConfigs = features.getConfigs();
        assertNotNull(tenantConfigs);
        tenantConfigs = features.getConfigs();
        assertNotNull(tenantConfigs);
    }

    @Test
    public void buildLogposterFeaturesTest() throws IOException {
        IWebApiFeatures features = PluginLoader.buildWebApiFeatures(tenantName, "pluginName");
        assertNotNull(features);

        List<HashMap<String, Object>> tenantPlugins = new ArrayList<>();
        testConfig.setProperty(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key, tenantPlugins);
        testConfig.setProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, "true");
        CombinedConfiguration tenantConfigs = features.getConfigs();

        IEdfLogPoster logPoster = features.getEdfPoster(tenantConfigs);

        IEdfMessage messageV1 = createPluginV1Message();
        logPoster.publishEdfLog(messageV1);

        PluginEdfV1MessageQueueable msgV1Queueable = new PluginEdfV1MessageQueueable(messageV1, tenantName, testConfigs);
        String json = msgV1Queueable.toJson(false);
        assertTrue(json.contains(LOG_ID));
        assertTrue(json.contains(API_NAME));

        IEdfV2Message messageV2 = createPluginV2Message();
        logPoster.publishEdfLog(messageV2);

        PluginEdfV2MessageQueueable msgV2Queueable = new PluginEdfV2MessageQueueable(messageV2, tenantName, testConfigs);
        json = msgV2Queueable.toJson(true);
        assertEquals(EdfMessageQueueable.EdfMessageFormat.EDF_V2_FORMAT, msgV2Queueable.getEdfMessageFormat());
        assertEquals(EdfMessageQueueable.JmsMessageType.EDF_LOG, msgV2Queueable.getJmsType());
        assertEquals("edfLog.v2.pluginLog.", msgV2Queueable.getEdfLogTag());

        assertTrue(json.contains(LOG_CUSTOMER));
        assertTrue(json.contains(LOG_SESSION_ID));
        features.close();
    }


    private IEdfMessage createPluginV1Message()
    {
        return new IEdfMessage()
        {
            @Override
            public EdfMessageType getMessageType()
            {
                return EdfMessageType.dataMessage;
            }

            @Override
            public String toJson(boolean prettyPrint)
                    throws JsonProcessingException
            {
                return "{\"data\": \"test\"}";
            }

            @Override
            public String getApiName()
            {
                return API_NAME;
            }

            @Override
            public String getLogId()
            {
                return LOG_ID;
            }
        };
    }

    private IEdfV2Message createPluginV2Message()
    {
        return new IEdfV2Message()
        {
            @Override
            public String getCustomer()
            {
                return LOG_CUSTOMER;
            }

            @Override
            public String getSessionId()
            {
                return LOG_SESSION_ID;
            }

            @Override
            public Integer getSequenceNumber()
            {
                return 123;
            }

            @Override
            public String getData()
            {
                return "{\"data\": \"test\"}";
            }

            @Override
            public EdfMessageType getMessageType()
            {
                return EdfMessageType.dataMessage;
            }
        };
    }


}