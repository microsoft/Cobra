package com.nuance.plugin.kryptonws.params;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class KryptonWsRecognitionParametersTest
{
    private void testParamBoolean(String kryptonParamName, String webapiParamName, boolean value)
    {
        testParamBoolean(kryptonParamName, webapiParamName, value, value);
    }

    private void testParamBoolean(String kryptonParamName, String webapiParamName, boolean kryptonValue, boolean webapiValue)
    {
        String recoParams_string = String.format("{\"%s\": %b}", kryptonParamName, kryptonValue);

        KryptonWsParameters recoParams = new KryptonWsParameters(null, new JSONObject(recoParams_string));
        assertEquals(webapiValue, recoParams.getConfigurations().getBoolean(webapiParamName));
    }

    private void testParamString(String kryptonParamName, String webapiParamName, String value)
    {
        testParamString(kryptonParamName, webapiParamName, value, value);
    }

    private void testParamString(String kryptonParamName, String webapiParamName, String kryptonValue, String webapiValue)
    {
        String recoParams_string = String.format("{\"%s\": \"%s\"}", kryptonParamName, kryptonValue);

        KryptonWsParameters recoParams = new KryptonWsParameters(null, new JSONObject(recoParams_string));
        assertEquals(webapiValue, recoParams.getConfigurations().getString(webapiParamName));
    }

    private void testParamInt(String kryptonParamName, String webapiParamName, int value)
    {
        testParamInt(kryptonParamName, webapiParamName, value, value);
    }

    private void testParamInt(String kryptonParamName, String webapiParamName, int kryptonValue, int webapiValue)
    {
        String recoParams_string = String.format("{\"%s\": %d}", kryptonParamName, kryptonValue);

        KryptonWsParameters recoParams = new KryptonWsParameters(null, new JSONObject(recoParams_string));
        assertEquals(webapiValue, recoParams.getConfigurations().getInt(webapiParamName));
    }

    @Test
    public void audioFormat_PCM_16_8K()
    {
        testParamString("audioFormat", "speechRecognitionCodec", "audio/L16;rate=8000", "pcm_16_8k");
    }

     // 'audio/L16;rate=16000' and 'audio/x-raw;format=S16LE;rate=16000' become 'PCM_16_16K'
    @Test
    public void audioFormat_PCM_16_16K()
    {
        testParamString("audioFormat", "speechRecognitionCodec", "audio/L16;rate=16000", "pcm_16_16k");
    }

    // 'audio/basic;rate=8000' becomes 'ULAW'
    @Test
    public void audioFormat_ULAW()
    {
        testParamString("audioFormat", "speechRecognitionCodec", "audio/basic;rate=8000", "ulaw");
    }

    //'audio/x-alaw-basic;rate=8000' becomes 'ALAW'
    @Test
    public void audioFormat_ALAW()
    {
        testParamString("audioFormat", "speechRecognitionCodec", "audio/x-alaw-basic;rate=8000", "alaw");
    }

    //'enablePartialResults' becomes 'wordStream'
    @Test
    public void enablePartialResults()
    {
        testParamBoolean("enablePartialResults", "wordStream", true);
        testParamBoolean("enablePartialResults", "wordStream", false);
    }

    // 'enableUtteranceDetection' becomes 'endPointDetection'
    @Test
    public void disableUtteranceDetection()
    {
        testParamBoolean("disableUtteranceDetection", "endPointDetection", true, false);
        testParamBoolean("disableUtteranceDetection", "endPointDetection", false, true);
    }

    //'autoEnd' becomes 'stopOnEndOfSpeech'
    @Test
    public void autoEnd()
    {
        testParamBoolean("autoEnd", "stopOnEndOfSpeech", true);
        testParamBoolean("autoEnd", "stopOnEndOfSpeech", false);
    }

    //'noInputTimeout' becomes 'startOfSpeechTimeoutSeconds'
    @Test
    public void noInputTimeout()
    {
        testParamString("noInputTimeout", "startOfSpeechTimeoutSeconds", "1000ms", "1");
    }

    //'recognitionTimeout' becomes 'utteranceMaxTimeSeconds'
    @Test
    public void recognitionTimeout()
    {
        testParamString("recognitionTimeout", "utteranceMaxTimeSeconds", "1000ms", "1");
    }

    //'enableProfanityFilter' becomes 'kryptonEnableProfanityFilter'
    @Test
    public void enableProfanityFilter()
    {
        testParamBoolean("enableProfanityFilter", "kryptonEnableProfanityFilter", true);
        testParamBoolean("enableProfanityFilter", "kryptonEnableProfanityFilter", false);
    }

    //'nBestListLength' becomes 'maxTranscriptions'
    @Test
    public void nBestListLength()
    {
        testParamInt("nBestListLength", "maxTranscriptions", 5);
    }

    //'speechDomain' is forwarded as is
    @Test
    public void speechDomain()
    {
        testParamString("speechDomain", "speechDomain", "travel");
    }

    //'formattingType' becomes 'speechRecognitionFormattingType'
    @Test
    public void formattingType()
    {
        testParamString("formattingType", "speechRecognitionFormattingType", "all_as_words");
    }

    //'immutablePartialResults' becomes 'speechRecognitionImmutablePartialResults'
    @Test
    public void immutablePartialResults()
    {
        testParamBoolean("immutablePartialResults", "speechRecognitionImmutablePartialResults", true);
    }

}
