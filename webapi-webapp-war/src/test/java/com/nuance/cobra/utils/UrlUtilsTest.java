package com.nuance.cobra.utils;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class UrlUtilsTest
{

    private static final String urlString = "https://host:123/";
    private static final URL url = UrlUtils.getServerUrl(urlString);

    private static final String urlStringPlainText = "http://host:123/";
    private static final URL urlPlainText = UrlUtils.getServerUrl(urlStringPlainText);

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
       new UrlUtils();
    }

    @Test
    public void getServerUrl()
    {
        assertNull(UrlUtils.getServerUrl(null));
        assertNotNull(url);
    }

    @Test
    public void getServerHost()
    {
        assertNull(UrlUtils.getServerHost(null));
        assertNotNull(UrlUtils.getServerHost(url));
    }

    @Test
    public void getServerPort()
    {
        assertNull(UrlUtils.getServerPort(null));
        assertNotNull(UrlUtils.getServerPort(url));
    }

    @Test
    public void getProtocol()
    {
        assertNull(UrlUtils.getProtocol(null));
        assertNotNull(UrlUtils.getProtocol(url));
    }

    @Test
    public void isPlainTextProtocol()
    {
        assertFalse(UrlUtils.isPlainTextProtocol((URL)null));
        assertFalse(UrlUtils.isPlainTextProtocol(url));
        assertTrue(UrlUtils.isPlainTextProtocol(urlPlainText));
    }

    @Test
    public void testIsPlainTextProtocol()
    {
        assertFalse(UrlUtils.isPlainTextProtocol((String)null));
        assertFalse(UrlUtils.isPlainTextProtocol("https"));
        assertFalse(UrlUtils.isPlainTextProtocol("grpcs"));
        assertFalse(UrlUtils.isPlainTextProtocol("wss"));
        assertFalse(UrlUtils.isPlainTextProtocol("rediss"));
        assertFalse(UrlUtils.isPlainTextProtocol("aaas"));
        assertFalse(UrlUtils.isPlainTextProtocol("coaps"));
        assertFalse(UrlUtils.isPlainTextProtocol("ssh"));
        assertFalse(UrlUtils.isPlainTextProtocol("sips"));
        assertFalse(UrlUtils.isPlainTextProtocol("ipps"));
        assertFalse(UrlUtils.isPlainTextProtocol("ircs"));
        assertFalse(UrlUtils.isPlainTextProtocol("iris.xpcs"));
        assertFalse(UrlUtils.isPlainTextProtocol("stuns"));
        assertFalse(UrlUtils.isPlainTextProtocol("turns"));
        assertFalse(UrlUtils.isPlainTextProtocol("xmlrpc.beeps"));
        assertTrue(UrlUtils.isPlainTextProtocol("http"));
    }

    @Test
    public void testIsHttpScheme()
    {
        assertFalse(UrlUtils.isHttpScheme("ftp"));
        assertTrue(UrlUtils.isHttpScheme("http"));
        assertTrue(UrlUtils.isHttpScheme("https"));
        assertFalse(UrlUtils.isHttpScheme("httpsftp"));
    }

    @Test
    public void testIsSSRF()
    {
        UrlUtils.initalize();
        assertFalse(UrlUtils.isSSRF("https://asr.api.example.com"));
        assertFalse(UrlUtils.isSSRF("https://asr.api.example.eu"));
        assertTrue(UrlUtils.isSSRF("ftp://localhost"));
        assertTrue(UrlUtils.isSSRF("https://<script>"));
        assertTrue(UrlUtils.isSSRF("https://10.3.2.1/admin"));
        assertTrue(UrlUtils.isSSRF("https://127.0.0.1/admin"));
        assertFalse(UrlUtils.isSSRF("http://webapi-perf/tenants/"));
        assertFalse(UrlUtils.isSSRF("http://nina4-config-prod.nina4-shared.svc.cluster.local/tenant-config/webapi/"));
        assertFalse(UrlUtils.isSSRF("http://mtl-blade20vm71/"));
    }
}