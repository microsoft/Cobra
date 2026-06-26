package com.nuance.plugin.ncs7.translators;

import com.nuance.plugin.ncs7.Ncs7InterfaceConfig;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

import static com.nuance.plugin.ncs7.Ncs7InterfaceConfig.Property.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class StartSessionTranslatorTest {
    Orchestrator orchestrator;
    Session mockSession;
    IWebApiEndpoint mockWebapi;
    int testState = 1;
    RemoteEndpoint.Async mockAsync;

    @Before
    public void setUp() throws Exception {
        mockSession = mock(Session.class);
        mockWebapi = mock(IWebApiEndpoint.class);
        mockAsync = mock(RemoteEndpoint.Async.class);

        when(mockSession.isOpen()).thenReturn(true);
        doReturn(mockAsync).when(mockSession).getAsyncRemote();

        orchestrator = new Orchestrator(mockSession);
        orchestrator.setWebApi(mockWebapi);
        orchestrator.setWebApiFeatures(mock(IWebApiFeatures.class));
        when(orchestrator.getWebApiFeatures().getConfigs()).thenReturn(mock(CombinedConfiguration.class));
        when(orchestrator.getWebApiFeatures().getConfigs().getString(HASH_SECRET.getKey(), HASH_SECRET.getDefaultValue().toString())).thenReturn("ZmFrZSBzYWx0");
        when(orchestrator.getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString())).thenReturn(HASH_TYPE.getDefaultValue().toString());
        when(orchestrator.getWebApiFeatures().getConfigs().getString(AUTH_TYPE.getKey(), AUTH_TYPE.getDefaultValue().toString())).thenReturn(AUTH_TYPE.getDefaultValue().toString());
    }

    @After
    public void tearDown() throws Exception {
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
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\"\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"code\": 1302,\n" +
                            "                \"message\": \"StartSession service failed.\"\n" +
                            "            }\n" +
                            "        ]\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

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

                    Thread.yield(); //wait here

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        NcsRequest request = new NcsRequest();
        request.app_key = "app_key";
        request.codec = "opus_nb";
        request.app_id = "app_id";
        request.user_id = "user_id";
        request.device_id = "device_id";
        request.subscriber_id = "subscriber_id";
        request.transaction_id = "transaction_id";

        testState = 2;
        StartSessionTranslator translator = new StartSessionTranslator(orchestrator, request);
        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }

        translator.commandCleanup();
        testState = 0;

        verify(mockAsync,atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void execute_jwt()
            throws IOException, InterruptedException
    {
        when(orchestrator.getWebApiFeatures().getConfigs().getString(AUTH_TYPE.getKey(), AUTH_TYPE.getDefaultValue().toString())).thenReturn(Ncs7InterfaceConfig.ValidationTypes.JWS.toString());

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
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\"\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"code\": 1302,\n" +
                            "                \"message\": \"StartSession service failed.\"\n" +
                            "            }\n" +
                            "        ]\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

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

                    Thread.yield(); //wait here

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        NcsRequest request = new NcsRequest();
        request.app_key = "app_key";
        request.codec = "opus_nb";
        request.app_id = "app_id";
        request.user_id = "user_id";
        request.device_id = "device_id";
        request.subscriber_id = "subscriber_id";
        request.transaction_id = "transaction_id";

        testState = 2;
        StartSessionTranslator translator = new StartSessionTranslator(orchestrator, request);
        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }

        translator.commandCleanup();
        testState = 0;

        verify(mockAsync,atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void execute_ima_adpcm()
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
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"success\"\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

                    orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                            "    \"type\": \"result\",\n" +
                            "    \"command\": {\n" +
                            "        \"id\": \"27\",\n" +
                            "        \"name\": \"startSession\"\n" +
                            "    },\n" +
                            "    \"result\": {\n" +
                            "        \"final\": true,\n" +
                            "        \"status\": \"error\",\n" +
                            "        \"errors\": [\n" +
                            "            {\n" +
                            "                \"code\": 1302,\n" +
                            "                \"message\": \"StartSession service failed.\"\n" +
                            "            }\n" +
                            "        ]\n" +
                            "    }\n" +
                            "}"));

                    Thread.yield(); //wait here

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

                    Thread.yield(); //wait here

                    while (testState == 2) {
                        Thread.yield(); //wait here
                    }
                });
        t.start();

        NcsRequest request = new NcsRequest();
        request.app_key = "app_key";
        request.codec = "ima_adpcm";
        request.app_id = "app_id";
        request.user_id = "user_id";
        request.device_id = "device_id";
        request.subscriber_id = "subscriber_id";
        request.transaction_id = "transaction_id";

        testState = 2;
        StartSessionTranslator translator = new StartSessionTranslator(orchestrator, request);
        try {
            translator.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }

        translator.commandCleanup();
        testState = 0;

        verify(mockAsync,atLeastOnce()).sendText(anyString());
        t.interrupt();
        t.join(10000);
    }

    @Test
    public void testExecuteWithClientMetadata()
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

            orchestrator.webapiMessageBufferQueue.offer(Map.entry(Orchestrator.MessageType.MESSAGE,"{\n" +
                                                        "    \"type\": \"result\",\n" +
                                                        "    \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                                                        "    \"command\": {\n" +
                                                        "        \"id\": \"27\",\n" +
                                                        "        \"name\": \"startSession\"\n" +
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
                                                        "        \"id\": \"27\",\n" +
                                                        "        \"name\": \"startSession\"\n" +
                                                        "    },\n" +
                                                        "    \"result\": {\n" +
                                                        "        \"final\": true,\n" +
                                                        "        \"status\": \"error\"\n" +
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
        thread.start();

        NcsRequest request = new NcsRequest();
        request.app_key = "app_key";
        request.codec = "opus_nb";
        request.app_id = "app_id";
        request.user_id = "user_id";
        request.device_id = "device_id";
        request.subscriber_id = "subscriber_id";
        request.transaction_id = "transaction_id";
        request.context_tag = "context_tag";
        request.locale = "locale";
        request.application_name = "application_name";
        request.application = "application";
        request.field_id = "field_id";
        request.audio_source = "audio_source";
        request.location = "location";
        request.client_hardware_model = "client_hardware_model";
        request.uid = "uid";
        request.application_state_id = "application_state_id";
        request._IS_LEGACY_CLIENT_ = "TRUE";
        request.type = 5;
        request.buffer_id = 3333333;
        request.lcd_height = 1;
        request.lcd_width = 1;
        request.pdx_version = "1";
        request.client_os_version = "1.0";
        request.original_session_id = "test";
        request.carrier = "test_carrier";
        request.sdk_version = "1.0";
        request.script_version = "1.0";
        request.client_version = "1.0";
        request.region = "US";
        request.new_session_id  = "test_new";

        testState = 2;
        StartSessionTranslator translator = new StartSessionTranslator(orchestrator, request);
        try {
            translator.execute();
        } catch (Throwable t) {
            System.out.println("Got another typecast exception.");
            t.printStackTrace();
            fail();
        }

        translator.commandCleanup();
        testState = 0;

        verify(mockAsync,atLeastOnce()).sendText(anyString());
        thread.interrupt();
        thread.join(10000);
    }

    @Test
    public void testExecuteWithDataCollectionParameters()
    {
        NcsRequest request = new NcsRequest();

        request.datacollection_prompt_index = 1;
        request.datacollection_name = "datacollection_name";
        request.datacollection_prompt = "datacollection_prompt";
        request.datacollection_prompt_type = "datacollection_prompt_type";
        request.datacollection_participant_id = "datacollection_participant_id";

        StartSessionTranslator translator = new StartSessionTranslator(orchestrator, request);

        Map<String, Object> clientMetadataMap = translator.populateClientMetadata(request);
        assertEquals(request.datacollection_prompt_index, Integer.valueOf((String) clientMetadataMap.get("datacollection_prompt_index")));
        assertEquals(request.datacollection_name, clientMetadataMap.get("datacollection_name"));
        assertEquals(request.datacollection_prompt, clientMetadataMap.get("datacollection_prompt"));
        assertEquals(request.datacollection_prompt_type, clientMetadataMap.get("datacollection_prompt_type"));
        assertEquals(request.datacollection_participant_id, clientMetadataMap.get("datacollection_participant_id"));
    }
}
