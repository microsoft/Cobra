package com.nuance.cobra.client.nlucachedb;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.db.redis.RedisClients;
import com.nuance.cobra.utils.response.ResponseHandler;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RedisClients.class })
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class NluCacheDbClientTest
{
    private CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConfig = CobraConfigurationUtils.getHighestConfig(testConfigs);

    private ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
    private StatefulRedisConnection<String, String> mockStatefulRedisConnection = mock(StatefulRedisConnection.class);
    private RedisAsyncCommands<String, String> mockRedisAsyncCommands = mock(RedisAsyncCommands.class);
    private RedisFuture<String> mockRedisFuture = mock(RedisFuture.class);

    private NluCacheDbClient nluCacheDbClient;

    @Before
    public void setUp() throws Exception
    {
        PowerMockito.mockStatic(RedisClients.class);
        PowerMockito.when(RedisClients.getStatefulRedisConnection(anyString())).thenReturn(mockStatefulRedisConnection);
        PowerMockito.when(mockStatefulRedisConnection.async()).thenReturn(mockRedisAsyncCommands);
        PowerMockito.when(mockRedisAsyncCommands.get(anyString())).thenReturn(mockRedisFuture);

        testConfig.setProperty(WebApiConfig.Property.NLU_CACHE_REDIS_URI.key, "rediss://auth@localhost:6380/0");
        testConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");
        testConfig.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        testConfig.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "goodbye");
        testConfig.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, NluCacheDbClientTest.class.getName());
        nluCacheDbClient = new NluCacheDbClient(testConfigs, "abc123", mockResponseHandler);
    }

    @Test
    public void startInterpretation()
    {
        nluCacheDbClient.startInterpretation();
        nluCacheDbClient.getCompleted("{ \"text\": \"later alligator}\"}");
        verify(mockResponseHandler).onResponse(any());

        nluCacheDbClient.getCompleted(null);
        verify(mockResponseHandler).onResponse(any());

        nluCacheDbClient.stopInterpretation();
        verify(mockResponseHandler).onResponse(any());

        String verboseResult = "{\"resultType\":\"FINAL\",\"absStartMs\":0.0,\"absEndMs\":2880.0,\"utteranceInfo\":{\"durationMs\":2880.0,\"clippingDurationMs\":0.0,\"droppedSpeechPackets\":0.0,\"droppedNonspeechPackets\":0.0,\"dsp\":{\"snrEstimateDb\":19.0,\"level\":18433.0,\"numChannels\":1.0,\"initialSilenceMs\":100.0,\"initialEnergy\":-49.1414,\"finalEnergy\":-40.6837,\"meanEnergy\":112.669}},\"hypotheses\":[{\"confidence\":0.964,\"averageConfidence\":0.771,\"rejected\":false,\"formattedText\":\"Pas d'extension Zelda\",\"minimallyFormattedText\":\"Pas d'extension Zelda\",\"words\":[{\"text\":\"Pas\",\"confidence\":0.922,\"startMs\":280.0,\"endMs\":480.0,\"silenceAfterWordMs\":340.0},{\"text\":\"d'\",\"confidence\":0.917,\"startMs\":820.0,\"endMs\":960.0,\"silenceAfterWordMs\":0.0},{\"text\":\"extension\",\"confidence\":0.831,\"startMs\":960.0,\"endMs\":1460.0,\"silenceAfterWordMs\":280.0},{\"text\":\"Zelda\",\"confidence\":0.593,\"startMs\":1740.0,\"endMs\":2180.0,\"silenceAfterWordMs\":0.0}],\"encryptedTokenization\":\"EoxrSG9Sj963VnBXt9LmbCWZIhQzQ9GCHOSM8zpso4DrrX7p6BpFjidrvgKMYRJkxzI2yJIkSntJQHrfMFzgXZTql5vuvUaUocR0rwolwLxApZiIyWXX0rOHo0iZ19D2kmdxmOIG8iug88gXO4/POvZdDp1FwX0bPc/x9v6On61vn6edZUzKX21kVQYpEJzb4Jp7Y5GtqKJJmq6KO1LLF4oMwsjd2DSGVrN4kdDeMGr9xOvZlOyXRwuXcVYKgf+SX3hivlM3Ikq9QCJNeUtE6IDyAmUSHhtwu8yswIltdmay6D8isoYMjt4YFgDbcpugUticIuQdv9GH0fp0fWFMOA==8cZxIoJIHNRJAnEp5ZXCbkIhMgMuqU1neDSsgoj+/GmvV/fAVBF1lweQBmg1xoKBSGDyczIRIML1oG79nGFYjrkr/uEUZ9+9b4P63EAsufDYq16Ku7xer5xTZBvUeHRxaC1dJ+oMQd7pFZQfCb2ghgJbjzacWRnk5ve0yjRqDw5/+JsNJePekM5CgNjax366gsT8AL32K/CMYlPoZF3S1YQ9c03Di4/BPqU8uYEsiK/APfg5fWpO38Ich1agcyqOv00SUt61+5c75tPDlwZUTddWGLIJygrm/ZVfMnsgeUCLJtjmAapKZWiH7gZkQUt+YHFq+iP3gHC0S+WmtrRgupDcEFPPZ6fQ9LhHWBY1YrUEJJARl3zeslKE1FY7rtC7rb3OJqDxpDkS/a83i9f0WwJM57ZEcWoTEkQFFSoTB0RgiEgHxp3mioD4+VHv1eWb\"},{\"averageConfidence\":0.56,\"rejected\":false,\"formattedText\":\"Pas d'extension de Zelda\",\"minimallyFormattedText\":\"Pas d'extension de Zelda\",\"words\":[{\"text\":\"Pas\",\"confidence\":0.924,\"startMs\":280.0,\"endMs\":480.0,\"silenceAfterWordMs\":340.0},{\"text\":\"d'\",\"confidence\":0.713,\"startMs\":820.0,\"endMs\":960.0,\"silenceAfterWordMs\":0.0},{\"text\":\"extension\",\"confidence\":0.637,\"startMs\":960.0,\"endMs\":1460.0,\"silenceAfterWordMs\":240.0},{\"text\":\"de\",\"confidence\":0.087,\"startMs\":1700.0,\"endMs\":1720.0,\"silenceAfterWordMs\":20.0},{\"text\":\"Zelda\",\"confidence\":0.284,\"startMs\":1740.0,\"endMs\":2180.0,\"silenceAfterWordMs\":0.0}],\"encryptedTokenization\":\"EoxrSG9Sj963VnBXt9LmbCWZIhQzQ9GCHOSM8zpso4DrrX7p6BpFjidrvgKMYRJkxzI2yJIkSntJQHrfMFzgXZTql5vuvUaUocR0rwolwLxApZiIyWXX0rOHo0iZ19D2kmdxmOIG8iug88gXO4/POvZdDp1FwX0bPc/x9v6On61vn6edZUzKX21kVQYpEJzb4Jp7Y5GtqKJJmq6KO1LLF4oMwsjd2DSGVrN4kdDeMGr9xOvZlOyXRwuXcVYKgf+SX3hivlM3Ikq9QCJNeUtE6IDyAmUSHhtwu8yswIltdmay6D8isoYMjt4YFgDbcpugUticIuQdv9GH0fp0fWFMOA==8cZxIoJIHNRJAnEp5ZXCbkIhMgMuqU1neDSsgoj+/GmhTCxye8jkT5jGM5zZKw/2UcI2TEPkxkMaP/RwG5gLDXNzFNfrY3sQBp5tWV1waGWKkBlVvuiLIlLjxN8HkqRnyPpWk8AQRg34ayUUDBH6TS8o62vMMwCQRRmUWQNhn9cN1+8G8N7unAyD8VZ0jFoMnjGktbBZtmLMoXhBeXiAoGGew8gQG7HFRNpCsibYesV8rxMPvVHE66DZNo0FAmAFnJASOT+tuspccSNJkVG0QS1MRD7iPDJJPH8yKSz+DShs1Haj8SH7ewO2+ebCJXWG0gi8yhjTKQtAZXVC8Xw3+ssrLD4Gg1N+9omLxSIWIgElZ5hSzgTlFSB7ofzqc8OAFWDK2oiIupC5cpsivf+lz48tqQt7MMpBm/Anz9GHoogUHMVNVcvqn/2eJxa9etlskRhle7ViOC0Q5nc1Sb4vKK5lAYK8vyREKB25yOErDjPO3EM5PQE8T0dHYUhz4OW1U7Kvl0fR3AyA/Wd8WWlX/EegY8TSHWhP1QVswLWt/sQ=\"},{\"averageConfidence\":0.555,\"rejected\":false,\"formattedText\":\"Pas d'extensions Zelda\",\"minimallyFormattedText\":\"Pas d'extensions Zelda\",\"words\":[{\"text\":\"Pas\",\"confidence\":0.865,\"startMs\":280.0,\"endMs\":480.0,\"silenceAfterWordMs\":340.0},{\"text\":\"d'\",\"confidence\":0.804,\"startMs\":820.0,\"endMs\":960.0,\"silenceAfterWordMs\":0.0},{\"text\":\"extensions\",\"confidence\":0.384,\"startMs\":960.0,\"endMs\":1460.0,\"silenceAfterWordMs\":280.0},{\"text\":\"Zelda\",\"confidence\":0.534,\"startMs\":1740.0,\"endMs\":2180.0,\"silenceAfterWordMs\":0.0}],\"encryptedTokenization\":\"EoxrSG9Sj963VnBXt9LmbCWZIhQzQ9GCHOSM8zpso4DrrX7p6BpFjidrvgKMYRJkxzI2yJIkSntJQHrfMFzgXZTql5vuvUaUocR0rwolwLxApZiIyWXX0rOHo0iZ19D2kmdxmOIG8iug88gXO4/POvZdDp1FwX0bPc/x9v6On61vn6edZUzKX21kVQYpEJzb4Jp7Y5GtqKJJmq6KO1LLF4oMwsjd2DSGVrN4kdDeMGr9xOvZlOyXRwuXcVYKgf+SX3hivlM3Ikq9QCJNeUtE6IDyAmUSHhtwu8yswIltdmay6D8isoYMjt4YFgDbcpugUticIuQdv9GH0fp0fWFMOA==8cZxIoJIHNRJAnEp5ZXCbkIhMgMuqU1neDSsgoj+/GmPDEivgddyVLR91dZpsoYkLo7+6Zw+xIWemgHGbSuI1GRa3tZQTsRLbhDUKbeoRv9OSyT+eDqYHpaXk2K52n2KrBKM9UCN2AUEB5nVFK+ZBkUtGyktuVORKuyFqLfBd5dazlgSvzcERg5idgVySwDfWvUoPiXSARSI2k8UVKeRw6T7TLkZ+l8Ve/zL9xe9zNRL3LazP3uWT/Z2jl2/ymB51WJRRd8JzGdqjT1jhTWbeFdXxXC1k8iEKw4H25D2mvAkKSdXvyTDJ5YOI9DF/+m/gIsjO9L+6NWutrVplDA6qJMHAzpa5Zm3HcpO2gMPAmMQJ2cNRSCnt6Ydc++Z5t2ldQ9osuzfiL+EAO8YhnQaDGZ553vXfp6W5e7ZUfYgVKaQtBRROH+zInoPl+S6tKct\"}],\"dataPack\":{\"language\":\"fra-FRA\",\"topic\":\"GEN\",\"version\":\"4.1.0\"},\"notifications\":[{\"severity\":\"SEVERITY_INFO\",\"code\":1004.0,\"data\":{\"x-nuance-supplemental-metrics-result\":\"{\\\"metrics\\\":{\\\"version\\\":\\\"1\\\",\\\"firstAudioToResult\\\":684}}\"},\"message\":{\"messageResourceId\":\"1004\",\"locale\":\"en-US\",\"message\":\"Supplemental metrics.\"}}]}";
        JSONObject verboseResultJson = new JSONObject(verboseResult);
        testConfig.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "speechRecognitionVerboseResult");
        testConfig.setProperty(WebApiConfig.Property.COMMAND_DATA.key, verboseResultJson.toMap());
        nluCacheDbClient.startInterpretation();
        nluCacheDbClient.cancelInterpretation(true);
        verify(mockResponseHandler).onResponse(any());
    }

}