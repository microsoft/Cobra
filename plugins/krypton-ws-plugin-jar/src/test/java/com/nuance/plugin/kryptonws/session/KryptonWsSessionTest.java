package com.nuance.plugin.kryptonws.session;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import javax.websocket.CloseReason;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsSessionTest
{
    KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
    IWebApiEndpoint mockWebApiEndpoint = mock(IWebApiEndpoint.class);
    IWebApiFeatures iWebApiFeatures = mock(IWebApiFeatures.class);

    @Test(expected = Test.None.class)
    public void webapiWsSession()
    {
        kryptonWsSession.initWebapiWsSession();
        kryptonWsSession.initWebapiWsSession();
        kryptonWsSession.setWebApiEndpoint(mockWebApiEndpoint);

        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, "");
        kryptonWsSession.terminateWebapiWsSession(closeReason);
        kryptonWsSession.terminateWebapiWsSession(closeReason);
    }

    @Test
    public void testGetSet()
    {
        kryptonWsSession.setSessionObjects(null);
        assertNull(kryptonWsSession.getSessionObjects());
        kryptonWsSession.setCreateSessionLanguagePack(null);
        assertNull(kryptonWsSession.getCreateSessionLanguagePack());
        assertNull(kryptonWsSession.getWebapiResponseListener());
    }

    @Test
    public void getPluginConfig()
            throws IOException
    {
        kryptonWsSession.setWebapiFeatures(iWebApiFeatures);
        CombinedConfiguration cc = new CombinedConfiguration();

        when(iWebApiFeatures.getConfigs()).thenReturn(cc);

        assertEquals(cc, kryptonWsSession.getPluginConfig());

        when(iWebApiFeatures.getConfigs()).thenThrow(new IOException());
        assertNotEquals(cc, kryptonWsSession.getPluginConfig());
    }
}
