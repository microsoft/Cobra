package com.nuance.plugin.kryptonws;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.nuance.cobra.test.TestUtils;
import com.nuance.plugin.kryptonws.methods.KryptonWsCreateSessionMethodTest;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ASYNC_WEBAPI_CALLS;
import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_IGNORE_STRAY_AUDIO;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KryptonWsPluginTest
{
    Session session = mock(Session.class);
    IWebApiFeatures iWebApiFeatures = mock(IWebApiFeatures.class);
    KryptonWsPlugin kryptonWsPlugin;

    CombinedConfiguration cc;
    Configuration config;

    @Before
    public void setUp()
            throws IOException
    {
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession session = mock(ISession.class);
        cc = new CombinedConfiguration();
        config = KryptonWsConfig.getKryptonWsDefaultConfig();
        cc.addConfiguration(config);

        when(iWebApiFeatures.getWebApiEndpoint(any())).thenReturn(webApiEndpoint);
        when(webApiEndpoint.getActiveSession()).thenReturn(session);
        when(session.getSessionConfig()).thenReturn(cc);
        when(iWebApiFeatures.getConfigs()).thenReturn(cc);

        kryptonWsPlugin = new KryptonWsPlugin();
        kryptonWsPlugin.onInitPlugin(iWebApiFeatures);

        kryptonWsPlugin.getKryptonWsSession().setWebApiEndpoint(webApiEndpoint);
    }

    @Test
    public void kryptonWsPluginTest()
            throws IOException, NoSuchFieldException, IllegalAccessException, NullPointerException
    {
        final String URL_PATTERN = KryptonWsPlugin.PLUGIN_URL_FILTER;

        assertEquals(URL_PATTERN, kryptonWsPlugin.getUrlPattern());

        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        KryptonWsResponseSender mockKryptonWsResponseSender = new KryptonWsResponseSender(session);
        when(session.isOpen()).thenReturn(Boolean.FALSE);
        Whitebox.setInternalState(mockKryptonWsResponseSender, "webSocketSession", session);
        kryptonWsSession.setKryptonWsResponseSender(mockKryptonWsResponseSender);
        TestUtils.inject(kryptonWsPlugin, KryptonWsPlugin.class, "kryptonWsSession", kryptonWsSession);

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);
        kryptonWsSession.setWebapiFeatures(iWebApiFeatures);

        when(iWebApiFeatures.getWebApiEndpoint(any())).thenReturn(webApiEndpoint);
        when(webApiEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(cc);

        assertTrue(kryptonWsPlugin.onSocketOpen(session));
        assertFalse(kryptonWsPlugin.onSocketMessage(null, null));
        assertTrue(kryptonWsPlugin.onSocketClosed(null, null));
        assertTrue(kryptonWsPlugin.onSocketPong(null, null));
        assertTrue(kryptonWsPlugin.onSocketError(null, null));
        kryptonWsPlugin.onInitPlugin(iWebApiFeatures);

        TestUtils.inject(kryptonWsPlugin, KryptonWsPlugin.class, "kryptonWsSession", kryptonWsSession);

        assertTrue(kryptonWsPlugin.onSocketOpen(session));
        assertFalse(kryptonWsPlugin.onSocketMessage(session, null));
        assertFalse(kryptonWsPlugin.onSocketMessage(session, null));
//        assertTrue(kryptonWsPlugin.onSocketBinary(session, null));
        assertTrue(kryptonWsPlugin.onSocketClosed(session, null));
        assertTrue(kryptonWsPlugin.onSocketPong(session, null));
        assertTrue(kryptonWsPlugin.onSocketError(session, null));
        assertTrue(kryptonWsPlugin.onSocketError(session, new Throwable("Idle timeout expired")));
        assertTrue(kryptonWsPlugin.onSocketError(session, new Throwable("Timeout on Read")));
        assertTrue(kryptonWsPlugin.onSocketError(session, new Throwable("Broken Pipe")));
        assertTrue(kryptonWsPlugin.onSocketError(session, new Throwable()));

        assertFalse(kryptonWsPlugin.getPluginFeatures().getJwtValidationsConfigurator().isPresent());
    }

    @Test
    public void sessionAndMethodValidationScenarios()
    {
        assertTrue(kryptonWsPlugin.onSocketOpen(session));

        String startRecognitionTimersMethodJson = "{\n" +
                                                  "   \"method\" : \"StartRecognitionTimers\",\n" +
                                                  "   \"sessionId\" : \"123\"\n" +
                                                  "}";
        //Should fail as there's no session
        assertFalse(kryptonWsPlugin.onSocketMessage(session, startRecognitionTimersMethodJson));

        //Should fail as languagePack is missing
        JSONObject createSessionNoLanguagePack = new JSONObject(KryptonWsCreateSessionMethodTest.createSessionJsonString);
        createSessionNoLanguagePack.remove("languagePack");
        assertTrue(kryptonWsPlugin.onSocketMessage(session, createSessionNoLanguagePack.toString()));

        //Should succeed as there's no session
        assertTrue(kryptonWsPlugin.onSocketMessage(session, KryptonWsCreateSessionMethodTest.createSessionJsonString));

        //Should fail as we already have a session
        assertFalse(kryptonWsPlugin.onSocketMessage(session, KryptonWsCreateSessionMethodTest.createSessionJsonString));

        //Should fail as the session id doesn't match
        assertFalse(kryptonWsPlugin.onSocketMessage(session, startRecognitionTimersMethodJson));
    }

    @Test
    public void onSocketInvalidIdsAndUnsupportedVersion(){

        String invalidSessionId = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00001\",\n" +
                "    \"sessionId\": \"!@#$@#$\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }";

        String invalidRequestId = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"@#$%@#\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }}";

        String invalidVersion = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00001\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": \"13.0\",\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }}";

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);

        kryptonWsPlugin.getKryptonWsSession().setKryptonWsResponseSender(mockSender);
        assertFalse(kryptonWsPlugin.onSocketMessage(session, invalidSessionId));
        assertFalse(kryptonWsPlugin.onSocketMessage(session, invalidRequestId));
        assertFalse(kryptonWsPlugin.onSocketMessage(session, invalidVersion));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSender, times(3)).sendResponse(captor.capture());

        JSONObject requestIdResponse = new JSONObject(captor.getAllValues().get(1));
        JSONObject sessionIdResponse = new JSONObject(captor.getAllValues().get(0));
        JSONObject sessionVersionResponse = new JSONObject(captor.getAllValues().get(2));

        assertEquals("failed", requestIdResponse.get("status"));
        assertEquals("failed", sessionIdResponse.get("status"));
        assertEquals("failed", sessionVersionResponse.get("status"));

        JSONObject requestIdError = new JSONObject(captor.getAllValues().get(1)).getJSONArray("errors").getJSONObject(0);
        JSONObject sessionIdError = new JSONObject(captor.getAllValues().get(0)).getJSONArray("errors").getJSONObject(0);
        JSONObject versionError = new JSONObject(captor.getAllValues().get(2)).getJSONArray("errors").getJSONObject(0);

        assertEquals("sessionId or requestId format is invalid", sessionIdError.get("message"));
        assertEquals("sessionId or requestId format is invalid", requestIdError.get("message"));
        assertEquals("Unsupported protocol version", versionError.get("message"));
        assertEquals("Supported versions are [1.0]", versionError.get("extra"));
    }

    @Test
    public void onSocketNullVersion(){
        String nullIdVersion = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00001\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": null,\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }}";

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);

        kryptonWsPlugin.getKryptonWsSession().setKryptonWsResponseSender(mockSender);
        assertFalse(kryptonWsPlugin.onSocketMessage(session, nullIdVersion));
    }

    @Test
    public void onSocketMissingVersion(){
        String missingIdVersion = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00001\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }}";

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);

        kryptonWsPlugin.getKryptonWsSession().setKryptonWsResponseSender(mockSender);
        assertFalse(kryptonWsPlugin.onSocketMessage(session, missingIdVersion));
    }

    @Test
    public void onSocketEmptyVersions() {
        String emptyIdVersion = "  {\n" +
                "    \"attach\": true,\n" +
                "    \"clientData\": {\n" +
                "      \"applicationName\": \"MyApp\",\n" +
                "      \"applicationVersion\": \"1.0\",\n" +
                "      \"companyName\": \"Nuance\"\n" +
                "    },\n" +
                "    \"method\": \"CreateSession\",\n" +
                "    \"recognitionParameters\": {\n" +
                "      \"startRecognitionTimers\": false\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00001\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": \"   \",\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }}";

        KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);

        kryptonWsPlugin.getKryptonWsSession().setKryptonWsResponseSender(mockSender);
        assertFalse(kryptonWsPlugin.onSocketMessage(session, emptyIdVersion));
    }
    @Test
    public void onSocketBinary_rightTime() throws IOException
    {
        KryptonWsSession kryptonWsSession = kryptonWsPlugin.getKryptonWsSession();

        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
        IWebApiEndpoint webApiEndpoint = kryptonWsSession.getWebapiFeatures()
                                                         .getWebApiEndpoint(kryptonWsWebapiResponseListener);

        KryptonWsMethod kryptonWsMethod = mock(KryptonWsMethod.class);
        kryptonWsWebapiResponseListener.setCurrentMethod(kryptonWsMethod);

        kryptonWsSession.setKryptonWsWsSession(kryptonWsSession.getKryptonWsWsSession())
                        .setKryptonWsResponseSender(kryptonWsResponseSender)
                        .setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setWebApiEndpoint(webApiEndpoint)
                        .setKryptonWsSessionId("kryptonWsSessionId")
                        .setWebapiSessionId("wapiSessionId");

        kryptonWsPlugin.onSocketBinary(session, new byte[0]);
        verify(kryptonWsMethod).onAudio(any());
    }

    @Test
    public void onSocketBinary_wrongTime() throws IOException
    {
        KryptonWsSession kryptonWsSession = kryptonWsPlugin.getKryptonWsSession();

        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
        IWebApiEndpoint webApiEndpoint = kryptonWsSession.getWebapiFeatures().getWebApiEndpoint(
                kryptonWsWebapiResponseListener);

        kryptonWsSession.setKryptonWsWsSession(kryptonWsSession.getKryptonWsWsSession())
                        .setKryptonWsResponseSender(kryptonWsResponseSender)
                        .setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setWebApiEndpoint(webApiEndpoint)
                        .setKryptonWsSessionId("kryptonWsSessionId")
                        .setWebapiSessionId("wapiSessionId");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        kryptonWsPlugin.onSocketBinary(session, new byte[0]);
        verify(kryptonWsResponseSender, never()).sendResponse(captor.capture());

        config.setProperty(KRYPTON_WS_IGNORE_STRAY_AUDIO.getKey(), false);
        kryptonWsPlugin.onSocketBinary(session, new byte[0]);

        verify(kryptonWsResponseSender).sendResponse(captor.capture());
        String actualResponse = captor.getValue();

        String expectedResponse = "{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"extra\": \"idle\",\n" +
                "      \"message\": \"Command not supported in this state\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"method\": \"Response\",\n" +
                "  \"requestId\": \"2cf0ff0c-f0bf-413b-a55f-18037b69b554\",\n" +
                "  \"responseTo\": \"Audio\",\n" +
                "  \"sessionId\": \"kryptonWsSessionId\",\n" +
                "  \"status\": \"failed\",\n" +
                "  \"version\": \"1.0\"\n" +
                "}";

        Map<String, Object> expected = new JSONObject(expectedResponse).toMap();
        expected.remove("requestId");

        Map<String, Object> actual = new JSONObject(actualResponse).toMap();
        actual.remove("requestId");

        assertEquals(expected, actual);
    }

    @Test
    public void initExecutorService()
            throws IOException
    {
        KryptonWsPlugin kryptonWsPlugin = new KryptonWsPlugin();

        config.setProperty(KRYPTON_WS_ASYNC_WEBAPI_CALLS.getKey(), false);
        kryptonWsPlugin.initPluginSettingsAndExecutorService(iWebApiFeatures);

        assertTrue(kryptonWsPlugin.getExecutorService() instanceof ListeningExecutorService);

        when(iWebApiFeatures.getConfigs()).thenThrow(new IOException());
        kryptonWsPlugin.initPluginSettingsAndExecutorService(iWebApiFeatures);

        assertFalse(kryptonWsPlugin.getExecutorService() instanceof ListeningExecutorService);
    }

    @Test
    public void cannotRunWhile()
    {
        assertTrue(kryptonWsPlugin.onSocketOpen(session));

        assertTrue(kryptonWsPlugin.onSocketMessage(session, KryptonWsCreateSessionMethodTest.createSessionJsonString));

        // Recognize cannot be run right now because the create session method has not finished
        assertFalse(kryptonWsPlugin.onSocketMessage(session, "{\n" +
                                                             "  \"method\": \"Recognize\",\n" +
                                                             "  \"recognitionParameters\": {\n" +
                                                             "    \"audioFormat\": \"audio/L16;rate=16000\"\n" +
                                                             "  },\n" +
                                                             "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                                                             "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                                                             "  \"version\": \"1.0\"\n" +
                                                             "}"));
    }
}