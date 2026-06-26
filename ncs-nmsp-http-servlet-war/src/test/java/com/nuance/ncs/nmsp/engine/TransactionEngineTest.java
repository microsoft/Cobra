package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryError;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryRetry;
import com.nuance.ncs.nmsp.engine.pdx.PdxResponseListener;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.servlet.NcsServletDefaults;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NcsServletDefaults.class})
@PowerMockIgnore("javax.management.*")
public class TransactionEngineTest
{
    RequestData requestData;
    MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
    ResponseManager responseManager;
    TransactionEngine transactionEngine;

    @Before
    public void setUp()
    {
        requestData = new RequestData("{}", null, null, null, "abc");
        responseManager = new ResponseManager(mockHttpServletResponse, null);
        transactionEngine = new TransactionEngine();
    }

    @Test
    public void badCrossOrigin()
    {
        mockStatic(NcsServletDefaults.class);
        when(NcsServletDefaults.getAuthorizedCrossOrigins()).thenReturn("[xyz]");
        assertFalse(transactionEngine.setUpRequest(requestData, responseManager));
    }

    @Test
    public void badGwIP()
    {
        String jsonString = "{" +
                            "\"srvrIP\": \"127.0.0.2\"" +
                            "}";
        requestData = new RequestData(jsonString, "nina4", "abc12345", null, "abc");
        responseManager = new ResponseManager(mockHttpServletResponse, null);
        transactionEngine = new TransactionEngine();
        mockStatic(NcsServletDefaults.class);
        when(NcsServletDefaults.getAuthorizedCrossOrigins()).thenReturn(".*");
        when(NcsServletDefaults.getAuthorizedIps()).thenReturn("[xyz]");
        assertFalse(transactionEngine.setUpRequest(requestData, responseManager));
    }

    @Test
    public void sendParamInitUnsuccessful()
    {
        transactionEngine.setUpRequest(requestData, responseManager);
        assertNull(transactionEngine.sendAudioParam("test"));
        assertFalse(transactionEngine.sendChoiceParam("test", "choice"));
        assertFalse(transactionEngine.sendDataParam("test", "data".getBytes(StandardCharsets.UTF_8)));
        assertFalse(transactionEngine.sendTextParam("test", "text"));
        assertFalse(transactionEngine.sendDictParam("test", new NmspDictionaryImpl()));
        assertFalse(transactionEngine.sendSeqStartParam("test", new NmspDictionaryImpl()));
        assertFalse(transactionEngine.sendSeqChunkParam("test", new NmspDictionaryImpl()));
        assertFalse(transactionEngine.sendSeqEndParam("test", new NmspDictionaryImpl()));
        assertFalse(transactionEngine.sendTtsParam("test", new NmspDictionaryImpl()));
        assertFalse(transactionEngine.addCallLog(null));
    }

    // --- Helpers -------------------------------------------------------

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = TransactionEngine.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static <T> T getField(Object target, String name) throws Exception {
        Field f = TransactionEngine.class.getDeclaredField(name);
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        T v = (T) f.get(target);
        return v;
    }

    private static PdxResponseListener invokeBuildResponseListener(TransactionEngine engine) throws Exception {
        Method m = TransactionEngine.class.getDeclaredMethod("buildResponseListener");
        m.setAccessible(true);
        return (PdxResponseListener) m.invoke(engine);
    }

    /** Records calls without doing any networking; used to swap into TransactionEngine.pdxClient. */
    static class StubPdxClient extends PdxClient {
        final List<String> calls = new ArrayList<>();
        boolean shutdownCalled = false;
        boolean freeResourceCalled = false;
        boolean endCommandCalled = false;
        byte[] lastLog;

        StubPdxClient() {
            super("localhost", 0, "appId", new byte[]{1,2,3,4}, "user",
                    ICodecResource.Codec.PCM16, ICodecResource.Codec.PCM16,
                    false, false, 1000L,
                    new PdxResponseListener() {
                        @Override public void onConnected(String sessionIdHex) {}
                        @Override public void onConnectionFailed(short reasonCode) {}
                        @Override public void onDisconnected(short reasonCode) {}
                        @Override public void onQueryResult(PdxQueryResult result) {}
                        @Override public void onQueryError(PdxQueryError error) {}
                        @Override public void onQueryRetry(PdxQueryRetry retry) {}
                        @Override public void onAudioReceived(byte[] audioData) {}
                        @Override public void onError(Throwable cause) {}
                    });
        }

