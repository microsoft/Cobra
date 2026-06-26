package com.nuance.cobra.client.globalregistry;

import com.nuance.cobra.api.globalregistry.Company;
import com.nuance.cobra.api.globalregistry.adapter.GlobalRegistryJSONParserAdapter;
import com.nuance.cobra.api.globalregistry.parser.CompanyJSONParser;
import com.nuance.cobra.api.response.InternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.owasp.esapi.ESAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Singleton GlobalRegistry client
 */
public class GlobalRegistryClient
        implements Runnable
{
    private static final Logger logger = LogManager.getLogger(GlobalRegistryClient.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(GlobalRegistryClient.class);
    private String lastModified = null;
    private Map<String, Company> companies = new ConcurrentHashMap<String, Company>();
    private static GlobalRegistryClient globalRegClient;
    //Latch for initialization of global registry in-memory cache.
    private CountDownLatch latch = new CountDownLatch(1);

    private ResponseErrorHandler errorHandler = null;

    private GlobalRegistryClient()
    {

    }

    private ResponseErrorHandler getErrorHandler()
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }
    /**
     * Creates singleton instance of GlobalRegistryClient
     *
     * @return Singleton instance of GlobalRegistryClient
     */
    public static GlobalRegistryClient getInstance()
    {
        if (globalRegClient == null)
        {
            initializeObject();
        }
        return globalRegClient;
    }

    private static synchronized void initializeObject() {
        if (globalRegClient == null)
        {
            globalRegClient = new GlobalRegistryClient();
        }
    }

    public void init()
    {
        /**
         * This invokes global registry fetch operation at every configured interval. Here the
         * "GLOBAL_REGISTRY_FETCH_INTERVAL_IN_MINUTES" is the fixed delay between termination of one execution
         * and the commencement of the next.
         */
        ScheduledTaskUtils.scheduleWithFixedDelay(GlobalRegistryClient.getInstance(), 0, WebApiConfigManager.getInstance()
                                                                                                            .getInt(
                                                                                                   WebApiConfig.Property.GLOBAL_REGISTRY_FETCH_INTERVAL_SECONDS.key),
                TimeUnit.SECONDS);
    }

    /**
     * Getter method for Company map
     *
     * @return Company map
     */
    public Map<String, Company> getCompanies()

    {
        try
        {
            logger.info("Waiting for initialization of global registry");
            if(!latch.await(WebApiConfigManager.getInstance()
            .getInt(WebApiConfig.Property.GLOBAL_REGISTRY_FETCH_WAIT_TIME_MILLISECONDS.key), TimeUnit.MILLISECONDS)){
                logger.info("GlobalRegistryClient was interrupted while waiting for global registry fetch");
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted while waiting for global registry fetch: {}", Thread.currentThread().getName(), e);
        }
        return companies;
    }

    public String getHostAndPort()
    {
        CombinedConfiguration serverConfig = WebApiConfigManager.getInstance();

        String globalRegistryScheme = serverConfig.getString(WebApiConfig.Property.GLOBAL_REGISTRY_SCHEME.key);
        String globalRegistryHostName = serverConfig.getString(WebApiConfig.Property.GLOBAL_REGISTRY_HOST.key);
        String globalRegistryPortNumber = serverConfig.getString(WebApiConfig.Property.GLOBAL_REGISTRY_PORT.key);

        return globalRegistryScheme + globalRegistryHostName + ":" + globalRegistryPortNumber;
    }

    /**
     * Makes http connection to fetch global registry.
     * In case of success response, it parses the global registry json file and stores it in in-memory cache.
     * In case of HTTP NOT MODIFIED response, it does nothing.
     * In case of error response, it just logs the error and returns.
     */
    private void fetchGlobalRegistry()
    {
        BufferedReader globalRegistryReader = null;
        try
        {
            CombinedConfiguration serverConfig = WebApiConfigManager.getInstance();

            String globalRegistryHost = serverConfig.getString(WebApiConfig.Property.GLOBAL_REGISTRY_HOST.key);
            if(globalRegistryHost == null)
            {
                return;
            }

            String globalRegistryPath = serverConfig.getString(WebApiConfig.Property.GLOBAL_REGISTRY_PATH.key);

            URL globalRegistryURL = new URL(getHostAndPort() + globalRegistryPath);
            HttpURLConnection httpConnection = (HttpURLConnection) globalRegistryURL.openConnection();
            httpConnection.setConnectTimeout(WebApiConfigManager.getInstance()
                                                         .getInt(
                                                                 WebApiConfig.Property.GLOBAL_REGISTRY_CONNECTION_TIMEOUT_MILLISECONDS.key));
            httpConnection.setReadTimeout(WebApiConfigManager.getInstance()
                                                      .getInt(WebApiConfig.Property.GLOBAL_REGISTRY_READ_TIMEOUT_MILLISECONDS.key));

            //Set If-Modified-Since property
            if (lastModified != null)
            {
                httpConnection.setRequestProperty("If-Modified-Since", lastModified);
            }
            int statusCode = httpConnection.getResponseCode();

            if (statusCode == HttpURLConnection.HTTP_OK)
            { // In case of HTTP_OK(200) response, read global registry http response. Populate in-memory cache by parsing global registry json file.
                globalRegistryReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                String globalRegInputLine;
                StringBuffer globalRegistryResponse = new StringBuffer();

                while ((globalRegInputLine = globalRegistryReader.readLine()) != null)
                {
                    globalRegistryResponse.append(globalRegInputLine);
                }
                globalRegistryReader.close();

                //Save last modified timestamp which will be set as If-Modified-Since property for next request.
                lastModified = httpConnection.getHeaderField("Last-Modified");
                EsapiLogger.debug(esapiLogger, "GlobalRegistry last modified timestamp: %s", lastModified);

                //Parse globalRegistry.json and store it in in-memory cache
                JSONObject responseJsonObj = new JSONObject(new JSONTokener(globalRegistryResponse.toString()));
                JSONObject globalRegJsonObj = responseJsonObj.getJSONObject(WebApiConfigManager.getInstance()
                                                                                        .getString(
                                                                                                WebApiConfig.Property.GLOBAL_REGISTRY_ROOT.key));

                for (String companyName : globalRegJsonObj.keySet())
                {
                    EsapiLogger.debug(esapiLogger,"Company Name: %s", companyName);
                    Object companyObj = globalRegJsonObj.get(companyName);
                    if (companyObj instanceof JSONObject)
                    {
                        GlobalRegistryJSONParserAdapter<Company> adapter = new GlobalRegistryJSONParserAdapter<Company>(
                                new CompanyJSONParser());
                        Company company = adapter.parseJSON((JSONObject) companyObj);
                        companies.put(companyName, company);
                    }
                }
            }
            else if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED)
            { //In case of HTTP Not Modified(304) response, do nothing. This response code means that global registry is not modified since last fetch, so no need to transmit and update the data.
                EsapiLogger.info(esapiLogger, "GlobalRegistry not modified since last fetch: %s", lastModified);
            }
            else
            {
                //In case of any other response code, log the error and send SNMP Trap
                EsapiLogger.error(esapiLogger, "GET request for GlobalRegistry failed, try again! Error response code: %d %s", statusCode, httpConnection.getResponseMessage());
                ResponseResult responseResult = new ResponseResult(
                        ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_HTTP_RESPONSE_ERROR, Integer.toString(statusCode), httpConnection.getResponseMessage()));
                getErrorHandler().handleError(responseResult);
            }
        }
        catch (MalformedURLException mue)
        {
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_URL_MALFORMED));
            getErrorHandler().handleError(responseResult);
        }
        catch (IOException ioe)
        {
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_CANNOT_BE_FETCHED));
            getErrorHandler().handleError(responseResult);
        }
        catch (JSONException jse)
        {
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_CANNOT_BE_PARSED));
            getErrorHandler().handleError(responseResult);
        }
        catch (Exception e)
        {
            ResponseResult responseResult = new ResponseResult(
                    ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_UNKNOWN_EXCEPTION));
            getErrorHandler().handleError(responseResult);
        }
        finally
        {
            if(globalRegistryReader != null)
            {
                try
                {
                    globalRegistryReader.close();
                }
                catch (IOException e)
                {
                    logger.warn("IOException while trying to close globalRegistryReader. Possible leak.");
                }
            }
            
            /**
             * Call countDown on latch when global registry initialization is invoked.
             * This ensures that this fetch method is invoked before any request comes up for NES grammar URI
             */
            if (latch.getCount() == 1)
            {
                logger.debug("countdown latch called: " + latch.getCount());
                latch.countDown();
            }
        }
    }

    @Override
    public void run()
    {
        fetchGlobalRegistry();
    }

}
