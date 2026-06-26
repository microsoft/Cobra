package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
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

public class InterpretationTranslatorTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        when(mockSession.isOpen()).thenReturn(true);

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
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

            //success
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
        dictionary.put("nlu_nbest_interpretations", 1);

        NcsRequest request = new NcsRequest();
        request.dictionary = dictionary;
        request.transaction_id = "transaction_id";
        request.tts_language = "en-us";
        request.context_tag = "A539_C1";
        request.language = "eng-CAN";

        RemoteEndpoint.Async mockAsync = mock (RemoteEndpoint.Async.class);
        doReturn(mockAsync).when(mockSession).getAsyncRemote();
        ISession mochSession = mock(ISession.class);
        doReturn(mochSession).when(mockWebapi).getActiveSession();
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
        orchestrator.setProductImprovements(List.of(Map.of("feature", "model-improvements","enable",false)));

        InterpretationTranslator translator = new InterpretationTranslator(orchestrator, request);
        translator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }
}