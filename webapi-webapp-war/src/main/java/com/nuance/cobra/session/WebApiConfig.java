package com.nuance.cobra.session;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.client.InterpretationClients;
import com.nuance.cobra.client.SpeechRecognitionClients;
import com.nuance.cobra.client.UserResetClients;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.azure.AzureASRClient;
import com.nuance.cobra.client.mix.krypton.KryptonGrpcClient;
import com.nuance.cobra.client.mix.nvc.NvcClient;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.command.CommandChain;
import com.nuance.cobra.utils.audio.SpeechDetectorTypes;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.nuance.cobra.client.mix.nlu.NluAasGrpcParams.InputLoggingMode;
import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.EDF_V1_LOG_FORMAT_NAME;
import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.NEAP_V1_LOG_FORMAT_NAME;

/**
 * Master config class. Can be used to maintain default values for variables when it's desired.
 */
public class WebApiConfig
{
    private static String buildVersion = null;
    private static final Logger logger = LogManager.getLogger();

    private static StringWriter stringWriter = new StringWriter();
    private static JsonGenerator jsonGenerator = null;

    static
    {
        try
        {
            jsonGenerator = new JsonFactory().createGenerator(stringWriter);
            jsonGenerator.writeStartObject();
        }
        catch (IOException e)
        {
            logger.error("Could not create JsonGenerator in WebApiConfig!");
        }
    }

    public static String getDefaultsAsJson()
    {
        if (!jsonGenerator.isClosed())
        {
            try
            {
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
            }
            catch (IOException e)
            {
                logger.error("Could not close JsonGenerator in WebApiConfig");
            }
        }

        stringWriter.flush();
        return stringWriter.toString();
    }

    // We should eventually provide a description for these parameters instead. The annotations Undocumented or
    // internal ensure they are not exposed in the documentation.
    public static final String UNDOCUMENTED_PARAMETER = "undocumented";
    public static final String NOT_DEFINED = "notDefined";
    public static final String MIX_DOC_RECO_PARAMS = "See https://docs.mix.nuance.com/asr-grpc/v1/#recognitionparameters";
    public static final String MIX_DOC_RECO_FLAGS = "See https://docs.mix.nuance.com/asr-grpc/v1/#recognitionflags";
    public static final String PCM_16_8K = "pcm_16_8k";

    public static String getBuildVersion()
    {
        return buildVersion;
    }

    public static void setBuildVersion(String buildVersion)
    {
        WebApiConfig.buildVersion = buildVersion;
        logger.info("Running WebAPI Version: {}", buildVersion);
    }

    public enum Property implements IConfigProperty
    {
        // FIXME prevent setting  =some) params in tenantConfig  =such as applicationKey verificationCode)

        @Internal
        API_VERSION ("apiVersion", "2.1.0", UNDOCUMENTED_PARAMETER), //Set by maven at build time. Shouldn't be set by config.

        //***
        //*** Server/webapi.json params
        //***

