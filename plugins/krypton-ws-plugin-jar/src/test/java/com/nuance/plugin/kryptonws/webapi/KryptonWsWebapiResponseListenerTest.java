package com.nuance.plugin.kryptonws.webapi;

import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.methods.KryptonWsCreateSessionMethod;
import com.nuance.plugin.kryptonws.methods.KryptonWsInvalidMethod;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KryptonWsWebapiResponseListenerTest
{
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();

    @Test
    public void sendResponse()
    {
        kryptonWsWebapiResponseListener.sendResponse("");

        KryptonWsCreateSessionMethod kryptonWsCreateSessionMethod = mock(KryptonWsCreateSessionMethod.class);
        kryptonWsWebapiResponseListener.setCurrentMethod(kryptonWsCreateSessionMethod);
        assertEquals(kryptonWsCreateSessionMethod, kryptonWsWebapiResponseListener.getCurrentMethod());

        kryptonWsWebapiResponseListener.sendResponse("");
        verify(kryptonWsCreateSessionMethod, timeout(1000).times(1)).processWebapiResponse("");
    }

    @Test(expected = Test.None.class)
    public void misc()
            throws IOException
    {
        kryptonWsWebapiResponseListener.sendBinary("test".getBytes());

        Session session = mock(Session.class);
        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        KryptonWsSession kryptonWsSession = mock(KryptonWsSession.class);
        KryptonWsParameters kryptonWsParameters = mock(KryptonWsParameters.class);
        KryptonWsResponseSender kryptonWsResponseSender = mock(KryptonWsResponseSender.class);
        when(kryptonWsSession.getKryptonWsResponseSender()).thenReturn(kryptonWsResponseSender);
        when(kryptonWsSession.getKryptonWsWsSession()).thenReturn(session);
        when(kryptonWsSession.getKryptonWsParameters()).thenReturn(kryptonWsParameters);
        when(kryptonWsParameters.getConfigurations()).thenReturn(mockConfig);

        when(kryptonWsSession.getWebapiFeatures()).thenReturn(mockFeatures);
        when(mockFeatures.getConfigs()).thenReturn(mockConfig);
        when(kryptonWsSession.getPluginConfig()).thenReturn(mockConfig);
        when(kryptonWsSession.getWebApiEndpoint()).thenReturn(mock(IWebApiEndpoint.class));
        when(kryptonWsSession.getWebApiEndpoint().getActiveSession()).thenReturn(mock(ISession.class));
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(mock(CombinedConfiguration.class));
        when(kryptonWsSession.getWebapiSessionConfig().getBoolean(anyString(),anyBoolean())).thenReturn(false);

        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(mock(CombinedConfiguration.class));

        kryptonWsWebapiResponseListener.setCurrentMethod(new KryptonWsInvalidMethod(new JSONObject(), kryptonWsSession));
        kryptonWsWebapiResponseListener.closeConnection(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "hello"));

        when(kryptonWsSession.getWebapiSessionConfig().getBoolean(anyString(),anyBoolean())).thenReturn(true);
        kryptonWsWebapiResponseListener.closeConnection(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "goodbye"));

        doThrow(new IOException("misc")).when(session).close(any());
        kryptonWsSession.setKryptonWsWsSession(session);
        kryptonWsWebapiResponseListener.closeConnection(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "goodbye with exception"));
    }
}