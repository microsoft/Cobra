package com.nuance.cobra.client.mix;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.MaxSizeHashMap;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import java.util.*;

public class MixUtils
{
    public static final String HEADER_X_ENVOY_RATELIMITED = "x-envoy-ratelimited";
    public static final boolean HEADER_X_ENVOY_RATELIMITED_DEFAULT = false;
    private static final Logger logger = LogManager.getLogger(MixUtils.class);

    private MixUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    public static void handleMixThrowable(Logger logger, String className, String methodName, Throwable e)
    {
        if (e == null)
        {
            logger.error("Throwable from Mix was null in {}.{}()", className, methodName);
            return;
        }
        logger.error("Mix client failed with exception", e);
    }

    public static void invalidateTokenIfUnauthorized(Logger logger, Throwable e, CombinedConfiguration config)
    {
        invalidateTokenIfUnauthorized(logger, e, mixClientCredentialsFromConfig(config));
    }

    public static void invalidateTokenIfUnauthorized(Logger logger, Throwable e, MixClientCredentials mixClientCredentials)
    {
        org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(logger.getClass());

        String message = e.getMessage();
        if(message == null) return;

        String appId = mixClientCredentials.getMixClientAppId();

        if(message.contains("UNAUTHENTICATED") ||
           message.contains("\"code\":401") ||
           message.contains("\"status\":\"Unauthorized\""))
        {
            EsapiLogger.error(esapiLogger, String.format("Invalidating Mix auth token for %s due to unauthorized message: %s", appId, message));
            MixClientCredentialsManager.invalidate(mixClientCredentials);
        }
    }

    public static MixClientCredentials mixClientCredentialsFromConfig(CombinedConfiguration cc)
    {
        var mixAppId = cc.getString(WebApiConfig.Property.MIX_CLIENT_ID.key);
        List<String> mixClientSecrets = cc.getList(String.class, WebApiConfig.Property.MIX_CLIENT_SECRETS.key);
        var mixScope = cc.getString(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_SCOPE.key);
        var mixForm = cc.getString(WebApiConfig.Property.MIX_CLIENT_CREDENTIALS_FORM.key);
        var mixAuthUri = cc.getString(WebApiConfig.Property.MIX_AUTH_URI.key);
        return new MixClientCredentials(mixAppId, mixClientSecrets, mixScope, mixForm, mixAuthUri);
    }

    public static boolean shouldAddUserId(CombinedConfiguration config, WebApiConfig.Property addUserIdProperty, Logger logger)
    {
        if (config.getBoolean(addUserIdProperty.key, (boolean)addUserIdProperty.defaultValue))
        {
            if (config.getBoolean(WebApiConfig.Property.APPEND_SUFFIX_TO_USERID.key))
            {
                logger.warn("Not adding user_id due to {} = true", WebApiConfig.Property.APPEND_SUFFIX_TO_USERID.key);
                return false;
            }
            return true;
        }
        return false;
    }

    private static void setMixDiscussedClientData(MaxSizeHashMap<String, String> clientData, String sessionId, CombinedConfiguration config)
    {
        //x-nuance-dialog-session-id
        putIfNotDefault("x-nuance-dialog-session-id", WebApiConfig.Property.CLIENT_SESSION_ID, clientData, config);

        //x-nuance-client-id
        putIfNotDefault("x-nuance-client-id", WebApiConfig.Property.APPLICATION_KEY, clientData, config);
        putIfNotDefault("x-nuance-client-id", WebApiConfig.Property.MIX_CLIENT_ID, clientData, config);

        //user_id
        putIfNotDefault("user_id", WebApiConfig.Property.CLIENT_USER_ID, clientData, config);

        //x-nuance-request-id as sessionId-commandId
        var currentSession = SessionManager.getInstance().getSession(sessionId, true);
        if(currentSession != null)
        {
            String sessionIdToUse = sessionId;
            if(!isDefaultValue(WebApiConfig.Property.CLIENT_SESSION_ID, config))
            {
                sessionIdToUse = config.getString(WebApiConfig.Property.CLIENT_SESSION_ID.key, sessionId);
            }
            String xNuanceRequestId = String.format("%s-%s", sessionIdToUse, currentSession.getCommandId());
            clientData.putWithLimit("x-nuance-request-id", xNuanceRequestId);
        }
    }

    private static void putIfNotDefault(String paramName, WebApiConfig.Property property, MaxSizeHashMap<String, String> clientData,
            CombinedConfiguration config)
    {
        if(!isDefaultValue(property, config))
        {
            clientData.putWithLimit(paramName, config.getString(property.key));
        }
    }

    private static boolean isDefaultValue(WebApiConfig.Property property, CombinedConfiguration config)
    {
        var defaultValue = (String) property.defaultValue;
        var value = config.getString(property.key);
        return value.equals(defaultValue);
    }

    private static MaxSizeHashMap<String, String> newMaxSizeHashMap(CombinedConfiguration config)
    {
        int maxSize = config.getInt(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.key, (int)WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.defaultValue);
        int maxStringLength = config.getInt(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_STRING_LENGTH.key, (int)WebApiConfig.Property.MIX_MAX_CLIENT_DATA_STRING_LENGTH.defaultValue);
        return new MaxSizeHashMap<>(maxSize, maxStringLength);
    }

