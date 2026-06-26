package com.nuance.cobra.session;

import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.TenantHttpClient;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.nuance.cobra.session.WebApiConfig.Property.APPLICATION_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

//Needs a separate class to reset singletons.
@RunWith(PowerMockRunner.class)
@PrepareForTest({TenantHttpClient.class, CobraConfigurationUtils.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class WebApiConfigManagerHttpTest
{
    static String workingDir;
    static File tenantsDir;
    static File webapiJson;

    private static String tenantsDirString = "/tenantsHttp/";
    private static String webapiConfigPrefix = "webapiHttp";

    @BeforeClass
    public static void setUp() throws FileNotFoundException, UnsupportedEncodingException {
        workingDir = System.getProperty("user.dir");

        tenantsDir = new File(workingDir + tenantsDirString);
        tenantsDir.mkdirs();

        webapiJson = new File(workingDir + "/config/" + webapiConfigPrefix + ".json");
        PrintWriter writer = new PrintWriter(webapiJson, "UTF-8");
        writer.println("{");
        writer.println("\"tenantConfigScheme\":\"fetched\",");
        writer.println("\"tenantConfigSuffixSearchOrder\":[\".json\", \".properties\", \".xml\", \".yml\"]");
        writer.println("}");
        writer.close();

        WebApiConfigManager.getInstance(webapiConfigPrefix);
    }

    @AfterClass
    public static void cleanup() {
        FileUtils.deleteQuietly(tenantsDir);
        FileUtils.deleteQuietly(webapiJson);
    }

    @Test
    public void testGetConfigForTenantHttp() throws CobraException, IOException {
        WebApiJSONConfiguration mockConfigs = mock(WebApiJSONConfiguration.class);
        File file = new File(workingDir + tenantsDirString + "testTenantHttp.json");
        PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
        writer.println("{");
        writer.println("\"testkey\":\"testValue\"");
        writer.println("}");
        writer.close();

        PowerMockito.mockStatic(TenantHttpClient.class);
        PowerMockito.mockStatic(CobraConfigurationUtils.class);

        CombinedConfiguration mockedCombinedConfig = mock(CombinedConfiguration.class);
        PowerMockito.when(CobraConfigurationUtils.shallowCopyCombinedConfiguration(any())).thenReturn(mockedCombinedConfig);

        PowerMockito.when(TenantHttpClient.getConfigForTenant("testTenantHttp")).thenReturn(mockConfigs);

        WebApiConfigManager.getCombinedConfigForTenant("testTenantHttp", tenantsDirString);
        verify(mockConfigs).setProperty(APPLICATION_KEY.key, "testTenantHttp");
    }
}
