package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsInterpretMethodTest
{
    KryptonWsSession kryptonWsSession;

    @Before
    public void setup() throws IOException {
        // arrange
        kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        ISession mockSession = mock(ISession.class);
        IWebApiEndpoint mockEndpoint = mock(IWebApiEndpoint.class);

        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        kryptonWsSession.setWebapiSessionId("test");
        when(mockEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        kryptonWsSession.setWebApiEndpoint(mockEndpoint);

        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        kryptonWsSession.setWebapiFeatures(mockFeatures);

        CombinedConfiguration defaultConfig = KryptonWsConfig.getKryptonWsDefaultCombinedConfig();
        when(mockFeatures.getConfigs()).thenReturn(defaultConfig);

        KryptonWsWebapiResponseListener mockListener = mock(KryptonWsWebapiResponseListener.class);
        kryptonWsSession.setWebapiResponseListener(mockListener);

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(mockSender);

        kryptonWsSession.setKryptonWsParameters(new KryptonWsParameters(null, null));
        kryptonWsSession.setSemanticModel(new JSONObject("{\"id\":\"id\",\"url\":\"myUrl\"}"));
    }

    @Test
    public void testProcess()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }

    @Test
    public void testProcess_noSemanticModel()
    {
        kryptonWsSession.setSemanticModel(null);
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendResponse(KryptonWsMessage.STATUS.FAILED);
    }

    @Test
    public void testProcess_sensitive()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setSensitive("suppress")
                .setResponseType("application/plain")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"sensitivity\":\"masked\",\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }

    @Test
    public void testProcess_useWordSet()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setUseWordSets(new JSONArray("[\"wordset1\", \"wordest1\"]"))
                .build();

        kryptonWsSession.addWordSet("wordset1");
        kryptonWsSession.addWordSet("wordest1");
        kryptonWsSession.setSessionObjects(Map.of
                ("wordset1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-nle-pkg\"}"),
                "wordest1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-nle-pkg\"}")));
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"activeDynamicVocabularySets\":[\"wordset1\",\"wordest1\"],\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }

    @Test
    public void testProcess_useWordSet_notLoaded()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setUseWordSets(new JSONArray("[\"wordset1\", \"wordest1\"]"))
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendResponse(KryptonWsMessage.STATUS.FAILED);
    }

    @Test
    public void testProcess_useWordSet_WithInvalidSessionObject()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setUseWordSets(new JSONArray("[\"wordset1\", \"wordest1\"]"))
                .build();
        kryptonWsSession.addWordSet("wordset1");
        kryptonWsSession.addWordSet("wordest1");
        kryptonWsSession.setSessionObjects(Map.of
                ("randomId", new JSONObject("{}")));

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }
    @Test
    public void testProcess_useWordSet_WithValidWordsetObject()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setUseWordSets(new JSONArray("[\"wordset1\", \"wordest1\"]"))
                .build();

        kryptonWsSession.addWordSet("wordset1");
        kryptonWsSession.addWordSet("wordest1");
        kryptonWsSession.setWordsetObjects(Map.of
                ("wordset1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-nle-pkg\"}"),
                        "wordest1", new JSONObject("{\"url\":\"someUrl\", \"weight\":\"heavy\", \"type\":\"application/x-nuance-wordset-nle-pkg\"}")));
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"activeDynamicVocabularySets\":[\"wordset1\",\"wordest1\"],\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }
    @Test
    public void testProcess_useWordSet_WithInvalidWordsetObject()
    {
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setUseWordSets(new JSONArray("[\"wordset1\", \"wordest1\"]"))
                .build();
        kryptonWsSession.addWordSet("wordset1");
        kryptonWsSession.addWordSet("wordest1");
        kryptonWsSession.setWordsetObjects(Map.of
                ("randomId", new JSONObject("{}")));
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);

        // act
        methodToTest.process();

        // assert
        verify(methodToTest).sendToWebapi("{\"sessionId\":\"test\",\"type\":\"command\",\"command\":{\"name\":\"interpretation\",\"id\":\"1\",\"parameters\":{\"data\":\"bestResult\",\"interpretationVerboseResult\":true,\"type\":\"text/plain\",\"statistics\":true}}}",
                methodToTest);
    }

    @Test
    public void processWebapiResults() {
        String webApiResponse = "{\"type\": \"result\",\"sessionId\": \"2c7c1b61-dc8e-48cf-95f4-573e27392721\",\"command\": {\"name\": \"interpretation\",\"id\": \"2\"},\"result\": {\"final\": true,\"status\": \"success\",\"interpretations\": [{\"concepts\": [{\"concept\": \"nuance_CALENDARX\",\"value\": {\"nuance_CALENDAR\": {\"nuance_DATE\": {\"nuance_DATE_ABS\": {\"nuance_DAY\": 1,\"nuance_MONTH\": 4}}}},\"literal\": \"april first\",\"begin\": 29,\"end\": 40},{\"concept\": \"FLOWER_TYPE\",\"value\": \"daisy\",\"literal\": \"daisies\",\"begin\": 15,\"end\": 22}],\"confidence\": 0,\"literal\": \"I want to keep daisies until april first\",\"intent\": \"BUY_FLOWERS\",\"origin\": \"STATISTICAL\"},{\"concepts\": [{\"concept\": \"nuance_CALENDARX\",\"value\": {\"nuance_CALENDAR\": {\"nuance_DATE\": {\"nuance_DATE_ABS\": {\"nuance_DAY\": 1,\"nuance_MONTH\": 4}}}},\"literal\": \"april first\",\"begin\": 29,\"end\": 40},{\"concept\": \"FLOWER_TYPE\",\"value\": \"daisy\",\"literal\": \"daisies\",\"begin\": 15,\"end\": 22}],\"confidence\": 1,\"literal\": \"I want to keep daisies until april first\",\"intent\": \"MAINTAIN_FLOWERS\",\"origin\": \"STATISTICAL\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"nuance_CALENDARX\",\"value\": {\"nuance_CALENDAR\": {\"nuance_DATE\": {\"nuance_DATE_ABS\": {\"nuance_DAY\": 1,\"nuance_MONTH\": 4}}}},\"literal\": \"april first\",\"begin\": 29,\"end\": 40},{\"concept\": \"FLOWER_TYPE\",\"value\": \"daisy\",\"literal\": \"daisies\",\"begin\": 15,\"end\": 22}],\"confidence\": 0,\"literal\": \"I want to keep daisies until april first\",\"intent\": \"BUY_FLOWERS\",\"origin\": \"STATISTICAL\"},\"verboseResult\": {\"metadata\": {\"x-nuance-supplemental-metrics-result\":{\"modelLanguage\":\"eng-USA\",\"tokens\":[\"good\",\"morning\",\"how\",\"are\",\"you\",\"doing\",\"today\"],\"versions\":{\"qnlu\":\"2.22.9\"}}},\"literal\": \"I want to keep daisies until april first\",\"interpretations\": [{\"singleIntentInterpretation\": {\"intent\": \"MAINTAIN_FLOWERS\",\"confidence\": 0.99909896,\"origin\": \"STATISTICAL\",\"entities\": {\"nuance_CALENDARX\": {\"entities\": [{\"textRange\": {\"startIndex\": 29,\"endIndex\": 40},\"confidence\": 0.40598857,\"origin\": \"STATISTICAL\",\"structValue\": {\"nuance_CALENDAR\": {\"nuance_DATE\": {\"nuance_DATE_ABS\": {\"nuance_DAY\": 1,\"nuance_MONTH\": 4}}}},\"literal\": \"april first\",\"sensitive\": false,\"formattedLiteral\": \"april first\",\"formattedTextRange\": {\"startIndex\": 29,\"endIndex\": 40}}]},\"FLOWER_TYPE\": {\"entities\": [{\"textRange\": {\"startIndex\": 15,\"endIndex\": 22},\"confidence\": 0.8039608,\"origin\": \"STATISTICAL\",\"stringValue\": \"daisy\",\"literal\": \"daisies\",\"sensitive\": false,\"formattedLiteral\": \"daisies\",\"formattedTextRange\": {\"startIndex\": 15,\"endIndex\": 22}}]}}}},{\"singleIntentInterpretation\": {\"intent\": \"BUY_FLOWERS\",\"confidence\": 0.0008219178,\"origin\": \"STATISTICAL\",\"entities\": {\"nuance_CALENDARX\": {\"entities\": [{\"textRange\": {\"startIndex\": 29,\"endIndex\": 40},\"confidence\": 0.40598857,\"origin\": \"STATISTICAL\",\"structValue\": {\"nuance_CALENDAR\": {\"nuance_DATE\": {\"nuance_DATE_ABS\": {\"nuance_DAY\": 1,\"nuance_MONTH\": 4}}}},\"literal\": \"april first\",\"sensitive\": false,\"formattedLiteral\": \"april first\",\"formattedTextRange\": {\"startIndex\": 29,\"endIndex\": 40}}]},\"FLOWER_TYPE\": {\"entities\": [{\"textRange\": {\"startIndex\": 15,\"endIndex\": 22},\"confidence\": 0.8039608,\"origin\": \"STATISTICAL\",\"stringValue\": \"daisy\",\"literal\": \"daisies\",\"sensitive\": false,\"formattedLiteral\": \"daisies\",\"formattedTextRange\": {\"startIndex\": 15,\"endIndex\": 22}}]}}}}],\"sensitive\": false,\"formattedLiteral\": \"I want to keep daisies until april first\"}}}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .build();

        interpretMethod.areMetricsEnabled = true;
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendCompleteResponse();
    }

    @Test
    public void processWebapiResults_metricsParsingError() {
        String webApiResponse = "{\"command\":{\"id\":\"2\",\"name\":\"interpretation\"},\"result\":{\"bestInterpretation\":{\"concepts\":[{\"begin\":29,\"concept\":\"nuance_CALENDARX\",\"end\":40,\"literal\":\"april first\",\"value\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_ABS\":{\"nuance_DAY\":1,\"nuance_MONTH\":4}}}}},{\"begin\":15,\"concept\":\"FLOWER_TYPE\",\"end\":22,\"literal\":\"daisies\",\"value\":\"daisy\"}],\"confidence\":0,\"intent\":\"BUY_FLOWERS\",\"literal\":{\"isthis\":2,\"what\":1},\"origin\":\"STATISTICAL\"},\"final\":true,\"interpretations\":[{\"concepts\":[{\"begin\":29,\"concept\":\"nuance_CALENDARX\",\"end\":40,\"literal\":\"april first\",\"value\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_ABS\":{\"nuance_DAY\":1,\"nuance_MONTH\":4}}}}},{\"begin\":15,\"concept\":\"FLOWER_TYPE\",\"end\":22,\"literal\":\"daisies\",\"value\":\"daisy\"}],\"confidence\":0,\"intent\":\"BUY_FLOWERS\",\"literal\":\"I want to keep daisies until april first\",\"origin\":\"STATISTICAL\"},{\"concepts\":[{\"begin\":29,\"concept\":\"nuance_CALENDARX\",\"end\":40,\"literal\":\"april first\",\"value\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_ABS\":{\"nuance_DAY\":1,\"nuance_MONTH\":4}}}}},{\"begin\":15,\"concept\":\"FLOWER_TYPE\",\"end\":22,\"literal\":\"daisies\",\"value\":\"daisy\"}],\"confidence\":1,\"intent\":\"MAINTAIN_FLOWERS\",\"literal\":\"I want to keep daisies until april first\",\"origin\":\"STATISTICAL\"}],\"status\":\"success\",\"verboseResult\":{\"formattedLiteral\":\"I want to keep daisies until april first\",\"interpretations\":[{\"singleIntentInterpretation\":{\"confidence\":0.99909896,\"entities\":{\"FLOWER_TYPE\":{\"entities\":[{\"confidence\":0.8039608,\"formattedLiteral\":\"daisies\",\"formattedTextRange\":{\"endIndex\":22,\"startIndex\":15},\"literal\":\"daisies\",\"origin\":\"STATISTICAL\",\"sensitive\":false,\"stringValue\":\"daisy\",\"textRange\":{\"endIndex\":22,\"startIndex\":15}}]},\"nuance_CALENDARX\":{\"entities\":[{\"confidence\":0.40598857,\"formattedLiteral\":\"april first\",\"formattedTextRange\":{\"endIndex\":40,\"startIndex\":29},\"literal\":\"april first\",\"origin\":\"STATISTICAL\",\"sensitive\":false,\"structValue\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_ABS\":{\"nuance_DAY\":1,\"nuance_MONTH\":4}}}},\"textRange\":{\"endIndex\":40,\"startIndex\":29}}]}},\"intent\":\"MAINTAIN_FLOWERS\",\"origin\":\"STATISTICAL\"}},{\"singleIntentInterpretation\":{\"confidence\":0.0008219178,\"entities\":{\"FLOWER_TYPE\":{\"entities\":[{\"confidence\":0.8039608,\"formattedLiteral\":\"daisies\",\"formattedTextRange\":{\"endIndex\":22,\"startIndex\":15},\"literal\":\"daisies\",\"origin\":\"STATISTICAL\",\"sensitive\":false,\"stringValue\":\"daisy\",\"textRange\":{\"endIndex\":22,\"startIndex\":15}}]},\"nuance_CALENDARX\":{\"entities\":[{\"confidence\":0.40598857,\"formattedLiteral\":\"april first\",\"formattedTextRange\":{\"endIndex\":40,\"startIndex\":29},\"literal\":\"april first\",\"origin\":\"STATISTICAL\",\"sensitive\":false,\"structValue\":{\"nuance_CALENDAR\":{\"nuance_DATE\":{\"nuance_DATE_ABS\":{\"nuance_DAY\":1,\"nuance_MONTH\":4}}}},\"textRange\":{\"endIndex\":40,\"startIndex\":29}}]}},\"intent\":\"BUY_FLOWERS\",\"origin\":\"STATISTICAL\"}}],\"literal\":\"I want to keep daisies until april first\",\"metadata\":{\"x-nuance-supplemental-metrics-result\":\"unexpected\"},\"sensitive\":false}},\"sessionId\":\"2c7c1b61-dc8e-48cf-95f4-573e27392721\",\"type\":\"result\"}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .build();

        interpretMethod.areMetricsEnabled = true;
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendCompleteResponse();
    }

    @Test
    public void processWebapiResults_failureToParse() {
        String webApiResponse = "{\"type\": \"result\"\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"success\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendErrorResponse("Unable to parse webapi response json", null);
    }

    @Test
    public void processWebapiResults_errorResponse() throws JsonProcessingException {
        String webApiResponse = "{\"type\": \"result\",\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"error\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        interpretMethod.areMetricsEnabled = true;
        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendErrorResponseFromWebapiResponseObject(any());
    }

    @Test
    public void processWebapiResults_noMatchResponse() throws JsonProcessingException {
        String webApiResponse = "{\"type\": \"result\",\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"noMatch\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendErrorResponse("Interpret resulted in a no match.", null);
    }

    @Test
    public void processWebapiResults_cancelledResponse() throws JsonProcessingException {
        String webApiResponse = "{\"type\": \"result\",\"sessionId\": \"3d09f61a-1ba8-4cbb-9763-3ce2f63a9695\",\"command\": {\"name\": \"interpretation\",\"id\": \"3\"},\"result\": {\"final\": true,\"status\": \"cancelled\",\"statistics\": {\"hostInfo\": {\"version\": \"2.22.0-202104071755\"},\"startTime\": \"2021/04/08 18:13:53.209\",\"endTime\": \"2021/04/08 18:13:53.391\",\"modelVersion\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\",\"enableSpeechRecognitionSpeakerProfile\": false,\"engineRequests\": {\"engine\": \"nle\",\"model\": \"/var/Nuance_PS/Dominos-MultiChannel_AU-en-US/1.0/1.0.6/nle_20181020-001702.zip\"}},\"interpretations\": [{\"concepts\": [{\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"ORDER_METHOD\",\"literal\": \"order\",\"begin\": 10,\"end\": 15},{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"},{\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"NO_INTENT\",\"origin\": \"Grammar\"}],\"bestInterpretation\": {\"concepts\": [{\"concept\": \"PRODUCT\",\"value\": \"generic,pizza\",\"literal\": \"pizza\",\"begin\": 16,\"end\": 21}],\"confidence\": 100,\"literal\": \"I want to order pizza\",\"intent\": \"ROUTING_LOGIC\",\"origin\": \"Grammar\"}}}";
        KryptonWsInterpretMethod interpretMethod = new KryptonWsInterpretMethod.Builder()
                .setKryptonWsSession(kryptonWsSession)
                .setMethod("Interpret")
                .setVersion("1.0")
                .setSessionId("123")
                .setRequestId("123")
                .setModel("id")
                .setInputType("text/plain")
                .setInput("bestResult")
                .setResponseType("application/plain")
                .setSensitive("off")
                .build();

        KryptonWsInterpretMethod methodToTest = spy(interpretMethod);
        methodToTest.processWebapiResponse(webApiResponse);

        verify(methodToTest).sendErrorResponse("Interpret was cancelled.", null);
    }
}
