package com.nuance.cobra.api.request;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RequestCommandTest {

    private String replaceDoubleQuoteInJsonString(String s)
    {
        // Since single quote is easier to read, we define test strings with single quotes
        // and then use this method to replace them by double quote before parsing
        return s.replaceAll("'", "\"");
    }

    @Test
    public void testToString2_commandChain() throws Exception
    {
        // Test values
        String test = "{'type':'commandChain','sessionId':'sid','commands':[{'name':'speechRecognition','id':'srid','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}},{'name':'speechSynthesis','id':'ssid','parameters':{'type':'text','data':'_(result.bestResult)_','voice':'Carol'}}]}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        String firstCommandOfTheCommandChain = Request.toSingleCommandRequestJson(req);
        Request singleCommandRequest = Request.fromJson(firstCommandOfTheCommandChain);
        req.requestCommand = singleCommandRequest.requestCommand;

        // Check value. We can't compare directly jsonSource and jsonTarget since the members
        // are not in the same order. We need to put them in JSONObject to compare them!
        String expected_String = "{\n" +
                "  \"name\": \"speechRecognition\",\n" +
                "  \"id\": \"srid\",\n" +
                "  \"parameters\": {\n" +
                "    \"language\": \"en-us\",\n" +
                "    \"endPointDetection\": true,\n" +
                "    \"startOfSpeechHistoryFrames\": 15,\n" +
                "    \"startOfSpeechVoicedFrames\": 7,\n" +
                "    \"endOfSpeechHistoryFrames\": 50,\n" +
                "    \"endOfSpeechVoicedFrames\": 5,\n" +
                "    \"stopOnEndOfSpeech\": true,\n" +
                "    \"wordStream\": true\n" +
                "  }\n" +
                "}";

        JSONObject expected = new JSONObject(expected_String);
        JSONObject actual = new JSONObject(req.requestCommand.toJsonString());

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void textToString(){
        Map<String, Object> requestCommand = new HashMap<String, Object>();
        Map<String, Object> parameters = new HashMap<String, Object>();
        requestCommand.put("name", "Test");
        requestCommand.put("id", "Test");
        parameters.put("language","en-US");
        parameters.put("stopOnEndOfSpeech",true);
        requestCommand.put("parameters",parameters);
        RequestCommand command = new RequestCommand(requestCommand);

        String expectedString = "\n---name: Test\n" +
                "---id: Test\n" +
                "Parameters: " + Arrays.toString(parameters.entrySet().toArray()) ;

        assertEquals(expectedString, command.toString());

    }
}
