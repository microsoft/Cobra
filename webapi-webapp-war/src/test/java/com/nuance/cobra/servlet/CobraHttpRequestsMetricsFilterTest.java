package com.nuance.cobra.servlet;

import com.nuance.cobra.testing.AbstractMetricsVerifier;
import com.nuance.cobra.testing.MetricsVerifier;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CobraHttpRequestsMetricsFilterTest {

    Logger logger = LogManager.getLogger(CobraHttpRequestsMetricsFilterTest.class);


    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = Test.None.class)
    public void init() throws ServletException {
        CobraHttpRequestsMetricsFilter testFilter = new CobraHttpRequestsMetricsFilter();
        FilterConfig mockConfig = mock(FilterConfig.class);
        testFilter.init(mockConfig);
    }

    @Test
    public void doFilter() throws IOException, ServletException, InterruptedException {

        CobraHttpRequestsMetricsFilter testFilter = new CobraHttpRequestsMetricsFilter();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        final String TENANT_NAME = "test-tenant";
        final String FUNCTION_NAME = "test-function";
        final String ACTION_NAME = "test-action";
        final String METHOD = "POST";

        final int SLEEP_TIME_MS = 2200;
        final Double  EXPECTED_BUCKET = 3.0;

        when(mockRequest.getAttribute(MetricsConstants.TENANT_LABEL_ATTRIBUTE_KEY)).thenReturn(TENANT_NAME);
        when(mockRequest.getAttribute(MetricsConstants.FUNCTION_LABEL_ATTRIBUTE_KEY)).thenReturn(FUNCTION_NAME);
        when(mockRequest.getAttribute(MetricsConstants.ACTION_LABEL_ATTRIBUTE_KEY)).thenReturn(ACTION_NAME);
        when(mockRequest.getMethod()).thenReturn(METHOD);

        when(mockResponse.getStatus()).thenReturn(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        final int NB_FILTER_CALLS = 3;

        FilterChain sleepingFilter = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                try {
                    Thread.sleep(SLEEP_TIME_MS); // NOSONAR deliberately wait to simulate processing time.
                } catch (InterruptedException e) {
                    fail();
                }
            }
        };

        for (int i = 0; i < NB_FILTER_CALLS; ++i) {
            testFilter.doFilter(mockRequest, mockResponse, sleepingFilter);
        }
        final Double EPSILON = 0.000001;

        AbstractMetricsVerifier abstractMetricsVerifier = new AbstractMetricsVerifier() {
            @Override
            public void doMatch(Map<String, String> labelValuePair, double actualVal, double expectedVal) {
                String leValue = labelValuePair.get("le");
                Double bucket = leValue.equals("+Inf") ? Double.POSITIVE_INFINITY :
                        Double.parseDouble(leValue);

                if (bucket >= 3.0 - EPSILON) {
                    assertTrue(Math.abs(actualVal - expectedVal ) <= EPSILON );
                }
                else {
                    assertTrue(Math.abs(actualVal) <= EPSILON );
                }

                assertEquals(TENANT_NAME, labelValuePair.get(MetricsConstants.TENANT_LABEL));
                assertEquals(METHOD, labelValuePair.get(MetricsConstants.HTTP_METHOD_LABEL));
                assertEquals(FUNCTION_NAME, labelValuePair.get(MetricsConstants.FUNCTION_LABEL));
                assertEquals(ACTION_NAME, labelValuePair.get(MetricsConstants.API_ACTION_LABEL));
            }
        };

        assertTrue(abstractMetricsVerifier.verifyMetric(
                CobraHttpRequestsMetricsFilter.HTTP_LATENCY_HISTOGRAM_NAME +
                        "_bucket",
                new String[]{
                        MetricsConstants.TENANT_LABEL,
                        MetricsConstants.HTTP_METHOD_LABEL,
                        MetricsConstants.FUNCTION_LABEL,
                        MetricsConstants.API_ACTION_LABEL,
                        "le"},
                new String[]{TENANT_NAME, METHOD, FUNCTION_NAME, ACTION_NAME},
                NB_FILTER_CALLS));

        // test 2

        abstractMetricsVerifier = new AbstractMetricsVerifier() {
            @Override
            public void doMatch(Map<String, String> labelValuePair, double actualVal, double expectedVal) {
                String statusCode = labelValuePair.get(MetricsConstants.STATUS_CODE_LABEL);

                assertEquals(Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), statusCode);
                assertTrue(Math.abs(actualVal - expectedVal) <= EPSILON );

                assertEquals(TENANT_NAME, labelValuePair.get(MetricsConstants.TENANT_LABEL));
                assertEquals(METHOD, labelValuePair.get(MetricsConstants.HTTP_METHOD_LABEL));
                assertEquals(FUNCTION_NAME, labelValuePair.get(MetricsConstants.FUNCTION_LABEL));
                assertEquals(ACTION_NAME, labelValuePair.get(MetricsConstants.API_ACTION_LABEL));
            }
        };

        assertTrue(abstractMetricsVerifier.verifyMetric(
                CobraHttpRequestsMetricsFilter.HTTP_REQUESTS_COUNTER_NAME,
                new String[]{
                        MetricsConstants.TENANT_LABEL,
                        MetricsConstants.HTTP_METHOD_LABEL,
                        MetricsConstants.FUNCTION_LABEL,
                        MetricsConstants.API_ACTION_LABEL,
                        MetricsConstants.STATUS_CODE_LABEL},
                new String[]{TENANT_NAME, METHOD, FUNCTION_NAME, ACTION_NAME,
                        Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)},
                NB_FILTER_CALLS));
    }


    @Test(expected = Test.None.class)
    public void destroy() {
        CobraHttpRequestsMetricsFilter testFilter = new CobraHttpRequestsMetricsFilter();
        testFilter.destroy();
    }

}