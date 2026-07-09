package com.nuance.cobra.logging.edf;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.Before;
import org.junit.Test;

import org.apache.http.protocol.HttpContext;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientContext.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class EdfRedirectStrategyTest {
    private EdfRedirectStrategy testEdfRedirectStrategy;
    private HttpRequest mockReqst = mock(HttpRequest.class);
    private HttpResponse mockResponse = mock(HttpResponse.class);
    private HttpContext mockContext = mock(HttpContext.class);

    @Before
    public void setUp(){
        testEdfRedirectStrategy = new EdfRedirectStrategy();
    }

    @Test
    public void testIdRedirected() throws ProtocolException {
        StatusLine mockStatusline = mock(StatusLine.class);

        when(mockResponse.getStatusLine()).thenReturn(mockStatusline);
        when(mockStatusline.getStatusCode()).thenReturn(301);

        assertTrue(testEdfRedirectStrategy.isRedirected(mockReqst, mockResponse, mockContext));

        when(mockStatusline.getStatusCode()).thenReturn(304);
        assertFalse(testEdfRedirectStrategy.isRedirected(mockReqst, mockResponse, mockContext));
    }

    @Test
    public void testGetRedirect() throws ProtocolException {
        HttpClientContext mockClientContext = mock(HttpClientContext.class);
        PowerMockito.mockStatic(HttpClientContext.class);
        PowerMockito.when(HttpClientContext.adapt(mockContext)).thenReturn(mockClientContext);
        RequestConfig mockReqConfig = mock(RequestConfig.class);

        RequestLine mockLine = mock(RequestLine.class);
        StatusLine mockStatusline = mock(StatusLine.class);
        Header mockHeader = mock(Header.class);

        HttpHost mockHost = new HttpHost("Nuance");

        when(mockClientContext.getRequestConfig()).thenReturn(mockReqConfig);
        when(mockClientContext.getTargetHost()).thenReturn(mockHost);
        when(mockHeader.getValue()).thenReturn("http://test/loc/test/");
        when(mockResponse.getFirstHeader("location")).thenReturn(mockHeader);
        when(mockReqst.getRequestLine()).thenReturn(mockLine);

        when(mockLine.getMethod()).thenReturn("HEAD");
        testEdfRedirectStrategy.getRedirect(mockReqst, mockResponse, mockContext);

        when(mockLine.getMethod()).thenReturn("GET");
        testEdfRedirectStrategy.getRedirect(mockReqst, mockResponse, mockContext);

        when(mockLine.getMethod()).thenReturn("other");

        when(mockResponse.getStatusLine()).thenReturn(mockStatusline);
        when(mockStatusline.getStatusCode()).thenReturn(309);
        assertEquals(309, mockStatusline.getStatusCode());
        assertNotNull(testEdfRedirectStrategy.getRedirect(mockReqst, mockResponse, mockContext));
    }
}