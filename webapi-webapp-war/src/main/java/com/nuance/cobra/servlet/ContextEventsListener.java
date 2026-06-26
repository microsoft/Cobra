package com.nuance.cobra.servlet;

import com.nuance.cobra.api.endpoint.WebApiEndpoint;
import com.nuance.cobra.plugin.PluginLoader;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import io.prometheus.client.Gauge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.jar.Manifest;

@WebListener
public class ContextEventsListener implements ServletContextListener {
    private static Logger logger = LogManager.getLogger(ContextEventsListener.class);

    /**
     * "Gauge" used to expose meta information about this instance.
     */
    private static final Gauge webapiInfoMetric = Gauge.build()
                                                       .name("webapi_info")
                                                       .help("Exposes static information about this WebAPI instance.")
                                                       .labelNames("version")
                                                       .register();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.warn("WebApi contextInitialized called!");
        doContextInitialized(servletContextEvent);
        WebApiEndpoint.contextInitialized(servletContextEvent);
        try {
            PluginLoader.startSystemPlugins();
        } catch (MalformedURLException e) {
            logger.error("Fail to load system plugins", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.warn("WebApi contextDestroyed called!");
        doContextDestroyed(servletContextEvent);
        WebApiEndpoint.contextDestroyed(servletContextEvent);
        PluginLoader.stopSystemPlugins();
    }

    public static void doContextInitialized(ServletContextEvent servletContextEvent) {
        WebApiConfig.setBuildVersion(getVersion(servletContextEvent.getServletContext()));

        // Set the gauge to a dummy value. This value will never change, we are only interested in the label values,
        // which are used to report information about the running software.
        Double dummyValue = 1.0;
        webapiInfoMetric.labels(WebApiConfig.getBuildVersion() != null ? WebApiConfig.getBuildVersion() :
                MetricsConstants.UNDEFINED_LABEL_VALUE).set(dummyValue);
    }

    public static void doContextDestroyed(ServletContextEvent servletContextEvent)
    {
        //Nothing, for now...
    }


    private static synchronized String getVersion(ServletContext application)
    {
        String version = null;

        // try to load from maven properties first
        try {
            InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");

            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);
                version = manifest.getMainAttributes().getValue("Implementation-Version");
            }

        } catch (Exception e) {
            // ignore
        }

        return version;
    }

}
