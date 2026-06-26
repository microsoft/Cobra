package com.nuance.plugin.ncs7;

import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.plugin.ncs7.translators.*;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrchestratorTest {
    private static final Integer TIMEOUT_MILLISECONDS = 750;

    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.getAsyncRemote()).thenReturn(new RemoteEndpoint.Async() {
            @Override
            public long getSendTimeout() {
                return 0;
            }

            @Override
            public void setSendTimeout(long l) {

            }

            @Override
            public void sendText(String s, SendHandler sendHandler) {

            }

            @Override
            public Future<Void> sendText(String s) {
                return null;
            }

            @Override
            public Future<Void> sendBinary(ByteBuffer byteBuffer) {
                return null;
            }

            @Override
            public void sendBinary(ByteBuffer byteBuffer, SendHandler sendHandler) {

            }

            @Override
            public Future<Void> sendObject(Object o) {
                return null;
            }

            @Override
            public void sendObject(Object o, SendHandler sendHandler) {

            }

            @Override
            public void setBatchingAllowed(boolean b) throws IOException {

            }

            @Override
            public boolean getBatchingAllowed() {
                return false;
            }

            @Override
            public void flushBatch() throws IOException {

            }

            @Override
            public void sendPing(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException {

            }

            @Override
            public void sendPong(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException {

            }
        });
        doReturn(new ISession() {
            @Override
            public String getSessionId() {
                return "id";
            }

            @Override
            public ICommand getCommand() {
                return null;
            }

            @Override
            public CombinedConfiguration getSessionConfig() {
                return new CombinedConfiguration();
            }

            @Override
            public CombinedConfiguration getConfigForRequest() {
                return null;
            }
        }).when(mockWebapi).getActiveSession();

        orchestrator = new Orchestrator(mockSession);
        orchestrator.webApi = mockWebapi;
        orchestrator.setWebApiFeatures(mock(IWebApiFeatures.class));
        when(orchestrator.getWebApiFeatures().getConfigs()).thenReturn(new CombinedConfiguration());
    }

    @After
    public void tearDown() throws Exception {
        doReturn(false).when(mockSession).isOpen();
    }

//    @Test
//    public void doConnect() throws InterruptedException {
//        HashMap<String, Object> testMap = new HashMap();
//        testMap.put("message", "connect");
//        testMap.put("app_key", "connect");
//        testMap.put("codec", AudioCodec.PCM_16_16K.name());
//        orchestrator.clientMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
//
//        orchestrator.webapiMessageBufferQueue.poll(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
//    }

    @Test
    public void doTTS() throws InterruptedException {
        HashMap<String, Object> testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("command", "NMDP_TTS_CMD");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));

        orchestrator.webapiMessageBufferQueue.poll(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void doReco() throws InterruptedException {
        HashMap<String, Object> testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("command", "NMDP_ASR_CMD");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));

        testMap = new HashMap();
        testMap.put("message", "audio_begin");
        orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.DATA, testMap));

        testMap = new HashMap();
        testMap.put("message", "audio");
        orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.DATA, testMap));

        testMap = new HashMap();
        testMap.put("message", "audio_end");
        orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.DATA, testMap));

        orchestrator.webapiMessageBufferQueue.poll(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void doAllOther() throws InterruptedException {
        Thread.yield();

        orchestrator.clientMessageBufferQueue.clear();

        HashMap<String, Object> testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("command", SpeechRecognitionTranslator.NCS_NAME);
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("command", SpeechSynthesisTranslator.NCS_NAME);
        testMap.put("app_key", "connect");
        HashMap<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("audio_id", 1);
        testMap.put("dictionary", dictionary);
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        testMap.put("command", InterpretationWithSpeechRecognitionTranslator.NCS_NAME);
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        dictionary = new HashMap<String, Object>();
        dictionary.put("application_data", new HashMap<String, Object>());
        testMap.put("dictionary", dictionary);
        testMap.put("command", InterpretationTranslator.NCS_NAME);
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        dictionary = new HashMap<String, Object>();
        dictionary.put("action", "add");
        dictionary.put("value", "add");
        dictionary.put("literal", "add");
        testMap.put("dictionary", dictionary);
        testMap.put("concept_id", "test");
        testMap.put("command", UploadDynamicVocabularyTranslator.INCREMENTAL_NCS_NAME);
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        testMap.put("command", DeleteDynamicVocabularyTranslator.NCS_NAME);
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        testMap.put("command", "NOT_IMPLEMENTED");
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));
        orchestrator.webapiMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, ""));

        int timeout = 10;
        while (orchestrator.clientMessageBufferQueue.size() > 0 && timeout > 0) {
            Thread.sleep(1000);
            timeout --;
        }
        if (orchestrator.clientMessageBufferQueue.size() > 0) fail("Not all commands have been tested. There are still messages in the queue");

        orchestrator.cleanup();

        testMap = new HashMap();
        testMap.put("message", "query_begin");
        testMap.put("app_key", "connect");
        testMap.put("codec", "invalid_codec");
        testMap.put("command", "NOT_IMPLEMENTED");
        orchestrator.clientMessageBufferQueue.add(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));

        timeout = 5;
        while (orchestrator.clientMessageBufferQueue.size() > 0 && timeout > 0) {
            Thread.sleep(1000);
            timeout --;
        }
    }

    @Test
    public void doUserReset() throws InterruptedException {
        assertNull(orchestrator.command);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("message", "query_begin");
        testMap.put("command", UserResetTranslator.NCS_NAME);
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));

        orchestrator.webapiMessageBufferQueue.poll(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void doSessionDelete() throws InterruptedException {
        assertNull(orchestrator.command);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("message", "query_begin");
        testMap.put("command", SessionDeleteTranslator.NCS_NAME);
        testMap.put("app_key", "connect");
        testMap.put("codec", AudioCodec.PCM_16_16K.name());
        orchestrator.clientMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, testMap));

        orchestrator.webapiMessageBufferQueue.poll(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void getters() {
        assertNotNull(orchestrator.getWebSocketSession());
        assertNotNull(orchestrator.getWebApi());

        orchestrator.setAudioId(1);
        assertEquals(1, (int) orchestrator.getAudioId());

        orchestrator.setCodec("opus");
        assertEquals("opus", orchestrator.getCodec());

        orchestrator.setStartTime(1L);
        assertEquals(1L, (long)orchestrator.getStartTime());

        orchestrator.setEndTime(1L);
        assertEquals(1L, (long)orchestrator.getEndTime());

        orchestrator.setCodecId("1");
        assertEquals(1L, (long)orchestrator.getCodecId());

        JSONObject obj = new JSONObject();
        obj.put("test", new JSONObject());
        orchestrator.addDynamicVocabulary("1", obj);
        assertEquals(obj, orchestrator.getDynamicVocabulary().get("1"));

        orchestrator.removeDynamicVocabulary("1");
        assertEquals(0, orchestrator.getDynamicVocabulary().size());
    }
}