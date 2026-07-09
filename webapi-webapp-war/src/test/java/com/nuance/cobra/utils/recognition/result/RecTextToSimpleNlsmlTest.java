package com.nuance.cobra.utils.recognition.result;

import com.nuance.cobra.client.response.SpeechRecoResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecTextToSimpleNlsmlTest
{
    @Test
    public void testRecTextToSimpleNlsml()
    {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result grammar=\"http://grammar\"><interpretation grammar=\"http://grammar\" confidence=\"1.0\"><instance/><input mode=\"voice\">Tell me my transactions</input></interpretation><interpretation grammar=\"http://grammar\" confidence=\"0.002\"><instance/><input mode=\"voice\">Tell me my</input></interpretation></result>";

        String result = RecTextToSimpleNlsml.recTextToSimpleNlsml(buildData(), 1000.0f, "http://grammar");

        assertEquals(expectedResult, result);
    }

    private SpeechRecoResponse.Data buildData()
    {
        final String[] transcriptions = { "Tell me my transactions", "Tell me my" };
        final int confidence = 1000;
        final int[] averageWeightedConfidences = { 1000, 2 };
        final SpeechRecoResponse.Word[][] words = new SpeechRecoResponse.Word[2][];

        SpeechRecoResponse.Data data = new SpeechRecoResponse.Data(transcriptions, averageWeightedConfidences, confidence, words, -1);
        return data;
    }
}