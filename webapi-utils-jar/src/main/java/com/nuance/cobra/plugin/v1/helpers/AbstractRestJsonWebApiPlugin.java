package com.nuance.cobra.plugin.v1.helpers;

import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.plugin.v1.*;
import okhttp3.MediaType;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nuance.cobra.utils.errors.RestPluginsError.*;

/**
 * Abstract base class for Plugins implementing a REST Interface with a JSON body.
 */
public abstract class AbstractRestJsonWebApiPlugin implements IWebApiRawPlugin
{
    private static Logger logger = LogManager.getLogger(AbstractRestJsonWebApiPlugin.class);
    private boolean wasSuccessfullyInitialized = true;
    private final String servicePath;

    private static final String   MEDIA_TYPE_JSON_UTF8 = "application/json; charset=utf-8";


    protected AbstractRestJsonWebApiPlugin(String servicePath)
    {
        this.servicePath = servicePath;
    }

    public static class PluginInitException
            extends Exception
    {
        public PluginInitException(String message)
        {
            super(message);
        }
    }

    @Override
    public String getUrlPattern()
    {
        return "^" + servicePath + "/.*";
    }

    /**
     *  return the lable to identify the function provided by this plug in. This will be used in the metrics.
     */
    protected abstract String getFunctionLabelValue();

    /**
     * Validates the content-type of the request. If present it is expected to be application/json
     */
    protected void validateRequestContentType(javax.servlet.http.HttpServletRequest request)
            throws WebApiCommonException
    {
        String requestContentType = request.getContentType();

        if (requestContentType != null)
        {
            logger.debug("request content type: {}", requestContentType);
            MediaType contentType;
            try
            {
                contentType = MediaType.get(requestContentType);
            }
            catch (IllegalArgumentException e)
            {
                throw new WebApiCommonException(INVALID_CONTENT_TYPE, requestContentType);
            }
            logger.trace("parsed content type: {}", contentType);

            if (!(contentType.type().equals("application") && contentType.subtype().equals("json")))
            {
                final String APPL_JSON_CONTENT_TYPE = "application/json";
                throw new WebApiCommonException(UNSUPPORTED_CONTENT_TYPE, APPL_JSON_CONTENT_TYPE, requestContentType);
            }
        }
        else
        {
            logger.debug("content-type not specified in request, assuming application/json");
        }
    }


    /**
     * Returns the action (and query param if applicable) from the request (throws PluginException if the request
     * URL doesn't match the expected format.
     */
    protected String getAction(HttpServletRequest request)
            throws WebApiCommonException
    {
        // pathInfo is expected to contain /<tenantName>/<LOGGING_SERVICE_PATH>/command
        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith("/"))
        {
            pathInfo = pathInfo.substring(1);
        }

        String actionWithServicePath = pathInfo.substring(pathInfo.indexOf('/') + 1);
        if (!actionWithServicePath.startsWith(servicePath))
        {
            // Note: should not reach this point unless there is a problem or change in the plugin framework logic
            final String errorMsg = "Path info in URL should start with /tenantName_appName/" + servicePath;
            throw new WebApiCommonException(UNSUPPORTED_URI, errorMsg);
        }

        String action = actionWithServicePath.substring(servicePath.length() + 1);
        logger.debug("request action is: {}", action);
        request.setAttribute(MetricsConstants.ACTION_LABEL_ATTRIBUTE_KEY, action);

        return action;
    }

    /**
     * Read the content in to a string
     */
    protected String readContent(ServletRequest request)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), request.getCharacterEncoding()));

        return reader
                .lines()
                .collect(
                        Collectors.joining(
                                System.lineSeparator()));
    }


    /**
     * Check that the plugin was properly initialized, otherwise throws exception.
     */
    protected void checkPluginStatus()
            throws WebApiCommonException
    {
        if (!wasSuccessfullyInitialized)
        {
            throw new WebApiCommonException(PLUGIN_NOT_INITIALIZED);
        }
    }

    /**
     * Loads a parameter from the tenant config, throws exception if the parameter non-optional and not present
     * and sets the plugin status as not initialized properly in that case.
     */
    protected String loadConfigurationParameter(CombinedConfiguration tenantConfig, String paramName, boolean optional)
            throws PluginInitException
    {
        String paramValue = tenantConfig.getString(paramName);
        if (!optional && paramValue == null)
        {
            final String errorMsg = "Missing " + paramName + " in " + getClass().getName() + " plugin tenant config";
            logger.warn(errorMsg);
            wasSuccessfullyInitialized = false;
            throw new PluginInitException(errorMsg);
        }
        logger.debug("{} plug-in {}: {}", getClass().getSimpleName(), paramName, paramValue);
        return paramValue;
    }

    /**
     * Loads a parameter from the tenant config, throws exception if parameter is not present and sets the
     * plugin status as not initialized properly.
     */
    protected String loadConfigurationParameter(CombinedConfiguration tenantConfig, String paramName)
            throws PluginInitException
    {
        return loadConfigurationParameter(tenantConfig, paramName, false);
    }

    @Override
    public boolean onHttpPost(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
            throws IOException, WebApiCommonException {

        checkPluginStatus();
        request.setAttribute(MetricsConstants.FUNCTION_LABEL_ATTRIBUTE_KEY, getFunctionLabelValue());
        processPostRequest(request, response);

        return true;
    }

    protected abstract void processPostRequest(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
            throws WebApiCommonException, IOException;

    @Override
    public IPluginFeatures getPluginFeatures()
    {
        return new IPluginFeatures()
        {

            @Override
            public Optional<IJwtValidationsConfigurator> getJwtValidationsConfigurator()
            {
                return Optional.of(
                        new IJwtValidationsConfigurator()
                        {
                            @Override
                            public void configureBuilder(JwtConsumerBuilder claimsConsumerBuilder)
                            {
                                claimsConsumerBuilder.setExpectedAudience(getJwtAudience());
                            }
                        }
                );
            }
        };
    }


    protected abstract String getJwtAudience();
}
