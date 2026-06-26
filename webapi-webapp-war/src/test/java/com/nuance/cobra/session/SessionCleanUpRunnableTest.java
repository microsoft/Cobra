package com.nuance.cobra.session;

import com.nuance.cobra.utils.CobraException;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SessionCleanUpRunnableTest {

    @Test
    public void test_run() throws CobraException {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.init();
        Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn("session");
        when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));
        when(mockSession.getSessionConfig().getString(WebApiConfig.Property.APPLICATION_KEY.key)).thenReturn("tenant1");
        when(mockSession.getSessionConfig().getInt(WebApiConfig.Property.TENANT_RATE_LIMIT_CONCURRENT_SESSIONS.key)).thenReturn(5);
        when(mockSession.isExpired()).thenReturn(true);
        sessionManager.putSession(mockSession);
        SessionCleanUpRunnable runnable = new SessionCleanUpRunnable(sessionManager);
        runnable.run();
        verify(mockSession).cancelActiveCommand(anyString());
    }
}
