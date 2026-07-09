package com.nuance.cobra.utils;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.http.CachingHttpClient;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import java.io.InputStream;
import java.util.List;

public class TenantHttpClient
{
    private static Logger logger = LogManager.getLogger(TenantHttpClient.class);
    private static org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(TenantHttpClient.class);

    private TenantHttpClient()
    {
        throw new IllegalStateException("Utility class");
    }

    public static WebApiJSONConfiguration getConfigForTenant(String tenantKey) throws CobraException
    {
        CombinedConfiguration webapiServerConfig = WebApiConfigManager.getInstance();

        String urlPrefix = webapiServerConfig.getString(WebApiConfig.Property.TENANT_CONFIG_URI_PREFIX.key);
        if (!urlPrefix.endsWith("/"))
        {
            urlPrefix += "/";
        }

        List<String> extensions = webapiServerConfig.getList(String.class, WebApiConfig.Property.TENANT_CONFIG_SUFFIX_SEARCH_ORDER.key);

        WebApiJSONConfiguration rc = null;

        for (String extension : extensions)
        {
            String uri = urlPrefix;
                   uri += tenantKey; //Written this way for sonarqube analysis
                   uri += extension;
            EsapiLogger.debug(esapiLogger, "Attempting to fetch tenant config from %s", uri);

            try
            {
                CloseableHttpResponse response = CachingHttpClient.getUriSynchronously(uri);
                if (response == null)
                {
                    continue;
                }

                int statusCode = response.getStatusLine().getStatusCode();
                EsapiLogger.debug(esapiLogger, "Status Code %d for URI %s", statusCode, uri);
                if (statusCode / 100 != 2)
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                    continue;
                }

                rc = getCobraConfigurationFromInputStream(extension, response.getEntity().getContent());
                EntityUtils.consume(response.getEntity());
                response.close();
            }
            catch (Exception e)
            {
                String log = String.format("Caught exception while attempting to fetch tenant config from %s: ", uri);
                EsapiLogger.warning(esapiLogger, log, e);
                if(e instanceof InterruptedException) Thread.currentThread().interrupt();
                continue; //Try another config, if there is one.
            }
            return rc;
        }
        throw new CobraException(
                ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, urlPrefix + tenantKey));
    }

    private static WebApiJSONConfiguration getCobraConfigurationFromInputStream(String extension, InputStream is)
            throws ConfigurationException
    {
        if (extension.equals(".json"))
        {
            WebApiJSONConfiguration jsonConfiguration = new WebApiJSONConfiguration();
            jsonConfiguration.read(is);
            return jsonConfiguration;
        }
        return null;
    }
}
