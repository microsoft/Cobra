package com.nuance.plugin.backend;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;

import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpMonitoringPluginTest {
    private CombinedConfiguration mockSessionConfig = mock(CombinedConfiguration.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);
    private PrintWriter writer = mock(PrintWriter.class);

    @Before
    public void initTest() throws IOException {
        LinkedHashMap<String, String> testMap = new LinkedHashMap<>();
        testMap.put("testKey", "testVal");
        when(mockSessionConfig.get(LinkedHashMap.class, "backendServicesMap")).thenReturn(testMap);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void onInitPlugin() {
        HttpMonitoringPlugin plugin = new HttpMonitoringPlugin();
        plugin.onInitPlugin(null);
    }

    @Test
    public void getUrlPattern() {
        HttpMonitoringPlugin plugin = new HttpMonitoringPlugin();
        assertEquals("^monitoring.*", plugin.getUrlPattern());
    }

    @Test(expected = NullPointerException.class)
    public void onHttpGet_exception() {
        HttpMonitoringPlugin plugin = new HttpMonitoringPlugin();
        plugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {

                return mockSessionConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() { }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException {
                return null;
            }
        });
        try {
            plugin.onHttpGet(request, response);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void onHttpGet() {
        HttpMonitoringPlugin plugin = new HttpMonitoringPlugin();
        plugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                CombinedConfiguration sessionConfig = new CombinedConfiguration();
                LinkedHashMap<String, String> testMap = new LinkedHashMap<>();
                testMap.put("testKey", "testVal");
                sessionConfig.setProperty("backendServicesMap", testMap);
                return sessionConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() { }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException {
                return null;
            }
        });
        try {
            assertTrue(plugin.onHttpGet(request, response));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void extractRootKeys()
    {
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        webApiJSONConfiguration.setProperty("nvcgrpcurl", "https://host:443");
        webApiJSONConfiguration.setProperty("Evan.language", "en-us");
        webApiJSONConfiguration.setProperty("Evan.name", "Evan");
        webApiJSONConfiguration.setProperty("Ava.pcm_16_16k.voice_model", "enhanced");
        webApiJSONConfiguration.setProperty("Tim.opus_nb.voice_model", "enhanced");
        webApiJSONConfiguration.setProperty("George.opus_nb.vbr", "off");
        webApiJSONConfiguration.setProperty("speechSynthesisCodec", "pcm_16_16k");

        Set<String> names = HttpMonitoringPlugin.extractRootKeys(webApiJSONConfiguration);
        Set<String> expectedNames = Set.of("nvcgrpcurl", "Evan", "Ava", "Tim", "George", "speechSynthesisCodec");
        assertEquals(expectedNames, names);
    }
}