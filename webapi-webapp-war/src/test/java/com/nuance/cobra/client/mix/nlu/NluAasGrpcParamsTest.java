package com.nuance.cobra.client.mix.nlu;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.DynamicVocabularyDb;
import com.nuance.cobra.session.DynamicVocabularyTest;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.rpc.nlu.v1.InterpretRequest;
import com.nuance.rpc.nlu.v1.InterpretationParameters;
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

import java.net.URL;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DynamicVocabularyDb.class })
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class NluAasGrpcParamsTest
{
    private CombinedConfiguration configs;
    private Configuration config;
    private NluAasGrpcParams params;

    @Before
    public void runsBeforeEachTest()
    {
        configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        config = CobraConfigurationUtils.getHighestConfig(configs);
    }

    @Test
    public void testGetSessionId() {
        params = new NluAasGrpcParams("123", configs);
        assertEquals("123", params.getSessionId());
    }

    @Test
    public void testGetCobraConfig() {
        params = new NluAasGrpcParams("123", configs);
        assertEquals(configs, params.getCobraConfig());
    }

    @Test
    public void testGetUri() {
        config.setProperty(WebApiConfig.Property.NLUAAS_GRPC_SERVER_URL.key, "http://host:443");
        params = new NluAasGrpcParams("session-id", configs);

        URL url = UrlUtils.getServerUrl(params.getUrl());
        assertEquals("host", UrlUtils.getServerHost(url));

        //invalid url test
        config.setProperty(WebApiConfig.Property.NLUAAS_GRPC_SERVER_URL.key, "host:443");
        params = new NluAasGrpcParams("session-id", configs);

        assertNull(UrlUtils.getServerUrl(params.getUrl()));
    }

    @Test
    public void testHasSemanticModelUri() {
        params = new NluAasGrpcParams("session-id", configs);
        assertFalse(params.hasSemanticModelUri());

        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "semantic-model-version");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "test-lang");
        params = new NluAasGrpcParams("session-id", configs);
        assertTrue(params.hasSemanticModelUri());
    }

    @Test
    public void testGetSemanticModelUri() {
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "semantic-model-version");
        config.setProperty(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key, "eng-USA");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("urn:nuance-mix:tag:model/semantic-model-version/mix.nlu?=language=eng-USA", params.getSemanticModelUri());
    }

    @Test
    public void testGetSemanticModelUri_semanticModel() {
        config.setProperty(WebApiConfig.Property.NLE_SEMANTIC_MODEL_URL.key, "semantic-model-url");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("semantic-model-url", params.getSemanticModelUri());
    }

    @Test
    public void testGetInputText() throws ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals(null, params.getInput());

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, "test");
        config.addProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("test", params.getInput().getText());

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, "test");
        config.addProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("test", params.getInput().getText());
    }

    @Test
    public void testGetInputTextKryptonResults() throws InvalidProtocolBufferException, ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals(null, params.getInput());

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, new JSONObject("{\"hypotheses\": [{\"averageConfidence\": 0.922,\"confidence\": 0.842,\"words\": [{\"startMs\": 740,\"endMs\": 1140,\"confidence\": 0.894,\"silence_after_wordMs\": 0,\"text\": \"Good\"},{\"startMs\": 1140,\"endMs\": 1600,\"confidence\": 0.949,\"silence_after_wordMs\": 0,\"text\": \"morning\"}]},{\"averageConfidence\": 0.939,\"words\": [{\"startMs\": 1140,\"endMs\": 1600,\"confidence\": 0.939,\"silence_after_wordMs\": 0,\"text\": \"Morning\"}]}],\"absStartMs\": 0,\"utteranceInfo\": {\"dsp\": {\"numChannels\": 1,\"level\": 23041,\"initialSilenceMs\": 740,\"snrEstimateDb\": 28,\"initialEnergy\": -65.2161,\"finalEnergy\": -13.6442,\"meanEnergy\": 68.1209},\"clippingDurationMs\": 0,\"droppedNonspeechPackets\": 0,\"durationMs\": 2740,\"droppedSpeechPackets\": 0},\"absEndMs\": 2740}}").toMap());
        config.addProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "speechRecognitionVerboseResult");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("type_url: \"type.googleapis.com/nuance.asr.v1.Result\"\n" +
                "value: \"\\030\\264\\025\\\"#\\b\\264\\025*\\036\\r\\000\\000\\340A\\025\\000\\002\\264F\\030\\001 \\344\\005-\\245n\\202\\3025\\245NZ\\301=\\347=\\210B*3\\rP\\215W?\\0251\\bl?2\\021\\n\\004Good\\025/\\335d?\\030\\344\\005 \\364\\b2\\024\\n\\amorning\\025\\252\\361r?\\030\\364\\b \\300\\f*\\033\\025Nbp?2\\024\\n\\aMorning\\025Nbp?\\030\\364\\b \\300\\f\"\n", params.getInput().getAsrResult().toString());
    }

    @Test
    public void testGetUserId() {
        config.addProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "test");
        params = new NluAasGrpcParams("session-id", configs);
        assertEquals("test", params.getUserId());
    }

    @Test
    public void testAssertValidity_allGood() throws ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, "test");
        config.addProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text/plain");
        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "semantic-model-version");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");

        params.assertValidity();
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAssertValidity_missingAppKey() throws ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, "test");

        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "semantic-model-version");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");

        params.assertValidity();
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAssertValidity_missingSemanticModel() throws ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");

        config.addProperty(WebApiConfig.Property.COMMAND_DATA.key, "test");

        params.assertValidity();
    }

    @Test(expected = ExceptionWithExternalResponseErrorCode.class)
    public void testAssertValidity_missingInputText() throws ExceptionWithExternalResponseErrorCode {
        params = new NluAasGrpcParams("session-id", configs);

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test-app-key");

        config.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "semantic-model-version");
        config.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "eng-USA");

        params.assertValidity();
    }

    @Test
    public void testMaxInterpretations() {
        config.setProperty(WebApiConfig.Property.NLUAAS_GRPC_MAX_INTERPRETATIONS.key, 4);
        params = new NluAasGrpcParams("session-id", configs);
        params.setMaxInterpretations(InterpretationParameters.newBuilder());
        assertEquals(4, params.getMaxInterpretations());
    }

    @Test
    public void testMaxInterpretationsDefault() {
        config.setProperty(WebApiConfig.Property.NLUAAS_GRPC_MAX_INTERPRETATIONS.key, -5);
        params = new NluAasGrpcParams("session-id", configs);
        params.setMaxInterpretations(InterpretationParameters.newBuilder());
        assertEquals(-1, params.getMaxInterpretations());
    }

    @Test
    public void loadPersistedWordsets()
    {
        InterpretRequest.Builder interpretRequestBuilder = InterpretRequest.newBuilder();
        params = new NluAasGrpcParams("session-id", configs);

        params.loadPersistedWordsets(interpretRequestBuilder);

        config.setProperty(WebApiConfig.Property.LOAD_PERSISTED_WORDSETS.key, true);
        params.loadPersistedWordsets(interpretRequestBuilder);

        PowerMockito.mockStatic(DynamicVocabularyDb.class);
        PowerMockito.when(DynamicVocabularyDb.getPersistedWordsets(any())).thenReturn(Set.of("place-name", "hockey-team"));
        params.loadPersistedWordsets(interpretRequestBuilder);

        DynamicVocabulary dynamicVocabularyInline = DynamicVocabulary.fromJsonMap(DynamicVocabularyTest.getInlineJson().toMap(), false, configs);
        PowerMockito.when(DynamicVocabularyDb.getWordset(anyString(), any())).thenReturn(dynamicVocabularyInline);
        params.loadPersistedWordsets(interpretRequestBuilder);
        assertTrue(interpretRequestBuilder.getResourcesCount() > 0);
    }
}
