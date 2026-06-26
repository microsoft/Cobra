package com.nuance.cobra.session;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@RunWith(PowerMockRunner.class)
@PrepareForTest({SecretCacheProvider.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class WebApiConfigManagerTest
{
    static String workingDir;
    static File tenantsDir;
    static File webapiJson;
    static File webapiJsonKeyVault;
    SecretCacheProvider providerMock;

    @BeforeClass
    public static void setUp() throws FileNotFoundException, UnsupportedEncodingException {
        workingDir = System.getProperty("user.dir");

        tenantsDir = new File(workingDir + "/tenants");
        tenantsDir.mkdirs();

        webapiJson = new File(workingDir + "/config/webapi.json");
        PrintWriter writer = new PrintWriter(webapiJson, "UTF-8");
        writer.println("{");
        writer.println("\"tenantConfigSuffixSearchOrder\":[\".json\", \".properties\", \".xml\", \".yml\"]");
        writer.println("}");
        writer.close();

        //json file parser
        File file = new File(workingDir + "/tenants/testTenant.json");
        writer = new PrintWriter(file, "UTF-8");
        writer.println("{");
        writer.println("\"testkey\":\"testValue\"");
        writer.println("}");
        writer.close();
    }

    @AfterClass
    public static void cleanup() {
        FileUtils.deleteQuietly(tenantsDir);
        FileUtils.deleteQuietly(webapiJson);
        FileUtils.deleteQuietly(webapiJsonKeyVault);
    }

    @Before
    public void setUpMocks() {
        // Mock the SecretCacheProvider
        providerMock = mock(SecretCacheProvider.class);
        PowerMockito.mockStatic(SecretCacheProvider.class);
        PowerMockito.when(SecretCacheProvider.getInstance()).thenReturn(providerMock);
        when(providerMock.getSecretFromKeyVault(anyString(), any(CombinedConfiguration.class))).thenReturn(null);
    }

    @After
    public void tearDown() {
        Mockito.reset(providerMock);
    }
}
