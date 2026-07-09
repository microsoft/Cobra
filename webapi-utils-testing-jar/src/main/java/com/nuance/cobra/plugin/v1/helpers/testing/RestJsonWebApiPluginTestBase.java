package com.nuance.cobra.plugin.v1.helpers.testing;

import com.nuance.cobra.testing.JwtAuthenticationTestBase;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Base class for REST plugin tests.
 *
 * Provides the basic functionality and helpers for implementing common unit test cases where an HTTP service method
 * is invoked on WebApiRawPlugin wiht a request URI, where the method body is JSON, and that the returned response
 * status code and content are asserted on.
 *
 * Since it is expected that a few variations of method invocations with different request URIs, JSON body and expected
 * result will be done in a test suite, a TestSpec object is defined to pass in those parameter. A variation on the
 * builder pattern is used by this object. The object can be extended by Plugin test cases that may need additional
 * parameters.
 */
public abstract class RestJsonWebApiPluginTestBase extends JwtAuthenticationTestBase
{
    private Logger logger = LogManager.getLogger(RestJsonWebApiPluginTestBase.class);

    protected static final String WEBAPI_PATH = "/webapi-platform/http"; // NOSONAR
    protected static final String TENANT_APP_PATH = "/companyName_appName";  // NOSONAR

    protected static final Charset UTF_8_ENCODING = StandardCharsets.UTF_8;

    /**
     * Method pointer a the plugin REST method
     */
    @FunctionalInterface
    public interface RestPluginMethod
    {
        boolean invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, WebApiCommonException;
    }

    /**
     * Since most of the tests are variations on the input parameters and expected results, these are captured
     * in a test spec, and passed to a generic test implementation.
     *
     * The sole purpose of this class is to hold the parameters values. Setters
     * return the object so they can be chained in a builder-like pattern.
     */
    protected static class TestSpec
    {
        String       jsonRequestResourcePath;     // path of the resource (file) containing the json request body
        int          expectedStatusCode;          // exepcted reponse status code
        String       expectedResultRegex;         // expected regex in response
        String       servicePath;                 // service path path in the URL
        String       action;                      // action in the URL
        String       requestContentType;          // content type of the request

        String[]     loggerNames;       // Logger name if we want to capture logger output
        Level        loggerLevel;        // Log level when we capture logger output
        String       expectedLogRegex;   // expected regex in captured logs

        String       expectedExceptionRegex;   // expected regex in exception message




        public TestSpec()
        {
            // expose public constructor (class has protected visibility)
        }

        public TestSpec setJsonRequestResourcePath(String jsonRequestResourcePath)
        {
            this.jsonRequestResourcePath = jsonRequestResourcePath;
            return this;
        }

        public TestSpec setExpectedStatusCode(int expectedStatusCode)
        {
            this.expectedStatusCode = expectedStatusCode;
            return this;
        }

        public TestSpec setExpectedResultRegex(String expectedResultRegex)
        {
            this.expectedResultRegex = expectedResultRegex;
            return this;
        }

        /**
         * Specify the service path part of the request RUI. Request URI will be WEBAPI_PATH//companyName_appName/<servicePath>/<action>
         */
        public TestSpec setServicePath(String servicePath)
        {
            this.servicePath = servicePath;
            return this;
        }

        /**
         * Specify the action part of the request RUI. Request URI will be WEBAPI_PATH/<servicePath>/<action>
         */
        public TestSpec setAction(String action)
        {
            this.action = action;
            return this;
        }

        /**
         * specify the content-type of the request. If called the content type header will be added to the
         * request
         */
        public TestSpec setRequestContentType(String requestContentType)
        {
            this.requestContentType = requestContentType;
            return this;
        }

        /**
         * specify which logs to capture. If called the logs will be captured for specified logger and output
         * asserted on.
         */
        public TestSpec setExpectedLog(String loggerName, Level level, String expectedLogRegex)
        {
            setExpectedLog(new String[] { loggerName }, level, expectedLogRegex);
            return this;
        }

        /**
         * specify which logs to capture. If called the logs will be captured for specified loggers and output
         * asserted on.
         */
        public TestSpec setExpectedLog(String[] loggerNames, Level level, String expectedLogRegex)
        {
            this.loggerNames = loggerNames;
            this.loggerLevel = level;
            this.expectedLogRegex = expectedLogRegex;
            return this;
        }


        /**
         * specify which logs to capture. If called the logs will be captured for specified loggers and output
         * asserted on.
         */
        public TestSpec setExpectedException(String expectedExceptionRegex)
        {
            this.expectedExceptionRegex = expectedExceptionRegex;
            return this;
        }
    }

