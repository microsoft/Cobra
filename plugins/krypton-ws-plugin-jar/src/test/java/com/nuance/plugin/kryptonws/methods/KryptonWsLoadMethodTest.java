package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KryptonWsLoadMethodTest
{

    KryptonWsResponseSender mockKryptonWsResponseSender = mock(KryptonWsResponseSender.class);
    KryptonWsWebapiResponseListener mockKryptonWsWebapiResponseListener = mock(KryptonWsWebapiResponseListener.class);
    KryptonWsSession kryptonWsSession = mock(KryptonWsSession.class);
    protected KryptonWsLoadMethod kryptonWsLoadMethod;

    @Before
    public void prepare()
    {
        when(kryptonWsSession.getKryptonWsSessionId()).thenReturn("123");
        when(kryptonWsSession.getKryptonWsResponseSender()).thenReturn(mockKryptonWsResponseSender);
        when(kryptonWsSession.getWebapiResponseListener()).thenReturn(mockKryptonWsWebapiResponseListener);
        when(kryptonWsSession.getCreateSessionObjects()).thenReturn(new JSONArray());

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(mockConfig);
    }

    @Test
    public void testGetLogger() {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        assertNotNull(kryptonWsLoadMethod.getLogger());
    }

    @Test(expected = Test.None.class)
    public void testProcessAudio() {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        byte[] bytes = new byte[0];
        kryptonWsLoadMethod.onAudioImplementation(bytes);
    }

    @Test
    public void testProcessWordSet() throws Exception {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"urn://nuance-mix:tag:wordset:lang/A100_C1/places-compiled-ws/eng-USA/mix.asr\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        String command = "{\"type\":\"command\",\"command\":{\"name\":\"uploadDynamicVocabulary\",\"id\":\"0\",\"parameters\":{\"dynamicVocabularies\":[{\"id\":\"some_id1\",\"type\":\"application/x-nuance-wordset-pkg\",\"uri\":\"urn:nuance-mix:tag:wordset:lang/A100_C1/places-compiled-ws/eng-USA/mix.asr\"},{\"id\":\"some_id2\",\"type\":\"application/x-nuance-wordset\",\"body\":{\"<category-name_M>\":[{\"spoken\":[\"<spoken form 1>\",\"<spoken form n>\"],\"literal\":\"<written form>\"}],\"<category-name_1>\":[{\"spoken\":[\"<spoken form 1>\",\"<spoken form n>\"],\"literal\":\"<written form>\"}]}}]}}}";
        KryptonWsLoadMethod kryptonWsLoadMethodToTest = spy(kryptonWsLoadMethod);
        kryptonWsLoadMethodToTest.process();
        verify(kryptonWsLoadMethodToTest).sendToWebapi(command, kryptonWsLoadMethodToTest);
    }

    @Test
    public void testProcessWordSetNoWordSets() throws Exception {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        KryptonWsLoadMethod kryptonWsLoadMethodToTest = spy(kryptonWsLoadMethod);
        kryptonWsLoadMethodToTest.process();
        verify(kryptonWsLoadMethodToTest).sendCompleteResponse();
        verify(kryptonWsLoadMethodToTest).cleanup();
    }

    @Test
    public void testSendError()
    {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        String errorMessage = "Object id already in use";
        kryptonWsLoadMethod.sendError(errorMessage);
        verify(mockKryptonWsResponseSender).sendResponse(String.format("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"123\",\"requestId\":\"request1\",\"responseTo\":\"Load\",\"status\":\"failed\",\"errors\":[{\"message\":\"%s\"}]}",
                errorMessage));
    }

    @Test
    public void testProcessWebapiResponseSuccess() {
        String requestId = "request1";
        String sessionId = "123";
        String request = String.format("{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"%s\",\"requestId\": \"%s\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n",
                sessionId,
                requestId);
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        kryptonWsLoadMethod.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"whatever\",\"command\":{\"id\":\"whatever\",\"name\":\"uploadDynamicVocabulary\"},\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockKryptonWsResponseSender).sendResponse(String.format("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"%s\",\"requestId\":\"%s\",\"responseTo\":\"Load\",\"status\":\"complete\"}",
                sessionId,
                requestId));
    }

    @Test (expected = Test.None.class)
    public void testProcessWebapiResponseFailure() throws JsonProcessingException {
        String requestId = "request1";
        String sessionId = "123";
        String request = String.format("{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"%s\",\"requestId\": \"%s\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n",
                sessionId,
                requestId);
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        KryptonWsLoadMethod kryptonWsLoadMethodToTest = spy(kryptonWsLoadMethod);
        String webapiResponseString = "{\"type\":\"result\",\"sessionId\":\"whatever\",\"command\":{\"id\":\"whatever\",\"name\":\"uploadDynamicVocabulary\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[{\"message\":\"Invalid verification code.\",\"code\":1205}]}}";
        WebApiResponse webApiResponse = WebApiResponse.fromJson(webapiResponseString);
        kryptonWsLoadMethodToTest.processWebapiResponse(webapiResponseString);
        verify(kryptonWsLoadMethodToTest).sendErrorResponse(eq(webApiResponse.getErrorsAsString()), any());
        verify(kryptonWsLoadMethodToTest).cleanup();
    }

    @Test
    public void testGetLoadedObjects() {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        JSONArray loadedObjects = new JSONArray("[{\"id\": \"some_id1\"},{\"id\": \"some_id2\"},{\"id\": \"some_id3\"}]");
        kryptonWsLoadMethod.setLoadedObjects(loadedObjects);
        assertEquals(loadedObjects, kryptonWsLoadMethod.getLoadedObjects());
    }

    @Test
    public void testProcessWebapiResponseError() {
        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        KryptonWsLoadMethod kryptonWsLoadMethodToTest = spy(kryptonWsLoadMethod);
        kryptonWsLoadMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"id\":\"whatever\",\"name\":\"uploadDynamicVocabulary\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[\"an error\":\"is an error\"]}}");
        verify(kryptonWsLoadMethodToTest).sendError("Unable to parse webapi response json");
    }

    @Test
    public void testDuplicateObjectId(){
        String requestJson = "\n{" +
                "  \"method\" : \"Load\",\n" +
                "  \"requestId\" : \"d2679e94-3167-4fd5-a6c8-015851dd82d2\",\n" +
                "  \"objects\" : [ {\n" +
                "    \"weight\" : 100,\n" +
                "    \"id\" : \"Pharmacy_20180626\",\n" +
                "    \"type\" : \"application/x-nuance-domainlm\",\n" +
                "    \"url\" : \"https://nes-db.nuance.mobi/var/NinaDemo-Abhinav-en-US/Pharmacy_20180626/1.0/1.0.0/krypton/dlm.zip\"\n" +
                "  }, {\n" +
                "    \"id\" : \"Pharmacy_20180626\",\n" +
                "    \"type\" : \"application/x-nuance-wordset\",\n" +
                "    \"body\" : \"{\\\"contacts\\\": [ {\\\"literal\\\": \\\"Alphonse Gregorio\\\",\\\"spoken\\\": [\\\"Alphonse Gregorio\\\"]}, {\\\"literal\\\":\\\"Slipgator\\\",\\\"spoken\\\": [\\\"Slipgator\\\"]}, {\\\"literal\\\":\\\"Namco\\\",\\\"spoken\\\": [\\\"Namco\\\"]}, {\\\"literal\\\":\\\"Superintoxicator\\\",\\\"spoken\\\": [\\\"Superintoxicator\\\"]}]}\"\n" +
                "  } ],\n" +
                "  \"sessionId\" : \"d2679e94-3167-4fd5-a6c8-015851dd82d2\",\n" +
                "  \"version\" : \"1.0\"\n" +
                "}";

        kryptonWsLoadMethod = new KryptonWsLoadMethod(new JSONObject(requestJson), kryptonWsSession);
        kryptonWsLoadMethod.process();
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        // expecting this 3 times, since we send in-progress, fail, and complete
        verify(mockKryptonWsResponseSender, times(3)).sendResponse(argumentCaptor.capture());
        JSONObject failedObj = new JSONObject(argumentCaptor.getAllValues().get(1));
        assertEquals("failed", failedObj.get("status"));
    }
}
