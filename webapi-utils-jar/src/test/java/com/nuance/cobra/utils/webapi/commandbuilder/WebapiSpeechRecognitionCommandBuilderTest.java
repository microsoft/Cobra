package com.nuance.cobra.utils.webapi.commandbuilder;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WebapiSpeechRecognitionCommandBuilderTest
{
    @Test
    public void buildRequest()
    {
        WebapiSpeechRecognitionCommandBuilder builder = getWebapiSpeechRecognitionCommandBuilder();

        JSONObject actual = builder.buildRequest();

        String expected = "{\n" +
                "  \"sessionId\": \"sessionId\",\n" +
                "  \"type\": \"command\",\n" +
                "  \"command\": {\n" +
                "    \"name\": \"speechRecognition\",\n" +
                "    \"id\": \"requestId\",\n" +
                "    \"parameters\": {\n" +
                "      \"speechRecognitionCodec\": \"speechRecognitionCodec\",\n" +
                "      \"speechDetector\": \"speechDetector\",\n" +
                "      \"speechRecognitionVerboseResult\": true,\n" +
                "      \"noiseLevelMean\": 1.1,\n" +
                "      \"language\": \"language\",\n" +
                "      \"stopOnEndOfSpeech\": true,\n" +
                "      \"startOfSpeechVoicedFrames\": 1,\n" +
                "      \"utteranceMaxTimeSeconds\": 1,\n" +
                "      \"startOfSpeechHistoryFrames\": 1,\n" +
                "      \"speechDomain\": \"speechDomain\",\n" +
                "      \"endOfSpeechHistoryFrames\": 1,\n" +
                "      \"rawEngineResult\": true,\n" +
                "      \"wordStream\": true,\n" +
                "      \"noiseLevelStandardDeviation\": 1.1,\n" +
                "      \"topic\": \"topic\",\n" +
                "      \"startOfSpeechTimeoutSeconds\": 1,\n" +
                "      \"sensitivity\": \"sensitivity\",\n" +
                "      \"beginNoiseSampleFrames\": 1,\n" +
                "      \"endOfSpeechVoicedFrames\": 1,\n" +
                "      \"dialogModelApplicationVersion\": \"dialogModelApplicationVersion\",\n" +
                "      \"voiceThreshold\": 1.1,\n" +
                "      \"considerNegativeRatios\": true,\n" +
                "      \"statistics\": true\n" +
                "    }\n" +
                "  }\n" +
                "}";

        assertEquals(new JSONObject(expected).toMap().toString(), actual.toMap().toString());
    }

    protected static WebapiSpeechRecognitionCommandBuilder getWebapiSpeechRecognitionCommandBuilder() {
        WebapiSpeechRecognitionCommandBuilder builder = new WebapiSpeechRecognitionCommandBuilder("sessionId", "requestId");

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("type", "type");
        parameters.put("sessionId", "sessionId");
//        parameters.put("command", "");
        parameters.put("name", "name");
        parameters.put("id", "id");
//        parameters.put("parameters", "");
        parameters.put("language", "language");
        parameters.put("dialogModelApplicationVersion", "dialogModelApplicationVersion");
        parameters.put("speechRecognitionCodec", "speechRecognitionCodec");
        parameters.put("endPointDetection", "true");
        parameters.put("speechDetector", "speechDetector");
        parameters.put("beginNoiseSampleFrames", 1);
        parameters.put("voiceThreshold", 1.1);
        parameters.put("startOfSpeechVoicedFrames", 1);
        parameters.put("startOfSpeechHistoryFrames", 1);
        parameters.put("endOfSpeechVoicedFrames", 1);
        parameters.put("endOfSpeechHistoryFrames", 1);
        parameters.put("considerNegativeRatios", true);
        parameters.put("stopOnEndOfSpeech", true);
        parameters.put("wordStream", true);
        parameters.put("speechDomain", "speechDomain");
        parameters.put("speechRecognitionVerboseResult", true);
        parameters.put("rawEngineResult", true);
        parameters.put("activeDynamicVocabularySets", ""); // TODO
        parameters.put("sensitivity", "sensitivity");
        parameters.put("startOfSpeechTimeoutSeconds", 1);
        parameters.put("utteranceMaxTimeSeconds", 1);
        parameters.put("statistics", true);
        parameters.put("noiseLevelMean", 1.1);
        parameters.put("noiseLevelStandardDeviation", 1.1);
        parameters.put("topic", "topic");

        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        webApiJSONConfiguration.load(parameters);

        builder.setParameters(webApiJSONConfiguration);
        return builder;
    }
}
