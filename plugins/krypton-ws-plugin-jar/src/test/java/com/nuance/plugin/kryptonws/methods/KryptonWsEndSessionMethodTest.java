package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
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
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KryptonWsEndSessionMethodTest
{

    Session session = new WebSocketHttpSession(new FakeHttpSession());
    public static final String endSessionString = "  {\n" +
            "    \"method\": \"EndSession\",\n" +
            "    \"requestId\": \"b84051e3a33f2094-00002\",\n" +
            "    \"sessionId\": \"b84051e3a33f2094\",\n" +
            "    \"version\": \"1.0\"\n" +
            "  }";

    public String endSessionWebapiError = "\n" +
            "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
            "    \"command\": {\n" +
            "        \"id\": \"2\",\n" +
            "        \"name\": \"speechRecognition\"\n" +
            "    },\n" +
            "    \"result\": {\n" +
            "         \"final\": true,\n" +
            "         \"status\": \"error\",\n" +
            "         \"errors\": [\n" +
            "             {\n" +
            "                 \"code\": 1600,\n" +
            "                 \"message\": \"speechRecognition failed.\"\n" +
            "             },\n" +
            "             {\n" +
            "                 \"code\": 1601,\n" +
            "                 \"message\": \"speechRecognition command couldn't be executed.\"\n" +
            "             }\n" +
            "         ]\n" +
            "    }\n" +
            "}";

    private String recognizeCancelled = "{\n" +
            "  \"type\": \"result\",\n" +
            "  \"sessionId\": \"123\",\n" +
            "  \"command\": {\n" +
            "    \"name\": \"speechRecognition\",\n" +
            "    \"id\": \"2\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"final\": true,\n" +
            "    \"status\": \"cancelled\"\n" +
            "  }\n" +
            "}";

    private String endSessionResponseAfterActiveCommandWasCancelled = "{\n" +
            "  \"type\": \"result\",\n" +
            "  \"sessionId\": \"123\",\n" +
            "  \"command\": {\n" +
            "    \"name\": \"endSession\",\n" +
            "    \"id\": \"3\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"final\": true,\n" +
            "    \"status\": \"cancelled\"\n" +
            "  }\n" +
            "}";

    KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
    KryptonWsResponseSender kryptonWsResponseSender = new KryptonWsResponseSender(session);
    KryptonWsEndSessionMethod kryptonWsEndSessionMethod = new KryptonWsEndSessionMethod(new JSONObject(endSessionString),
            kryptonWsSession);

    @Before
    public void setUp() {

        kryptonWsSession.setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setKryptonWsWsSession(session)
                        .setKryptonWsResponseSender(kryptonWsResponseSender);

    }

    @Test
    public void processTest() {


        IWebApiEndpoint mockEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        KryptonWsSession mockKryptonWsSession = mock(KryptonWsSession.class);

        kryptonWsEndSessionMethod = new KryptonWsEndSessionMethod(new JSONObject(endSessionString), mockKryptonWsSession);

        when(mockKryptonWsSession.getWebApiEndpoint()).thenReturn(mockEndpoint);
        when(mockEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockSession.getSessionId()).thenReturn("b84051e3a33f2094");
        when(mockKryptonWsSession.getWebapiResponseListener()).thenReturn(kryptonWsWebapiResponseListener);
        when(mockKryptonWsSession.getKryptonWsSessionId()).thenReturn("b84051e3a33f2094");
        when(mockKryptonWsSession.isKryptonSessionEstablished()).thenReturn(true);
        when(mockKryptonWsSession.getExecutorService()).thenReturn(mock(ExecutorService.class));
        kryptonWsEndSessionMethod.process();

        assertEquals("b84051e3a33f2094-00002", kryptonWsEndSessionMethod.getKryptonWsRequestId());
        assertEquals("b84051e3a33f2094", kryptonWsEndSessionMethod.getKryptonWsSessionId());
        assertEquals(KryptonWsEndSessionMethod.NAME, kryptonWsEndSessionMethod.getMethodName());

    }

    @Test
    public void processWebapiResponseTest() throws IOException {
        String successResponse = "{\"type\":\"result\",\"sessionId\":\"ecd23248-35af-4d9e-98c0-e2897906f3c4\",\"command\":{\"name\":\"endSession\",\"id\":\"b84051e3a33f2094-00002\"},\"result\":{\"final\":true,\"status\":\"success\"}}";


        IWebApiFeatures mockWebapiFeatures = mock(IWebApiFeatures.class);
        KryptonWsResponseSender responseSender = mock(KryptonWsResponseSender.class);

        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);

        Whitebox.setInternalState(kryptonWsResponseSender, "webSocketSession", session );
        Whitebox.setInternalState(kryptonWsSession, "webapiFeatures", mockWebapiFeatures);
        when(mockWebapiFeatures.getConfigs()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(anyString())).thenReturn(false);
        kryptonWsEndSessionMethod.kryptonWsSession = kryptonWsSession;
        kryptonWsSession.setKryptonWsResponseSender(responseSender);

        IWebApiEndpoint iWebApiEndpoint = mock(IWebApiEndpoint.class);
        ISession session = mock(ISession.class);
        CombinedConfiguration cc = mock(CombinedConfiguration.class);
        when(iWebApiEndpoint.getActiveSession()).thenReturn(session);
        when(session.getSessionConfig()).thenReturn(cc);
        KryptonWsParameters kryptonWsParameters = mock(KryptonWsParameters.class);
        kryptonWsSession.setKryptonWsParameters(kryptonWsParameters);
        when(kryptonWsParameters.getConfigurations()).thenReturn(cc);
        kryptonWsSession.setWebApiEndpoint(iWebApiEndpoint);
        kryptonWsEndSessionMethod.processWebapiResponse(successResponse);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responseSender).sendResponse(captor.capture());
        JSONObject responseObj = new JSONObject(captor.getValue());
        assertEquals("Response", responseObj.get("method"));
        assertEquals("1.0", responseObj.get("version"));
        assertEquals(KryptonWsEndSessionMethod.NAME, responseObj.get("responseTo"));
        assertEquals("complete", responseObj.get("status"));

        kryptonWsEndSessionMethod.kryptonWsSession = kryptonWsSession;
        kryptonWsEndSessionMethod.processWebapiResponse(endSessionWebapiError);
        captor = ArgumentCaptor.forClass(String.class);
        verify(responseSender, times(2)).sendResponse(captor.capture());

        responseObj = new JSONObject(captor.getValue());
        assertEquals("Response", responseObj.get("method"));
        assertEquals("failed", responseObj.get("status"));

        JSONArray errorArray = responseObj.getJSONArray("errors");
        JSONObject error1 = errorArray.getJSONObject(0);
        JSONObject error2 = errorArray.getJSONObject(1);

        assertEquals("speechRecognition failed.", error1.get("message"));
        assertEquals("speechRecognition command couldn't be executed.", error2.get("message"));

        String invalidJson = "some invalid json string.";
        kryptonWsEndSessionMethod.processWebapiResponse(invalidJson);
        captor = ArgumentCaptor.forClass(String.class);
        verify(responseSender, times(3)).sendResponse(captor.capture());

        responseObj = new JSONObject(captor.getValue());

        assertEquals("failed", responseObj.get("status"));
        assertEquals(KryptonWsEndSessionMethod.NAME, responseObj.get("responseTo"));
    }

    @Test
    public void processWebapiResponseTest_anActiveCommandWasCancelledByEndSession() throws IOException
    {
        IWebApiFeatures mockWebapiFeatures = mock(IWebApiFeatures.class);
        KryptonWsResponseSender responseSender = mock(KryptonWsResponseSender.class);

        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);

        Whitebox.setInternalState(kryptonWsResponseSender, "webSocketSession", session);
        Whitebox.setInternalState(kryptonWsSession, "webapiFeatures", mockWebapiFeatures);
        when(mockWebapiFeatures.getConfigs()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(anyString())).thenReturn(false);
        kryptonWsEndSessionMethod.kryptonWsSession = kryptonWsSession;
        kryptonWsSession.setKryptonWsResponseSender(responseSender);

        IWebApiEndpoint iWebApiEndpoint = mock(IWebApiEndpoint.class);
        ISession session = mock(ISession.class);
        CombinedConfiguration cc = mock(CombinedConfiguration.class);
        when(iWebApiEndpoint.getActiveSession()).thenReturn(session);
        when(session.getSessionConfig()).thenReturn(cc);
        KryptonWsParameters kryptonWsParameters = mock(KryptonWsParameters.class);
        kryptonWsSession.setKryptonWsParameters(kryptonWsParameters);
        when(kryptonWsParameters.getConfigurations()).thenReturn(cc);
        kryptonWsSession.setWebApiEndpoint(iWebApiEndpoint);

        kryptonWsEndSessionMethod.processWebapiResponse(recognizeCancelled);
        verify(responseSender, times(0)).sendResponse(any());

        kryptonWsEndSessionMethod.processWebapiResponse(endSessionResponseAfterActiveCommandWasCancelled);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responseSender, times(1)).sendResponse(captor.capture());

        assertEquals("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"b84051e3a33f2094\",\"requestId\":\"b84051e3a33f2094-00002\",\"responseTo\":\"EndSession\",\"status\":\"complete\"}", captor.getValue());
    }

    @Test
    public void canRunWhileTest()
    {
        KryptonWsMethod kryptonWsMethod = null;

        assertTrue(kryptonWsEndSessionMethod.canRunWhile(kryptonWsMethod));

        kryptonWsMethod = mock(KryptonWsMethod.class);
        when(kryptonWsMethod.getMethodName()).thenReturn(KryptonWsRecognizeMethod.NAME);

        assertTrue(kryptonWsEndSessionMethod.canRunWhile(kryptonWsMethod));

        when(kryptonWsMethod.getMethodName()).thenReturn("someCommand");

        assertFalse(kryptonWsEndSessionMethod.canRunWhile(kryptonWsMethod));
    }
}