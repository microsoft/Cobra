package com.nuance.cobra.client.mix;

import com.google.common.cache.*;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MixClientCredentialsManager
{
    private static final Logger logger = LogManager.getLogger(MixClientCredentialsManager.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(MixClientCredentialsManager.class);
    private static LoadingCache<String, MixAccessToken> mixClientCredentialsMixAccessTokenCache;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static ScheduledFuture<?> cleanupFuture = null;

    private MixClientCredentialsManager()
    {
        throw new IllegalStateException();
    }

    private static RemovalListener<String, MixAccessToken> initRemovalListener()
    {
        RemovalListener<String, MixAccessToken> removalListener = MixClientCredentialsManager::requeueRemovedItem;
        return RemovalListeners.asynchronous(removalListener, executorService);
    }

    static void requeueRemovedItem(RemovalNotification<String, MixAccessToken> removalNotification)
    {
        RemovalCause removalCause = removalNotification.getCause();
        String mixClientCredentialsString = removalNotification.getKey();
        MixClientCredentials mixClientCredentials = MixClientCredentials.deserialize(mixClientCredentialsString);
        String token = StringUtils.right(removalNotification.getValue().getAccessToken(), 20);

        if(removalCause != RemovalCause.EXPIRED) { return; }

        String log = String.format("Token has expired for %s ending in %s", mixClientCredentials.getMixClientAppId(), token);
        EsapiLogger.trace(esapiLogger, log);
        try
        {
            mixClientCredentialsMixAccessTokenCache.get(mixClientCredentialsString);
        }
        catch (ExecutionException e)
        {
            logger.error("Exception retrieving auth token from mix: {}", e.getMessage(), e);
            invalidate(mixClientCredentials);
        }
    }

    @java.lang.SuppressWarnings("java:S3740")
    public static void init()
    {
        int refreshSeconds = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_REFRESH_SECONDS.key);
        int expireSeconds = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_EXPIRE_SECONDS.key);

        CacheBuilder cacheBuilder = CacheBuilder.newBuilder()
                    .refreshAfterWrite(refreshSeconds, TimeUnit.SECONDS)
                    .expireAfterWrite(expireSeconds, TimeUnit.SECONDS);

        boolean idleRefreshOnExpire = WebApiConfigManager.getInstance().getBoolean(WebApiConfig.Property.MIX_IDLE_REFRESH_ON_EXPIRE.key);
        if (idleRefreshOnExpire)
        {
            RemovalListener<String, MixAccessToken> removalListener = initRemovalListener();
            cacheBuilder = cacheBuilder.removalListener(removalListener);

            Runnable task = new ThreadContextRunnable()
            {
                @Override
                public void runWithContext() {
                    mixClientCredentialsMixAccessTokenCache.cleanUp();
                }
            };
            cleanupFuture = ScheduledTaskUtils.scheduleWithFixedDelay(task, 5, 5, TimeUnit.SECONDS);
        }

        LoadingCache<String, MixAccessToken> cache = cacheBuilder.build(new MixCacheLoader(executorService));
        setMixClientCredentialsMixAccessTokenCache(cache);
    }

    public static void shutdown()
    {
        if (cleanupFuture == null) { return; }
        cleanupFuture.cancel(true);
    }


    static void setMixClientCredentialsMixAccessTokenCache(LoadingCache<String, MixAccessToken> cache)
    {
        mixClientCredentialsMixAccessTokenCache = cache;
    }

    public static String getBearerToken(MixClientCredentials mixClientCredentials) {
        MixAccessToken token = getMixAccessToken(mixClientCredentials);
        return token == null ? null : token.getAccessToken();
    }

    public static MixAccessToken getMixAccessToken(MixClientCredentials mixClientCredentials)
    {
        EsapiLogger.trace(esapiLogger, String.format("Entered getMixAccessToken for MixAppId: %s. MixAuthUri: %s", mixClientCredentials.getMixClientAppId(), mixClientCredentials.getMixAuthUri()));

        try
        {
            MixAccessToken rc = mixClientCredentialsMixAccessTokenCache.get(mixClientCredentials.serialize());
            if(rc == null) return null;
            if(Instant.now().isAfter(rc.getActualExpirationTime()))
            {
                EsapiLogger.error(esapiLogger, String.format("Access token for %s is expired. Invalidating and getting a new token.", mixClientCredentials.getMixClientAppId()));
                invalidate(mixClientCredentials);
                rc = mixClientCredentialsMixAccessTokenCache.get(mixClientCredentials.serialize());
                if(rc == null) return null;
            }
            if (logger.isTraceEnabled())
            {
                String log = String.format("Leaving getBearerToken for MixAppId: %s. Token tail: %s", mixClientCredentials.getMixClientAppId(),
                        StringUtils.right(rc.getAccessToken(), 20));
                EsapiLogger.trace(esapiLogger, log);
            }
            return rc;
        } catch (Exception e)
        {
            logger.error("Exception retrieving auth token from mix: {}", e.getMessage(), e);
            invalidate(mixClientCredentials);
            return null;
        }
    }

    public static void invalidate(MixClientCredentials mixClientCredentials)
    {
        mixClientCredentialsMixAccessTokenCache.invalidate(mixClientCredentials.serialize());
    }

    static MixAccessToken callAuthService(MixClientCredentials mixClientCredentials, boolean isReload)
            throws IOException
    {
        int timeout;
        if(isReload)
        {
            timeout = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.MIX_AUTH_ASYNC_TIMEOUT.key);
        } else
        {
            timeout = WebApiConfigManager.getInstance().getInt(WebApiConfig.Property.MIX_AUTH_SYNC_TIMEOUT.key);
        }

        return callAuthService(mixClientCredentials, timeout);
    }

    //Does the actual update and http request
    @java.lang.SuppressWarnings("squid:S135")
    static MixAccessToken callAuthService(MixClientCredentials mixClientCredentials, int timeout)
            throws IOException

    {
        String mixAuthUri = mixClientCredentials.getMixAuthUri();
        EsapiLogger.info(esapiLogger, String.format("Called auth service for MixAppId: %s. MixAuthUri: %s", mixClientCredentials.getMixClientAppId(), mixAuthUri));

        for(String clientSecret : mixClientCredentials.getMixClientSecrets())
        {
            String clientId = URLEncoder.encode(mixClientCredentials.getMixClientAppId(), StandardCharsets.UTF_8);
            String basicAuth = clientId + ":" + clientSecret;
            EsapiLogger.trace(esapiLogger, String.format("Attempting Mix auth (%s) with clientId = %s. clientSecret = %s", mixAuthUri, clientId, clientSecret));
            String basicAuthBase64 = Base64.getEncoder()
                                           .encodeToString(basicAuth.getBytes());

            String scope = mixClientCredentials.getMixClientScope();
            String form = mixClientCredentials.getMixClientForm() + scope;
            byte[] formBytes = form.getBytes(StandardCharsets.UTF_8);

            ByteArrayOutputStream responseBodyBaos = new ByteArrayOutputStream();
            AtomicInteger responseCode = new AtomicInteger(-3);
            httpPostWrapper(mixAuthUri, basicAuthBase64, formBytes, timeout, responseBodyBaos, responseCode);

            String responseBody = responseBodyBaos.toString(StandardCharsets.UTF_8);
            String mixAuthResponseToLog = String.format("Mix Auth (%s) Mix appID: %s Response Status = %d Body = %s",
                    mixAuthUri, mixClientCredentials.getMixClientAppId(), responseCode.get(), responseBody);
            if(responseCode.get() / 100 == 2)
            {
                EsapiLogger.trace(esapiLogger, mixAuthResponseToLog);
            } else
            {
                EsapiLogger.warning(esapiLogger, mixAuthResponseToLog);
                continue;
            }

            JSONObject authJson = getJsonResponseBody(responseBody);
            String accessToken = MixAccessToken.extractAccessToken(authJson);
            if (authJson == null || accessToken.isEmpty())
            {
                EsapiLogger.warning(esapiLogger, String.format("Mix Auth (%s) Has returned a 2xx response with a non-token body. Response Status = %d Body = %s", mixAuthUri, responseCode.get(), responseBody));
                continue;
            }

            int expiresIn = authJson.optInt("expires_in", 900);
            scope = authJson.optString("scope", scope);
            String tokenType = authJson.optString("token_type", "bearer");
            String abbreviatedToken = StringUtils.right(accessToken, 20);
            String log = String.format("Obtained auth token for MixAppId: %s. Expires in %d. Scope is %s. Token Type is %s. Token tail %s",
                    mixClientCredentials.getMixClientAppId(), expiresIn, scope, tokenType, abbreviatedToken);
            EsapiLogger.info(esapiLogger, log);

            return new MixAccessToken(authJson, Instant.now()
                                                        .plus(expiresIn - (long) 2, ChronoUnit.SECONDS));
        }
        throw new IOException("Failed to refresh token.");
    }
    private static JSONObject getJsonResponseBody(String responseBody)
    {
        try
        {
            return new JSONObject(responseBody);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    private static void httpPostWrapper(String mixAuthUri, String basicAuthBase64, byte[] formBytes, int timeout,
            ByteArrayOutputStream responseBodyBaos, AtomicInteger responseCode)
    {
        try
        {
            httpPost(mixAuthUri, basicAuthBase64, formBytes, timeout, responseBodyBaos, responseCode);
        }
        catch (Exception e)
        {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            EsapiLogger.error(esapiLogger, String.format("Exception while processing httpPost for Mix Auth (%s) ResponseCode: %d : %s. %s", mixAuthUri, responseCode.get(), e.getMessage(), stackTrace));
        }
    }


    @java.lang.SuppressWarnings({"squid:S2647", "java:S4087"})
    private static void httpPost(String mixAuthUri, String basicAuthBase64, byte[] formBytes, int timeout,
            ByteArrayOutputStream responseBodyBaos, AtomicInteger responseCode)
            throws IOException, URISyntaxException
    {
        URIBuilder uriBuilder = new URIBuilder(mixAuthUri);
        List<NameValuePair> queryParams = uriBuilder.getQueryParams();
        uriBuilder.removeQuery();
        URL url = uriBuilder.build().toURL();

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(); //Doesn't open the connection
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpURLConnection.setRequestProperty("Authorization", "Basic " + basicAuthBase64);
        httpURLConnection.setRequestProperty("Content-Length", Integer.toString(formBytes.length));
        populateMixAuthUri(httpURLConnection, queryParams);
        httpURLConnection.setConnectTimeout(timeout);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setInstanceFollowRedirects(true);

        try (DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream()))
        {
            dataOutputStream.write(formBytes);
            dataOutputStream.close();

            //Response handling

            responseCode.set(httpURLConnection.getResponseCode());
            if (responseCode.get() == -1)
            {
                EsapiLogger.warning(esapiLogger, String.format("Mix Auth Response Status = -1 (Non-HTTP). Please check %s for validity.", mixAuthUri));
                return;
            }

            getResponseBody(httpURLConnection, responseBodyBaos);
        }
        finally
        {
            httpURLConnection.disconnect();
        }
    }

    private static void populateMixAuthUri(HttpURLConnection httpURLConnection, List<NameValuePair> queryParams)
    {
        for(NameValuePair nameValuePair : queryParams)
        {
            if(nameValuePair.getName().equals("authUri"))
            {
                httpURLConnection.setRequestProperty("Mix-Auth-Uri", nameValuePair.getValue());
            }
        }
    }

    private static void getResponseBody(HttpURLConnection httpURLConnection, ByteArrayOutputStream responseBodyBaos)
    {
        try
        {
            InputStream resultStream = httpURLConnection.getErrorStream();
            if (resultStream == null)
            {
                resultStream = httpURLConnection.getInputStream();
            }

            responseBodyBaos.writeBytes(resultStream.readAllBytes());
        } catch (IOException e)
        {
            logger.error("Exception while reading Mix Auth http response body: {}", e.getMessage(), e);
        }
    }

}
