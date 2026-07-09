package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class InterpretationWithSpeechRecognitionTranslatorTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;
    private RemoteEndpoint.Async mockAsync;
    private ISession mockWebApiSession;
    CombinedConfiguration configs;
    WebApiJSONConfiguration config;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);

        when(mockSession.isOpen()).thenReturn(true);

        mockAsync = mock (RemoteEndpoint.Async.class);
        doReturn(mockAsync).when(mockSession).getAsyncRemote();
        mockWebApiSession = mock(ISession.class);
        doReturn(mockWebApiSession).when(mockWebapi).getActiveSession();
        when(mockWebApiSession.getSessionId()).thenReturn("test");

        config = new WebApiJSONConfiguration();
        configs = new CombinedConfiguration();
        configs.addConfiguration(config);
        when(mockWebApiSession.getSessionConfig()).thenReturn(configs);

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
        orchestrator.setWebApiFeatures(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return null;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
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
                return null;
            }
        });
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
                            "    \"type\": \"event\",\n" +
                            "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                            "    \"command\": {\n" +
                            "        \"name\": \"speechRecognition\",\n" +
                            "        \"id\": \"2\"\n" +
                            "    },\n" +
                            "    \"event\": {\n" +
                            "        \"name\": \"startOfSpeech\"\n" +
                            "    }\n" +
                            "}"));

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"event\",\n" +
                            "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                            "    \"command\": {\n" +
                            "        \"name\": \"speechRecognition\",\n" +
                            "        \"id\": \"2\"\n" +
                            "    },\n" +
                            "    \"event\": {\n" +
                            "        \"name\": \"endOfSpeech\"\n" +
                            "    }\n" +
                            "}"));

                    //success
                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"f528a79b-b847-4439-b420-b529011da83d\",\n" +
                            "    \"command\": {\n" +
                            "        \"name\": \"speechRecognition\",\n" +
                            "        \"id\": \"6\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\",\n" +
                            "        \"data\": {\n" +
                            "            \"transcriptions\": [\n" +
                            "                \"Hello\"\n" +
                            "            ],\n" +
                            "            \"confidence\": 960,\n" +
                            "            \"averageWeightedConfidences\": [\n" +
                            "                950\n" +
                            "            ],\n" +
                            "            \"words\": [\n" +
                            "                [\n" +
                            "                    {\n" +
                            "                        \"confidence\": 950,\n" +
                            "                        \"word\": \"Hello\"\n" +
                            "                    }\n" +
                            "                ]\n" +
                            "            ]\n" +
                            "        },\n" +
                            "        \"bestResult\": \"Hello\",\n" +
                            "        \"noiseLevel\": {\n" +
                            "            \"mean\": 0,\n" +
                            "            \"standardDeviation\": 0\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"));

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"interpretation\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\",\n" +
                            "        \"interpretations\": [\n" +
                            "            {\n" +
                            "                \"concepts\": [\n" +
                            "                    {\n" +
                            "                        \"concept\": \"FLOWER\",\n" +
                            "                        \"value\": \"Tulips\",\n" +
                            "                        \"literal\": \"tulips\",\n" +
                            "                        \"begin\": 10,\n" +
                            "                        \"end\": 16\n" +
                            "                    },\n" +
                            "                    {\n" +
                            "                        \"concept\": \"FLOWER\",\n" +
                            "                        \"value\": \"ROSES\",\n" +
                            "                        \"literal\": \"roses\",\n" +
                            "                        \"begin\": 21,\n" +
                            "                        \"end\": 26\n" +
                            "                    }\n" +
                            "                ],\n" +
                            "                \"confidence\": 100,\n" +
                            "                \"literal\": \"Can I buy tulips and roses?\",\n" +
                            "                \"intent\": \"BUY_FLOWERS\",\n" +
                            "                \"origin\": \"SSM\"\n" +
                            "            }\n" +
                            "        ],\n" +
                            "        \"bestInterpretation\": {\n" +
                            "            \"concepts\": [\n" +
                            "                {\n" +
                            "                    \"concept\": \"FLOWER\",\n" +
                            "                    \"value\": \"Tulips\",\n" +
                            "                    \"literal\": \"tulips\",\n" +
                            "                    \"begin\": 10,\n" +
                            "                    \"end\": 16\n" +
                            "                },\n" +
                            "                {\n" +
                            "                    \"concept\": \"FLOWER\",\n" +
                            "                    \"value\": \"ROSES\",\n" +
                            "                    \"literal\": \"roses\",\n" +
                            "                    \"begin\": 21,\n" +
                            "                    \"end\": 26\n" +
                            "                }\n" +
                            "            ],\n" +
                            "            \"confidence\": 100,\n" +
                            "            \"literal\": \"Can I buy tulips and roses?\",\n" +
                            "            \"intent\": \"BUY_FLOWERS\",\n" +
                            "            \"origin\": \"SSM\"\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"));
                    //fail
                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"interpretation\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"code\": 1800,\n" +
                            "                \"message\": \"Interpretation failed.\"\n" +
                            "            }\n" +
                            "        ]\n" +
                            "    }\n" +
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
        t.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        HashMap<String, Object> recoData = new HashMap<>();
        recoData.put("text_input","Hello");
        dictionary.put("application_data", recoData);
        dictionary.put("result_delivery", "progressive");

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.context_tag = "A539_C1";
        request.language = "eng-CAN";
        request.device_codec = 5;

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

        // tenant config
        config.addProperty("contextTagToTopicMap.A539_C1.eng-CAN.topic", "DTV");
        config.addProperty("contextTagToTopicMap.A539_C1.eng-CAN.speechDomain", "websearch");

        config.addProperty("clientDetails", Map.of());

        when(mockWebApiSession.getSessionConfig()).thenReturn(configs);

        orchestrator.setProductImprovements(List.of(Map.of("feature", "model-improvements","enable",false)));
        testState = 2;
        InterpretationWithSpeechRecognitionTranslator translator = new InterpretationWithSpeechRecognitionTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void executeFailure()
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

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"sessionId\": \"bbe328d9-e25f-44b1-ab81-ab2550d815a4\",\n" +
                            "    \"command\": {\n" +
                            "        \"name\": \"speechRecognition\",\n" +
                            "        \"id\": \"8\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"message\": \"SpeechRecognition failed.\",\n" +
                            "                \"code\": 1600\n" +
                            "            }\n" +
                            "        ],\n" +
                            "        \"noiseLevel\": {\n" +
                            "            \"mean\": 0,\n" +
                            "            \"standardDeviation\": 0\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"));

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        HashMap<String, Object> recoData = new HashMap<>();
        recoData.put("text_input","Hello");
        dictionary.put("application_data", recoData);
        dictionary.put("result_delivery", "progressive");

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.context_tag = "A539_C1";
        request.language = "eng-CAN";
        request.device_codec = 5;

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

        config.addProperty("clientDetails", Map.of());

        when(mockWebApiSession.getSessionConfig()).thenReturn(configs);

        orchestrator.setProductImprovements(List.of(Map.of("feature", "model-improvements","enable",false)));
        testState = 2;
        InterpretationWithSpeechRecognitionTranslator translator = new InterpretationWithSpeechRecognitionTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void streamingTest() throws IOException {
        NcsRequest ncsRequest = mock(NcsRequest.class);
        when(ncsRequest.getASRLanguage()).thenReturn("eng-usa");
        InterpretationWithSpeechRecognitionTranslator testObj = new InterpretationWithSpeechRecognitionTranslator(orchestrator, ncsRequest);
        testObj.sendBinary("test".getBytes());
        verify(mockWebapi, atLeastOnce()).onBinary(any(), any());

        testObj.onStreamingDone();
        verify(mockWebapi, atLeastOnce()).onMessage(any(), anyString());
    }

    @Test
    public void test_handleSpeechRecoResults() throws IOException {
        NcsRequest ncsRequest = mock(NcsRequest.class);
        when(ncsRequest.getASRLanguage()).thenReturn("eng-usa");
        InterpretationWithSpeechRecognitionTranslator testObj = new InterpretationWithSpeechRecognitionTranslator(orchestrator, ncsRequest);

        WebApiResponse response = testObj.createDummyWebapiResponse(ncsRequest);
        response.command.put("name", "speechRecognition");
        response.command.put("id", "5");
        response.result.put("status", "noMatch");

        testObj.doResponse(response);
    }
}