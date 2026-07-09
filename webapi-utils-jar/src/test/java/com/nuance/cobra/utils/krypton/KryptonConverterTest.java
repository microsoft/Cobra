package com.nuance.cobra.utils.krypton;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KryptonConverterTest {

    @Test
    public void getVerboseResultFromJson() {
        String testJsonString = "{\n" +
                "    \"utterance_complete\": true,\n" +
                "    \"completion_cause\": \"SUCCESS\",\n" +
                "    \"absStart\": 0,\n" +
                "    \"absEnd\": 3360,\n" +
                "    \"utterance_info\": {\n" +
                "        \"duration\": 3360,\n" +
                "        \"dropped_speech_packets\": 0,\n" +
                "        \"mean_energy\": 142.078,\n" +
                "        \"initial_silence\": 520,\n" +
                "        \"level\": 24065,\n" +
                "        \"initial_energy\": -44.5059,\n" +
                "        \"final_energy\": -13.8844,\n" +
                "        \"clipping_duration\": 390,\n" +
                "        \"snr_estimate\": 19,\n" +
                "        \"dropped_non_speech_packets\": 0\n" +
                "    },\n" +
                "    \"status\": \"IN-PROGRESS\",\n" +
                "    \"nBest\": [\n" +
                "        {\n" +
                "            \"averageConfidence\": 925,\n" +
                "            \"rejected\": false,\n" +
                "            \"confidence\": 595,\n" +
                "            \"words\": [\n" +
                "                {\n" +
                "                    \"confidence\": 960,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 520,\n" +
                "                    \"end\": 700,\n" +
                "                    \"text\": \"I'm\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 924,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 700,\n" +
                "                    \"end\": 1060,\n" +
                "                    \"text\": \"telling\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 973,\n" +
                "                    \"silence_after_word\": 240,\n" +
                "                    \"start\": 1060,\n" +
                "                    \"end\": 1200,\n" +
                "                    \"text\": \"you\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 788,\n" +
                "                    \"silence_after_word\": 400,\n" +
                "                    \"start\": 1440,\n" +
                "                    \"end\": 1600,\n" +
                "                    \"text\": \"buy\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 964,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 2000,\n" +
                "                    \"end\": 2140,\n" +
                "                    \"text\": \"go\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 949,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 2140,\n" +
                "                    \"end\": 2440,\n" +
                "                    \"text\": \"away\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"formattedText\": \"I'm telling you buy go away\",\n" +
                "            \"tokenization\": \"...\",\n" +
                "            \"minimallyFormattedText\": \"I'm telling you buy go away\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"averageConfidence\": 922,\n" +
                "            \"rejected\": false,\n" +
                "            \"words\": [\n" +
                "                {\n" +
                "                    \"confidence\": 960,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 520,\n" +
                "                    \"end\": 700,\n" +
                "                    \"text\": \"I'm\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 926,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 700,\n" +
                "                    \"end\": 1060,\n" +
                "                    \"text\": \"telling\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 972,\n" +
                "                    \"silence_after_word\": 240,\n" +
                "                    \"start\": 1060,\n" +
                "                    \"end\": 1200,\n" +
                "                    \"text\": \"you\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 778,\n" +
                "                    \"silence_after_word\": 400,\n" +
                "                    \"start\": 1440,\n" +
                "                    \"end\": 1600,\n" +
                "                    \"text\": \"bye\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 946,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 2000,\n" +
                "                    \"end\": 2140,\n" +
                "                    \"text\": \"go\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"confidence\": 944,\n" +
                "                    \"silence_after_word\": 0,\n" +
                "                    \"start\": 2140,\n" +
                "                    \"end\": 2440,\n" +
                "                    \"text\": \"away\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"formattedText\": \"I'm telling you bye go away\",\n" +
                "            \"tokenization\": \"...\",\n" +
                "            \"minimallyFormattedText\": \"I'm telling you bye go away\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n" +
                "\n";

        JSONObject testJson = new JSONObject(testJsonString);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);

        when(mockConfig.getBoolean(any())).thenReturn(true);
        Map<String, Object> verboseResp = KryptonConverter.getVerboseResultFromJson(testJson);
        assertEquals(0.0, verboseResp.get("absStartMs"));
        assertEquals(3360.0, verboseResp.get("absEndMs"));
    }

    @Test
    public void convertVerboseResultToKryptonWs()
    {
        String testJsonString = "{\n" +
                "\t\"hypotheses\": [{\n" +
                "\t\t\"averageConfidence\": 0.975,\n" +
                "\t\t\"rejected\": false,\n" +
                "\t\t\"confidence\": 0.977,\n" +
                "\t\t\"words\": [{\n" +
                "\t\t\t\"startMs\": 20,\n" +
                "\t\t\t\"endMs\": 180,\n" +
                "\t\t\t\"silenceAfterWordMs\": 0,\n" +
                "\t\t\t\"confidence\": 0.975,\n" +
                "\t\t\t\"text\": \"One\"\n" +
                "\t\t}],\n" +
                "\t\t\"formattedText\": \"One\",\n" +
                "\t\t\"encryptedTokenization\": \"abc123\",\n" +
                "\t\t\"minimallyFormattedText\": \"One\"\n" +
                "\t}],\n" +
                "\t\"dataPack\": {\n" +
                "\t\t\"topic\": \"GEN\",\n" +
                "\t\t\"language\": \"eng-USA\",\n" +
                "\t\t\"id\": \"\",\n" +
                "\t\t\"version\": \"4.7.0\"\n" +
                "\t},\n" +
                "\t\"absStartMs\": 0,\n" +
                "\t\"utteranceInfo\": {\n" +
                "\t\t\"dsp\": {\n" +
                "\t\t\t\"numChannels\": 1,\n" +
                "\t\t\t\"level\": 27649.0,\n" +
                "\t\t\t\"initialSilenceMs\": 20,\n" +
                "\t\t\t\"snrEstimateDb\": 28.0,\n" +
                "\t\t\t\"initialEnergy\": -64.739,\n" +
                "\t\t\t\"finalEnergy\": -49.7646,\n" +
                "\t\t\t\"meanEnergy\": 158.316\n" +
                "\t\t},\n" +
                "\t\t\"clippingDurationMs\": 0,\n" +
                "\t\t\"droppedNonspeechPackets\": 0,\n" +
                "\t\t\"durationMs\": 580,\n" +
                "\t\t\"droppedSpeechPackets\": 0\n" +
                "\t},\n" +
                "\t\"resultType\": \"FINAL\",\n" +
                "\t\"absEndMs\": 580\n" +
                "}";

        JSONObject testJson = new JSONObject(testJsonString);
        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);

        when(mockConfig.getBoolean(any())).thenReturn(true);
        JSONObject verboseResp = KryptonConverter.convertVerboseResultToKryptonWs(testJson);

        String expectedResult = "{\"utterance_complete\":true,\"absStart\":0,\"absEnd\":580,\"completion_cause\":\"SUCCESS\",\"utterance_info\":{\"duration\":580,\"dropped_speech_packets\":0,\"mean_energy\":158.316,\"initial_silence\":20,\"level\":27649,\"initial_energy\":-64.739,\"final_energy\":-49.7646,\"stereo\":false,\"clipping_duration\":0,\"snr_estimate\":28,\"dropped_non_speech_packets\":0},\"nBest\":[{\"averageConfidence\":975,\"rejected\":false,\"confidence\":977,\"words\":[{\"silence_after_word\":0,\"confidence\":975,\"start\":20,\"end\":180,\"text\":\"One\"}],\"formattedText\":\"One\",\"tokenization\":\"abc123\",\"minimallyFormattedText\":\"One\"}],\"status\":\"COMPLETE\"}";

        assertEquals(verboseResp.toString(), expectedResult);
    }
}