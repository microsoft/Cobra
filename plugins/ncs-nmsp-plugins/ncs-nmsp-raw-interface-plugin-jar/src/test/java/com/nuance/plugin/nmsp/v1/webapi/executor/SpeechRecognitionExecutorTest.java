package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs.utils.ICodecResource;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class SpeechRecognitionExecutorTest {
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
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        PdxDictionary dict = new PdxDictionary();
        dict.put("audio_id", 1);
        dict.put("enable_profanity_filtering", 1);

        PdxDictionary queryInfo = new PdxDictionary();
        queryInfo.put("dict", dict);
        queryInfo.put("dictation_type", "dtv");
        queryInfo.put("dictation_language", "eng-USA");
        queryInfo.put("context_tag", "ACContext_C1");

        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        config.addProperty("maxTranscriptions", -1);
        configs.addConfiguration(config);
        when(mockSession.getSessionConfig()).thenReturn(configs);

        // tenant config
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.topic", "DTV");
        config.addProperty("dictationTypeToTopicMap.dtv.eng-USA.speechDomain", "websearch");

        when(mockOrchestrator.getSessionConfigs()).thenReturn(configs);
        when(mockOrchestrator.getAudioBufferQueue()).thenReturn(new LinkedBlockingQueue<>());
        message.setQueryBegin(queryInfo);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.execute();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void onBinary() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        speechRecognitionExecutor.onBinary("test".getBytes());
    }

    @Test
    public void onClose() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        speechRecognitionExecutor.onClose(mock(CloseReason.class));
    }

    @Test
    public void sendBinary() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.sendBinary("test".getBytes());

        verify(mockOrchestrator, atLeastOnce()).streamToWebApi(any(), eq(false));
    }

    @Test
    public void onResponse() {
        String response = "{\n" +
                "   \"type\":\"result\",\n" +
                "   \"sessionId\":\"be5c7d62-3275-429d-b442-3827030ccf06\",\n" +
                "   \"command\":{\n" +
                "      \"name\":\"speechRecognition\",\n" +
                "      \"id\":\"NMSPPlugin_ID_1132\"\n" +
                "   },\n" +
                "   \"result\":{\n" +
                "      \"final\":true,\n" +
                "      \"status\":\"success\",\n" +
                "      \"data\":{\n" +
                "         \"transcriptions\":[\n" +
                "            \"I'm telling you buy go away\",\n" +
                "            \"I'm telling you bye go away\",\n" +
                "            \"I'm telling you by go away\",\n" +
                "            \"I'm telling you bye Goaway\",\n" +
                "            \"I'm going to buy go away\",\n" +
                "            \"I'm telling you by Goaway\",\n" +
                "            \"I'm telling you buy Goaway\"\n" +
                "         ],\n" +
                "         \"confidence\":477,\n" +
                "         \"averageWeightedConfidences\":[\n" +
                "            477,\n" +
                "            0,\n" +
                "            0,\n" +
                "            0,\n" +
                "            0,\n" +
                "            0,\n" +
                "            0\n" +
                "         ],\n" +
                "         \"words\":[\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":958.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":880.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":942.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":678.0,\n" +
                "                  \"word\":\"buy\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":828.0,\n" +
                "                  \"word\":\"go\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":831.0,\n" +
                "                  \"word\":\"away\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":958.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":876.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":935.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":555.0,\n" +
                "                  \"word\":\"bye\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":764.0,\n" +
                "                  \"word\":\"go\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":829.0,\n" +
                "                  \"word\":\"away\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":958.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":874.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":931.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":520.0,\n" +
                "                  \"word\":\"by\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":732.0,\n" +
                "                  \"word\":\"go\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":816.0,\n" +
                "                  \"word\":\"away\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":961.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":878.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":927.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":512.0,\n" +
                "                  \"word\":\"bye\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":273.0,\n" +
                "                  \"word\":\"Goaway\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":926.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":461.0,\n" +
                "                  \"word\":\"going\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":616.0,\n" +
                "                  \"word\":\"to\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":694.0,\n" +
                "                  \"word\":\"buy\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":764.0,\n" +
                "                  \"word\":\"go\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":821.0,\n" +
                "                  \"word\":\"away\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":961.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":877.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":923.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":476.0,\n" +
                "                  \"word\":\"by\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":239.0,\n" +
                "                  \"word\":\"Goaway\"\n" +
                "               }\n" +
                "            ],\n" +
                "            [\n" +
                "               {\n" +
                "                  \"confidence\":961.0,\n" +
                "                  \"word\":\"I'm\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":883.0,\n" +
                "                  \"word\":\"telling\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":927.0,\n" +
                "                  \"word\":\"you\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":580.0,\n" +
                "                  \"word\":\"buy\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"confidence\":224.0,\n" +
                "                  \"word\":\"Goaway\"\n" +
                "               }\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      \"bestResult\":\"I'm telling you buy go away\",\n" +
                "      \"noiseLevel\":{\n" +
                "         \"mean\":0.0,\n" +
                "         \"standardDeviation\":0.0\n" +
                "      },\n" +
                "      \"verboseResult\":{\n" +
                "         \"resultType\":\"FINAL\",\n" +
                "         \"absStartMs\":0.0,\n" +
                "         \"absEndMs\":3360.0,\n" +
                "         \"utteranceInfo\":{\n" +
                "            \"durationMs\":3360.0,\n" +
                "            \"clippingDurationMs\":390.0,\n" +
                "            \"droppedSpeechPackets\":0.0,\n" +
                "            \"droppedNonspeechPackets\":0.0,\n" +
                "            \"dsp\":{\n" +
                "               \"snrEstimateDb\":19.0,\n" +
                "               \"level\":24065.0,\n" +
                "               \"numChannels\":1.0,\n" +
                "               \"initialSilenceMs\":520.0,\n" +
                "               \"initialEnergy\":-44.5059,\n" +
                "               \"finalEnergy\":-13.8844,\n" +
                "               \"meanEnergy\":142.078\n" +
                "            }\n" +
                "         },\n" +
                "         \"hypotheses\":[\n" +
                "            {\n" +
                "               \"confidence\":0.477,\n" +
                "               \"averageConfidence\":0.855,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm telling you buy go away\",\n" +
                "               \"minimallyFormattedText\":\"I'm telling you buy go away\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.958,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"telling\",\n" +
                "                     \"confidence\":0.88,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"you\",\n" +
                "                     \"confidence\":0.942,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1200.0,\n" +
                "                     \"silenceAfterWordMs\":260.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"buy\",\n" +
                "                     \"confidence\":0.678,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"go\",\n" +
                "                     \"confidence\":0.828,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2140.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"away\",\n" +
                "                     \"confidence\":0.831,\n" +
                "                     \"startMs\":2140.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            {\n" +
                "               \"averageConfidence\":0.831,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm telling you bye go away\",\n" +
                "               \"minimallyFormattedText\":\"I'm telling you bye go away\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.958,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"telling\",\n" +
                "                     \"confidence\":0.876,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"you\",\n" +
                "                     \"confidence\":0.935,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1200.0,\n" +
                "                     \"silenceAfterWordMs\":260.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"bye\",\n" +
                "                     \"confidence\":0.555,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"go\",\n" +
                "                     \"confidence\":0.764,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2140.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"away\",\n" +
                "                     \"confidence\":0.829,\n" +
                "                     \"startMs\":2140.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            {\n" +
                "              \"rejected\": true\n"+
                "            },\n" +
                "            {\n" +
                "               \"averageConfidence\":0.641,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm telling you bye Goaway\",\n" +
                "               \"minimallyFormattedText\":\"I'm telling you bye Goaway\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.961,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"telling\",\n" +
                "                     \"confidence\":0.878,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"you\",\n" +
                "                     \"confidence\":0.927,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1200.0,\n" +
                "                     \"silenceAfterWordMs\":260.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"bye\",\n" +
                "                     \"confidence\":0.512,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"Goaway\",\n" +
                "                     \"confidence\":0.273,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            {\n" +
                "               \"averageConfidence\":0.688,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm going to buy go away\",\n" +
                "               \"minimallyFormattedText\":\"I'm going to buy go away\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.926,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"going\",\n" +
                "                     \"confidence\":0.461,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"to\",\n" +
                "                     \"confidence\":0.616,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1180.0,\n" +
                "                     \"silenceAfterWordMs\":280.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"buy\",\n" +
                "                     \"confidence\":0.694,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"go\",\n" +
                "                     \"confidence\":0.764,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2140.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"away\",\n" +
                "                     \"confidence\":0.821,\n" +
                "                     \"startMs\":2140.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            {\n" +
                "               \"averageConfidence\":0.624,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm telling you by Goaway\",\n" +
                "               \"minimallyFormattedText\":\"I'm telling you by Goaway\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.961,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"telling\",\n" +
                "                     \"confidence\":0.877,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"you\",\n" +
                "                     \"confidence\":0.923,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1200.0,\n" +
                "                     \"silenceAfterWordMs\":260.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"by\",\n" +
                "                     \"confidence\":0.476,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"Goaway\",\n" +
                "                     \"confidence\":0.239,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            {\n" +
                "               \"averageConfidence\":0.634,\n" +
                "               \"rejected\":false,\n" +
                "               \"formattedText\":\"I'm telling you buy Goaway\",\n" +
                "               \"minimallyFormattedText\":\"I'm telling you buy Goaway\",\n" +
                "               \"words\":[\n" +
                "                  {\n" +
                "                     \"text\":\"I'm\",\n" +
                "                     \"confidence\":0.961,\n" +
                "                     \"startMs\":520.0,\n" +
                "                     \"endMs\":700.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"telling\",\n" +
                "                     \"confidence\":0.883,\n" +
                "                     \"startMs\":700.0,\n" +
                "                     \"endMs\":1060.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"you\",\n" +
                "                     \"confidence\":0.927,\n" +
                "                     \"startMs\":1060.0,\n" +
                "                     \"endMs\":1200.0,\n" +
                "                     \"silenceAfterWordMs\":260.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"buy\",\n" +
                "                     \"confidence\":0.58,\n" +
                "                     \"startMs\":1460.0,\n" +
                "                     \"endMs\":1600.0,\n" +
                "                     \"silenceAfterWordMs\":400.0\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"text\":\"Goaway\",\n" +
                "                     \"confidence\":0.224,\n" +
                "                     \"startMs\":2000.0,\n" +
                "                     \"endMs\":2440.0,\n" +
                "                     \"silenceAfterWordMs\":0.0\n" +
                "                  }\n" +
                "               ]\n" +
                "            }\n" +
                "         ],\n" +
                "         \"dataPack\":{\n" +
                "            \"language\":\"eng-USA\",\n" +
                "            \"topic\":\"GEN\",\n" +
                "            \"version\":\"4.1.0\"\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}";

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
        message.setQueryBegin(query);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse(response);
        speechRecognitionExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");

        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void onResponseBinaryResult() {
        String response = "{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"cf3387c6-9a1c-4f00-b912-d4c19c6676b8\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"NMSPPlugin_ID_9370\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"data\": {\n" +
                "      \"transcriptions\": [\n" +
                "        \"I'm telling you buy go away\",\n" +
                "        \"I'm telling you bye go away\"\n" +
                "      ],\n" +
                "      \"confidence\": 595,\n" +
                "      \"averageWeightedConfidences\": [\n" +
                "        925,\n" +
                "        922\n" +
                "      ],\n" +
                "      \"words\": [\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 960.0,\n" +
                "            \"word\": \"I'm\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 924.0,\n" +
                "            \"word\": \"telling\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 973.0,\n" +
                "            \"word\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 788.0,\n" +
                "            \"word\": \"buy\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 964.0,\n" +
                "            \"word\": \"go\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 949.0,\n" +
                "            \"word\": \"away\"\n" +
                "          }\n" +
                "        ],\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 960.0,\n" +
                "            \"word\": \"I'm\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 926.0,\n" +
                "            \"word\": \"telling\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 972.0,\n" +
                "            \"word\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 778.0,\n" +
                "            \"word\": \"bye\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 946.0,\n" +
                "            \"word\": \"go\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 944.0,\n" +
                "            \"word\": \"away\"\n" +
                "          }\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bestResult\": \"I'm telling you buy go away\",\n" +
                "    \"noiseLevel\": {\n" +
                "      \"mean\": 0.0,\n" +
                "      \"standardDeviation\": 0.0\n" +
                "    },\n" +
                "    \"verboseResult\": {\n" +
                "      \"hypotheses\": [\n" +
                "        {\n" +
                "          \"averageConfidence\": 0.925,\n" +
                "          \"confidence\": 0.595,\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"startMs\": 520.0,\n" +
                "              \"endMs\": 700.0,\n" +
                "              \"confidence\": 0.96,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"I'm\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 700.0,\n" +
                "              \"endMs\": 1060.0,\n" +
                "              \"confidence\": 0.924,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"telling\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"rejected\": true\n"+
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 1440.0,\n" +
                "              \"endMs\": 1600.0,\n" +
                "              \"confidence\": 0.788,\n" +
                "              \"silence_after_wordMs\": 400.0,\n" +
                "              \"text\": \"buy\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 2000.0,\n" +
                "              \"endMs\": 2140.0,\n" +
                "              \"confidence\": 0.964,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"go\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 2140.0,\n" +
                "              \"endMs\": 2440.0,\n" +
                "              \"confidence\": 0.949,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"away\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"averageConfidence\": 0.922,\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"startMs\": 520.0,\n" +
                "              \"endMs\": 700.0,\n" +
                "              \"confidence\": 0.96,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"I'm\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 700.0,\n" +
                "              \"endMs\": 1060.0,\n" +
                "              \"confidence\": 0.926,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"telling\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 1060.0,\n" +
                "              \"endMs\": 1200.0,\n" +
                "              \"confidence\": 0.972,\n" +
                "              \"silence_after_wordMs\": 240.0,\n" +
                "              \"text\": \"you\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 1440.0,\n" +
                "              \"endMs\": 1600.0,\n" +
                "              \"confidence\": 0.778,\n" +
                "              \"silence_after_wordMs\": 400.0,\n" +
                "              \"text\": \"bye\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 2000.0,\n" +
                "              \"endMs\": 2140.0,\n" +
                "              \"confidence\": 0.946,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"go\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"startMs\": 2140.0,\n" +
                "              \"endMs\": 2440.0,\n" +
                "              \"confidence\": 0.944,\n" +
                "              \"silence_after_wordMs\": 0.0,\n" +
                "              \"text\": \"away\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"absStartMs\": 0.0,\n" +
                "      \"utteranceInfo\": {\n" +
                "        \"dsp\": {\n" +
                "          \"numChannels\": 1.0,\n" +
                "          \"level\": 24065.0,\n" +
                "          \"initialSilenceMs\": 520.0,\n" +
                "          \"snrEstimateDb\": 19.0,\n" +
                "          \"initialEnergy\": -44.5059,\n" +
                "          \"finalEnergy\": -13.8844,\n" +
                "          \"meanEnergy\": 142.078\n" +
                "        },\n" +
                "        \"clippingDurationMs\": 390.0,\n" +
                "        \"droppedNonspeechPackets\": 0.0,\n" +
                "        \"durationMs\": 3360.0,\n" +
                "        \"droppedSpeechPackets\": 0.0\n" +
                "      },\n" +
                "      \"absEndMs\": 3360.0\n" +
                "    }\n" +
                "  }\n" +
                "}";

        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);

        PdxDictionary dict = new PdxDictionary();
        dict.put("binary_results", 1);

        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        query.put("dict", dict);
        message.setQueryBegin(query);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse(response);

        verify(mockOrchestrator, atLeastOnce()).sendError(any(), any());
    }

    @Test
    public void onResponsePartialBinaryResult() {
        String response = "{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"a7266228-4d20-4e49-8150-31ccea323219\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"NMSPPlugin_ID_8435\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": false,\n" +
                "    \"status\": \"success\",\n" +
                "    \"data\": {\n" +
                "      \"transcriptions\": [\n" +
                "        \"I'm done\"\n" +
                "      ],\n" +
                "      \"confidence\": 0,\n" +
                "      \"averageWeightedConfidences\": [\n" +
                "        0\n" +
                "      ],\n" +
                "      \"words\": [\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 0.0,\n" +
                "            \"word\": \"I'm\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 0.0,\n" +
                "            \"word\": \"done\"\n" +
                "          }\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bestResult\": \"I'm done\",\n" +
                "    \"noiseLevel\": {\n" +
                "      \"mean\": 0.0,\n" +
                "      \"standardDeviation\": 0.0\n" +
                "    },\n" +
                "    \"verboseResult\": {\n" +
                "      \"utterance_complete\": false,\n" +
                "      \"completion_cause\": \"SUCCESS\",\n" +
                "      \"absStart\": 0.0,\n" +
                "      \"absEnd\": 1390.0,\n" +
                "      \"status\": \"IN-PROGRESS\",\n" +
                "      \"nBest\": [\n" +
                "        {\n" +
                "          \"rejected\": false,\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"silence_after_word\": 0.0,\n" +
                "              \"start\": 520.0,\n" +
                "              \"end\": 700.0,\n" +
                "              \"text\": \"I'm\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"silence_after_word\": 0.0,\n" +
                "              \"start\": 700.0,\n" +
                "              \"end\": 920.0,\n" +
                "              \"text\": \"done\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"formattedText\": \"I'm done\",\n" +
                "          \"minimallyFormattedText\": \"I'm done\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "\n";

        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);

        PdxDictionary dict = new PdxDictionary();
        dict.put("binary_results", 1);
        dict.put("enable_intermediate_response", 1);

        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        query.put("dict", dict);
        message.setQueryBegin(query);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse(response);

        verify(mockOrchestrator, atLeastOnce()).sendError(any(), any());
    }

    @Test
    public void onEndSession () {
        String response = "{\n" +
                "   \"type\":\"command\",\n" +
                "   \"sessionId\":\"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
                "   \"command\":{\n" +
                "      \"name\":\"endSession\",\n" +
                "      \"id\":\"45\"\n" +
                "   }\n" +
                "}";
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse(response);

        verify(mockOrchestrator, atMost(0)).setWebAPISessionID("");
    }

    @Test
    public void onStreamingDone() throws IOException {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onStreamingDone();

        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }

    @Test
    public void testTermination() {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);
        message.setQueryBegin(new PdxDictionary());
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");

        speechRecognitionExecutor.onClose(null);

        speechRecognitionExecutor.cancelCommand();
        assertTrue(speechRecognitionExecutor.isTerminated());
    }

    @Test
    public void testFailedResponses() {
        when(mockOrchestrator.getAudioInfo()).thenReturn(new PDXDictionary());
        when(mockOrchestrator.getWebAPISessionID()).thenReturn("testID");
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        IClientHandler mockClientHelper = mock(IClientHandler.class);
        nmspSession.setClient(mockClientHelper);
        BCPBeginMessage message = new BCPBeginMessage((short) 20, "test".getBytes(), (short) 1);

        PdxDictionary dict = new PdxDictionary();
        dict.put("binary_results", 1);
        dict.put("enable_intermediate_response", 1);

        PdxDictionary query = new PdxDictionary();
        query.put("command", "NVC_ASR_CMD");
        query.put("dict", dict);
        message.setQueryBegin(query);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"speechRecognition\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "         \"final\": true,\n" +
                "         \"status\": \"error\",\n" +
                "         \"errors\": [\n" +
                "             {\n" +
                "                 \"code\": 1600,\n" +
                "                 \"message\": \"speechRecognition failed.\"\n" +
                "             },\n" +
                "             {\n" +
                "                 \"code\": 1601,\n" +
                "                 \"message\": \"speechRecognition command couldn't be executed.\"\n" +
                "             }\n" +
                "         ]\n" +
                "    }\n" +
                "}");
        verify(mockOrchestrator, atLeastOnce()).sendError(any(), any());


        speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"2\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"noInput\",\n" +
                "        \"noiseLevel\": {\n" +
                "            \"mean\": 67.82,\n" +
                "            \"standardDeviation\": 24.48\n" +
                "        }\n" +
                "    }\n" +
                "}");
        speechRecognitionExecutor.onResponse("{\"type\":\"commandChainResult\",\"sessionId\":\"01220510-b6d4-4490-808b-bc8ee95c0e65\",\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());

        speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession, message.getQueryBeginInfo(), "testSession");
        speechRecognitionExecutor.onResponse("{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"5fc721dc-07e7-4cbd-be74-4f217126d0d1\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"2\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"noMatch\",\n" +
                "        \"noiseLevel\": {\n" +
                "            \"mean\": 67.82,\n" +
                "            \"standardDeviation\": 24.48\n" +
                "        }\n" +
                "    }\n" +
                "}");
        verify(mockClientHelper, atLeastOnce()).sendData(any(), anyShort(), anyLong(), any());
    }

    @Test
    public void test_isPartialResults() {
        PdxDictionary dict = new PdxDictionary();
        assertFalse(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("enable_intermediate_response", 0);
        assertFalse(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
        assertFalse(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "UtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("enable_intermediate_response", 1);
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
        assertFalse(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "UtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict = new PdxDictionary();
        dict.put("intermediate_response_mode", "UtteranceDetectionWithCompleteRecognition");
        assertFalse(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "UtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));

        dict.put("intermediate_response_mode", "NoUtteranceDetectionWithPartialRecognition");
        assertTrue(SpeechRecognitionExecutor.isPartialResultEnabled(dict));
    }

    @Test
    public void noMatchNoPartialResults()
            throws IOException
    {
        Session nmspSession = new Session(mock(Channel.class), mockOrchestrator);
        PdxDictionary pdxDictionary = new PdxDictionary();
        pdxDictionary.put("enable_intermediate_response", 0);
        Map<String, Object> queryInfo = Map.of("command", "NMDP_ASR_CMD", "dict", pdxDictionary);
        SpeechRecognitionExecutor speechRecognitionExecutor = new SpeechRecognitionExecutor(nmspSession,
                new HashMap<>(queryInfo), "testSession");

        WebApiResponse webApiResponse = mock(WebApiResponse.class);
        when(webApiResponse.isResult()).thenReturn(true);
        when(webApiResponse.isForCommand(anyString())).thenReturn(true);
        when(webApiResponse.isSuccess()).thenReturn(false);
        when(webApiResponse.isNoMatch()).thenReturn(true);
        speechRecognitionExecutor.onResponse(webApiResponse);
        speechRecognitionExecutor.onStreamingDone();
        verify(mockOrchestrator, atLeastOnce()).sendToWebapi(any(), anyString(), eq(false));
    }
}