package com.nuance.cobra.utils;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.http.CachingHttpClient;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@SuppressStaticInitializationFor({"com.nuance.cobra.utils.http.CachingHttpClient"})
public class TenantHttpClientTest {

    private CombinedConfiguration theTestConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

    @Before
    public void setup() {
        Configuration testConfig = CobraConfigurationUtils.getHighestConfig(theTestConfigs);

        testConfig.setProperty(WebApiConfig.Property.TENANT_CONFIG_URI_PREFIX.key, "http://127.0.0.1/");
        testConfig.setProperty(WebApiConfig.Property.TENANT_CONFIG_SUFFIX_SEARCH_ORDER.key, ".json");

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.mockStatic(CachingHttpClient.class);

        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(theTestConfigs);
    }

    @Test
    public void getConfigForTenant()
            throws Exception
    {
        CloseableHttpResponse mockResp = mock(CloseableHttpResponse.class);
        StatusLine mockStatus = mock(StatusLine.class);
        when(mockStatus.getStatusCode()).thenReturn(200);
        when(mockResp.getStatusLine()).thenReturn(mockStatus);

        HttpEntity mockEntity = mock(HttpEntity.class);
        when(mockResp.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("{\"test\":\"no\"}".getBytes()));

        when(CachingHttpClient.getUriSynchronously(anyString())).thenReturn(mockResp);

        WebApiJSONConfiguration resultConf = TenantHttpClient.getConfigForTenant("test");
        assertEquals("no", resultConf.getString("test"));

        when(mockStatus.getStatusCode()).thenReturn(404);
        try
        {
            TenantHttpClient.getConfigForTenant("test");
            fail();
        } catch (CobraException e) { }

        when(CachingHttpClient.class, "getUriSynchronously", anyString()).thenReturn(null);
        try
        {
            TenantHttpClient.getConfigForTenant("test");
            fail();
        } catch (CobraException e) { }

        doThrow(new InterruptedException()).when(CachingHttpClient.class, "getUriSynchronously", anyString());
        try
        {
            TenantHttpClient.getConfigForTenant("test");
            fail();
        } catch (CobraException e) { }
    }
}