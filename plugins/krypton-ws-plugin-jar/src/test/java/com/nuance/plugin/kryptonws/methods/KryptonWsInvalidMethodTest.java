package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import javax.websocket.Session;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KryptonWsInvalidMethodTest
{
    Session session = new WebSocketHttpSession(new FakeHttpSession());
    KryptonWsInvalidMethod kryptonWsInvalidMethod = new KryptonWsInvalidMethod(null, new KryptonWsSession(mock(ExecutorService.class)));

    @Test(expected = Test.None.class)
    public void onAudioImplementation()
            throws IOException
    {
        KryptonWsResponseSender responseSender = mock(KryptonWsResponseSender.class);
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession session = mock(ISession.class);
        CombinedConfiguration cc = mock(CombinedConfiguration.class);

        when(webApiEndpoint.getActiveSession()).thenReturn(session);
        when(session.getSessionConfig()).thenReturn(cc);

        IWebApiFeatures iWebApiFeatures = mock(IWebApiFeatures.class);
        when(iWebApiFeatures.getConfigs()).thenReturn(cc);

        kryptonWsInvalidMethod.kryptonWsSession.setKryptonWsResponseSender(responseSender);
        kryptonWsInvalidMethod.kryptonWsSession.setWebApiEndpoint(webApiEndpoint);
        kryptonWsInvalidMethod.kryptonWsSession.setWebapiFeatures(iWebApiFeatures);

        kryptonWsInvalidMethod.onAudioImplementation(null);
    }

    @Test(expected = Test.None.class)
    public void processWebapiResponse()
    {
        kryptonWsInvalidMethod.processWebapiResponse("");
    }

    @Test
    public void getLogger()
    {
        assertNotNull(kryptonWsInvalidMethod.getLogger());
    }
}