    /**
     * Perform a test according to the given test specification. The request and response mock are setup, the
     * plugin is initialized and the method is invoke.
     *
     * @param pluginMethod Method reference (e.g. testPlugin::onHttpPost, where testPlugin is the plugin instance in
     *                     testcase.
     * @param testSpec     The test specification built for this test case.
     */
    protected void performRestMethodTest(IWebApiRawPlugin plugin, RestPluginMethod pluginMethod,
            IWebApiFeatures webapiFeatures, TestSpec testSpec)
            throws IOException, ServletException {
        HttpSession mockHttpSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getSession()).thenReturn(mockHttpSession);

        if (testSpec.servicePath == null) {
            throw new IllegalArgumentException("servicePath in test spec must be set.");
        }

        if (testSpec.action == null) {
            throw new IllegalArgumentException("action in test spec must be set.");
        }

        if (testSpec.jsonRequestResourcePath == null) {
            throw new IllegalArgumentException("jsonRequestResourcePath in test spec must be set.");
        }

        final String pathInfo = TENANT_APP_PATH + testSpec.servicePath + "/" + testSpec.action;
        when(mockRequest.getRequestURI()).thenReturn(WEBAPI_PATH + pathInfo);
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);

        when(mockRequest.getInputStream()).thenReturn(new TestServletInputStream(getClass()
                .getClassLoader()
                .getResourceAsStream(testSpec.jsonRequestResourcePath)));

        when(mockRequest.getCharacterEncoding()).thenReturn(StandardCharsets.UTF_8.toString());


        if (testSpec.requestContentType != null)
        {
            when(mockRequest.getContentType()).thenReturn(testSpec.requestContentType);
        }

        if (testSpec.loggerNames != null)
        {
            startCapturingLogOutput(testSpec.loggerLevel, testSpec.loggerNames);
        }


        Exception exceptionCaught = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ServletOutputStream sos = new ServletOutputStream() {
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
        }) {
            when(mockResponse.getOutputStream()).thenReturn(sos);

            preInvokePlugin(mockRequest, mockResponse, testSpec);
            plugin.onInitPlugin(webapiFeatures);

            try {
                pluginMethod.invoke(mockRequest, mockResponse);
            }
            catch (WebApiCommonException e) {
                exceptionCaught = e;
                if (testSpec.expectedExceptionRegex != null) {
                    Pattern p = Pattern.compile(testSpec.expectedExceptionRegex);
                    Matcher m = p.matcher(e.getMessage());
                    logger.trace("Exception message: {}", e.getMessage());
                    assertTrue("Expected '" + testSpec.expectedExceptionRegex + "' in exception message.", m.find());
                }
                else {
                    fail("Unexpected exception: " + e.getMessage());
                }
            }
        }

        // Check response content
        byte[] bytes = bos.toByteArray();
        Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), Charset.forName("UTF-8"));

        String responseBody = IOUtils.toString(reader);
        logger.trace("response content: {}", responseBody);

        if (testSpec.expectedResultRegex != null)
        {
            Pattern p = Pattern.compile(testSpec.expectedResultRegex);
            Matcher m = p.matcher(responseBody);
            assertTrue("Response must match : " + testSpec.expectedResultRegex, m.find());
        }

        int expectedStatus = testSpec.expectedStatusCode;
        if (expectedStatus == 0)
        {
            expectedStatus = HttpServletResponse.SC_OK;
        }

        if (testSpec.loggerNames != null && testSpec.expectedLogRegex != null)
        {
            Pattern p = Pattern.compile(testSpec.expectedLogRegex);
            Matcher m = p.matcher(getCapturedLogOutput());
            assertTrue("Captured logs must match : " + testSpec.expectedLogRegex, m.find());
        }

        validateResult(mockRequest, mockResponse, testSpec);
    }

    static class TestServletInputStream
            extends ServletInputStream
    {
        InputStream inputStream;

        TestServletInputStream(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }

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
            // intentionally left empty
        }

        @Override
        public int read()
                throws IOException
        {
            return inputStream.read();
        }
    }

    /**
     * Subclasses can override to do additional setup  (e.g. mocks) prior to the plugin invocation
     */
    protected void preInvokePlugin(HttpServletRequest mockRequest, HttpServletResponse mockResponse, TestSpec testSpec)
    {
    }

    /**
     * Subclasses can override to perform additional validations (beyond status code verification and regexp matching
     * after the method was invoked.
     * @param testSpec
     * @param mockRequest
     * @param mockResponse
     */
    protected void validateResult(HttpServletRequest mockRequest, HttpServletResponse mockResponse, TestSpec testSpec)
    {
    }
}