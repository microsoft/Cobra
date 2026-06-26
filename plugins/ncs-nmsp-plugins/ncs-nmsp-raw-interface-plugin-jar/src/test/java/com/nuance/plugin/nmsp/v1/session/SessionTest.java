package com.nuance.plugin.nmsp.v1.session;

import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioContainerType;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioGenerationPolicy;
import com.nuance.plugin.nmsp.v1.nmspgw.AudioHandlingSpec;
import com.nuance.plugin.nmsp.v1.nmspgw.SameCodecMGWPolicy;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.Parameter;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import junit.framework.TestCase;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import io.netty.channel.Channel;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_SECRET;
import static com.nuance.plugin.nmsp.v1.PdxInterfaceConfig.Property.HASH_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionTest extends TestCase {

    @Test
    public void testAddTranscodeRequest() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        ISessionListener mockListener = mock(ISessionListener.class);
        session.addTranscodeRequest(1, mockListener);

        ISessionListener returnedListener = session.getTranscodeListener(1);
        assertEquals(mockListener, returnedListener);

        returnedListener = session.getTranscodeListener(1);
        assertNull(returnedListener);
    }

    @Test
    public void testSetGetBcpVersion() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        short actualValue = 123;
        session.setBcpVersion(actualValue);
        assertEquals(session.getBcpVersion(), actualValue);
    }

    @Test
    public void testSetGetBufferSize() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        int actualValue = 123;
        session.setBufferSize(actualValue);
        assertEquals(session.getBufferSize(), actualValue);
    }

    @Test
    public void testSetGetClient() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        IClientHandler actualValue = mock(IClientHandler.class);
        session.setClient(actualValue);
        assertEquals(session.getClient(), actualValue);
    }

    @Test
    public void testSetGetClientSessionListener() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        ISessionListener actualValue = mock(ISessionListener.class);
        session.setClientSessionListener(actualValue);
        assertEquals(session.getClientSessionListener(), actualValue);
    }

    @Test
    public void testSetGetCopVersion() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        short actualValue = 123;
        session.setCopVersion(actualValue);
        assertEquals(session.getCopVersion(), actualValue);
    }

    @Test
    public void testSetGetFromCodec() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        ICodecResource.Codec actualValue = ICodecResource.Codec.PCM16_16K;
        session.setFromCodec(actualValue);
        assertEquals(session.getFromCodec(), actualValue);
    }

    @Test
    public void testSetGetToCodec() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        ICodecResource.Codec actualValue = ICodecResource.Codec.PCM16_16K;
        session.setToCodec(actualValue);
        assertEquals(session.getToCodec(), actualValue);
    }

    @Test
    public void testRequiresGeneratedAudioTranscoding() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        session.setOutAudioHandlingSpec(new AudioHandlingSpec(AudioGenerationPolicy.preferMGW, SameCodecMGWPolicy.skipGW, AudioContainerType.unspecified));
        assertTrue(session.requiresGeneratedAudioTranscoding(ICodecResource.Codec.PCM16_16K, ICodecResource.Codec.PCM16_22K));
        assertFalse(session.requiresGeneratedAudioTranscoding(ICodecResource.Codec.PCM16_16K, ICodecResource.Codec.PCM16_16K));
        session.setOutAudioHandlingSpec(new AudioHandlingSpec(AudioGenerationPolicy.preferMGW, SameCodecMGWPolicy.throughGW, AudioContainerType.unspecified));
        assertTrue(session.requiresGeneratedAudioTranscoding(ICodecResource.Codec.PCM16_16K, ICodecResource.Codec.PCM16_16K));
    }

    @Test
    public void testSetGetMissedPingResponsesBeforeDisconnect() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        int actualValue = 123;
        session.setMissedPingResponsesBeforeDisconnect(actualValue);
        assertEquals(session.getMissedPingResponsesBeforeDisconnect(), actualValue);
    }

    @Test
    public void testSetGetNmaid() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "123";
        session.setNmaid(actualValue);
        assertEquals(session.getNmaid(), actualValue);
    }

    @Test
    public void testSetGetParent() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "123";
        session.setParent(actualValue);
        assertEquals(session.getParent(), actualValue);
    }

    @Test
    public void testSetGetPingIntervalSecs() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        float actualValue = 123;
        session.setPingIntervalSecs(actualValue);
        assertEquals(session.getPingIntervalSecs(), actualValue);

        session.setPingIntervalSecs(0);
        assertEquals(session.getPingIntervalSecs(), actualValue);

        session.setPingIntervalSecs(-1);
        assertEquals(session.getPingIntervalSecs(), actualValue);
    }

    @Test
    public void testPingRequestId() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        long actualValue = 123;
        session.setPingRequestId(actualValue);
        assertEquals(session.getPingRequestId(), actualValue);
    }

    @Test
    public void testSetGetClientAudioListener() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        IAudioListener actualValue = mock(IAudioListener.class);
        session.setClientAudioListener(actualValue);
        assertEquals(session.getClientAudioListener(), actualValue);
    }

    @Test
    public void testSetGetUserId() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String passedValue = "é";
        String expectedValue = "C3A9";
        session.setUserId(passedValue);
        assertEquals(expectedValue, session.getUserId());

        passedValue = "test user?";
        expectedValue = "test+user3F";
        session.setUserId(passedValue);
        assertEquals(expectedValue, session.getUserId());

        passedValue = "testuser";
        expectedValue = "testuser";
        session.setUserId(passedValue);
        assertEquals(expectedValue, session.getUserId());

        passedValue = "test%user";
        expectedValue = "test25user";
        session.setUserId(passedValue);
        assertEquals(expectedValue, session.getUserId());
    }


    @Test
    public void testSetGetHasOggContainers() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        boolean actualValue = true;
        session.setOggContainer(actualValue);
        assertEquals(session.hasOggContainer(), actualValue);
    }

    @Test
    public void testGetWebAPIOrchestrator() {
        WebAPIOrchestrator orchestrator = mock(WebAPIOrchestrator.class);
        Session session = new Session(mock(Channel.class), orchestrator);
        assertEquals(session.getWebAPIOrchestrator(), orchestrator);
    }

    @Test
    public void testGetResourceParameters() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        List<Parameter> resourceParameters = session.getResourceParameters();
        Parameter param = new Parameter("test-param", Parameter.Type.XML);
        resourceParameters.add(param);
        assertEquals(session.getResourceParameters().get(0), param);
    }

    @Test
    public void testSessionTimeoutSecs() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        assertNull(session.getPingRequestScheduledFuture());
        session.setPingIntervalSecs(42);
        session.start();
        ScheduledFuture future = session.getPingRequestScheduledFuture();
        assertNotNull(future);
        session.disconnectingClient();
        assertNull(session.getPingRequestScheduledFuture());
        assertTrue(future.isCancelled());
    }

    @Test
    public void testSetGetSessionTimeoutSecs() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        session.setSessionTimeoutSecs(42);
        assertEquals(session.getSessionTimeoutSecs(), 42);
    }

    @Test
    public void testGetUUidString() {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session session = new Session(mock(Channel.class), mockOrchestrator);
        String uuid = session.getSessionIdString();
        assertNotNull(uuid);
        assertFalse(uuid.isBlank());
    }

    @Test
    public void testSetGetDisconnectionReason() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "test";
        session.setDisconnectionReason(actualValue);
        assertEquals(session.getDisconnectionReason(), actualValue);
    }

    @Test
    public void testSetGetCommandName() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "test";
        session.setCommandName(actualValue);
        assertEquals(session.getCommandName(), actualValue);
    }

    @Test
    public void testSetGetStreamingStartTime() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        long actualValue = 42;
        session.setStreamingStartTime(actualValue);
        assertEquals(session.getStreamingStartTime(), actualValue);
    }

    @Test
    public void testSetGetDictationLanguage() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "test";
        session.setDictationLanguage(actualValue);
        assertEquals(session.getDictationLanguage(), actualValue);
    }

    @Test
    public void testSetIsPdxKeysAsciiEncoded() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        boolean actualValue = true;
        session.setPdxKeysAsciiEncoded(actualValue);
        assertEquals(session.isPdxKeysAsciiEncoded(), actualValue);
    }

    @Test
    public void testSetGetObfuscatedDeviceId() throws IOException {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        when(session.getWebAPIOrchestrator().getWebApiFeatures()).thenReturn(mock(IWebApiFeatures.class));
        CombinedConfiguration config = new CombinedConfiguration();
        when(session.getWebAPIOrchestrator().getWebApiFeatures().getConfigs()).thenReturn(config);
        HierarchicalConfiguration nmspConfig = new BaseHierarchicalConfiguration();
        nmspConfig.setProperty(HASH_SECRET.getKey(), "ZmFrZSBzYWx0");
        nmspConfig.setProperty(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString());
        config.addConfiguration(nmspConfig, "", PdxInterfaceConfig.Property.NCS_NMSP_RAW_SERVER.getKey());
        String actualValue = "test";
        session.setUserId(actualValue);
        assertEquals("b53be8f4-e515-30c3-b7b1-06c97f5826a4", session.getObfuscatedUserId());
    }

    @Test
    public void testSetGetClientNmaidKey() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        String actualValue = "test";
        session.setClientNmaidKey(actualValue);
        assertEquals(session.getClientNmaidKey(), actualValue);
    }

    @Test
    public void testSetGetNumTransactions() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        int actualValue = 12;
        session.setNumTransactions(actualValue);
        assertEquals(session.getNumTransactions(), actualValue);
    }

    @Test
    public void testSetGetCurrentTransactionId() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        short actualValue = 32;
        session.setCurrentTransactionId(actualValue);
        assertEquals(session.getCurrentTransactionId(), actualValue);
    }

    @Test
    public void testPutGetBcpMessagePdxDicts() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        Map map = new HashMap();
        Object testObject = new Object();
        map.put("test", testObject);
        session.putBcpMessagePdxDicts(map);
        assertEquals(session.getBcpMessagePdxDictCopy().get("test"), testObject);
    }

    @Test
    public void testGetSessionCreationTime() {
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        long sessionCreationTime = session.getSessionCreationTime();
        assertTrue(sessionCreationTime > 0);
    }
}
