package com.nuance.plugin.nmsp.v1.nmspgw.client;

import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.InvalidMessageException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static com.nuance.plugin.nmsp.v1.session.Session.STATE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NMSPClientManagerTest {
    NMSPClientManager testClientManager;
    CombinedConfiguration mockConfig;
    IWebApiFeatures testFeatures;
    Session testSession;
    Channel mockChannel;

    @Before
    public void initTest() throws IOException {
        NMSPClientManager.init();
        testClientManager = NMSPClientManager.getInstance();

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        config.addProperty(PdxInterfaceConfig.Property.SERVER_PORT.getKey(), false);
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
            public void close() {
            }

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
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
    }

    @Test
    public void testConnection() {
        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);
        mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);

        testClientManager.connect(testSession);
        assertEquals(STATE.CHALLENGING, testSession.getState());
    }

    @Test
    public void connected() {
        testClientManager.connected(testSession);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testSession.setPingIntervalSecs(1);
        testClientManager.connected(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(any());
    }

    @Test
    public void sendDisconnect() {
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.connect(testSession);

        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.SESSION_TIMEOUT);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 1", argument.getValue().toString());

        //we only need to validate this once.
        assertEquals(STATE.DISCONNECTING, testSession.getState());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.PING_TIMEOUT);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 4", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.SERVER_SHUTDOWN);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 2", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.CRITICAL_ERROR);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 3", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.MAX_AUDIO_REACHED);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 7", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendDisconnect(testSession, IClientHandler.DisconnectReason.AUTHENTICATION_DATA);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_DISCONNECT protocolVersion: 0 reason: 9", argument.getValue().toString());
    }

    @Test
    public void connectFailed() {
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.connect(testSession);

        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.connectFailed(testSession, IClientHandler.ConnectFailureReason.FORBIDDEN, "test");
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.connectFailed(testSession, IClientHandler.ConnectFailureReason.PARAMETERERROR, "test");
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void confirm() throws DecoderException {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockConfig.getString("nmaidKey")).thenReturn("8911");

        try {
            testClientManager.confirm(testSession, "test".getBytes());
            fail("Session is in wrong state. Test should have failed here");
        } catch (InvalidMessageException e) {
            //success
        }

        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.connect(testSession);

        when(mockConfig.getString("nmaidKey")).thenReturn("8911");

        testClientManager.confirm(testSession, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());

        byte[] AppKey = Hex.decodeHex("a94afacbefad23e035650a8260f4b857919e8047".toCharArray());
        when(mockConfig.getString("nmaidKey")).thenReturn("1234567890");
        testClientManager.confirm(testSession, AppKey);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void sendPingRequest() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.connect(testSession);

        testClientManager.sendPingRequest(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_PING_REQUEST protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void pingResponse() {
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.pingResponse(testSession, 1, 1);

        testSession.setState(STATE.CONNECTED);

        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.pingResponse(testSession, 1, 1);
        assertTrue(testSession.getRoundTripTime() > 0);
    }

    @Test
    public void sendEvent() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.CLIENT_PING_TIMEOUT, "test".getBytes(), true);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 2", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.CLIENT_IDLE_TIMEOUT, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 3", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.NSS_DISCONNECT, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 4", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.NSS_ERROR, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 5", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.INTERNAL_ERROR, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 6", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.APPSERVER_DISCONNECT, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 7", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        testClientManager.sendEvent(testSession, (short) 1, IClientHandler.EventCode.APPSERVER_ERROR, "test".getBytes(), false);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 eventCode: 8", argument.getValue().toString());
    }

    @Test
    public void sendData() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.sendData(testSession, (short) 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_DATA protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 rid: 1 payload: test", argument.getValue().toString());
    }

    @Test
    public void logData() {
        testClientManager.logData(testSession, (short) 1, 1, "test".getBytes());
        //I don't know what to valdate here.
    }

    @Test
    public void logResponse() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.logResponse(testSession, (short) 1, 1, (short) 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_LOG_RESPONSE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 status code: 1", argument.getValue().toString());
    }

    @Test
    public void close() {
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.close(testSession);
        verify(mockChannel, atLeastOnce()).close();
    }

    @Test
    public void resourceLoaded() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.resourceLoaded(testSession, (short) 1, 1, 1, 1, "testAddr");
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_LOAD_RESOURCE_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 resourceId:1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void resourceDisconnected() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.resourceDisconnected(testSession, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_EVENT protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 0 eventCode: 0", argument.getValue().toString());
    }

    @Test
    public void response() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.response(testSession, (short) 1, 1, 1, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_RESPONSE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 statusCode: 1 requestState: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void generateAudioComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.generateAudioComplete(testSession, (short) 1, 1, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_GENERATE_AUDIO_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 audioId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void retrieveArtifactsComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.retrieveArtifactsComplete(testSession, (short) 1, 1, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_RETRIEVE_ARTIFACTS_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 audioId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void startOfSpeech() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.startOfSpeech(testSession, (short) 1, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_START_OF_SPEECH protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 requestState: 1", argument.getValue().toString());
    }

    @Test
    public void endOfSpeech() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.endOfSpeech(testSession, (short) 1, 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_END_OF_SPEECH protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 requestState: 1 test", argument.getValue().toString());
    }

    @Test
    public void recognitionComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.recognitionComplete(testSession, (short) 1, 1, 1, "test".getBytes(), (short) 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_RECOGNITION_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void recognitionIntermediateResults() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.recognitionIntermediateResults(testSession, (short) 1, 1, 1, "test".getBytes(), (short) 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_RECOGNITION_INTERMRESULTS protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void interpretationComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.interpretationComplete(testSession, (short) 1, 1, 1, "test".getBytes(), (short) 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_INTERPRETATION_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void setParamsComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.setParamsComplete(testSession, (short) 1, 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_SET_PARAMS_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 statusCode: 1", argument.getValue().toString());
    }

    @Test
    public void getParamsComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.getParamsComplete(testSession, (short) 1, 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_GET_PARAMS_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 statusCode: 1", argument.getValue().toString());
    }

    @Test
    public void cancelResponse() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.cancelResponse(testSession, (short) 1, 1, 1, 1);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_CANCEL_RESPONSE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 statusCode: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void resourceTunnel() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.resourceTunnel(testSession, (short) 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_RESOURCE_TUNNEL protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 resid: 1", argument.getValue().toString());
    }

    @Test
    public void transcodeComplete() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.transcodeComplete(testSession, (short) 1, 1, 1, 1, "test".getBytes());
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("BCP_TRANSCODE_COMPLETE protocolVersion: " + BCPMessage.BCP_MAXIMUM_VERSION + " transactionID: 1 requestId: 1 audioId: 1 completionCause: 1", argument.getValue().toString());
    }

    @Test
    public void audioReceivedStart() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.audioReceivedStart(testSession, 1, 1L, ICodecResource.Codec.PCM16);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("VAP_PLAY_BEGIN protocolVersion: " + VAPMessage.VAP_MINIMUM_VERSION + " audioID: 1 codec: PCM16", argument.getValue().toString());
    }

    @Test
    public void audioReceived() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.audioReceived(testSession, 1L, 1L, ICodecResource.Codec.PCM16, "test".getBytes(), 1, true, true);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("VAP_PLAY_END protocolVersion: " + VAPMessage.VAP_MINIMUM_VERSION + " audioID: 1", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        reset(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.audioReceived(testSession, 1L, 1L, ICodecResource.Codec.PCM16, "test".getBytes(), 1, false, true);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("VAP_PLAY protocolVersion: " + VAPMessage.VAP_MINIMUM_VERSION + " audioID: 1 audio length: 4 bytes", argument.getValue().toString());

        argument = ArgumentCaptor.forClass(NMSPMessage.class);
        reset(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testClientManager.audioReceived(testSession, 1L, 1L, ICodecResource.Codec.PCM16, null, 1, false, true);
        verify(mockChannel, atMost(0)).writeAndFlush(argument.capture());
    }

    @Test
    public void subAudioReceived() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.subAudioReceived(testSession, 1L, 1L, ICodecResource.Codec.PCM16, "test".getBytes(), 1, true, true);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("VAP_PLAY_END protocolVersion: " + VAPMessage.VAP_MINIMUM_VERSION + " audioID: 1", argument.getValue().toString());
    }

    @Test
    public void authenticationRejected() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.connect(testSession);
        testClientManager.authenticationRejected(testSession, "test");
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECTED protocolVersion: 0", argument.getValue().toString());

        reset(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testSession.setState(STATE.AUTHENTICATING);
        testClientManager.authenticationRejected(testSession, "test");
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECT_FAILED protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void authenticationChallenged() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.connect(testSession);
        testClientManager.authenticationChallenged(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECTED protocolVersion: 0", argument.getValue().toString());

        reset(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testSession.setState(STATE.AUTHENTICATING);
        testClientManager.authenticationChallenged(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECTED protocolVersion: 0", argument.getValue().toString());
    }

    @Test
    public void authenticationAccepted() {
        ArgumentCaptor<NMSPMessage> argument = ArgumentCaptor.forClass(NMSPMessage.class);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);

        testClientManager.connect(testSession);
        testClientManager.authenticationAccepted(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECTED protocolVersion: 0", argument.getValue().toString());

        reset(mockChannel);
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);
        when(mockChannel.isOpen()).thenReturn(true);
        testSession.setState(STATE.AUTHENTICATING);
        testClientManager.authenticationAccepted(testSession);
        verify(mockChannel, atLeastOnce()).writeAndFlush(argument.capture());
        assertEquals("COP_CONNECTED protocolVersion: 0", argument.getValue().toString());
    }
}