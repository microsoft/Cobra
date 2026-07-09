package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageHelper;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxCommand;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxObject;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.*;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class BCPMessageHandlerTest {
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
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return new IWebApiEndpoint() {
                    @Override
                    public void onOpen(javax.websocket.Session session) {

                    }

                    @Override
                    public void onMessage(javax.websocket.Session session, String message) {

                    }

                    @Override
                    public void onBinary(javax.websocket.Session session, byte[] buffer) {

                    }

                    @Override
                    public void onPong(javax.websocket.Session session, PongMessage pongMessage) {

                    }

                    @Override
                    public void onError(javax.websocket.Session session, Throwable throwable) {

                    }

                    @Override
                    public void onClose(javax.websocket.Session session, CloseReason reason) {

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
        when(mockChannel.isActive()).thenReturn(true);
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
        NMSPClientManager.init();
        testSession.setClient(NMSPClientManager.getInstance());
    }

    @Test
    public void handleBCPGenerateAudioMessage() {
        BCPGenerateAudioMessage testMessage = new BCPGenerateAudioMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPTTSServiceRequestMessage() {
        BCPTTSServiceRequestMessage testMessage = new BCPTTSServiceRequestMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPLoadGrammarMessage() {
        BCPLoadGrammarMessage testMessage = new BCPLoadGrammarMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPRecognizeMessage() {
        BCPRecognizeMessage testMessage = new BCPRecognizeMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPCancelMessage() {
        BCPCancelMessage testMessage = new BCPCancelMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPRetrieveArtifactsMessage() {
        BCPRetrieveArtifactsMessage testMessage = new BCPRetrieveArtifactsMessage((short) 20, "test".getBytes(), (short) 1);
        testMessage.setAudioId(1L);
        testMessage.setRequestId(1L);
        testMessage.setResourceId(1L);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPSetParamsMessage() {
        BCPSetParamsMessage testMessage = new BCPSetParamsMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPGetParamsMessage() {
        BCPGetParamsMessage testMessage = new BCPGetParamsMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPLoadResourceMessage() {
        BCPLoadResourceMessage testMessage = new BCPLoadResourceMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPFreeResourceMessage() {
        BCPFreeResourceMessage testMessage = new BCPFreeResourceMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPFreeResourceIdMessage() {
        BCPFreeResourceIdMessage testMessage = new BCPFreeResourceIdMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPInterpretMessage() {
        BCPInterpretMessage testMessage = new BCPInterpretMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPDataMessage() {
        BCPDataMessage testMessage = new BCPDataMessage((short) 20, "test".getBytes(), (short) 1);

        PdxDictionary data = new PdxDictionary();
        data.put("name", "name");
        data.put("type", "type");
        PdxObject obj = new PdxObject(PdxCommand.QueryParam, data);
        try {
            testMessage.setPayload(NMSPMessageHelper.serialize(obj));

        } catch (Exception e) {
        }


        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
    }

    @Test
    public void handleBCPBeginMessage() {
        BCPBeginMessage testMessage = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary data = new PdxDictionary();
        data.put("uid", "test");
        data.put("phone_model", "testPhoneModel");
        data.put("deviceModel", "testDeviceModel");
        data.put("subscriber_id", "testSubscriberId");
        testMessage.setQueryBegin(data);

        testSession.setToCodec(ICodecResource.Codec.PCM16);
        testSession.setFromCodec(ICodecResource.Codec.PCM16);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
    }

    @Test
    public void handleBCPResourceTunnelMessage() {
        BCPResourceTunnelMessage testMessage = new BCPResourceTunnelMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPTranscodeMessage() {
        BCPTranscodeMessage testMessage = new BCPTranscodeMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPLogMessage() {
        BCPLogMessage testMessage = new BCPLogMessage((short) 20, "test".getBytes(), (short) 1);
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPMdsMessage() {
        BCPMdsMessage testMessage = new BCPMdsMessage((short) 20, "test".getBytes());
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }

    @Test
    public void handleBCPMdsMessageResponse() {
        BCPMdsMessageResponse testMessage = new BCPMdsMessageResponse((short) 20, "test".getBytes());
        BCPMessageHandler.handleMessage(testSession, testMessage, NMSPClientManager.getInstance());
        //Command not used yet
    }
}