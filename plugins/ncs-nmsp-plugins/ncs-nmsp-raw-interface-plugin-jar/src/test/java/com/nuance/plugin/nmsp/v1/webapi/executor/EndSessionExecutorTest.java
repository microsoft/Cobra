package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class EndSessionExecutorTest {
    WebAPIOrchestrator mockOrchestrator;
    CombinedConfiguration mockConfig;
    private ISession mockSession;
    private IWebApiEndpoint mockEndpoint;

    @Before
    public void initTest() {
        mockSession = mock(ISession.class);

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);

        mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);

        IWebApiFeatures testFeatures = new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() {
                return mockConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return mockConfig;
            }

            @Override
            public void close() { }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException {
                return new IWebApiEndpoint() {
                    @Override
                    public void onOpen(javax.websocket.Session session) throws IOException {

                    }

                    @Override
                    public void onMessage(javax.websocket.Session session, String message) throws IOException {

                    }

                    @Override
                    public void onBinary(javax.websocket.Session session, byte[] buffer) throws IOException {

                    }

                    @Override
                    public void onPong(javax.websocket.Session session, PongMessage pongMessage) throws IOException {

                    }

                    @Override
                    public void onError(javax.websocket.Session session, Throwable throwable) throws IOException {

                    }

                    @Override
                    public void onClose(javax.websocket.Session session, CloseReason reason) throws IOException {

                    }

                    @Override
                    public ISession getActiveSession() {
                        return mockSession;
                    }

                    @Override
                    public ISession getWebapiSession(String sessionID) {
                        return mockSession;
                    }
                };
            }
        };

        mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getWebApiFeatures()).thenReturn(testFeatures);
        mockEndpoint = mock(IWebApiEndpoint.class);
        doReturn(mockEndpoint).when(mockOrchestrator).getWebApiEndpoint();
        doReturn(mockSession).when(mockEndpoint).getActiveSession();
        doReturn(mockSession).when(mockEndpoint).getWebapiSession(any());
    }

    @Test
    public void testExecute() throws IOException {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);

        IEndSessionListener mockListener = mock(IEndSessionListener.class);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        EndSessionExecutor endSessionExecutor = new EndSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession", null);
        endSessionExecutor.execute();
        verify(mockListener, atMost(0)).sessionEnded();

        when(mockOrchestrator.isWebapiSessionAlive()).thenReturn(true);
        endSessionExecutor = new EndSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession", null);
        endSessionExecutor.execute();
        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), anyBoolean());
    }

    @Test
    public void testOnResponse() {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        IEndSessionListener mockListener = mock(IEndSessionListener.class);
        nmspSession.setClient(mockClientHelper);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        EndSessionExecutor endSessionExecutor = new EndSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession", null);

        endSessionExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"name\":\"endSession\",\n" +
                "      \"id\":\"45\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"success\"\n" +
                "   }\n" +
                "}");
        verify(mockListener, atMost(0)).sessionEnded();

        endSessionExecutor = new EndSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession", null);
        endSessionExecutor.onResponse("{\n" +
                "     \"type\": \"command\",\n" +
                "     \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "     \"command\": {\n" +
                "         \"name\": \"cancelCommand\",\n" +
                "         \"id\": \"2\"\n" +
                "     }\n" +
                "}");
        verify(mockListener, atMost(0)).sessionEnded();

        endSessionExecutor = new EndSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession", null);
        endSessionExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"45\",\n" +
                "      \"name\":\"endSession\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"error\",\n" +
                "      \"errors\":[\n" +
                "         {\n" +
                "            \"code\":1402,\n" +
                "            \"message\":\"EndSession service failed.\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}");
        verify(mockListener, atMost(0)).sessionEnded();
    }
}