package com.nuance.cobra.utils.http;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("java:S125")
public final class CachingHttpClient
{
    private static Logger logger = LogManager.getLogger(CachingHttpClient.class);
    private static org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(CachingHttpClient.class);

    private static CacheConfig cacheConfig;

    private static RequestConfig requestConfig;

    private static PoolingHttpClientConnectionManager httpManager = new PoolingHttpClientConnectionManager();

    private static CloseableHttpClient httpCachingClient;

    private static int maxPoolSize;

    private static Semaphore semaphore;

    private static ScheduledFuture<?> closeConnectionsFuture;

    static
    {
        CombinedConfiguration webapiServerConfig = WebApiConfigManager.getInstance();
        maxPoolSize = webapiServerConfig.getInt(WebApiConfig.Property.CACHING_HTTP_CLIENT_CONNECTION_POOL_SIZE.key);
        int maxCacheEntries = webapiServerConfig.getInt(WebApiConfig.Property.CACHING_HTTP_CLIENT_MAX_CACHE_ENTRIES.key);
        int maxObjectSize = webapiServerConfig.getInt(WebApiConfig.Property.CACHING_HTTP_CLIENT_CACHE_MAX_OBJECT_SIZE_BYTES.key);
        int connectTimeout = webapiServerConfig.getInt(WebApiConfig.Property.CACHING_HTTP_CLIENT_FETCH_CONNECT_TIMEOUT_MS.key);
        int socketTimeout = webapiServerConfig.getInt(WebApiConfig.Property.CACHING_HTTP_CLIENT_FETCH_SOCKET_TIMEOUT_MS.key);

        httpManager.setMaxTotal(maxPoolSize);
        httpManager.setDefaultMaxPerRoute(maxPoolSize);

        cacheConfig = CacheConfig.custom()
                                 .setMaxCacheEntries(maxCacheEntries)
                                 .setMaxObjectSize(maxObjectSize)
                                 .build();

        requestConfig = RequestConfig.custom()
                                     .setConnectTimeout(connectTimeout)
                                     .setSocketTimeout(socketTimeout)
                                     .build();

        semaphore = new Semaphore(maxPoolSize);

        try
        {
            resetCachingClient();
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }

        Runnable task = new ThreadContextRunnable() {
            @Override
            public void runWithContext() {
                closeConnections(httpManager);
            }
        };
        closeConnectionsFuture = ScheduledTaskUtils.scheduleWithFixedDelay(task, 30, 5, TimeUnit.SECONDS);
    }

    static void closeConnections(PoolingHttpClientConnectionManager httpManager)
    {
        httpManager.closeExpiredConnections();
        httpManager.closeIdleConnections(30, TimeUnit.SECONDS);
    }

    public static void shutdown()
    {
        closeConnectionsFuture.cancel(true);
    }


    private CachingHttpClient() { }

    public static void resetCachingClient() throws InterruptedException
    {
        semaphore.acquire(maxPoolSize);
        httpCachingClient = CachingHttpClients.custom()
                                              .setCacheConfig(cacheConfig)
                                              .setDefaultRequestConfig(requestConfig)
                                              .setConnectionManager(httpManager)
                                              .setConnectionManagerShared(true)
                                              .disableRedirectHandling()
                                              .build();
        semaphore.release(maxPoolSize);
    }

    //Please make sure to:
    // 1) Fully consume the entity
    //    e.g. EntityUtils.consume(response.getEntity());
    // 2) Close the response when done:
    //    e.g. response.close()
    //
    //Synchronous fetches with this caching client should only be used when the content
    //is expected to be allowed to be cached most of the time (high % of CACHE_HIT)
    public static CloseableHttpResponse getUriSynchronously(String uri)
            throws IOException, InterruptedException
    {
        HttpCacheContext httpCacheContext = HttpCacheContext.create();

        if(UrlUtils.isSSRF(uri))
        {
            throw new IOException("URI: " + uri + "did not pass SSRF validation!");
        }

        HttpGet httpget = new HttpGet(uri);

        CloseableHttpResponse response;
        semaphore.acquire();
        try
        {
            response = httpCachingClient.execute(httpget, httpCacheContext);
        } finally
        {
            semaphore.release();
        }

        CacheResponseStatus cacheResponseStatus = httpCacheContext.getCacheResponseStatus();

        String cacheResponseStatusString = cacheResponseStatus.toString();
        EsapiLogger.debug(esapiLogger, "Cache Response Status for %s: %s", uri, cacheResponseStatusString);

        return response;
    }

    public static PoolingHttpClientConnectionManager getHttpManager()
    {
        return httpManager;
    }
}
