package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyShort;
import static org.mockito.Mockito.*;

public class SpeechSynthesisExecutorTest {
    WebApiJSONConfiguration config;
    CombinedConfiguration configs;
    @Before
    public void initTest() {
        config = new WebApiJSONConfiguration();
        configs = new CombinedConfiguration();
        configs.addConfiguration(config);
    }

    @Test
    public void execute() throws IOException {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        when(mockOrchestrator.getSessionConfigs()).thenReturn(configs);
        nmspSession.setToCodec(ICodecResource.Codec.PCM16_16K);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("tts_type", "text");
        dict.put("audio_id", 1);

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);

        message.setQueryBegin(queryInfo);
        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.execute();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void executeMp3() throws IOException {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        IWebApiFeatures mockFeatures = mock(IWebApiFeatures.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockFeatures.getConfigs()).thenReturn(mockConfig);
        when(mockOrchestrator.getWebApiFeatures()).thenReturn(mockFeatures);
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        nmspSession.setToCodec(ICodecResource.Codec.MP3_128KBPS);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("tts_type", "text");
        dict.put("audio_id", 1);

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);

        message.setQueryBegin(queryInfo);
        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.execute();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void convertTTSLanguage() throws IOException {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        nmspSession.setToCodec(ICodecResource.Codec.PCM16_16K);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("tts_type", "text");
        dict.put("audio_id", 1);
        dict.put("tts_text_genre", "typeA");

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);

        queryInfo.put("tts_language", "en-us");
        IWebApiEndpoint mockWebapiEndpoint = mock(IWebApiEndpoint.class);
        doReturn(mockWebapiEndpoint).when(mockOrchestrator).getWebApiEndpoint();

        ISession mockSession = mock(ISession.class);
        doReturn(mockSession).when(mockWebapiEndpoint).getActiveSession();

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        config.addProperty("vocsMapping.Ava.language", "no-match");
        config.addProperty("vocsMapping.Ava.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Ava.name", "Evan");


        when(mockSession.getSessionConfig()).thenReturn(configs);
        when(mockOrchestrator.getSessionConfigs()).thenReturn(configs);

        message.setQueryBegin(queryInfo);
        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.execute();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void onResponseSucess() {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        message.setQueryBegin(query);

        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.onResponse("{\n" +
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
                        "}");
        speechSynthesisExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.onResponse("{\n" +
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
                "}");

        speechSynthesisExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.onResponse("{\n" +
                "    \"type\": \"directive\",\n" +
                "    \"sessionId\": \"cff17d31-042a-42c6-947c-64d3c9a59638\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechSynthesis\",\n" +
                "        \"id\": \"42\"\n" +
                "    },\n" +
                "    \"directive\": {\n" +
                "        \"name\": \"cancel\"\n" +
                "    }\n" +
                "}");
        speechSynthesisExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void onBinary() {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");

        Channel mockChannel = mock(Channel.class);
        when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        nmspSession.setToCodec(ICodecResource.Codec.ALAW);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        message.setQueryBegin(query);
        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechSynthesisExecutor.onBinary("testBinary".getBytes());

        verify(mockChannel, atLeastOnce()).write(any());
    }

    @Test
    public void testTermination() {
        WebAPIOrchestrator mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");

        Channel mockChannel = mock(Channel.class);
        when(mockChannel.isActive()).thenReturn(true);
        Session nmspSession = new Session(mockChannel, mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        message.setQueryBegin(query);
        SpeechSynthesisExecutor speechSynthesisExecutor = new SpeechSynthesisExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        speechSynthesisExecutor.onClose(null);

        ISession mockSession = mock(ISession.class);
        doReturn(mockSession).when(mockOrchestrator).getActiveSession();
        ICommand mockCommand = mock(ICommand.class);
        doReturn(mockCommand).when(mockSession).getCommand();

        speechSynthesisExecutor.cancelCommand();
        assertTrue(speechSynthesisExecutor.isTerminated());
    }

    @Test
    public void testTTSGenre() {
        PdxDictionary dict = new PdxDictionary();
        Session nmspSession = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));

        SpeechSynthesisExecutor executor = new SpeechSynthesisExecutor(nmspSession, dict, "testSession");
        assertNull(executor.getTtsGenre(dict));

        dict.put("tts_text_genre", "typeA");
        assertEquals(dict.get("tts_text_genre"), executor.getTtsGenre(dict));

        dict.put("tts_genre", "typeB");
        assertEquals(dict.get("tts_text_genre"), executor.getTtsGenre(dict));

        dict.remove("tts_text_genre");
        assertEquals(dict.get("tts_genre"), executor.getTtsGenre(dict));
    }
}