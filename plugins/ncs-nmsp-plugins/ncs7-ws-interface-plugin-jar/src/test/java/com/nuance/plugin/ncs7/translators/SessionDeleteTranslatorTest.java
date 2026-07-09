package com.nuance.plugin.ncs7.translators;

import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SessionDeleteTranslatorTest {

    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    RemoteEndpoint.Async mockAsync;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        mockAsync = mock(RemoteEndpoint.Async.class);

        when(mockSession.isOpen()).thenReturn(true);
        doReturn(mockAsync).when(mockSession).getAsyncRemote();

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
        orchestrator.setWebApiFeatures(mock(IWebApiFeatures.class));
    }

    @After
    public void tearDown() throws Exception {
        when(mockSession.isOpen()).thenReturn(false);
    }

    @Test
    public void execute() throws InterruptedException
    {
        orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                "\"type\":\"result\",\n" +
                "\"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "\"command\":{\n" +
                "\"name\":\"sessionDelete\",\n" +
                "\"id\":\"45\"\n" +
                "},\n" +
                "\"result\":{\n" +
                "\"final\":true,\n" +
                "\"status\":\"success\"\n" +
                "}\n" +
                "}"));

        NcsRequest request = new NcsRequest();
        request.data_session_id = "fakeSessionId";
        SessionDeleteTranslator translator = new SessionDeleteTranslator(orchestrator, request);

        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }

        translator.commandCleanup();

        verify(mockAsync,atLeastOnce()).sendText("{\"message\":\"query_response\",\"transaction_id\":45,\"result_type\":\"NVC_DELETE_SESSION_DATA_CMD\",\"NMAS_PRFX_TRANSACTION_ID\":\"45\"}");
    }
}