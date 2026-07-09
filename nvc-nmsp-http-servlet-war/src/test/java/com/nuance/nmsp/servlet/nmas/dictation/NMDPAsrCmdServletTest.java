package com.nuance.nmsp.servlet.nmas.dictation;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryError;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryRetry;
import com.nuance.ncs.nmsp.engine.pdx.PdxResponseListener;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.NMSPServlet;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.nmas.AsrContext;
import com.nuance.nmsp.servlet.nmas.audio.AudioStreamer;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NMDPAsrCmdServletTest
{
    String appId = "nina4";
    String appKey = "abc12345";

    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    NMDPAsrCmdServlet servlet;

    @Before
    public void setUp() throws ServletException
    {
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletResponse = new MockHttpServletResponse();
        servlet = new NMDPAsrCmdServlet();
        servlet.init();
    }

    @Test
    public void doGetReturnsUnauthorizedWithoutCredentials()
    {
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetReturnsNotAcceptableForUnknownCodec()
    {
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addParameter("appId", appId);
        mockHttpServletRequest.addParameter("appKey", appKey);
        mockHttpServletRequest.setContentType("audio/unknown-codec");
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostDelegatesToDoGet() throws Exception
    {
        // Without auth params doPost → doGet → SC_UNAUTHORIZED
        servlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetReturnsNotAcceptableForInvalidTopic()
    {
        mockHttpServletRequest.addParameter("appId", appId);
        mockHttpServletRequest.addParameter("appKey", appKey);
        mockHttpServletRequest.addParameter("codec", "wav");
        mockHttpServletRequest.addParameter("topic", "garbage-topic");
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetSuccessPathReachesSendAndFails()
    {
        // Valid auth + codec + topic → executeNmspAudioTransaction → sendNMASRequest
        // which fails connecting to nmspGateway (defaults to 127.0.0.1:8911, closed).
        // Outcome: SC_INTERNAL_SERVER_ERROR via NMSPServlet.executeNmspAudioTransaction catch.
        mockHttpServletRequest.addParameter("appId", appId);
        mockHttpServletRequest.addParameter("appKey", appKey);
        mockHttpServletRequest.addParameter("codec", "wav");
        mockHttpServletRequest.addParameter("topic", "Dictation");
        mockHttpServletRequest.addHeader("X-Dictation-AudioSource", "microphone");
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mockHttpServletResponse.getStatus());
    }

    // --- Direct sendNMASRequest coverage ----------------------------------

    private static int findClosedPort() throws IOException
    {
        try (ServerSocket s = new ServerSocket(0))
        {
            return s.getLocalPort();
        }
    }

    private static void setShortField(NMSPServlet s, String name, short value) throws Exception
    {
        Field f = NMSPServlet.class.getDeclaredField(name);
        f.setAccessible(true);
        Field mods = Field.class.getDeclaredField("modifiers");
        mods.setAccessible(true);
        int orig = f.getModifiers();
        mods.setInt(f, orig & ~Modifier.FINAL);
        f.setShort(s, value);
        mods.setInt(f, orig);
    }

    private static void setBoolField(NMSPServlet s, String name, boolean value) throws Exception
    {
        Field f = NMSPServlet.class.getDeclaredField(name);
        f.setAccessible(true);
        Field mods = Field.class.getDeclaredField("modifiers");
        mods.setAccessible(true);
        int orig = f.getModifiers();
        mods.setInt(f, orig & ~Modifier.FINAL);
        f.setBoolean(s, value);
        mods.setInt(f, orig);
    }

    private void runSendUntilConnectFails(NMDPAsrCmdServlet target, ServletCodecWrapper codec) throws Exception
    {
        setShortField(target, "nmspGatewayPort", (short) findClosedPort());
        setBoolField(target, "nmspSslEnabled", false);
        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, "uid-1");
        try
        {
            AsrContext ctx = new AsrContext();
            ctx.setAudioSource("mic"); // exercise the optional audio-source branch
            target.sendNMASRequest(mockHttpServletRequest, codec, ctx, transaction, "en_US", "Dictation");
            fail("Expected IOException when PDX connect fails");
        }
        catch (IOException expected)
        {
            // Either "Failed to connect" or transport setup failure
        }
        finally
        {
            transaction.cleanup();
        }
    }

    @Test
    public void sendNMASRequestWithSpeexCodecFailsOnConnect() throws Exception
    {
        // Use a real Speex codec wrapper if available; otherwise fall back to WAV.
        ServletCodecWrapper speex = null;
        for (Field f : ServletCodecWrapper.class.getDeclaredFields())
        {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && ServletCodecWrapper.class.isAssignableFrom(f.getType())
                    && f.getName().startsWith("SPEEX"))
            {
                Object v = f.get(null);
                if (v != null) { speex = (ServletCodecWrapper) v; break; }
            }
        }
        if (speex == null)
        {
            speex = ServletCodecWrapper.WAV;
        }
        NMDPAsrCmdServlet target = new NMDPAsrCmdServlet();
        assertNotNull(speex);
        runSendUntilConnectFails(target, speex);
    }

    @Test
    public void sendNMASRequestWithPcmCodecFailsOnConnect() throws Exception
    {
        NMDPAsrCmdServlet target = new NMDPAsrCmdServlet();
        runSendUntilConnectFails(target, ServletCodecWrapper.PCM_16_16K);
    }

    // --- Stub PdxClient tests covering full sendNMASRequest body ---------

    static class StubPdxClient extends PdxClient
    {
        boolean connectReturn = true;
        boolean awaitReturn = true;
        final List<String> calls = new ArrayList<>();

        StubPdxClient()
        {
            super("localhost", 0, "app", new byte[]{1, 2, 3, 4}, "user",
                    ICodecResource.Codec.PCM16, ICodecResource.Codec.PCM16,
                    false, false, 1000L,
                    new PdxResponseListener() {
                        @Override public void onConnected(String s) {}
                        @Override public void onConnectionFailed(short r) {}
                        @Override public void onDisconnected(short r) {}
                        @Override public void onQueryResult(PdxQueryResult r) {}
                        @Override public void onQueryError(PdxQueryError e) {}
                        @Override public void onQueryRetry(PdxQueryRetry r) {}
                        @Override public void onAudioReceived(byte[] b) {}
                        @Override public void onError(Throwable c) {}
                    });
        }

        @Override public boolean connect() { calls.add("connect"); return connectReturn; }
        @Override public boolean awaitConnected(long timeoutMs) { calls.add("await"); return awaitReturn; }
        @Override public void loadResource(String appName) { calls.add("loadResource:" + appName); }
        @Override public void beginCommand(String commandName, String appName, String appVersion,
                String dictationLanguage, String carrier, String phoneModel, NmspDictionary optionalKeys) {
            calls.add("beginCommand:" + commandName);
        }
        @Override public boolean sendDictParam(String paramName, NmspDictionary d) {
            calls.add("dict:" + paramName); return true;
        }
        @Override public PdxAudioSink createAudioSink(String paramName) {
            calls.add("audioSink:" + paramName);
            return new PdxAudioSink() {
                @Override public void addAudioBuf(byte[] buf, int offset, int length, boolean isLastBuf) {}
            };
        }
        @Override public void endCommand() { calls.add("endCommand"); }
        @Override public void freeResource() { calls.add("freeResource"); }
        @Override public void shutdown() { calls.add("shutdown"); }
    }

    /** Subclass that returns a controllable stub client. */
    static class StubbedAsrServlet extends NMDPAsrCmdServlet
    {
        final StubPdxClient stub = new StubPdxClient();
        @Override
        protected PdxClient createPdxClient(String host, int port,
                String appId, byte[] appKey, String userId,
                ICodecResource.Codec inCodec, ICodecResource.Codec outCodec,
                boolean sslEnabled, boolean sslSelfSigned, long timeoutMs, PdxResponseListener listener)
        {
            return stub;
        }
    }

    @Test
    public void sendNMASRequestFullPathWithPcmCodec() throws Exception
    {
        StubbedAsrServlet target = new StubbedAsrServlet();
        target.init();
        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, "uid-2");
        AsrContext ctx = new AsrContext();
        AudioStreamer streamer = target.sendNMASRequest(mockHttpServletRequest,
                ServletCodecWrapper.PCM_16_16K, ctx, transaction, "en_US", "Dictation");
        assertNotNull(streamer);
        assertTrue(target.stub.calls.contains("connect"));
        assertTrue(target.stub.calls.contains("await"));
        assertTrue(target.stub.calls.toString(), target.stub.calls.toString().contains("loadResource:NMASSampleApp"));
        assertTrue(target.stub.calls.toString(), target.stub.calls.toString().contains("beginCommand:"));
        assertTrue(target.stub.calls.toString(), target.stub.calls.toString().contains("endCommand"));
        // Audio streamer is started; transaction.client is the stub
        assertEquals(target.stub, transaction.getClient());
    }

    @Test
    public void sendNMASRequestFullPathWithAudioSourceOverridesLocale() throws Exception
    {
        StubbedAsrServlet target = new StubbedAsrServlet();
        target.init();
        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, null);  // covers the "1234" fallback
        AsrContext ctx = new AsrContext();
        ctx.setAudioSource("microphone");
        AudioStreamer streamer = target.sendNMASRequest(mockHttpServletRequest,
                ServletCodecWrapper.PCM_16_16K, ctx, transaction, "fr_FR", "WebSearch");
        assertNotNull(streamer);
    }

    @Test
    public void sendNMASRequestConnectFailureThrows() throws Exception
    {
        StubbedAsrServlet target = new StubbedAsrServlet();
        target.init();
        target.stub.connectReturn = false;
        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, "uid-3");
        try
        {
            target.sendNMASRequest(mockHttpServletRequest, ServletCodecWrapper.PCM_16_16K,
                    new AsrContext(), transaction, "en_US", "Dictation");
            fail("Expected IOException");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("Failed to connect"));
        }
    }

    @Test
    public void sendNMASRequestAwaitTimeoutThrows() throws Exception
    {
        StubbedAsrServlet target = new StubbedAsrServlet();
        target.init();
        target.stub.awaitReturn = false;
        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, "uid-4");
        try
        {
            target.sendNMASRequest(mockHttpServletRequest, ServletCodecWrapper.PCM_16_16K,
                    new AsrContext(), transaction, "en_US", "Dictation");
            fail("Expected IOException");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("Timed out"));
        }
    }
}
