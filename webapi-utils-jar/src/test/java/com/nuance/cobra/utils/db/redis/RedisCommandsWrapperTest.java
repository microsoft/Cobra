package com.nuance.cobra.utils.db.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RedisClients.class })
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class RedisCommandsWrapperTest
{
    StatefulRedisConnection mockStatefulRedisConnection;
    RedisCommands mockRedisCommands;

    String userId = "abc123";
    String appKey = "abc123";
    String redisUri = "rediss://authKey@hostname.redis.cache.windows.net:6380/0";

    @Before
    public void setUp()
    {
        mockStatefulRedisConnection = PowerMockito.mock(StatefulRedisConnection.class);
        mockRedisCommands = PowerMockito.mock(RedisCommands.class);

        PowerMockito.mockStatic(RedisClients.class);
        PowerMockito.when(RedisClients.getStatefulRedisConnection(anyString())).thenReturn(mockStatefulRedisConnection);
        PowerMockito.when(mockStatefulRedisConnection.sync()).thenReturn(mockRedisCommands);
    }

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new RedisCommandsWrapper();
    }

    @Test
    public void del()
    {
        PowerMockito.when(mockRedisCommands.del(any())).thenReturn((long) 1);
        Long rc = RedisCommandsWrapper.del(List.of("a"), appKey, redisUri);
        assertEquals(Long.valueOf(1), rc);
    }

    @Test
    public void sadd()
    {
        PowerMockito.when(mockRedisCommands.sadd(any(), any())).thenReturn((long) 1);
        Long rc = RedisCommandsWrapper.sadd("a", appKey, Set.of("a"), redisUri);
        assertEquals(Long.valueOf(1), rc);
    }

    @Test
    public void srem()
    {
        PowerMockito.when(mockRedisCommands.srem(any(), any())).thenReturn((long) 1);
        Long rc = RedisCommandsWrapper.srem("a", appKey, Set.of("a"), redisUri);
        assertEquals(Long.valueOf(1), rc);
    }

    @Test
    public void getAsJson()
    {
        PowerMockito.when(mockRedisCommands.get(any())).thenReturn("");
        JSONObject rc = RedisCommandsWrapper.getConceptAsJson("a", appKey, redisUri, "hockey-team");
        assertTrue(rc.getJSONArray("hockey-team").isEmpty());

        PowerMockito.when(mockRedisCommands.get(any())).thenReturn("{\"hockey-team\":[{\"spoken\":[\"montreal canadiens\",\"habs\"],\"literal\":\"Montreal Canadiens\"}]}");
        rc = RedisCommandsWrapper.getConceptAsJson("a", appKey, redisUri, "hockey-team");
        assertEquals("Montreal Canadiens", rc.getJSONArray("hockey-team").getJSONObject(0).getString("literal"));
    }

    @Test
    public void keys()
    {
        PowerMockito.when(mockRedisCommands.keys(any())).thenReturn(List.of(""));
        List<String> rc = RedisCommandsWrapper.keys("userId:abc123:appKey:abc123:*", appKey, redisUri);
        assertEquals(1, rc.size());
    }

    @Test
    public void mset()
    {
        PowerMockito.when(mockRedisCommands.mset(any())).thenReturn("2");
        String rc = RedisCommandsWrapper.mset(Map.of("a", "1", "b", "2"), appKey, redisUri);
        assertEquals("2", rc);
    }

    @Test
    public void smembers()
    {
        PowerMockito.when(mockRedisCommands.smembers(any())).thenReturn(Set.of("a"));
        Set<String> rc = RedisCommandsWrapper.smembers("a", appKey, redisUri);
        assertEquals(1, rc.size());
    }
}