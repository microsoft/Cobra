package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.deserializers;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class VAPRecordDeserializerTest {

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
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
    }

    @Test
    public void deserialize() throws CorruptedFrameException {
        testSession.setFromCodec(ICodecResource.Codec.OPUS_16K_MONO_CBR_CPLX10);

        byte[] buffer = new byte[3000];
        buffer[3] = 1;
        buffer[6] = 10;
        buffer[7] = 92;
        buffer[8] = 10;
        buffer[19] = -125;
        buffer[20] = 10;
        ByteBuf testData = Unpooled.wrappedBuffer(buffer);
        VAPRecordDeserializer.deserialize((short) 1, testData, testSession);

        testSession.setFromCodec(ICodecResource.Codec.PCM16_16K);

        testData = Unpooled.wrappedBuffer(buffer);
        VAPRecordDeserializer.deserialize((short) 1, testData, testSession);
    }
}