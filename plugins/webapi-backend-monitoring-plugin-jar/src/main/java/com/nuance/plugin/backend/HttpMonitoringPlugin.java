package com.nuance.plugin.backend;

import com.nuance.plugin.backend.response.MonitoringResponse;
import com.nuance.plugin.backend.response.ServiceResponse;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.*;

public class HttpMonitoringPlugin implements IWebApiRawPlugin {
    private static Logger logger = LogManager.getLogger(HttpMonitoringPlugin.class);
    public static final String FORMAT_VERSION = "1.0";
    private IWebApiFeatures webApiFeatures;

    @Override
    public void onInitPlugin(IWebApiFeatures featuresList) {
        this.webApiFeatures = featuresList;
    }

    @Override
    public String getUrlPattern() {
        return "^monitoring.*";
    }

    /**
     * Called on an HTTP get.
     * @param request The Http request object.
     * @param response The object to be used to send the response.
     */
    public boolean onHttpGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String trackingID = java.util.UUID.randomUUID().toString();
        String status = "success";

        CombinedConfiguration tenantConfigs = webApiFeatures.getConfigs();
        HierarchicalConfiguration<ImmutableNode> backendMap;

        try
        {
            backendMap = tenantConfigs.configurationAt("backendServicesMap");
        } catch(Exception e)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        Map<String, ServiceResponse> services = new HashMap<>();

        Set<String> uniqueKeys = extractRootKeys(backendMap);
        for (String key: uniqueKeys) {
            ServiceResponse serviceStatus = new ServiceResponse();
            try {
                URI uri = new URI("internalProtocol://" + backendMap.getString(key));
                SocketConnectHealthCheckClient checker = new SocketConnectHealthCheckClient(uri.getHost(), uri.getPort());
                checker.checkHealth();
                serviceStatus.setStatus("UP");
            } catch (Exception e) {
                serviceStatus.setStatus("DOWN");
                serviceStatus.setStatistics(e.getMessage());
                status = "fail";
                logger.error("Health-check " + trackingID, e);
            }
            services.put(key, serviceStatus);
        }

        MonitoringResponse monitoringResponse = new MonitoringResponse(trackingID);
        monitoringResponse.setServices(services);
        monitoringResponse.setStatisticsHostInfo(tenantConfigs.getString("hostRegion"), tenantConfigs.getString("hostEnvironment"), tenantConfigs.getString("apiVersion"));
        monitoringResponse.setStatus(status);

        response.reset();
        response.setStatus(HttpServletResponse.SC_OK);

        response.setContentType(String.format("application/vnd.nuance.healthcheck.%s+json", FORMAT_VERSION));
        PrintWriter out = response.getWriter();
        out.println(monitoringResponse.toJson());
        out.flush();

        return true;
    }

    static Set<String> extractRootKeys(HierarchicalConfiguration<ImmutableNode> config) {
        Iterator<String> keys = config.getKeys();
        Set<String> uniqueKeys = new HashSet<>();

        while (keys.hasNext()) {
            uniqueKeys.add(keys.next().split("\\.")[0]);
        }

        return uniqueKeys;
    }
}
