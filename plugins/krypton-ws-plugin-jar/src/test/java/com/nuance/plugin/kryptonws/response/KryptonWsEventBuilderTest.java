package com.nuance.plugin.kryptonws.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KryptonWsEventBuilderTest
{
    String cobraPartialResult = "{\n" +
            "  \"command\": {\n" +
            "    \"id\": \"2\",\n" +
            "    \"name\": \"speechRecognition\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"bestResult\": \"Monday\",\n" +
            "    \"data\": {\n" +
            "      \"averageWeightedConfidences\": [\n" +
            "        0\n" +
            "      ],\n" +
            "      \"confidence\": 0,\n" +
            "      \"transcriptions\": [\n" +
            "        \"Monday\"\n" +
            "      ],\n" +
            "      \"words\": [\n" +
            "        [\n" +
            "          {\n" +
            "            \"confidence\": 0.0,\n" +
            "            \"word\": \"Monday\"\n" +
            "          }\n" +
            "        ]\n" +
            "      ]\n" +
            "    },\n" +
            "    \"final\": false,\n" +
            "    \"noiseLevel\": {\n" +
            "      \"mean\": 1.29,\n" +
            "      \"standardDeviation\": 1.36\n" +
            "    },\n" +
            "    \"status\": \"success\",\n" +
            "    \"verboseResult\": {\n" +
            "      \"absEndMs\": 690.0,\n" +
            "      \"absStartMs\": 0.0,\n" +
            "      \"dataPack\": {\n" +
            "        \"language\": \"eng-USA\",\n" +
            "        \"topic\": \"GEN\",\n" +
            "        \"version\": \"4.4.0\"\n" +
            "      },\n" +
            "      \"hypotheses\": [\n" +
            "        {\n" +
            "          \"formattedText\": \"Monday\",\n" +
            "          \"minimallyFormattedText\": \"Monday\",\n" +
            "          \"rejected\": false,\n" +
            "          \"words\": [\n" +
            "            {\n" +
            "              \"endMs\": 600.0,\n" +
            "              \"silenceAfterWordMs\": 0.0,\n" +
            "              \"startMs\": 280.0,\n" +
            "              \"text\": \"Monday\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"resultType\": \"PARTIAL\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"sessionId\": \"6e23c169-810f-4545-b1df-0d80a9bf64a3\",\n" +
            "  \"type\": \"result\"\n" +
            "}";

    String cobraFinalResult = "{  \"command\": {\n" +
            "    \"id\": \"2\",\n" +
            "    \"name\": \"speechRecognition\"\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"bestResult\": \"Monday Tuesday Wednesday\",\n" +
            "    \"data\": {\n" +
            "      \"averageWeightedConfidences\": [\n" +
            "        934\n" +
            "      ],\n" +
            "      \"confidence\": 934,\n" +
            "      \"transcriptions\": [\n" +
            "        \"Monday Tuesday Wednesday\"\n" +
            "      ],\n" +
            "      \"words\": [\n" +
            "        [\n" +
            "          {\n" +
            "            \"confidence\": 968.0,\n" +
            "            \"word\": \"Monday\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"confidence\": 974.0,\n" +
            "            \"word\": \"Tuesday\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"confidence\": 981.0,\n" +
            "            \"word\": \"Wednesday\"\n" +
            "          }\n" +
            "        ]\n" +
            "      ]\n" +
            "    },\n" +
            "    \"final\": true,\n" +
            "    \"noiseLevel\": {\n" +
            "      \"mean\": 1.29,\n" +
            "      \"standardDeviation\": 1.36\n" +
            "    },\n" +
            "    \"status\": \"success\",\n" +
            "    \"verboseResult\": {\n" +
            "      \"absEndMs\": 2300.0,\n" +
            "      \"absStartMs\": 0.0,\n" +
            "      \"dataPack\": {\n" +
            "        \"language\": \"eng-USA\",\n" +
            "        \"topic\": \"GEN\",\n" +
            "        \"version\": \"4.4.0\"\n" +
            "      },\n" +
            "      \"hypotheses\": [\n" +
            "        {\n" +
            "          \"averageConfidence\": 0.974,\n" +
            "          \"confidence\": 0.934,\n" +
            "          \"encryptedTokenization\": \"SSES0v5J8e0ni516AD4xJfw+xgO95qhf8cSX8EFgH9WZBd29M0PCejUDgazhzewRB0KWuHbCJwNX6kPfqWgFxGGyI2ifaw882CSipUKp6Kt1fowVHIH/6FQyhf/dYIByGRb1b4ZbyXP1rabn7nsUlMCrZBOJGyaYlkV6XKbi0yiBdbq68bOkxgehglwgka/BYmlCMy5gRQb9Qs2UL1vdmzy7MuO6GyCaFxLfF6IgpRnM65N7FhtWefXt3un2fCRcV3kuHUkf0RTstdqak9I0W6vjraW+wC4hWDCZ1sYbODaVDjZ0xhDASQJi4WrCIzH2bwAl0vrENLWPbvrMp0fYkg==dp9MyrmQ034yBUXdrKCmc6AuHrhmTvbSq1hGi2HDGdes7qhvZaL67GUPhOvGnh+77tDl+NCkZQO+Yy0YzRSgREVsMZwDK6AFcSeuN+aE7RY8xJwpMNG9w0KjF0jaoQXWlejwVQZfQrH9DHhV237AxyijAvrVo18IAUH9aRif7TpIjx6b1nj1qBqirQ+hmN2Wu1epTHCw4vlX/LS6QdqZXAd93IkIfox81pNE4T/Nob+M+9RzUM8J9EwSy4V5VWHsObalCbIAsbigkttMHdOKlvnXWR1DN6xkWRIXYPnRED4ac0LcvwxXHVQmEIyC2hsYNfqZfLSnhkMXMFqJDd3v5Ui2fYaNY11lP9XawG8IjNE=\",\n" +
            "          \"formattedText\": \"Monday Tuesday Wednesday\",\n" +
            "          \"minimallyFormattedText\": \"Monday Tuesday Wednesday\",\n" +
            "          \"rejected\": false,\n" +
            "          \"words\": [\n" +
            "            {\n" +
            "              \"confidence\": 0.968,\n" +
            "              \"endMs\": 620.0,\n" +
            "              \"silenceAfterWordMs\": 0.0,\n" +
            "              \"startMs\": 280.0,\n" +
            "              \"text\": \"Monday\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"confidence\": 0.974,\n" +
            "              \"endMs\": 1200.0,\n" +
            "              \"silenceAfterWordMs\": 0.0,\n" +
            "              \"startMs\": 620.0,\n" +
            "              \"text\": \"Tuesday\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"confidence\": 0.981,\n" +
            "              \"endMs\": 1760.0,\n" +
            "              \"silenceAfterWordMs\": 0.0,\n" +
            "              \"startMs\": 1200.0,\n" +
            "              \"text\": \"Wednesday\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"resultType\": \"FINAL\",\n" +
            "      \"utteranceInfo\": {\n" +
            "        \"clippingDurationMs\": 0.0,\n" +
            "        \"droppedNonspeechPackets\": 0.0,\n" +
            "        \"droppedSpeechPackets\": 0.0,\n" +
            "        \"dsp\": {\n" +
            "          \"finalEnergy\": -85.2089,\n" +
            "          \"initialEnergy\": -85.4209,\n" +
            "          \"initialSilenceMs\": 280.0,\n" +
            "          \"level\": 17921.0,\n" +
            "          \"meanEnergy\": 80.3057,\n" +
            "          \"numChannels\": 1.0,\n" +
            "          \"snrEstimateDb\": 58.0\n" +
            "        },\n" +
            "        \"durationMs\": 2300.0\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"sessionId\": \"42698024-6461-4267-af43-141b5a127ac3\",\n" +
            "  \"type\": \"result\"\n" +
            "}";

    @Test
    public void buildTest() {
        JSONObject utteranceInfo = new JSONObject("{\n" +
                "      \"duration\": 580,\n" +
                "      \"dropped_speech_packets\": 0,\n" +
                "      \"mean_energy\": 158.316,\n" +
                "      \"initial_silence\": 20,\n" +
                "      \"level\": 27649,\n" +
                "      \"initial_energy\": -64.739,\n" +
                "      \"final_energy\": -49.7646,\n" +
                "      \"stereo\": false,\n" +
                "      \"clipping_duration\": 0,\n" +
                "      \"snr_estimate\": 28,\n" +
                "      \"dropped_non_speech_packets\": 0\n" +
                "    }");


        JSONArray nbest = new JSONArray("[\n" +
                "      {\n" +
                "        \"averageConfidence\": 975,\n" +
                "        \"rejected\": false,\n" +
                "        \"confidence\": 977,\n" +
                "        \"words\": [\n" +
                "          {\n" +
                "            \"silence_after_word\": 0,\n" +
                "            \"confidence\": 975,\n" +
                "            \"start\": 20,\n" +
                "            \"end\": 180,\n" +
                "            \"text\": \"One\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"formattedText\": \"One\",\n" +
                "        \"tokenization\": \"U1W/PB+68ToHHYBY3+6OpcQgKFexbfOZKOAbOjfcHzLrMyhIPRYMykThfccBk5ewWiWTAk1obwcD0EZI6747xvd3589RHBwWHuPMMsamgJJSgh9hxUqvF67h1UlHYtxC8J9aj1C8fbca0/I0KuFhiWCQHDLi9zyjJhdr+QW4Z2DpHjYe6TF3D7DeCQqi3x0ga6FjpCl9hVcuhZQi6xeke39wb0qmc7Jq1KKkqP9WdWH2V1/F31Ma4MKOFmtFVEFNIGdD5ITweFZ/amu1O4tci7Q4D4/j8rvhHkCNO60fYmIUErR7Vnkkk0dHdVmGontgBEtOzwd3U+ioSAhGyTxImw==6mJURG7V5ulPEhTvx3aGA+RZFSk8QV3yVI6lkr6dUWmUYs8WbbSbLHo5tKwsZjqppPk2TE4WpV0p1ytr5lKqxsgi3Zga/J5wL8042y92Ef7wDr47J4rF4mcth1tK7mLS\",\n" +
                "        \"minimallyFormattedText\": \"One\"\n" +
                "      }]");

        KryptonWsEvent event = new KryptonWsEventBuilder()
                .setEvent(KryptonWsEvent.EVENT.FINAL_RESULT)
                .setMethod("Event")
                .setRequestId("b84051e3a33f2094-00004")
                .setSessionId("b84051e3a33f2094")
                .setVersion("1.0")
                .setCause(KryptonWsMessage.COMPLETION_CAUSE.SUCCESS)
                .setStatus(KryptonWsMessage.STATUS.COMPLETE)
                .setUtteranceComplete(true)
                .setAbsEnd(580)
                .setAbsStart(0)
                .setnBest(nbest.toList())
                .setUtteranceInfo(utteranceInfo.toMap())
                .build();

        JSONObject eventJson = new JSONObject(event.toJson());
        JSONObject dataJson = eventJson.getJSONObject("data");

        System.out.println(new JSONObject(event.toJson()).toString(4));
        assertEquals("FinalResult", eventJson.get("event"));
        assertEquals("Event", eventJson.get("method"));
        assertEquals("1.0", eventJson.get("version"));
        assertEquals("b84051e3a33f2094-00004", eventJson.get("requestId"));
        assertEquals("b84051e3a33f2094", eventJson.get("sessionId"));
        assertEquals("SUCCESS", dataJson.get("completion_cause"));
        assertEquals("COMPLETE", dataJson.get("status"));
        assertEquals(0, dataJson.get("absStart"));
        assertEquals(580, dataJson.get("absEnd"));

    }

    @Test
    public void buildEndOfSessionEventTest() {
        KryptonWsEventBuilder event = KryptonWsEventBuilder.buildEndOfSessionEvent("123445", "shutdown");
        JSONObject json = new JSONObject(event.build().toJson());

        assertEquals("123445", json.get("sessionId"));
        assertEquals("shutdown", json.get("reason"));

        event = new KryptonWsEventBuilder()
                .setReason("idleTimeout");

        json = new JSONObject(event.build().toJson());
        assertEquals("idleTimeout", json.get("reason"));
    }

    @Test
    public void buildFromWebapiResponseTest() throws JsonProcessingException {
        String webapiRes = "{\n" +
                "    \"type\": \"result\",\n" +
                "    \"sessionId\": \"b18f19a8-0161-4c5d-a08a-50d764e18027\",\n" +
                "    \"command\": {\n" +
                "        \"name\": \"speechRecognition\",\n" +
                "        \"id\": \"8\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"final\": true,\n" +
                "        \"status\": \"success\",\n" +
                "        \"data\": {\n" +
                "            \"transcriptions\": [\n" +
                "                \"Hi my name is\"\n" +
                "            ],\n" +
                "            \"confidence\": 910,\n" +
                "            \"averageWeightedConfidences\": [\n" +
                "                965\n" +
                "            ],\n" +
                "            \"words\": [\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"confidence\": 959,\n" +
                "                        \"word\": \"Hi\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 920,\n" +
                "                        \"word\": \"my\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 980,\n" +
                "                        \"word\": \"name\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"confidence\": 987,\n" +
                "                        \"word\": \"is\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            ]\n" +
                "        },\n" +
                "        \"bestResult\": \"Hi my name is\",\n" +
                "        \"noiseLevel\": {\n" +
                "            \"mean\": 14.28,\n" +
                "            \"standardDeviation\": 19.97\n" +
                "        },\n" +
                "        \"verboseResult\": {\n" +
                "            \"hypotheses\": [\n" +
                "                {\n" +
                "                    \"averageConfidence\": 0.959,\n" +
                "                    \"confidence\": 0.91,\n" +
                "                    \"words\": [\n" +
                "                        {\n" +
                "                            \"startMs\": 320,\n" +
                "                            \"endMs\": 640,\n" +
                "                            \"confidence\": 0.959,\n" +
                "                            \"silence_after_wordMs\": 0,\n" +
                "                            \"text\": \"Hi\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"startMs\": 640,\n" +
                "                            \"endMs\": 880,\n" +
                "                            \"confidence\": 0.92,\n" +
                "                            \"silence_after_wordMs\": 0,\n" +
                "                            \"text\": \"my\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"startMs\": 880,\n" +
                "                            \"endMs\": 1100,\n" +
                "                            \"confidence\": 0.98,\n" +
                "                            \"silence_after_wordMs\": 0,\n" +
                "                            \"text\": \"name\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"startMs\": 1100,\n" +
                "                            \"endMs\": 1300,\n" +
                "                            \"confidence\": 0.987,\n" +
                "                            \"silence_after_wordMs\": 0,\n" +
                "                            \"text\": \"is\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ],\n" +
                "            \"absStartMs\": 0,\n" +
                "            \"utteranceInfo\": {\n" +
                "                \"dsp\": {\n" +
                "                    \"numChannels\": 1,\n" +
                "                    \"level\": 19457,\n" +
                "                    \"initialSilenceMs\": 320,\n" +
                "                    \"snrEstimateDb\": 49,\n" +
                "                    \"initialEnergy\": -70.1576,\n" +
                "                    \"finalEnergy\": -14.9012,\n" +
                "                    \"meanEnergy\": 64.9249\n" +
                "                },\n" +
                "                \"clippingDurationMs\": 0,\n" +
                "                \"droppedNonspeechPackets\": 0,\n" +
                "                \"durationMs\": 2540,\n" +
                "                \"droppedSpeechPackets\": 0\n" +
                "            },\n" +
                "            \"absEndMs\": 2540\n" +
                "        }\n" +
                "    }\n" +
                "}";

        WebApiResponse res = WebApiResponse.fromJson(webapiRes);

        KryptonWsEventBuilder builder = KryptonWsEventBuilder.buildSuccessFinalResultFromResponse(res);
        JSONObject eventObj = new JSONObject(builder.build().toJson());
        JSONObject utteranceInfo = eventObj.getJSONObject("data").getJSONObject("utterance_info");
        JSONArray words = ((JSONObject) eventObj.getJSONObject("data").getJSONArray("nBest").get(0)).getJSONArray("words");
        String phrase = "";

        for (Object obj : words) {
            phrase += ((JSONObject) obj).getString("text") + " ";
        }

        assertEquals("Event", eventObj.get("method"));
        assertEquals("FinalResult", eventObj.get("event"));
        assertEquals(2540, utteranceInfo.get("duration"));
        assertEquals("Hi my name is", phrase.strip());

        WebApiResponse response2 = WebApiResponse.fromJson(cobraFinalResult);
        builder = KryptonWsEventBuilder.buildSuccessFinalResultFromResponse(response2);
        eventObj = new JSONObject(builder.build().toJson());
        utteranceInfo = eventObj.getJSONObject("data").getJSONObject("utterance_info");

        words = ((JSONObject) eventObj.getJSONObject("data").getJSONArray("nBest").get(0)).getJSONArray("words");
        phrase = "";

        for (Object obj : words) {
            phrase += ((JSONObject) obj).getString("text") + " ";
        }

        assertEquals("Event", eventObj.get("method"));
        assertEquals("FinalResult", eventObj.get("event"));
        assertEquals(2300, utteranceInfo.getInt("duration"));
        assertEquals(17921, utteranceInfo.getInt("level"));
        assertEquals("Monday Tuesday Wednesday", phrase.strip());
    }

    @Test
    public void buildFromPartialResultTest() throws JsonProcessingException {
        WebApiResponse res = WebApiResponse.fromJson(cobraPartialResult);

        KryptonWsEventBuilder builder = KryptonWsEventBuilder.buildSuccessFinalResultFromResponse(res);
        JSONObject eventObj = new JSONObject(builder.build().toJson());
        JSONArray words = ((JSONObject) eventObj.getJSONObject("data").getJSONArray("nBest").get(0)).getJSONArray("words");

        JSONObject singleWord = words.getJSONObject(0);

        assertEquals(280, singleWord.getInt("start"));
        assertEquals(600, singleWord.getInt("end"));
        assertEquals("Monday", singleWord.getString("text"));
    }

    @Test
    public void testInterpretationFields()
    {
        JSONObject data = new JSONObject();
        data.put("key", "value");

        KryptonWsEventBuilder builder = new KryptonWsEventBuilder()
                .setInterpretationData(data.toMap())
                .setInterpretationStatus("status");

        assertEquals(data.toMap(), builder.build().interpretationData);
        assertEquals("status", builder.build().interpretationStatus);
    }

    @Test
    public void testInterpretationMetrics()
    {
        JSONObject data = new JSONObject();
        data.put("key", "value");

        KryptonWsEventBuilder builder = new KryptonWsEventBuilder()
                .setInterpretationMetrics(data.toMap());

        assertEquals(data.toMap(), builder.build().interpretationMetrics);
    }
}