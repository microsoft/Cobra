package com.nuance.cobra.servlet;


import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Used to gather metrics for the HTTP endpoint.
 */
public class CobraHttpRequestsMetricsFilter implements Filter {
    protected FilterConfig config;

    private static final Logger logger = LogManager.getLogger(CobraHttpRequestsMetricsFilter.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(CobraHttpRequestsMetricsFilter.class);

    static final String  HTTP_REQUESTS_COUNTER_NAME = "webapi_http_requests_total";
    static final String  HTTP_LATENCY_HISTOGRAM_NAME = "webapi_http_requests_latency_seconds";

    /**
     * Prometheus metrics for counting HTTP requests.
     */
    private static final Counter requestCounter =
            Counter.build(HTTP_REQUESTS_COUNTER_NAME, "HTTP requests counter")
                    .labelNames(MetricsConstants.TENANT_LABEL,
                            MetricsConstants.HTTP_METHOD_LABEL,
                            MetricsConstants.FUNCTION_LABEL,
                            MetricsConstants.API_ACTION_LABEL,
                            MetricsConstants.STATUS_CODE_LABEL)
                    .register();

    /**
     * Prometheus metrics for measuring HTTP request internal latency.
     */
    private static final Histogram requestLatencyHistogram =
            Histogram.build(HTTP_LATENCY_HISTOGRAM_NAME, "HTTP requests latency")
                    .labelNames(MetricsConstants.TENANT_LABEL,
                            MetricsConstants.HTTP_METHOD_LABEL,
                            MetricsConstants.FUNCTION_LABEL,
                            MetricsConstants.API_ACTION_LABEL)
                    .buckets(0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0)
                    .register();


    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        // nothing to do
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long elapsedTimeInMilliseconds = System.currentTimeMillis() - startTime;

        String uri = "<undefined>";
        if (request instanceof HttpServletRequest) {
            uri = ((HttpServletRequest) request).getRequestURI();
        }

        String tenant = MetricsConstants.nonNullLabelValue(
                (String) request.getAttribute(MetricsConstants.TENANT_LABEL_ATTRIBUTE_KEY));

        String httpMethod = MetricsConstants.UNDEFINED_LABEL_VALUE;
        if (request instanceof HttpServletRequest) {
            httpMethod = ((HttpServletRequest) request).getMethod();
        }

        String function = MetricsConstants.nonNullLabelValue(
                (String) request.getAttribute(MetricsConstants.FUNCTION_LABEL_ATTRIBUTE_KEY));

        String action = MetricsConstants.nonNullLabelValue(
                (String) request.getAttribute(MetricsConstants.ACTION_LABEL_ATTRIBUTE_KEY));

        String statusCode = MetricsConstants.UNDEFINED_LABEL_VALUE;
        if (response instanceof HttpServletResponse) {
            statusCode = Integer.toString(((HttpServletResponse) response).getStatus());
        }

        EsapiLogger.trace(esapiLogger, "Metrics: uri=%s, tenant=%s, method=%s, function=%s, action=%s, status=%s. Latency %d ms",
                uri, tenant, httpMethod, function, action, statusCode, elapsedTimeInMilliseconds);

        requestLatencyHistogram.labels(tenant, httpMethod, function, action)
                .observe((double) elapsedTimeInMilliseconds / 1000.0);

        requestCounter.labels(tenant, httpMethod, function, action, statusCode).inc();
    }
}