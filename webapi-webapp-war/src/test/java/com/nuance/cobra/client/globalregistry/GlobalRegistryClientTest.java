package com.nuance.cobra.client.globalregistry;

import com.nuance.cobra.api.globalregistry.Company;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import static org.junit.Assert.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*", "sun.net.*", "com.sun.*"})
public class GlobalRegistryClientTest
        extends CobraTest
{
    private GlobalRegistryClient globalRegistryClientTest;

    private CombinedConfiguration cobraConfigurations = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration cobraConfiguration = CobraConfigurationUtils.getHighestConfig(cobraConfigurations);

    private Map<String, Company> companyMap;
    private HttpServer server;

    @Before
    public void setUp()
            throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        // snippet of json from a non-mocked response
        JSONObject companies = new JSONObject("{\"registry\":{\"BartCo\":" +
                "{\"applications\":" +
                "{\"BartApp\":" +
                "{\"applicationVersions\":" +
                "{\"0.0\":" +
                "{\"grammars\":{}}}}}}," +
                "\"Fidelity\":" +
                "{\"applications\":" +
                "{\"CSI_POC\":" +
                "{\"applicationVersions\":" +
                "{\"0.0\":" +
                "{\"grammars\":" +
                "{\"grammars\":" +
                "{\"versionURLMap\":" +
                "{\"0.0.1\":\"/var/Fidelity/CSI_POC/0.0/0.0.1/trainedTopSSMGrammar.grxml\"}," +
                "\"currentVersion\":\"0.0.1\"}}}}}," +
                "\"Fidelity_App\":" +
                "{\"applicationVersions\":" +
                "{\"0.0\":" +
                "{\"grammars\":" +
                "{\"grammars\":" +
                "{\"versionURLMap\":" +
                "{\"0.0.19\":\"/var/Fidelity/Fidelity_App/0.0/0.0.19/trainedTopSSMGrammar.grxml\"}," +
                "\"currentVersion\":\"0.0.19\"}}}}}}}}}");

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(cobraConfigurations);

        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_HOST.key, "127.0.0.1");
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_PORT.key, server.getAddress().getPort());
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_PATH.key, "/var/globalRegistry.json");

        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_CONNECTION_TIMEOUT_MILLISECONDS.key, 4000);
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_READ_TIMEOUT_MILLISECONDS.key, 4000);
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_FETCH_WAIT_TIME_MILLISECONDS.key, 60000);
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_FETCH_INTERVAL_SECONDS.key, 300);
        cobraConfiguration.setProperty(WebApiConfig.Property.GLOBAL_REGISTRY_ROOT.key, "registry");

        globalRegistryClientTest = GlobalRegistryClient.getInstance();

        companyMap = Map.of("testCompany", new Company());

        String path = cobraConfigurations.getProperty(WebApiConfig.Property.GLOBAL_REGISTRY_PATH.key).toString();

        server.setExecutor(null);
        server.start();

        server.createContext(path, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String res = companies.toString();

                exchange.sendResponseHeaders(200, res.length());
                OutputStream os = exchange.getResponseBody();

                os.write(res.getBytes());
                os.close();
            }
        });
    }

    @Test
    public void testFetchGlobalRegistry()
    {
        globalRegistryClientTest.init();
        Map<String, Company> companyMap = globalRegistryClientTest.getCompanies();

        assertNotNull(companyMap);

        assertTrue(companyMap.containsKey("BartCo"));
        assertTrue(companyMap.containsKey("Fidelity"));

        assertFalse(companyMap.isEmpty());
    }

    @After
    public void tearDown(){
        server.stop(0);
    }

}
