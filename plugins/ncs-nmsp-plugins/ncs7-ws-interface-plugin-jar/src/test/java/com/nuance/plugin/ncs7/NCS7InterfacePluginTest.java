package com.nuance.plugin.ncs7;

import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NCS7InterfacePluginTest {


    @Test
    public void testPluginMethods() {
        NCS7InterfacePlugin plugin = new NCS7InterfacePlugin();
        assertEquals("ncs7($|/|/.*)", plugin.getUrlPattern());
    }

    @Test
    public void onSocketOpen() throws IOException {
        NCS7InterfacePlugin plugin = new NCS7InterfacePlugin();
        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint mockWebapi = mock(IWebApiEndpoint.class);
        when(mockFeatures.getWebApiEndpoint(any())).thenReturn(mockWebapi);
        Session mockSession = mock(Session.class);

        plugin.onInitPlugin(mockFeatures);

        assertTrue(plugin.onSocketOpen(mockSession));
        assertTrue(plugin.onSocketError(mockSession, new IOException()));
        assertTrue(plugin.onSocketClosed(mockSession, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "test")));
    }

    @Test
    public void onSocketMessage() throws IOException {
        NCS7InterfacePlugin plugin = new NCS7InterfacePlugin();
        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint mockWebapi = mock(IWebApiEndpoint.class);
        when(mockFeatures.getWebApiEndpoint(any())).thenReturn(mockWebapi);
        Session mockSession = mock(Session.class);
        RemoteEndpoint.Async mockAsync = mock (RemoteEndpoint.Async.class);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);

        plugin.onInitPlugin(mockFeatures);

        assertTrue(plugin.onSocketOpen(mockSession));
        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"connect\",\n" +
                "  \"user_id\" : \"aaaaa\",\n" +
                "  \"codec\" : \"audio/L16;rate=16000\",\n" +
                "  \"app_id\" : \"NMDPTRIAL_carlos_araujo_nuance_com_20200115T142407\",\n" +
                "  \"app_key\" : \"87D804396828430F76BC4067790665E82C26DB722537865B2083F914CB7950413665C39BE67740781FCA73F24A0E4DCF40CEB40F0D862B98B8258AD90D853678\",\n" +
                "  \"device_id\" : \"Win32_GoogleInc._en-US\",\n" +
                "  \"phone_model\" : \"nuance_internal_mixjsapp\",\n" +
                "  \"phone_number\" : \"aaaaa\"\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_begin\",\n" +
                "  \"transaction_id\" : 4,\n" +
                "  \"language\" : \"eng-USA\",\n" +
                "  \"codec\" : \"audio/L16;rate=16000\",\n" +
                "  \"dictation_type\" : \"nma_dm_main\",\n" +
                "  \"command\" : \"NMDP_ASR_CMD\"\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_parameter\",\n" +
                "  \"transaction_id\" : 4,\n" +
                "  \"parameter_name\" : \"REQUEST_INFO\",\n" +
                "  \"parameter_type\" : \"dictionary\",\n" +
                "  \"dictionary\" : {\n" +
                "    \"result_delivery\" : \"final\"\n" +
                "  }\n" +
                "}");
        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_parameter\",\n" +
                "  \"transaction_id\" : 4,\n" +
                "  \"parameter_name\" : \"AUDIO_INFO\",\n" +
                "  \"parameter_type\" : \"audio\",\n" +
                "  \"audio_id\" : 2\n" +
                "}");
        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_end\",\n" +
                "  \"transaction_id\" : 4\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"audio_begin\",\n" +
                "  \"audio_id\" : 2\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"audio\",\n" +
                "  \"audio_id\" : 2\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"audio_end\",\n" +
                "  \"audio_id\" : 2\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"should_not_be_implemented\",\n" +
                "  \"audio_id\" : 2\n" +
                "}");

        plugin.onSocketBinary(mockSession, "testBlob".getBytes());
    }

    @Test
    public void onSocketMessage_ttsNoQueryEnd() throws IOException {
        NCS7InterfacePlugin plugin = new NCS7InterfacePlugin();
        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        IWebApiEndpoint mockWebapi = mock(IWebApiEndpoint.class);
        when(mockFeatures.getWebApiEndpoint(any())).thenReturn(mockWebapi);
        Session mockSession = mock(Session.class);
        RemoteEndpoint.Async mockAsync = mock(RemoteEndpoint.Async.class);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsync);

        plugin.onInitPlugin(mockFeatures);

        assertTrue(plugin.onSocketOpen(mockSession));
        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"connect\",\n" +
                "  \"app_id\" : \"companyName_appNameDTV\",\n" +
                "  \"app_key\" : \"xxx\"\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_begin\",\n" +
                "  \"transaction_id\" : 1,\n" +
                "  \"command\" : \"NMDP_TTS_CMD\",\n" +
                "  \"language\" : \"en-us\",\n" +
                "  \"tts_voice\" : \"Evan\",\n" +
                "  \"codec\" : \"audio/L16;rate=8000\"\n" +
                "}");

        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_parameter\",\n" +
                "  \"transaction_id\" : 1,\n" +
                "  \"parameter_name\" : \"TEXT_TO_READ\",\n" +
                "  \"parameter_type\" : \"dictionary\",\n" +
                "  \"dictionary\" : {\n" +
                "    \"audio_id\" : 789,\n" +
                "    \"tts_input\" : \"how is it going today my friend\",\n" +
                "    \"tts_type\" : \"text\"\n" +
                "  }\n" +
                "}");
        plugin.onSocketMessage(mockSession, "{\n" +
                "  \"message\" : \"query_end\",\n" +
                "  \"transaction_id\" : 1\n" +
                "}");
    }
}