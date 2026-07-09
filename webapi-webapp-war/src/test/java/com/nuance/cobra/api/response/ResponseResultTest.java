package com.nuance.cobra.api.response;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.utils.CobraException;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ResponseResultTest
{
    private String replaceDoubleQuoteInJsonString(String s)
    {
        // Since single quote is easier to read, we define test strings with single quotes
        // and then use this method to replace them by double quote before parsing
        return s.replaceAll("'", "\"");
    }

    @Test
    public void testResponseResultOneError() throws CobraException
    {
        // Setup
        String expectedJson = "{'final':true,'status':'error','errors':[{'message':'StartSession service failed.','code':1302}]}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);

        // Call method
        ResponseResult responseResult = new ResponseResult(responseCode1);

        // Check result
        assertEquals(responseResult.toJson(), expectedJson);
    }

    @Test
    public void testResponseResultTwoErrors() throws CobraException
    {
        // Setup
        String expectedJson = "{'final':true,'status':'error','errors':[{'message':'StartSession service failed.','code':1302},{'message':'Error building JSON from the request: test.','code':1103}]}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);
        ResponseCode responseCode2 = ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR, "test");

        // Call method
        ResponseResult responseResult = new ResponseResult(responseCode1, responseCode2);

        // Check result
        assertEquals(responseResult.toJson(), expectedJson);
    }

/*
    @Test
    public void testResponseResultWithStatistics() throws CobraException
    {
        // Setup
        String expectedJson =
                "{'final':true,'status':'error','errors':[{'message':'StartSession service failed.','code':1302},{'message':'Error building JSON from the request: test.','code':1103}]}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);
        ResponseError responseError1 = ResponseError.createResponseError(START_SESSION_NMAS_ERROR);
        Command.CommandStatistics commandStatistics = new Command.CommandStatistics();
        ResponseStatistics responseStatistics = new ResponseStatistics();

        // Call method
        ResponseResult responseResult = new ResponseResult(responseError1, responseError1);

        // Check result
        assertEquals(responseResult.toJson(), expectedJson);
    }
*/
    @Test
    public void testResponseResultResponseStatus()
    {
        // Call method and check results
        assertEquals(ResponseResult.ResponseStatus.fromString("ERROR"), ResponseResult.ResponseStatus.ERROR);
        assertEquals(ResponseResult.ResponseStatus.fromString("noMatch"), ResponseResult.ResponseStatus.NO_MATCH);
        assertEquals(ResponseResult.ResponseStatus.fromString("Groundhog"), ResponseResult.ResponseStatus.ERROR);
    }

    @Test
    public void testResponseResultEquals()
    {
        // Setup
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);
        ResponseCode responseCode2 = ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR, "test");
        ResponseResult responseResult1 = new ResponseResult(responseCode1);
        ResponseResult responseResult2 = new ResponseResult(responseCode2);
        ResponseResult responseResult3 = new ResponseResult(responseCode2);

        // Call method and check results
        assertTrue(responseResult2.equals(responseResult3));
        assertTrue(responseResult1.equals(responseResult1));
        assertFalse(responseResult2.equals(responseCode2));
        assertFalse(responseResult2.equals(null));
    }

    @Test
    public void testConcurrentResponseResult() throws CobraException
    {
        ResponseCode responseCode = ResponseCode.createResponseError(ExternalResponseErrorCode.CONCURRENT_COMMAND_ERROR, "606", "THIS_IS_A_TEST_COMMAND");
        ResponseResult responseResult = new ResponseResult(responseCode);

        String expectedJson = "{'final':true,'status':'error','errors':[{'message':'Already have an active command: id = 606. name = THIS_IS_A_TEST_COMMAND','code':1203}]}";
        expectedJson = replaceDoubleQuoteInJsonString(expectedJson);

        // Check result
        assertEquals(responseResult.toJson(), expectedJson);
    }

    @Test
    public void testCommandChainResponseResult() throws CobraException {

        // Test values
        String request_json = "{\"type\":\"commandChain\",\"sessionId\":\"sid\",\"commands\":[{\"name\":\"speechRecognition\",\"id\":\"srid\",\"parameters\":{\"language\":\"en-us\",\"endPointDetection\":true,\"startOfSpeechHistoryFrames\":15,\"startOfSpeechVoicedFrames\":7,\"endOfSpeechHistoryFrames\":50,\"endOfSpeechVoicedFrames\":5,\"stopOnEndOfSpeech\":true,\"wordStream\":true}},{\"name\":\"speechSynthesis\",\"id\":\"ssid\",\"parameters\":{\"type\":\"text\",\"data\":\"_(result.bestResult)_\",\"voice\":\"Carol\"}}]}";
        Request request = Request.fromJson(request_json);
        Response response = Response.fromCommandChainRequest(request, true);

        // Method tested
        ResponseResult result = new ResponseResult();
        response.setResult(result);

        // Check values
        String expected = "{\"type\":\"commandChainResult\",\"sessionId\":\"sid\",\"result\":{\"final\":true,\"status\":\"success\"}}";
        String actual = response.toJson();

        assertEquals(expected, actual);

        // Method tested
        result = new ResponseResult("nextCommand");
        response.setResult(result);

        // Check values
        expected = "{\"type\":\"commandChainResult\",\"sessionId\":\"sid\",\"result\":{\"final\":false,\"status\":\"success\",\"nextCommand\":\"nextCommand\"}}";
        actual = response.toJson();

        assertEquals(expected, actual);
    }

    @Test
    public void testSetGetFirstIntermediateResponse(){
        boolean toTest = true;
        ResponseResult testRes = new ResponseResult();
        testRes.setIsFirstIntermediateResponse(toTest);
        assertTrue(testRes.getIsFirstIntermediateResponse());
        testRes.setIsFirstIntermediateResponse(!toTest);
        assertFalse(testRes.getIsFirstIntermediateResponse());
    }
}