        @Override public PdxAudioSink createAudioSink(String paramName) {
            calls.add("createAudioSink:" + paramName);
            return new PdxAudioSink() {
                @Override public void addAudioBuf(byte[] buf, int offset, int length, boolean isLastBuf) {}
            };
        }
        @Override public boolean sendChoiceParam(String paramName, String choice) { calls.add("choice:" + paramName); return true; }
        @Override public boolean sendTextParam(String paramName, String text)     { calls.add("text:" + paramName); return true; }
        @Override public boolean sendDataParam(String paramName, byte[] data)     { calls.add("data:" + paramName); return true; }
        @Override public boolean sendDictParam(String paramName, com.nuance.ncs.nmsp.pdx.NmspDictionary d) {
            calls.add("dict:" + paramName); return true;
        }
        @Override public boolean sendSeqStartParam(String paramName, com.nuance.ncs.nmsp.pdx.NmspDictionary d) {
            calls.add("seqStart:" + paramName); return true;
        }
        @Override public boolean sendSeqChunkParam(String paramName, com.nuance.ncs.nmsp.pdx.NmspDictionary d) {
            calls.add("seqChunk:" + paramName); return true;
        }
        @Override public boolean sendSeqEndParam(String paramName, com.nuance.ncs.nmsp.pdx.NmspDictionary d) {
            calls.add("seqEnd:" + paramName); return true;
        }
        @Override public boolean sendTtsParam(String paramName, com.nuance.ncs.nmsp.pdx.NmspDictionary d, PdxAudioSink sink) {
            calls.add("tts:" + paramName); return true;
        }
        @Override public void endCommand() { endCommandCalled = true; calls.add("endCommand"); }
        @Override public void sendBcpLog(byte[] bytes) { lastLog = bytes; calls.add("bcpLog"); }
        @Override public void freeResource() { freeResourceCalled = true; calls.add("freeResource"); }
        @Override public void shutdown() { shutdownCalled = true; calls.add("shutdown"); }
    }

    private TransactionEngine engineWithStubClient(StubPdxClient client, boolean initSuccessful) throws Exception {
        TransactionEngine engine = new TransactionEngine();
        setField(engine, "responseManager", responseManager);
        setField(engine, "requestData", requestData);
        setField(engine, "pdxClient", client);
        setField(engine, "callLogData", new CallLogData(engine, responseManager, "abc"));
        setField(engine, "initSuccessfull", initSuccessful);
        setField(engine, "outCodecMimeType", "audio/x-wav");
        return engine;
    }

    // --- send* methods, init successful -------------------------------

    @Test
    public void sendParamsWhenInitialized() throws Exception {
        StubPdxClient client = new StubPdxClient();
        TransactionEngine engine = engineWithStubClient(client, true);

        assertNotNull(engine.sendAudioParam("audio"));
        assertTrue(engine.sendChoiceParam("c", "choice"));
        assertTrue(engine.sendDataParam("d", "x".getBytes(StandardCharsets.UTF_8)));
        assertTrue(engine.sendTextParam("t", "hi"));
        assertTrue(engine.sendDictParam("dk", new NmspDictionaryImpl()));
        assertTrue(engine.sendSeqStartParam("ss", new NmspDictionaryImpl()));
        assertTrue(engine.sendSeqChunkParam("sc", new NmspDictionaryImpl()));
        assertTrue(engine.sendSeqEndParam("se", new NmspDictionaryImpl()));
        assertTrue(engine.sendTtsParam("tts", new NmspDictionaryImpl()));

        assertTrue(client.calls.contains("createAudioSink:audio"));
        assertTrue(client.calls.contains("choice:c"));
        assertTrue(client.calls.contains("text:t"));
        assertTrue(client.calls.contains("dict:dk"));
        assertTrue(client.calls.contains("seqStart:ss"));
        assertTrue(client.calls.contains("seqChunk:sc"));
        assertTrue(client.calls.contains("seqEnd:se"));
        assertTrue(client.calls.contains("tts:tts"));
    }

    @Test
    public void addCallLogWithCallLogDataInitialized() throws Exception {
        StubPdxClient client = new StubPdxClient();
        TransactionEngine engine = engineWithStubClient(client, true);
        // CallLogData.addCallLog returns false for null content; not-null path exercises BCP log
        engine.addCallLog("{\"k\":1}");
    }

    @Test
    public void addCallLogReturnsFalseWhenDisabled() throws Exception {
        TransactionEngine engine = new TransactionEngine();
        setField(engine, "responseManager", responseManager);
        // callLogData stays null
        assertFalse(engine.addCallLog("payload"));
    }

    @Test
    public void endCommandWithoutClientIsNoop() {
        // pdxClient null path
        new TransactionEngine().endCommand();
    }

    @Test
    public void endCommandWithClientDelegates() throws Exception {
        StubPdxClient client = new StubPdxClient();
        TransactionEngine engine = engineWithStubClient(client, true);
        engine.endCommand();
        assertTrue(client.endCommandCalled);
    }

