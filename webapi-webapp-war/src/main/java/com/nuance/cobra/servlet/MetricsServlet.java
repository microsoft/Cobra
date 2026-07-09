package com.nuance.cobra.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Splitter;
import com.nuance.cobra.api.http.HttpEndpoint;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.http.CachingHttpClient;
import com.nuance.cobra.utils.logging.EsapiLogger;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MetricsServlet
        extends HttpServlet
{

    static CollectorRegistry registry = CollectorRegistry.defaultRegistry;
    private static final Logger logger = LogManager.getLogger(MetricsServlet.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(MetricsServlet.class);

    @SuppressWarnings("java:S1215")
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (!checkMetricsAuth(req))
        {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setHeader("WWW-Authenticate", "Basic");
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(TextFormat.CONTENT_TYPE_004);

        try (Writer writer = resp.getWriter())
        {
            TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(req)));
            writer.flush();
        }
        catch (Exception e)
        {
            HttpEndpoint.quietErrorResponse(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        try
        {
            final Map<String, String> map = Splitter.on('&')
                                                    .trimResults()
                                                    .withKeyValueSeparator("=")
                                                    .split(req.getQueryString());
            if (map.containsKey("gc"))
            {
                logger.info("Explcit System.gc() call via metrics servlet!");
                System.gc();
            }
            else if (map.containsKey("flushHttpCache"))
            {
                logger.info("Flushing cache for CachingHttpClient!");
                CachingHttpClient.resetCachingClient();
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (Exception e)
        {
            //Just swallow it. This is ancillary.
        }

    }

    private Set<String> parse(HttpServletRequest req)
    {
        String[] includedParam = req.getParameterValues("name[]");
        if (includedParam == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return new HashSet<>(Arrays.asList(includedParam));
        }
    }

    //Returns the username and password (joined in Base64)
    private String getBasicAuthCreds(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) { return null; }

        StringTokenizer st = new StringTokenizer(authHeader);
        if (!st.nextToken().equalsIgnoreCase("Basic")) { return null; }

        return st.nextToken();
    }

    private boolean checkMetricsAuth(HttpServletRequest req) {
        String username = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.METRICS_USERNAME.key);
        String password = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.METRICS_PASSWORD.key);

        if(username == null || password == null) {
            logger.error("{} and/or {} not set in key vault or in webapi.json. Please set the username and password for prometheus metrics.", WebApiConfig.Property.METRICS_USERNAME.key, WebApiConfig.Property.METRICS_PASSWORD.key);
            return false;
        }

        String httpUserpwBase64 = getBasicAuthCreds(req);
        if(httpUserpwBase64 == null) { return false; }

        String configBase64 = String.format("%s:%s", username, password);
        configBase64 = new String(Base64.getEncoder().encode(configBase64.getBytes(StandardCharsets.UTF_8)));

        return configBase64.equals(httpUserpwBase64);
    }

}
