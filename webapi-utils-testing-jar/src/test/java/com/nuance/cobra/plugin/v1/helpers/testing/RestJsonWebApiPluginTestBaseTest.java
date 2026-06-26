package com.nuance.cobra.plugin.v1.helpers.testing;

import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * Even unit test base classes need to be unit tested...
 */
public class RestJsonWebApiPluginTestBaseTest
{

    class TestClass extends RestJsonWebApiPluginTestBase
    {

        IWebApiRawPlugin mockPlugin = mock(IWebApiRawPlugin.class);
        CombinedConfiguration testConf = mock(CombinedConfiguration.class);
        IWebApiFeatures webApiFeatures = mock(IWebApiFeatures.class);

        static final String TEST_REGEXP = "this(.*)test";
        String responseBodyStr;
        int responseStatusCode;

        void setup()
                throws IOException
        {
            when(webApiFeatures.getConfigs()).thenReturn(testConf);
        }

        void performSuccessfulDefaultTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setServicePath("/service/path")
                    .setAction("action")
                    .setJsonRequestResourcePath("test_request.json");

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
            verify(mockPlugin, times(1)).onHttpPost(any(), any());
        }

        void performSuccessfulWithRegexpTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setServicePath("/service/path")
                    .setAction("action")
                    .setJsonRequestResourcePath("test_request.json")
                    .setExpectedResultRegex(TEST_REGEXP);

            responseBodyStr = "this is just a test!";

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
            verify(mockPlugin, times(1)).onHttpPost(any(), any());
        }

        void performRegexpMismatchTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setServicePath("/service/path")
                    .setAction("action")
                    .setJsonRequestResourcePath("test_request.json")
                    .setExpectedResultRegex(TEST_REGEXP);

            responseBodyStr = "mismatch!";

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
            verify(mockPlugin, times(1)).onHttpPost(any(), any());
        }

        @Override
        protected void preInvokePlugin(HttpServletRequest mockRequest, HttpServletResponse mockResponse, TestSpec testSpec)
        {
            if (responseBodyStr != null)
            {
                try
                {
                    mockResponse.getOutputStream()
                                .println(responseBodyStr);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("IO Exception while writing response.", e);
                }
            }
        }

        void performServicePathTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setAction("action")
                    .setJsonRequestResourcePath("test_request.json");

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
        }

        void performMissingActionTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setServicePath("/service/path")
                    .setJsonRequestResourcePath("test_request.json");

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
        }

        void performMissingJsonRequestTest()
                throws IOException, ServletException, WebApiCommonException {
            TestSpec testSpec = new TestSpec();
            testSpec.setServicePath("/service/path")
                    .setAction("action");

            performRestMethodTest(mockPlugin, mockPlugin::onHttpPost, webApiFeatures, testSpec);
        }
    }

    @Test
    public void successfulDefaultTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performSuccessfulDefaultTest();
    }

    @Test
    public void successfulWithRegexpTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performSuccessfulWithRegexpTest();
    }

    @Test(expected =java.lang.AssertionError.class)
    public void regexpMismatchTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performRegexpMismatchTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingServicePathTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performServicePathTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingActionTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performMissingActionTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingJsonRequestTest() throws IOException, ServletException, WebApiCommonException {
        TestClass testInstance = new TestClass();
        testInstance.performMissingJsonRequestTest();
    }

}