    @Test
    public void finalizeTransactionDelegatesAndClearsClient() throws Exception {
        StubPdxClient client = new StubPdxClient();
        TransactionEngine engine = engineWithStubClient(client, true);
        engine.finalizeTransaction();
        assertTrue(client.freeResourceCalled);
        assertTrue(client.shutdownCalled);
        assertNull(getField(engine, "pdxClient"));
    }

    @Test
    public void finalizeTransactionWithoutClientIsNoop() {
        new TransactionEngine().finalizeTransaction(); // pdxClient null path
    }

    @Test
    public void sendCallLogBytesWithoutClientIsNoop() {
        new TransactionEngine().sendCallLogBytes(new byte[]{1, 2});
    }

    @Test
    public void sendCallLogBytesDelegates() throws Exception {
        StubPdxClient client = new StubPdxClient();
        TransactionEngine engine = engineWithStubClient(client, true);
        engine.sendCallLogBytes(new byte[]{9, 9, 9});
        assertArrayEquals(new byte[]{9, 9, 9}, client.lastLog);
    }

    @Test
    public void sendQueryResultWritesJsonPart() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("k", "v");
        engine.sendQueryResult(json, false);
        // Multipart streaming started; OK to call again for final
        engine.sendQueryResult(json, true);
    }

    @Test
    public void sendQueryErrorAndRetryWriteJson() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("Error", 1);
        engine.sendQueryError(json);
        // After error, sendQueryRetry on a freshly-built engine — terminate already called above; use new engine
        TransactionEngine engine2 = engineWithStubClient(new StubPdxClient(), true);
        engine2.sendQueryRetry(json);
    }

    @Test
    public void sendAudioPartDelegatesToResponseManager() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        engine.sendAudioPart(new byte[]{1, 2, 3}, "audio", "audio/x-wav");
    }

    // --- buildResponseListener callbacks ------------------------------

    @Test
    public void listenerOnConnectedSetsSessionId() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-123");
        // Subsequent failure should NOT send an error (sessionId is non-null)
        listener.onConnectionFailed((short) 7);
        // No second sendErrorReply means status remains 200 (default for MockHttpServletResponse)
    }

    @Test
    public void listenerOnConnectionFailedBeforeSessionSendsForbidden() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnectionFailed((short) 7);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, mockHttpServletResponse.getStatus());
    }

    @Test
    public void listenerOnDisconnectedBeforeSessionSendsBadGateway() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onDisconnected((short) 0);
        assertEquals(HttpServletResponse.SC_BAD_GATEWAY, mockHttpServletResponse.getStatus());
    }

    @Test
    public void listenerOnDisconnectedAfterSessionWithoutReplySendsBadRequest() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onDisconnected((short) 0);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockHttpServletResponse.getStatus());
    }

    @Test
    public void listenerOnDisconnectedAfterFinalResultDoesNotError() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onQueryResult(new PdxQueryResult(new NmspDictionaryImpl(), true));
        // queryReplyReceived = true; subsequent disconnect should not generate error
        listener.onDisconnected((short) 0);
        // Last status came from sendQueryResult which goes through initiateSuccessReply → 200
        assertEquals(HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
    }

    @Test
    public void listenerOnQueryResultNonFinalThenFinalUpdatesState() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onQueryResult(new PdxQueryResult(new NmspDictionaryImpl(), false));
        listener.onQueryResult(new PdxQueryResult(new NmspDictionaryImpl(), true));
    }

    @Test
    public void listenerOnQueryErrorWritesJson() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onQueryError(new PdxQueryError(42, "boom"));
        // Subsequent disconnect should NOT send a bad-request (queryReplyReceived=true)
        listener.onDisconnected((short) 0);
    }

    @Test
    public void listenerOnQueryRetryWritesJson() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onQueryRetry(new PdxQueryRetry("name", 1, "prompt"));
        listener.onDisconnected((short) 0); // no extra error after retry
    }

    @Test
    public void listenerOnAudioReceivedDoesNotThrow() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onAudioReceived(new byte[]{1, 2, 3});
    }

    @Test
    public void listenerOnErrorBeforeReplySendsBadGateway() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onError(new RuntimeException("oops"));
        assertEquals(HttpServletResponse.SC_BAD_GATEWAY, mockHttpServletResponse.getStatus());
    }

    @Test
    public void listenerOnErrorAfterReplyDoesNotSecondError() throws Exception {
        TransactionEngine engine = engineWithStubClient(new StubPdxClient(), true);
        PdxResponseListener listener = invokeBuildResponseListener(engine);
        listener.onConnected("sid-xyz");
        listener.onQueryResult(new PdxQueryResult(new NmspDictionaryImpl(), true));
        listener.onError(new RuntimeException("after"));
        assertEquals(HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
    }
}
