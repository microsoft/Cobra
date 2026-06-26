package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiSpeechRecognitionCommandBuilder;
import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN;
import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static com.nuance.plugin.kryptonws.params.KryptonWsParameters.KRYPTON_WS_PARAM_SENSITIVTY;
import static com.nuance.plugin.kryptonws.params.KryptonWsParameters.RECOGNIZE_RECOGNITION_PARAMS_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsRecognizeMethodTest
{
    KryptonWsResponseSender mockKryptonWsResponseSender = mock(KryptonWsResponseSender.class);
    KryptonWsWebapiResponseListener mockKryptonWsWebapiResponseListener = mock(KryptonWsWebapiResponseListener.class);
    KryptonWsSession kryptonWsSession;
    IWebApiEndpoint mockWebApiEndpoint = mock(IWebApiEndpoint.class);
    IWebApiFeatures mockWebApiFeatures = mock(IWebApiFeatures.class);
    CombinedConfiguration configs = KryptonWsConfig.getKryptonWsDefaultCombinedConfig();
    ExecutorService executorService;
    CombinedConfiguration mockConfig;

    static final String RECOGNIZE_METHOD = "{\n" +
            "  \"method\": \"Recognize\",\n" +
            "  \"recognitionParameters\": {\n" +
            "    \"audioFormat\": \"audio/L16;rate=16000\",\n" +
            "    \"startRecognitionTimers\": false\n" +
            "  },\n" +
            "  \"requestId\": \"b84051e3a33f2094\",\n" +
            "  \"sessionId\": \"y7gfy23hud0u9e1h\",\n" +
            "  \"version\": \"1.0\"\n" +
            "}";

    static final String RECOGNIZE_METHOD_WORDSET = "{\n" +
            "  \"method\": \"Recognize\",\n" +
            "  \"recognitionParameters\": {\n" +
            "    \"audioFormat\": \"audio/L16;rate=16000\",\n" +
            "    \"startRecognitionTimers\": false\n" +
            "  },\n" +
            "  \"useWordSets\": [\n" +
            "    \"wordSet1\"\n" +
            "  ],\n" +
            "  \"requestId\": \"b84051e3a33f2094\",\n" +
            "  \"sessionId\": \"y7gfy23hud0u9e1h\",\n" +
            "  \"version\": \"1.0\"\n" +
            "}";

    @Before
    public void setUp()
            throws IOException
    {
        executorService = mock(ExecutorService.class);
        kryptonWsSession = new KryptonWsSession(executorService);

        HierarchicalConfiguration<ImmutableNode> kryptonDlmsPerLanguage = mock(HierarchicalConfiguration.class);
        when(kryptonDlmsPerLanguage.getList(any(), anyString())).thenReturn(new ArrayList<>(List.of(Map.of("url", "someUrl", "weight", "medium"))));
        mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("kryptonDomainLmsPerLanguage", true)).thenReturn(kryptonDlmsPerLanguage);
        ISession mockSession = mock(ISession.class);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        kryptonWsSession.setWebapiSessionId("test");
        when(mockWebApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        kryptonWsSession.setWebApiEndpoint(mockWebApiEndpoint);
        kryptonWsSession.setSessionObjects(Map.of("someId2", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\"}")));
        kryptonWsSession.setCreateSessionLanguagePack(new JSONObject("{\"language\":\"eng-USA\"}"));

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        kryptonWsSession.setWebapiResponseListener(mockKryptonWsWebapiResponseListener);
        kryptonWsSession.setKryptonWsResponseSender(mockKryptonWsResponseSender);

        kryptonWsSession.setWebapiFeatures(mockWebApiFeatures);
        when(mockWebApiFeatures.getConfigs()).thenReturn(configs);
        when(mockConfig.getString(KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN.getKey())).thenReturn((String)KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN.getDefaultValue());

        kryptonWsSession.setKryptonWsParameters(new KryptonWsParameters(null, null));

        when(mockConfig.getConfiguration(RECOGNIZE_RECOGNITION_PARAMS_NAME)).thenReturn(configs.getConfiguration(0));
    }

    @Test
    public void testProcess() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        kryptonWsRecognizeMethod.areMetricsEnabled = true;
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"statistics\":true}}}");
    }

    @Test
    public void testProcessBuiltins() {
        var recoJson = new JSONObject(RECOGNIZE_METHOD);
        recoJson.put("builtinWeights", new JSONObject("{\"AMOUNT\":\"high\",\"DURATION\":10}"));
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(recoJson,
                kryptonWsSession);
        kryptonWsRecognizeMethod.areMetricsEnabled = true;
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"commandChain\",\"commands\":[{\"name\":\"uploadDynamicVocabulary\",\"id\":\"2\",\"parameters\":{\"dynamicVocabularies\":[{\"builtin\":\"AMOUNT\",\"weight\":\"high\",\"id\":\"AMOUNT\",\"type\":\"application/x-nuance-builtin\"},{\"builtin\":\"DURATION\",\"weight\":10,\"id\":\"DURATION\",\"type\":\"application/x-nuance-builtin\"}]}},{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"activeDynamicVocabularySets\":[\"AMOUNT\",\"DURATION\"],\"statistics\":true}}]}");
    }

    @Test
    public void testProcess_invalidRequest() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        kryptonWsRecognizeMethod.areMetricsEnabled = true;
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"statistics\":true}}}");
    }

    @Test
    public void testProcessWordSet() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD_WORDSET),
                kryptonWsSession);
        kryptonWsSession.addWordSet("wordSet1");
        kryptonWsSession.setSessionObjects(Map.of("wordSet1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-pkg\"}")));
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"activeDynamicVocabularySets\":[\"wordSet1\"],\"statistics\":false}}}");
    }

    @Test
    public void testProcessWordSetWithValidWordsetObject() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD_WORDSET),
                kryptonWsSession);
        kryptonWsSession.addWordSet("wordSet1");
        kryptonWsSession.setWordsetObjects(Map.of("wordSet1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-pkg\"}")));
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"activeDynamicVocabularySets\":[\"wordSet1\"],\"statistics\":false}}}");
    }

    @Test
    public void testProcessValidWordSetAndInvalidSessionObject() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD_WORDSET),
                kryptonWsSession);
        kryptonWsSession.addWordSet("wordSet1");
        kryptonWsSession.setSessionObjects(Map.of());
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"statistics\":false}}}");
    }

    @Test
    public void testProcessValidWordSetAndInvalidWordsetObject() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD_WORDSET),
                kryptonWsSession);
        kryptonWsSession.addWordSet("wordSet1");
        kryptonWsSession.setWordsetObjects(Map.of());
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\",\"parameters\":{\"speechRecognitionCodec\":\"pcm_16_16k\",\"wordStream\":true,\"speechRecognitionVerboseResult\":true,\"endPointDetection\":true,\"language\":\"eng-USA\",\"stopOnEndOfSpeech\":true,\"statistics\":false}}}");
    }

    @Test
    public void testProcessInvalidWordSet() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD_WORDSET),
                kryptonWsSession);
        kryptonWsRecognizeMethod.process();

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.process();
        verify(kryptonWsRecognizeMethodToTest).sendResponse(KryptonWsMessage.STATUS.FAILED);
    }

    @Test
    public void testOnAudio() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        var audio = new byte[0];
        kryptonWsRecognizeMethodToTest.onAudio(audio);
        verify(kryptonWsRecognizeMethodToTest).sendAudioToWebapi(audio);
    }

    @Test
    public void testProcessWebapiResponse() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"sessionId\": \"123\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"request1\"},\"result\": {\"final\": true,\"status\": \"success\",\"data\": {\"transcriptions\": [\"Good morning\"],\"confidence\": 930,\"averageWeightedConfidences\": [930],\"words\": [[{\"confidence\": 963,\"word\": \"Good\"},{\"confidence\": 940,\"word\": \"morning\"}]]},\"bestResult\": \"Good morning\",\"noiseLevel\": {\"mean\": 25.47,\"standardDeviation\": 27.34}}}");
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"event\":\"FinalResult\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"SUCCESS\",\"status\":\"COMPLETE\"}}");
        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponse_parseFailure() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\"\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":false,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"failed\",\"errors\":[{\"message\":\"Unable to parse webapi response json\"}]}");
    }

    @Test
    public void testProcessWebapiResponse_intermediateResult() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":false,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"event\":\"PartialResult\",\"data\":{\"utterance_complete\":false,\"completion_cause\":\"SUCCESS\",\"status\":\"IN-PROGRESS\"}}");
    }

    @Test
    public void testProcessWebapiResponse_shouldInterpret() {
        // arrange
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":true,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        // assert
        verify(mockKryptonWsResponseSender, never()).sendResponse(any());
        verify(kryptonWsRecognizeMethodToTest, never()).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest, never()).cleanup();

        verify(executorService).submit(any(Runnable.class));

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"interpretation\",\"id\":\"request2\"},\"result\":{\"final\":true,\"status\":\"success\",\"interpretations\":[{\"concepts\":[{\"concept\":\"FLOWER\",\"value\":\"Tulips\",\"literal\":\"tulips\",\"begin\":10,\"end\":16},{\"concept\":\"FLOWER\",\"value\":\"ROSES\",\"literal\":\"roses\",\"begin\":21,\"end\":26}],\"confidence\":100,\"literal\":\"Can I buy tulips and roses?\",\"intent\":\"BUY_FLOWERS\",\"origin\":\"SSM\"}],\"bestInterpretation\":{\"concepts\":[{\"concept\":\"FLOWER\",\"value\":\"Tulips\",\"literal\":\"tulips\",\"begin\":10,\"end\":16},{\"concept\":\"FLOWER\",\"value\":\"ROSES\",\"literal\":\"roses\",\"begin\":21,\"end\":26}],\"confidence\":100,\"literal\":\"Can I buy tulips and roses?\",\"intent\":\"BUY_FLOWERS\",\"origin\":\"SSM\"}}}");

        // assert
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        assertEquals(new JSONObject("{\"method\":\"Event\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"SUCCESS\",\"status\":\"COMPLETE\"},\"requestId\":\"b84051e3a33f2094\",\"interpretationStatus\":\"complete\",\"event\":\"FinalResult\",\"version\":\"1.0\",\"interpretationData\":{\"interpretations\":[{\"confidence\":100,\"origin\":\"SSM\",\"formattedLiteral\":\"Can I buy tulips and roses?\",\"mentions\":[{\"formattedEnd\":16,\"concept\":\"FLOWER\",\"formattedLiteral\":\"tulips\",\"end\":16,\"value\":\"Tulips\",\"begin\":10,\"formattedBegin\":10,\"literal\":\"tulips\"},{\"formattedEnd\":26,\"concept\":\"FLOWER\",\"formattedLiteral\":\"roses\",\"end\":26,\"value\":\"ROSES\",\"begin\":21,\"formattedBegin\":21,\"literal\":\"roses\"}],\"literal\":\"Can I buy tulips and roses?\",\"intention\":\"BUY_FLOWERS\"}]}}").toMap(), new JSONObject(argumentCaptor.getAllValues().get(0)).toMap());
        assertEquals(new JSONObject("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}").toMap(), new JSONObject(argumentCaptor.getAllValues().get(1)).toMap());

        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponse_shouldInterpretWithNoMatchIntent() {
        // arrange
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);

        // act: Send speech recognition
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":true,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        // assert
        verify(mockKryptonWsResponseSender, never()).sendResponse(any());
        verify(kryptonWsRecognizeMethodToTest, never()).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest, never()).cleanup();

        verify(executorService).submit(any(Runnable.class));

        // act: Send interpretation with NO_MATCH intent
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"interpretation\",\"id\":\"request2\"},\"result\":{\"final\":true,\"status\":\"success\",\"interpretations\":[{\"concepts\":[{\"concept\":\"FLOWER\",\"value\":\"Tulips\",\"literal\":\"tulips\",\"begin\":10,\"end\":16},{\"concept\":\"FLOWER\",\"value\":\"ROSES\",\"literal\":\"roses\",\"begin\":21,\"end\":26}],\"confidence\":100,\"literal\":\"Can I buy tulips and roses?\",\"intent\":\"NO_MATCH\",\"origin\":\"SSM\"}],\"bestInterpretation\":{\"concepts\":[{\"concept\":\"FLOWER\",\"value\":\"Tulips\",\"literal\":\"tulips\",\"begin\":10,\"end\":16},{\"concept\":\"FLOWER\",\"value\":\"ROSES\",\"literal\":\"roses\",\"begin\":21,\"end\":26}],\"confidence\":100,\"literal\":\"Can I buy tulips and roses?\",\"intent\":\"NO_MATCH\",\"origin\":\"SSM\"}}}");

        // assert
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());

        JSONObject expectedEventResponse = new JSONObject("{\"method\":\"Event\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"NO-MATCH\",\"status\":\"COMPLETE\"},\"requestId\":\"b84051e3a33f2094\",\"interpretationStatus\":\"complete\",\"event\":\"FinalResult\",\"version\":\"1.0\",\"interpretationData\":{\"interpretations\":[{\"confidence\":100,\"origin\":\"SSM\",\"formattedLiteral\":\"Can I buy tulips and roses?\",\"mentions\":[{\"formattedEnd\":16,\"concept\":\"FLOWER\",\"formattedLiteral\":\"tulips\",\"end\":16,\"value\":\"Tulips\",\"begin\":10,\"formattedBegin\":10,\"literal\":\"tulips\"},{\"formattedEnd\":26,\"concept\":\"FLOWER\",\"formattedLiteral\":\"roses\",\"end\":26,\"value\":\"ROSES\",\"begin\":21,\"formattedBegin\":21,\"literal\":\"roses\"}],\"literal\":\"Can I buy tulips and roses?\",\"intention\":\"NO_MATCH\"}]}}");
        assertEquals(expectedEventResponse.toMap(), new JSONObject(argumentCaptor.getAllValues().get(0)).toMap());

        JSONObject expectedCompletionResponse = new JSONObject("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}");
        assertEquals(expectedCompletionResponse.toMap(), new JSONObject(argumentCaptor.getAllValues().get(1)).toMap());

        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponse_shouldInterpret_interpretWasCancelled()
    {
        // arrange
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":true,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        // assert
        verify(mockKryptonWsResponseSender, never()).sendResponse(any());
        verify(kryptonWsRecognizeMethodToTest, never()).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest, never()).cleanup();

        verify(executorService).submit(any(Runnable.class));

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"interpretation\",\"id\":\"request2\"},\"result\":{\"final\":true,\"status\":\"cancelled\"}}");

        // assert
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"interpretationStatus\":\"failed\",\"interpretationErrors\":[{\"message\":\"Command was cancelled.\"}],\"event\":\"FinalResult\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"SUCCESS\",\"status\":\"COMPLETE\"}}");
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}");
        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponse_shouldInterpret_interpretError()
    {
        // arrange
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"request1\"},\"result\":{\"final\":true,\"status\":\"success\",\"data\":{\"transcriptions\":[\"Good morning\"],\"confidence\":930,\"averageWeightedConfidences\":[930],\"words\":[[{\"confidence\":963,\"word\":\"Good\"},{\"confidence\":940,\"word\":\"morning\"}]]},\"bestResult\":\"Good morning\",\"noiseLevel\":{\"mean\":25.47,\"standardDeviation\":27.34}}}");

        // assert
        verify(mockKryptonWsResponseSender, never()).sendResponse(any());
        verify(kryptonWsRecognizeMethodToTest, never()).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest, never()).cleanup();

        verify(executorService).submit(any(Runnable.class));

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"name\":\"interpretation\",\"id\":\"request2\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[{\"message\":\"An error occurred.\",\"code\":1234}]}}");

        // assert
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"interpretationStatus\":\"failed\",\"interpretationErrors\":[{\"message\":\"An error occurred.\",\"code\":1234}],\"event\":\"FinalResult\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"SUCCESS\",\"status\":\"COMPLETE\"}}");
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}");
        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponse_metrics() {
        configs.getConfiguration(0).setProperty(KRYPTON_WS_ENABLE_METRICS.getKey(), true);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"sessionId\": \"123\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"request1\"},\"result\":{\"statistics\": {\"hostInfo\": {\"version\": \"2.21.0-SNAPSHOT\"},\"startTime\": \"2021/03/29 20:47:43.533\",\"endTime\": \"2021/03/29 20:47:46.470\",\"firstIntermediateResultTime\": \"2021/03/29 20:47:44.971\",\"lastIntermediateResultTime\": \"2021/03/29 20:47:44.993\",\"firstAudioBufferReceivedTime\": \"2021/03/29 20:47:43.788\",\"lastAudioBufferReceivedTime\": \"2021/03/29 20:47:45.806\",\"directives\": [{\"name\": \"STOP\",\"startTime\": \"2021/03/29 20:47:45.985\"}],\"events\": [{\"name\": \"startOfSpeech\",\"startTime\": \"2021/03/29 20:47:44.613\"}]},\"final\": true,\"status\": \"success\",\"data\": {\"transcriptions\": [\"Good morning\"],\"confidence\": 930,\"averageWeightedConfidences\": [930],\"words\": [[{\"confidence\": 963,\"word\": \"Good\"},{\"confidence\": 940,\"word\": \"morning\"}]]},\"bestResult\": \"Good morning\",\"noiseLevel\": {\"mean\": 25.47,\"standardDeviation\": 27.34}}}");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("COMPLETE", resObject.getJSONObject("data").get("status"));
        assertEquals("SUCCESS", resObject.getJSONObject("data").get("completion_cause"));
        assertTrue(resObject.getJSONObject("data").getBoolean("utterance_complete"));
        assertEquals("Event", resObject.get("method"));
        assertEquals("FinalResult", resObject.get("event"));
        assertNotNull(resObject.get("metrics"));
        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponseMetrics() {
        configs.getConfiguration(0).setProperty(KRYPTON_WS_ENABLE_METRICS.getKey(), true);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"147274a5-81a1-4b59-9a01-2c0ee7887eed\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"3\"},\"result\":{\"final\":true,\"status\":\"success\",\"statistics\":{\"hostInfo\":{\"region\":\"east-us\",\"environment\":\"dev\",\"version\":\"2.31.2-SNAPSHOT\"},\"startTime\":\"2021/06/23 14:10:21.452\",\"endTime\":\"2021/06/23 14:10:22.343\",\"engineBufferedAudioChunks\":0,\"engineSentAudioChunks\":143,\"engineFirstAudioChunk\":\"2021/06/23 14:10:21.934\",\"engineLastAudioChunk\":\"2021/06/23 14:10:22.300\",\"firstIntermediateResultTime\":\"2021/06/23 14:10:22.168\",\"lastIntermediateResultTime\":\"2021/06/23 14:10:22.304\",\"firstAudioBufferReceivedTime\":\"2021/06/23 14:10:21.934\",\"lastAudioBufferReceivedTime\":\"2021/06/23 14:10:22.318\",\"modelVersion\":\"urn:nuance-mix:tag:model/jfethier_context_tag_8/mix.asr?=language=eng-USA\",\"events\":[{\"name\":\"startOfSpeech\",\"startTime\":\"2021/06/23 14:10:21.949\"},{\"name\":\"endOfSpeech\",\"startTime\":\"2021/06/23 14:10:22.301\"}],\"enableSpeechRecognitionSpeakerProfile\":false,\"mediaDuration\":2880,\"engineRequests\":{\"dlms\":[{\"uri\":\"urn:nuance-mix:tag:model/jfethier_context_tag_8/mix.asr?=language=eng-USA\"}],\"topic\":\"GEN\",\"language\":\"eng-USA\",\"engine\":\"krypton-grpc\"},\"engineResponses\":{\"grpcHeaders\":{\"date\":\"Wed, 23 Jun 2021 14:10:21 GMT\",\"x-request-id\":\"a05c91b2-412e-4ced-bd8a-83cf04c1e468\",\"server\":\"envoy\",\"grpc-accept-encoding\":\"identity,deflate,gzip\",\"content-type\":\"application/grpc\",\"accept-encoding\":\"identity,gzip\"}}},\"data\":{\"transcriptions\":[\"Good morning how are you doing today\"],\"confidence\":915,\"averageWeightedConfidences\":[978],\"words\":[[{\"confidence\":984.0,\"word\":\"Good\"},{\"confidence\":988.0,\"word\":\"morning\"},{\"confidence\":980.0,\"word\":\"how\"},{\"confidence\":980.0,\"word\":\"are\"},{\"confidence\":982.0,\"word\":\"you\"},{\"confidence\":982.0,\"word\":\"doing\"},{\"confidence\":969.0,\"word\":\"today\"}]]},\"bestResult\":\"Good morning how are you doing today\",\"noiseLevel\":{\"mean\":1.21,\"standardDeviation\":1.28},\"verboseResult\":{\"resultType\":\"FINAL\",\"absStartMs\":0.0,\"absEndMs\":2860.0,\"utteranceInfo\":{\"durationMs\":2860.0,\"clippingDurationMs\":0.0,\"droppedSpeechPackets\":0.0,\"droppedNonspeechPackets\":0.0,\"dsp\":{\"snrEstimateDb\":55.0,\"level\":18945.0,\"numChannels\":1.0,\"initialSilenceMs\":460.0,\"initialEnergy\":-80.2776,\"finalEnergy\":-75.9371,\"meanEnergy\":142.439}},\"hypotheses\":[{\"confidence\":0.915,\"averageConfidence\":0.978,\"rejected\":false,\"formattedText\":\"Good morning how are you doing today\",\"minimallyFormattedText\":\"Good morning how are you doing today\",\"words\":[{\"text\":\"Good\",\"confidence\":0.984,\"startMs\":460.0,\"endMs\":720.0,\"silenceAfterWordMs\":0.0},{\"text\":\"morning\",\"confidence\":0.988,\"startMs\":720.0,\"endMs\":1120.0,\"silenceAfterWordMs\":0.0},{\"text\":\"how\",\"confidence\":0.98,\"startMs\":1120.0,\"endMs\":1260.0,\"silenceAfterWordMs\":0.0},{\"text\":\"are\",\"confidence\":0.98,\"startMs\":1260.0,\"endMs\":1560.0,\"silenceAfterWordMs\":0.0},{\"text\":\"you\",\"confidence\":0.982,\"startMs\":1560.0,\"endMs\":1680.0,\"silenceAfterWordMs\":0.0},{\"text\":\"doing\",\"confidence\":0.982,\"startMs\":1680.0,\"endMs\":2020.0,\"silenceAfterWordMs\":0.0},{\"text\":\"today\",\"confidence\":0.969,\"startMs\":2020.0,\"endMs\":2280.0,\"silenceAfterWordMs\":0.0}],\"encryptedTokenization\":\"HwWlZZeXRpK7aE+Q1PsS4+ZGYk1I3mR6egYn1Z/8E9D5KURGFBD4IW8+KRjYCWuh6TFh/OHALH6iIOWr+3J5W2nMi4+h7fG6sW+2M3TVBzf4Yc+fIO8UAup+UBLrZYK4jk7+LqY9ybHCvdeQDiJ8cI34NnA0iHr+mo6GKO6/Mero+/nAdtZTx0yLu9EvNi1SYGbMssepjPbOUIus26ziVPQOdN5o4ZeOfJVW0Q+78SNLMyrwvdLis8qlPaau0Ip8GEpCzZeSF0MFnRdjtglN1cI6MwVkSGHrx4xIwsLJza4lheghCr/xS3uC0bjsiO8nohdM9M3iKgjSZoiPAUUsVw==ZAIhpH+zJzY3eSr2d12TmbgkENAPvlZ9ulSNBj0cXPCsV2EM2Op7aY5lb215TK7qli4yCI2O8rltu4eWM5DxxbD3WXKL+FR6k0dDKmJjrBKYCwPEs5j0XCVE1Cx4SQaoXAOQQPV8yQxN51JwhkkI6x6WWmWmt8g4DzaCe1MLrMmjOb2hFDhEgDa2jn3svT96cMS/LVBmAqMCR3MIl6Flsh94Bx/3KUXn6Pq8i/rNcnz0Ihh6lEamHGmRKCwEWzlm+1QKrCJGrcaMH5bH5xDZ3IWiGUU9p9XthQyj6sET6FEyYaKnnKZyFjA/BNId10mbFzZObqZoYHR5hQ8eWXql/Yx6/H2Cd6c/P/GOyWdOIrMamuQ5iN+XY7l1hwoX+EmpNP3UaXer1SffxZx5wGh1hMN/6lmtEhsEkgreCQ/4e5twGqKQaxZrZmWRQhmRwpyaBY2p3DsWtX9R+xbRLyvep/9mML2iGohXCrMy2viF4+PbDmFlMwyTF6IyZNGiColjoRCj8AyESWc0k1FmSx/uTtLEowexf/+E38IABwJ/RV+9ErwLqemMjQVEDJNPvWr77aUerPJ7XVjj9/qDO+Z6eRZ6UKJPbxu4VT4VL294UEWVCbAnBahnsgQ+ug9cG0Q9Im1Z8rc1HaAmdnrSl00/uCHUhmX3A0vfWIi25BZ+Nd0Qw58/9Cr3MN1HDoi+1yN+o96kSd+cnSm0/WayS7LnO9QBS4K/tHillVaLiPTcMhQcef1ZFGawjk95WeWF4VgfIcHztFfuL7W2kbXRp/0O/g==\"}],\"dataPack\":{\"language\":\"eng-USA\",\"topic\":\"GEN\",\"version\":\"4.4.0\"},\"notifications\":[{\"severity\":\"SEVERITY_INFO\",\"code\":1004.0,\"data\":{\"x-nuance-supplemental-metrics-result\":\"{\\\"metrics\\\":{\\\"addedWords\\\":{\\\"dlm\\\":{\\\"jfethier_context_tag_8\\\":4},\\\"wordset\\\":[{\\\"wordsets\\\":[\\\"Pharmacy_20180626\\\"],\\\"words\\\":5}]},\\\"weights\\\":{\\\"dlm\\\":{\\\"jfethier_context_tag_8\\\":250},\\\"wordset\\\":[{\\\"wordsets\\\":[\\\"Pharmacy_20180626\\\"],\\\"weight\\\":100}]},\\\"version\\\":\\\"1\\\",\\\"firstAudioToResult\\\":396}}\"},\"message\":{\"messageResourceId\":\"1004\",\"locale\":\"en-US\",\"message\":\"Supplemental metrics.\"}}]}}}");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertNotNull(resObject.getJSONObject("metrics"));
        JSONObject metrics = resObject.getJSONObject("metrics");
        assertNotNull(metrics.get("elapsed"));
        assertNotNull(metrics.getJSONArray("timestamps"));
        assertEquals(2, metrics.getJSONArray("timestamps").length());
        assertEquals("pcm_16_8k", metrics.getString("media"));
    }

    @Test
    public void testProcessWebapiResponseError() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"result\": {\"final\": true,\"status\": \"error\",\"errors\": [{\"message\": \"Error parsing the request: #: \\\"/command/parameters\\\" -- object instance has properties which are not allowed by the schema: [\\\"dwq\\\"].\",\"code\": 1100}]}}");
        verify(kryptonWsRecognizeMethodToTest).sendErrorResponseFromWebapiResponseObject(any());
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponseCancelled() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"sessionId\": \"00f82f35-1f19-4549-b909-5f180f413b87\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"8\"},\"result\": {\"final\": true,\"status\": \"cancelled\",\"noiseLevel\": {\"mean\": 1538.99,\"standardDeviation\": 2524.57}}}");
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\",\"completionCause\":\"ERROR\"}");
        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponseNoMatch() throws JsonProcessingException
    {
        // arrange
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);

        CombinedConfiguration combinedConfiguration = mock(CombinedConfiguration.class);
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(combinedConfiguration);
        when(combinedConfiguration.getBoolean("kryptonWsEnableMetrics")).thenReturn(true);

        String lastReceivedPartialResult = "{\"type\":\"result\",\"sessionId\":\"a9276f19-d1b8-4527-b139-32d2de86ddc6\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\"},\"result\":{\"final\":false,\"status\":\"success\",\"data\":{\"transcriptions\":[\"\"],\"confidence\":0,\"averageWeightedConfidences\":[0],\"words\":[[]]},\"noiseLevel\":{\"mean\":20.95,\"standardDeviation\":22.55},\"verboseResult\":{\"hypotheses\":[{}],\"absStartMs\":0.0,\"utteranceInfo\":{\"dsp\":{\"numChannels\":1.0,\"level\":24065.0,\"initialSilenceMs\":2740.0,\"snrEstimateDb\":22.0,\"initialEnergy\":-55.5592,\"finalEnergy\":-55.5592,\"meanEnergy\":60.6007},\"clippingDurationMs\":0.0,\"droppedNonspeechPackets\":0.0,\"durationMs\":2740.0,\"droppedSpeechPackets\":0.0},\"absEndMs\":2740.0}}}";
        Whitebox.setInternalState(kryptonWsRecognizeMethodToTest, "lastReceivedPartialResult", WebApiResponse.fromJson(lastReceivedPartialResult));

        // act
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"a9276f19-d1b8-4527-b139-32d2de86ddc6\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"2\"},\"result\":{\"final\":true,\"status\":\"noMatch\",\"statistics\":{\"hostInfo\":{\"region\":\"ma\",\"environment\":\"Prod\",\"version\":\"2.31.2-SNAPSHOT\"},\"startTime\":\"2021/04/08 16:26:15.410\",\"endTime\":\"2021/04/08 16:26:28.659\",\"firstIntermediateResultTime\":\"2021/04/08 16:26:21.870\",\"lastIntermediateResultTime\":\"2021/04/08 16:26:21.870\",\"firstAudioBufferReceivedTime\":\"2021/04/08 16:26:15.477\",\"lastAudioBufferReceivedTime\":\"2021/04/08 16:26:28.658\",\"modelVersion\":\"notFound\",\"events\":[{\"name\":\"startOfSpeech\",\"startTime\":\"2021/04/08 16:26:15.612\"},{\"name\":\"endOfSpeech\",\"startTime\":\"2021/04/08 16:26:20.043\"}],\"enableSpeechRecognitionSpeakerProfile\":false,\"mediaDuration\":2760,\"engineRequests\":{\"language\":\"eng-USA\",\"engine\":\"krypton\"}},\"noiseLevel\":{\"mean\":20.95,\"standardDeviation\":22.55}}}");

        // assert
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        assertEquals("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"event\":\"FinalResult\",\"data\":{\"utterance_complete\":true,\"absEnd\":2740,\"absStart\":0,\"completion_cause\":\"NO-MATCH\",\"utterance_info\":{\"duration\":2740,\"dropped_speech_packets\":0,\"mean_energy\":60.6007,\"initial_silence\":2740,\"level\":24065,\"initial_energy\":-55.5592,\"final_energy\":-55.5592,\"stereo\":false,\"clipping_duration\":0,\"snr_estimate\":22,\"dropped_non_speech_packets\":0},\"nBest\":[{}],\"status\":\"COMPLETE\"}}", argumentCaptor.getAllValues().get(0));
        assertEquals("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}", argumentCaptor.getAllValues().get(1));

        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponseNoInput() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"sessionId\": \"00f82f35-1f19-4549-b909-5f180f413b87\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"9\"},\"result\": {\"final\": true,\"status\": \"noInput\",\"noiseLevel\": {\"mean\": 225.27,\"standardDeviation\": 405.75}}}");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockKryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        assertEquals("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"event\":\"FinalResult\",\"data\":{\"utterance_complete\":true,\"completion_cause\":\"NO-INPUT-TIMEOUT\",\"status\":\"COMPLETE\"}}", argumentCaptor.getAllValues().get(0));
        assertEquals("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"y7gfy23hud0u9e1h\",\"requestId\":\"b84051e3a33f2094\",\"responseTo\":\"Recognize\",\"status\":\"complete\"}", argumentCaptor.getAllValues().get(1));

        verify(kryptonWsRecognizeMethodToTest).sendCompleteResponse();
        verify(kryptonWsRecognizeMethodToTest).cleanup();
    }

    @Test
    public void testProcessWebapiResponseStartOfSpeech() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        kryptonWsRecognizeMethod.processWebapiResponse("{\"type\": \"event\",\"sessionId\": \"1394f13b-3699-4d55-bfc3-71ec96aadf1b\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"4\"},\"event\": {\"name\": \"startOfSpeech\"}}");
        verify(mockKryptonWsResponseSender).sendResponse("{\"method\":\"Event\",\"version\":\"1.0\",\"requestId\":\"b84051e3a33f2094\",\"event\":\"StartOfSpeech\"}");
    }

    @Test
    public void testProcessWebapiResponseDynamicVocab() {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);
        KryptonWsRecognizeMethod kryptonWsRecognizeMethodToTest = spy(kryptonWsRecognizeMethod);
        kryptonWsRecognizeMethodToTest.processWebapiResponse("{\"type\": \"result\",\"sessionId\": \"123\",\"command\": {\"name\": \"uploadDynamicVocabulary\",\"id\": \"request1\"},\"result\": {\"final\": true,\"status\": \"error\",\"errors\": [{\"code\": 1400,\"message\": \"cool\"}]}}}");
        verify(mockKryptonWsResponseSender, never()).sendResponse(any());
    }


    @Test
    public void startStopRecognitionTimer()
            throws NoSuchFieldException, IllegalAccessException
    {
        KryptonWsSession kryptonWsSession = mock(KryptonWsSession.class);
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        CombinedConfiguration combinedConfiguration = mock(CombinedConfiguration.class);
        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        ISession session = mock(ISession.class);
        ICommand command = mock(ICommand.class);
        KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = mock(KryptonWsWebapiResponseListener.class);

        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD),
                kryptonWsSession);

        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(combinedConfiguration);
        when(combinedConfiguration.getInt("utteranceMaxTimeSeconds")).thenReturn(30);

        when(kryptonWsSession.getWebapiSessionId()).thenReturn("abc123");
        when(kryptonWsSession.getWebapiSession()).thenReturn(session);
        when(session.getCommand()).thenReturn(command);
        when(command.getCommandId()).thenReturn("1");
        when(kryptonWsSession.getWebapiResponseListener()).thenReturn(kryptonWsWebapiResponseListener);
        when(kryptonWsSession.getExecutorService()).thenReturn(executorService);

        assertEquals(KryptonWsMessage.STATUS.COMPLETE, kryptonWsRecognizeMethod.startRecognitionTimer());
        kryptonWsRecognizeMethod.stopRecognitionTimer();

        TestUtils.inject(kryptonWsRecognizeMethod, KryptonWsRecognizeMethod.class, "recognitionTimerFuture", scheduledFuture);
        assertEquals(KryptonWsMessage.STATUS.NO_ACTION, kryptonWsRecognizeMethod.startRecognitionTimer());
        kryptonWsRecognizeMethod.stopRecognitionTimer();
    }

    @Test
    public void setLanguageAndTopic()
    {
        kryptonWsSession.setCreateSessionLanguagePack(new JSONObject("{\"language\":\"eng-USA\", \"topic\":\"SARS2\"}"));
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);
        WebapiSpeechRecognitionCommandBuilder wapiCommandBuilder = mock(WebapiSpeechRecognitionCommandBuilder.class);
        kryptonWsRecognizeMethod.setLanguageAndTopic(wapiCommandBuilder);
        verify(wapiCommandBuilder).language("eng-USA");
        verify(wapiCommandBuilder).topic("SARS2");
    }

    @Test
    public void invalidRecognitionTimerInput(){
        String methodJson = "  {\"domainLmWeights\": {  \"lm_ARK@speechworks.com\": 100},\"method\": \"Recognize\",\"model\": \"USAA@speechworks.com\",\"recognitionParameters\": {  \"startRecognitionTimers\": 123},\"requestId\": \"b84051e3a33f2094-00004\",\"responseType\": \"application/x-vnd.nuance.emma+xml\",\"sensitive\": \"off\",\"sessionId\": \"b84051e3a33f2094\",\"version\": \"1.0\",\"wordsets\": []  }\n";

        configs.setProperty("startRecognitionTimers", 123);
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(configs);
        KryptonWsRecognizeMethod recognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(methodJson), kryptonWsSession);
        recognizeMethod.process();
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockKryptonWsResponseSender, times(1)).sendResponse(argumentCaptor.capture());
        assertTrue(argumentCaptor.getAllValues().get(0).contains("'startRecognitionTimers' cannot be converted to class java.lang.Boolean. Value is: '123'"));

    }

    @Test
    public void buildInterpretationMetrics() throws JsonProcessingException {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);
        kryptonWsRecognizeMethod.interpretationStartTime = KryptonWsMethod.getCurrentUTCTime();
        WebApiResponse webApiResponse = WebApiResponse.fromJson("{\"type\": \"result\",\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"success\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}");
        Map<String, Object> metrics = KryptonWsInterpretMethod.buildInterpretationMetrics(webApiResponse, kryptonWsRecognizeMethod.interpretationStartTime);
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("elapsed"));
        assertTrue(metrics.containsKey("pipeline"));
        assertTrue(metrics.containsKey("timestamps"));
        assertTrue(metrics.containsKey("completionCause"));
        assertTrue(metrics.containsKey("status"));
        assertTrue(metrics.containsKey("tokenizer"));
    }


    @Test
    public void buildInterpretationMetricsNoMatch() throws JsonProcessingException {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);
        kryptonWsRecognizeMethod.interpretationStartTime = KryptonWsMethod.getCurrentUTCTime();
        WebApiResponse webApiResponse = WebApiResponse.fromJson("{\"type\": \"result\",\"sessionId\": \"b561927c-7936-4743-a4b1-217a13d96730\",\"command\": {\"name\": \"interpretation\",\"id\": \"7\"},\"result\": {\"final\": true,\"status\": \"success\",\"interpretations\": [{\"confidence\": 1,\"literal\": \"9u3p2he\",\"intent\": \"NO_MATCH\",\"origin\": \"STATISTICAL\"}],\"bestInterpretation\": {\"confidence\": 1,\"literal\": \"9u3p2he\",\"intent\": \"NO_MATCH\",\"origin\": \"STATISTICAL\"}}}");
        Map<String, Object> metrics = KryptonWsInterpretMethod.buildInterpretationMetrics(webApiResponse, kryptonWsRecognizeMethod.interpretationStartTime);
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("completionCause"));
        assertEquals("NO-MATCH", metrics.get("completionCause"));
    }

    @Test
    public void buildInterpretationMetricsFailure() throws JsonProcessingException {
        KryptonWsRecognizeMethod kryptonWsRecognizeMethod = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);
        kryptonWsRecognizeMethod.interpretationStartTime = KryptonWsMethod.getCurrentUTCTime();
        WebApiResponse webApiResponse = WebApiResponse.fromJson("{\"type\": \"result\",\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"success\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}");
        Map<String, Object> metrics = KryptonWsInterpretMethod.buildInterpretationMetrics(webApiResponse, kryptonWsRecognizeMethod.interpretationStartTime);
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("elapsed"));
        assertTrue(metrics.containsKey("pipeline"));
        assertTrue(metrics.containsKey("timestamps"));
        assertTrue(metrics.containsKey("completionCause"));
        assertTrue(metrics.containsKey("status"));
        assertTrue(metrics.containsKey("tokenizer"));
    }

    @Test(expected = JSONException.class)
    public void buildInvalidSpeechDetectionSensitivityRecognitionParameter() {
        JSONObject recognitionParameters = new JSONObject();
        recognitionParameters.put(KRYPTON_WS_PARAM_SENSITIVTY, 2000);
        kryptonWsSession.setKryptonWsParameters(new KryptonWsParameters(null, recognitionParameters));
    }

    @Test
    public void buildValidSpeechDetectionSensitivityRecognitionParameter() {
        JSONObject recognitionParameters = new JSONObject();
        recognitionParameters.put(KRYPTON_WS_PARAM_SENSITIVTY, 300);
        kryptonWsSession.setKryptonWsParameters(new KryptonWsParameters(null, recognitionParameters));
        assertEquals(recognitionParameters.get(KRYPTON_WS_PARAM_SENSITIVTY), kryptonWsSession.getKryptonWsParameters().getConfigurations().getInt(KRYPTON_WS_PARAM_SENSITIVTY));
    }
}
