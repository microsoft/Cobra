package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NeapLogTest {
    private CommandLog commandLog;
    private NeapLog log;
    private CombinedConfiguration config;

    @Before
    public void before()
    {
        config = new CombinedConfiguration();
        config.addProperty(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key, "topic1");
        commandLog = buildCommandLog();
        log = new NeapLog("", commandLog, config);
    }

    private CommandLog buildCommandLog()
    {
        Command.CommandStatistics mockStats = mock(Command.CommandStatistics.class);
        Command.StatisticsHostInfo mockHost = mock(Command.StatisticsHostInfo.class);
        RequestHeader requestHeader = new RequestHeader();
        RequestLog requestLog = new RequestLog();
        commandLog = new CommandLog();
        commandLog.request = requestLog;
        commandLog.requestHeader = requestHeader;
        requestHeader.channel = "wapiChannel";
        requestHeader.tenant = "tenantKey";
        requestHeader.clientID = 101;
        requestHeader.sessionStartTime = "15413254321";
        requestHeader.applicationID = CommandLog.WAPI_APPLICATION_ID;
        requestHeader.clientApplicationID = "appID";
        requestHeader.clientApplicationVersion = "appVersion";
        requestLog.statistics = mockStats;
        requestLog.statistics.hostInfo = mockHost;
        requestLog.command = "interpretation";
        mockHost.environment = "dev";
        commandLog.session = mock(Session.class);
        when(commandLog.session.getSessionConfig()).thenReturn(config);
        return commandLog;
    }

    @Test
    public void toJson() throws Exception
    {
        String jsonString = log.toJson(false);

        log = NeapLog.fromJson(jsonString);

        assertNotNull(log);
    }


    @Test
    public void toJsonPrettyPrint() throws Exception
    {
        String jsonString = log.toJson(true);

        log = NeapLog.fromJson(jsonString);

        assertNotNull(log);
    }

    @Test
    public void getTenantKey()
    {
        assertEquals(commandLog.requestHeader.tenant, log.getTenantKey());
    }

    @Test
    public void postingEnabled()
    {
        assertTrue(log.postingEnabled());
    }

    @Test
    public void getEdfEndpointUrlProperty()
    {
        assertNotNull(log.getEdfEndpointUrlProperty());
    }

    @Test
    public void getJmsType()
    {
        assertEquals(EdfMessageQueueable.JmsMessageType.EDF_LOG, log.getJmsType());
    }

    @Test
    public void copyTest()
    {
        assertNotNull(new NeapLog(log));
    }

    @Test
    public void topicNameTest()
    {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration config = configs.getConfiguration(0);
        config.setProperty(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key, "NeapLogTest");
        assertNotNull(new NeapLog("", buildCommandLog(), configs));
    }

    @Test
    public void neapHeadersTest() throws IOException, ConfigurationException {

        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Parameters params = new Parameters();

        String jsonConfig = ("{'" + WebApiConfig.Property.REPORTING_TENANT_DATA.key + "': {'my_key': 'my_value'}}")
                .replaceAll("'", "\"");

        Reader overriddenValuesReader = new StringReader(jsonConfig);
        WebApiJSONConfiguration overridesConfig = new WebApiJSONConfiguration();
        overridesConfig.read(overriddenValuesReader);
        overriddenValuesReader.close();

        configs.addConfiguration(overridesConfig, "test-config");

        NeapLog log = new NeapLog("", buildCommandLog(), configs);
        assertNotNull(log);
        String jsonLog = log.toJson(false);
        assertTrue(jsonLog.contains("{\"tenantData\":{\"my_key\":\"my_value\"}}"));

        System.out.println(jsonLog);
    }

    @Test
    public void testListConstructor() throws JsonProcessingException
    {
        NeapLog neapLog2 = new NeapLog("", buildCommandLog(), config);
        NeapLog chainLog = new NeapLog("chain_name", List.of(log, neapLog2), Map.of("key1", "value1", "key2", 5), config);

        assertNotNull(chainLog);
        assertEquals(2, new JSONObject(new JSONObject(chainLog.toJson(false)).getString("data")).getJSONArray("commands").length());
    }

    @Test
    public void testListConstructorSingleAudio() throws JsonProcessingException
    {
        NeapLog neapLog2 = new NeapLog("", buildCommandLog(), config);
        neapLog2.audioBlobPath = "audioblobpath";
        NeapLog chainLog = new NeapLog("chain_name", List.of(log, neapLog2), Map.of("key1", "value1", "key2", 5), config);

        assertNotNull(chainLog);
        assertEquals(neapLog2.audioBlobPath, chainLog.audioBlobPath);
    }

    @Test
    public void testListConstructorMultipleAudio() throws JsonProcessingException
    {
        NeapLog neapLog = new NeapLog("", buildCommandLog(), config);
        neapLog.audioBlobPath = "audioblobpath1";
        NeapLog neapLog2 = new NeapLog("", buildCommandLog(), config);
        neapLog2.audioBlobPath = "audioblobpath2";
        NeapLog chainLog = new NeapLog("chain_name", List.of(neapLog, neapLog2), Map.of("key1", "value1", "key2", 5), config);

        assertNotNull(chainLog);
        assertEquals(neapLog.audioBlobPath, chainLog.audioBlobPath);
    }
}