        // Server params - webapi.json
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        SESSION_CLEAN_UP_SLEEP_TIME_MILLISECONDS ("sessionCleanUpSleepTimeMilliseconds", 60000, "Webapi session cleanup sweep interval (in milliseconds). Webapi max duration and inactive sessions are only cleaned up when a sweep occurs."), // 1 minute
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        SESSION_CLEAN_UP_ITERATIONS_BEFORE_OUTPUT_STATS ("sessionCleanUpIterationsBeforeOutputStats", 60, "Number of session cleanup iterations before outputting session counts and JVM stats. For memory tracking purposes."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        SOCKET_CONNECT_HEALTH_CHECK_TIMEOUT_MILLISECONDS ("socketConnectHealthCheckTimeoutMilliseconds", 2500, "Socket connection timeout for health check (in milliseconds)"), // 2.5 second

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        HOST_REGION("hostRegion" , null, "Geographic region where webapi is located. e.g. ma"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        HOST_ENVIRONMENT("hostEnvironment" , null, "A name given to the webapi environment. e.g. preprod"),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        GRPC_CHANNELS_PER_DESTINATION("grpcChannelsPerDestination" , 2, "Target number of gRPC channels per destination to create. Due to backoffs and cycling, at times, actual number channels could be double this figure. Guidance is ~ 100 HTTP/2 streams per channel. If less than 1, uses 1 gRPC channel (i.e. 1 TCP connection) per request and calls shutdown() (i.e. closes TCP connection) when request is completed."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        GRPC_CHANNEL_LIFETIME_SECONDS("grpcChannelLifetimeSeconds" , 60, "Amount of time, in seconds, to keep a gRPC channel alive before shutting it down. gRPC channel roughly maps to a TCP connection lifetime. May need to consider maximum TCP connection duration on firewalls."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        GRPC_CHANNEL_LIFETIME_BACKOFF_SECONDS("grpcChannelLifetimeBackoffSeconds" , 5, "Amount of time, in seconds, prior to grpcChannelLifetimeSeconds, to stop adding new HTTP2 streams to a gRPC channel."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        GRPC_CHANNEL_KEEP_ALIVE_WITHOUT_CALLS("grpcChannelKeepAliveWithoutCalls" , true, "Sets whether keepalive will be performed when there are no outstanding RPCs on a connection"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        GRPC_CHANNEL_KEEP_ALIVE_TIME_SECONDS("grpcChannelKeepAliveTimeSeconds" , 15, "Sets the time without read activity before sending a keepalive ping. Set to -1 library default."),

        @Undocumented
        STUB_TTS_BUFFER_SIZE ("stubTtsBufferSize", 100000, UNDOCUMENTED_PARAMETER), //Stub impl is not really used by ECS
        @Undocumented
        STUB_TTS_DELAY_MILLISECONDS ("stubTtsDelayMilliseconds", 500, UNDOCUMENTED_PARAMETER), //Stub impls is not really used by ECS
        @Undocumented
        STUB_RECO_DELAY_MILLISECONDS ("stubRecoDelayMilliseconds", 500, UNDOCUMENTED_PARAMETER), //Stub impls is not really used by ECS
        @Undocumented
        STUB_RECO_SOS_EOS_DELAY_MILLISECONDS ("stubSosEosRecoDelayMilliseconds", 3000, UNDOCUMENTED_PARAMETER), //Stub impls is not really used by ECS
        @Undocumented
        BOUNDED_THREAD_POOL_SIZE ("boundedThreadPoolSize", 10, UNDOCUMENTED_PARAMETER), //Not used much by webapi (stub and nlps) . Would only cause confusion

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        GLOBAL_RATE_LIMIT_CONCURRENT_SESSIONS("globalRateLimitConcurrentSessions", -1, "Global rate limit for concurrent WebAPI sessions"),//By default, rate limiting is disabled

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_CREDENTIALS_REFRESH_SECONDS("mixClientCredentialsRefreshSeconds", 840, "Number of seconds between refreshing Mix auth client credentials. This value should be lower than mixClientCredentialsExpireSeconds to allow refreshing the token before expiry."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_CREDENTIALS_EXPIRE_SECONDS("mixClientCredentialsExpireSeconds", 900, "Number of seconds Mix auth client credentials expire. This value should be higher than mixClientCredentialsRefreshSeconds to allow refreshing the token before expiry."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_IDLE_REFRESH_ON_EXPIRE("mixIdleRefreshOnExpire", false, "If true, refreshes Mix tokens even when traffic is idle."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_AUTH_SYNC_TIMEOUT("mixAuthSyncTimeout", 30000, "Mix Auth HTTP Client Timeout for when fetching the auth token synchronously. If this timeout fires, we have no backup and the webapi request will fail."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_AUTH_ASYNC_TIMEOUT("mixAuthAsyncTimeout", 15000, "Mix Auth HTTP Client Timeout for when fetching the auth token asynchronously. The existing/valid auth token is always provided for async fetches so this just provides an upper limit for the request."),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_GENERAL)
        COMMAND_PARAMS_TO_REMOVE_FROM_LOGS("commandParamsToRemoveFromLogs", new ArrayList<>(),  "List of parameter keys to remove from logs. verificationCode and authentication will always be removed."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_GENERAL)
        COMMAND_CHAIN_LOG_TYPE("commandChainLogType", CommandChain.CommandChainLogType.PER_REQUEST.toString(), CommandChain.CommandChainLogType.values(),  "Defines how command chains handle reporting."),

        //Global Registry - webapi.json
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_SCHEME ("globalRegistryScheme", "http://", "Global Registry Url scheme."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_HOST ("globalRegistryHost", null, "Global Registry Url host."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_PORT ("globalRegistryPort", "80", "Global Registry Url port."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_PATH ("globalRegistryPath", "/var/globalRegistry.json", "Global Registry Url path."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_ROOT ("globalRegistryRoot", "registry", "Global Registry Json root key"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_FETCH_INTERVAL_SECONDS ("globalRegistryFetchIntervalSeconds", 300, "Global Registry polling interval in seconds."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_FETCH_WAIT_TIME_MILLISECONDS ("globalRegistryFetchWaitTimeMilliseconds", 60000, "Global Registry Fetch wait at server initialization in ms"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_CONNECTION_TIMEOUT_MILLISECONDS ("globalRegistryConnectionTimeoutMilliseconds", 60000, "Global repository connection timeout in ms."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        GLOBAL_REGISTRY_READ_TIMEOUT_MILLISECONDS ("globalRegistryReadTimeoutMilliseconds", 60000, "Global repository read timeout in ms."),

        //Alarms
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_GENERAL)
        ALARMS_PER_MINUTE("alarmsPerMinute" , -1, "Alarms sending throttling. Negative values mean no limit."),

        //HTTP alarms
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_ENDPOINT_URL ("httpAlarmEndpointUrl"  , null, "Endpoint URL where the http event should be posted to."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_TEMPLATE_FILE ("httpAlarmTemplateFile"  , "httpAlarm.vt", "Velocity template file to be used. The file is loaded form the webapi config folder."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_MEDIA_TYPE ("httpAlarmMediaType"  , "application/json", "Http media type header."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_BASICAUTH_USERNAME("httpAlarmBasicauthUser", null, "Username for basic authentication on the HTTP alarm endpoint."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_BASICAUTH_USERNAME_FILE_PATH("httpAlarmBasicauthUserFilePath", null,
                "Path of a file (typically a mounted k8s secret) containing the username for basic authentication on the HTTP alarm endpoint."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_BASICAUTH_PASSWORD("httpAlarmBasicauthPassword", null,
                "Password for basic authentication on the HTTP alarm endpoint. Note: the password needs to be encrypted using openssl and Nuance WebAPI encryption key." +
                        " Has precedence over httpAlarmBasicauthPasswordClear if both are defined."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_BASICAUTH_CLEARTEXT_PASSWORD("httpAlarmBasicauthPasswordClear", null,
                "Password for basic authenticationon the HTTP alarm endpoint. The password is specified in clear text. Use this parameter instead of httpAlarmBasicauthPassword if you do not have access to the Nuance WebAPI encryption key (on-prem deployments)."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_BASICAUTH_PASSWORD_FILE_PATH("httpAlarmBasicauthPasswordFilePath", null,
                "Path of a file (typically a mounted k8s secret) containing the password for basic authentication on the HTTP alarm endpoint." +
                        " Has precedence over httpAlarmBasicauthPassword and httpAlarmBasicauthPasswordClear if more than one is defined."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_ZENOSS_API_KEY("httpAlarmZenOssApiKey", null,
                "ZenOss API Key for Zen OSS cloud. Used to set the z-api-key header. Has precedence over HTTP digest authentication if both defined."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_HTTP)
        HTTP_ALARM_ZENOSS_API_KEY_FILE_PATH("httpAlarmZenOssApiKeyFilePath", null,
                "Path of a file (typically a mounted k8s secret) containing ZenOss API Key for Zen OSS cloud. Used to set the z-api-key header." +
                        " Has precedence over httpAlarmZenOssApiKey and HTTP digest authentication if more than one is defined."),

        //SNMP parameters - webapi.json
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAP_PROTOCOL ("snmpTrapProtocol" , SnmpResponseTrapSender.TrapVersion.V3.toString(), SnmpResponseTrapSender.TrapVersion.values(), "SNMP protocol version."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_HOST_ADDRESS("snmpNMSHost" , null, "SNMP server address."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_HOST_PORT("snmpNMSPort", null, "SNMP server port."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAP_V2_COMMUNITY ("snmpTrapV2Community"  , "public", "The name of the SNMP community."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAP_SECURITY_ENCRYPTION_PROTOCOL ("snmpTrapSecurityEncryptionProtocol", SnmpResponseTrapSender.TrapSecurityEncryptionAlgorithm.usmAesCfb128Protocol.toString(),
                SnmpResponseTrapSender.TrapSecurityEncryptionAlgorithm.values(), "Encryption protocol to be used for snmp traps."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_TRAP_OID ("snmpTrapOID" , "1.3.6.1.4.1.3621.31415", "Default SNMP trap OID."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAP_SEND_RETRIES("snmpTrapSendRetries" , 1, "How many times to retry a failed trap."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAP_SEND_TIMEOUT ("snmpTrapSendTimeoutMs" , 1000, "Sending timeout."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_LOCAL_ENGINE_OID("snmpLocalEngineId",null, "The OID to be used for the local engine."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_CONTEXT_NAME("snmpContextName",null, "Trap context name."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_CONTEXT_ENGINE_OID("snmpContextEngineIdHex", null, "The trap context engine ID in hexadecimals."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_USERNAME("snmpNMSUser", null, "Username for authenticating the trap."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_PASSWORD("snmpNMSPassword", null, "Password for authenticating the trap. Note the password needs to be encrypted using openssl."),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_CLEARTEXT_PASSWORD("snmpNMSPasswordClear", null, "Password for authenticating the trap in cleartext."),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_PRIVACY("snmpNMSPrivacy", null, "Privacy passphrase. Note the password needs to be encrypted using openssl."),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_NMS_PRIVACY_CLEARTEXT("snmpNMSPrivacyClear", null, "Privacy passphrase in cleartext."),

        @Deprecated
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        SNMP_TRAPS_PER_MINUTE ("snmpTrapsPerMinute" , -1, "[DEPRECATED use alarmsPerMinute] Trap sending throttling. Negative values mean no limit."),

        //Where to get tenant configs - webapi.json
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.TENANT_CONFIGURATION_FILES)
        TENANT_CONFIG_SCHEME("tenantConfigScheme", "onDisk", WebApiConfigManager.TenantConfigScheme.values(),"Specifies whether to load the tenant configuration from disk or to fetch."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.TENANT_CONFIGURATION_FILES)
        TENANT_CONFIG_SUFFIX_SEARCH_ORDER("tenantConfigSuffixSearchOrder", Collections.singletonList(".json"), new String[]{".json", ".properties", ".xml", ".yml"},"Tenant config suffix (file extension) search order. Note that search order lists of size > 1 could have adverse performance when fetching configs via http."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.TENANT_CONFIGURATION_FILES)
        TENANT_CONFIG_URI_PREFIX("fetchedTenantConfigUriPrefix", null, "Specifies the Uri Prefix for tenant config fetching. Specify the Uri up to the applicationKey. For example:  https://vip-resource-cluster/wapi/preprod/"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.TENANT_CONFIGURATION_FILES)
        TENANT_VALIDATION_REGEX("tenantValidationRegex", "^[A-Za-z0-9_-]+$", "Specifies the regex to use for tenant validation."),

        //Caching http client - webapi.json
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        CACHING_HTTP_CLIENT_CONNECTION_POOL_SIZE("cachingHttpClientConnectionPoolSize", 10, "The connection pool size for the caching http client."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        CACHING_HTTP_CLIENT_MAX_CACHE_ENTRIES("cachingHttpClientMaxCacheEntries", 10 * 1024, "The maximum number of cache entries to keep in the caching http client."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        CACHING_HTTP_CLIENT_CACHE_MAX_OBJECT_SIZE_BYTES("cachingHttpClientCacheMaxObjectSizeBytes", 1024 * 1024, "The cache's maximum per-object size (in bytes) for the caching http client."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        CACHING_HTTP_CLIENT_FETCH_CONNECT_TIMEOUT_MS("cachingHttpClientConnectTimeoutMs", 5000, "The connect timeout for the caching http client."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        CACHING_HTTP_CLIENT_FETCH_SOCKET_TIMEOUT_MS("cachingHttpClientSocketTimeoutMs", 5000, "The socket timeout for the caching http client (See Apache HTTP Client's http.socket.timeout for more information)."),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.CACHING_HTTP_CLIENT)
        EDF_LOG_POSTER_POOL_SIZE("edfLogPosterPoolSize", 10, "Thread and Connection pool size for the EDF log poster."),

        //Request-response logging
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        AUDIO_LOGGING_IN_REQUEST_RESPONSE_FILTER("audioLoggingInRequestResponseFilter", false, "When enabled, audio will be logged for request/response logging in the HTTP filter logs. Note that removal of audio will cause fidelity of the output to be lost due to json parsing."),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        INPUT_TEXT_LOGGING_IN_REQUEST_RESPONSE_FILTER("inputTextLoggingInRequestResponseFilter", false, "When enabled, TTS input text will be logged in the HTTP request filter logs. Note that removal of the input text will cause fidelity of the input to be lost due to json parsing."),

        //***
        //*** Tenant params
        //***

        //PluginInfo
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        TENANT_ACTIVE_PLUGINS_LIST("activePluginsList", new ArrayList<Object>(), "A list of regex expressions to select active plugins."),
        COMBINED_CONFIG_LINGER_TIME_MILLISECONDS("combinedConfigLingerTimeMilliseconds", 0, "How long to linger the CombinedConfiguration objects. Values less than 1, clear config immediately."),

        //NMAID ID and Key. The key is also used without NCS/NMAS for the verificationCode
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_START_SESSION)
        NMAID_ID("nmaidId", null, "String-key for the Nuance Mobile Application Identifier"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_START_SESSION)
        NMAID_KEY("nmaidKey",null, "64-byte key concatenated into a string or array of strings. The array of strings is used to allow key rotation. Each key string is 128 characters long. Values from 0-f. Example: 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef. The SHA-256 of this string (without a new line) is the verificationCode, even when NCS/NMSP is not used."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.GENERAL_CONFIG)
        TIME_FORMAT("timeFormat","yyyy-MM-dd", "Format of the times defined in the configuration. Defaults to \"yyyy-MM-dd\""),

        //NCS/NMSP Params
        @Backend(backend = Backend.Value.NCS)
        NMSP_GATEWAY_HOST("nmspGatewayHost", null, "Hostname for the nmsp Gateway."),
        @Backend(backend = Backend.Value.NCS)
        NMSP_GATEWAY_PORT ("nmspGatewayPort" , 443, "Port number for the nmsp Gateway."),
        @Backend(backend = Backend.Value.NCS)
        NMSP_GATEWAY_SSL_SOCKET_ENABLED ("nmspGatewaySSLEnabled",  true, "Flag to indicate if nmsp Gateway uses SSL."),
        @Backend(backend = Backend.Value.NCS)
        NMSP_GATEWAY_SESSION_MAX_AUDIO_BYTES ("nmspGatewaySessionMaxAudioBytes", 1024 * 1024 * 2, "Maximum audio bytes supported per NMSP session. When this limit is reached the session with the NMSP gateway will be recreated."), //2MB,
        @Backend(backend = Backend.Value.NCS)
        NMSP_GATEWAY_SSL_IS_CERTIFICATE_SELF_SIGNED ("nmspGatewaySSLIsCertificateSelfSigned" , true, "Flag to indicate that self-signed certs should be tolerated"),

        @Backend(backend = Backend.Value.NCS)
        DICTATION_LANGUAGE ("dictationLanguage", "en-us", "The value of the dictation_language key to send to NCS"),
        @Backend(backend = Backend.Value.NCS)
        MMF_CONTROLLER_VERSION ("mmfControllerVersion", "3.0.0", "Value of mmfControllerVersion to send to NCS"),
        @Backend(backend = Backend.Value.NCS)
        SERVICE_PARAM_APPLICATION("serviceParamApplication"  , "NinaCloud", "cloudName or webapp name for NiM/NCS"),
        @Backend(backend = Backend.Value.NCS)
        NMAS_COMMAND_TIMEOUT_MILLISECONDS ("nmasCommandTimeoutMilliseconds", 60000, "NMAS command timeout (in milliseconds)"), // 1 minute
        @Backend(backend = Backend.Value.NCS)
        USE_CALL_LOG_SENDER ("useCallLogSender"  , true, "Flag to indicate if NMSP client call logging is enabled or not. Normally kept enabled."),
        @Backend(backend = Backend.Value.NCS)
        APPEND_SUFFIX_TO_USERID ("appendSuffixToClientUserId"  , false, "Flag to indicate that a generated suffix should be appended to the client-provided clientUserId. Must be disabled when using userIds and speaker profiles."),

        //For Global Registry Lookups and NiM/NCS/ADK Dialog Model lookups
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        DIALOG_MODEL_COMPANY_NAME("dialogModelCompanyName", null, "Company Name used for Global Registry lookups for NiM/NCS and Krypton. Also used for ADK Company Name (NIM/NCS)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        DIALOG_MODEL_APPLICATION_NAME("dialogModelApplicationName", null, "Application Name used for Global Registry lookups for NiM/NCS and Krypton. Also used for ADK Application Name (NIM/NCS)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.GLOBAL_REGISTRY)
        DIALOG_MODEL_APPLICATION_VERSION("dialogModelApplicationVersion" , "1.0", "Application Version used for Global Registry lookups for NiM/NCS and Krypton. Also used for ADK Application Version (NIM/NCS)"),

        //Timeouts and maximums

        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        SESSION_INACTIVITY_TIMEOUT_SECONDS ("sessionInactivityTimeoutSeconds" , 300, "Webapi inactive session timeout (in seconds)"), // 5 minutes
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        SESSION_TIMEOUT_SECONDS ("sessionTimeoutSeconds" , 900, "Maximum webapi session duration, even if session is still active (in seconds)"), // 15 minutes
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        COMMAND_TIMEOUT_MILLISECONDS ("commandTimeoutMilliseconds", 60000, "Webapi command timeout (in milliseconds)."), // 1 minute
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        WEBSOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE ("websocketMaxTextMessageBufferSize", 65536, "Maximum size of a websocket text message (in bytes)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        WEBSOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE( "websocketMaxBinaryMessageBufferSize", 65536, "Maximum size of a websocket binary buffer (in bytes)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        WEBSOCKET_MAX_IDLE_TIMEOUT("websocketMaxIdleTimeout", 300000, "The timeout for an idle websocket connection (in milliseconds"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.WEBAPI_WS_INTERFACE)
        TENANT_RATE_LIMIT_CONCURRENT_SESSIONS("tenantRateLimitConcurrentSessions", -1, "Tenant rate limit for concurrent WebAPI sessions"),//By default, rate limiting is disabled

        //Authentication
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_GENERAL)
        ACTIVE_AUTH_METHODS("activeAuthenticationMethods" , Collections.singletonList(AuthTypeAndData.ValidationTypes.hashcode.toString()), AuthTypeAndData.ValidationTypes.values(), "A list of allowed authentication methods."),
        //App Key authentication
        //JWT parameters
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        JWT_ALLOWED_CLOCK_SKEW_SECONDS ("jwtAllowClockSkewSeconds" , 30, "If set, it will allow the clock of the JWT (JSON WEB TOKEN) issuer and the clock of the server to be off by this amount (in seconds)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        JWT_FORCE_EXPIRATION_CHECK ("jwtForceExpirationCheck" , true, "Flag to indicate if the JWT (JSON WEB TOKEN) is required to have an expiration field set."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        JWT_HASH_ALGORITHM_WHITELIST ("jwtHashAlgorithmWhitelist" , Collections.singletonList("RS256"),
                new String[]{"none", "HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512"},
                "A list of allowed hashing algorithm."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        JWT_ISSUER("jwtIssuer",null, "Name of the JWT (JSON WEB TOKEN) issuer."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        JWT_PUBLIC_KEYS("publicKeysMap",new HashMap<String, Object>(), "A map containing the ID of the public key and the actual key in PEM format. The reserved key '__no_kid__' is used to identify the JWS key for JWTs without the 'kid' parameter in header."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_TENANT_ID("azureTenantId", null, "A tenant id to fetch the public key from a specific 'kid' attribute of an Azure Active Directory Token."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_CLIENT_ID("azureClientId", null, "A client id to validate audience of an Azure Active Directory Token."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_KEYS_URL_FORMAT("azureKeysURLFormat", "https://login.microsoftonline.com/%s/discovery/v2.0/keys", "A URL to get public keys"),

        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_MAX_CACHED_KEYS("azureMaxCachedKeys", 500, "A maximum amount of public keys cached"),

        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_CACHE_LIFESPAN_HOURS("azureCachedLifespanHours", 24, "Limit of public keys cached in hours."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_JWT)
        AZURE_ISSUER_URL_FORMAT("azureIssuerURLFormat", "https://sts.windows.net/%s/", "Issuer of JWT Token."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_BASIC)
        BASIC_AUTH_USER("basicAuthenticationUsername",null, "Basic authentication username to be used for http_auth (do not use for prometheus metrics)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_BASIC)
        BASIC_AUTH_PASS("basicAuthenticationPasscode",null, "Basic authentication password to be used for http_auth (do not use for prometheus metrics)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AUTHENTICATION_BASIC)
        BASIC_AUTH_ACCOUNTS("basicAuthenticationAccounts", null, "Basic authentication accounts list (e.g  [{\"basicAuthenticationUsername\": \"user\", \"basicAuthenticationPasscode\": \"password\"},{\"basicAuthenticationUsername\": \"user2\",\"basicAuthenticationPasscode\": \"password2\") to be used for http_auth (do not use for prometheus metrics)"),

        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.PROMETHEUS_METRICS)
        METRICS_USERNAME("metricsUsername", null, "Basic authentication username used by the metrics service (for prometheus)"),
        @WebapiJsonOnly
        @ConfigurationGroup(group = ConfigurationGroup.Value.PROMETHEUS_METRICS)
        METRICS_PASSWORD("metricsPassword", null, "Basic authentication password used by the metrics service (for prometheus)"),

        //EDF and OAuth
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_POSTING_ENABLED_TENANT ("logPostingEnabled"   , false, "Enable EDF log posting for this tenant"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BINARY_ENDPOINT("binaryEndpoint",null, "Url of the EDF binary endpoint."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_DATA_ENDPOINT("dataEndpoint",null, "Url of the EDF data endpoint."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_CLIENT_ID("projectId", 0, "The EDF client Id. e.g. 10005342 in /v1/webapi/Prod/10005342/startSession"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_ENVIRONMENT("edfEnvironment", "Prod", "The EDF environment being posted to. e.g. Prod in /v1/webapi/Prod/10005342/startSession"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_PROXY_URL("proxyUrl", null, "Forward Proxy Url to use when posting to EDF"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_DISABLE_TLS_VALIDATION("edfDisableTlsValidation", false, "When enabled, TLS hostname validation is disabled. Does not affect certificate validation."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_DELAY_ON_FAILED_REQUEST_MS("edfDelayOnFailedRequestMs", 5000, "In case edf log posting fails, wait for this amount before re-posting. (in milliseconds)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_MESSAGES_BETWEEN_STATS("edfMessagesBetweenQueueAndJvmStats", 0, "The EDF monitoring related statistics will be logged once every N number of messages. 0=disabled"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_HTTP_CONNECT_TIMEOUT_MS("edfHttpConnectTimeoutMs", 5000, "EDF client http connection timeout in ms."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_HTTP_CONNECTION_REQUEST_TIMEOUT_MS("edfHttpConnectionRequestTimeoutMs",  5000, "EDF client http request connection timeout in ms."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_HTTP_SOCKET_TIMEOUT_MS("edfHttpSocketTimeoutMs", 10000, "EDF client http socket timeout in ms."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_OUTPUT_MESSAGES_COUNT("edfOutputMessagesCount", false, "Enable EDF stats \"MessageCount\" to be printed into the logs."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_POPULATE_FIRST_PUT_BODY("edfPopulateFirstPutBody", false, "EDF Binary API always returns a first redirect. We usually don't fill the body to prevent unnecessary uploading. In case EDF changes (or we use a proxy) and we want to fill the first PUT."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BROKER_URI("edfBrokerUri", "broker:(vm://webapi-platform)?brokerName=webapi-platform&useJmx=false", "EDF JMS Queue Broker URI. Append \"&dataDirectory=/someDiskPartition/$JETTY_BASENAME/\" to set the activemq-data folder"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BROKER_CONNECT_URL("edfBrokerConnectUrl", "vm://webapi-platform?create=false", "Location of the JMS que broker used by EDF."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BROKER_MEMORY_LIMIT_MB("edfBrokerMemoryLimitMb", 128L, "Limit the memory used by the JMS Queue broker for EDF (in Megabytes)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BROKER_STORAGE_LIMIT_GB("edfBrokerStorageLimitGb", 100L, "Limit the disk store size for the JMS Queue broker for EDF (in Gigabytes)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_BROKER_TEMP_STORAGE_LIMIT_GB("edfBrokerTempStorageLimitGb", 50L, "Limit the temporary disk store size for the JMS Queue broker for EDF (in Gigabytes)"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_IAM_SCOPE("googleIapIamScope", "https://www.googleapis.com/auth/iam", "OAuth. EDF. Google IAM service scope."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_OAUTH_TOKEN_URI("googleIapOauthTokenUri", "https://www.googleapis.com/oauth2/v4/token", "OAuth. EDF. Google IAP oAuth token uri."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_JWT_BEARER_TOKEN_GRANT_TYPE("googleIapJwtBearerTokenGrantType",
                "urn:ietf:params:oauth:grant-type:jwt-bearer", "OAuth. EDF. Google IAP JWT bearer token grant type."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_JWT_DURATION_SECONDS("googleIapJwtDurationSeconds",
                3600L, "OAuth. EDF. Google IAP JWT token valid duration in seconds."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_TOKEN_CACHE_DURATION_MINUTES("googleIapTokenCacheDurationMinutes",
                30L, "OAuth. EDF. Google IAP JWT token cache duration in minutes."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        GOOGLE_IAP_CREDENTIALS_CACHE_DURATION_MINUTES("googleIapCredentialsCacheDurationMinutes",
                30L, "OAuth. EDF. Google IAP credentials cache duration in minutes."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        DATA_CLIENT_ID("dataClientId",null, "Client ID to be used for the EDF data post. e.g. 988115083047-fspbgdddvtiqibvqsouodtme059h0nt1.apps.googleusercontent.com"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        BINARY_CLIENT_ID("binaryClientId", null, "Client ID to be used for the EDF binary post. e.g. 988115083047-fspbgdddvtiqibvqsouodtme059h0nt1.apps.googleusercontent.com"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        ENABLE_EDF_LOG_POSTER ("enableEdfLogPoster" , false, "Flag to indicate whether or not the EDF log poster is enabled."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        EDF_JSON_CRED_FILE ("edfCredsFile", null, "Absolute path (or http[s] URL) of the OpenSSL encrypted credentials (.pem) file. The file should originally be a json file provided by the EDF team. The file must then be encrypted using openssl by the hosting team and placed in the config directory. Command to encrypt the file can be found in the following document: https://nuance.jiveon.com/docs/DOC-187440"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EDF)
        SEND_LOGS_TO_QUEUE("sendLogsToQueue", true,
                "Per-tenant flag to indicate whether or not to enqueue logs to the legacy log queue (i.e. JMS for EDFv1)"),

        //EventHub configurations to be configured in tenant file
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EVENTHUB)
        ENABLE_EVENTHUB_LOG_POSTER("enableEventHubLogPoster", false,
                "Flag to indicate whether or not the Event hub poster is enabled."),
        //EventHub Client ID for AKS Cluster
        @ConfigurationGroup(group = ConfigurationGroup.Value.KEY_VAULT)
        EVENTHUB_CLIENT_ID("eventHubClientId", null, "The user assigned identity client id to post to EventHub."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_EVENTHUB)
        EVENTHUB_NAMESPACE("eventHubNamespace", "",
                "EventHub namespace to post log to."),

        //Fluentbit configurations to be configured in tenant file
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        ENABLE_FLUENT_LOG_POSTER("enableFluentLogPoster", false,
                "Flag to indicate whether or not the Fluent log poster is enabled."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_LOG_CHANNEL("fluentLogChannel", "wapi", "The NEAP channel. Defaults to wapi."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        REPORTING_TENANT_DATA("reportingTenantData", null, "Additional headers to be added in reporting log. They will be added in a reportingHeader.tenantData field in the reporting log JSON document."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_LOG_TOPIC_NAME("fluentLogTopicName", null, "Explicitly sets the fluent topic name for a tenant. If not set, uses \"fluentLogChannel-projectId\""),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_LOG_FORMAT("fluentLogFormat", NEAP_V1_LOG_FORMAT_NAME,
                "Format of logs to be written that will be picked up by fluentbit. Supported values: " + NEAP_V1_LOG_FORMAT_NAME + ", " + EDF_V1_LOG_FORMAT_NAME),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_LOGGER_LOG_LEVEL_NAME("fluentLoggerLogLevelName", "REPORTING",
                "Custom log level name for reporting logs."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_LOGGER_LOG_LEVEL("fluentLoggerLogLevel", 190,
                "Custom log level for reporting logs."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        ENABLE_FLUENT_LOG_SYSOUT("enableFluentLogSysout", false, "Enable fluent logs to be printed to SYSOUT"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_MULTIPLE_AUDIO_LOGS_PER_BLOB("fluentMultipleAudioLogsPerBlob", false,
                "PREVIEW ONLY: Multiple audio per blob not yet supported by NEAP. Whether to combine multiple audio logs in a blob. " +
                        "If enabled, an additional audioLogKey field is added to the command and audio logs. "),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_FLUENTD)
        FLUENT_AUDIO_LOGS_TIMESLICE_DURATION("fluentAudioLogsTimesliceMinutes", 5,
                "PREVIEW ONLY: Multiple audio per blob not yet supported by NEAP. When multiple audio logs are grouped in a blob, this " +
                    "determines how long the time slices are. Acceptable values are 1, 5, 10, 15, 20, 30, 60 (configured value will be rounded down to the " +
                        "preceding valid value). "),

        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_SECURITY)
        SECURITY_LOGGER_LOG_LEVEL_NAME("securityLoggerLogLevelName", "SECURITY",
                "Custom log level name for security logs."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.REPORTING_SECURITY)
        SECURITY_LOGGER_LOG_LEVEL("securityLoggerLogLevel", 180,
                "Custom log level for security logs."),

        //To start a session with NCS
        @Backend(backend = Backend.Value.NCS)
        NIM_NCS_SESSION_REQUIRED( "nimNcsSessionRequired", true, "Flag to determine if a real NCS session to be used or a stub one. Disable this when using VoCS/Krypton"),

        // Speech Synthesis
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        VOCALIZER ("vocalizer", VocalizerClients.nim.getValue(), VocalizerClients.getValues(), "Speech synthesis engine to use."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_VOICE ("voice", null, "Default TTS Voice Name"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        ALLOW_SSML("allowSsml", false, "Switch to explicitly enable TTS type 'ssml' for given tenant"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_LANGUAGE ("ttsLanguage","en-us", "Default TTS language."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_NMAS_CMD_NAME ("tts_nmas_cmd_name", "NINA_TTS_CMD", "Name of the command to be used when executing a TTS on an NMAS server."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_RATE("speechSynthesisRate", null, "Speaking rate. 0.0 to 2.0, where 1.0 is normal rate."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_AUDIO_CHUNK_DURATION_MS("speechSynthesisAudioChunkDurationMs", -1, "Maximum duration, in ms, of an audio chunk. Set to '0' for the mix unary-tts command."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_TARGET_AUDIO_LENGTH_MS("speechSynthesisTargetAudioLengthMs", -1, "Maximum duration, in ms, of synthesized audio."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_DISABLE_EARLY_EMISSIONS("speechSynthesisDisableEarlyEmissions", null, "Disabling early emissions will force audio results to be sent only when available."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_TUNING_BUNDLE("speechSynthesisTuningBundle", null, "An optional parameter which allows the client to specify the text rule set and/or text dictionary files."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_SYNTHESIS)
        SPEECH_SYNTHESIS_TUNING_BUNDLES("speechSynthesisTuningBundles", null, "List of allowed speech synthesis tuning bundles config"),

        //Mix
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_AUTH_URI("mixAuthUri", "https://auth.crt.nuance.com/oauth2/token", "Mix's Auth URI for tokens"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_CREDENTIALS_SCOPE("mixClientCredentialsScope", "asr nlu tts dlg", "Mix Auth Client Credentials Scope as per https://docs.mix.nuance.com/authorization/#generate-the-client-secret"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_CREDENTIALS_FORM("mixClientCredentialsForm", "grant_type=client_credentials&scope=", "Mix Auth Client Credentials Form (must end with scope=) as per https://docs.mix.nuance.com/authorization/#request-an-access-token"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_ID("mixClientId", "", "Mix Client Id for Auth. e.g. appID:NMDPTRIAL_your_app_id_here    No need to URL Encode as webapi will URL Encode the value."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.CORE_TECH_SAAS)
        MIX_CLIENT_SECRETS("mixClientSecrets", Collections.singletonList(""), "Mix Client Secrets for Auth. e.g. [\"riAbk888CC2B.97D7eUklVe6pD\"]. Secrets will be attempted in array order. The array allows for potential key rotation."),

        VOCS_URL("vocsURL",null, "Optional per-voice URL override key (looked up inside vocsMapping.<voice>)."),
        VOCS_MAPPING("vocsMapping",null, "WebAPI voice name to voice name/model/output/etc. parameters mappings."),

        // Speech Recognition
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_ENGINE ("speechRecognitionEngine" ,
                SpeechRecognitionClients.MREC_NIM.getValue(), SpeechRecognitionClients.getValues(), "Speech recognition engine to use."),
        @Undocumented
        MICROSOFT_SPEECH_CONFIDENCE_THRESHOLD ("mcsftSpeechConfidenceThreshold" , 0.01, "Speech recognition confidence threshold to get a no match result."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MICROSOFT_SPEECH_API_KEY ("azureApiKey" , null, "Speech recognition api key."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MICROSOFT_SPEECH_SERVICE_REGION ("serviceRegion", null,"Speech Service region."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_RECOGNITION_END_SILENCE_TIMEOUT_MS ("mcsftEndSilenceTimeoutMs" , "500", "The end silence timeout value (in milliseconds). Setting for microsoft speech sdk service."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_RECOGNITION_SEGMENTATION_SILENCE_TIMEOUT_MS ("mcsftSegmentationSilenceTimeoutMs" , "500", "A duration of detected silence, measured in milliseconds, after which speech-to-text will determine a spoken phrase has ended and generate a final Recognized result. Setting for microsoft speech sdk service."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_RECOGNITION_MODE ("mcsftRecognitionMode" , AzureASRClient.RecognitionMode.DICTATION.getMode(), AzureASRClient.RecognitionMode.values(), "The Cognitive Services Speech Service recognition Mode. Can be: INTERACTIVE, CONVERSATION, DICTATION."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_RECOGNITION_BEST_RESULT_FORMAT ("mcsftRecognitionBestResultFormat" , AzureASRClient.BestResultFormat.LEXICAL.getValue(), AzureASRClient.BestResultFormat.values(),"The Cognitive Services Speech Service best result format type. Can be: Lexical, ITN, MaskedITN, Display"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_OUTPUT_FORMAT ("mcsftOutputFormat" , "Detailed", "The Cognitive Services Speech Service recognition format. It is either Detailed or Simple"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        MCSFT_SPEECH_PROCESS_DELAY_MS ("mcsftProcessDelayMs" , 5000, "Delay to process all audio."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_NMAS_CMD_NAME ("asr_nmas_cmd_name" ,"NINA_MREC_CMD", "Name of the command to be used when executing ASR on an NMAS server."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_LANGUAGE_MAPPING ("speechRecognitionLanguageMap" ,null, "Map to change external speech recognition language codes to internal codes"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        TOPIC("topic",null, "Default for topic. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_FORMATTING_TYPE("speechRecognitionFormattingType", null, "A keyword that affects how ambiguous numbers are presented in the formattedText field."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_IMMUTABLE_PARTIAL_RESULTS("speechRecognitionImmutablePartialResults", false, "Set to true to delay the delivery of partial results until the recognizer is confident that the recognized words will no longer change with the rest of the received speech."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER("speechRecognitionEnableProfanityFilter",false, "Speech recognition profanity filter. Default value is false. Can be configured at 4 different levels (in this order of precedence): command, session, tenant, server. The Web API starts by looking at the command level to see if the value is set. If it is, it uses that value. If it's not, it looks at the next level. It repeats this process for each configuration level until it finds a value. If it doesn't, the default value is used."),

        //Krypton specific
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_LANGUAGE_PACK("kryptonLanguagePack",null, "Default Krypton datapack language. Client language parameter overrides this."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_TOPIC("kryptonTopic",null, "Krypton datapack topic to use."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_DOMAIN_LM_URL("kryptonDomainLmUrl",null, "Optional Krypton domain language-model url when not using global registry or when not using Mix."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_DOMAIN_LMS_PER_LANGUAGE("kryptonDomainLmsPerLanguage",null, "(Krypton gRPC only) Optional Krypton domain language-models map when not using global registry or when not using Mix. e.g. {\"eng-USA\": [{\"uri\":\"http://host/dlms/app-eng-USA-dlm.zip\", \"weight\":\"medium\"}]}"),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_DOMAIN_LM_WEIGHT("kryptonDomainLmWeight",null, "Optional Krypton domain language-model weight. Not defined in the global registry. Values can be (string, one of \"lowest\", \"low\", \"medium\", \"high\", \"highest\" ; or integer, 0-1000)"),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_ENABLE_PROFANITY_FILTER("kryptonEnableProfanityFilter",false, "[DEPRECATED use speechRecognitionEnableProfanityFilter] Krypton profanity filter. Default value is false. Can be configured at 4 different levels (in this order of precedence): command, session, tenant, server. The Web API starts by looking at the command level to see if the value is set. If it is, it uses that value. If it's not, it looks at the next level. It repeats this process for each configuration level until it finds a value. If it doesn't, the default value is used."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_LANGUAGE_TO_URL_MAPPING("kryptonLanguageToUrlMapping", null, "Use only when a single tenant needs to access multiple Krypton pools. Define a mapping between the webAPI language parameter and the Krypton URL to use to serve that language. When used, kryptonGrpcServerUrl is ignored."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_RESTRICT_LANGUAGES("kryptonRestrictLanguages", false, "When true, only allows the client to specify languages that are listed in kryptonLanguagePack and kryptonLanguageToUrlMapping."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_SPEECH_DETECTION_SENSITIVITY("kryptonSpeechDetectionSensitivity",-1, "An integer value that controls the aggressiveness of speech detection. 0 is the setting least likely to interpret noise as speech and 1000 is the least likely to interpret speech as noise or silence."),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_SUPPRESS_CALL_RECORDING("kryptonSuppressCallRecording",null, new Boolean[]{false, true}, MIX_DOC_RECO_FLAGS),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_UTTERANCE_END_SILENCE_MS("kryptonUtteranceEndSilenceMs",-1, MIX_DOC_RECO_PARAMS),
        @Backend(backend = Backend.Value.KRYPTON)
        KRYPTON_DISCARD_SPEAKER_ADAPTATION("kryptonDiscardSpeakerAdaptation",null, MIX_DOC_RECO_FLAGS),

        // Interpretation
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_BACKEND("interpretationBackend", InterpretationClients.NLU_GRPC.getValue(), InterpretationClients.getValues(), "Interpretation backend to use."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_LANGUAGE("interpretationLanguage", "en-US", "Interpretation language to use."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_USE_FULLY_FORMATTED_LITERAL("interpretationUseFullyFormattedLiteral", null, "Should the fully formatted form of the literal be returned or the minimally formatted form."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_LANGUAGE_MAPPING ("interpretationLanguageMap" ,null, "Map to change external interpretation language codes to internal codes"),

        //NLU semantic model (shared by NLU gRPC client)
        NLE_SEMANTIC_MODEL_URL("semanticModelUrl",null, "When not using the Global Registry, loads a specific NLU model via url."),

        //NLU-Cache
        @Backend(backend = Backend.Value.NLU_CACHE)
        NLU_CACHE_REDIS_URI("nluCacheRedisUri", null, "Redis URI for Nlu Cache e.g. rediss://authKey@hostname.redis.cache.windows.net:6380/0"),

        //User reset
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_USER_RESET)
        USER_RESET_BACKEND("userResetBackend", null, UserResetClients.getValues(), "UserReset backend to use."),

        //A per-tenant keychain for encryption and JWT
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        PUBLIC_KEY ("debugPublicKey" , "", "A per-tenant keychain for encryption and JWTs"),

        //A per-tenant keychain for encryption and JWT (Overrides debugPublicKey)
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        CLIENT_PUBLIC_KEY ("clientPublicKey" , "", "A per-tenant keychain for encryption and JWTs"),
        //Debug/Audio capture
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        DEBUG_ENABLED("debugEnabled", false, "Master switch for all debugging features."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        DEBUG_PATH ("debugPath" , "/var/lib/jetty/", "Audio capture disk path"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        AUDIO_CAPTURE ("debugAudioCapture", 101, "Percentage of how many audio utterances to be captured on file (for debug usage)."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        DISK_USAGE_LIMIT_PERCENTAGE ("debugDiskUsagePercentLimit", 95, "If debug is enabled, this is the disk usage limit percentage for the audio logging path's partition."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        WAV_ENABLED("decodeAudioCapturedToWav", true, "If enabled, all audio capturing will be done in wave format."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        AUDIO_ENCRYPTION ("debugAudioEncryption"  , false, "Enable audio capture encryption."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.DEBUGGING)
        TTS_AUDIO_CAPTURE ("debugTtsAudioCapture", false, "Should tts output to be captured on file (for debug usage)."),

        //Application vocabularies in tenant config
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_UPLOAD_DYNAMIC_VOCABULARY)
        APPLICATION_VOCABULARIES("applicationVocabularies", new ArrayList<>(), "Allows defining application vocabulary in tenant config file. Follows Upload Dynamic Vocabulary command format for the command.parameters.dynamicVocabularies"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_UPLOAD_DYNAMIC_VOCABULARY)
        PERSISTENCE_REDIS_URI("persistenceRedisUri", null, "Redis URI for Persistence (Dynamic Vocabulary) e.g. rediss://authKey@hostname.redis.cache.windows.net:6380/0"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_UPLOAD_DYNAMIC_VOCABULARY)
        LOAD_PERSISTED_WORDSETS("loadPersistedWordsets", false, "Loads persisted wordsets from storage"),

        //Http interface maximums.
        // Unused at the moment, mark as undocumented
        @Undocumented
        HTTP_MAX_FILE_SIZE_MB("httpMaxFileSizeMb", 40, "Maximum size of an uploaded file using the HTTP interface."),
        @Undocumented
        HTTP_MAX_REQUEST_SIZE_MB("httpMaxRequestSizeMb", 50, "Maximum HTTP request size."),

        //NLPS. NLPS has no authentication so we keep it disabled unless needed.
        @ConfigurationGroup(group = ConfigurationGroup.Value.NLPS)
        NLPS_ENABLED("nlpsEnabled", false, "Enable or disable webapi's NLPS API (for IVR). NLPS has no authentication. Requires whitelisting to protect."),

        //Key vault tenant secret name
        @ConfigurationGroup(group = ConfigurationGroup.Value.KEY_VAULT)
        KEY_VAULT_TENANT_SECRET_NAME("keyVaultTenantSecretName", null, "The tenant secret name in the key vault that contains the secret values."),
        //Key vault server secret name
        @ConfigurationGroup(group = ConfigurationGroup.Value.KEY_VAULT)
        KEY_VAULT_SERVER_SECRET_NAME("keyVaultServerSecretName", null, "The server secret name in the key vault that contains the secret values."),

        //Key vault Client ID for AKS Cluster
        @ConfigurationGroup(group = ConfigurationGroup.Value.KEY_VAULT)
        KEY_VAULT_CLIENT_ID("keyVaultClientId", null, "The user assigned identity client id to authenticate to fetch the secrets from key vault / get AAD tokens."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.KEY_VAULT)
        KEY_VAULT_VAULT_URI("keyVaultUri", null, "The uri to access a secret."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.AZURE)
        AZURE_SPEECH_SERVICE_RESOURCE_ID("azureResourceId", null, "The Speech resource ID given from Speech Service."),

        //For performance testing without a backend - very limited use
        @Undocumented
        STUB_BACKEND ("stubBackend" , false, UNDOCUMENTED_PARAMETER),

        //***
        //*** Client params
        //***

        //Defaults for parameters settable at runtime
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        END_POINT_DETECTION ("endPointDetection" , true, "Default for endPointDetection. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_DETECTOR ("speechDetector" , SpeechDetectorTypes.ADAPTIVE.getValue(), SpeechDetectorTypes.getValues(), "Default speechDetector. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        VOICE_THRESHOLD ("voiceThreshold", 4.0, "Default voiceThreshold. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        BEGIN_NOISE_SAMPLE_FRAMES ("beginNoiseSampleFrames"  , 10, "Default beginNoiseSampleFrames. See APIdoc for more info."),
        // legacy parameter startOfSpeechFrames backstops startOfSpeechVoicedFrames
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        START_OF_SPEECH_FRAMES ("startOfSpeechFrames", 7, "Default startOfSpeechFrames. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        START_OF_SPEECH_VOICED_FRAMES ("startOfSpeechVoicedFrames" , -1, "Default for startOfSpeechVoicedFrames. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        START_OF_SPEECH_HISTORY_FRAMES ("startOfSpeechHistoryFrames" , 15, "Default for startOfSpeechHistoryFrames. See APIdoc for more info."),
        // legacy parameter endOfSpeechFrames backstops endOfSpeechVoicedFrames
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        END_OF_SPEECH_FRAMES ("endOfSpeechFrames" , 5, "Default for endOfSpeechFrames. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        END_OF_SPEECH_VOICED_FRAMES ("endOfSpeechVoicedFrames" , -1, "Default for endOfSpeechVoicedFrames. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        END_OF_SPEECH_HISTORY_FRAMES ("endOfSpeechHistoryFrames"  , 50, "Default for endOfSpeechHistoryFrames. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        CONSIDER_NEGATIVE_RATIOS ("considerNegativeRatios"  , false, "Default for considerNegativeRatios. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        STOP_ON_END_OF_SPEECH ("stopOnEndOfSpeech" , true, "Default for stopOnEndOfSpeech. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        START_OF_SPEECH_TIMEOUT_SECONDS ("startOfSpeechTimeoutSeconds" , 5, "Default for startOfSpeechTimeoutSeconds. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        UTTERANCE_MAX_TIME_SECONDS ("utteranceMaxTimeSeconds"  , 30, "Default for utteranceMaxTimeSeconds. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        START_RECOGNITION_TIMERS ("startRecognitionTimers" , true, "NLPS/IVR Only: Default for startRecognitionTimers"),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        WORD_STREAM ("wordStream" , true, "Default for wordStream. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_DOMAIN ("speechDomain" , null, "Default for speechDomain. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_VERBOSE_RESULT ("speechRecognitionVerboseResult" , false, "Default for speechRecognitionVerboseResult. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS("maxTranscriptions" , -1, "Default for maxTranscriptions. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        SPEECH_RECOGNITION_BUILTINS("speechRecognitionBuiltins",null, "List of builtins to load. Example: \"speechRecognitionBuiltins\": [{ \"builtin\": \"ALPHANUM\",\"weight\": 0.1}]"),

        //not used for now. Use verbose result.
        // if re-enambed add this to speech reco docs:
        // | <p class="param-2">rawEngineResult</p> | `Boolean` | **optional** <p>Provides raw (unaltered) backend engine result under the 'rawEngineResult' key. NOTE: the format of the 'rawEngineResult' is not guaranteed.</p>_Default value: false_<br> |
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_GENERAL)
        ENGINE_RAW_RESULT ("rawEngineResult" , false, "Default for rawEngineResult. See APIdoc for more info."),


        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_GENERAL)
        SENSITIVITY ("sensitivity" , Command.CommandSensitivityType.MASKED.name().toLowerCase(), "Default for sensitivity. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_GENERAL)
        STATISTICS ("statistics", false, "Default for statistics. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.TENANT_CONFIGURATION_FILES)
        ALLOW_ASR_ENGINE_METRICS_IN_STATISTICS("statisticsAllowAsrEngineMetrics", false, "Set to true to allow ASR engine statistics to be returned as part of the statistics object."),

        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_UPLOAD_DYNAMIC_VOCABULARY)
        ACTIVE_DYNAMIC_VOCABULARY("activeDynamicVocabularySets",null, "Default for activeDynamicVocabularySets. See APIdoc for more info."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION)
        NOISE_LEVEL("noiseLevel", null, "Default for noiseLevel. See APIdoc for more info."),

        //Parameters normally set by the client at runtime - we don't want them set in config
        @RequestParameter
        APPLICATION_KEY("applicationKey", null, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        VERIFICATION_CODE("verificationCode", null, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_OS_NAME ("clientOsName"   , NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_OS_VERSION("clientOsVersion"  , NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_USER_ID ("clientUserId", NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_DEVICE_ID ("clientDeviceId", NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_SESSION_ID ("clientSessionId", NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_SUBSCRIBER_ID ("clientSubscriberId", NOT_DEFINED, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        @Backend(backend = Backend.Value.NCS)
        CLIENT_CARRIER("clientCarrier"  , NOT_DEFINED, UNDOCUMENTED_PARAMETER), //NCS only. Should be a client parameter documented by APIdoc. e.g. Verizon. Should be set by the client, not in server-side/tenant config
        @RequestParameter
        @Backend(backend = Backend.Value.NCS)
        CLIENT_PHONE_MODEL("clientPhoneModel", NOT_DEFINED, UNDOCUMENTED_PARAMETER), //NCS only. Should be a client parameter documented by APIdoc. e.g. iPhone X. Should be set by the client, not in server-side/tenant config
        @RequestParameter
        CLIENT_SCRIPT_VERSION("clientScriptVersion" , NOT_DEFINED, UNDOCUMENTED_PARAMETER),
        @RequestParameter
        CLIENT_DETAILS("clientDetails", null, "Placeholder for any details i.e client metadata or other information"),

        @Internal
        AUTH_DATA("authentication",null, UNDOCUMENTED_PARAMETER),

        @RequestParameter
        SPEECH_SYNTHESIS_CODEC ("speechSynthesisCodec", PCM_16_8K,
                new String[]{"pcm_16_22k", "audio/l16;rate=22050", "pcm_16_16k", "audio/l16;rate=16000", "audio/x-raw;format=s16le;rate=16000", PCM_16_8K, "audio/l16;rate=8000", "audio/x-raw;format=s16le;rate=8000", "opus_wb", "opus_nb", "ulaw", "audio/basic;rate=8000", "alaw", "audio/x-alaw-basic;rate=8000", "speex_nb", "speex_wb"},
                UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        SPEECH_RECOGNITION_CODEC ("speechRecognitionCodec", PCM_16_8K,
                new String[]{"pcm_16_16k", "audio/l16;rate=16000", "audio/x-raw;format=s16le;rate=16000", PCM_16_8K, "audio/l16;rate=8000", "audio/x-raw;format=s16le;rate=8000", "opus_wb", "opus_nb", "ulaw", "audio/basic;rate=8000", "alaw", "audio/x-alaw-basic;rate=8000", "speex_nb", "speex_wb"},
                UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config

        @RequestParameter
        COMMAND_DATA ("data", null, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        COMMAND_DATA_TYPE("type", "text/plain", UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config

        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_DATA_TYPE ("interpretationType" , "text/plain", "Default for type of input to NLE. Currently, only text/plain is supported."),
        @ConfigurationGroup(group = ConfigurationGroup.Value.COMMAND_INTERPRETATION)
        INTERPRETATION_VERBOSE_RESULT ("interpretationVerboseResult" , false, "Default for interpretationVerboseResult. See APIdoc for more info."),

        @RequestParameter
        SPEECH_RECOGNITION_LANGUAGE("language",null, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config
        @RequestParameter
        DYNAMIC_VOCABULARIES("dynamicVocabularies",null, UNDOCUMENTED_PARAMETER), //Should be set by the client, not in server-side/tenant config

        // gRPC
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_SERVER_URL("kryptonGrpcServerUrl", null, "Backend url for the krypton grpc client. NOTE: It needs to be a valid URL format."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE("enableSpeechRecognitionSpeakerProfile", false, "If available, send the clientUserId to  gRPC for speaker profiles. Note that appendSuffixToClientUserId must also be disabled for speaker profiles."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        SPEECH_RECOGNITION_MASK_LOAD_FAILURES("speechRecognitionMaskLoadFailures", false, "If set to true, failure to load resources will not cause the recognition to fail."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_USE_USERID("kryptonGrpcUseUserId", false, "DEPRECATED use " + ENABLE_SPEECH_RECOGNITION_SPEAKER_PROFILE.key + " If available, send the clientUserId to Krypton gRPC for speaker profiles. Note that appendSuffixToClientUserId must also be disabled for speaker profiles."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_AUTH_METHOD("kryptonGrpcAuthenticationMethod",
                KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, new String[]{KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, KryptonGrpcClient.AUTH_NO_AUTH, KryptonGrpcClient.AUTH_PASSTHROUGH}, "Authentication method to use for Krypton-gRPC."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_RESOURCE_HEADERS("kryptonGrpcResourceHeaders",null, "Optional headers to provide to Krypton gRPC for resource fetching e.g. {\"cache-control\": \"max-age=600,max-stale=300\"}"),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_RESOURCE_REQUEST_TIMEOUT_MS("kryptonGrpcResourceRequestTimeoutMs",10000, "Optional parameter that tells krypton for how to wait before timing out while trying to load external resources. Default value is 10s."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_DLM_RESOURCE_REQUEST_TIMEOUT_MS("kryptonGrpcDlmResourceRequestTimeoutMs",null, "Optional parameter that tells krypton for how to wait before timing out while trying to load DLM resources. Fallback to kryptonGrpcResourceRequestTimeoutMs if not defined."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_WORDSET_RESOURCE_REQUEST_TIMEOUT_MS("kryptonGrpcWordSetResourceRequestTimeoutMs",null, "Optional parameter that tells krypton for how to wait before timing out while trying to load wordset resources. Fallback to kryptonGrpcResourceRequestTimeoutMs if not defined."),
        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_SPEAKER_PROFILE_RESOURCE_REQUEST_TIMEOUT_MS("kryptonGrpcSpeakerProfileResourceRequestTimeoutMs",null, "Optional parameter that tells krypton for how to wait before timing out while trying to load speaker profile resources. Fallback to kryptonGrpcResourceRequestTimeoutMs if not defined."),

        @Backend(backend = Backend.Value.KRYPTON_GRPC)
        KRYPTON_GRPC_THWART_NO_AUDIO_TIMER_SECONDS("kryptonGrpcThwartNoAudioTimerSeconds", -1, "Time interval for which we should send a few silence packets to thwart Krypton's no-audio (idle) timers. No effect if less than 1. Silence sending stops on first real audio packet."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_SERVER_URL("nluAasGrpcServerUrl", null, "Backend URL for the NLU gRPC client. NOTE: It needs to be a valid URL format."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_AUTH_METHOD("nluAasGrpcAuthenticationMethod", KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, new String[]{KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, KryptonGrpcClient.AUTH_NO_AUTH, KryptonGrpcClient.AUTH_PASSTHROUGH}, "Authentication method to use for NLU gRPC client."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_USE_USERID("nluAasGrpcUseUserId", false, "If available, send the clientUserId to NLU gRPC to identify the user. Note that appendSuffixToClientUserId must also be disabled."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_MAX_INTERPRETATIONS("maxInterpretations", -1, "The maximum number of interpretations to return."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_INPUT_LOGGING_MODE("nluAasGrpcInputLoggingMode", InputLoggingMode.PLAIN_TEXT.toString(), new String[]{InputLoggingMode.PLAIN_TEXT.toString(), InputLoggingMode.SUPPRESSED.toString()}, "How NLUaaS logs the input."),

        @Backend(backend = Backend.Value.NLUAAS_GRPC)
        NLUAAS_GRPC_RESOURCE_REQUEST_TIMEOUT_MS("nluAasGrpcResourceRequestTimeoutMs",0, "Time, in ms, to wait for fetching nlu resources. Default is 0 for the NLU server's configured setting."),

        @Backend(backend = Backend.Value.NVC_GRPC)
        NVC_GRPC_SERVER_URL("nvcGrpcServerUrl", null, "Backend url for the nvc grpc client. NOTE: It needs to be a valid URL format."),
        @Backend(backend = Backend.Value.NVC_GRPC)
        NVC_GRPC_USE_USERID("nvcGrpcUseUserId", false, "If available, send the clientUserId to NVC gRPC to identify the user. Note that appendSuffixToClientUserId must also be disabled."),
        @Backend(backend = Backend.Value.NVC_GRPC)
        NVC_GRPC_AUTH_METHOD("nvcGrpcAuthenticationMethod", NvcClient.AUTH_MIX_BEARER_TOKEN, new String[]{NvcClient.AUTH_MIX_BEARER_TOKEN, NvcClient.AUTH_NO_AUTH, NvcClient.AUTH_PASSTHROUGH}, "Authentication method to use for NVC-gRPC."),

        @Backend(backend = Backend.Value.FORGETME_GRPC)
        ASR_FORGET_ME_GRPC_SERVER_URL("asrForgetMeGrpcServerUrl", null, "Backend URL for the ASR ForgetMe gRPC client. NOTE: It needs to be a valid URL format."),
        @Backend(backend = Backend.Value.FORGETME_GRPC)
        ASR_FORGET_ME_GRPC_AUTH_METHOD("asrForgetMeGrpcAuthenticationMethod", KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, new String[]{KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, KryptonGrpcClient.AUTH_NO_AUTH, KryptonGrpcClient.AUTH_PASSTHROUGH}, "Authentication method to use for the ASR ForgetMe gRPC client."),

        @Backend(backend = Backend.Value.FORGETME_GRPC)
        LOG_FORGET_ME_GRPC_SERVER_URL("logForgetMeGrpcServerUrl", null, "Backend URL for the Log ForgetMe gRPC client. NOTE: It needs to be a valid URL format."),
        @Backend(backend = Backend.Value.FORGETME_GRPC)
        LOG_FORGET_ME_GRPC_AUTH_METHOD("logForgetMeGrpcAuthenticationMethod", KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, new String[]{KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN, KryptonGrpcClient.AUTH_NO_AUTH, KryptonGrpcClient.AUTH_PASSTHROUGH}, "Authentication method to use for the Log ForgetMe gRPC client."),
        @Backend(backend = Backend.Value.FORGETME_GRPC)
        LOG_FORGET_ME_GRPC_AUTHORITY_OVERRIDE("logForgetMeGrpcAuthorityOverride", null, "Overrides the :authority header for logForgetMe API calls"),

        @Backend(backend = Backend.Value.MIX)
        MIX_MAX_CLIENT_DATA_KEYS("mixMaxClientDataKeys", 20, "Maximum number of keys to log in Mix client_data. 0 will not log client_data. A value less than 0 becomes INT_MAX"),
        @Backend(backend = Backend.Value.MIX)
        MIX_MAX_CLIENT_DATA_STRING_LENGTH("mixMaxClientDataStringLength", 127, "Maximum string length for keys and values to log in Mix client_data. 0 will not log. A value less than 0 becomes INT_MAX");

        public final String key;
        public final Object defaultValue;
        public final String description;
        private final Object[] validValues;
        private ObjectMapper objectMapper = new ObjectMapper();

        Property(String name, Object defaultValue, String description){
            this(name, defaultValue, null, description);
        }

        Property(String name, Object defaultValue, Object[] validValues, String description){
            this.key = name;
            this.defaultValue = defaultValue;
            this.description = description;
            this.validValues = validValues;

            if(null == defaultValue)
            {
                return;
            }

            try
            {
                jsonGenerator.writeFieldName(key);
                objectMapper.writeValue(jsonGenerator, defaultValue);
            }
            catch (IOException e)
            {
                logger.error("Could not write to JsonGenerator in WebApiConfig!");
            }
        }

        public static Property fromString(String key)
        {
            for (Property p : values())
            {
                if (p.key.equalsIgnoreCase(key))
                {
                    return p;
                }
            }

            throw new IllegalArgumentException("Illegal parameter value: No enum key " + key);
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public Object[] getValidValues() {
            return validValues;
        }

        @Override
        public String getConstantName() {
            return name();
        }

    }
}
