package com.nuance.cobra.client.nlucachedb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.InterpretationClient;
import com.nuance.cobra.client.InterpretationClients;
import com.nuance.cobra.client.NluClientUtils;
import com.nuance.cobra.client.response.AppServerResultsResponse;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.db.redis.RedisClients;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.ResponseHandler;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import static com.nuance.cobra.utils.db.redis.RedisClients.REDIS_LATENCY_HISTOGRAM;

public class NluCacheDbClient extends InterpretationClient
{
    private static final Logger logger = LogManager.getLogger(NluCacheDbClient.class);

    @SuppressWarnings({})
    private static final String ENGINE_TYPE = InterpretationClients.NLU_CACHE_DB.getValue();

    private final CombinedConfiguration config;
    private final ResponseHandler responseHandler;

    private boolean hasSentFinalResponse = false;

    StatefulRedisConnection<String, String> statefulRedisConnection;
    RedisFuture<String> redisFuture;

    public static final Counter nluCacheHitCounter = Counter.build()
                                                            .name("webapi_nlu_cache_hit_total")
                                                            .help("NLU cache hits per tenant, command")
                                                            .labelNames(MetricsConstants.TENANT_LABEL)
                                                            .register();

    public static final Counter nluCacheMissCounter = Counter.build()
                                                            .name("webapi_nlu_cache_miss_total")
                                                            .help("NLU cache misses per tenant, command")
                                                            .labelNames(MetricsConstants.TENANT_LABEL)
                                                            .register();

    protected Histogram.Timer redisLatencyHistogramTimer;

    public NluCacheDbClient(CombinedConfiguration requestConfig, String sessionId, ResponseHandler handler)
    {
        super(sessionId);
        config = requestConfig;
        responseHandler = handler;

        String redisUri = requestConfig.getString(WebApiConfig.Property.NLU_CACHE_REDIS_URI.key);
        statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
    }

    @Override
    public void startInterpretation()
    {
        RedisAsyncCommands<String, String> asyncCommands = statefulRedisConnection.async();

        String key = String.format("lang:%s:input:%s", getLang(), getText(true));

        String appKey = config.getString(WebApiConfig.Property.APPLICATION_KEY.key);

        logger.debug("Getting from redis value for \"{}\"", key);
        redisLatencyHistogramTimer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        redisFuture = asyncCommands.get(key);

        redisFuture.thenAccept(this::getCompleted);
    }

    private String getText(boolean lowercase)
    {
        String type = config.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key);

        String text = "";
        if(type.equalsIgnoreCase("text/plain"))
        {
            text = config.getString(WebApiConfig.Property.COMMAND_DATA.key);
        } else if(type.equalsIgnoreCase("speechRecognitionVerboseResult"))
        {
            Object input = config.getProperty(WebApiConfig.Property.COMMAND_DATA.key);
            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = gson.toJsonTree(input).getAsJsonObject();
            text = jsonObject.getAsJsonArray("hypotheses").get(0).getAsJsonObject().get("formattedText").getAsString();
        }

        return lowercase ? text.toLowerCase() : text;
    }

    private String getLang()
    {
        return NluClientUtils.getInterpretationLanguage(config);
    }

    void getCompleted(String value)
    {
        if (redisLatencyHistogramTimer != null) {
            redisLatencyHistogramTimer.observeDuration();
        }

        logger.debug("Got string value from redis: {}", value);

        String appKey = config.getString(WebApiConfig.Property.APPLICATION_KEY.key);

        if(value == null)
        {
            nluCacheMissCounter.labels(appKey).inc();
            value = getUnspecifiedNaNa(getText(false), getLang());
            logger.debug("Got null from redis. Using internal unspecified-na-na result: {}", value);
        } else
        {
            nluCacheHitCounter.labels(appKey).inc();
        }

        JSONObject appserverResultsJson = new JSONObject(value);
        AppServerResultsResponse appServerResultsResponse = new AppServerResultsResponse(ResponseResult.ResponseStatus.SUCCESS, appserverResultsJson, true);
        sendResult(appServerResultsResponse);
    }

    @Override
    public void stopInterpretation()
    {
        logger.debug("stopInterpretation");
        stopOrCancelInterpretation(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Override
    public void cancelInterpretation(boolean isFinal)
    {
        logger.debug("cancelInterpretation: {}", isFinal);
        stopOrCancelInterpretation(ResponseResult.ResponseStatus.CANCELLED);
    }

    private void stopOrCancelInterpretation(ResponseResult.ResponseStatus status)
    {
        InterpretationResponse interpretationResponse = new InterpretationResponse(status, null, true);
        sendResult(interpretationResponse);

        redisFuture.cancel(true);
        redisFuture = null;
    }

    private void sendResult(ResponseResult result)
    {
        if(!hasSentFinalResponse)
        {
            hasSentFinalResponse = true;
            responseHandler.onResponse(result);
        }
    }

    private String getUnspecifiedNaNa(String text, String lang)
    {
        String unspecifiedTemplate = "{" +
                                     "  \"payload\": {" +
                                     //"    \"generic_order\": \"next\"," +
                                     "    \"text\": \"%s\"," +
                                     "    \"intent\": \"unspecified-na-na\"," +
                                     "    \"nlps_version\": \"nlps(z):6.4.300.16-B10;Version: nlps-base-Zeppelin-6.4.300-B00-GMT20171206222658;\"," +
                                     "    \"diagnostic_info\": {" +
                                     "      \"adk_dialog_manager_status\": \"undefined\"," +
                                     "      \"nlps_profile\": \"DTV-BCK-COMPATIBILITY\"," +
                                     "      \"nlps_profile_package_version\": \"r180030\"," +
                                     "      \"third_party_delay\": \"2\"," +
                                     "      \"nlu_annotator\": \"urn:analysisEngineDescription:default?endPoint=qNVCCP%%5FDTV%%5Ffr%%2Dfr&brokerURL=tcp%%3A%%2F%%2Flocalhost%%3A61616&timeout=35000\"," +
                                     "      \"nlps_nlu_type\": \"nlu_preproc\"," +
                                     "      \"ext_map_time\": \"0\"," +
                                     "      \"nlu_component_flow\": \"[Input:TextProcJSON] [FieldID|dm_main] [NLUlib|C-pascal-r$Rev: 94861 $.f20171128.1824] [build|G-r93496M.f20171031.0959] [vlmodel|Version: vlmodels-DTV-%s] [Flow|+VlingoTokenized] [Flow|+NLUPipeline]\"," +
                                     "      \"nlps_profile_package\": \"DTV-BCK-COMPATIBILITY\"," +
                                     "      \"application\": \"LG_TV_NLU_20_20140429\"," +
                                     "      \"nlu_language\": \"%s\"," +
                                     "      \"int_map_time\": \"0\"," +
                                     "      \"nmaid\": \"LG_TV_NLU_20_20140429\"," +
                                     "      \"nlu_version\": \"[NLU_PROJECT:DTV-%s];[Datapack:Version: nlps-%s];[VL-Models:Version: vlmodels-DTV-%s]\"," +
                                     "      \"nlu_use_literal_annotator\": \"0\"," +
                                     "      \"fieldId\": \"dm_main\"" +
                                     "    }" +
                                     "  }," +
                                     "  \"final_response\": 1," +
                                     "  \"status\": \"success\"" +
                                     "}";
        String rc = "{}";
        try
        {
            rc = String.format(unspecifiedTemplate, text, lang, lang, lang, lang, lang);
        } catch (Exception e)
        {
            logger.trace("Caught an exception while formatting: ", e);
        }
        return rc;
    }
}
