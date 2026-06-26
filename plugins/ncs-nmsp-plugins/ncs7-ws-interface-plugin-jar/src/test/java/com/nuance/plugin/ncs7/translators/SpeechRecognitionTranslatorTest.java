package com.nuance.plugin.ncs7.translators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SpeechRecognitionTranslatorTest {
    Orchestrator orchestrator;
    Session mockWsSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;

    RemoteEndpoint.Async mockAsync;
    ISession mockSession;

    WebApiJSONConfiguration config;
    CombinedConfiguration configs;

    @Before
    public void setUp() throws Exception {
        mockWsSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        mockAsync = mock(RemoteEndpoint.Async.class);
        mockSession = mock(ISession.class);
        config = new WebApiJSONConfiguration();
        configs = mock(CombinedConfiguration.class);

        when(mockWsSession.isOpen()).thenReturn(true);

        doReturn(mockAsync).when(mockWsSession).getAsyncRemote();
        doReturn(mockSession).when(mockWebapi).getActiveSession();
        when(mockSession.getSessionId()).thenReturn("test");

        configs.addConfiguration(config);
        when(mockSession.getSessionConfig()).thenReturn(configs);

        orchestrator = new Orchestrator(mockWsSession);
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
        when(mockWsSession.isOpen()).thenReturn(false);
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

            ObjectMapper mapper = new ObjectMapper();
            try {
                orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, mapper.readValue("{\"message\": \"audio\", \"audio_id\": 2}", HashMap.class)));
                orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.DATA, new byte[10000]));

                while (!orchestrator.clientAudioBytesBufferQueue.isEmpty()) {
                    Thread.yield();
                }

                orchestrator.clientAudioBytesBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, mapper.readValue("{\"message\": \"audio_end\", \"audio_id\": 2}", HashMap.class)));

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
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

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                    "    \"command\": {\n" +
                    "        \"name\": \"speechRecognition\",\n" +
                    "        \"id\": \"2\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": false,\n" +
                    "        \"status\": \"success\",\n" +
                    "        \"data\": {\n" +
                    "            \"transcriptions\": [\n" +
                    "                \"Hello where\"\n" +
                    "            ],\n" +
                    "            \"confidence\": 0,\n" +
                    "            \"averageWeightedConfidence\": [\n" +
                    "                0\n" +
                    "            ],\n" +
                    "            \"words\": [\n" +
                    "                [\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"Hello\\\\*no-space-before\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"where\"\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            ]\n" +
                    "        },\n" +
                    "        \"bestResult\": \"Hello where\"\n" +
                    "    }\n" +
                    "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
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

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                    "    \"command\": {\n" +
                    "        \"name\": \"speechRecognition\",\n" +
                    "        \"id\": \"2\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": true,\n" +
                    "        \"status\": \"success\",\n" +
                    "        \"data\": {\n" +
                    "            \"transcriptions\": [\n" +
                    "                \"Hello web API\",\n" +
                    "                \"Hello weather API\",\n" +
                    "                \"Hello web NPR\"\n" +
                    "            ],\n" +
                    "            \"confidence\": 563,\n" +
                    "            \"averageWeightedConfidences\": [\n" +
                    "                867,\n" +
                    "                1,\n" +
                    "                1\n" +
                    "            ],\n" +
                    "            \"words\": [\n" +
                    "                [\n" +
                    "                    {\n" +
                    "                        \"confidence\": 1,\n" +
                    "                        \"word\": \"Hello\\\\*no-space-before\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0.786,\n" +
                    "                        \"word\": \"web\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0.725,\n" +
                    "                        \"word\": \"API\"\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                [\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0.003,\n" +
                    "                        \"word\": \"Hello\\\\*no-space-before\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"weather\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"API\"\n" +
                    "                    }\n" +
                    "                ],\n" +
                    "                [\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"Hello\\\\*no-space-before\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0,\n" +
                    "                        \"word\": \"web\"\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"confidence\": 0.003,\n" +
                    "                        \"word\": \"NPR\"\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            ]\n" +
                    "        },\n" +
                    "        \"bestResult\": \"Hello web API\",\n" +
                    "        \"noiseLevel\": {\n" +
                    "            \"mean\": 67.82,\n" +
                    "            \"standardDeviation\": 24.48\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                    "    \"type\": \"result\",\n" +
                    "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                    "    \"command\": {\n" +
                    "        \"id\": \"2\",\n" +
                    "        \"name\": \"speechRecognition\"\n" +
                    "    },\n" +
                    "    \"result\": {\n" +
                    "        \"final\": true,\n" +
                    "        \"status\": \"error\",\n" +
                    "        \"errors\": [\n" +
                    "            {\n" +
                    "                \"code\": 1600,\n" +
                    "                \"message\": \"speechRecognition failed.\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"code\": 1601,\n" +
                    "                \"message\": \"speechRecognition command couldn't be executed.\"\n" +
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
            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                                                      "    \"type\": \"result\",\n" +
                                                      "    \"sessionId\": \"bffeaa93-126c-41e0-9bec-ebf26e48a5b6\",\n" +
                                                      "    \"command\": {\n" +
                                                      "        \"name\": \"speechRecognition\",\n" +
                                                      "        \"id\": \"2\"\n" +
                                                      "    },\n" +
                                                      "    \"result\": {\n" +
                                                      "        \"final\": true,\n" +
                                                      "        \"status\": \"noInput\",\n" +
                                                      "        \"noiseLevel\": {\n" +
                                                      "            \"mean\": 527.66,\n" +
                                                      "            \"standardDeviation\": 575.35\n" +
                                                      "        }\n" +
                                                      "    }\n" +
                                                      "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                                                        "    \"type\": \"result\",\n" +
                                                        "    \"sessionId\": \"bffeaa93-126c-41e0-9bec-ebf26e48a5b6\",\n" +
                                                        "    \"command\": {\n" +
                                                        "        \"name\": \"speechRecognition\",\n" +
                                                        "        \"id\": \"3\"\n" +
                                                        "    },\n" +
                                                        "    \"result\": {\n" +
                                                        "        \"final\": true,\n" +
                                                        "        \"status\": \"noMatch\",\n" +
                                                        "        \"noiseLevel\": {\n" +
                                                        "            \"mean\": 59.68,\n" +
                                                        "            \"standardDeviation\": 64.11\n" +
                                                        "        }\n" +
                                                        "    }\n" +
                                                        "}"));

            while (testState == 2) {
                Thread.yield(); //wait here
            }
        });
        t.start();

        HashMap<String, Object> dictionary = new HashMap<>();
        dictionary.put("audio_id", 1);
        dictionary.put("result_delivery", "progressive");

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "1";
        request.utterance_detection_silence_duration = "Aggressive";
        request.dictation_type = "dtv";
        request.dictation_language = "eng-USA";
        request.device_codec = 5;

        // tenant config
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.topic", "DTV");
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.speechDomain", "websearch");

        testState = 2;
        SpeechRecognitionTranslator translator = new SpeechRecognitionTranslator(orchestrator, request);
        translator.execute();

        testState = 0;

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockAsync, atLeastOnce()).sendText(argumentCaptor.capture());

