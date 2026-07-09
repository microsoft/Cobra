package com.nuance.plugin.ncs7.translators;

import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UploadDynamicVocabularyTranslatorTest {
    Orchestrator orchestrator;
    Session mockWebsocketSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;
    RemoteEndpoint.Async mockAsync;
    ISession mockSession;

    WebApiJSONConfiguration config;
    CombinedConfiguration configs;

    @Before
    public void setUp() throws Exception {
        mockWebsocketSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        mockAsync = mock(RemoteEndpoint.Async.class);
        mockSession = mock(ISession.class);

        when(mockWebsocketSession.isOpen()).thenReturn(true);
        doReturn(mockAsync).when(mockWebsocketSession).getAsyncRemote();
        doReturn(mockSession).when(mockWebapi).getActiveSession();
        when(mockSession.getSessionId()).thenReturn("test");

        config = new WebApiJSONConfiguration();
        configs = new CombinedConfiguration();
        configs.addConfiguration(config);

        when(mockSession.getSessionConfig()).thenReturn(configs);

        orchestrator = new Orchestrator(mockWebsocketSession);
        orchestrator.setWebApi(mockWebapi);
    }

    @After
    public void tearDown() throws Exception {
        doReturn(false).when(mockWebsocketSession).isOpen();
        testState = 0;
    }

    @Test
    public void execute()
            throws IOException, InterruptedException
    {
        Thread thread =
        new Thread(() -> {
            while (testState == 1) {
                Thread.yield(); //wait here
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //success
            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "   \"type\":\"result\",\n" +
                    "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                    "   \"command\":{\n" +
                    "      \"id\":\"27\",\n" +
                    "      \"name\":\"uploadDynamicVocabulary\"\n" +
                    "   },\n" +
                    "   \"result\":{\n" +
                    "      \"final\":true,\n" +
                    "      \"status\":\"success\"\n" +
                    "   }\n" +
                    "}"));

            //done
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
        thread.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        ArrayList<Map<String, String>> items = new ArrayList<>();
        items.add(ImmutableMap.of("literal", "literal1", "value", "value1"));
        dictionary.put("items", items);

        NcsRequest request = new NcsRequest();
        request.command = UploadDynamicVocabularyTranslator.NCS_NAME;
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.concept_id = "test_id";

        testState = 2;
        UploadDynamicVocabularyTranslator translator = new UploadDynamicVocabularyTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        thread.interrupt();
        thread.join(10000);
    }

    @Test
    public void executeIncrementalDelete() {
        Thread thread =
                new Thread(() -> {
                    while (testState == 1) {
                        Thread.yield(); //wait here
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //success
                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "   \"type\":\"result\",\n" +
                            "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "   \"command\":{\n" +
                            "      \"id\":\"27\",\n" +
                            "      \"name\":\"uploadDynamicVocabulary\"\n" +
                            "   },\n" +
                            "   \"result\":{\n" +
                            "      \"final\":true,\n" +
                            "      \"status\":\"success\"\n" +
                            "   }\n" +
                            "}"));

                    //done
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
        thread.start();

        try {
            HashMap<String, Object> dictionary = new HashMap<>();
            dictionary.put("action", "add");
            dictionary.put("literal", "literal1");
            dictionary.put("value", "value1");

            NcsRequest request = new NcsRequest();
            request.command = UploadDynamicVocabularyTranslator.INCREMENTAL_NCS_NAME;
            request.dictionary = dictionary;
            request.transaction_id = "1";
            request.tts_language = "en-us";
            request.concept_id = "test_id";

            testState = 2;
            UploadDynamicVocabularyTranslator translator = new UploadDynamicVocabularyTranslator(orchestrator, request);
            translator.execute();

            dictionary = new HashMap<>();
            dictionary.put("action", "remove");
            dictionary.put("value", "value1");

            request = new NcsRequest();
            request.command = UploadDynamicVocabularyTranslator.INCREMENTAL_NCS_NAME;
            request.dictionary = dictionary;
            request.transaction_id = "1";
            request.tts_language = "en-us";
            request.concept_id = "test_id";

            translator = new UploadDynamicVocabularyTranslator(orchestrator, request);
            translator.execute();

            translator.commandCleanup();
            testState = 0;
            verify(mockAsync, atLeastOnce()).sendText(anyString());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } catch (Throwable t) {
            System.out.println("Got another typecast exception.");
            t.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void executeError()
            throws IOException, InterruptedException
    {
        Thread thread =
                new Thread(() -> {
                    while (testState == 1) {
                        Thread.yield(); //wait here
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //fail
                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "   \"type\":\"result\",\n" +
                            "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "   \"command\":{\n" +
                            "      \"id\":\"27\",\n" +
                            "      \"name\":\"uploadDynamicVocabulary\"\n" +
                            "   },\n" +
                            "   \"result\":{\n" +
                            "      \"final\":true,\n" +
                            "      \"status\":\"error\",\n" +
                            "      \"errors\":[\n" +
                            "         {\n" +
                            "            \"code\":1700,\n" +
                            "            \"message\":\"UploadDynamicVocabulary failed.\"\n" +
                            "         }\n" +
                            "      ]\n" +
                            "   }\n" +
                            "}"));

                    //done
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
        thread.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        ArrayList<Map<String, String>> items = new ArrayList<>();
        items.add(ImmutableMap.of("literal", "literal1", "value", "value1"));
        dictionary.put("items", items);

        NcsRequest request = new NcsRequest();
        request.command = UploadDynamicVocabularyTranslator.NCS_NAME;
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.concept_id = "test_id";

        testState = 2;
        UploadDynamicVocabularyTranslator translator = new UploadDynamicVocabularyTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        thread.interrupt();
        thread.join(10000);
    }
}
