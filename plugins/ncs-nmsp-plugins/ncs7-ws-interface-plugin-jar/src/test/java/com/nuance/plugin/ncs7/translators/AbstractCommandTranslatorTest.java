package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class AbstractCommandTranslatorTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    RemoteEndpoint.Async mockAsync;
    ISession mochSession;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        mockAsync = mock (RemoteEndpoint.Async.class);
        mochSession = mock(ISession.class);

        doReturn(mockAsync).when(mockSession).getAsyncRemote();
        doReturn(mochSession).when(mockWebapi).getActiveSession();
        when(mochSession.getSessionId()).thenReturn("test");

        when(mockSession.isOpen()).thenReturn(true);

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
    }

    @Test
    public void testUtilities() {
        assertEquals("Upper", AbstractCommandTranslator.capitalise("upper"));
        assertEquals("Upper", AbstractCommandTranslator.capitalise("Upper"));
        assertEquals("", AbstractCommandTranslator.capitalise(""));
        assertNull(AbstractCommandTranslator.capitalise(null));
    }

    @After
    public void tearDown() throws Exception {
        doReturn(false).when(mockSession).isOpen();
    }

    @Test
    /**
     * When protocol is v1, a 'disconnect' message is NOT sent after each transaction.
     */
    public void doFinal_V1() throws IOException {
//        try (MockedStatic<AbstractCommandTranslator> staticMockAbstractCommandTranslator = Mockito.mockStatic(AbstractCommandTranslator.class)) {

//            Orchestrator mockOrchestrator = mock(Orchestrator.class);
//            when(mockOrchestrator.getProtocolVersion()).thenReturn(Orchestrator.ProtocolVersion.V1);
        Orchestrator orch = orchestrator;

        WebApiResponse webApiResponse = mock(WebApiResponse.class);
        when(webApiResponse.isFinal()).thenReturn(true);
        when(webApiResponse.isSuccess()).thenReturn(true);

        AbstractCommandTranslator translator = new AbstractCommandTranslator(null, null) {
            @Override
            public void execute() throws IOException {
            }

            @Override
            public void commandCleanup() {
            }

            @Override
            ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
                return doFinal(orch, webApiResponse);
            }
        };

        translator.doResponse(webApiResponse);

//            staticMockAbstractCommandTranslator.verify(
//                    times(0),
//                    () -> AbstractCommandTranslator.sendDisconnect(any(), eq(1), any())
//            );
//        }
    }

    @Test
    /**
     * When protocol is v2, a 'disconnect' message is sent after each transaction.
     */
    public void doFinal_V2_statusSuccess() throws IOException {
//        try (MockedStatic<AbstractCommandTranslator> staticMockAbstractCommandTranslator = Mockito.mockStatic(AbstractCommandTranslator.class)) {

//            Orchestrator mockOrchestrator = mock(Orchestrator.class);
//            when(mockOrchestrator.getProtocolVersion()).thenReturn(Orchestrator.ProtocolVersion.V2);
        Orchestrator orch = orchestrator;
        Map<String, String> pathParams = new HashMap();
        pathParams.put("pathParam", "v2");
        when(mockSession.getPathParameters()).thenReturn(pathParams);

        WebApiResponse webApiResponse = mock(WebApiResponse.class);
        when(webApiResponse.isFinal()).thenReturn(true);
        when(webApiResponse.isSuccess()).thenReturn(true);

        AbstractCommandTranslator translator = new AbstractCommandTranslator(orchestrator, null) {
            @Override
            public void execute() throws IOException {
            }

            @Override
            public void commandCleanup() {
            }

            @Override
            ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
                return doFinal(orch, webApiResponse);
            }
        };

        translator.doResponse(webApiResponse);

//            staticMockAbstractCommandTranslator.verify(
//                    times(1),
//                    () -> AbstractCommandTranslator.sendDisconnect(any(), anyInt(), any())
//            );
//        }
    }
}