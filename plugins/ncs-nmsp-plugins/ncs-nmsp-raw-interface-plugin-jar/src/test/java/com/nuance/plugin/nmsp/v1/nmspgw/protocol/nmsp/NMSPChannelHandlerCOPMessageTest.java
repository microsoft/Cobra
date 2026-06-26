package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.server.NVCGWServerPipelineFactory;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.net.SocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NMSPChannelHandlerCOPMessageTest {

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
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
        NMSPClientManager.init();
        testSession.setClient(NMSPClientManager.getInstance());
    }

    @Test
    public void messageReceivedCOP() {
        NMSPMessage testMessage = new COPConnectMessage((short) 24);
        NMSPChannelHandler.messageReceived(testMessage, testSession);
        assertEquals(Session.STATE.CHALLENGING, testSession.getState());
    }

    @Test
    public void messageReceivedBCP() {
        WebAPIOrchestrator webAPIOrchestrator = testSession.getWebAPIOrchestrator();

        //session in init state
        NMSPMessage testMessage = new BCPBeginMessage((short) 24, "testID".getBytes(), (short) 1);
        NMSPChannelHandler.messageReceived(testMessage, testSession);
        testSession.setWebAPIOrchestrator(webAPIOrchestrator);

        //happy path
        testMessage = new BCPBeginMessage((short) 24, "testID".getBytes(), (short) 1);
        testSession.setState(Session.STATE.CONNECTED);
        testSession.setToCodec(ICodecResource.Codec.OPUS_8K_MONO_CBR_CPLX0);
        testSession.setFromCodec(ICodecResource.Codec.OPUS_8K_MONO_CBR_CPLX0);
        testSession.setClient(NMSPClientManager.getInstance());
        PdxDictionary query = new PdxDictionary();
        query.put("uid", "testUID");
        ((BCPBeginMessage) testMessage).setQueryBegin(query);
        NMSPChannelHandler.messageReceived(testMessage, testSession);

        //session in wrong state
        testMessage = new BCPBeginMessage((short) 24, "testID".getBytes(), (short) 1);
        testSession.setState(Session.STATE.AUTHENTICATING);
        testSession.setClient(NMSPClientManager.getInstance());
        NMSPChannelHandler.messageReceived(testMessage, testSession);
    }

    @Test
    public void messageReceivedVAP() {
        //session in init state
        NMSPMessage testMessage = new VAPPlayBeginMessage((short) 24);
        NMSPChannelHandler.messageReceived(testMessage, testSession);
    }

    @Test
    public void messageReceivedHandshake() {
        //session in init state
        NMSPMessage testMessage = new NMSPMessage(NMSPMessage.HANDSHAKEBYTE, (short) 24, 0);
        NMSPChannelHandler.messageReceived(testMessage, testSession);
    }

    @Test
    public void channelRegistered() throws Exception {
        ChannelGroup mockChannels = mock(ChannelGroup.class);

        NMSPChannelHandler testNmspChannelHandler = new NMSPChannelHandler(mockChannels, testFeatures);
        ChannelHandlerContext mockHandlerContext = mock(ChannelHandlerContext.class);
        when(mockHandlerContext.channel()).thenReturn(mockChannel);
        testNmspChannelHandler.channelRegistered(mockHandlerContext);

        verify(mockChannels, atLeastOnce()).add(mockChannel);
    }


    @Test
    public void channelActive() throws Exception {
        ChannelGroup mockChannels = mock(ChannelGroup.class);

        NMSPChannelHandler testNmspChannelHandler = new NMSPChannelHandler(mockChannels, testFeatures);
        ChannelHandlerContext mockHandlerContext = mock(ChannelHandlerContext.class);
        ChannelPipeline mockPipeline = mock(ChannelPipeline.class);
        io.netty.util.Attribute mockAttribute = mock(io.netty.util.Attribute.class);

        when(mockHandlerContext.channel()).thenReturn(mockChannel);
        when(mockChannel.pipeline()).thenReturn(mockPipeline);
        when(mockChannel.attr(any())).thenReturn(mockAttribute);
        when(mockChannel.writeAndFlush(any())).thenReturn(mock(ChannelFuture.class));

        testNmspChannelHandler.channelActive(mockHandlerContext);

        verify(mockChannel, atLeastOnce()).attr(any());
        verify(mockChannel, atLeastOnce()).writeAndFlush(any());
    }

    @Test
    public void channelRead() throws Exception {
        ChannelGroup mockChannels = mock(ChannelGroup.class);

        NMSPChannelHandler testNmspChannelHandler = new NMSPChannelHandler(mockChannels, testFeatures);

        ChannelHandlerContext mockHandlerContext = mock(ChannelHandlerContext.class);
        io.netty.util.Attribute mockAttribute = mock(io.netty.util.Attribute.class);

        when(mockHandlerContext.channel()).thenReturn(mockChannel);
        when(mockChannel.attr(any())).thenReturn(mockAttribute);
        when(mockAttribute.get()).thenReturn(null);  // Session is null

        VAPPlayBeginMessage testMessage = new VAPPlayBeginMessage((short) 24);
        testNmspChannelHandler.channelRead(mockHandlerContext, testMessage);
    }

    @Test
    public void channelInactive() throws Exception {
        ChannelGroup mockChannels = mock(ChannelGroup.class);

        NMSPChannelHandler testNmspChannelHandler = new NMSPChannelHandler(mockChannels, testFeatures);
        ChannelHandlerContext mockHandlerContext = mock(ChannelHandlerContext.class);
        ChannelPipeline mockPipeline = mock(ChannelPipeline.class);
        io.netty.util.Attribute mockAttribute = mock(io.netty.util.Attribute.class);
        Channel mockParentChannel = mock(Channel.class);

        when(mockHandlerContext.channel()).thenReturn(mockChannel);
        when(mockChannel.pipeline()).thenReturn(mockPipeline);
        when(mockChannel.attr(any())).thenReturn(mockAttribute);
        when(mockChannel.parent()).thenReturn(mockParentChannel);
        when(mockParentChannel.isActive()).thenReturn(false);

        // Test with null session
        when(mockAttribute.get()).thenReturn(null);
        testNmspChannelHandler.channelInactive(mockHandlerContext);

        // Test with non-null session
        when(mockAttribute.get()).thenReturn(testSession);
        testNmspChannelHandler.channelInactive(mockHandlerContext);
    }

    @Test
    public void exceptionCaught() throws Exception {
        ChannelGroup mockChannels = mock(ChannelGroup.class);

        NMSPChannelHandler testNmspChannelHandler = new NMSPChannelHandler(mockChannels, testFeatures);
        ChannelHandlerContext mockHandlerContext = mock(ChannelHandlerContext.class);
        io.netty.util.Attribute mockAttribute = mock(io.netty.util.Attribute.class);

        when(mockHandlerContext.channel()).thenReturn(mockChannel);
        when(mockChannel.attr(any())).thenReturn(mockAttribute);
        when(mockChannel.isOpen()).thenReturn(true);

        //Session null
        when(mockAttribute.get()).thenReturn(null);
        testNmspChannelHandler.exceptionCaught(mockHandlerContext, new IOException());

        //session not null IOException
        when(mockAttribute.get()).thenReturn(testSession);
        testNmspChannelHandler.exceptionCaught(mockHandlerContext, new IOException());

        //session not null non IOException
        testNmspChannelHandler.exceptionCaught(mockHandlerContext, new InvalidMessageException());

        //session not null IOException with open channel
        when(mockAttribute.get()).thenReturn(testSession);
        when(mockChannel.isOpen()).thenReturn(true);
        testNmspChannelHandler.exceptionCaught(mockHandlerContext, new IOException());
    }

}