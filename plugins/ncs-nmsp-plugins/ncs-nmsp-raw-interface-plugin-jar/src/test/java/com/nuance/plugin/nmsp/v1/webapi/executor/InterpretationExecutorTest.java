package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class InterpretationExecutorTest {
    WebAPIOrchestrator mockOrchestrator;
    CombinedConfiguration mockConfig;
    private ISession mockSession;
    private IWebApiEndpoint mockEndpoint;

    @Before
    public void initTest() {
        mockSession = mock(ISession.class);

        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        PdxInterfaceConfig.setDefaultValues(config);

        mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(config);

        IWebApiFeatures testFeatures = new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() {
                return mockConfig;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return mockConfig;
            }

            @Override
            public void close() { }

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
                return new IWebApiEndpoint() {
                    @Override
                    public void onOpen(javax.websocket.Session session) throws IOException {

                    }

                    @Override
                    public void onMessage(javax.websocket.Session session, String message) throws IOException {

                    }

                    @Override
                    public void onBinary(javax.websocket.Session session, byte[] buffer) throws IOException {

                    }

                    @Override
                    public void onPong(javax.websocket.Session session, PongMessage pongMessage) throws IOException {

                    }

                    @Override
                    public void onError(javax.websocket.Session session, Throwable throwable) throws IOException {

                    }

                    @Override
                    public void onClose(javax.websocket.Session session, CloseReason reason) throws IOException {

                    }

                    @Override
                    public ISession getActiveSession() {
                        return mockSession;
                    }

                    @Override
                    public ISession getWebapiSession(String sessionID) {
                        return mockSession;
                    }
                };
            }
        };

        mockOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockOrchestrator.getWebApiFeatures()).thenReturn(testFeatures);
        mockEndpoint = mock(IWebApiEndpoint.class);
        when(mockOrchestrator.getWebApiEndpoint()).thenReturn(mockEndpoint);
        when(mockEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockEndpoint.getWebapiSession(any())).thenReturn(mockSession);
    }

    @Test
    public void execute() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        nmspSession.setFromCodec(ICodecResource.Codec.PCM16_16K);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("audio_id", 1);
        dict.put("appserver_data", new PdxDictionary());
        dict.put("nlu_nbest_interpretations", 1);
        ((PdxDictionary)dict.get("appserver_data")).put("text_input", "test");

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);
        queryInfo.put("dictation_type", "dtv");
        queryInfo.put("dictation_language", "eng-USA");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);
        when(mockSession.getSessionConfig()).thenReturn(configs);

        // tenant config
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.topic", "DTV");
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.speechDomain", "websearch");

        when(mockOrchestrator.getSessionConfigs()).thenReturn(configs);
        when(mockOrchestrator.getAudioBufferQueue()).thenReturn(new LinkedBlockingQueue<>());
        message.setQueryBegin(queryInfo);

        InterpretationExecutor interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationExecutor.execute();
        PdxDictionary messageDict = (PdxDictionary) message.getQueryBeginInfo().get("dict");
        assertEquals(1, messageDict.get("nlu_nbest_interpretations"));
        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void onResponse() throws PdxException {
        HierarchicalConfiguration<ImmutableNode> config = new BaseHierarchicalConfiguration();

        config.addProperty("fr_fr", 20);
        when(mockConfig.configurationAt(PdxInterfaceConfig.Property.CONFIDENCE_DYNAMIC_RANGE_LANGUAGE_TUNING.getKey())).thenReturn(config);
        when(mockConfig.getString("apiVersion")).thenReturn("test");

        doReturn(mockConfig).when(mockSession).getSessionConfig();
        doReturn(mockConfig).when(mockOrchestrator).getSessionConfigs();

        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        nmspSession.setDictationLanguage("fr_fr");
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        query.put("context_tag", "tag");
        message.setQueryBegin(query);

        InterpretationExecutor interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationExecutor.onResponse("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"c30c630f-9eec-4dbd-b37c-bd38269af957\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"interpretation\",\n" +
                "        \"id\": \"NMSPPlugin_ID_1320\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"success\",\n" +
                "        \"interpretations\": [\n" +
                "            {\n" +
                "                \"concepts\": [\n" +
                "                    {\n" +
                "                        \"concept\": \"COFFEE_TYPE\",\n" +
                "                        \"value\": \"americano\",\n" +
                "                        \"literal\": \"americano\",\n" +
                "                        \"begin\": 16,\n" +
                "                        \"end\": 25\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"concept\": \"COFFEE_SIZE\",\n" +
                "                        \"value\": \"lg\",\n" +
                "                        \"literal\": \"large\",\n" +
                "                        \"begin\": 10,\n" +
                "                        \"end\": 15\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"confidence\": 1,\n" +
                "                \"literal\": \"Give me a large americano\",\n" +
                "                \"intent\": \"ORDER_COFFEE\",\n" +
                "                \"origin\": \"GRAMMAR\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bestInterpretation\": {\n" +
                "            \"concepts\": [\n" +
                "                {\n" +
                "                    \"concept\": \"COFFEE_TYPE\",\n" +
                "                    \"value\": \"americano\",\n" +
                "                    \"literal\": \"americano\",\n" +
                "                    \"begin\": 16,\n" +
                "                    \"end\": 25\n" +
                "                },\n" +
                "                {\n" +
                "                    \"concept\": \"COFFEE_SIZE\",\n" +
                "                    \"value\": \"lg\",\n" +
                "                    \"literal\": \"large\",\n" +
                "                    \"begin\": 10,\n" +
                "                    \"end\": 15\n" +
                "                }\n" +
                "            ],\n" +
                "            \"confidence\": 1,\n" +
                "            \"literal\": \"Give me a large americano\",\n" +
                "            \"intent\": \"ORDER_COFFEE\",\n" +
                "            \"origin\": \"GRAMMAR\"\n" +
                "        }\n" +
                "    }\n" +
                "}");
        interpretationExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"27\",\n" +
                "      \"name\":\"interpretation\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"noMatch\"\n" +
                "   }\n" +
                "}");
        interpretationExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationExecutor.onResponse("{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"id\":\"27\",\n" +
                "      \"name\":\"interpretation\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"error\",\n" +
                "      \"errors\":[\n" +
                "         {\n" +
                "            \"code\":1800,\n" +
                "            \"message\":\"Interpretation failed.\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}");
        interpretationExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationExecutor.onResponse("{\n" +
                "     \"type\":\"result\",\n" +
                "     \"sessionId\":\"cff17d31-042a-42c6-947c-64d3c9a59638\",\n" +
                "     \"command\":{\n" +
                "         \"name\":\"interpretation\",\n" +
                "         \"id\":\"21\"\n" +
                "     },\n" +
                "     \"result\":{\n" +
                "         \"final\":true,\n" +
                "         \"status\":\"cancelled\"\n" +
                "     }\n" +
                "}");
        interpretationExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void onBinary() {
    }

    @Test
    public void onClose() {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        speechRecognitionExecutor.onClose(mock(CloseReason.class));
    }

    @Test
    public void testTermination() {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationExecutor interpretationExecutor = new InterpretationExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        interpretationExecutor.onClose(null);

        interpretationExecutor.cancelCommand();
        assertTrue(interpretationExecutor.isTerminated());
    }
}