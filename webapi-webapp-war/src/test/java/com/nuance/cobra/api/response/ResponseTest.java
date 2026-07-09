package com.nuance.cobra.api.response;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.utils.CobraException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResponseTest
{
    private String replaceDoubleQuoteInJsonString(String s)
    {
        // Since single quote is easier to read, we define test strings with single quotes
        // and then use this method to replace them by double quote before parsing
        return s.replaceAll("'", "\"");
    }

    @Test
    public void testFromCommandChainRequest() throws CobraException
    {
        // Setup
        String request_json = "{\"type\":\"commandChain\",\"sessionId\":\"sid\",\"commands\":[{\"name\":\"speechRecognition\",\"id\":\"srid\",\"parameters\":{\"language\":\"en-us\",\"endPointDetection\":true,\"startOfSpeechHistoryFrames\":15,\"startOfSpeechVoicedFrames\":7,\"endOfSpeechHistoryFrames\":50,\"endOfSpeechVoicedFrames\":5,\"stopOnEndOfSpeech\":true,\"wordStream\":true}},{\"name\":\"speechSynthesis\",\"id\":\"ssid\",\"parameters\":{\"type\":\"text\",\"data\":\"_(result.bestResult)_\",\"voice\":\"Carol\"}}]}";
        Request request = Request.fromJson(request_json);

        // Call method
        Response response = Response.fromCommandChainRequest(request, true);

        // Check result
        String expected = "{\"type\":\"commandChainResult\",\"sessionId\":\"sid\"}";
        String actual = response.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void testFromRequest() throws CobraException
    {
        // Setup
        String json = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechRecognition', 'id': 'i1', 'parameters': {'language': 'l1', 'samplingRate': 123, 'endPointDetection': false}}}";
        json = replaceDoubleQuoteInJsonString(json);
        String expectedJson = "{'type':'result','sessionId':'s1','command':{'name':'speechRecognition','id':'i1'}}";
        String sessionLessResponse = "{'type':'result','command':{'name':'speechRecognition','id':'i1'}}";
        sessionLessResponse = replaceDoubleQuoteInJsonString(sessionLessResponse);
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        Request req = Request.fromJson(json);

        Response response = Response.fromRequest(req, false);

        assertEquals(response.toJson(), sessionLessResponse);

        // Call method
        response = Response.fromRequest(req, true);

        // Check result
        assertEquals(response.toJson(), expectedJson);

        response = Response.fromRequest(req);

        assertEquals(response.toJson(), expectedJson);
    }

    @Test
    public void testEventFromRequest() throws CobraException
    {
        // Setup
        String json = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'type': 'text', 'data':'This is a sentence.'}}}";
        json = replaceDoubleQuoteInJsonString(json);
        String expectedJson = "{'type':'event','sessionId':'s1','command':{'name':'speechSynthesis','id':'1'},'event':{'name':'testEvent'}}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        Request request = Request.fromJson(json);
        ResponseEvent responseEvent = new ResponseEvent("testEvent");

        // Call method
        Response response = Response.eventFromRequest(request, responseEvent);

        // Check result
        assertEquals(response.toJson(), expectedJson);
    }

    @Test
    public void testEventFromRequestInvalidSession() throws CobraException
    {
        // Setup
        String json = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechRecognition', 'id': 'i1', 'parameters': {'language': 'l1', 'samplingRate': 123, 'endPointDetection': false}}}";
        json = replaceDoubleQuoteInJsonString(json);
        String expectedJson = "{'type':'result','command':{'name':'speechRecognition','id':'i1'}}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        Request request = Request.fromJson(json);

        // Call method
        Response response = Response.fromRequest(request, false);

        // Check result
        assertEquals(response.toJson(), expectedJson);
    }

    @Test
    public void testInvalidRequest(){
        Response response = Response.fromInvalidRequest();
        assert(response instanceof Response);
        assertNotNull(response);

        response = Response.fromInvalidCommandChainRequest();
        assert(response instanceof Response);
        assertNotNull(response);
    }
}