//        System.out.println(argumentCaptor.getAllValues());
        assertTrue(argumentCaptor.getAllValues().get(0).contains("partialRecognition"));
        assertTrue(argumentCaptor.getAllValues().get(1).contains("completeRecognition"));
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void executeNoMatchNoInput()
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
                                                        "    \"sessionId\": \"bffeaa93-126c-41e0-9bec-ebf26e48a5b6\",\n" +
                                                        "    \"command\": {\n" +
                                                        "        \"name\": \"speechRecognition\",\n" +
                                                        "        \"id\": \"2\"\n" +
                                                        "    },\n" +
                                                        "    \"result\": {\n" +
                                                        "        \"final\": true,\n" +
                                                        "        \"status\": \"noInput\",\n" +
                                                        "        \"noiseLevel\": {\n" +
                                                        "            \"mean\": 527.66,\n" +
                                                        "            \"standardDeviation\": 575.35\n" +
                                                        "        }\n" +
                                                        "    }\n" +
                                                        "}"));

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                                                        "    \"type\": \"result\",\n" +
                                                        "    \"sessionId\": \"bffeaa93-126c-41e0-9bec-ebf26e48a5b6\",\n" +
                                                        "    \"command\": {\n" +
                                                        "        \"name\": \"speechRecognition\",\n" +
                                                        "        \"id\": \"3\"\n" +
                                                        "    },\n" +
                                                        "    \"result\": {\n" +
                                                        "        \"final\": true,\n" +
                                                        "        \"status\": \"noMatch\",\n" +
                                                        "        \"noiseLevel\": {\n" +
                                                        "            \"mean\": 59.68,\n" +
                                                        "            \"standardDeviation\": 64.11\n" +
                                                        "        }\n" +
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
        dictionary.put("audio_id", 1);

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "1";
        request.dictation_language = "eng-USA";
        request.device_codec = 5;

        testState = 2;
        SpeechRecognitionTranslator translator = new SpeechRecognitionTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;

        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void streamingTest() throws IOException {
        SpeechRecognitionTranslator testObj = new SpeechRecognitionTranslator(orchestrator, null);
        testObj.sendBinary("test".getBytes());
        verify(mockWebapi, atLeastOnce()).onBinary(any(), any());

        testObj.onStreamingDone();
        verify(mockWebapi, atLeastOnce()).onMessage(any(), anyString());
    }

    @Test
    public void doRequest_productImprovements() throws IOException{
        SpeechRecognitionTranslator testObj = new SpeechRecognitionTranslator(orchestrator, null);
        NcsRequest ncsRequest = new NcsRequest();
        orchestrator.setProductImprovements(List.of(Map.of("feature", "model-improvements","enable",false)));
        testObj.doRequest(ncsRequest);
        ArgumentCaptor<WebApiJSONConfiguration> captor = ArgumentCaptor.forClass(WebApiJSONConfiguration.class);
        verify(configs).addConfiguration(captor.capture(), eq("ncs7-ws-plugin-recognition-parameters"));
        assertTrue(captor.getValue().getBoolean("kryptonSuppressCallRecording"));
        assertTrue(captor.getValue().getBoolean("kryptonDiscardSpeakerAdaptation"));
        assertEquals(captor.getValue().getString("nluAasGrpcInputLoggingMode"), "SUPPRESSED");
    }

    @Test
    public void doRequest_productImprovementsMissingModelImprovments() throws IOException{
        SpeechRecognitionTranslator testObj = new SpeechRecognitionTranslator(orchestrator, null);
        NcsRequest ncsRequest = new NcsRequest();
        orchestrator.setProductImprovements(new ArrayList<>());
        testObj.doRequest(ncsRequest);
        verify(configs, times(0)).addConfiguration(any(), eq("ncs7-ws-plugin-recognition-parameters"));
    }

    @Test
    public void doRequest_productImprovementsMissing() throws IOException{
        SpeechRecognitionTranslator testObj = new SpeechRecognitionTranslator(orchestrator, null);
        NcsRequest ncsRequest = new NcsRequest();
        testObj.doRequest(ncsRequest);
        verify(configs, times(0)).addConfiguration(any(), eq("ncs7-ws-plugin-recognition-parameters"));
    }
}