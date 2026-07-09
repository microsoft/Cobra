package com.nuance.plugin.nmsp.v1.session;

import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.mockito.Mockito.*;

public class NMSPSessionControllerTest extends TestCase {

    @Test
    public void test_beginTransaction() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.CONNECTED);
        when(mockSession.getNmaid()).thenReturn("AnyNMAID");
        when(mockSession.getNumTransactions()).thenReturn(255);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockSession, times(1)).putBcpMessagePdxDicts(captor.capture());
        assertFalse(captor.getValue().containsKey("CONFIDENTIAL_INFO_PATEO_VIN_PTNCS_24050"));
    }

    @Test
    public void test_beginTransaction_confidentialPateo() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.CONNECTED);
        when(mockSession.getNmaid()).thenReturn("NCS_PateoPSA_VoiceRecognitionPCS20150206210026");
        when(mockSession.getNumTransactions()).thenReturn(0);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockSession, times(1)).putBcpMessagePdxDicts(captor.capture());
        assertTrue(captor.getValue().containsKey("CONFIDENTIAL_INFO_PATEO_VIN_PTNCS_24050"));
    }

    @Test
    public void test_beginTransaction_disconnected() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.DISCONNECTED);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        verify(mockSession, times(0)).putBcpMessagePdxDicts(any());
    }

    @Test
    public void test_beginTransaction_maxTransactions() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        IClientHandler mockClient = mock(IClientHandler.class);
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.CONNECTED);
        when(mockSession.getNumTransactions()).thenReturn(256);
        when(mockSession.getClient()).thenReturn(mockClient);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        verify(mockClient, times(1)).sendDisconnect(any(), any());
    }

    @Test
    public void test_beginTransaction_maxTransactionsPerMinute() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        IClientHandler mockClient = mock(IClientHandler.class);
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.CONNECTED);
        when(mockSession.getNumTransactions()).thenReturn(255);
        when(mockSession.getClient()).thenReturn(mockClient);
        when(mockSession.getSessionCreationTime()).thenReturn(System.currentTimeMillis() - 10);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        verify(mockClient, times(1)).sendDisconnect(any(), any());
    }

    @Test
    public void test_beginTransaction_maxTransactionsPerMinute_allowed() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        IClientHandler mockClient = mock(IClientHandler.class);
        Session mockSession = mock(Session.class);
        when(mockSession.getState()).thenReturn(Session.STATE.CONNECTED);
        when(mockSession.getNmaid()).thenReturn("AnyNMAID");
        when(mockSession.getNumTransactions()).thenReturn(65);
        when(mockSession.getClient()).thenReturn(mockClient);
        when(mockSession.getSessionCreationTime()).thenReturn(System.currentTimeMillis() - 120000);
        short transactionId = 1;
        byte[] pdxPayload = new byte[0];
        HashMap<String, Object> queryBeginInfo = new HashMap<>();
        short shortVersion = 0;
        nmspSessionController.beginTransaction(mockSession, transactionId, pdxPayload, queryBeginInfo, shortVersion);
        verify(mockClient, times(0)).sendDisconnect(any(), any());
        verify(mockSession, times(1)).putBcpMessagePdxDicts(any());
    }

    @Test
    public void test_setParams() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        short transactionId = 0;
        long requestId = 1;
        int resourceId = 2;
        boolean sendResponse = true;
        Vector<Parameter> parameters = new Vector<>();

        // Types XML, URI, NMSP, APP, NSS, SLOG, NSSLOG, GWLOG, SVSP, EXT, SIP, SDP
        parameters.add(new Parameter("Ping_IntervalSecs", "1", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_MissedResponsesBeforeDisconnect", "2", Parameter.Type.NMSP));
        parameters.add(new Parameter("sessionTimeoutSecs", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("param1", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_IntervalSecs", "blue", Parameter.Type.NMSP));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.GWLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.EXT));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.APP));
        ISessionListener mockSessionListener = mock(ISessionListener.class);
        nmspSessionController.setParams(mockSession, transactionId, requestId, resourceId, parameters, sendResponse, mockSessionListener);
        verify(mockSessionListener, times(1)).setParamsComplete(eq(mockSession), eq(transactionId), eq(requestId), eq(201), any());
    }

    @Test
    public void test_setParams_allParams() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        short transactionId = 0;
        int requestId = 1;
        int resourceId = 2;
        boolean sendResponse = true;
        Vector<Parameter> parameters = new Vector<>();

        // Types XML, URI, NMSP, APP, NSS, SLOG, NSSLOG, GWLOG, SVSP, EXT, SIP, SDP
        parameters.add(new Parameter("Ping_IntervalSecs", "1", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_MissedResponsesBeforeDisconnect", "2", Parameter.Type.NMSP));
        parameters.add(new Parameter("sessionTimeoutSecs", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("param1", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_IntervalSecs", "blue", Parameter.Type.NMSP));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.NSS));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.NSSLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.GWLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.EXT));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.APP));
        ISessionListener mockSessionListener = mock(ISessionListener.class);
        nmspSessionController.setParams(mockSession, transactionId, requestId, resourceId, parameters, sendResponse, mockSessionListener);
        verify(mockSessionListener, times(0)).setParamsComplete(eq(mockSession), eq(transactionId), eq(requestId), eq(201), any());
    }

    @Test
    public void test_getParams() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        short transactionId = 0;
        long requestId = 1;
        int resourceId = 2;
        Vector<Parameter> parameters = new Vector<>();

        // Types XML, URI, NMSP, APP, NSS, SLOG, NSSLOG, GWLOG, SVSP, EXT, SIP, SDP
        parameters.add(new Parameter("Ping_IntervalSecs", "1", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_MissedResponsesBeforeDisconnect", "2", Parameter.Type.NMSP));
        parameters.add(new Parameter("sessionTimeoutSecs", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("param1", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_IntervalSecs", "blue", Parameter.Type.NMSP));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.GWLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.EXT));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.APP));
        ISessionListener mockSessionListener = mock(ISessionListener.class);
        nmspSessionController.getParams(mockSession, transactionId, requestId, resourceId, parameters, mockSessionListener);
        verify(mockSessionListener, times(1)).getParamsComplete(eq(mockSession), eq(transactionId), eq(requestId), eq(201), any());
    }

    @Test
    public void test_getParams_allParams() {
        NMSPSessionController nmspSessionController = NMSPSessionController.getInstance();
        Session mockSession = mock(Session.class);
        short transactionId = 0;
        int requestId = 1;
        int resourceId = 2;
        Vector<Parameter> parameters = new Vector<>();

        // Types XML, URI, NMSP, APP, NSS, SLOG, NSSLOG, GWLOG, SVSP, EXT, SIP, SDP
        parameters.add(new Parameter("Ping_IntervalSecs", "1", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_MissedResponsesBeforeDisconnect", "2", Parameter.Type.NMSP));
        parameters.add(new Parameter("sessionTimeoutSecs", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("param1", "3", Parameter.Type.NMSP));
        parameters.add(new Parameter("Ping_IntervalSecs", "blue", Parameter.Type.NMSP));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.NSS));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.NSSLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.GWLOG));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.EXT));
        parameters.add(new Parameter("nssParam", "blue", Parameter.Type.APP));
        ISessionListener mockSessionListener = mock(ISessionListener.class);
        nmspSessionController.getParams(mockSession, transactionId, requestId, resourceId, parameters, mockSessionListener);
        verify(mockSessionListener, times(0)).getParamsComplete(eq(mockSession), eq(transactionId), eq(requestId), eq(201), any());
    }
}
