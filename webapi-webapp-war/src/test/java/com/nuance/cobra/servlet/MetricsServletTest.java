package com.nuance.cobra.servlet;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.http.CachingHttpClient;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class, CachingHttpClient.class, SecretCacheProvider.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*", "sun.security.*", "javax.net.ssl.*"})
public class MetricsServletTest
{
    private MetricsServlet testMetricsServlet;
    private final CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private final Configuration testConfig = testConfigs.getConfiguration(0);

    @Before
    public void setUp() throws Exception {
        testMetricsServlet = new MetricsServlet();
        MetricsServlet.registry = new CollectorRegistry();
    }

    @Test
    public void testDoGet()
            throws Exception
    {
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        //null username and pw
        testMetricsServlet.doGet(mockRequest, mockResponse);

        testConfig.setProperty(WebApiConfig.Property.METRICS_USERNAME.key, "testUser");
        testConfig.setProperty(WebApiConfig.Property.METRICS_PASSWORD.key, "testPassword");
        testMetricsServlet.doGet(mockRequest, mockResponse);

        when(mockRequest.getHeader("Authorization")).thenReturn("testUser:testPassword");
        assertNotNull(mockRequest.getHeader("Authorization"));
        testMetricsServlet.doGet(mockRequest, mockResponse);

        when(mockRequest.getHeader("Authorization")).thenReturn("basic\ttestUser:testPassword");
        testMetricsServlet.doGet(mockRequest, mockResponse);

        when(mockRequest.getHeader("Authorization")).thenReturn("basic\t"+new String(Base64.getEncoder().encode("testUser:testPassword".getBytes(
                StandardCharsets.UTF_8))));
        testMetricsServlet.doGet(mockRequest, mockResponse);

        when(mockRequest.getQueryString()).thenReturn("gc=true");
        testMetricsServlet.doGet(mockRequest, mockResponse);

        PowerMockito.mockStatic(CachingHttpClient.class);
        PowerMockito.doThrow(new InterruptedException()).when(CachingHttpClient.class, "resetCachingClient");
        when(mockRequest.getQueryString()).thenReturn("flushHttpCache=true");
        when(mockRequest.getParameterValues("name[]")).thenReturn(new String[]{"chris"});
        testMetricsServlet.doGet(mockRequest, mockResponse);
    }

    @Test
    public void testDoGetKeyVault() throws Exception {
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfigs);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        testConfig.setProperty(WebApiConfig.Property.KEY_VAULT_SERVER_SECRET_NAME.key, "keyVaultServerSecretNameTest");
        testConfig.setProperty(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key, "https://entrd-dev-secrets.vault.azure.net");
        testConfig.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "7d7f1b8e-038a-4b41-850b-3a8b949f7135");

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode node = factory.objectNode();
        node.put(WebApiConfig.Property.METRICS_USERNAME.key, "testUser");
        node.put(WebApiConfig.Property.METRICS_PASSWORD.key, "testPassword");

        // Mock SecretCacheProvider and stub the methods
        SecretCacheProvider providerMock = mock(SecretCacheProvider.class);
        when(providerMock.getSecretFromKeyVault(anyString(), any(CombinedConfiguration.class))).thenReturn(node);

        // Mock the static method SecretCacheProvider.getInstance()
        PowerMockito.mockStatic(SecretCacheProvider.class);
        when(SecretCacheProvider.getInstance()).thenReturn(providerMock);

        when(mockRequest.getHeader("Authorization")).thenReturn("basic\t"+new String(Base64.getEncoder().encode("testUser:testPassword".getBytes(
                StandardCharsets.UTF_8))));
        testMetricsServlet.doGet(mockRequest, mockResponse);


    }
}