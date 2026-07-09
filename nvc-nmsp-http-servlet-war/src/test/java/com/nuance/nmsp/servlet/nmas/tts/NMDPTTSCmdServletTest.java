package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.NMSPServlet;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.nmas.TransactionFactory;
import com.nuance.nmsp.servlet.util.HttpUtil;
import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NMDPTTSCmdServletTest
{
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;
    private NMDPTTSCmdServlet servlet;

    @Before
    public void setUp() throws ServletException
    {
        mockHttpServletRequest = new MockHttpServletRequest();
        // MockHttpServletRequest defaults to "" for getMethod(); explicit GET avoids
        // MethodType.valueOf throwing inside TTSContent.
        mockHttpServletRequest.setMethod("GET");
        mockHttpServletResponse = new MockHttpServletResponse();
        servlet = new NMDPTTSCmdServlet();
        servlet.init();
    }

    private void addValidAuth()
    {
        mockHttpServletRequest.addParameter("appId", "appA");
        mockHttpServletRequest.addParameter("appKey", "abcd"); // even length
    }

    /** Auth + a codec that resolves through codecToMimeType (DEFAULT does not). */
    private void addValidAuthAndCodec()
    {
        addValidAuth();
        mockHttpServletRequest.addParameter("codec", "wav");
    }

    /**
     * Replace the protected-final transactionFactory with a no-wait variant so
     * waitForResponse() returns immediately instead of blocking on Condition.await().
     */
    private static void installNoWaitFactory(NMSPServlet servlet) throws Exception
    {
        Field f = NMSPServlet.class.getDeclaredField("transactionFactory");
        f.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        int original = f.getModifiers();
        modifiers.setInt(f, original & ~Modifier.FINAL);
        f.set(servlet, new NoWaitTransactionFactory());
        modifiers.setInt(f, original);
    }

    @Test
    public void doGetReturnsUnauthorizedWithoutCredentials()
    {
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostDelegatesToDoGet()
    {
        servlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetWithOddAppKeyLengthIsUnauthorized()
    {
        mockHttpServletRequest.addParameter("appId", "appA");
        mockHttpServletRequest.addParameter("appKey", "abc"); // odd length → reject
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    public void initRunsSuccessfully() throws ServletException
    {
        NMDPTTSCmdServlet fresh = new NMDPTTSCmdServlet();
        fresh.init();
    }

    @Test
    public void destroyCleansUpInheritedState()
    {
        servlet.destroy();
    }

    @Test
    public void doGetWithBadCodecParamReturnsNotAcceptable()
    {
        addValidAuth();
        mockHttpServletRequest.addParameter("codec", "garbage_not_a_codec");
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostWithUnparseableAcceptReturnsNotAcceptable()
    {
        addValidAuth();
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addHeader(HttpUtil.ACCEPT, "audio/");
        servlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetWithoutTtsParamsReturnsBadRequest()
    {
        addValidAuthAndCodec();
        // No text, uri, or ssml param → TTSContent throws IllegalArgumentException → 400.
        servlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostWithoutContentTypeReturnsUnsupportedMediaType()
    {
        addValidAuthAndCodec();
        mockHttpServletRequest.setMethod("POST");
        servlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostWithInvalidContentTypeReturnsUnsupportedMediaType()
    {
        addValidAuthAndCodec();
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.setContentType("application/octet-stream"); // not text/url/ssml
        servlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetWithValidTextRoutesThroughSendError() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);
        test.throwFromSend = new IOException("simulated gateway failure");

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello world");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mockHttpServletResponse.getStatus());
        assertTrue("sendNMASRequest should have been called", test.sendCalled);
    }

    @Test
    public void doGetWithTransactionNotCompletedSendsCustomErrorId() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);
        test.simulate = (tx) -> {
            tx.setError("custom error");
            tx.setErrorID(503);
            tx.setSessionId("sid-abc");
            tx.setRequestCompleted(false);
        };

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello world");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);
        // !isRequestCompleted && errorId > 0 → 503.
        assertEquals(503, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetWithTransactionNotCompletedAndNoErrorIdSends500() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);
        test.simulate = (tx) -> {
            tx.setError("some error without id");
            tx.setRequestCompleted(false);
        };

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doGetWithCompletedTransactionSetsHeadersAndNoErrorStatus() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);
        // No simulate → default behavior: leave requestCompleted at constructor default (true).

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello world");
        mockHttpServletRequest.addHeader(HttpUtil.NUANCE_CONTEXT_HEADER, "ctx-123");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);

        assertEquals(HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
        assertNotNull(mockHttpServletResponse.getHeader("Content-Type"));
        assertNotNull(mockHttpServletResponse.getHeader("Content-Disposition"));
        assertEquals("ctx-123", mockHttpServletResponse.getHeader(HttpUtil.NUANCE_CONTEXT_HEADER));
    }

    @Test
    public void doGetGeneratesContextWhenHeaderAbsent() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);
        String generated = mockHttpServletResponse.getHeader(HttpUtil.NUANCE_CONTEXT_HEADER);
        assertNotNull(generated);
        // A UUID is 36 chars (8-4-4-4-12 + 4 dashes).
        assertEquals(36, generated.length());
    }

    @Test
    public void doGetCoversVoiceAndGenreAndIdLogging() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);

        addValidAuthAndCodec();
        mockHttpServletRequest.addParameter("text", "hello");
        mockHttpServletRequest.addParameter("voice", "Samantha");
        mockHttpServletRequest.addParameter("id", "req-42");
        mockHttpServletRequest.addHeader("X-TTS-Genre", "news");
        test.doGet(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
    }

    @Test
    public void doPostWithPlainTextContentSucceeds() throws Exception
    {
        TestableServlet test = new TestableServlet();
        test.init();
        installNoWaitFactory(test);

        addValidAuth();
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addHeader(HttpUtil.ACCEPT, "audio/x-wav");
        mockHttpServletRequest.setContentType("text/plain");
        mockHttpServletRequest.setContent("synthesize me".getBytes());
        test.doPost(mockHttpServletRequest, mockHttpServletResponse);
        assertEquals(HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
    }

    /** Test-only subclass that lets us inject behavior for sendNMASRequest. */
    private static class TestableServlet extends NMDPTTSCmdServlet
    {
        @FunctionalInterface
        interface TxSim { void apply(Transaction tx); }

        TxSim simulate;
        IOException throwFromSend;
        boolean sendCalled;

        @Override
        public void sendNMASRequest(ServletCodecWrapper codec, HttpServletResponse response, String ttsLanguage,
                TTSContent ttsContent, Transaction transaction, String ttsGenre) throws IOException
        {
            sendCalled = true;
            if (throwFromSend != null)
            {
                throw throwFromSend;
            }
            if (simulate != null)
            {
                simulate.apply(transaction);
            }
        }
    }

    /** Issues Transactions whose waitForResponse() is a no-op (avoids Condition.await() deadlock). */
    private static class NoWaitTransactionFactory extends TransactionFactory
    {
        @Override
        public Transaction newTransaction(AuthorizationInfo authorization, String uniqueId)
        {
            return new Transaction(authorization, uniqueId)
            {
                @Override
                public void waitForResponse()
                {
                    // no-op
                }
            };
        }
    }

    /**
     * Allocates a port nothing is listening on so PdxClient.connect() fails fast
     * (TCP RST) instead of waiting the full 10-second connect timeout.
     */
    private static int findClosedPort() throws IOException
    {
        try (java.net.ServerSocket s = new java.net.ServerSocket(0))
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

    private TTSContent buildTextTtsContent(String voice) throws IOException
    {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setMethod("GET");
        r.addParameter("text", "hello");
        if (voice != null)
        {
            r.addParameter("voice", voice);
        }
        return new TTSContent(r);
    }

    private void runSendUntilConnectFails(ServletCodecWrapper codec, String voice, String genre) throws Exception
    {
        NMDPTTSCmdServlet servlet = new NMDPTTSCmdServlet();
        setShortField(servlet, "nmspGatewayPort", (short) findClosedPort());
        setBoolField(servlet, "nmspSslEnabled", false);

        AuthorizationInfo auth = new AuthorizationInfo("appA", "abcd");
        Transaction transaction = new Transaction(auth, "uid-1");

        try
        {
            servlet.sendNMASRequest(codec, mockHttpServletResponse, "enus",
                    buildTextTtsContent(voice), transaction, genre);
            org.junit.Assert.fail("Expected IOException when PDX connect fails");
        }
        catch (IOException expected)
        {
            // Either "Failed to connect" or transport setup failure; both are fine.
        }
        finally
        {
            transaction.cleanup();
        }
    }

    @Test
    public void sendNMASRequestWavCodecMapsAndFailsOnConnect() throws Exception
    {
        runSendUntilConnectFails(ServletCodecWrapper.WAV, null, null);
    }

    @Test
    public void sendNMASRequestSpeex8kCodecMapsAndFailsOnConnect() throws Exception
    {
        runSendUntilConnectFails(ServletCodecWrapper.SPEEX_8K, null, "news");
    }

    @Test
    public void sendNMASRequestSpeex16kCodecMapsAndFailsOnConnect() throws Exception
    {
        runSendUntilConnectFails(ServletCodecWrapper.SPEEX_16K, "Samantha", null);
    }

    @Test
    public void sendNMASRequestPcmCodecPassthroughFailsOnConnect() throws Exception
    {
        // No remapping for PCM — covers the default fall-through after the if/else if chain.
        runSendUntilConnectFails(ServletCodecWrapper.PCM_16_8K, "Samantha", "news");
    }
}
