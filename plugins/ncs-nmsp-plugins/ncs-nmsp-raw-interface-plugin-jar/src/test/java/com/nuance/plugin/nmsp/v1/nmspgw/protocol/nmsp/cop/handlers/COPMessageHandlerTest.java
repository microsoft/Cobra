package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.handlers;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConfirmMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPDisconnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPPingResponseMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class COPMessageHandlerTest {

    CombinedConfiguration mockConfig;
    IWebApiFeatures testFeatures;
    Session testSession;
    Channel mockChannel;

    @Before
    public void initTest() throws IOException {

        //init deserializers:
        BCPBeginDeserializerRegistry.findBCPBeginDeserializerResource();
        COPConnectDeserializerRegistry.findCOPConnectDeserializerResource();

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        config.addProperty(PdxInterfaceConfig.Property.SSL_ENABLED.getKey(), false);
        mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);
        when(mockConfig.getString("nmaidKey")).thenReturn("1234");

        testFeatures = new IWebApiFeatures() {
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
                        return null;
                    }

                    @Override
                    public ISession getWebapiSession(String sessionID) {
                        return null;
                    }
                };
            }
        };

        mockChannel = mock(Channel.class);
        when(mockChannel.closeFuture()).thenReturn(mock(ChannelFuture.class));
        when(mockChannel.isOpen()).thenReturn(true);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
        NMSPClientManager.init();
        testSession.setClient(NMSPClientManager.getInstance());
    }

    @Test
    public void handleCOPConnectMessage() {
        COPConnectMessage testMessage = new COPConnectMessage((short) 20);
        testMessage.setNmaid("test");
        COPMessageHandler.handleMessage(testSession, testMessage);
        assertEquals("test", testSession.getNmaid());
    }

    @Test
    public void handleCOPConfirmMessage() {
        testSession.setState(Session.STATE.CHALLENGING);
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        COPConfirmMessage testMessage = new COPConfirmMessage((short) 20);
        testSession.setNmaid("test");
        COPMessageHandler.handleMessage(testSession, testMessage);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());

        try {
            testMessage.setEncryptedSessionId(Hex.decodeHex("a94afacbefad23e035650a8260f4b857919e8047"));
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        COPMessageHandler.handleMessage(testSession, testMessage);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void handleCOPDisconnectMessage() {
        testSession.setState(Session.STATE.CHALLENGING);
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        COPDisconnectMessage testMessage = new COPDisconnectMessage((short) 20);
        testSession.setNmaid("test");
        COPMessageHandler.handleMessage(testSession, testMessage);

        assertEquals(Session.STATE.DISCONNECTED, testSession.getState());
    }

    @Test
    public void handleCOPPingResponseMessage() {
        testSession.setState(Session.STATE.CHALLENGING);
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        COPPingResponseMessage testMessage = new COPPingResponseMessage((short) 20);
        testSession.setNmaid("test");
        COPMessageHandler.handleMessage(testSession, testMessage);

        assertEquals(0, testSession.getRoundTripTime());
    }
}