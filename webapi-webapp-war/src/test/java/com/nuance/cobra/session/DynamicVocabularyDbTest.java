package com.nuance.cobra.session;

import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.db.redis.RedisClients;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RedisClients.class })
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class DynamicVocabularyDbTest
{
    CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

    StatefulRedisConnection mockStatefulRedisConnection;
    RedisCommands mockRedisCommands;

    @Before
    public void setUp()
    {
        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "abc123");
        config.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "abc123");
        config.setProperty(WebApiConfig.Property.PERSISTENCE_REDIS_URI.key, "rediss://authKey@hostname.redis.cache.windows.net:6380/0");

        mockStatefulRedisConnection = PowerMockito.mock(StatefulRedisConnection.class);
        mockRedisCommands = PowerMockito.mock(RedisCommands.class);

        PowerMockito.mockStatic(RedisClients.class);
        PowerMockito.when(RedisClients.getStatefulRedisConnection(anyString())).thenReturn(mockStatefulRedisConnection);
        PowerMockito.when(mockStatefulRedisConnection.sync()).thenReturn(mockRedisCommands);
    }

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new DynamicVocabularyDb();
    }

    @Test
    public void saveDynamicVocabToDb()
    {
        PowerMockito.when(mockRedisCommands.mset(any())).thenReturn("1");

        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(DynamicVocabularyTest.getInlineJson().toMap(), false, configs);
        String rc = DynamicVocabularyDb.saveDynamicVocabToDb(configs, dynamicVocabularyInline.getBody());
        assertEquals("1", rc);

        config.clearProperty(WebApiConfig.Property.PERSISTENCE_REDIS_URI.key);
        rc = DynamicVocabularyDb.saveDynamicVocabToDb(configs, dynamicVocabularyInline.getBody());
        assertEquals("", rc);

        config.clearProperty(WebApiConfig.Property.CLIENT_USER_ID.key);
        rc = DynamicVocabularyDb.saveDynamicVocabToDb(configs, dynamicVocabularyInline.getBody());
        assertEquals("", rc);
    }

    @Test
    public void getPersistedWordsets()
    {
        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(DynamicVocabularyTest.getInlineJson().toMap(), false, configs);
        PowerMockito.when(mockRedisCommands.smembers(any())).thenReturn(dynamicVocabularyInline.getBody().keySet());
        Set<String> wordsets = DynamicVocabularyDb.getPersistedWordsets(configs);
        assertEquals(dynamicVocabularyInline.getBody().keySet(), wordsets);
    }

    @Test
    public void deleteUserFromDb()
    {
        config.clearProperty(WebApiConfig.Property.CLIENT_USER_ID.key);
        long rc = DynamicVocabularyDb.deleteUserFromDb(configs);
        assertEquals(-1, rc);

        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "abc123:delete_some_other_key");
        rc = DynamicVocabularyDb.deleteUserFromDb(configs);
        assertEquals(-2, rc);

        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "abc123");
        PowerMockito.when(mockRedisCommands.keys(any())).thenReturn(List.of());
        rc = DynamicVocabularyDb.deleteUserFromDb(configs);
        assertEquals(-3, rc);

        PowerMockito.when(mockRedisCommands.keys(any())).thenReturn(List.of("abc"));
        PowerMockito.when(mockRedisCommands.del(any())).thenReturn((long) 1);
        rc = DynamicVocabularyDb.deleteUserFromDb(configs);
        assertEquals(1, rc);

        PowerMockito.when(mockRedisCommands.keys(any())).thenThrow(new IllegalArgumentException());
        rc = DynamicVocabularyDb.deleteUserFromDb(configs);
        assertEquals(-4, rc);
    }

    @Test
    public void incrementVocabInDb()
    {
        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(DynamicVocabularyTest.getInlineJson().toMap(), false, configs);
        dynamicVocabularyInline.getBody().remove("place-name");

        config.clearProperty(WebApiConfig.Property.CLIENT_USER_ID.key);
        boolean rc = DynamicVocabularyDb.incrementVocabInDb(configs, "add", dynamicVocabularyInline.getBody());
        assertFalse(rc);

        config.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "abc123");
        rc = DynamicVocabularyDb.incrementVocabInDb(configs, "add", dynamicVocabularyInline.getBody());
        assertTrue(rc);

        PowerMockito.when(mockRedisCommands.get(any())).thenReturn("{\"hockey-team\":[{\"spoken\":[\"montreal canadiens\",\"habs\"],\"literal\":\"Montreal Canadiens\"}]}");
        rc = DynamicVocabularyDb.incrementVocabInDb(configs, "remove", dynamicVocabularyInline.getBody());
        assertTrue(rc);

        DynamicVocabularyDb.incrementVocabInDb(configs, "add", dynamicVocabularyInline.getBody());
        dynamicVocabularyInline.getBody().get("hockey-team").remove(1);
        dynamicVocabularyInline.getBody().get("hockey-team").get(0).clearSpokenList();
        dynamicVocabularyInline.getBody().get("hockey-team").get(0).addSpoken("le tricolore");
        rc = DynamicVocabularyDb.incrementVocabInDb(configs, "add", dynamicVocabularyInline.getBody());
        assertTrue(rc);

        PowerMockito.when(mockRedisCommands.get(any())).thenReturn("{\"hockey-team\":[{\"literal\":\"Montreal Canadiens\"}]}");
        dynamicVocabularyInline.getBody().get("hockey-team").get(0).setCanonincal("montreal canadiens");
        rc = DynamicVocabularyDb.incrementVocabInDb(configs, "add", dynamicVocabularyInline.getBody());
        assertTrue(rc);
    }

    @Test
    public void getWordset()
    {
        PowerMockito.when(mockRedisCommands.get(any())).thenReturn("{\"hockey-team\":[{\"spoken\":[\"montreal canadiens\",\"habs\"],\"literal\":\"Montreal Canadiens\"}]}");
        DynamicVocabulary dynamicVocabulary = DynamicVocabularyDb.getWordset("hockey-team", configs);
        assertNotNull(dynamicVocabulary);
    }
}