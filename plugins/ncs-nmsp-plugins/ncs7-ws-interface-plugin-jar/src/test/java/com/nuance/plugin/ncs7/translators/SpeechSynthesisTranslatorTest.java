package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nuance.plugin.ncs7.Ncs7InterfaceConfig.Property.INSERT_AUDIO_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SpeechSynthesisTranslatorTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);

        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.getAsyncRemote()).thenReturn(mock(RemoteEndpoint.Async.class));

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);

        orchestrator.setWebApiFeatures(mock(IWebApiFeatures.class));
        when(orchestrator.getWebApiFeatures().getConfigs()).thenReturn(mock(CombinedConfiguration.class));
        when(orchestrator.getWebApiFeatures().getConfigs().getBoolean(INSERT_AUDIO_MESSAGE.getKey(), (boolean)INSERT_AUDIO_MESSAGE.getDefaultValue())).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        when(mockSession.isOpen()).thenReturn(false);
        testState = 0;
    }

    @Test
    public void execute()
            throws IOException, InterruptedException
    {
        Thread t =
        new Thread(() -> {
            while (testState == 1) {
                Thread.yield(); //wait here
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                    "    \"command\": {\n" +
                    "        \"id\": \"7\",\n" +
                    "        \"name\": \"speechSynthesis\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": true,\n" +
                    "        \"status\": \"success\"\n" +
                    "    }\n" +
                    "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                    "    \"command\": {\n" +
                    "        \"id\": \"7\",\n" +
                    "        \"name\": \"speechSynthesis\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": true,\n" +
                    "        \"status\": \"error\",\n" +
                    "        \"errors\": [\n" +
                    "            {\n" +
                    "                \"code\": 1500,\n" +
                    "                \"message\": \"SpeechSynthesis failed.\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"code\": 1501,\n" +
                    "                \"message\": \"SpeechSynthesis command couldn't be executed.\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }\n" +
                    "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                    "    \"command\": {\n" +
                    "        \"name\": \"endSession\",\n" +
                    "        \"id\": \"45\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": true,\n" +
                    "        \"status\": \"success\"\n" +
                    "    }\n" +
                    "}"));

            while (testState == 2) {
                Thread.yield(); //wait here
            }
        });
        t.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        dictionary.put("tts_input", "test");
        dictionary.put("tts_type", "text");
        dictionary.put("tts_language", "en-us");
        dictionary.put("audio_id", 1);

        orchestrator.setProductImprovements(List.of(new JSONObject("{\"feature\":\"model-improvements\",\"enable\":false}")));

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.tts_text_genre = "typeA";

        RemoteEndpoint.Async mockAsync = mock (RemoteEndpoint.Async.class);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);
        ISession mochSession = mock(ISession.class);
        when(mockWebapi.getActiveSession()).thenReturn(mochSession);
        when(mochSession.getSessionId()).thenReturn("test");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        config.addProperty("vocsMapping.Ava.language", "no-match");
        config.addProperty("vocsMapping.Ava.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Ava.name", "Evan");

        config.addProperty("insertAudioMessage", "false");

        when(mochSession.getSessionConfig()).thenReturn(configs);

        testState = 2;
        SpeechSynthesisTranslator translator = new SpeechSynthesisTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void testTTSGenre() {
        NcsRequest ncsRequest = new NcsRequest();

        SpeechSynthesisTranslator translator = new SpeechSynthesisTranslator(orchestrator, ncsRequest);
        assertNull(translator.getTtsGenre(ncsRequest));

        ncsRequest.tts_text_genre = "typeA";
        assertEquals(ncsRequest.tts_text_genre, translator.getTtsGenre(ncsRequest));

        ncsRequest.tts_genre = "typeB";
        assertEquals(ncsRequest.tts_text_genre, translator.getTtsGenre(ncsRequest));

        ncsRequest.tts_text_genre = null;
        assertEquals(ncsRequest.tts_genre, translator.getTtsGenre(ncsRequest));
    }

    @Test
    public void testSendBinary() throws IOException{
        NcsRequest ncsRequest = new NcsRequest();

        SpeechSynthesisTranslator translator = new SpeechSynthesisTranslator(orchestrator, ncsRequest);

        byte[] audio = new byte[5];
        translator.sendBinary(audio);

        verify(orchestrator.getWebSocketSession().getAsyncRemote(), times(1)).sendBinary(any());
    }

    @Test
    public void testSendBinary_exception() throws IOException{
        NcsRequest ncsRequest = new NcsRequest();

        when(orchestrator.getWebApiFeatures().getConfigs()).thenThrow(new IOException("error"));

        orchestrator = spy(orchestrator);

        SpeechSynthesisTranslator translator = new SpeechSynthesisTranslator(orchestrator, ncsRequest);

        byte[] audio = new byte[5];
        translator.sendBinary(audio);

        verify(orchestrator.getWebSocketSession().getAsyncRemote(), times(1)).sendBinary(any());
    }

    @Test
    public void executeFail()
            throws IOException, InterruptedException
    {
        Thread t =
                new Thread(() -> {
                    while (testState == 1) {
                        Thread.yield(); //wait here
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"7\",\n" +
                            "        \"name\": \"speechSynthesis\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"code\": 1500,\n" +
                            "                \"message\": \"SpeechSynthesis failed.\"\n" +
                            "            },\n" +
                            "            {\n" +
                            "                \"code\": 1501,\n" +
                            "                \"message\": \"SpeechSynthesis command couldn't be executed.\"\n" +
                            "            }\n" +
                            "        ]\n" +
                            "    }\n" +
                            "}"));

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "    \"command\": {\n" +
                            "        \"name\": \"endSession\",\n" +
                            "        \"id\": \"45\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\"\n" +
                            "    }\n" +
                            "}"));

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        dictionary.put("tts_input", "test");
        dictionary.put("tts_type", "text");
        dictionary.put("tts_language", "en-us");
        dictionary.put("audio_id", 1);

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.tts_text_genre = "typeA";

        RemoteEndpoint.Async mockAsync = mock (RemoteEndpoint.Async.class);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);
        ISession mochSession = mock(ISession.class);
        when(mockWebapi.getActiveSession()).thenReturn(mochSession);
        when(mochSession.getSessionId()).thenReturn("test");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        config.addProperty("vocsMapping.Ava.language", "no-match");
        config.addProperty("vocsMapping.Ava.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Ava.name", "Evan");

        when(mochSession.getSessionConfig()).thenReturn(configs);

        testState = 2;
        SpeechSynthesisTranslator translator = new SpeechSynthesisTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }
}