    public static ImmutableMap<String, String> getClientDataMap(String sessionId, CombinedConfiguration config, Map<String, String> additionalItems)
    {
        MaxSizeHashMap<String, String> clientData = newMaxSizeHashMap(config);

        clientData.putAll(getClientDataMap(sessionId, config));
        for(Map.Entry<String, String> entry : additionalItems.entrySet())
        {
            clientData.putWithLimit(entry.getKey(), entry.getValue());
        }
        return ImmutableMap.copyOf(clientData);
    }

    public static ImmutableMap<String, String> getClientDataMap(String sessionId, CombinedConfiguration config)
    {
        MaxSizeHashMap<String, String> clientData = newMaxSizeHashMap(config);

        HierarchicalConfiguration<ImmutableNode> clientDetailsConfig = CobraConfigurationUtils.configurationAtOrNull(config, WebApiConfig.Property.CLIENT_DETAILS.key);
        Map<String, Object> clientDetailsMap = CobraConfigurationUtils.hierarchicalConfigurationToMap(clientDetailsConfig);

        var currentSession = SessionManager.getInstance().getSession(sessionId, true);

        if (clientDetailsMap != null) {
            // this is a bit of a hack for GENESIS/NEAP. They want clientGUID field to show up at a higher level in the userdata structure.
            for (Map.Entry<String, Object> key: clientDetailsMap.entrySet()) {
                clientData.putWithLimit(key.getKey(), key.getValue().toString());
            }

            var clientDetailsJson = new JSONObject(clientDetailsMap).toString();
            clientData.putWithLimit(WebApiConfig.Property.CLIENT_DETAILS.key, clientDetailsJson);
        }

        setMixDiscussedClientData(clientData, sessionId, config);
        if(currentSession != null) clientData.putWithLimit("x-nuance-dialog-seqid", Integer.toString(currentSession.getStepId()));

        clientData.putWithLimit(WebApiConfig.Property.APPLICATION_KEY.key, config.getString(WebApiConfig.Property.APPLICATION_KEY.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_USER_ID.key, config.getString(WebApiConfig.Property.CLIENT_USER_ID.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_SESSION_ID.key, config.getString(WebApiConfig.Property.CLIENT_SESSION_ID.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_DEVICE_ID.key, config.getString(WebApiConfig.Property.CLIENT_DEVICE_ID.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_OS_NAME.key, config.getString(WebApiConfig.Property.CLIENT_OS_NAME.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_OS_VERSION.key, config.getString(WebApiConfig.Property.CLIENT_OS_VERSION.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_CARRIER.key, config.getString(WebApiConfig.Property.CLIENT_CARRIER.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_PHONE_MODEL.key, config.getString(WebApiConfig.Property.CLIENT_PHONE_MODEL.key));
        clientData.putWithLimit(WebApiConfig.Property.CLIENT_SUBSCRIBER_ID.key, config.getString(WebApiConfig.Property.CLIENT_SUBSCRIBER_ID.key));
        clientData.putWithLimit("webapiSessionId", sessionId);

        Map<String, String> tenantData = new HashMap<>();
        try {
            HierarchicalConfiguration<ImmutableNode> neapHeadersConfig = config.configurationAt(WebApiConfig.Property.REPORTING_TENANT_DATA.getKey());

            // Adds the neapHeaders found in tenant config to the tenantData field.
            for (Iterator<String> it = neapHeadersConfig.getKeys(); it.hasNext(); ) {
                String key = it.next();
                tenantData.put(key, neapHeadersConfig.getString(key));
            }
            clientData.putWithLimit("customerName", tenantData.get("customerName"));
        } catch (ConfigurationRuntimeException e) {
            // exception intentionally not handled, the NEAP_HEADERS element is optional.
        }

        clientData.putWithLimit("tenant", config.getString(WebApiConfig.Property.APPLICATION_KEY.key));
        if(currentSession != null) {
            clientData.putWithLimit("command", currentSession.getCommand().getCommandName());
            clientData.putWithLimit("sessionStartTime", currentSession.getStartSessionTimeString());
            clientData.putWithLimit("audioBlobPath", currentSession.makeAudioBlobPath());
            String audioLogKey = currentSession.getAudioLogKey();
            if (audioLogKey != null) {
                clientData.putWithLimit("audioLogKey", audioLogKey);
            }
        }

        clientData.entrySet().removeIf(entry -> entry.getValue() == null);

        return ImmutableMap.copyOf(clientData);
    }

    public static void checkIfRateLimited(Map grpcCallHeaders, Logger logger)
    {
        if (MapUtils.getBooleanValue(grpcCallHeaders, HEADER_X_ENVOY_RATELIMITED, HEADER_X_ENVOY_RATELIMITED_DEFAULT)) {
            logger.error("Engine is rate limited (x-envoy-ratelimited: true)");
        }
    }
}
