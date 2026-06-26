package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.Session;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.params.KryptonWsParameters.KRYPTON_WS_PARAM_NBEST_LIST_LENGTH;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsCreateSessionMethodTest
{
    Session session = new WebSocketHttpSession(new FakeHttpSession());
    public static String createSessionJsonString = "{\n" +
                                                   "   \"attach\" : true,\n" +
                                                   "   \"clientData\" : {\n" +
                                                   "      \"applicationName\" : \"MyApp\",\n" +
                                                   "      \"applicationVersion\" : \"1.0\",\n" +
                                                   "      \"companyName\" : \"Nuance\"\n" +
                                                   "   },\n" +
                                                   "   \"method\" : \"CreateSession\",\n" +
                                                   "   \"recognitionParameters\" : {\n" +
                                                   "      \"audioFormat\" : \"audio/basic;rate=8000\",\n" +
                                                   "      \"noInputTimeout\" : \"19000ms\",\n" +
                                                   "      \"recognitionTimeout\" : \"19000ms\",\n" +
                                                   "      \"startRecognitionTimers\" : false\n" +
                                                   "   },\n" +
                                                   "   \"requestId\" : \"b84051e3a33f2094-00001\",\n" +
                                                   "   \"sessionId\" : \"b84051e3a33f2094\",\n" +
                                                   "   \"sessionParameters\" : {\n" +
                                                   "      \"audioFormat\" : \"audio/basic;rate=8000\"\n" +
                                                   "   },\n" +
                                                   "   \"version\" : \"1.0\",\n" +
                                                   "   \"languagePack\": {" +
                                                   "     \"language\":\"eng-USA\"," +
                                                   "     \"topic\":\"GEN\"" +
                                                   "   }," +
                                                   "   \"sessionObjects\": [{\n" +
                                                   "     \"id\": \"1\",\n" +
                                                   "     \"type\": \"application/x-nuance-wordset\",\n" +
                                                   "     \"body\": \"{\\\"PLACES\\\":[{\\\"literal\\\":\\\"Fordoun\\\",\\\"spoken\\\":[\\\"forden\\\"]},{\\\"literal\\\":\\\"Auchenblae\\\"}]}\"\n" +
                                                   "     }" +
                                                   "    ]," +
                                                   "   \"trust\": {" +
                                                   "     \"uuid\":\"9b871c68-5f44-11eb-bfef-5f8cac72e978\"," +
                                                   "     \"signature\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"" +
                                                   "   }" +
                                                   "}";

    KryptonWsSession kryptonWsSession;
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener;
    KryptonWsResponseSender kryptonWsResponseSender;
    KryptonWsCreateSessionMethod kryptonWsCreateSessionMethod;
    CombinedConfiguration configs = KryptonWsConfig.getKryptonWsDefaultCombinedConfig();

    @Before
    public void setUp()
            throws IOException
    {
        IWebApiFeatures iWebApiFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        when(iWebApiFeatures.getWebApiEndpoint(any())).thenReturn(webApiEndpoint);

        ISession isession = mock(ISession.class);
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(isession);
        CombinedConfiguration cc = mock(CombinedConfiguration.class);
        when(isession.getSessionConfig()).thenReturn(cc);
        Configuration config = mock(Configuration.class);
        when(cc.getConfiguration(anyString())).thenReturn(config);
        when(iWebApiFeatures.getConfigs()).thenReturn(configs);

        KryptonWsParameters kryptonWsParameters = mock(KryptonWsParameters.class);

        kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        kryptonWsSession.setWebapiSessionId("test");
        kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
        kryptonWsResponseSender = new KryptonWsResponseSender(session);
        kryptonWsCreateSessionMethod = new KryptonWsCreateSessionMethod(new JSONObject(createSessionJsonString),
                kryptonWsSession);
        kryptonWsSession.setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setKryptonWsWsSession(session)
                        .setKryptonWsResponseSender(kryptonWsResponseSender)
                        .setWebApiEndpoint(webApiEndpoint)
                        .setWebapiFeatures(iWebApiFeatures)
                        .setKryptonWsParameters(kryptonWsParameters);

    }

    @Test
    public void process()
    {
        //kryptonWsSession.setWebapiFeatures(mock(IWebApiFeatures.class));
        kryptonWsCreateSessionMethod.process();
        assertEquals("b84051e3a33f2094-00001", kryptonWsCreateSessionMethod.getKryptonWsRequestId());
        assertEquals(KryptonWsCreateSessionMethod.NAME, kryptonWsCreateSessionMethod.getMethodName());

        assertEquals("b84051e3a33f2094", kryptonWsCreateSessionMethod.getKryptonWsSessionId());

        assertEquals("error1", KryptonWsResponseBuilder.buildError("error1", null).get("message"));
        assertEquals("just kidding", KryptonWsResponseBuilder.buildError("error2", "just kidding").get("extra"));
    }

    @Test
    public void processWebapiResponse()
    {
        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(kryptonWsResponseSender);

        String webapiResponseSuccess = "{\"type\":\"result\",\"sessionId\":\"e63bb5f0-e842-4ca9-b178-42650e2bf1ff\",\"command\":{\"name\":\"startSession\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"success\"}}";
        kryptonWsCreateSessionMethod.processWebapiResponse(webapiResponseSuccess);
        ArgumentCaptor<String> argumentCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender).sendResponse(argumentCaptor2.capture());
        assertTrue(argumentCaptor2.getValue().contains("\"status\":\"complete\""));

        String webapiResponseError = "{\"type\":\"result\",\"sessionId\":\"12fb2d0f-241e-4afe-88bf-f66f7e22cc12\",\"command\":{\"name\":\"startSession\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[{\"message\":\"Invalid verification code.\",\"code\":1205}]}}";
        kryptonWsCreateSessionMethod.processWebapiResponse(webapiResponseError);
        ArgumentCaptor<String> argumentCaptor1 = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender, times(2)).sendResponse(argumentCaptor1.capture());
        assertTrue(argumentCaptor1.getValue().contains("\"status\":\"failed\""));
    }

    @Test
    public void processWebapiResponseWithLoad()
    {
        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(kryptonWsResponseSender);

        String webapiResponseSuccess = "{\"type\":\"result\",\"sessionId\":\"e63bb5f0-e842-4ca9-b178-42650e2bf1ff\",\"command\":{\"name\":\"startSession\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"success\"}}";
        kryptonWsCreateSessionMethod.processWebapiResponse(webapiResponseSuccess);
        ArgumentCaptor<String> argumentCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender).sendResponse(argumentCaptor2.capture());
        assertTrue(argumentCaptor2.getValue().contains("\"status\":\"complete\""));

        KryptonWsLoadMethod mockKryptonWsLoadMethod = mock(KryptonWsLoadMethod.class);
        when(mockKryptonWsLoadMethod.getLoadedObjects()).thenReturn(new JSONArray("[{\"id\":\"someId\"}]"));
        kryptonWsCreateSessionMethod.setKryptonWsLoadMethod(mockKryptonWsLoadMethod);
        String webapiResponseError = "{\"type\":\"result\",\"sessionId\":\"12fb2d0f-241e-4afe-88bf-f66f7e22cc12\",\"command\":{\"name\":\"startSession\",\"id\":\"1\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[{\"message\":\"Invalid verification code.\",\"code\":1205}]}}";
        kryptonWsCreateSessionMethod.processWebapiResponse(webapiResponseError);
        ArgumentCaptor<String> argumentCaptor1 = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender, times(2)).sendResponse(argumentCaptor1.capture());
        assertTrue(argumentCaptor1.getValue().contains("\"status\":\"failed\""));
    }

    @Test
    public void processWebapiResponse2()
    {
        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(kryptonWsResponseSender);

        String webapiInvalidJsonResponse = "hello";
        kryptonWsCreateSessionMethod.processWebapiResponse(webapiInvalidJsonResponse);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender).sendResponse(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().contains("\"status\":\"failed\""));
    }

    @Test
    public void loadSessionObjects()
    {
        kryptonWsCreateSessionMethod.loadSessionObjects();
        assertNotNull(kryptonWsCreateSessionMethod.kryptonWsLoadMethod);
    }

    @Test
    public void noClientData()
    {
        missingDataTest("clientData", "Couldn't authenticate request");
    }

    @Test
    public void noClientDataApplicationVersion()
    {
        missingDataTest("clientData", "applicationVersion", "Application version is missing.");
    }

    @Test
    public void noTrust()
    {
        missingDataTest("trust", "Couldn't authenticate request");
    }

    public void missingDataTest(String toRemove, String expectedMessage)
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        createSessionJson.remove(toRemove);
        testValidation(createSessionJson, expectedMessage);
    }

    public void missingDataTest(String parentKey, String childKey, String expectedMessage) {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        JSONObject parentJson = createSessionJson.getJSONObject(parentKey);
        parentJson.remove(childKey);
        testValidation(createSessionJson, expectedMessage);
    }

    @Test
    public void noTrustSignature()
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        JSONObject trust = createSessionJson.getJSONObject("trust");
        trust.remove("signature");
        testValidation(createSessionJson, "Couldn't authenticate request");
    }

    @Test
    public void hybridNoClientIdNoSecret()
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        createSessionJson.put("hybrid", new JSONObject());
        testValidation(createSessionJson, "", 0);
    }

    @Test
    public void hybridWithClientIdAndSecret()
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        JSONObject hybrid = new JSONObject("{\"clientId\": \"1234\", \"secret\": \"5678\"}");
        createSessionJson.put("hybrid", hybrid);
        testValidation(createSessionJson, "", 0);
    }

    @Test
    public void noLanguagePack()
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        createSessionJson.remove("languagePack");
        testValidation(createSessionJson, "languagePack is required");
    }

    @Test
    public void noLanguagePackLanguage()
    {
        JSONObject createSessionJson = new JSONObject(createSessionJsonString);
        JSONObject trust = createSessionJson.getJSONObject("languagePack");
        trust.remove("language");
        testValidation(createSessionJson, "languagePack.language is required");
    }

    @Test
    public void invalidObjectTypeTest()
    {
        JSONObject createSessionJsonObject = new JSONObject(createSessionJsonString);
        createSessionJsonObject.remove("recognitionParameters");
        JSONObject recognitionParametersMap = new JSONObject();

        recognitionParametersMap.put(KRYPTON_WS_PARAM_NBEST_LIST_LENGTH, "abc");
        createSessionJsonObject.put("recognitionParameters", recognitionParametersMap);
        testValidation(createSessionJsonObject, "cannot be converted to class java.lang.Integer. Value is: 'abc'.");

        recognitionParametersMap.remove(KRYPTON_WS_PARAM_NBEST_LIST_LENGTH);
        createSessionJsonObject.remove("recognitionParameters");

        recognitionParametersMap.put("enableProfanityFilter", "123");
        createSessionJsonObject.put("recognitionParameters", recognitionParametersMap);
        testValidation(createSessionJsonObject, "cannot be converted to class java.lang.Boolean. Value is: '123'.");
    }

    public void testValidation(JSONObject createSessionJson, String toCheck)
    {
        testValidation(createSessionJson, toCheck, 1);
    }

    public void testValidation(JSONObject createSessionJson, String toCheck, int times)
    {
        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        kryptonWsSession.setKryptonWsResponseSender(kryptonWsResponseSender);

        kryptonWsCreateSessionMethod = new KryptonWsCreateSessionMethod(createSessionJson, kryptonWsSession);
        kryptonWsCreateSessionMethod.process();

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(kryptonWsResponseSender, times(times)).sendResponse(argumentCaptor.capture());
        if(times > 0)
        {
            assertTrue(argumentCaptor.getValue().contains(toCheck));
        }
    }
}