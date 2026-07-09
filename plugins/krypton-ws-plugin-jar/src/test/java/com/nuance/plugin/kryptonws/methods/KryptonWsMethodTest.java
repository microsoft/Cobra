package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.response.KryptonWsResponseBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.Session;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_LOG_AUDIO;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KryptonWsMethodTest
{
    Session mockSession = mock(Session.class);
    KryptonWsResponseSender mockResponseSender = mock(KryptonWsResponseSender.class);
    ExecutorService executorService = mock(ExecutorService.class);
    KryptonWsSession kryptonWsSession = new KryptonWsSession(executorService);
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
    KryptonWsMethod kryptonWsMethod = new KryptonWsCreateSessionMethod(new JSONObject(), kryptonWsSession);
    IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
    ISession session = mock(ISession.class);
    CombinedConfiguration cc = mock(CombinedConfiguration.class);
    IWebApiFeatures iWebApiFeatures = mock(IWebApiFeatures.class);

    @Before
    public void setUp()
    {
        kryptonWsSession.setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setKryptonWsWsSession(mockSession)
                        .setKryptonWsResponseSender(mockResponseSender)
                        .setKryptonWsSessionId("123")
                        .setWebapiFeatures(iWebApiFeatures);
        kryptonWsMethod.setMethodName(KryptonWsCreateSessionMethod.NAME);
        kryptonWsMethod.setKryptonWsRequestId("123-001");
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);
        when(webApiEndpoint.getActiveSession()).thenReturn(session);
        when(session.getSessionConfig()).thenReturn(cc);
        when(kryptonWsSession.getPluginConfig()).thenReturn(cc);
    }

    @Test
    public void sendInitialResponse()
    {
        kryptonWsMethod.sendInitialResponse(true);
        verify(mockResponseSender, atLeastOnce()).sendResponse(anyString());
    }

    @Test
    public void sendFinalResponse()
    {
        kryptonWsMethod.sendInitialResponse(false);

        Map<String, String> error = KryptonWsResponseBuilder.buildError("some message", "some extra");
        kryptonWsMethod.errors.add(error);
        verify(mockResponseSender, atLeastOnce()).sendResponse(anyString());
    }

    @Test
    public void isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession()
    {
        String methodJson = "{\n" +
                            "   \"method\" : \"Something\",\n" +
                            "   \"sessionId\" : \"b84051e3a33f2094\"\n" +
                            "}";
        KryptonWsMethod kryptonWsMethod = new KryptonWsInvalidMethod(new JSONObject(methodJson), kryptonWsSession);
        assertFalse(kryptonWsMethod.isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession());
        kryptonWsSession.setKryptonWsSessionId("b84051e3a33f2094");
        assertTrue(kryptonWsMethod.isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession());
    }

    @Test
    public void logAudio()
    {
        kryptonWsMethod.sendAudioToWebapi(new byte[0]);

        when(cc.getBoolean(KRYPTON_WS_LOG_AUDIO.getKey())).thenReturn(true);
        kryptonWsMethod.sendAudioToWebapi(new byte[0]);

        verify(executorService, times(2)).submit(any(Runnable.class));
    }

    @Test(expected = InvocationTargetException.class)
    public void invalidRequestIdTest() throws InvocationTargetException {
        String methodJsonString = "{\n" +
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
                "    \"requestId\": true,\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"sessionParameters\": {\n" +
                "      \"audioFormat\": \"audio/basic;rate=8000\"\n" +
                "    },\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"languagePack\": {\n" +
                "      \"language\": \"eng-USA\",\n" +
                "      \"topic\": \"GEN\"\n" +
                "    },\n" +
                "    \"sessionObjects\": [\n" +
                "      {\n" +
                "        \"weight\": 100,\n" +
                "        \"id\": \"Pharmacy_20180626\",\n" +
                "        \"type\": \"application/x-nuance-domainlm\",\n" +
                "        \"url\": \"https://nes-db.nuance.mobi/var/NinaDemo-Abhinav-en-US/Pharmacy_20180626/1.0/1.0.0/krypton/dlm.zip\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"trust\": {\n" +
                "      \"uuid\": \"9b871c68-5f44-11eb-bfef-5f8cac72e978\",\n" +
                "      \"signature\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\"\n" +
                "    }\n" +
                "  }\n";

        KryptonWsMethod method = KryptonWsMethodFactory.createMethod(methodJsonString, kryptonWsSession);

    }

    @Test
    public void testCommonMetrics()
    {
        Map<String, Object> metrics = kryptonWsMethod.buildCommonMetrics();
        long diff = (long)metrics.get("elapsed");
        long start = Long.parseLong((String)((List)metrics.get("timestamps")).get(0));
        long end = Long.parseLong((String)((List)metrics.get("timestamps")).get(1));
        assertNotNull(metrics);
        assertEquals(diff, end - start);
    }

    @Test
    public void testVersion()
    {
        KryptonWsMethod.ProtocolVersion version = KryptonWsMethod.ProtocolVersion.fromString("1.0");

        assertEquals(KryptonWsMethod.ProtocolVersion.V1_0, version);
        assertTrue(KryptonWsMethod.ProtocolVersion.supported.contains(KryptonWsMethod.ProtocolVersion.V1_0));

        version = KryptonWsMethod.ProtocolVersion.fromString("");
        assertEquals(KryptonWsMethod.ProtocolVersion.UNSUPPORTED, version);

        version = KryptonWsMethod.ProtocolVersion.fromString("2.0");
        assertEquals(KryptonWsMethod.ProtocolVersion.UNSUPPORTED, version);
    }
}