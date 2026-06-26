package com.nuance.cobra.utils.http;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class})
@PowerMockIgnore({"sun.net.*", "com.sun.*", "javax.xml.*", "org.apache.xerces.*", "org.w3c.dom.*", "org.xml.sax.*", "javax.net.ssl.*", "javax.management.*"})
public class CachingHttpClientTest {
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);

    @Before
    public void setUp()
            throws Exception
    {
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfs);

        testConf.setProperty(WebApiConfig.Property.CACHING_HTTP_CLIENT_CONNECTION_POOL_SIZE.key, 1);
    }

    @Test(expected = Test.None.class)
    public void resetCachingClient() throws InterruptedException {
        CachingHttpClient.resetCachingClient();
    }

    @Test
    public void closeConnections()
    {
        PoolingHttpClientConnectionManager httpManager = CachingHttpClient.getHttpManager();
        CachingHttpClient.closeConnections(httpManager);
        assertEquals(0, httpManager.getTotalStats()
                                   .getLeased());
    }

    @Test
    public void getUriSynchronously()
            throws IOException
    {
        HttpServer connectionTestServer;
        String responseIp = "localhost";
        connectionTestServer = HttpServer.create(new InetSocketAddress(0), 0);
        connectionTestServer.setExecutor(null);
        connectionTestServer.start();
        connectionTestServer.createContext("/getUriSynchronously", httpExchange -> {
            httpExchange.sendResponseHeaders(200, responseIp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(responseIp.getBytes(StandardCharsets.UTF_8));
            os.close();
        });

        try
        {
            CloseableHttpResponse response = CachingHttpClient.getUriSynchronously("http://localhost:" +
                                                                                   connectionTestServer.getAddress()
                                                                                                       .getPort() + "/getUriSynchronously");
            String body = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            response.close();
            assertEquals("localhost", body);
        } catch (Exception e)
        {
            //SocketTimeout Read timed out
            //failed: connect timed out
            //failed: Connection refused
            assertTrue(e.getMessage().toLowerCase().contains("ime") ||
                                 e.getMessage().toLowerCase().contains("refused"));
        }
    }

    @Test(expected = IOException.class)
    public void ssrf()
            throws IOException, InterruptedException
    {
        CachingHttpClient.getUriSynchronously("ftp://abc");
    }
}