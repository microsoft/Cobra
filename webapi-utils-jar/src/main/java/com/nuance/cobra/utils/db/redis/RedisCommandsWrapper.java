package com.nuance.cobra.utils.db.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.prometheus.client.Histogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nuance.cobra.utils.db.redis.RedisClients.REDIS_LATENCY_HISTOGRAM;

public class RedisCommandsWrapper
{
    private static final Logger logger = LogManager.getLogger(RedisCommandsWrapper.class);

    RedisCommandsWrapper() { throw new IllegalStateException("Utility class!"); }

    public static Long del(List<String> keys, String appKey, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Deleting keys: {}", keys);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        Long rc = redisCommands.del(keys.toArray(new String[0]));
        timer.observeDuration();
        logger.debug("Deleted keys from redis: \"{}\"", keys);
        return rc;
    }

    public static Long sadd(String key, String appKey, Set<String> members, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Adding to set in redis: {} = {}", key, members);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        Long rcSadd = redisCommands.sadd(key, members.toArray(new String[0]));
        timer.observeDuration();
        logger.debug("Completed set addition on redis: {}", rcSadd);
        return rcSadd;
    }

    public static Long srem(String wordsetsKey, String appKey, Set<String> concepts, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Removing from set in redis: {} = {}", wordsetsKey, concepts);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        Long rcSrem = redisCommands.srem(wordsetsKey, concepts.toArray(new String[0]));
        timer.observeDuration();
        logger.debug("Completed set removal on redis: {}", rcSrem);
        return rcSrem;
    }

    public static JSONObject getConceptAsJson(String redisKey, String appKey, String redisUri, String conceptName)
    {
        String rc = get(redisKey, appKey, redisUri);
        if (rc == null || rc.isBlank())
        {
            return (new JSONObject()).put(conceptName, List.of());
        }
        return new JSONObject(rc);
    }

    public static String get(String redisKey, String appKey, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Getting value of key from redis: {}", redisKey);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        String rc = redisCommands.get(redisKey);
        timer.observeDuration();
        logger.debug("Got \"{}\" = \"{}\" from redis", redisKey, rc);
        return rc;
    }

    public static List<String> keys(String keyPattern, String appKey, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Getting keys matching pattern: {}", keyPattern);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        List<String> keys = redisCommands.keys(keyPattern);
        timer.observeDuration();
        logger.debug("Got keys from redis: \"{}\"", keys);
        return keys;
    }

    public static String mset(Map<String, String> msetMap, String appKey, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        String rcMset = redisCommands.mset(msetMap);
        timer.observeDuration();
        logger.debug("Completed setting value on redis: {}", rcMset);
        return rcMset;
    }

    public static Set<String> smembers(String redisKey, String appKey, String redisUri)
    {
        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection(redisUri);
        RedisCommands<String, String> redisCommands = statefulRedisConnection.sync();

        logger.debug("Getting set members from redis: {}", redisKey);
        Histogram.Timer timer = REDIS_LATENCY_HISTOGRAM.labels(appKey).startTimer();
        Set<String> setMembers = redisCommands.smembers(redisKey);
        timer.observeDuration();
        logger.debug("Got set members from redis: \"{}\"", setMembers);
        return setMembers;
    }

}
