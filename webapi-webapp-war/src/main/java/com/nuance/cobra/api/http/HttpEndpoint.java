package com.nuance.cobra.api.http;

import com.nuance.cobra.plugin.PluginLoader;
import com.nuance.cobra.plugin.WebApiPlugin;
import com.nuance.cobra.servlet.WebApiPlatformServlet;
import com.nuance.cobra.session.AuthTypeAndData;
import com.nuance.cobra.session.SessionValidator;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.plugin.v1.IJwtValidationsConfigurator;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.shareable.IErrorCode;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nuance.cobra.utils.errors.RestPluginsError.*;

public class HttpEndpoint extends WebApiPlatformServlet {

    private static Logger logger = LogManager.getLogger(HttpEndpoint.class);
    private static org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(HttpEndpoint.class);

    class RequestFields
    {
        protected String tenant;
        protected String requestPath;

        public RequestFields(String url) {
            StringBuilder requestUrlPath = new StringBuilder(url);
            if (!url.startsWith("/")) {
                requestUrlPath.insert(0, "/");
            }

            requestUrlPath.deleteCharAt(0);
            tenant = requestUrlPath.substring(0, requestUrlPath.indexOf("/")); // /{tenant}/...
            requestUrlPath.delete(0, requestUrlPath.indexOf("/") + 1);
            requestPath = requestUrlPath.toString();
        }
    }

    private boolean callPlugin(String methodName, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        RequestFields fields = new RequestFields(request.getPathInfo());
        List<WebApiPlugin<IWebApiRawPlugin>> plugins = null;

        if (!WebApiConfigManager.validateTenantKey(fields.tenant)) {
            sendJsonErrorResponse(response, INVALID_TENANT_PATH_FORMAT);
            return false;
        }

        try {
            plugins = PluginLoader.loadRawPlugins(fields.tenant);
            request.setAttribute( MetricsConstants.TENANT_LABEL_ATTRIBUTE_KEY, fields.tenant);
        } catch(IOException ioe) {
            sendJsonErrorResponse(response, FAILED_TO_LOAD_TENANT_CONFIG);
            EsapiLogger.error(esapiLogger,"Tenant not found: %s", fields.tenant);
            return false;
        }

        for (WebApiPlugin<IWebApiRawPlugin> plugin: plugins) {
            if (fields.requestPath.matches(plugin.getPlugin().getUrlPattern())) {

                CombinedConfiguration config = null;
                try
                {
                     config = plugin.getPluginSettings();

                    if (!doAuthentication(config, request, plugin.getPlugin()
                                                                 .getPluginFeatures()
                                                                 .getJwtValidationsConfigurator()))
                    {
                        response.setHeader("WWW-Authenticate", "Basic");
                        sendJsonErrorResponse(response, UNAUTHORIZED);
                        return false;
                    }
                } finally
                {
                    CobraConfigurationUtils.clearCombinedConfiguration(config, null);
                }

                try {
                    boolean methodResponse = getMethodResponse(methodName, request, response, plugin);
                    if (methodResponse) {
                        return true;
                    }
                    else {
                        EsapiLogger.error(esapiLogger, "Method not implemented or processing failed: %s", request.getMethod());
                        sendJsonErrorResponse(response, METHOD_NOT_IMPLEMENTED);
                        return false;
                    }

                } catch (WebApiCommonException e) {
                    logger.error("Error while processing request: {}", e.getMessage());
                    sendJsonErrorResponse(response, e.getHttpStatusCode(), e.getErrorCodeValue());
                    return false;
                }
            }
        }

        EsapiLogger.error(esapiLogger, "No configured plugin matched request path: %s", fields.requestPath);
        sendJsonErrorResponse(response, REQUEST_PATH_NOT_FOUND);
        return false;
    }

    private boolean getMethodResponse(String methodName, HttpServletRequest request, HttpServletResponse response,
                                      WebApiPlugin<IWebApiRawPlugin> plugin)
            throws ServletException, IOException, WebApiCommonException {
        boolean methodResponse = false;

        switch (methodName) {
            case "onHttpGet":
                methodResponse = plugin.getPlugin().onHttpGet(request, response);
                break;
            case "onHttpPost":
                methodResponse = plugin.getPlugin().onHttpPost(request, response);
                break;
            case "onHttpPut":
                methodResponse = plugin.getPlugin().onHttpPut(request, response);
                break;
            case "onHttpDelete":
                methodResponse = plugin.getPlugin().onHttpDelete(request, response);
                break;
        }

        return methodResponse;
    }

    private boolean doAuthentication(CombinedConfiguration config, HttpServletRequest request,
            Optional<IJwtValidationsConfigurator> validationsConfigurator) {
        final String authorization = request.getHeader("Authorization");

        Map<String, Object> configMap = new HashMap<>();
        Map<String, Object> authData = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.no_auth.name());
        }
        else if (authorization.toLowerCase().startsWith("bearer"))
        {
            authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.name());
            authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, authorization.trim().split(" ")[1]);
        }
        else {
            authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.name());
            authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, authorization);
        }

        WebApiJSONConfiguration topConfig = (WebApiJSONConfiguration) CobraConfigurationUtils.getHighestConfig(config);
        topConfig.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        return (new SessionValidator(config, validationsConfigurator)).validate();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            callPlugin("onHttpGet", request, response);
        }
        catch(Exception e){
            quietErrorResponse(response, HttpServletResponse.SC_NOT_FOUND);
            logger.warn("There is an exception when executing onHttpGet", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            callPlugin("onHttpPost", request, response);
        }
        catch(Exception e){
            quietErrorResponse(response, HttpServletResponse.SC_NOT_FOUND);
            logger.warn("There is an exception when executing onHttpPost", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            callPlugin("onHttpPut", request, response);
        }
        catch(Exception e){
            quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            logger.warn("There is an exception when executing onHttpPut", e);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            callPlugin("onHttpDelete", request, response);
        }
        catch(Exception e){
            quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            logger.warn("There is an exception when executing onHttpDelete", e);
        }
    }

    public static void quietErrorResponse(HttpServletResponse response, int responseCode) {
        try {
            response.sendError(responseCode);
        } catch (IOException e) {
            logger.warn("Failed to send error response", e);
        }
    }

    static void sendJsonErrorResponse(HttpServletResponse response, int responseCode, int errorCode) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("errorCode", errorCode);
        response.setStatus(responseCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(obj.toString());
    }

    private static void sendJsonErrorResponse(HttpServletResponse response, IErrorCode e) throws IOException {
        sendJsonErrorResponse(response, e.getHttpStatusCode(), e.getValue());
    }
}
