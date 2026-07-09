package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class KryptonWsStartRecognitionTimersMethodTest
{
    KryptonWsSession kryptonWsSession;
    KryptonWsStartRecognitionTimersMethod kryptonWsStartRecognitionTimersMethod;
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener;
    KryptonWsRecognizeMethod kryptonWsRecognizeMethod;
    KryptonWsResponseSender kryptonWsResponseSender;

    @Before
    public void setUp()
    {
        kryptonWsSession = mock(KryptonWsSession.class);
        kryptonWsStartRecognitionTimersMethod = new KryptonWsStartRecognitionTimersMethod(new JSONObject(),
                kryptonWsSession);
        kryptonWsWebapiResponseListener = mock(KryptonWsWebapiResponseListener.class);
        kryptonWsRecognizeMethod = mock(KryptonWsRecognizeMethod.class);
        kryptonWsResponseSender = mock(KryptonWsResponseSender.class);

        when(kryptonWsSession.getWebapiResponseListener()).thenReturn(kryptonWsWebapiResponseListener);
        when(kryptonWsSession.getKryptonWsResponseSender()).thenReturn(kryptonWsResponseSender);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(mockConfig);
    }

    @Test
    public void misc()
    {
        kryptonWsStartRecognitionTimersMethod.processWebapiResponse("");
        kryptonWsStartRecognitionTimersMethod.onAudioImplementation(new byte[0]);
        assertNotNull(kryptonWsStartRecognitionTimersMethod.getLogger());
    }

    @Test
    public void process()
    {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        when(kryptonWsWebapiResponseListener.getCurrentMethod()).thenReturn(null);
        kryptonWsStartRecognitionTimersMethod.process();
        verify(kryptonWsResponseSender).sendResponse(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().contains("no-action"));


        when(kryptonWsWebapiResponseListener.getCurrentMethod()).thenReturn(kryptonWsRecognizeMethod);
        when(kryptonWsRecognizeMethod.startRecognitionTimer(true)).thenReturn(KryptonWsMessage.STATUS.COMPLETE);
        kryptonWsStartRecognitionTimersMethod.process();
        verify(kryptonWsResponseSender, times(2)).sendResponse(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().contains("complete"));


        when(kryptonWsWebapiResponseListener.getCurrentMethod()).thenReturn(mock(KryptonWsInvalidMethod.class));
        kryptonWsStartRecognitionTimersMethod.process();
        verify(kryptonWsResponseSender, times(3)).sendResponse(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().contains("failed"));
    }

    @Test
    public void canRunWhile()
    {
        kryptonWsSession = mock(KryptonWsSession.class);
        kryptonWsStartRecognitionTimersMethod = new KryptonWsStartRecognitionTimersMethod(new JSONObject(),
                kryptonWsSession);

        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"http://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"http://host/path/domainlm\"}]}\n";
        KryptonWsLoadMethod
                commandThatIsAlreadyRunning = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        assertTrue(kryptonWsStartRecognitionTimersMethod.canRunWhile(commandThatIsAlreadyRunning));
    }
}
