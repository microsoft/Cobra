package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class InterpretationWithSpeechRecognitionExecutorTest {
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
        doReturn(mockEndpoint).when(mockOrchestrator).getWebApiEndpoint();
        doReturn("test").when(mockOrchestrator).getWebAPISessionID();
        doReturn(mockSession).when(mockOrchestrator).getActiveSession();

        ICommand mockCommand = mock(ICommand.class);
        doReturn(mockCommand).when(mockSession).getCommand();

        doReturn(mockSession).when(mockEndpoint).getActiveSession();
        doReturn(mockSession).when(mockEndpoint).getWebapiSession(any());
    }

    @Test
    public void execute() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        nmspSession.setFromCodec(ICodecResource.Codec.PCM16_16K);

        String dictationLanguage = "eng-USA";
        nmspSession.setDictationLanguage(dictationLanguage);

        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("audio_id", 1);

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);
        queryInfo.put("context_tag", "A539_C1");
        queryInfo.put("dictation_language", dictationLanguage);
        queryInfo.put("command", "NDSP_ASR_APP_CMD");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        config.addProperty("maxTranscriptions", -1);
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);
        when(mockSession.getSessionConfig()).thenReturn(configs);

        // tenant config
        config.addProperty("contextTagToTopicMap.A539_C1.eng-USA.topic", "DTV");
        config.addProperty("contextTagToTopicMap.A539_C1.eng-USA.speechDomain", "websearch");

        when(mockOrchestrator.getSessionConfigs()).thenReturn(configs);
        when(mockOrchestrator.getAudioBufferQueue()).thenReturn(new LinkedBlockingQueue<>());
        message.setQueryBegin(queryInfo);
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationSpeechRecognitionExecutor.execute();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void onBinary() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        interpretationSpeechRecognitionExecutor.onBinary("test".getBytes());
    }

    @Test
    public void onClose() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        interpretationSpeechRecognitionExecutor.onClose(mock(CloseReason.class));
    }

    @Test
    public void sendBinary() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationSpeechRecognitionExecutor.sendBinary("test".getBytes());

        verify(mockOrchestrator, atLeastOnce()).streamToWebApi(any(), eq(false));
    }

    @Test
    public void onResponseSpeechReco() {
        String response = "\n" +
                "\n" +
                "{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"a7111c2c-ef59-4d29-abdf-1e4ee72ee510\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"1\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"success\",\n" +
                "        \"data\": {\n" +
                "            \"transcriptions\": [\n" +
                "                \"I would like to small Americano please\"\n" +
                "            ],\n" +
                "            \"confidence\": 857,\n" +
                "            \"averageWeightedConfidences\": [\n" +
                "                981\n" +
                "            ],\n" +
                "            \"words\": [\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"confidence\": 990,\n" +
                "                        \"word\": \"I\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 982,\n" +
                "                        \"word\": \"would\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 988,\n" +
                "                        \"word\": \"like\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 958,\n" +
                "                        \"word\": \"to\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 979,\n" +
                "                        \"word\": \"small\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 988,\n" +
                "                        \"word\": \"Americano\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 982,\n" +
                "                        \"word\": \"please\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            ]\n" +
                "        },\n" +
                "        \"bestResult\": \"I would like a small Americano please\",\n" +
                "        \"noiseLevel\": {\n" +
                "            \"mean\": 0,\n" +
                "            \"standardDeviation\": 0\n" +
                "        },\n" +
                "        \"verboseResult\": {\n" +
                "            \"resultType\": \"FINAL\",\n" +
                "            \"absStartMs\": 0,\n" +
                "            \"absEndMs\": 2460,\n" +
                "            \"utteranceInfo\": {\n" +
                "                \"durationMs\": 2460,\n" +
                "                \"clippingDurationMs\": 20,\n" +
                "                \"droppedSpeechPackets\": 0,\n" +
                "                \"droppedNonspeechPackets\": 0,\n" +
                "                \"dsp\": {\n" +
                "                    \"snrEstimateDb\": 60,\n" +
                "                    \"level\": 9729,\n" +
                "                    \"numChannels\": 1,\n" +
                "                    \"initialSilenceMs\": 20,\n" +
                "                    \"initialEnergy\": -82.8496,\n" +
                "                    \"finalEnergy\": -82.3921,\n" +
                "                    \"meanEnergy\": 109.208\n" +
                "                }\n" +
                "            },\n" +
                "            \"hypotheses\": [\n" +
                "                {\n" +
                "                    \"averageConfidence\": 0.981,\n" +
                "                    \"rejected\": false,\n" +
                "                    \"formattedText\": \"I would like to small Americano please\",\n" +
                "                    \"minimallyFormattedText\": \"I would like to small Americano please\",\n" +
                "                    \"words\": [\n" +
                "                        {\n" +
                "                            \"text\": \"I\",\n" +
                "                            \"confidence\": 0.99,\n" +
                "                            \"startMs\": 20,\n" +
                "                            \"endMs\": 120,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"would\",\n" +
                "                            \"confidence\": 0.982,\n" +
                "                            \"startMs\": 120,\n" +
                "                            \"endMs\": 220,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"like\",\n" +
                "                            \"confidence\": 0.988,\n" +
                "                            \"startMs\": 220,\n" +
                "                            \"endMs\": 460,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"to\",\n" +
                "                            \"confidence\": 0.958,\n" +
                "                            \"startMs\": 460,\n" +
                "                            \"endMs\": 540,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"small\",\n" +
                "                            \"confidence\": 0.979,\n" +
                "                            \"startMs\": 540,\n" +
                "                            \"endMs\": 840,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"Americano\",\n" +
                "                            \"confidence\": 0.988,\n" +
                "                            \"startMs\": 840,\n" +
                "                            \"endMs\": 1500,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"please\",\n" +
                "                            \"confidence\": 0.982,\n" +
                "                            \"startMs\": 1500,\n" +
                "                            \"endMs\": 1840,\n" +
                "                            \"silenceAfterWordMs\": 0\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"encryptedTokenization\": \"avnakF6FIEsTseKHKTtLH\\/j9xP7gD00GX8JuYOFgwZBn\\/iFzYwEmitdUijJ3FwaU6ixZzh2SjE2BK+SbmEWBn2zvy\\/9l2EhRd3R5lPnl0luLFxSzHyG2JnLFeXRaTqsghve\\/7crDnZISXLbZXRx18t3i3SXYuDgXkM6uNf6wDP1AHi4DWITviJumh5Ztp2c4ShJ7zdFaLjuYvNbC\\/CnCIIZ1wrPZtigoUVHNgYsDA5u5qdDBHoA2orb7Nx1wFKZUD9s3GGWSyvCJk6HoQuOceGXsWxWo8c8A0uNGrLnqmQpHBEBlTAXods7mULTf\\/Vc5fyODY6JSx2alfjf++esgzA==hwmBeXiBDJghA2vrA+ggHCNjbjzvoAjg7DXZbwJDr1u0y1PriB+5PzJwlRP1SxYgvJmk9TLApYCuje0xp7Gk0r2uwoEWeXCUwylfLx2l\\/m4JVdIR03EvuOS+bufqh3kpUYw2eHBEkGfiXBPJbBI6YsIXIDPBDhJP62bpIBwmhPMnBs1wlKWrrMx5Q1pU8P2dlfOGH+ZzWhwrz+fap\\/BSjrZkbLBDnWTPkSL8NVQ9pZkJaG\\/VmbbhtqlQJgflf9PKkpqzoKiQJOknIqrnQS9qJqtMpKsKk0yaNDICvIJulTy5uE45vABcuEl6\\/Ms4fhGeKt82yQeTzfdKZtkNj9iphrY5LjSu1L2LkjEHaZqzGbhLho\\/R7HSfawe60zo8Hi1kvMp7qIJoaP+TjekvKoSUjzMmw5te2wcDmjXTrM7B\\/\\/7uVsNq\\/pioLLpKwH2dcVku\\/KWsommGkJvN2\\/Gzjk3M4+w3AR859ooQudXBf4S83jNJ526W+V6vESj5a1gZvBFp2Bt\\/VF0HDO3XfYK7YITt6iL5a9FMRN3N6XP2WxhsaOPKkeC69Av6ptsRPch89Smv10iiN03vMGrfvmo0yzSIlcD4TE87brltyGlsn3YnoSK6DB1eKz9EYGQ\\/XD1x0kAb4XTCurhC\\/1da4N72zs3Jt1aY+THIsGUuzvRILXaH56RlZpaRWQcJpSE0Dq5wIxUlB\\/N34y5YtkHQ7fukW8kECm9dvCsxUmgZgrEUR63j2ARqcD1p7VSq8QF8KHSSb7gHe0xZxkuH8Xw1QsFeWoR4EQ==\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"dataPack\": {\n" +
                "                \"language\": \"eng-USA\",\n" +
                "                \"topic\": \"GEN\",\n" +
                "                \"version\": \"4.4.0\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n";

        HierarchicalConfiguration<ImmutableNode> config = new BaseHierarchicalConfiguration();

        config.addProperty("fr_fr", 20);
        when(mockConfig.configurationAt(PdxInterfaceConfig.Property.CONFIDENCE_DYNAMIC_RANGE_LANGUAGE_TUNING.getKey())).thenReturn(config);

        when(mockSession.getSessionConfig()).thenReturn(mockConfig);

        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        nmspSession.setDictationLanguage("fr_fr");
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");

        PdxDictionary dict = new PdxDictionary();
        dict.put("enable_intermediate_response", 1);
        query.put("dict", dict);

        message.setQueryBegin(query);
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\"type\":\"commandChainResult\",\"sessionId\":\"a7111c2c-ef59-4d29-abdf-1e4ee72ee510\",\"result\":{\"final\":false,\"status\":\"success\",\"nextCommand\":\"interpretation\"}}";
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\n" +
                "\"type\": \"result\",\n" +
                "\"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "\"command\": {\n" +
                "\"name\": \"speechRecognition\",\n" +
                "\"id\": \"2\"\n" +
                "},\n" +
                "\"result\": {\n" +
                "\"final\": true,\n" +
                "\"status\": \"noInput\",\n" +
                "\"noiseLevel\": {\n" +
                "\"mean\": 67.82,\n" +
                "\"standardDeviation\": 24.48\n" +
                "}\n" +
                "}\n" +
                "}\n";
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\n" +
                "\"type\": \"result\",\n" +
                "\"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "\"command\": {\n" +
                "\"name\": \"speechRecognition\",\n" +
                "\"id\": \"2\"\n" +
                "},\"result\": {\n" +
                "\"final\": true,\n" +
                "\"status\": \"noMatch\",\n" +
                "\"noiseLevel\": {\n" +
                "\"mean\": 67.82,\n" +
                "\"standardDeviation\": 24.48\n" +
                "}\n" +
                "}\n" +
                "}\n";
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\"type\":\"result\",\n" +
                "\"sessionId\":\"cff17d31-042a-42c6-947c-64d3c9a59638\",\n" +
                "\"command\":{\n" +
                "\"name\":\"speechRecognition\",\n" +
                "\"id\":\"42\"\n" +
                "},\n" +
                "\"result\":{\n" +
                "\"final\":true,\n" +
                "\"status\":\"cancelled\",\n" +
                "\"noiseLevel\": {\n" +
                "\"mean\": 67.82,\n" +
                "\"standardDeviation\": 24.48\n" +
                "}\n" +
                "}\n" +
                "}\n";
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"dd8b98c1-6376-4326-95be-f339cd4dbd6e\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"42\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"error\",\n" +
                "        \"errors\": [\n" +
                "            {\n" +
                "                \"message\": \"No active command.\",\n" +
                "                \"code\": 1202\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        interpretationSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void onResponseInterpretation() throws PdxException {
        String response = "\n" +
                "\n" +
                "{\n" +
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
                "}\n";

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

        InterpretationWithSpeechRecognitionExecutor interpretationWithSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationWithSpeechRecognitionExecutor.onResponse(response);
        interpretationWithSpeechRecognitionExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\"type\":\"result\",\n" +
                "\"sessionId\":\"cff17d31-042a-42c6-947c-64d3c9a59638\",\n" +
                "\"command\":{\n" +
                "\"name\":\"interpretation\",\n" +
                "\"id\":\"42\"\n" +
                "},\n" +
                "\"result\":{\n" +
                "\"final\":true,\n" +
                "\"status\":\"cancelled\",\n" +
                "\"noiseLevel\": {\n" +
                "\"mean\": 67.82,\n" +
                "\"standardDeviation\": 24.48\n" +
                "}\n" +
                "}\n" +
                "}\n";
        interpretationWithSpeechRecognitionExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        interpretationWithSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        response ="{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"dd8b98c1-6376-4326-95be-f339cd4dbd6e\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"interpretation\",\n" +
                "        \"id\": \"42\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"error\",\n" +
                "        \"errors\": [\n" +
                "            {\n" +
                "                \"message\": \"No active command.\",\n" +
                "                \"code\": 1202\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        interpretationWithSpeechRecognitionExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        interpretationWithSpeechRecognitionExecutor.onResponse(response);
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void testOnStreamingDone() throws IOException{
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        nmspSession.setDictationLanguage("eng-USA");
        nmspSession.setNmaid("fakeNmaid");
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationSpeechRecognitionExecutor.onStreamingDone();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));

    }

    @Test
    public void testOnStreamingDone_noInfo() throws IOException{
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        interpretationSpeechRecognitionExecutor.onStreamingDone();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));

    }

    @Test
    public void noMatchNoPartialResults()
            throws IOException
    {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        PdxDictionary pdxDictionary = new PdxDictionary();
        pdxDictionary.put("enable_intermediate_response", 0);
        Map<String, Object> queryInfo = Map.of("command", "NMDP_ASR_APP_CMD", "dict", pdxDictionary);
        InterpretationWithSpeechRecognitionExecutor interpretationSpeechRecognitionExecutor = new InterpretationWithSpeechRecognitionExecutor(nmspSession,
                new HashMap<>(queryInfo), "testSession");

        WebApiResponse webApiResponse = mock(WebApiResponse.class);
        when(webApiResponse.isResult()).thenReturn(true);
        when(webApiResponse.isSuccess()).thenReturn(false);
        when(webApiResponse.isNoMatch()).thenReturn(true);
        interpretationSpeechRecognitionExecutor.doSpeechRecoResponse(webApiResponse);
        interpretationSpeechRecognitionExecutor.onStreamingDone();
        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }
}