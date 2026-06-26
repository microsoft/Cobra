package com.nuance.cobra.utils.db.redis;

import com.nuance.cobra.utils.metrics.MetricsConstants;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.prometheus.client.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisClients
{
    private static final Logger logger = LogManager.getLogger(RedisClients.class);

    private static Map<String, RedisClient> redisClientMap = new ConcurrentHashMap<>();
    private static Map<String, StatefulRedisConnection<String, String>> statefulRedisConnectionMap = new ConcurrentHashMap<>();

    public static final Histogram REDIS_LATENCY_HISTOGRAM = Histogram.build("webapi_redis_latency_seconds",
                                                                                "Redis latency in seconds")
                                                                        .labelNames(MetricsConstants.TENANT_LABEL)
                                                                        .register();

    RedisClients() { throw new IllegalStateException("Utility class!"); }

    public static StatefulRedisConnection<String, String> getStatefulRedisConnection(String redisUri)
    {
        RedisClient redisClient = redisClientMap.get(redisUri);
        if(redisClient == null)
        {
            redisClient = createRedisClient(redisUri);
        }

        StatefulRedisConnection<String, String> statefulRedisConnection = statefulRedisConnectionMap.get(redisUri);
        if(statefulRedisConnection == null || !statefulRedisConnection.isOpen())
        {
            statefulRedisConnection = createStatefulRedisConnection(redisUri, redisClient);
        }

        return statefulRedisConnection;
    }

    static RedisClient createRedisClient(String redisUri)
    {
        ClientResources clientResources = DefaultClientResources.create();
        RedisClient redisClient = RedisClient.create(clientResources, redisUri);
        redisClient.setOptions(ClientOptions.builder().build());

        redisClientMap.put(redisUri, redisClient);

        return redisClient;
    }

    static StatefulRedisConnection<String, String> createStatefulRedisConnection(String redisUri, RedisClient redisClient)
    {
        logRedisConnecting(redisUri);
        StatefulRedisConnection<String, String> statefulRedisConnection = redisClient.connect();
        logger.trace("Connected to redis");

        statefulRedisConnectionMap.put(redisUri, statefulRedisConnection);

        return statefulRedisConnection;
    }

    static void logRedisConnecting(String redisUri)
    {
        try
        {
            URI uri = new URI(redisUri);
            //Omit the auth data from logs
            logger.trace("Connecting to redis at: {}://{}:{}{}", uri.getScheme(), uri.getHost(), uri.getPort(), uri.getRawPath());
        }
        catch (URISyntaxException e)
        {
            //Do nothing, just instrumentation
        }
    }
}
