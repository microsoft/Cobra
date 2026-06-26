package com.nuance.plugin.ncs7;

import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SocketResponseTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
        when(mockSession.isOpen()).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        when(mockSession.isOpen()).thenReturn(false);
    }

    @Test
    public void callEvents() {
        WebApiResponseHandler response = new WebApiResponseHandler(orchestrator);

        response.sendResponse("test");

        response.sendBinary("test".getBytes());

        response.closeConnection(new CloseReason(CloseReason.CloseCodes.NOT_CONSISTENT, "test"));
    }
}