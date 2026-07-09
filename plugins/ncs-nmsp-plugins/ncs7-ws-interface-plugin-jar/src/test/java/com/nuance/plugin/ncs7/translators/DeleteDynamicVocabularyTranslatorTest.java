package com.nuance.plugin.ncs7.translators;

import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DeleteDynamicVocabularyTranslatorTest
{
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

    @SuppressWarnings("java:S2925")
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

            //done
            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE, "{\n" +
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
        ArrayList<Map<String, String>> items = new ArrayList<>();
        items.add(ImmutableMap.of("literal", "literal1", "value", "value1"));
        dictionary.put("items", items);

        NcsRequest request = new NcsRequest();
        request.command = UploadDynamicVocabularyTranslator.NCS_NAME;
        request.dictionary = dictionary;
        request.transaction_id = "1";
        request.tts_language = "en-us";

        RemoteEndpoint.Async mockAsync = mock(RemoteEndpoint.Async.class);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);
        ISession mochSession = mock(ISession.class);
        when(mockWebapi.getActiveSession()).thenReturn(mochSession);
        when(mochSession.getSessionId()).thenReturn("test");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);

        when(mochSession.getSessionConfig()).thenReturn(configs);

        testState = 2;
        DeleteDynamicVocabularyTranslator translator = new DeleteDynamicVocabularyTranslator(orchestrator, request);
        DeleteDynamicVocabularyTranslator spyTranslator = spy(translator);
        WebApiResponse response = translator.createDummyWebapiResponse(request);
        response.result.put("status", "error");
        List<Map<String, Object>> errors = new ArrayList<>();
        Map<String, Object> error = new HashMap<>();
        error.put("code", 4);
        error.put("message", "fake Error message");
        errors.add(error);
        response.result.put("errors", errors);
        doReturn(response).when(spyTranslator).createDummyWebapiResponse(request);
        spyTranslator.execute();

        translator.commandCleanup();
        testState = 0;
        verify(mockAsync, atLeastOnce()).sendText(anyString());

        t.interrupt();
        t.join(10000);
    }
}