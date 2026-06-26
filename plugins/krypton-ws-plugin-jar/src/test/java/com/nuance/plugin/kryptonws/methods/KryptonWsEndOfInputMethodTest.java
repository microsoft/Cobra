package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsEventBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static com.nuance.plugin.kryptonws.methods.KryptonWsRecognizeMethodTest.RECOGNIZE_METHOD;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsEndOfInputMethodTest
{

    private String endOfInputMethodJson = "  {\n" +
            "    \"method\": \"EndOfInput\",\n" +
            "    \"requestId\": \"b84051e3a33f2094-00002\",\n" +
            "    \"sessionId\": \"b84051e3a33f2094\",\n" +
            "    \"version\": \"1.0\"\n" +
            "  }";

    String successfulWebapiFinalResult = "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"2e255b75-97c9-42a1-9c6e-275f9dc1767c\",\n" +
            "    \"command\": {\n" +
            "        \"name\": \"speechRecognition\",\n" +
            "        \"id\": \"6\"\n" +
            "    },\n" +
            "    \"result\": {\n" +
            "        \"final\": true,\n" +
            "        \"status\": \"success\",\n" +
            "        \"data\": {\n" +
            "            \"transcriptions\": [\n" +
            "                \"We are expecting 10 cm overnight and the winds blowing hard\",\n" +
            "                \"We are expecting 10 cm overnight and the winds blowing hard\",\n" +
            "                \"We are expecting 10 cm overnight and the winds are blowing hard\",\n" +
            "                \"We are expecting 10 cm overnight and the winds blowing hard\",\n" +
            "                \"We are expecting 10 cm overnight and the winds blowing hard\"\n" +
            "            ],\n" +
            "            \"confidence\": 90,\n" +
            "            \"averageWeightedConfidences\": [\n" +
            "                870,\n" +
            "                866,\n" +
            "                638,\n" +
            "                705,\n" +
            "                687\n" +
            "            ],\n" +
            "            \"words\": [\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"confidence\": 920,\n" +
            "                        \"word\": \"We\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 878,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 868,\n" +
            "                        \"word\": \"expecting\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 827,\n" +
            "                        \"word\": \"10\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 702,\n" +
            "                        \"word\": \"cm\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 819,\n" +
            "                        \"word\": \"overnight\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 931,\n" +
            "                        \"word\": \"and\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 895,\n" +
            "                        \"word\": \"the\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 784,\n" +
            "                        \"word\": \"winds\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 962,\n" +
            "                        \"word\": \"blowing\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 945,\n" +
            "                        \"word\": \"hard\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"confidence\": 920,\n" +
            "                        \"word\": \"We\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 878,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 868,\n" +
            "                        \"word\": \"expecting\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 827,\n" +
            "                        \"word\": \"10\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 702,\n" +
            "                        \"word\": \"cm\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 819,\n" +
            "                        \"word\": \"overnight\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 932,\n" +
            "                        \"word\": \"and\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 884,\n" +
            "                        \"word\": \"the\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 752,\n" +
            "                        \"word\": \"winds\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 961,\n" +
            "                        \"word\": \"blowing\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 942,\n" +
            "                        \"word\": \"hard\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"confidence\": 878,\n" +
            "                        \"word\": \"We\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 844,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 567,\n" +
            "                        \"word\": \"expecting\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 521,\n" +
            "                        \"word\": \"10\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 477,\n" +
            "                        \"word\": \"cm\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 623,\n" +
            "                        \"word\": \"overnight\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 721,\n" +
            "                        \"word\": \"and\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 746,\n" +
            "                        \"word\": \"the\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 463,\n" +
            "                        \"word\": \"winds\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 101,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 805,\n" +
            "                        \"word\": \"blowing\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 882,\n" +
            "                        \"word\": \"hard\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"confidence\": 884,\n" +
            "                        \"word\": \"We\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 847,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 578,\n" +
            "                        \"word\": \"expecting\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 532,\n" +
            "                        \"word\": \"10\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 488,\n" +
            "                        \"word\": \"cm\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 633,\n" +
            "                        \"word\": \"overnight\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 734,\n" +
            "                        \"word\": \"and\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 781,\n" +
            "                        \"word\": \"the\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 562,\n" +
            "                        \"word\": \"winds\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 905,\n" +
            "                        \"word\": \"blowing\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 899,\n" +
            "                        \"word\": \"hard\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                [\n" +
            "                    {\n" +
            "                        \"confidence\": 878,\n" +
            "                        \"word\": \"We\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 844,\n" +
            "                        \"word\": \"are\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 567,\n" +
            "                        \"word\": \"expecting\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 521,\n" +
            "                        \"word\": \"10\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 477,\n" +
            "                        \"word\": \"cm\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 623,\n" +
            "                        \"word\": \"overnight\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 721,\n" +
            "                        \"word\": \"and\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 743,\n" +
            "                        \"word\": \"the\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 498,\n" +
            "                        \"word\": \"winds\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 891,\n" +
            "                        \"word\": \"blowing\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"confidence\": 889,\n" +
            "                        \"word\": \"hard\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            ]\n" +
            "        },\n" +
            "        \"bestResult\": \"We are expecting 10 cm overnight and the winds blowing hard\",\n" +
            "        \"noiseLevel\": {\n" +
            "            \"mean\": 13.18,\n" +
            "            \"standardDeviation\": 15.38\n" +
            "        },\n" +
            "        \"verboseResult\": {\n" +
            "            \"hypotheses\": [\n" +
            "                {\n" +
            "                    \"averageConfidence\": 0.841,\n" +
            "                    \"confidence\": 0.09,\n" +
            "                    \"words\": [\n" +
            "                        {\n" +
            "                            \"startMs\": 1120,\n" +
            "                            \"endMs\": 1300,\n" +
            "                            \"confidence\": 0.92,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"We\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 1300,\n" +
            "                            \"endMs\": 1400,\n" +
            "                            \"confidence\": 0.878,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"are\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 1400,\n" +
            "                            \"endMs\": 2020,\n" +
            "                            \"confidence\": 0.868,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"expecting\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 2020,\n" +
            "                            \"endMs\": 2300,\n" +
            "                            \"confidence\": 0.827,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"10\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 2300,\n" +
            "                            \"endMs\": 2880,\n" +
            "                            \"confidence\": 0.702,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"cm\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 2880,\n" +
            "                            \"endMs\": 3380,\n" +
            "                            \"confidence\": 0.819,\n" +
            "                            \"silence_after_wordMs\": 20,\n" +
            "                            \"text\": \"overnight\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 3400,\n" +
            "                            \"endMs\": 3540,\n" +
            "                            \"confidence\": 0.931,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"and\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 3540,\n" +
            "                            \"endMs\": 3660,\n" +
            "                            \"confidence\": 0.895,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"the\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 3660,\n" +
            "                            \"endMs\": 4060,\n" +
            "                            \"confidence\": 0.784,\n" +
            "                            \"silence_after_wordMs\": 60,\n" +
            "                            \"text\": \"winds\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 4060,\n" +
            "                            \"endMs\": 4460,\n" +
            "                            \"confidence\": 0.962,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"blowing\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"startMs\": 4460,\n" +
            "                            \"endMs\": 4800,\n" +
            "                            \"confidence\": 0.945,\n" +
            "                            \"silence_after_wordMs\": 0,\n" +
            "                            \"text\": \"hard\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ],\n" +
            "            \"absStartMs\": 0,\n" +
            "            \"utteranceInfo\": {\n" +
            "                \"dsp\": {\n" +
            "                    \"numChannels\": 1,\n" +
            "                    \"level\": 14849,\n" +
            "                    \"initialSilenceMs\": 1100,\n" +
            "                    \"snrEstimateDb\": 55,\n" +
            "                    \"initialEnergy\": -75.4257,\n" +
            "                    \"finalEnergy\": -25.1784,\n" +
            "                    \"meanEnergy\": 52.4557\n" +
            "                },\n" +
            "                \"clippingDurationMs\": 0,\n" +
            "                \"droppedNonspeechPackets\": 0,\n" +
            "                \"durationMs\": 5760,\n" +
            "                \"droppedSpeechPackets\": 0\n" +
            "            },\n" +
            "            \"absEndMs\": 5760\n" +
            "        }\n" +
            "    }\n" +
            "}";


    String noActiveCommandString = "{\n" +
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

    String errorCommandString = "{\n" +
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
            "                \"message\": \"Some error.\",\n" +
            "                \"code\": 1337\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    String cancelledCommandString = "{\n" +
            "    \"type\": \"result\",\n" +
            "    \"sessionId\": \"dd8b98c1-6376-4326-95be-f339cd4dbd6e\",\n" +
            "    \"command\": {\n" +
            "        \"name\": \"speechRecognition\",\n" +
            "        \"id\": \"42\"\n" +
            "    },\n" +
            "    \"result\": {\n" +
            "        \"final\": true,\n" +
            "        \"status\": \"cancelled\",\n" +
            "        \"errors\": [\n" +
            "            {\n" +
            "                \"message\": \"Was cancelled.\",\n" +
            "                \"code\": 1337\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    String webapiErrorResult = "{\n" +
            "     \"type\": \"result\",\n" +
            "     \"sessionId\": \"873315e5-d8bd-45a1-aa35-7cb47827e6ec\",\n" +
            "     \"command\": {\n" +
            "         \"id\": \"7\",\n" +
            "         \"name\": \"speechSynthesis\"\n" +
            "     },\n" +
            "     \"result\": {\n" +
            "          \"final\": true,\n" +
            "          \"status\": \"error\",\n" +
            "          \"errors\": [\n" +
            "              {\n" +
            "                  \"code\": 1500,\n" +
            "                  \"message\": \"SpeechSynthesis failed.\"\n" +
            "              },\n" +
            "              {\n" +
            "                  \"code\": 1501,\n" +
            "                  \"message\": \"SpeechSynthesis command couldn't be executed.\"\n" +
            "              }\n" +
            "          ]\n" +
            "     }\n" +
            "}";

    Session session = new WebSocketHttpSession(new FakeHttpSession());
    KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
    KryptonWsWebapiResponseListener responseListener = new KryptonWsWebapiResponseListener();
    KryptonWsResponseSender responseSender = new KryptonWsResponseSender(session);
    KryptonWsEndOfInputMethod
            endOfInputMethod = new KryptonWsEndOfInputMethod(new JSONObject(endOfInputMethodJson), kryptonWsSession);
    KryptonWsResponseSender mockSender = mock(KryptonWsResponseSender.class);

    @Before
    public void setUp() {
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        kryptonWsSession.setWebapiSessionId("test");
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);
        endOfInputMethod.recognizeMethod = mock(KryptonWsRecognizeMethod.class);
    }

    @Test
    public void processTest() throws IOException {
        responseListener.setCurrentMethod(new KryptonWsRecognizeMethod(null, mock(KryptonWsSession.class)));

        kryptonWsSession.setWebapiResponseListener(responseListener)
                        .setKryptonWsWsSession(session)
                        .setKryptonWsResponseSender(mockSender)
                        .setKryptonWsSessionId("abc123");

        ISession mockSession = mock(ISession.class);
        ICommand mockCommand = mock(ICommand.class);
        IWebApiEndpoint mockEndpoint = mock(IWebApiEndpoint.class);
        KryptonWsEndOfInputMethod endOfInputMethodMock = spy(endOfInputMethod);
        kryptonWsSession.setWebApiEndpoint(mockEndpoint);
        kryptonWsSession.setWebapiSessionId("test");
        when(mockEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn("2");
        endOfInputMethodMock.process();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(endOfInputMethodMock).sendToWebapi(captor.capture());

        JSONObject stopJson = new JSONObject(captor.getValue());
        JSONObject commandJson = stopJson.getJSONObject("command");
        assertEquals("directive", stopJson.get("type"));
        assertEquals("speechRecognition", commandJson.get("name"));
        assertEquals("2", commandJson.getString("id"));
        assertEquals("stop", stopJson.getJSONObject("directive").getString("name"));

        assertEquals("EndOfInput", endOfInputMethod.getMethodName());
        assertEquals("b84051e3a33f2094-00002", endOfInputMethod.getKryptonWsRequestId());

    }

    @Test
    public void processWebapiResponseTestSuccess()  {
        kryptonWsSession.setKryptonWsResponseSender(mockSender);

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        kryptonWsSession.setWebapiSessionId("test");
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(mockConfig.getBoolean("autoEnd", true)).thenReturn(true);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        endOfInputMethod.processWebapiResponse(successfulWebapiFinalResult);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender, times(3)).sendResponse(argumentCaptor.capture());

        JSONObject endOfInputResponse = new JSONObject(argumentCaptor.getAllValues().get(0));
        JSONObject finalResultEvent = new JSONObject(argumentCaptor.getAllValues().get(1));
        JSONObject recognizeResponse = new JSONObject(argumentCaptor.getAllValues().get(2));

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"EndOfInput\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), endOfInputResponse.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Event\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"event\": \"FinalResult\",\n" +
                "  \"data\": {\n" +
                "    \"utterance_complete\": true,\n" +
                "    \"absStart\": 0,\n" +
                "    \"absEnd\": 5760,\n" +
                "    \"completion_cause\": \"END-OF-STREAM\",\n" +
                "    \"utterance_info\": {\n" +
                "      \"duration\": 5760,\n" +
                "      \"dropped_speech_packets\": 0,\n" +
                "      \"mean_energy\": 52.4557,\n" +
                "      \"initial_silence\": 1100,\n" +
                "      \"level\": 14849,\n" +
                "      \"initial_energy\": -75.4257,\n" +
                "      \"final_energy\": -25.1784,\n" +
                "      \"stereo\": false,\n" +
                "      \"clipping_duration\": 0,\n" +
                "      \"snr_estimate\": 55,\n" +
                "      \"dropped_non_speech_packets\": 0\n" +
                "    },\n" +
                "    \"nBest\": [\n" +
                "      {\n" +
                "        \"averageConfidence\": 841,\n" +
                "        \"confidence\": 90,\n" +
                "        \"words\": [\n" +
                "          {\n" +
                "            \"confidence\": 920,\n" +
                "            \"start\": 1120,\n" +
                "            \"end\": 1300,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"We\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 878,\n" +
                "            \"start\": 1300,\n" +
                "            \"end\": 1400,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 868,\n" +
                "            \"start\": 1400,\n" +
                "            \"end\": 2020,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"expecting\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 827,\n" +
                "            \"start\": 2020,\n" +
                "            \"end\": 2300,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"10\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 702,\n" +
                "            \"start\": 2300,\n" +
                "            \"end\": 2880,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"cm\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 819,\n" +
                "            \"start\": 2880,\n" +
                "            \"end\": 3380,\n" +
                "            \"silence_after_wordMs\": 20,\n" +
                "            \"text\": \"overnight\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 931,\n" +
                "            \"start\": 3400,\n" +
                "            \"end\": 3540,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"and\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 895,\n" +
                "            \"start\": 3540,\n" +
                "            \"end\": 3660,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"the\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 784,\n" +
                "            \"start\": 3660,\n" +
                "            \"end\": 4060,\n" +
                "            \"silence_after_wordMs\": 60,\n" +
                "            \"text\": \"winds\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 962,\n" +
                "            \"start\": 4060,\n" +
                "            \"end\": 4460,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"blowing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 945,\n" +
                "            \"start\": 4460,\n" +
                "            \"end\": 4800,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"hard\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"status\": \"COMPLETE\"\n" +
                "  }\n" +
                "}").toMap(), finalResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"Recognize\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), recognizeResponse.toMap());
    }

    @Test
    public void processWebapiResponseTestSuccess_autoEndFalse()  {
        kryptonWsSession.setKryptonWsResponseSender(mockSender);

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        kryptonWsSession.setWebapiSessionId("test");
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(mockConfig.getBoolean("autoEnd", true)).thenReturn(false);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        endOfInputMethod.processWebapiResponse(successfulWebapiFinalResult);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender, times(4)).sendResponse(argumentCaptor.capture());

        JSONObject partialResultEvent = new JSONObject(argumentCaptor.getAllValues().get(0));
        JSONObject endOfInputResponse = new JSONObject(argumentCaptor.getAllValues().get(1));
        JSONObject finalResultEvent = new JSONObject(argumentCaptor.getAllValues().get(2));
        JSONObject recognizeResponse = new JSONObject(argumentCaptor.getAllValues().get(3));

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Event\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"event\": \"PartialResult\",\n" +
                "  \"data\": {\n" +
                "    \"utterance_complete\": true,\n" +
                "    \"absStart\": 0,\n" +
                "    \"absEnd\": 5760,\n" +
                "    \"completion_cause\": \"SUCCESS\",\n" +
                "    \"nBest\": [\n" +
                "      {\n" +
                "        \"averageConfidence\": 841,\n" +
                "        \"confidence\": 90,\n" +
                "        \"words\": [\n" +
                "          {\n" +
                "            \"confidence\": 920,\n" +
                "            \"start\": 1120,\n" +
                "            \"end\": 1300,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"We\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 878,\n" +
                "            \"start\": 1300,\n" +
                "            \"end\": 1400,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 868,\n" +
                "            \"start\": 1400,\n" +
                "            \"end\": 2020,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"expecting\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 827,\n" +
                "            \"start\": 2020,\n" +
                "            \"end\": 2300,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"10\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 702,\n" +
                "            \"start\": 2300,\n" +
                "            \"end\": 2880,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"cm\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 819,\n" +
                "            \"start\": 2880,\n" +
                "            \"end\": 3380,\n" +
                "            \"silence_after_wordMs\": 20,\n" +
                "            \"text\": \"overnight\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 931,\n" +
                "            \"start\": 3400,\n" +
                "            \"end\": 3540,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"and\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 895,\n" +
                "            \"start\": 3540,\n" +
                "            \"end\": 3660,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"the\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 784,\n" +
                "            \"start\": 3660,\n" +
                "            \"end\": 4060,\n" +
                "            \"silence_after_wordMs\": 60,\n" +
                "            \"text\": \"winds\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 962,\n" +
                "            \"start\": 4060,\n" +
                "            \"end\": 4460,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"blowing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 945,\n" +
                "            \"start\": 4460,\n" +
                "            \"end\": 4800,\n" +
                "            \"silence_after_wordMs\": 0,\n" +
                "            \"text\": \"hard\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"status\": \"IN-PROGRESS\"\n" +
                "  }\n" +
                "}").toMap(), partialResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"EndOfInput\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), endOfInputResponse.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Event\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"event\": \"FinalResult\",\n" +
                "  \"data\": {\n" +
                "    \"completion_cause\": \"END-OF-STREAM\",\n" +
                "    \"status\": \"COMPLETE\"\n" +
                "  }\n" +
                "}").toMap(), finalResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"Recognize\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), recognizeResponse.toMap());
    }

    @Test
    public void processWebapiResponseTestSuccess_withInterpretation()
    {
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        kryptonWsSession.setWebapiSessionId("test");
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(mockConfig.getBoolean("autoEnd", true)).thenReturn(true);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        kryptonWsSession
                .setKryptonWsWsSession(session)
                .setKryptonWsSessionId("b84051e3a33f2094")
                .setWebapiResponseListener(responseListener)
                .setKryptonWsResponseSender(mockSender)
                .setKryptonWsParameters(new KryptonWsParameters(null, null));

        KryptonWsRecognizeMethod recognizeMethod  = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);

        endOfInputMethod.recognizeMethod = recognizeMethod;

        responseListener.setCurrentMethod(recognizeMethod);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        endOfInputMethod.processWebapiResponse("{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"53996eb3-0852-4b4a-84e8-4dac35166a2e\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"2\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"statistics\": {\n" +
                "      \"hostInfo\": {\n" +
                "        \"region\": \"ma\",\n" +
                "        \"environment\": \"Prod\",\n" +
                "        \"version\": \"2.31.2-SNAPSHOT\"\n" +
                "      },\n" +
                "      \"startTime\": \"2021/05/14 01:38:52.622\",\n" +
                "      \"endTime\": \"2021/05/14 01:38:59.031\",\n" +
                "      \"firstIntermediateResultTime\": \"2021/05/14 01:38:54.397\",\n" +
                "      \"lastIntermediateResultTime\": \"2021/05/14 01:38:54.493\",\n" +
                "      \"firstAudioBufferReceivedTime\": \"2021/05/14 01:38:53.802\",\n" +
                "      \"lastAudioBufferReceivedTime\": \"2021/05/14 01:38:53.869\",\n" +
                "      \"modelVersion\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\",\n" +
                "      \"directives\": [\n" +
                "        {\n" +
                "          \"name\": \"STOP\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:58.929\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"events\": [\n" +
                "        {\n" +
                "          \"name\": \"startOfSpeech\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:53.817\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"endOfSpeech\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:53.852\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
                "      \"mediaDuration\": 4080,\n" +
                "      \"engineRequests\": {\n" +
                "        \"engine\": \"krypton-grpc\",\n" +
                "        \"dlms\": [\n" +
                "          {\n" +
                "            \"uri\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"language\": \"eng-USA\"\n" +
                "      },\n" +
                "      \"engineResponses\": {\n" +
                "        \"grpcHeaders\": {\n" +
                "          \"date\": \"Fri, 14 May 2021 01:38:54 GMT\",\n" +
                "          \"x-request-id\": \"087f05f6-b271-423c-971e-e1e27e37b542\",\n" +
                "          \"server\": \"envoy\",\n" +
                "          \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
                "          \"content-type\": \"application/grpc\",\n" +
                "          \"accept-encoding\": \"identity,gzip\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"data\": {\n" +
                "      \"transcriptions\": [\n" +
                "        \"Good morning how are you doing today\"\n" +
                "      ],\n" +
                "      \"confidence\": 919,\n" +
                "      \"averageWeightedConfidences\": [\n" +
                "        978\n" +
                "      ],\n" +
                "      \"words\": [\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 984.0,\n" +
                "            \"word\": \"Good\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 988.0,\n" +
                "            \"word\": \"morning\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"how\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"doing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 970.0,\n" +
                "            \"word\": \"today\"\n" +
                "          }\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bestResult\": \"Good morning how are you doing today\",\n" +
                "    \"noiseLevel\": {\n" +
                "      \"mean\": 1.21,\n" +
                "      \"standardDeviation\": 1.28\n" +
                "    },\n" +
                "    \"verboseResult\": {\n" +
                "      \"resultType\": \"FINAL\",\n" +
                "      \"absStartMs\": 0.0,\n" +
                "      \"absEndMs\": 4080.0,\n" +
                "      \"utteranceInfo\": {\n" +
                "        \"durationMs\": 4080.0,\n" +
                "        \"clippingDurationMs\": 0.0,\n" +
                "        \"droppedSpeechPackets\": 0.0,\n" +
                "        \"droppedNonspeechPackets\": 0.0,\n" +
                "        \"dsp\": {\n" +
                "          \"snrEstimateDb\": 57.0,\n" +
                "          \"level\": 4097.0,\n" +
                "          \"numChannels\": 1.0,\n" +
                "          \"initialSilenceMs\": 460.0,\n" +
                "          \"initialEnergy\": -79.9374,\n" +
                "          \"finalEnergy\": -83.1577,\n" +
                "          \"meanEnergy\": 100.651\n" +
                "        }\n" +
                "      },\n" +
                "      \"hypotheses\": [\n" +
                "        {\n" +
                "          \"confidence\": 0.919,\n" +
                "          \"averageConfidence\": 0.978,\n" +
                "          \"rejected\": false,\n" +
                "          \"formattedText\": \"Good morning how are you doing today\",\n" +
                "          \"minimallyFormattedText\": \"Good morning how are you doing today\",\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"text\": \"Good\",\n" +
                "              \"confidence\": 0.984,\n" +
                "              \"startMs\": 460.0,\n" +
                "              \"endMs\": 720.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"morning\",\n" +
                "              \"confidence\": 0.988,\n" +
                "              \"startMs\": 720.0,\n" +
                "              \"endMs\": 1120.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"how\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1120.0,\n" +
                "              \"endMs\": 1260.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"are\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1260.0,\n" +
                "              \"endMs\": 1560.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"you\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1560.0,\n" +
                "              \"endMs\": 1680.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"doing\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1680.0,\n" +
                "              \"endMs\": 2020.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"today\",\n" +
                "              \"confidence\": 0.97,\n" +
                "              \"startMs\": 2020.0,\n" +
                "              \"endMs\": 2280.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            }\n" +
                "          ],\n" +
                "          \"encryptedTokenization\": \"EwBMQ5jYYYtpfx/4EGvr2lq/ZcHkGGZDzjLeETp+hGcEl2SB8UjyJXXcg9p8oIpCyld8U7n15yxGTcwG6VkvA0jeR5NozWoWqkMXnfX8sORiRtzadauRyO7YrZkJ0X5ChjNVNiBw+4QLzjBm9YWTcUhOcMI5bmz4V/cfLoGnI1c+qQCi5lYRDYCepNkoDv46qn4rdNLIN7o4S3lIPYqjCqUoJslJV4+kghdPfd0Qb611HykFztvCLcDn4F0B2GNVKjPcELIf08O0Fxv86cMDcoj/meXOv/r2Nq1qcyRKaepEGeQ9M7RiesOJdLUBZ0Z69Pj+QhAQzT4SNz2zeMp8dA==ZAIhpH+zJzY3eSr2d12TmbgkENAPvlZ9ulSNBj0cXPCsV2EM2Op7aY5lb215TK7qli4yCI2O8rltu4eWM5DxxbD3WXKL+FR6k0dDKmJjrBKYCwPEs5j0XCVE1Cx4SQaoXAOQQPV8yQxN51JwhkkI6x6WWmWmt8g4DzaCe1MLrMmjOb2hFDhEgDa2jn3svT96cMS/LVBmAqMCR3MIl6Flsh94Bx/3KUXn6Pq8i/rNcnz0Ihh6lEamHGmRKCwEWzlmp22gu25B+wRey9AAMY8iFdlYiqCh4vkw30f1VAoKqmPnga9Id8Kb4ISm8zPaNqb77FPVY95J6M/OjlywFREeqUj4BbMA/TkLg5GUeNs/w4ovjQ4zAuiDTWxEsclRc5zDNe0W4uHnVRxJiCmFa7da2795Jl/+pTHLksIYjjpXuO/K0AH4UeSP16uthm8dGDJpeSrmM4yC+R2FVmZz5tVO2QYb85jx1QRbhoPvEBMrcjomW05wL/Ox1D2u31VK7+y6Vbgyz3eZn4U28dXZVYcV8J3IAc/wGjJw2pkcB3D0Ky6VhDbKecMzmTEwz3PiuCpNkMDOyI9BMOsHilLA7KxZ5DBffrnSdHtxRuVVSMbKEVKqYQ3+FlVRwn1wqvCE7tvg1yX2k1KqGYCMN2pwV94curBYlHZt/Dqmj71VYpDfT1lFIbvBhjneScoMD8aPaodFjUEFsi9KN4IkqAkJUyco5B8Doihy7v6CCrXYJG9S13cVbCJDEiQNOcvx2tHRnufickXchapc1pAWRycYWbK15w==\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dataPack\": {\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"version\": \"4.4.0\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");

        endOfInputMethod.processWebapiResponse("{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"53996eb3-0852-4b4a-84e8-4dac35166a2e\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"interpretation\",\n" +
                "    \"id\": \"3\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"statistics\": {\n" +
                "      \"hostInfo\": {\n" +
                "        \"region\": \"ma\",\n" +
                "        \"environment\": \"Dev\",\n" +
                "        \"version\": \"2.31.2-SNAPSHOT\"\n" +
                "      },\n" +
                "      \"startTime\": \"2021/05/14 01:39:04.824\",\n" +
                "      \"endTime\": \"2021/05/14 01:39:05.153\",\n" +
                "      \"modelVersion\": \"urn:nuance-mix:tag:model/context_tag/mix.nlu?=language=eng-USA\",\n" +
                "      \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
                "      \"engineRequests\": {\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"model\": \"urn:nuance-mix:tag:model/context_tag/mix.nlu?=language=eng-USA\",\n" +
                "        \"engine\": \"nlu-grpc\"\n" +
                "      },\n" +
                "      \"engineResponses\": {\n" +
                "        \"grpcHeaders\": {\n" +
                "          \"x-request-id\": \"ecd723aa-5604-96aa-92ac-2cb7cc7df2ee\",\n" +
                "          \"date\": \"Fri, 14 May 2021 01:39:04 GMT\",\n" +
                "          \"server\": \"envoy\",\n" +
                "          \"x-envoy-upstream-service-time\": \"4\",\n" +
                "          \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
                "          \"uber-trace-id\": \"eb967879139dd7613986b798061e072e:9b7780dbfd676196:5ad361440a3ec5ab:1\",\n" +
                "          \"content-type\": \"application/grpc\",\n" +
                "          \"accept-encoding\": \"identity,gzip\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"interpretations\": [\n" +
                "      {\n" +
                "        \"concepts\": [\n" +
                "          {\n" +
                "            \"concept\": \"TIME_OF_DAY\",\n" +
                "            \"literal\": \"morning\",\n" +
                "            \"begin\": 5,\n" +
                "            \"end\": 12\n" +
                "          },\n" +
                "          {\n" +
                "            \"concept\": \"DATE\",\n" +
                "            \"literal\": \"today\",\n" +
                "            \"begin\": 31,\n" +
                "            \"end\": 36\n" +
                "          }\n" +
                "        ],\n" +
                "        \"confidence\": 100,\n" +
                "        \"literal\": \"Good morning how are you doing today\",\n" +
                "        \"intent\": \"GREETINGS\",\n" +
                "        \"origin\": \"GRAMMAR\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"bestInterpretation\": {\n" +
                "      \"concepts\": [\n" +
                "        {\n" +
                "          \"concept\": \"TIME_OF_DAY\",\n" +
                "          \"literal\": \"morning\",\n" +
                "          \"begin\": 5,\n" +
                "          \"end\": 12\n" +
                "        },\n" +
                "        {\n" +
                "          \"concept\": \"DATE\",\n" +
                "          \"literal\": \"today\",\n" +
                "          \"begin\": 31,\n" +
                "          \"end\": 36\n" +
                "        }\n" +
                "      ],\n" +
                "      \"confidence\": 100,\n" +
                "      \"literal\": \"Good morning how are you doing today\",\n" +
                "      \"intent\": \"GREETINGS\",\n" +
                "      \"origin\": \"GRAMMAR\"\n" +
                "    }\n" +
                "  }\n" +
                "}");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender, times(3)).sendResponse(argumentCaptor.capture());

        JSONObject endOfInputResponse = new JSONObject(argumentCaptor.getAllValues().get(0));
        JSONObject finalResultEvent = new JSONObject(argumentCaptor.getAllValues().get(1));
        JSONObject recognizeResponse = new JSONObject(argumentCaptor.getAllValues().get(2));

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"EndOfInput\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), endOfInputResponse.toMap());

        assertEquals(new JSONObject("{\n" +
                "    \"method\": \"Event\",\n" +
                "    \"data\":\n" +
                "    {\n" +
                "        \"utterance_complete\": true,\n" +
                "        \"absEnd\": 4080,\n" +
                "        \"absStart\": 0,\n" +
                "        \"completion_cause\": \"END-OF-STREAM\",\n" +
                "        \"utterance_info\":\n" +
                "        {\n" +
                "            \"duration\": 4080,\n" +
                "            \"dropped_speech_packets\": 0,\n" +
                "            \"mean_energy\": 100.651,\n" +
                "            \"initial_silence\": 460,\n" +
                "            \"level\": 4097,\n" +
                "            \"initial_energy\": -79.9374,\n" +
                "            \"final_energy\": -83.1577,\n" +
                "            \"stereo\": false,\n" +
                "            \"clipping_duration\": 0,\n" +
                "            \"snr_estimate\": 57,\n" +
                "            \"dropped_non_speech_packets\": 0\n" +
                "        },\n" +
                "        \"nBest\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"averageConfidence\": 978,\n" +
                "                \"rejected\": false,\n" +
                "                \"confidence\": 919,\n" +
                "                \"words\":\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 984,\n" +
                "                        \"start\": 460,\n" +
                "                        \"end\": 720,\n" +
                "                        \"text\": \"Good\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 988,\n" +
                "                        \"start\": 720,\n" +
                "                        \"end\": 1120,\n" +
                "                        \"text\": \"morning\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 981,\n" +
                "                        \"start\": 1120,\n" +
                "                        \"end\": 1260,\n" +
                "                        \"text\": \"how\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 981,\n" +
                "                        \"start\": 1260,\n" +
                "                        \"end\": 1560,\n" +
                "                        \"text\": \"are\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 982,\n" +
                "                        \"start\": 1560,\n" +
                "                        \"end\": 1680,\n" +
                "                        \"text\": \"you\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 982,\n" +
                "                        \"start\": 1680,\n" +
                "                        \"end\": 2020,\n" +
                "                        \"text\": \"doing\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"silence_after_word\": 0,\n" +
                "                        \"confidence\": 970,\n" +
                "                        \"start\": 2020,\n" +
                "                        \"end\": 2280,\n" +
                "                        \"text\": \"today\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"formattedText\": \"Good morning how are you doing today\",\n" +
                "                \"tokenization\": \"EwBMQ5jYYYtpfx/4EGvr2lq/ZcHkGGZDzjLeETp+hGcEl2SB8UjyJXXcg9p8oIpCyld8U7n15yxGTcwG6VkvA0jeR5NozWoWqkMXnfX8sORiRtzadauRyO7YrZkJ0X5ChjNVNiBw+4QLzjBm9YWTcUhOcMI5bmz4V/cfLoGnI1c+qQCi5lYRDYCepNkoDv46qn4rdNLIN7o4S3lIPYqjCqUoJslJV4+kghdPfd0Qb611HykFztvCLcDn4F0B2GNVKjPcELIf08O0Fxv86cMDcoj/meXOv/r2Nq1qcyRKaepEGeQ9M7RiesOJdLUBZ0Z69Pj+QhAQzT4SNz2zeMp8dA==ZAIhpH+zJzY3eSr2d12TmbgkENAPvlZ9ulSNBj0cXPCsV2EM2Op7aY5lb215TK7qli4yCI2O8rltu4eWM5DxxbD3WXKL+FR6k0dDKmJjrBKYCwPEs5j0XCVE1Cx4SQaoXAOQQPV8yQxN51JwhkkI6x6WWmWmt8g4DzaCe1MLrMmjOb2hFDhEgDa2jn3svT96cMS/LVBmAqMCR3MIl6Flsh94Bx/3KUXn6Pq8i/rNcnz0Ihh6lEamHGmRKCwEWzlmp22gu25B+wRey9AAMY8iFdlYiqCh4vkw30f1VAoKqmPnga9Id8Kb4ISm8zPaNqb77FPVY95J6M/OjlywFREeqUj4BbMA/TkLg5GUeNs/w4ovjQ4zAuiDTWxEsclRc5zDNe0W4uHnVRxJiCmFa7da2795Jl/+pTHLksIYjjpXuO/K0AH4UeSP16uthm8dGDJpeSrmM4yC+R2FVmZz5tVO2QYb85jx1QRbhoPvEBMrcjomW05wL/Ox1D2u31VK7+y6Vbgyz3eZn4U28dXZVYcV8J3IAc/wGjJw2pkcB3D0Ky6VhDbKecMzmTEwz3PiuCpNkMDOyI9BMOsHilLA7KxZ5DBffrnSdHtxRuVVSMbKEVKqYQ3+FlVRwn1wqvCE7tvg1yX2k1KqGYCMN2pwV94curBYlHZt/Dqmj71VYpDfT1lFIbvBhjneScoMD8aPaodFjUEFsi9KN4IkqAkJUyco5B8Doihy7v6CCrXYJG9S13cVbCJDEiQNOcvx2tHRnufickXchapc1pAWRycYWbK15w==\",\n" +
                "                \"minimallyFormattedText\": \"Good morning how are you doing today\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"status\": \"COMPLETE\"\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "    \"interpretationStatus\": \"complete\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"event\": \"FinalResult\",\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"interpretationData\":\n" +
                "    {\n" +
                "        \"interpretations\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"confidence\": 100,\n" +
                "                \"origin\": \"GRAMMAR\",\n" +
                "                \"formattedLiteral\": \"Good morning how are you doing today\",\n" +
                "                \"mentions\":\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"formattedEnd\": 12,\n" +
                "                        \"concept\": \"TIME_OF_DAY\",\n" +
                "                        \"formattedLiteral\": \"morning\",\n" +
                "                        \"end\": 12,\n" +
                "                        \"begin\": 5,\n" +
                "                        \"formattedBegin\": 5,\n" +
                "                        \"literal\": \"morning\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"formattedEnd\": 36,\n" +
                "                        \"concept\": \"DATE\",\n" +
                "                        \"formattedLiteral\": \"today\",\n" +
                "                        \"end\": 36,\n" +
                "                        \"begin\": 31,\n" +
                "                        \"formattedBegin\": 31,\n" +
                "                        \"literal\": \"today\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"literal\": \"Good morning how are you doing today\",\n" +
                "                \"intention\": \"GREETINGS\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}").toMap(), finalResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"Recognize\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), recognizeResponse.toMap());
    }

    @Test
    public void processWebapiResponseTestSuccess_withInterpretation_autoEndFalse()
    {
        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        kryptonWsSession.setWebapiSessionId("test");
        when(webApiEndpoint.getWebapiSession(anyString())).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(mockConfig.getBoolean("autoEnd", true)).thenReturn(false);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        kryptonWsSession
                .setKryptonWsWsSession(session)
                .setKryptonWsSessionId("b84051e3a33f2094")
                .setWebapiResponseListener(responseListener)
                .setKryptonWsResponseSender(mockSender)
                .setKryptonWsParameters(new KryptonWsParameters(null, null));

        KryptonWsRecognizeMethod recognizeMethod  = new KryptonWsRecognizeMethod(new JSONObject(RECOGNIZE_METHOD), kryptonWsSession);

        endOfInputMethod.recognizeMethod = recognizeMethod;

        responseListener.setCurrentMethod(recognizeMethod);

        JSONObject semanticModel = new JSONObject()
                .put("url", "url")
                .put("id", "id");

        kryptonWsSession.setSemanticModel(semanticModel);

        endOfInputMethod.processWebapiResponse("{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"53996eb3-0852-4b4a-84e8-4dac35166a2e\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"2\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"statistics\": {\n" +
                "      \"hostInfo\": {\n" +
                "        \"region\": \"ma\",\n" +
                "        \"environment\": \"Prod\",\n" +
                "        \"version\": \"2.31.2-SNAPSHOT\"\n" +
                "      },\n" +
                "      \"startTime\": \"2021/05/14 01:38:52.622\",\n" +
                "      \"endTime\": \"2021/05/14 01:38:59.031\",\n" +
                "      \"firstIntermediateResultTime\": \"2021/05/14 01:38:54.397\",\n" +
                "      \"lastIntermediateResultTime\": \"2021/05/14 01:38:54.493\",\n" +
                "      \"firstAudioBufferReceivedTime\": \"2021/05/14 01:38:53.802\",\n" +
                "      \"lastAudioBufferReceivedTime\": \"2021/05/14 01:38:53.869\",\n" +
                "      \"modelVersion\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\",\n" +
                "      \"directives\": [\n" +
                "        {\n" +
                "          \"name\": \"STOP\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:58.929\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"events\": [\n" +
                "        {\n" +
                "          \"name\": \"startOfSpeech\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:53.817\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"endOfSpeech\",\n" +
                "          \"startTime\": \"2021/05/14 01:38:53.852\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
                "      \"mediaDuration\": 4080,\n" +
                "      \"engineRequests\": {\n" +
                "        \"engine\": \"krypton-grpc\",\n" +
                "        \"dlms\": [\n" +
                "          {\n" +
                "            \"uri\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"language\": \"eng-USA\"\n" +
                "      },\n" +
                "      \"engineResponses\": {\n" +
                "        \"grpcHeaders\": {\n" +
                "          \"date\": \"Fri, 14 May 2021 01:38:54 GMT\",\n" +
                "          \"x-request-id\": \"087f05f6-b271-423c-971e-e1e27e37b542\",\n" +
                "          \"server\": \"envoy\",\n" +
                "          \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
                "          \"content-type\": \"application/grpc\",\n" +
                "          \"accept-encoding\": \"identity,gzip\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"data\": {\n" +
                "      \"transcriptions\": [\n" +
                "        \"Good morning how are you doing today\"\n" +
                "      ],\n" +
                "      \"confidence\": 919,\n" +
                "      \"averageWeightedConfidences\": [\n" +
                "        978\n" +
                "      ],\n" +
                "      \"words\": [\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 984.0,\n" +
                "            \"word\": \"Good\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 988.0,\n" +
                "            \"word\": \"morning\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"how\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"doing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 970.0,\n" +
                "            \"word\": \"today\"\n" +
                "          }\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bestResult\": \"Good morning how are you doing today\",\n" +
                "    \"noiseLevel\": {\n" +
                "      \"mean\": 1.21,\n" +
                "      \"standardDeviation\": 1.28\n" +
                "    },\n" +
                "    \"verboseResult\": {\n" +
                "      \"resultType\": \"FINAL\",\n" +
                "      \"absStartMs\": 0.0,\n" +
                "      \"absEndMs\": 4080.0,\n" +
                "      \"utteranceInfo\": {\n" +
                "        \"durationMs\": 4080.0,\n" +
                "        \"clippingDurationMs\": 0.0,\n" +
                "        \"droppedSpeechPackets\": 0.0,\n" +
                "        \"droppedNonspeechPackets\": 0.0,\n" +
                "        \"dsp\": {\n" +
                "          \"snrEstimateDb\": 57.0,\n" +
                "          \"level\": 4097.0,\n" +
                "          \"numChannels\": 1.0,\n" +
                "          \"initialSilenceMs\": 460.0,\n" +
                "          \"initialEnergy\": -79.9374,\n" +
                "          \"finalEnergy\": -83.1577,\n" +
                "          \"meanEnergy\": 100.651\n" +
                "        }\n" +
                "      },\n" +
                "      \"hypotheses\": [\n" +
                "        {\n" +
                "          \"confidence\": 0.919,\n" +
                "          \"averageConfidence\": 0.978,\n" +
                "          \"rejected\": false,\n" +
                "          \"formattedText\": \"Good morning how are you doing today\",\n" +
                "          \"minimallyFormattedText\": \"Good morning how are you doing today\",\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"text\": \"Good\",\n" +
                "              \"confidence\": 0.984,\n" +
                "              \"startMs\": 460.0,\n" +
                "              \"endMs\": 720.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"morning\",\n" +
                "              \"confidence\": 0.988,\n" +
                "              \"startMs\": 720.0,\n" +
                "              \"endMs\": 1120.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"how\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1120.0,\n" +
                "              \"endMs\": 1260.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"are\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1260.0,\n" +
                "              \"endMs\": 1560.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"you\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1560.0,\n" +
                "              \"endMs\": 1680.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"doing\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1680.0,\n" +
                "              \"endMs\": 2020.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"today\",\n" +
                "              \"confidence\": 0.97,\n" +
                "              \"startMs\": 2020.0,\n" +
                "              \"endMs\": 2280.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            }\n" +
                "          ],\n" +
                "          \"encryptedTokenization\": \"EwBMQ5jYYYtpfx/4EGvr2lq/ZcHkGGZDzjLeETp+hGcEl2SB8UjyJXXcg9p8oIpCyld8U7n15yxGTcwG6VkvA0jeR5NozWoWqkMXnfX8sORiRtzadauRyO7YrZkJ0X5ChjNVNiBw+4QLzjBm9YWTcUhOcMI5bmz4V/cfLoGnI1c+qQCi5lYRDYCepNkoDv46qn4rdNLIN7o4S3lIPYqjCqUoJslJV4+kghdPfd0Qb611HykFztvCLcDn4F0B2GNVKjPcELIf08O0Fxv86cMDcoj/meXOv/r2Nq1qcyRKaepEGeQ9M7RiesOJdLUBZ0Z69Pj+QhAQzT4SNz2zeMp8dA==ZAIhpH+zJzY3eSr2d12TmbgkENAPvlZ9ulSNBj0cXPCsV2EM2Op7aY5lb215TK7qli4yCI2O8rltu4eWM5DxxbD3WXKL+FR6k0dDKmJjrBKYCwPEs5j0XCVE1Cx4SQaoXAOQQPV8yQxN51JwhkkI6x6WWmWmt8g4DzaCe1MLrMmjOb2hFDhEgDa2jn3svT96cMS/LVBmAqMCR3MIl6Flsh94Bx/3KUXn6Pq8i/rNcnz0Ihh6lEamHGmRKCwEWzlmp22gu25B+wRey9AAMY8iFdlYiqCh4vkw30f1VAoKqmPnga9Id8Kb4ISm8zPaNqb77FPVY95J6M/OjlywFREeqUj4BbMA/TkLg5GUeNs/w4ovjQ4zAuiDTWxEsclRc5zDNe0W4uHnVRxJiCmFa7da2795Jl/+pTHLksIYjjpXuO/K0AH4UeSP16uthm8dGDJpeSrmM4yC+R2FVmZz5tVO2QYb85jx1QRbhoPvEBMrcjomW05wL/Ox1D2u31VK7+y6Vbgyz3eZn4U28dXZVYcV8J3IAc/wGjJw2pkcB3D0Ky6VhDbKecMzmTEwz3PiuCpNkMDOyI9BMOsHilLA7KxZ5DBffrnSdHtxRuVVSMbKEVKqYQ3+FlVRwn1wqvCE7tvg1yX2k1KqGYCMN2pwV94curBYlHZt/Dqmj71VYpDfT1lFIbvBhjneScoMD8aPaodFjUEFsi9KN4IkqAkJUyco5B8Doihy7v6CCrXYJG9S13cVbCJDEiQNOcvx2tHRnufickXchapc1pAWRycYWbK15w==\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dataPack\": {\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"version\": \"4.4.0\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");

        endOfInputMethod.processWebapiResponse("{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"53996eb3-0852-4b4a-84e8-4dac35166a2e\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"interpretation\",\n" +
                "    \"id\": \"3\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"statistics\": {\n" +
                "      \"hostInfo\": {\n" +
                "        \"region\": \"ma\",\n" +
                "        \"environment\": \"Dev\",\n" +
                "        \"version\": \"2.31.2-SNAPSHOT\"\n" +
                "      },\n" +
                "      \"startTime\": \"2021/05/14 01:39:04.824\",\n" +
                "      \"endTime\": \"2021/05/14 01:39:05.153\",\n" +
                "      \"modelVersion\": \"urn:nuance-mix:tag:model/context_tag/mix.nlu?=language=eng-USA\",\n" +
                "      \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
                "      \"engineRequests\": {\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"model\": \"urn:nuance-mix:tag:model/context_tag/mix.nlu?=language=eng-USA\",\n" +
                "        \"engine\": \"nlu-grpc\"\n" +
                "      },\n" +
                "      \"engineResponses\": {\n" +
                "        \"grpcHeaders\": {\n" +
                "          \"x-request-id\": \"ecd723aa-5604-96aa-92ac-2cb7cc7df2ee\",\n" +
                "          \"date\": \"Fri, 14 May 2021 01:39:04 GMT\",\n" +
                "          \"server\": \"envoy\",\n" +
                "          \"x-envoy-upstream-service-time\": \"4\",\n" +
                "          \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
                "          \"uber-trace-id\": \"eb967879139dd7613986b798061e072e:9b7780dbfd676196:5ad361440a3ec5ab:1\",\n" +
                "          \"content-type\": \"application/grpc\",\n" +
                "          \"accept-encoding\": \"identity,gzip\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"interpretations\": [\n" +
                "      {\n" +
                "        \"concepts\": [\n" +
                "          {\n" +
                "            \"concept\": \"TIME_OF_DAY\",\n" +
                "            \"literal\": \"morning\",\n" +
                "            \"begin\": 5,\n" +
                "            \"end\": 12\n" +
                "          },\n" +
                "          {\n" +
                "            \"concept\": \"DATE\",\n" +
                "            \"literal\": \"today\",\n" +
                "            \"begin\": 31,\n" +
                "            \"end\": 36\n" +
                "          }\n" +
                "        ],\n" +
                "        \"confidence\": 100,\n" +
                "        \"literal\": \"Good morning how are you doing today\",\n" +
                "        \"intent\": \"GREETINGS\",\n" +
                "        \"origin\": \"GRAMMAR\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"bestInterpretation\": {\n" +
                "      \"concepts\": [\n" +
                "        {\n" +
                "          \"concept\": \"TIME_OF_DAY\",\n" +
                "          \"literal\": \"morning\",\n" +
                "          \"begin\": 5,\n" +
                "          \"end\": 12\n" +
                "        },\n" +
                "        {\n" +
                "          \"concept\": \"DATE\",\n" +
                "          \"literal\": \"today\",\n" +
                "          \"begin\": 31,\n" +
                "          \"end\": 36\n" +
                "        }\n" +
                "      ],\n" +
                "      \"confidence\": 100,\n" +
                "      \"literal\": \"Good morning how are you doing today\",\n" +
                "      \"intent\": \"GREETINGS\",\n" +
                "      \"origin\": \"GRAMMAR\"\n" +
                "    }\n" +
                "  }\n" +
                "}");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender, times(4)).sendResponse(argumentCaptor.capture());

        JSONObject partialResultEvent = new JSONObject(argumentCaptor.getAllValues().get(0));
        JSONObject endOfInputResponse = new JSONObject(argumentCaptor.getAllValues().get(1));
        JSONObject finalResultEvent = new JSONObject(argumentCaptor.getAllValues().get(2));
        JSONObject recognizeResponse = new JSONObject(argumentCaptor.getAllValues().get(3));

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Event\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"event\": \"PartialResult\",\n" +
                "  \"data\": {\n" +
                "    \"utterance_complete\": true,\n" +
                "    \"absStart\": 0,\n" +
                "    \"absEnd\": 4080,\n" +
                "    \"completion_cause\": \"SUCCESS\",\n" +
                "    \"nBest\": [\n" +
                "      {\n" +
                "        \"averageConfidence\": 978,\n" +
                "        \"rejected\": false,\n" +
                "        \"confidence\": 919,\n" +
                "        \"words\": [\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 984,\n" +
                "            \"start\": 460,\n" +
                "            \"end\": 720,\n" +
                "            \"text\": \"Good\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 988,\n" +
                "            \"start\": 720,\n" +
                "            \"end\": 1120,\n" +
                "            \"text\": \"morning\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 981,\n" +
                "            \"start\": 1120,\n" +
                "            \"end\": 1260,\n" +
                "            \"text\": \"how\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 981,\n" +
                "            \"start\": 1260,\n" +
                "            \"end\": 1560,\n" +
                "            \"text\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 982,\n" +
                "            \"start\": 1560,\n" +
                "            \"end\": 1680,\n" +
                "            \"text\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 982,\n" +
                "            \"start\": 1680,\n" +
                "            \"end\": 2020,\n" +
                "            \"text\": \"doing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 970,\n" +
                "            \"start\": 2020,\n" +
                "            \"end\": 2280,\n" +
                "            \"text\": \"today\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"formattedText\": \"Good morning how are you doing today\",\n" +
                "        \"tokenization\": \"EwBMQ5jYYYtpfx/4EGvr2lq/ZcHkGGZDzjLeETp+hGcEl2SB8UjyJXXcg9p8oIpCyld8U7n15yxGTcwG6VkvA0jeR5NozWoWqkMXnfX8sORiRtzadauRyO7YrZkJ0X5ChjNVNiBw+4QLzjBm9YWTcUhOcMI5bmz4V/cfLoGnI1c+qQCi5lYRDYCepNkoDv46qn4rdNLIN7o4S3lIPYqjCqUoJslJV4+kghdPfd0Qb611HykFztvCLcDn4F0B2GNVKjPcELIf08O0Fxv86cMDcoj/meXOv/r2Nq1qcyRKaepEGeQ9M7RiesOJdLUBZ0Z69Pj+QhAQzT4SNz2zeMp8dA==ZAIhpH+zJzY3eSr2d12TmbgkENAPvlZ9ulSNBj0cXPCsV2EM2Op7aY5lb215TK7qli4yCI2O8rltu4eWM5DxxbD3WXKL+FR6k0dDKmJjrBKYCwPEs5j0XCVE1Cx4SQaoXAOQQPV8yQxN51JwhkkI6x6WWmWmt8g4DzaCe1MLrMmjOb2hFDhEgDa2jn3svT96cMS/LVBmAqMCR3MIl6Flsh94Bx/3KUXn6Pq8i/rNcnz0Ihh6lEamHGmRKCwEWzlmp22gu25B+wRey9AAMY8iFdlYiqCh4vkw30f1VAoKqmPnga9Id8Kb4ISm8zPaNqb77FPVY95J6M/OjlywFREeqUj4BbMA/TkLg5GUeNs/w4ovjQ4zAuiDTWxEsclRc5zDNe0W4uHnVRxJiCmFa7da2795Jl/+pTHLksIYjjpXuO/K0AH4UeSP16uthm8dGDJpeSrmM4yC+R2FVmZz5tVO2QYb85jx1QRbhoPvEBMrcjomW05wL/Ox1D2u31VK7+y6Vbgyz3eZn4U28dXZVYcV8J3IAc/wGjJw2pkcB3D0Ky6VhDbKecMzmTEwz3PiuCpNkMDOyI9BMOsHilLA7KxZ5DBffrnSdHtxRuVVSMbKEVKqYQ3+FlVRwn1wqvCE7tvg1yX2k1KqGYCMN2pwV94curBYlHZt/Dqmj71VYpDfT1lFIbvBhjneScoMD8aPaodFjUEFsi9KN4IkqAkJUyco5B8Doihy7v6CCrXYJG9S13cVbCJDEiQNOcvx2tHRnufickXchapc1pAWRycYWbK15w==\",\n" +
                "        \"minimallyFormattedText\": \"Good morning how are you doing today\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"status\": \"IN-PROGRESS\"\n" +
                "  }\n" +
                "}").toMap(), partialResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"EndOfInput\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), endOfInputResponse.toMap());

        assertEquals(new JSONObject("{\n" +
                "    \"method\": \"Event\",\n" +
                "    \"data\":\n" +
                "    {\n" +
                "        \"completion_cause\": \"END-OF-STREAM\",\n" +
                "        \"status\": \"COMPLETE\"\n" +
                "    },\n" +
                "    \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "    \"interpretationStatus\": \"complete\",\n" +
                "    \"sessionId\": \"b84051e3a33f2094\",\n" +
                "    \"event\": \"FinalResult\",\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"interpretationData\":\n" +
                "    {\n" +
                "        \"interpretations\":\n" +
                "        [\n" +
                "            {\n" +
                "                \"confidence\": 100,\n" +
                "                \"origin\": \"GRAMMAR\",\n" +
                "                \"formattedLiteral\": \"Good morning how are you doing today\",\n" +
                "                \"mentions\":\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"formattedEnd\": 12,\n" +
                "                        \"concept\": \"TIME_OF_DAY\",\n" +
                "                        \"formattedLiteral\": \"morning\",\n" +
                "                        \"end\": 12,\n" +
                "                        \"begin\": 5,\n" +
                "                        \"formattedBegin\": 5,\n" +
                "                        \"literal\": \"morning\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"formattedEnd\": 36,\n" +
                "                        \"concept\": \"DATE\",\n" +
                "                        \"formattedLiteral\": \"today\",\n" +
                "                        \"end\": 36,\n" +
                "                        \"begin\": 31,\n" +
                "                        \"formattedBegin\": 31,\n" +
                "                        \"literal\": \"today\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"literal\": \"Good morning how are you doing today\",\n" +
                "                \"intention\": \"GREETINGS\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}").toMap(), finalResultEvent.toMap());

        assertEquals(new JSONObject("{\n" +
                "  \"method\": \"Response\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"sessionId\": \"b84051e3a33f2094\",\n" +
                "  \"requestId\": \"b84051e3a33f2094-00002\",\n" +
                "  \"responseTo\": \"Recognize\",\n" +
                "  \"status\": \"complete\"\n" +
                "}").toMap(), recognizeResponse.toMap());
    }

    @Test
    public void processWebapiError_noActiveCommand(){
        kryptonWsSession.setKryptonWsResponseSender(mockSender);

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(webApiEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        endOfInputMethod.processWebapiResponse(noActiveCommandString);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("no-action", resObject.get("status"));
        assertEquals("Response", resObject.get("method"));
        assertEquals(KryptonWsEndOfInputMethod.NAME, resObject.get("responseTo"));
    }

    @Test
    public void processWebapiError()
    {
        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        endOfInputMethod.processWebapiResponse(errorCommandString);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("failed", resObject.get("status"));
        assertEquals("Response", resObject.get("method"));
        assertEquals(KryptonWsEndOfInputMethod.NAME, resObject.get("responseTo"));
    }

    @Test
    public void processWebapiError_cancelled()
    {
        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        endOfInputMethod.processWebapiResponse(cancelledCommandString);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("failed", resObject.get("status"));
        assertEquals("Response", resObject.get("method"));
        assertEquals(KryptonWsEndOfInputMethod.NAME, resObject.get("responseTo"));
    }

    @Test
    public void invalidJsonTest(){
        String invalidWebapiJsonResponse = "\"type\": \"result\",\n" +
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
        kryptonWsSession.setKryptonWsResponseSender(mockSender);

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(webApiEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);

        endOfInputMethod.processWebapiResponse(invalidWebapiJsonResponse);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(captor.capture());
        JSONObject resObject = new JSONObject(captor.getAllValues().get(0));

        assertEquals("failed", resObject.get("status"));
        
    }

    @Test
    public void canRunWhile()
    {
        KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
        KryptonWsEndOfInputMethod
                endOfInputMethod = new KryptonWsEndOfInputMethod(new JSONObject(endOfInputMethodJson), kryptonWsSession);

        String request = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        KryptonWsLoadMethod
                commandThatIsAlreadyRunning = new KryptonWsLoadMethod(new JSONObject(request), kryptonWsSession);

        assertFalse(endOfInputMethod.canRunWhile(commandThatIsAlreadyRunning));
    }

    @Test
    public void processWebapiResponse_intermediateResult()
    {
        String intermediateResult = "{\n" +
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
                "               \"Hello where\"\n" +
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
                "}";

        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        endOfInputMethod.processWebapiResponse(intermediateResult);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("IN-PROGRESS", resObject.getJSONObject("data").get("status"));
        assertEquals("SUCCESS", resObject.getJSONObject("data").get("completion_cause"));
        assertFalse(resObject.getJSONObject("data").getBoolean("utterance_complete"));
        assertEquals("Event", resObject.get("method"));
        assertEquals("PartialResult", resObject.get("event"));
    }

    @Test
    public void processWebapiResponse_finalResult_autoEndTrue()
    {
        String finalResult = "{\n" +
                "  \"type\": \"result\",\n" +
                "  \"sessionId\": \"e7bf413d-6bff-4e54-b055-035c2fd2bddf\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"2\"\n" +
                "  },\n" +
                "  \"result\": {\n" +
                "    \"final\": true,\n" +
                "    \"status\": \"success\",\n" +
                "    \"statistics\": {\n" +
                "      \"hostInfo\": {\n" +
                "        \"region\": \"ma\",\n" +
                "        \"environment\": \"Prod\",\n" +
                "        \"version\": \"1.0--SNAPSHOT\"\n" +
                "      },\n" +
                "      \"startTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"endTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"firstIntermediateResultTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"lastIntermediateResultTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"firstAudioBufferReceivedTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"lastAudioBufferReceivedTime\": \"2021/04/26 12:00:00.000\",\n" +
                "      \"modelVersion\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\",\n" +
                "      \"events\": [\n" +
                "        {\n" +
                "          \"name\": \"startOfSpeech\",\n" +
                "          \"startTime\": \"2021/04/26 12:00:00.000\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"endOfSpeech\",\n" +
                "          \"startTime\": \"2021/04/26 12:00:00.000\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"enableSpeechRecognitionSpeakerProfile\": false,\n" +
                "      \"mediaDuration\": 2880,\n" +
                "      \"engineRequests\": {\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"engine\": \"krypton-grpc\",\n" +
                "        \"dlms\": [\n" +
                "          {\n" +
                "            \"uri\": \"urn:nuance-mix:tag:model/context_tag/mix.asr?=language=eng-USA\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"engineResponses\": {\n" +
                "        \"grpcHeaders\": {\n" +
                "          \"date\": \"Tue, 26 Apr 2021 12:00:00 GMT\",\n" +
                "          \"x-request-id\": \"6bf1ba67-17c8-434e-a678-8967b1d410f3\",\n" +
                "          \"server\": \"envoy\",\n" +
                "          \"grpc-accept-encoding\": \"identity,deflate,gzip\",\n" +
                "          \"content-type\": \"application/grpc\",\n" +
                "          \"accept-encoding\": \"identity,gzip\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"data\": {\n" +
                "      \"transcriptions\": [\n" +
                "        \"Good morning how are you doing today\"\n" +
                "      ],\n" +
                "      \"confidence\": 919,\n" +
                "      \"averageWeightedConfidences\": [\n" +
                "        978\n" +
                "      ],\n" +
                "      \"words\": [\n" +
                "        [\n" +
                "          {\n" +
                "            \"confidence\": 984.0,\n" +
                "            \"word\": \"Good\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 988.0,\n" +
                "            \"word\": \"morning\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"how\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 981.0,\n" +
                "            \"word\": \"are\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"you\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 982.0,\n" +
                "            \"word\": \"doing\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"confidence\": 970.0,\n" +
                "            \"word\": \"today\"\n" +
                "          }\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bestResult\": \"Good morning how are you doing today\",\n" +
                "    \"noiseLevel\": {\n" +
                "      \"mean\": 1.21,\n" +
                "      \"standardDeviation\": 1.28\n" +
                "    },\n" +
                "    \"verboseResult\": {\n" +
                "      \"resultType\": \"FINAL\",\n" +
                "      \"absStartMs\": 0.0,\n" +
                "      \"absEndMs\": 2860.0,\n" +
                "      \"utteranceInfo\": {\n" +
                "        \"durationMs\": 2860.0,\n" +
                "        \"clippingDurationMs\": 0.0,\n" +
                "        \"droppedSpeechPackets\": 0.0,\n" +
                "        \"droppedNonspeechPackets\": 0.0,\n" +
                "        \"dsp\": {\n" +
                "          \"snrEstimateDb\": 55.0,\n" +
                "          \"level\": 18945.0,\n" +
                "          \"numChannels\": 1.0,\n" +
                "          \"initialSilenceMs\": 460.0,\n" +
                "          \"initialEnergy\": -80.2776,\n" +
                "          \"finalEnergy\": -75.9371,\n" +
                "          \"meanEnergy\": 142.439\n" +
                "        }\n" +
                "      },\n" +
                "      \"hypotheses\": [\n" +
                "        {\n" +
                "          \"confidence\": 0.919,\n" +
                "          \"averageConfidence\": 0.978,\n" +
                "          \"rejected\": false,\n" +
                "          \"formattedText\": \"Good morning how are you doing today\",\n" +
                "          \"minimallyFormattedText\": \"Good morning how are you doing today\",\n" +
                "          \"words\": [\n" +
                "            {\n" +
                "              \"text\": \"Good\",\n" +
                "              \"confidence\": 0.984,\n" +
                "              \"startMs\": 460.0,\n" +
                "              \"endMs\": 720.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"morning\",\n" +
                "              \"confidence\": 0.988,\n" +
                "              \"startMs\": 720.0,\n" +
                "              \"endMs\": 1120.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"how\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1120.0,\n" +
                "              \"endMs\": 1260.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"are\",\n" +
                "              \"confidence\": 0.981,\n" +
                "              \"startMs\": 1260.0,\n" +
                "              \"endMs\": 1560.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"you\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1560.0,\n" +
                "              \"endMs\": 1680.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"doing\",\n" +
                "              \"confidence\": 0.982,\n" +
                "              \"startMs\": 1680.0,\n" +
                "              \"endMs\": 2020.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            },\n" +
                "            {\n" +
                "              \"text\": \"today\",\n" +
                "              \"confidence\": 0.97,\n" +
                "              \"startMs\": 2020.0,\n" +
                "              \"endMs\": 2280.0,\n" +
                "              \"silenceAfterWordMs\": 0.0\n" +
                "            }\n" +
                "          ],\n" +
                "          \"encryptedTokenization\": \"123\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"dataPack\": {\n" +
                "        \"language\": \"eng-USA\",\n" +
                "        \"topic\": \"GEN\",\n" +
                "        \"version\": \"1.0\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        endOfInputMethod.processWebapiResponse(finalResult);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender,times(4)).sendResponse(argumentCaptor.capture());

        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(2));
        assertEquals("COMPLETE", resObject.getJSONObject("data").get("status"));
        assertEquals("END-OF-STREAM", resObject.getJSONObject("data").get("completion_cause"));
        assertEquals("Event", resObject.get("method"));
        assertEquals("FinalResult", resObject.get("event"));

    }

    @Test
    public void testStatistics() {
        String intermediateResult = "{\n" +
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
                "               \"Hello where\"\n" +
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
                "}";

        IWebApiEndpoint webApiEndpoint = mock(IWebApiEndpoint.class);
        ISession mockSession = mock(ISession.class);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(webApiEndpoint.getActiveSession()).thenReturn(mockSession);
        when(mockSession.getSessionConfig()).thenReturn(mockConfig);
        when(mockConfig.getBoolean(eq(KRYPTON_WS_ENABLE_METRICS.getKey()), anyBoolean())).thenReturn(true);
        kryptonWsSession.setWebApiEndpoint(webApiEndpoint);
        kryptonWsSession.setKryptonWsResponseSender(mockSender);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            KryptonWsEventBuilder kryptonWsEventBuilder = (KryptonWsEventBuilder)args[1];
            kryptonWsEventBuilder.setMetrics(Map.of("elapsed", 17));
            return null; // void method in a block-style lambda, so return null
        }).when(endOfInputMethod.recognizeMethod).addEventMetrics(any(), any(), anyBoolean());

        endOfInputMethod.processWebapiResponse(intermediateResult);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSender).sendResponse(argumentCaptor.capture());
        JSONObject resObject = new JSONObject(argumentCaptor.getAllValues().get(0));
        assertEquals("IN-PROGRESS", resObject.getJSONObject("data").get("status"));
        assertEquals("SUCCESS", resObject.getJSONObject("data").get("completion_cause"));
        assertFalse(resObject.getJSONObject("data").getBoolean("utterance_complete"));
        assertEquals("Event", resObject.get("method"));
        assertEquals("PartialResult", resObject.get("event"));
        assertNotNull(resObject.get("metrics"));
    }
}
