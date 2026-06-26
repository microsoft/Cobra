package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NMSPMessageDecoderTest {

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
        WebAPIOrchestrator testOrchestrator = new WebAPIOrchestrator(testFeatures);
        testSession = new Session(mockChannel, testOrchestrator);
    }

    @Test
    public void decodeBCP() throws Exception {
        byte[] testBytes = "".getBytes();

        assertNull(NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes), Optional.of(2097152), testSession));

        //BCP Begin
        testBytes = Hex.decodeHex("0222250acd020000ddae1c41b33542f0b33c54d2ba39029202b8020000e08202b416086c6f636174696f6ec1213c2b34322e353034372c202d37312e313936313e202b2f2d20323235322e30306d160d70686f6e655f6e6574776f726bc10477696669160e646963746174696f6e5f74797065c109446963746174696f6e160b70686f6e655f6d6f64656cc1096950686f6e6520334716096c63645f7769647468c00101160c6465766963655f636f646563c001051606726567696f6ec1026e651607636f6d6d616e64160c4e4d44505f4153525f434d4416086669656c645f6964c108546f204669656c64160a6c63645f686569676874c00101160763617272696572c103415454160e7363726970745f76657273696f6ec103312e30160b6170706c69636174696f6e160378797a1603756964c10a3535353535353535353516056e6d616964c10378797a16166170706c69636174696f6e5f73657373696f6e5f6964c12438306563383539612d646536352d343062662d616138662d653638646238646331326164160870686f6e655f4f53c103342e30160c617564696f5f736f75726365c114537065616b6572416e644d6963726f70686f6e651615636c69656e745f68617264776172655f6d6f64656cc1096950686f6e6520334716136f726967696e616c5f73657373696f6e5f6964160016107574746572616e63655f6e756d626572c101351612646963746174696f6e5f6c616e6775616765c107656e672d555341160667656e646572c1046d616c6516086c616e6775616765c104656e757316066c6f63616c65c10663616e616461160b7064785f76657273696f6ec10131160e636c69656e745f76657273696f6ec1174a3253455f4f525f4a324d455f4f535f56455253494f4e160c70686f6e655f6e756d626572c10a3535353535353535353516106170706c69636174696f6e5f6e616d65c1174e6d73702053616d706c65204170706c69636174696f6e");
        assertTrue(((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString().contains("BCP_BEGIN"));

        //BCP Data
        testBytes = Hex.decodeHex("0222190abf000000ddae1c41b33542f0b33c54d2ba3902920200000000a60000000302e081a116046e616d65c10c524551554553545f494e464f160474797065c00105160464696374e07c1603656e64c001001617656e61626c655f6175746f5f70756e6374756174696f6ec00100160e62696e6172795f726573756c7473c00100160474657874c100161c656e61626c655f696e7465726d6564696174655f726573706f6e7365c0010016057374617274c00100160d6e6c736d6c5f726573756c7473c00100");
        assertTrue(((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString().startsWith("BCP_DATA protocolVersion: 34 transactionID: 2 rid: 0"));

        //BCP Data
        testBytes = Hex.decodeHex("0222190a46000000ddae1c41b33542f0b33c54d2ba39029202000000002d0000000302e02916046e616d65c10a415544494f5f494e464f16096275666665725f6964c00101160474797065c00101");
        assertTrue(((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString().startsWith("BCP_DATA protocolVersion: 34 transactionID: 2 rid: 0"));

        //BCP Invalid Version
        testBytes = Hex.decodeHex("0200190a46000000ddae1c41b33542f0b33c54d2ba39029202000000002d0000000302e02916046e616d65c10a415544494f5f494e464f16096275666665725f6964c00101160474797065c00101");
        try {
            NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession);
            fail("Expected CorruptedFrameException for invalid BCP version");
        } catch (io.netty.handler.codec.CorruptedFrameException e) {
            // Expected exception
        }
    }

    @Test
    public void decodeCOP() throws Exception {
        //COP Connect
        byte[] testBytes = Hex.decodeHex("03170001b2000000ae000000003c3f786d6c2076657273696f6e3d22312e30223f3e3c63633e3c733e3c2f733e3c743e363c2f743e3c623e32303039313032333c2f623e3c62733e3531323030303c2f62733e3c7473633e353c2f7473633e3c6673633e353c2f6673633e3c6e6d6169643e78797a3c2f6e6d6169643e3c7569643e353535353535353535353c2f7569643e3c6e6d737020703d2250696e675f496e74657276616c536563732220763d2230222f3e3c2f63633e");
        assertEquals("COP_CONNECT protocolVersion: 23 fromCodec: PCM16_16K toCodec: PCM16_16K nmaid: xyz", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //COP Confirm
        testBytes = Hex.decodeHex("03170601180000001400000038829c5c77081306f48e3d447ff32ee13bb23ba4");
        assertEquals("COP_CONFIRM protocolVersion: 23", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //COP Invalid version
        testBytes = Hex.decodeHex("03000601180000001400000038829c5c77081306f48e3d447ff32ee13bb23ba4");
        try {
            NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession);
            fail("Expected CorruptedFrameException for invalid COP version");
        } catch (io.netty.handler.codec.CorruptedFrameException e) {
            // Expected exception
        }
    }

    @Test
    public void decodeVAP() throws Exception {
        testSession.setFromCodec(ICodecResource.Codec.PCM16_16K);

        //VAP Connect
        byte[] testBytes = Hex.decodeHex("0112010106000000010000000500");
        assertEquals("VAP_RECORD_BEGIN protocolVersion: 18 audioID: 1 codec: PCM16_16K", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //VAP Data
        testBytes = Hex.decodeHex("011201024801000001000000400100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        assertEquals("VAP_RECORD protocolVersion: 18 audioID: 1 length: 1", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //VAP End
        testBytes = Hex.decodeHex("011200010400000001000000");
        assertEquals("VAP_RECORD_END protocolVersion: 18 audioID: 1", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //VAP Invalid version
        testBytes = Hex.decodeHex("010000010400000001000000");
        try {
            NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession);
            fail("Expected CorruptedFrameException for invalid VAP version");
        } catch (io.netty.handler.codec.CorruptedFrameException e) {
            // Expected exception
        }
    }

    @Test
    public void decodeHandShake() throws Exception {
        //Handshake
        byte[] testBytes = Hex.decodeHex("0f12010106000000010000000500");
        assertEquals("HANDSHAKEBYTE", ((NMSPMessage) NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession)).toString());

        //Unknown Protocol
        testBytes = Hex.decodeHex("aa12010106000000010000000500");
        try {
            NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession);
            fail("Expected CorruptedFrameException for unknown protocol");
        } catch (io.netty.handler.codec.CorruptedFrameException e) {
            // Expected exception
        }

        //Unknown bad length
        testBytes = Hex.decodeHex("aa120101060000000100000000000500");
        try {
            NMSPMessageDecoder.decode(Unpooled.wrappedBuffer(testBytes).order(ByteOrder.LITTLE_ENDIAN), Optional.of(2097152), testSession);
            fail("Expected CorruptedFrameException for unknown protocol with bad length");
        } catch (io.netty.handler.codec.CorruptedFrameException e) {
            // Expected exception
        }
    }
}