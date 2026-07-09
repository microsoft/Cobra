package com.nuance.plugin.kryptonws.params;

import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.params.KryptonWsParameters.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class KryptonWsParametersTest
{
    @Test
    public void checkDefaults()
    {
        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, null);

        String noInputTimeout = kryptonWsParameters.getConfigurations().getString(KRYPTON_WS_PARAM_NO_INPUT_TIMEOUT);
        assertEquals("disabled", noInputTimeout);
    }

    @Test
    public void checkMergeAndWebapiParam()
    {
        JSONObject sessionParametersJson = new JSONObject().put("idleTimeout", "5000ms");
        JSONObject recognitionParametersJson = new JSONObject().put(KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION, true);

        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(sessionParametersJson, recognitionParametersJson);

        boolean uttDetection = kryptonWsParameters.getConfigurations().getBoolean(KRYPTON_WS_PARAM_DISABLE_UTTERANCE_DETECTION);
        assertTrue(uttDetection);

        String kryptonIdleTimeout = kryptonWsParameters.getConfigurations().getString("idleTimeout");
        assertEquals("5000ms", kryptonIdleTimeout);

        int webapiSessionInactivityTimeoutSeconds = kryptonWsParameters.getConfigurations().getInt("sessionInactivityTimeoutSeconds");
        assertEquals(5, webapiSessionInactivityTimeoutSeconds);

        int webapiSessionInactivityTimeoutSecondsFromCc = kryptonWsParameters.getConfigurations().getInt("sessionInactivityTimeoutSeconds");
        assertEquals(5, webapiSessionInactivityTimeoutSecondsFromCc);
    }

    @Test
    public void testInterpretAddConfig()
    {
        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, null);
        CombinedConfiguration webapiCombinedConfiguration = new CombinedConfiguration();

        assertNull(webapiCombinedConfiguration.getConfiguration(INTERPRETATION_PARAMS_NAME));

        JSONObject kryptonWsInterpretParamsJson = new JSONObject()
                .put("model", "id")
                .put("inputType", "text/plain")
                .put("input", "hello");

        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        kryptonWsSession.setSemanticModel(new JSONObject("{\"id\":\"id\",\"url\":\"myUrl\"}"));
        kryptonWsParameters.interpretAddConfig(kryptonWsInterpretParamsJson, webapiCombinedConfiguration, kryptonWsSession);

        Configuration config = webapiCombinedConfiguration.getConfiguration(INTERPRETATION_PARAMS_NAME);

        assertNotNull(config);
        assertEquals("myUrl", config.getString("semanticModelUrl"));
        assertEquals("text/plain", config.getString("type"));
        assertEquals("hello", config.getString("data"));
    }

    @Test
    public void testInterpretAddConfigKryptonResult()
    {
        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, null);
        CombinedConfiguration webapiCombinedConfiguration = new CombinedConfiguration();

        assertNull(webapiCombinedConfiguration.getConfiguration(INTERPRETATION_PARAMS_NAME));

        JSONObject kryptonWsInterpretParamsJson = new JSONObject()
                .put("model", "id")
                .put("input", Map.of("fakeField", 5))
                .put("inputType", "application/x-nuance-krypton-result");

        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        kryptonWsSession.setSemanticModel(new JSONObject("{\"id\":\"id\",\"url\":\"myUrl\"}"));
        kryptonWsParameters.interpretAddConfig(kryptonWsInterpretParamsJson, webapiCombinedConfiguration,
                kryptonWsSession);

        Configuration config = webapiCombinedConfiguration.getConfiguration(INTERPRETATION_PARAMS_NAME);

        assertNotNull(config);
        assertEquals("myUrl", config.getString("semanticModelUrl"));
        assertEquals("speechRecognitionVerboseResult", config.getString("type"));
        assertEquals(JSONObject.class, config.getProperty("data")
                                             .getClass());
    }

    @Test
    public void testEwapi1602_1()
    {
        JSONObject kryptonParamsJson = new JSONObject()
                .put(KRYPTON_WS_PARAM_SENSITIVTY, 100)
                .put("suppressCallRecording", true)
                .put(KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE, "250ms");

        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, kryptonParamsJson);
        CombinedConfiguration webapiCombinedConfiguration = new CombinedConfiguration();

        kryptonWsParameters.afterCreateSessionAddConfigToWebapi(webapiCombinedConfiguration);

        assertEquals(100, webapiCombinedConfiguration.getInt("kryptonSpeechDetectionSensitivity"));
        assertTrue(webapiCombinedConfiguration.getBoolean("kryptonSuppressCallRecording"));
        assertEquals(250, webapiCombinedConfiguration.getInt("kryptonUtteranceEndSilenceMs"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEwapi1602_2()
    {
        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, new JSONObject().put(KRYPTON_WS_PARAM_UTTERANCE_END_SILENCE, -1));
        CombinedConfiguration webapiCombinedConfiguration = new CombinedConfiguration();

        kryptonWsParameters.afterCreateSessionAddConfigToWebapi(webapiCombinedConfiguration);
    }

    @Test(expected = JSONException.class)
    public void testEwapi1602_3()
    {
        KryptonWsParameters kryptonWsParameters = new KryptonWsParameters(null, new JSONObject().put(KRYPTON_WS_PARAM_SENSITIVTY, -1));
        CombinedConfiguration webapiCombinedConfiguration = new CombinedConfiguration();

        kryptonWsParameters.afterCreateSessionAddConfigToWebapi(webapiCombinedConfiguration);
    }
}
