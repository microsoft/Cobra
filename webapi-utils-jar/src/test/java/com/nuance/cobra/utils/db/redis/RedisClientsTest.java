package com.nuance.cobra.utils.db.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RedisClient.class})
@PowerMockIgnore("javax.management.*")
public class RedisClientsTest
{
    @Test(expected = IllegalStateException.class)
    public void noConstruct()
    {
        new RedisClients();
    }

    @Test
    public void getStatefulRedisConnection()
    {
        PowerMockito.mockStatic(RedisClient.class);

        RedisClient mockRedisClient = mock(RedisClient.class);
        StatefulRedisConnection<String, String> mockStatefulRedisConnection = mock(StatefulRedisConnection.class);

        PowerMockito.when(RedisClient.create(any(), anyString())).thenReturn(mockRedisClient);
        PowerMockito.when(mockRedisClient.connect()).thenReturn(mockStatefulRedisConnection);

        StatefulRedisConnection<String, String> statefulRedisConnection = RedisClients.getStatefulRedisConnection("rediss://auth@localhost:6380/0");
        assertSame(mockStatefulRedisConnection, statefulRedisConnection);
    }
}
