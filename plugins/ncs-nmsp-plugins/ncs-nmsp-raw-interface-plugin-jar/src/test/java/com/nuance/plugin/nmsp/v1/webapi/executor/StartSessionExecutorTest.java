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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_SECRET;
import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_TYPE;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class StartSessionExecutorTest {
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
        when(mockConfig.getString(HASH_SECRET.getKey(), HASH_SECRET.getDefaultValue().toString())).thenReturn("ZmFrZSBzYWx0");

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
    public void testCancelCommand() {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        StartSessionExecutor startSessionExecutor = new StartSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        startSessionExecutor.onClose(null);

        startSessionExecutor.cancelCommand();
        assertTrue(startSessionExecutor.isTerminated());
    }

    @Test
    public void testOnResponse() {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        StartSessionExecutor startSessionExecutor = new StartSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        startSessionExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"27\",\n" +
                "      \"name\":\"startSession\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"success\"\n" +
                "   }\n" +
                "}");
        verify(mockOrchestrator, atLeastOnce()).commandDone(any(), anyBoolean());

        startSessionExecutor = new StartSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        startSessionExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"27\",\n" +
                "      \"name\":\"startSession\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"error\",\n" +
                "      \"errors\":[\n" +
                "         {\n" +
                "            \"code\":1302,\n" +
                "            \"message\":\"StartSession service failed.\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}");
        verify(mockOrchestrator, atLeastOnce()).commandDone(any(), anyBoolean());
    }

    @Test
    public void testExecute() throws IOException {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        nmspSession.setUserId("7");
        nmspSession.setDeviceId("7");
        nmspSession.setDeviceModel("deviceModelXYZ");
        nmspSession.setSubscriberId("perfectSubscriberId");
        nmspSession.setPhoneModel("phoneModelXYZ");

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary pdxDictionary = new PdxDictionary();
        pdxDictionary.put("subscriber_id", "7");
        message.setQueryBegin(pdxDictionary);

        Mockito.when(mockOrchestrator.getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString())).thenReturn(HASH_TYPE.getDefaultValue().toString());

        StartSessionExecutor startSessionExecutor = new StartSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        startSessionExecutor.execute();

        ArgumentCaptor<String> webapiCommand = ArgumentCaptor.forClass(String.class);

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), webapiCommand.capture(), anyBoolean());
        assertTrue(webapiCommand.getValue().contains("deviceModelXYZ"));
    }

    @Test
    public void testExecute_noDeviceModel() throws IOException {
        Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = Mockito.mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        nmspSession.setUserId("7");
        nmspSession.setDeviceId("7");
        nmspSession.setPhoneModel("phoneModelXYZ");

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary pdxDictionary = new PdxDictionary();
        pdxDictionary.put("subscriber_id", "7");
        message.setQueryBegin(pdxDictionary);

        Mockito.when(mockOrchestrator.getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString())).thenReturn(HASH_TYPE.getDefaultValue().toString());

        StartSessionExecutor startSessionExecutor = new StartSessionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        startSessionExecutor.execute();

        ArgumentCaptor<String> webapiCommand = ArgumentCaptor.forClass(String.class);

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), webapiCommand.capture(), anyBoolean());
        assertTrue(webapiCommand.getValue().contains("phoneModelXYZ"));
    }
}