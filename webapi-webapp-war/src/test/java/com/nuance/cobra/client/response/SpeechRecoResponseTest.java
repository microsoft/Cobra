package com.nuance.cobra.client.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.nuance.cobra.utils.audio.NoiseLevel;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.Assert.*;

public class SpeechRecoResponseTest {

    @Test
    public void testJson() throws IOException {
        String[] testTranscripts = new String[]{"test 800", "test 900", "test 500", "test 990", "test 100"};
        int[] testAverageConfidence = new int[]{800, 900, 500, 990, 100};
        SpeechRecoResponse.Word[][] testWords = new SpeechRecoResponse.Word[testTranscripts.length][2];
        for (int i = 0; i < testTranscripts.length; ++i) {
            String[] wordSplit = testTranscripts[i].split(" ");
            SpeechRecoResponse.Word[] words = new SpeechRecoResponse.Word[wordSplit.length];
            for (int j = 0; j < wordSplit.length; ++j) {
                SpeechRecoResponse.Word newWord = new SpeechRecoResponse.Word(wordSplit[j], 999);
                words[j] = newWord;
            }
            testWords[i] = words;
        }
        SpeechRecoResponse.Data testData = new SpeechRecoResponse.Data(testTranscripts, testAverageConfidence, 900, testWords, -1);

        SpeechRecoResponse testResponse = new SpeechRecoResponse(true, testData, new NoiseLevel(0.0, 0.0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testResponse);

        assertEquals("test 800", testResponse.getBestResult());

        assertTrue(jsonResult.contains("[ \"test 800\", \"test 900\", \"test 500\", \"test 990\", \"test 100\" ]"));
        assertTrue(jsonResult.contains("\"averageWeightedConfidences\" : [ 800, 900, 500, 990, 100 ],"));
    }

    @Test
    public void testJsonMaxTranscriptions() throws IOException {
        String[] testTranscripts = new String[]{"test 800", "test 900", "test 500", "test 990", "test 100"};
        int[] testAverageConfidence = new int[]{800, 900, 500, 990, 100};
        SpeechRecoResponse.Word[][] testWords = new SpeechRecoResponse.Word[testTranscripts.length][2];
        for (int i = 0; i < testTranscripts.length; ++i) {
            String[] wordSplit = testTranscripts[i].split(" ");
            SpeechRecoResponse.Word[] words = new SpeechRecoResponse.Word[wordSplit.length];
            for (int j = 0; j < wordSplit.length; ++j) {
                SpeechRecoResponse.Word newWord = new SpeechRecoResponse.Word(wordSplit[j], 999);
                words[j] = newWord;
            }
            testWords[i] = words;
        }
        SpeechRecoResponse.Data testData = new SpeechRecoResponse.Data(testTranscripts, testAverageConfidence, 900, testWords,  2);

        SpeechRecoResponse testResponse = new SpeechRecoResponse(true, testData, new NoiseLevel(0.0, 0.0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testResponse);

        assertEquals("test 800", testResponse.getBestResult());

        assertTrue(jsonResult.contains("[ \"test 800\", \"test 900\" ]"));
        assertTrue(jsonResult.contains("\"averageWeightedConfidences\" : [ 800, 900 ],"));
    }

    @Test
    public void testVerboseResult()
            throws ParseException
    {
        String t1 = "{\"resultType\":\"PARTIAL\",\"absStartMs\":0,\"absEndMs\":1490,\"hypotheses\":[{\"rejected\":false,\"formattedText\":\"One\",\"minimallyFormattedText\":\"One\",\"words\":[{\"text\":\"One\",\"startMs\":1260,\"endMs\":1400,\"silenceAfterWordMs\":0}],\"encryptedTokenization\":\"\"}],\"dataPack\":{\"language\":\"eng-USA\",\"topic\":\"GEN\",\"version\":\"4.7.0\",\"id\":\"\"}}";
        SpeechRecoResponse speechRecoResponse = new SpeechRecoResponse(false, new NoiseLevel(0.0, 0.0));
        Gson gson = new GsonBuilder().create();
        speechRecoResponse.setVerboseResult(gson.fromJson(t1, Map.class));
        Map<String, Object> m1 = speechRecoResponse.getVerboseResult();

        //Prevent ints being converted to floats e.g. prevent 1490 becoming 1490.0
        gson = new GsonBuilder().registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
                                                 @Override
                                                 public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                                                     if(src == src.longValue())
                                                         return new JsonPrimitive(src.longValue());
                                                     return new JsonPrimitive(src);
                                                 }
                                             }).create();

        String t11 = gson.toJson(m1);
        assertEquals(t1, t11);
    }
}