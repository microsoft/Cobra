package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONObject;
import org.junit.Test;

import static com.nuance.cobra.utils.webapi.commandbuilder.WebapiInterpretationCommandBuilderTest.getWebapiInterpretationCommandBuilder;
import static com.nuance.cobra.utils.webapi.commandbuilder.WebapiSpeechRecognitionCommandBuilderTest.getWebapiSpeechRecognitionCommandBuilder;
import static org.junit.Assert.assertEquals;

public class WebapiCommandChainBuilderTest {

    @Test
    public void testBuildRequest() {
        var webapiCommandChainBuilder = new WebapiCommandChainBuilder();
        webapiCommandChainBuilder.addCommand(getWebapiInterpretationCommandBuilder());
        webapiCommandChainBuilder.addCommand(getWebapiSpeechRecognitionCommandBuilder());
        var result = webapiCommandChainBuilder.buildRequest();
        assertEquals("{\"sessionId\":\"sessionId\",\"type\":\"commandChain\",\"commands\":[{\"name\":\"interpretation\",\"id\":\"requestId\",\"parameters\":{\"data\":\"data\",\"interpretationVerboseResult\":false,\"activeDynamicVocabularySets\":[\"one\",\"two\"],\"language\":\"language\",\"sensitivity\":\"sensitivity\",\"type\":\"type\",\"dialogModelApplicationVersion\":\"dialogModelApplicationVersion\",\"statistics\":true}},{\"name\":\"speechRecognition\",\"id\":\"requestId\",\"parameters\":{\"speechRecognitionCodec\":\"speechRecognitionCodec\",\"speechDetector\":\"speechDetector\",\"speechRecognitionVerboseResult\":true,\"noiseLevelMean\":1.1,\"language\":\"language\",\"stopOnEndOfSpeech\":true,\"startOfSpeechVoicedFrames\":1,\"utteranceMaxTimeSeconds\":1,\"startOfSpeechHistoryFrames\":1,\"speechDomain\":\"speechDomain\",\"endOfSpeechHistoryFrames\":1,\"rawEngineResult\":true,\"wordStream\":true,\"noiseLevelStandardDeviation\":1.1,\"topic\":\"topic\",\"startOfSpeechTimeoutSeconds\":1,\"sensitivity\":\"sensitivity\",\"beginNoiseSampleFrames\":1,\"endOfSpeechVoicedFrames\":1,\"dialogModelApplicationVersion\":\"dialogModelApplicationVersion\",\"voiceThreshold\":1.1,\"considerNegativeRatios\":true,\"statistics\":true}}]}", result.toString());
    }
}
