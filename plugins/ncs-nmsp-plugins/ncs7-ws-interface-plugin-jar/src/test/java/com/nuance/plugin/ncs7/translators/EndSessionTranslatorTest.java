package com.nuance.plugin.ncs7.translators;

import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class EndSessionTranslatorTest {

    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);

        when(mockSession.isOpen()).thenReturn(true);

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
    }

    @After
    public void tearDown() throws Exception {
        when(mockSession.isOpen()).thenReturn(false);
        testState = 0;
    }

    @Test
    public void execute() {
        Thread t =
                new Thread(() -> {
                    while (testState == 1) {
                        Thread.yield(); //wait here
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "\"type\":\"result\",\n" +
                            "\"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "\"command\":{\n" +
                            "\"name\":\"endSession\",\n" +
                            "\"id\":\"45\"\n" +
                            "},\n" +
                            "\"result\":{\n" +
                            "\"final\":true,\n" +
                            "\"status\":\"success\"\n" +
                            "}\n" +
                            "}"));

                    Thread.yield(); //wait here

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "\"type\":\"result\",\n" +
                            "\"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "\"command\":{\n" +
                            "\"name\":\"endSession\",\n" +
                            "\"id\":\"45\"\n" +
                            "},\n" +
                            "\"result\":{\n" +
                            "\"final\":true,\n" +
                            "\"status\":\"success\"\n" +
                            "}\n" +
                            "}"));

                    Thread.yield(); //wait here

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        testState = 2;
        EndSessionTranslator translator = new EndSessionTranslator(orchestrator, null);
        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }

        RemoteEndpoint.Async mockAsync = mock (RemoteEndpoint.Async.class);
        doReturn(mockAsync).when(mockSession).getAsyncRemote();
        ISession mochSession = mock(ISession.class);
        doReturn(mochSession).when(mockWebapi).getWebapiSession(anyString());
        when(mochSession.getSessionId()).thenReturn("test");
        orchestrator.setWebapiSessionID("testID");

        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void commandCleanup() {
    }

    @Test
    /**
     * When protocol is v1, websocket is closed after EndSession command.
     */
    public void doResponse_V1() throws IOException {
        Orchestrator mockOrchestrator = mock(Orchestrator.class);
        when(mockOrchestrator.getProtocolVersion()).thenReturn(Orchestrator.ProtocolVersion.v1);
        Session mockWsSession = mock(Session.class);
        when(mockOrchestrator.getWebSocketSession()).thenReturn(mockWsSession);

        EndSessionTranslator translator = new EndSessionTranslator(mockOrchestrator, null);
        translator.doResponse(null);

        verify(mockWsSession, times(1)).close(any());
    }

    @Test
    /**
     * When protocol is v2, websocket is not closed after EndSession command.
     */
    public void doResponse_V2() throws IOException {
        Orchestrator mockOrchestrator = mock(Orchestrator.class);
        when(mockOrchestrator.getProtocolVersion()).thenReturn(Orchestrator.ProtocolVersion.v2);
        Session mockWsSession = mock(Session.class);
        when(mockOrchestrator.getWebSocketSession()).thenReturn(mockWsSession);

        EndSessionTranslator translator = new EndSessionTranslator(mockOrchestrator, null);
        translator.doResponse(null);

        verify(mockWsSession, times(0)).close(any());
    }
}