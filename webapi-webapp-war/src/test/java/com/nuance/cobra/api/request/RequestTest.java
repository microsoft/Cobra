package com.nuance.cobra.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.command.*;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RequestTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String replaceDoubleQuoteInJsonString(String s)
    {
        // Since single quote is easier to read, we define test strings with single quotes
        // and then use this method to replace them by double quote before parsing
        return s.replaceAll("'", "\"");
    }

    @Test
    public void testStartSessionDeviceIdValid()
            throws CobraException
    {
        // Test values
        Request req = null;
        String test =
                "{'version': '1.0', 'type': 'command', 'command': {'name': 'startSession', 'id': 'i1', 'parameters': {'applicationKey': 'k1', 'verificationCode':'5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c', 'clientDeviceId' : 'test1.1'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
        }
        catch (CobraException e)
        {
            fail(e.getMessage());
        }

        assertTrue(req.requestCommand.parameters.containsKey(WebApiConfig.Property.CLIENT_DEVICE_ID.key));
        assertTrue(!((String) req.requestCommand.parameters.get(WebApiConfig.Property.CLIENT_DEVICE_ID.key)).contains("INVALID"));
    }

    @Test(expected = CobraException.class)
    public void testStartSessionDeviceIdInvalid()
            throws CobraException
    {
        // Test values
        Request req = null;
        String test =
                "{'version': '1.0', 'type': 'command', 'command': {'name': 'startSession', 'id': 'i1', 'parameters': {'applicationKey': 'k1', 'verificationCode':'5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c', 'clientDeviceId' : 'test1%1'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
        }
        catch (CobraException exception)
        {
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test
    public void testStartSessionDeviceIdInvalidJson()
            throws CobraException
    {
        // Test values


       thrown.expect(CobraException.class);

        Request req = null;
        String test =
                "{'version': '1.0', 'type': 'command', 'command': {'name': 'startSession', 'id': 'i1', 'parameters': {'applicationKey': 'k1', 'verificationCode':'5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c', 'clientDeviceId' : 'test1%1'}}";
        String json = replaceDoubleQuoteInJsonString(test);


            // Method tested
            //
            req = Request.fromJson(json);
//            fail();


    }

    @Test(expected = CobraException.class)
    public void testStartSessionMissingCommand() throws CobraException
    {
        // Test values
        Request req = null;
        String test = "{'version': '1.0', 'type': 'command'}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
            fail();
        }
        catch(CobraException exception)
        {
            // Check values
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test(expected = CobraException.class)
    public void testStartSessionMissingCommandName() throws Exception
    {
        // Test values
        Request req = null;
        String test = "{'version': '1.0', 'type': 'command', 'command': {'id': 'i1'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
            fail();
        }
        catch(CobraException exception)
        {
            // Check values
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test(expected = CobraException.class)
    public void testStartSessionMissingCommandId() throws Exception
    {
        // Test values
        Request req = null;
        String test = "{'version': '1.0', 'type': 'command', 'command': {'name': 'startSession'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
            fail();
        }
        catch(CobraException exception)
        {
            // Check values
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test(expected = CobraException.class)
    public void testStartSessionWrongCommandName() throws Exception
    {
        // Test values
        Request req = null;
        String test = "{'version': '1.0', 'type': 'command', 'command': {'name': 'ZZZstartSession', 'id': 'i1'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            req = Request.fromJson(json);
            fail();
        }
        catch (CobraException exception)
        {
            // Check values
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test
    public void testStartSession() throws Exception
    {
        // Test values
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(WebApiConfig.Property.APPLICATION_KEY.key, "k1");
        parameters.put(WebApiConfig.Property.VERIFICATION_CODE.key, "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c");
        String test = "{'version': '1.0', 'type': 'command', 'command': {'name': 'startSession', 'id': 'i1', 'parameters': {'applicationKey': 'k1', 'verificationCode':'5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertEquals(req.version, "1.0");
        assertEquals(req.type, Request.RequestType.COMMAND);
        assertNull(req.sessionId);
        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(StartSessionCommand.class));
        assertEquals(req.requestCommand.id, "i1");
        assertNull(req.requestDirective);
        assertEquals(req.requestCommand.parameters, parameters);
    }

    @Test
    public void testEndSession() throws Exception
    {
        // Test values
        String test = "{'type': 'command','sessionId': 's1', 'command': {'name': 'endSession', 'id': 'i1'}}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertNull(req.version);
        assertEquals(req.type, Request.RequestType.COMMAND);
        assertEquals(req.sessionId, "s1");
        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(EndSessionCommand.class));
        assertEquals(req.requestCommand.id, "i1");
        assertNull(req.requestDirective);
        assertTrue(req.requestCommand.parameters.isEmpty());
    }

    @Test
    public void testCommandSpeechRecognition() throws CobraException
    {
        // Test values
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "l1");
        parameters.put(WebApiConfig.Property.END_POINT_DETECTION.key, false);
        parameters.put(WebApiConfig.Property.VOICE_THRESHOLD.key, 2.5);
        parameters.put(WebApiConfig.Property.BEGIN_NOISE_SAMPLE_FRAMES.key, 23);
        parameters.put(WebApiConfig.Property.START_OF_SPEECH_FRAMES.key, 15);
        parameters.put(WebApiConfig.Property.END_OF_SPEECH_FRAMES.key, 29);
        parameters.put(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key, false);
        parameters.put(WebApiConfig.Property.START_OF_SPEECH_TIMEOUT_SECONDS.key, 3);
        parameters.put(WebApiConfig.Property.UTTERANCE_MAX_TIME_SECONDS.key, 29);
        parameters.put(WebApiConfig.Property.WORD_STREAM.key, false);

        String test = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechRecognition', 'id': 'i1', 'parameters': " +
                "{'language': 'l1', 'endPointDetection': false," +
                "'voiceThreshold': 2.5, 'beginNoiseSampleFrames': 23, 'startOfSpeechFrames': 15," +
                "'endOfSpeechFrames': 29, 'stopOnEndOfSpeech': false, 'startOfSpeechTimeoutSeconds': 3," +
                "'utteranceMaxTimeSeconds': 29, 'wordStream': false}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertNull(req.version);
        assertEquals(req.type, Request.RequestType.COMMAND);
        assertEquals(req.sessionId, "s1");
        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(SpeechRecognitionCommand.class));
        assertEquals(req.requestCommand.id, "i1");
        assertNull(req.requestDirective);
        assertEquals(req.requestCommand.parameters, parameters);
    }

    @Test
    public void testCommandSpeechSynthesis() throws Exception
    {
        // Test values
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text");
        parameters.put(WebApiConfig.Property.COMMAND_DATA.key, "This is a sentence.");
        String test = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'type': 'text', 'data':'This is a sentence.'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertNull(req.version);
        assertEquals(req.type, Request.RequestType.COMMAND);
        assertEquals(req.sessionId, "s1");
        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(SpeechSynthesisCommand.class));
        assertEquals(req.requestCommand.id, "1");
        assertNull(req.requestDirective);
        assertEquals(req.requestCommand.parameters, parameters);
    }

    @Test
    public void testCommandDirectiveSpeechSynthesis() throws Exception
    {
        // Test values
        String test = "{'type': 'directive', 'sessionId': 's1', 'command': {'name': 'speechSynthesis', 'id': 'i1'}, 'directive': {'name': 'stop'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertNull(req.version);
        assertEquals(req.type, Request.RequestType.DIRECTIVE);
        assertEquals(req.sessionId, "s1");
        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(SpeechSynthesisCommand.class));
        assertEquals(req.requestCommand.id, "i1");
        assertEquals(req.requestDirective.getName(), RequestDirective.RequestDirectiveName.STOP);
        assertTrue(req.requestCommand.parameters.isEmpty());
    }

    @Test
    public void testCommandChain() throws Exception
    {
        // Test values
        Map<String, Object> recoCommandParameters = new HashMap<>();
        recoCommandParameters.put(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "en-us");
        recoCommandParameters.put(WebApiConfig.Property.END_POINT_DETECTION.key, true);
        recoCommandParameters.put(WebApiConfig.Property.START_OF_SPEECH_HISTORY_FRAMES.key, 15);
        recoCommandParameters.put(WebApiConfig.Property.START_OF_SPEECH_VOICED_FRAMES.key, 7);
        recoCommandParameters.put(WebApiConfig.Property.END_OF_SPEECH_HISTORY_FRAMES.key, 50);
        recoCommandParameters.put(WebApiConfig.Property.END_OF_SPEECH_VOICED_FRAMES.key, 5);
        recoCommandParameters.put(WebApiConfig.Property.STOP_ON_END_OF_SPEECH.key, true);
        recoCommandParameters.put(WebApiConfig.Property.WORD_STREAM.key, true);

        Map<String, Object> ttsCommandParameters = new HashMap<>();
        ttsCommandParameters.put(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text");
        ttsCommandParameters.put(WebApiConfig.Property.COMMAND_DATA.key, "_(result.bestResult)_");
        ttsCommandParameters.put(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "Carol");

        String test = "{'type':'commandChain','sessionId':'sid','commands':[{'name':'speechRecognition','id':'srid','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}},{'name':'speechSynthesis','id':'ssid','parameters':{'type':'text','data':'_(result.bestResult)_','voice':'Carol'}}]}";
        String json = replaceDoubleQuoteInJsonString(test);

        // Method tested
        Request req = Request.fromJson(json);

        String firstCommandOfTheCommandChain = Request.toSingleCommandRequestJson(req);
        Request singleCommandRequest = Request.fromJson(firstCommandOfTheCommandChain);
        req.requestCommand = singleCommandRequest.requestCommand;

        // Check values
        assertNull(req.version);
        assertEquals(req.type, Request.RequestType.COMMAND_CHAIN);
        assertEquals(req.sessionId, "sid");
        assertNull(req.requestDirective);

        assertEquals(req.requestCommand.name, CommandFactory.getCommandName(SpeechRecognitionCommand.class));
        assertEquals(req.requestCommand.id, "srid");
        assertEquals(req.requestCommand.parameters, recoCommandParameters);

        assertEquals(req.requestCommands.get(1).get("name"), CommandFactory.getCommandName(SpeechSynthesisCommand.class));
        assertEquals(req.requestCommands.get(1).get("id"), "ssid");
        assertEquals(req.requestCommands.get(1).get("parameters"), ttsCommandParameters);
    }

    @Test
    public void testBuildJsonSchemaFromFileNameError()
    {
        try
        {
            // Method tested
            Request.buildJsonSchemaFromFileName("unknownFile.json");
            fail();
        }
        catch (Exception e)
        {
            // Check values
            assertEquals(e.getMessage(), "Cannot get json schema: unknownFile.json with getResourceAsStream()");
        };
    }

    @Test
    public void testBuildJsonSchemaFromRequestError() throws Exception
    {
        // Test value
        ObjectMapper mapper = new ObjectMapper();
        String test = "{'type': 'command', 'command': {'name': 'speechSynthesis', 'id': '1'}}";
        String json = replaceDoubleQuoteInJsonString(test);
        RequestBasic requestBasic = mapper.readValue(json, RequestBasic.class);
        try
        {
            // Method tested
            requestBasic.command.setName(null);
            Request.buildJsonSchemaFromRequest(requestBasic);
            fail();
        }
        catch (Exception e)
        {
            // Check values
            assertEquals(e.getMessage(), "Cannot get schema from command name: null");
        };
    }

    @Test
    public void testBuildJsonSchemaFromDirectiveRequest() throws Exception
    {
        // Test value
        ObjectMapper mapper = new ObjectMapper();
        String test = "{'type': 'directive', 'command': {'name': 'speechSynthesis', 'id': '1'}}";
        String json = replaceDoubleQuoteInJsonString(test);
        RequestBasic requestBasic = mapper.readValue(json, RequestBasic.class);
        requestBasic.command.setName(null);
        Schema sc = Request.buildJsonSchemaFromRequest(requestBasic);
        assertNotNull("Schema is not null", sc);
    }

    @Test(expected = CobraException.class)
    public void testFromJsonInvalidCommand() throws Exception
    {
        // Test values
        String test = "{'version': '1.0', 'type': 'command', 'command': {'name': 'ZZZstartSession', 'id': 'i1'}}}";
        String json = replaceDoubleQuoteInJsonString(test);

        try
        {
            // Method tested
            Request req = Request.fromJson(json);
            fail();
        }
        catch (CobraException exception)
        {
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test(expected = CobraException.class)
    public void testFromJsonInvalidCommandChain() throws Exception
    {
        // Test values
        String test = "{'type':'commandChain','sessionId':'sid','commands':[{'name':'ZZZspeechRecognition','id':'srid','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}},{'name':'speechSynthesis','id':'ssid','parameters':{'type':'text','data':'_(result.bestResult)_','voice':'Carol'}}]}";
        String json = replaceDoubleQuoteInJsonString(test);

        Request req;
        String firstCommandOfTheCommandChain;

        try
        {
            req = Request.fromJson(json);
            firstCommandOfTheCommandChain = Request.toSingleCommandRequestJson(req);
        } catch (CobraException e)
        {
            fail();
            return;
        }

        try
        {
            // Method tested
            Request singleCommandRequest = Request.fromJson(firstCommandOfTheCommandChain);
            req.requestCommand = singleCommandRequest.requestCommand;

            fail();
        }
        catch (CobraException exception)
        {
            assertEquals(1,                                               exception.getResult().getErrors().size());
            assertEquals(ExternalResponseErrorCode.REQUEST_PARSE_ERROR.getValue(), exception.getResult().getErrors().get(0).getCode());
            throw exception;
        }
    }

    @Test
    public void testForNullRequestCommandChain(){
        try{
            Request.toSingleCommandRequestJson(null);
            fail();
        }catch (CobraException e){
            assertEquals(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR.getValue(), e.getResult().getErrors().get(0).getCode());
        }
    }

    @Test
    public void testToJson() throws Exception
    {
        // Test values
        String test = "{'type':'command','sessionId':'s1','command':{'name':'speechSynthesis','id':'1','parameters':{'type':'text','data':'This is a sentence.'}}}";
        String jsonSource = replaceDoubleQuoteInJsonString(test);
        Request request = Request.fromJson(jsonSource);

        // Method tested
        String jsonTarget = request.toJson(request);

        // Check value. We can't compare directly jsonSource and jsonTarget since the members
        // are not in the same order. We need to put them in JSONObject to compare them!
        JSONObject sourceJsonObject = new JSONObject(jsonSource);
        JSONObject targetJsonObject = new JSONObject(jsonTarget);
        assertEquals(sourceJsonObject.toString(), targetJsonObject.toString());
    }

    @Test (expected = CobraException.class)
    public void testToJson_commandChain() throws Exception
    {
        // Test values
        String test = "{'type':'commandChain','chainId':'chId','sessionId':'sid','commands':[{'name':'speechRecognition','id':'srid','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}},{'name':'speechSynthesis','id':'ssid','parameters':{'type':'text','data':'_(result.bestResult)_','voice':'Carol'}}]}";
        String jsonSource = replaceDoubleQuoteInJsonString(test);
        Request request = Request.fromJson(jsonSource);

        // Method tested
        String jsonTarget = request.toJson(request);

        // Check value. We can't compare directly jsonSource and jsonTarget since the members
        // are not in the same order. We need to put them in JSONObject to compare them!
        JSONObject sourceJsonObject = new JSONObject(jsonSource);
        JSONObject targetJsonObject = new JSONObject(jsonTarget);
        assertEquals(sourceJsonObject.toString(), targetJsonObject.toString());

        request = null;
        jsonTarget = request.toJson(request);
        fail();
    }

    @Test
    public void testToString() throws Exception
    {
        // Test values
        String test = "{'type': 'command', 'sessionId': 's1', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'type': 'text', 'data':'This is a sentence.'}}}";
        String json = replaceDoubleQuoteInJsonString(test);
        String testToString = "Type: COMMAND\n" + "Version: null\n" + "SessionId: s1\n" + "Command: \n" + "---name: speechSynthesis\n";
        testToString += "---id: 1\n" + "Parameters: [type=text, data=This is a sentence.]\n" + "Directive: null";

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertEquals(req.toString(), testToString);
    }

    @Test
    public void testToString_commandChain() throws Exception
    {
        // Test values
        String test = "{'type':'commandChain','sessionId':'sid','commands':[{'name':'speechRecognition','id':'srid','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}},{'name':'speechSynthesis','id':'ssid','parameters':{'type':'text','data':'_(result.bestResult)_','voice':'Carol'}}]}";
        String json = replaceDoubleQuoteInJsonString(test);
        String testToString = "Type: COMMAND_CHAIN\n" +
                "SessionId: sid\n" +
                "Command1: {name=speechRecognition, id=srid, parameters={language=en-us, endPointDetection=true, startOfSpeechHistoryFrames=15, startOfSpeechVoicedFrames=7, endOfSpeechHistoryFrames=50, endOfSpeechVoicedFrames=5, stopOnEndOfSpeech=true, wordStream=true}}\n" +
                "Command2: {name=speechSynthesis, id=ssid, parameters={type=text, data=_(result.bestResult)_, voice=Carol}}";

        // Method tested
        Request req = Request.fromJson(json);

        // Check values
        assertEquals(testToString, req.toString());
    }

    
    @Test
    public void validateWrongCommandChain(){
        String testToString = "Type: COMMAND_CHAIN\n" +
                "SessionId: sid\n" +
                "Command1: {name=speechRecognition, id=srid, parameters={language=en-us, endPointDetection=true, startOfSpeechHistoryFrames=15, startOfSpeechVoicedFrames=7, endOfSpeechHistoryFrames=50, endOfSpeechVoicedFrames=5, stopOnEndOfSpeech=true, wordStream=true}}\n" +
                "Command2: name=speechSynthesis, id=ssid, parameters={type=text, data=_(result.bestResult)_, voice=Carol}}";
        assertEquals(false, Request.isCommandChainRequest(testToString));
    }
}