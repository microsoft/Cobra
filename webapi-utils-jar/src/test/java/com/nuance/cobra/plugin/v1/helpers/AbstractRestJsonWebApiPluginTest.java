package com.nuance.cobra.plugin.v1.helpers;

import com.nuance.cobra.utils.errors.RestPluginsError;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.IJwtValidationsConfigurator;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AbstractRestJsonWebApiPluginTest
{
    final static String DUMMY_SERVICE_PATH = "dummy-service-path";
    final static String TEST_PARAM_KEY = "testParamKey";
    final static String TEST_PARAM_VALUE = "testParamValue";

    final static String WEBAPI_PATH = "/webapi-platform/http"; // NOSONAR
    final static String TEST_TENANT_APP_PATH = "/companyName_appName";  // NOSONAR
    final static String TEST_ACTION = "test-action";  // NOSONAR

    final static String DUMMY_AUDIENCE = "dummyAud";

    static class DummyPlugin
            extends AbstractRestJsonWebApiPlugin
    {
        String processingErrorMsg;

        protected DummyPlugin(String servicePath)
        {
            super(servicePath);
        }

        @Override
        protected String getFunctionLabelValue()
        {
            return "dummy-function";
        }

        protected DummyPlugin(String servicePath, String processingErrorMsg)
        {
            super(servicePath);
            this.processingErrorMsg = processingErrorMsg;
        }


        @Override
        protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
                throws WebApiCommonException
        {
            if (processingErrorMsg != null)
            {
                throw new WebApiCommonException(RestPluginsError.PROCESSING_ERROR_EXCEPTION, processingErrorMsg);
            }
        }

        @Override
        protected String getJwtAudience()
        {
            return DUMMY_AUDIENCE;
        }

        @Override
        public void onInitPlugin(IWebApiFeatures featuresList)
        {
        }
    }

    @Test
    public void getUrlPatternTest()
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);
        assertTrue(plugin.getUrlPattern()
                         .contains(DUMMY_SERVICE_PATH));
    }

    @Test
    public void validateRequestContentTypeTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json; charset=utf-8");
        plugin.validateRequestContentType(request);
    }

    @Test
    public void validateRequestNullContentTypeTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn(null);
        plugin.validateRequestContentType(request);
    }

    @Test(expected = WebApiCommonException.class)
    public void validateRequestInvalidContentTypeTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application");
        plugin.validateRequestContentType(request);
    }

    @Test(expected = WebApiCommonException.class)
    public void validateRequestUnacceptableContentTypeTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("plain/text");
        plugin.validateRequestContentType(request);
    }

    @Test
    public void getActionTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        final String pathInfo = TEST_TENANT_APP_PATH + "/" + DUMMY_SERVICE_PATH + "/" + TEST_ACTION;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        String action = plugin.getAction(mockRequest);
        assertEquals(TEST_ACTION, action);
    }

    @Test(expected = WebApiCommonException.class)
    public void getActionInvalidPathTest()
            throws WebApiCommonException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        final String pathInfo = TEST_TENANT_APP_PATH + "/" + "invalid-service" + "/" + TEST_ACTION;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        String action = plugin.getAction(mockRequest);
    }

    @Test
    public void getJwtConfiguratorTest()
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        Optional<IJwtValidationsConfigurator> validationsConfigurator =
                    plugin.getPluginFeatures().getJwtValidationsConfigurator();

        assertTrue(validationsConfigurator.isPresent());
    }

    @Test
    public void loadConfigurationParameterTest()
            throws AbstractRestJsonWebApiPlugin.PluginInitException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getString(TEST_PARAM_KEY)).thenReturn(TEST_PARAM_VALUE);

        String value =  plugin.loadConfigurationParameter(mockConfig, TEST_PARAM_KEY);
        assertEquals(TEST_PARAM_VALUE, value);
    }

    @Test(expected = AbstractRestJsonWebApiPlugin.PluginInitException.class)
    public void loadConfigurationInvalidParameterTest()
            throws AbstractRestJsonWebApiPlugin.PluginInitException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getString(TEST_PARAM_KEY)).thenReturn(TEST_PARAM_VALUE);

        String value =  plugin.loadConfigurationParameter(mockConfig, "invalid");
    }

    @Test
    public void checkPluginStatusTest()
            throws WebApiCommonException, AbstractRestJsonWebApiPlugin.PluginInitException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getString(TEST_PARAM_KEY)).thenReturn(TEST_PARAM_VALUE);

        // checkPlugin status throw an exception if there was an error during plugin initialization (e.g. mandatory config)
        // parameter missing.
        // This test checks that is it is initially valid, still valid after successfully getting parameter, but
        // fails check after it failed to read an invalid parameter

        plugin.checkPluginStatus();
        String value =  plugin.loadConfigurationParameter(mockConfig, TEST_PARAM_KEY);
        plugin.checkPluginStatus();

        boolean caughtException = false;
        try
        {
            plugin.loadConfigurationParameter(mockConfig, "invalid");
        }
        catch (AbstractRestJsonWebApiPlugin.PluginInitException initException)
        {
            caughtException = true;
        }
        assertTrue(caughtException);

        caughtException = false;
        try
        {
            plugin.checkPluginStatus();
        }
        catch (WebApiCommonException e)
        {
            caughtException = true;

            System.out.println(e.getMessage());
            assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getHttpStatusCode());
        }
        assertTrue(caughtException);
    }

    @Test
    public void doPostTestOk()
            throws WebApiCommonException, IOException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        final String pathInfo = TEST_TENANT_APP_PATH + "/" + DUMMY_SERVICE_PATH + "/" + TEST_ACTION;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        boolean returnValue = plugin.onHttpPost(mockRequest, mockResponse);
        assertTrue(returnValue);
    }

    @Test(expected = WebApiCommonException.class)
    public void doPostTestError()
            throws WebApiCommonException, IOException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH, "Processing error");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        final String pathInfo = TEST_TENANT_APP_PATH + "/" + DUMMY_SERVICE_PATH + "/" + TEST_ACTION;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ServletOutputStream sos = new TestServletOutputStream();

        when(mockResponse.getOutputStream()).thenReturn(sos);
        boolean returnValue = plugin.onHttpPost(mockRequest, mockResponse);
    }


    @Test
    public void readContentTest()
            throws WebApiCommonException, IOException
    {
        AbstractRestJsonWebApiPlugin plugin = new DummyPlugin(DUMMY_SERVICE_PATH, "Processing error");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        final String pathInfo = TEST_TENANT_APP_PATH + "/" + DUMMY_SERVICE_PATH + "/" + TEST_ACTION;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ServletOutputStream sos = new TestServletOutputStream();

        final String CONTENT = "hello world";
        when(mockRequest.getCharacterEncoding()).thenReturn(String.valueOf(StandardCharsets.UTF_8));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(CONTENT.getBytes(StandardCharsets.UTF_8));

        when(mockRequest.getInputStream()).thenReturn(new ServletInputStream()
        {
            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {
            }

            @Override
            public int read()
                    throws IOException
            {
                return inputStream.read();
            }
        });

        String content = plugin.readContent(mockRequest);
        assertEquals(CONTENT, content);
    }

    /**
     * Used to capture the servlet output.
     */
    static class TestServletOutputStream extends ServletOutputStream
    {
        private OutputStream bos = new ByteArrayOutputStream();

        @Override
        public boolean isReady()
        {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener)
        {
            // intentionally left empty
        }

        @Override
        public void write(int b)
                throws IOException
        {
            bos.write(b);
        }

        @Override
        public String toString()
        {
            return bos.toString();
        }
    }
}