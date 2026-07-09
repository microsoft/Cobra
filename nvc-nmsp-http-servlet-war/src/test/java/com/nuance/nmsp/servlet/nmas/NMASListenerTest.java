package com.nuance.nmsp.servlet.nmas;

import com.nuance.ncs.nmsp.engine.pdx.PdxQueryError;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryRetry;
import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.Transaction;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NMASListenerTest
{
    Transaction transaction;
    NMASListener nmasListener;

    @Before
    public void setUp()
    {
        transaction = new Transaction(new AuthorizationInfo("abc123", "xyz123"), "zyx987");
        nmasListener = new NMASListener(transaction);
    }

    @Test
    public void onConnected()
    {
        String sid = "abc123";
        nmasListener.onConnected(sid);
        assertEquals(sid, transaction.getSessionId());
    }

    @Test
    public void onConnectionFailed()
    {
        nmasListener.onConnectionFailed((short) 0);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, transaction.getErrorID());
    }

    @Test
    public void onDisconnected()
    {
        nmasListener.onDisconnected((short) 0);
        nmasListener.onDisconnected((short) 1);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, transaction.getErrorID());
        nmasListener.onDisconnected((short) 2);
        assertEquals(HttpServletResponse.SC_REQUEST_TIMEOUT, transaction.getErrorID());
        nmasListener.onDisconnected((short) 3);
        assertEquals(HttpServletResponse.SC_REQUEST_TIMEOUT, transaction.getErrorID());
        nmasListener.onDisconnected((short) 4);
        assertEquals(HttpServletResponse.SC_REQUEST_TIMEOUT, transaction.getErrorID());
        nmasListener.onDisconnected((short) 5);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, transaction.getErrorID());
        nmasListener.onDisconnected((short) 6);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, transaction.getErrorID());
        nmasListener.onDisconnected((short) 7);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, transaction.getErrorID());
        nmasListener.onDisconnected((short) 8);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, transaction.getErrorID());
        nmasListener.onDisconnected((short) 9);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, transaction.getErrorID());
    }

    @Test
    public void onQueryError()
    {
        int[] errors = { 550, 507, 508, 509, 510, 511, 515, 503, 512, 504, 505, 506, 999 };
        for (int error : errors)
        {
            String errorString = Integer.toString(error);
            PdxQueryError queryError = mock(PdxQueryError.class);
            when(queryError.getError()).thenReturn(error);
            when(queryError.getDescription()).thenReturn(errorString);
            nmasListener.onQueryError(queryError);
            assertTrue(transaction.getError().contains(errorString));
        }
    }

    @Test
    public void onQueryRetry()
    {
        PdxQueryRetry retry = mock(PdxQueryRetry.class);
        when(retry.getName()).thenReturn("Bad input");
        when(retry.getCause()).thenReturn(4);
        nmasListener.onQueryRetry(retry);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, transaction.getErrorID());
    }

    @Test
    public void onError()
    {
        nmasListener.onError(new RuntimeException("kaboom"));
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, transaction.getErrorID());
        assertTrue(transaction.getError().contains("kaboom"));
    }

    @Test(expected = Test.None.class)
    public void onAudioReceivedDefaultIsNoop()
    {
        nmasListener.onAudioReceived(new byte[]{ 1, 2, 3 });
    }
}
