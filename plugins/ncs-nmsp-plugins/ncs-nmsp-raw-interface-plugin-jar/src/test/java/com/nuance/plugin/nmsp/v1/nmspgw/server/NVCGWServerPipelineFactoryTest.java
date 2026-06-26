package com.nuance.plugin.nmsp.v1.nmspgw.server;

import com.nuance.cobra.utils.Tools;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*", "jdk.xml.*", "java.xml.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "javax.management.*", "org.w3c.dom.*", "sun.security.*", "javax.net.ssl.*"})
@PrepareForTest({Tools.class})
public class NVCGWServerPipelineFactoryTest {
    @Test
    public void getPipeline() {

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
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
                    public void onOpen(Session session) throws IOException {

                    }

                    @Override
                    public void onMessage(Session session, String message) throws IOException {

                    }

                    @Override
                    public void onBinary(Session session, byte[] buffer) throws IOException {

                    }

                    @Override
                    public void onPong(Session session, PongMessage pongMessage) throws IOException {

                    }

                    @Override
                    public void onError(Session session, Throwable throwable) throws IOException {

                    }

                    @Override
                    public void onClose(Session session, CloseReason reason) throws IOException {

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
        ChannelGroup mockChannelGroup = mock(ChannelGroup.class);
        NVCGWServerPipelineFactory testFactory = new NVCGWServerPipelineFactory(mockChannelGroup, testFeatures);

        // Create mock socket channel and pipeline
        SocketChannel mockChannel = mock(SocketChannel.class);
        ChannelPipeline mockPipeline = mock(ChannelPipeline.class);
        when(mockChannel.pipeline()).thenReturn(mockPipeline);
        when(mockPipeline.addLast(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(mockPipeline);

        // Test without SSL
        testFactory.initChannel(mockChannel);

        verify(mockPipeline, atLeastOnce()).addLast(
            org.mockito.ArgumentMatchers.eq(NVCGWServerPipelineFactory.NMSP_MESSAGE_DECODER_NAME),
            org.mockito.ArgumentMatchers.any());
        verify(mockPipeline, atLeastOnce()).addLast(
            org.mockito.ArgumentMatchers.eq(NVCGWServerPipelineFactory.NMSP_MESSAGE_ENCODER_NAME),
            org.mockito.ArgumentMatchers.any());
        verify(mockPipeline, atLeastOnce()).addLast(
            org.mockito.ArgumentMatchers.eq(NVCGWServerPipelineFactory.NMSP_MESSAGE_HANDLER_NAME),
            org.mockito.ArgumentMatchers.any());

        // Test with SSL enabled
        String currentPath = Tools.getJettyPath();
        mockStatic(Tools.class);
        PowerMockito.when(Tools.getJettyPath()).thenReturn(currentPath + "/src/test/resources/");
        SSLHandlerProvider.initSSLContext(config);

        SocketChannel mockChannelSSL = mock(SocketChannel.class);
        ChannelPipeline mockPipelineSSL = mock(ChannelPipeline.class);
        when(mockChannelSSL.pipeline()).thenReturn(mockPipelineSSL);
        when(mockPipelineSSL.addLast(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(mockPipelineSSL);

        testFactory.initChannel(mockChannelSSL);

        // Verify SSL handler was added
        verify(mockPipelineSSL, atLeastOnce()).addLast(
            org.mockito.ArgumentMatchers.eq(NVCGWServerPipelineFactory.SSL_PROVIDER_NAME),
            org.mockito.ArgumentMatchers.any());
    }
}