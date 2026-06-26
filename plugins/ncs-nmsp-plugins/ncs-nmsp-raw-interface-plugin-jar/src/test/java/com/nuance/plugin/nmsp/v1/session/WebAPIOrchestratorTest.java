package com.nuance.plugin.nmsp.v1.session;

import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageHelper;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPDataMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxCommand;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxObject;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_SECRET;
import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class WebAPIOrchestratorTest {
    Session testSession;
    Channel mockChannel;
    private CombinedConfiguration mockConfigs;
    private IWebApiFeatures testFeatures;

    @Before
    public void initTest() throws IOException {

        //init deserializers:
        BCPBeginDeserializerRegistry.findBCPBeginDeserializerResource();
        COPConnectDeserializerRegistry.findCOPConnectDeserializerResource();

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        config.addProperty(PdxInterfaceConfig.Property.SSL_ENABLED.getKey(), false);
        mockConfigs = PowerMockito.mock(CombinedConfiguration.class);
        when(mockConfigs.configurationAt("ncsNmspRawServer")).thenReturn(config);
        when(mockConfigs.getString("nmaidKey")).thenReturn("1234");
        when(mockConfigs.getString(HASH_SECRET.getKey(), HASH_SECRET.getDefaultValue().toString())).thenReturn("ZmFrZSBzYWx0");

        testFeatures = new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() {
                return mockConfigs;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return mockConfigs;
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
                        return null;
                    }

                    @Override
                    public ISession getWebapiSession(String sessionID) {
                        return null;
                    }
                };
            }
        };

        mockChannel = PowerMockito.mock(Channel.class);
        when(mockChannel.closeFuture()).thenReturn(PowerMockito.mock(ChannelFuture.class));
        when(mockChannel.isActive()).thenReturn(true);
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
        NMSPClientManager.init();
        testSession.setClient(NMSPClientManager.getInstance());
    }

    @Test
    public void getAudioInfo() throws IOException {
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(mock(IWebApiFeatures.class));

        testOrchestrator.doRecordBegin(1l);
        testOrchestrator.setAudioPacketInfo(new PDXDictionary());
        testOrchestrator.audioBytes("test".getBytes());
        testOrchestrator.audioBytes("test0".getBytes());
        testOrchestrator.audioBytes("test00".getBytes());
        testOrchestrator.audioBytes("test000".getBytes());
        testOrchestrator.audioBytes("test0000".getBytes());
        testOrchestrator.onRecordEnd(1l);

        assertTrue(testOrchestrator.getAudioInfo().toString().contains("end_time"));
    }

    @Test
    public void onBcpData() throws IOException {
        testSession.setToCodec(ICodecResource.Codec.PCM16);
        testSession.setFromCodec(ICodecResource.Codec.PCM16);

        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(mock(IWebApiFeatures.class));
        Mockito.when(testSession.getWebAPIOrchestrator().getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString())).thenReturn(HASH_TYPE.getDefaultValue().toString());

        BCPBeginMessage testMessageBegin = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary data = new PdxDictionary();
        data.put("uid", "test");
        testMessageBegin.setQueryBegin(data);
        testOrchestrator.onBCPBegin(testSession, testMessageBegin);

        BCPDataMessage testMessage = new BCPDataMessage((short) 20, "test".getBytes(), (short) 1);

        data = new PdxDictionary();
        data.put("name", "name");
        data.put("type", "type");
        PdxObject obj = new PdxObject(PdxCommand.QueryParam, data);
        try {
            testMessage.setPayload(NMSPMessageHelper.serialize(obj));

        } catch (Exception e) {
        }

        testOrchestrator.onBCPData(testSession, testMessage);

        testMessage = new BCPDataMessage((short) 20, "test".getBytes(), (short) 1);

        data = new PdxDictionary();
        obj = new PdxObject(PdxCommand.QueryEnd, data);
        try {
            testMessage.setPayload(NMSPMessageHelper.serialize(obj));

        } catch (Exception e) {
        }

        testOrchestrator.onBCPData(testSession, testMessage);
    }

    @Test
    public void sessionConfigTest() throws IOException {
        IWebApiFeatures mockWebapiFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint mockEndpoint = mock(IWebApiEndpoint.class);
        doReturn(mockEndpoint).when(mockWebapiFeatures).getWebApiEndpoint(any());
        ISession mocWebapiSession = mock (ISession.class);
        doReturn(mocWebapiSession).when(mockEndpoint).getWebapiSession(any());
        doReturn(mocWebapiSession).when(mockEndpoint).getActiveSession();
        doReturn(mockConfigs).when(mocWebapiSession).getSessionConfig();

        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(mockWebapiFeatures);
        testOrchestrator.setWebAPISessionID("test");

        assertNotNull(testOrchestrator.getSessionConfigs());
    }

    @Test
    public void terminateWebsocketSession() throws IOException {
        IWebApiFeatures mockWebapiFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint mockEndpoint = mock(IWebApiEndpoint.class);
        when(mockWebapiFeatures.getWebApiEndpoint(any())).thenReturn(mockEndpoint);
        ISession mocWebapiSession = mock (ISession.class);
        doReturn(mocWebapiSession).when(mockEndpoint).getWebapiSession(any());
        when(mocWebapiSession.getSessionConfig()).thenReturn(mockConfigs);

        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(mockWebapiFeatures);
        testOrchestrator.initWebsocketSession();

        testOrchestrator.terminateWebsocketSession();

        verify(mockEndpoint, atLeastOnce()).onClose(any(), any());
    }

    @Test
    public void onBcpBegin() throws IOException {
        testSession.setToCodec(ICodecResource.Codec.PCM16);
        testSession.setFromCodec(ICodecResource.Codec.PCM16);

        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(mock(IWebApiFeatures.class));

        Mockito.when(testSession.getWebAPIOrchestrator().getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString())).thenReturn(HASH_TYPE.getDefaultValue().toString());

        BCPBeginMessage testMessageBegin = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary data = new PdxDictionary();
        data.put("uid", "test");
        testMessageBegin.setQueryBegin(data);
        testOrchestrator.onBCPBegin(testSession, testMessageBegin);

        BCPBeginMessage testMessage = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);

        data = new PdxDictionary();
        data.put("name", "name");
        data.put("type", "type");
        PdxObject obj = new PdxObject(PdxCommand.QueryParam, data);
        try {
            testMessage.setPayload(NMSPMessageHelper.serialize(obj));
            testMessage.setQueryBegin(data);

        } catch (Exception e) {
        }

        testOrchestrator.onBCPBegin(testSession, testMessage);

    }
}