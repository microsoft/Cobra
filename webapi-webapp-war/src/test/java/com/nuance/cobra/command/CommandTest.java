package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestCommand;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.EndSessionResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.encryption.Encryptor;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseFormatter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static com.nuance.cobra.command.Command.CommandSensitivityType.MASKED;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class, SessionManager.class})
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class CommandTest extends CobraTest {
    Request request;
    RequestCommand requestCommand;
    SessionManager mockSessionManager;
    Session mockSession;
    CombinedConfiguration testConfs;
    Configuration testConf;
    Response response;
    ResponseResult responseResult;
    CommandLog commandLog;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        testConf = CobraConfigurationUtils.getHighestConfig(testConfs);

        mockSessionManager = mock(SessionManager.class);
        mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        PowerMockito.mockStatic(SessionManager.class);
        when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());

        String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + "startSession" +
                "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"applicationKey\" , \"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\" } }}";
        try {
            request = Request.fromJson(requestString);
        } catch (CobraException e) {
            e.printStackTrace();
        }
        requestCommand = mock(RequestCommand.class);
        requestCommand.name = CommandFactory.getCommandName(SpeechSynthesisCommand.class);
        Whitebox.setInternalState(request, "requestCommand", requestCommand);
        testConf.setProperty(WebApiConfig.Property.HOST_ENVIRONMENT.key, ("pred dev"));
        testConf.setProperty(WebApiConfig.Property.HOST_REGION.key, ("mtl"));
        testConf.setProperty(WebApiConfig.Property.API_VERSION.key, ("1.0"));
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, ("123"));
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key, ("DV_123"));
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key, ("Linux"));
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key, ("4.14"));
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key, ("as23213df"));
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key, ("appName"));
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, ("1"));
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, (Base64.encodeBase64String("12345".getBytes())));
        testConf.setProperty(WebApiConfig.Property.CLIENT_PUBLIC_KEY.key, "MTIzNDG7Jki2");
        testConf.setProperty(WebApiConfig.Property.COMMAND_PARAMS_TO_REMOVE_FROM_LOGS.key, new ArrayList<Object>());
        testConf.setProperty(WebApiConfig.Property.KRYPTON_GRPC_USE_USERID.key, (true));
        when(mockSession.getSessionConfig()).thenReturn(testConfs);

        PowerMockito.mockStatic(WebApiConfigManager.class);
        when(WebApiConfigManager.getInstance()).thenReturn(testConfs);

//        response = mock(Response.class);
        response = Response.fromRequest(request);
        responseResult = new ResponseResult();
        Whitebox.setInternalState(responseResult, "status", ResponseResult.ResponseStatus.SUCCESS);
//        response.getResult(,(responseResult);
//        response.getEdfCopy(,(requestCommand);

        response.setResult(responseResult);
        response.setEdfCopy(requestCommand);


        commandLog = mock(CommandLog.class);
        try {
            when(commandLog.toJson(false)).thenReturn("{bob}");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            PowerMockito.whenNew(CommandLog.class).withAnyArguments().thenReturn(commandLog);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetApiName() {

        Command command = mock(Command.class);

        String s = command.getAPIName();
        assertNull(s);

        requestCommand.name = CommandFactory.getCommandName(StartSessionCommand.class);
        request.requestCommand = requestCommand;

        StartSessionCommand commandObject = new StartSessionCommand(request, testConfs, null);
        s = commandObject.getAPIName();
        assertTrue(s.equals("startSession"));

        request.requestCommand.name = CommandFactory.getCommandName(SpeechSynthesisCommand.class);
        s = commandObject.getAPIName();
        assertTrue(s.equals("speechSynthesis"));

        request.requestCommand.name = CommandFactory.getCommandName(SpeechRecognitionCommand.class);
        s = commandObject.getAPIName();
        assertTrue(s.equals("speechRecognition"));

        request.requestCommand.name = CommandFactory.getCommandName(UploadDynamicVocabularyCommand.class);
        s = commandObject.getAPIName();
        assertTrue(s.equals("uploadDynamicVocabulary"));

        request.requestCommand.name = CommandFactory.getCommandName(EndSessionCommand.class);
        s = commandObject.getAPIName();
        assertTrue(s.equals("endSession"));

    }

    @Test
    public void testPrettyFormatResponse() {
        Response response = mock(Response.class);
        Command command = mock(Command.class);
        RequestCommand requestCommand = mock(RequestCommand.class);
        response = command.prettyFormatResponse(response);
    }

    @Test
    public void testFormatLog()
            throws JsonProcessingException {
        Command testCmd = new Command(request, testConfs, null) {
            @Override
            protected void executeImplementation() {

            }

            @Override
            protected void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal) {

            }

            @Override
            protected void onAudioImplementation(byte[] audio) {

            }

            @Override
            protected boolean onResponseImplementation(ResponseResult response) {
                return false;
            }

            @Override
            protected boolean onAudioResponseImplementation(byte[] audio, boolean isLastBuffer) {
                return false;
            }

            @Override
            public ResponseFormatter getResponseFormatter() {
                return null;
            }

            @Override
            public String getSessionId() {
                return "9u8y7egudew";
            }

            @Override
            public String formatResponse(Object response) throws CobraException {
                return null;
            }
        };

        requestCommand.name = "test";
        requestCommand.parameters = new HashMap<>();
        requestCommand.parameters.put("clientDeviceId", "test");
        requestCommand.parameters.put("sensitivity", "masked");
        request.requestCommand = requestCommand;

        Response response = Response.fromRequest(request);
        response.setResult(new ResponseResult());

        CommandLog commandLog = testCmd.formatLog(response);

        assertNotNull(commandLog);
        assertNotNull(commandLog.toJson(false));
    }

    @Test
    public void getCommandLogNoSession() throws JsonProcessingException {
        Command testCmd = new Command(request, testConfs, null) {
            @Override
            protected void executeImplementation() {

            }

            @Override
            protected void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal) {

            }

            @Override
            protected void onAudioImplementation(byte[] audio) {

            }

            @Override
            protected boolean onResponseImplementation(ResponseResult response) {
                return false;
            }

            @Override
            protected boolean onAudioResponseImplementation(byte[] audio, boolean isLastBuffer) {
                return false;
            }

            @Override
            public ResponseFormatter getResponseFormatter() {
                return null;
            }

            @Override
            public String getSessionId() {
                return "9u8y7egudew";
            }

            @Override
            public String formatResponse(Object response) throws CobraException {
                return null;
            }
        };
        CommandLog commandLog = testCmd.getCommandLogNoSession();

        assertNotNull(commandLog);
        assertNotNull(commandLog.toJson(false));
    }

    @Test
    public void testFormatLogNoInput()
            throws JsonProcessingException {
        Command testCmd = new Command(request, testConfs, null) {
            @Override
            protected void executeImplementation() {

            }

            @Override
            protected void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal) {

            }

            @Override
            protected void onAudioImplementation(byte[] audio) {

            }

            @Override
            protected boolean onResponseImplementation(ResponseResult response) {
                return false;
            }

            @Override
            protected boolean onAudioResponseImplementation(byte[] audio, boolean isLastBuffer) {
                return false;
            }

            @Override
            public ResponseFormatter getResponseFormatter() {
                return null;
            }

            @Override
            public String getSessionId() {
                return "9u8y7egudew";
            }

            @Override
            public String formatResponse(Object response) throws CobraException {
                return null;
            }
        };

        requestCommand.name = "test";
        requestCommand.parameters = new HashMap<>();
        requestCommand.parameters.put("clientDeviceId", "test");

        Response response = Response.fromRequest(request);
        ResponseResult result = mock(ResponseResult.class);
        when(result.getStatus()).thenReturn(ResponseResult.ResponseStatus.NO_INPUT);
        response.setResult(result);

        CommandLog commandLog = testCmd.formatLog(response);

        assertNotNull(commandLog);
        assertNotNull(commandLog.toJson(false));
    }

    @Test
    public void testCommandStatistics() {
        Command testCmd = new SpeechSynthesisCommand(request, testConfs, null);

        Command.CommandStatistics stats = testCmd.getStatistics();
        stats.setVoiceQuality("testQuality");
        assertEquals("testQuality", stats.getVoiceQuality());
        stats.setVoiceModel("testModel");
        assertEquals("testModel", stats.getVoiceModel());
        stats.setSpeechSynthesisTuningBundle("testGenre");
        assertEquals("testGenre", stats.getSpeechSynthesisTuningBundle());
        stats.addEngineResponse("testgRPC", "testgRPC");
        assertEquals("testgRPC", (String) stats.engineResponses.get("testgRPC"));
    }

    @Test
    public void testModelVersionStatistics() {
        request.requestCommand.name = CommandFactory.getCommandName(SpeechRecognitionCommand.class);
        Command speechRecoCommand = new SpeechRecognitionCommand(request, testConfs, null);
        Command.CommandStatistics stats = speechRecoCommand.getStatistics();
        stats.setModelVersion("notDefined");
        assertEquals("notDefined", stats.getModelVersion());
    }
//
//    @Test
//    public void testCommandLogNoSession()
//    {
//        StartSessionCommand startSessionCommand = new StartSessionCommand(request);
//        requestCommand.parameters = new HashMap<WebApiConfig, Object>();
//        requestCommand.name = RequestCommand.RequestCommandName.START_SESSION;
//        startSessionCommand.request.requestCommand.parameters.put(WebApiConfig.Property.SENSITIVITY, "encrypted");
//
//        startSessionCommand.currentSession = mockSession;
//
//        CommandLog commandLog = startSessionCommand.getCommandLogNoSession();
//
//        assertTrue(commandLog.API.equals("startSession"));
//        assertTrue(commandLog.statistics==startSessionCommand.getStatistics());
//        assertFalse(commandLog.request.isEmpty());
//        //Ensure verification code was removed (should always be removed)
//        assertFalse(commandLog.request.containsKey(WebApiConfig.Property.VERIFICATION_CODE));
//    }

    @Test
    public void getEDFLogForwarderAlternate() {
        request.requestCommand.name = CommandFactory.getCommandName(SpeechRecognitionCommand.class);
        Command speechRecoCommand = new SpeechRecognitionCommand(request, testConfs, null);

        testConf.setProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, false);
        testConf.setProperty(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, true);
        assertNotNull(speechRecoCommand.getEDFLogForwarder());
    }

    @Test
    public void testChainStatistics()
    {
        Integer currentIndex = 3;
        Integer commandCount = 5;
        String chainId = "myChain";
        Map<String, Object> metadata = Map.of("key1", "value1", "key2", "value2");
        CommandChain chainer = mock(CommandChain.class);
        when(chainer.getChainId()).thenReturn(chainId);
        when(chainer.getCurrentCommandIndex()).thenReturn(currentIndex);
        when(chainer.getCommandCount()).thenReturn(commandCount);
        request.requestCommand.name = CommandFactory.getCommandName(SpeechRecognitionCommand.class);
        request.metadata = metadata;
        Command speechRecoCommand = new SpeechRecognitionCommand(request, testConfs, chainer);

        assertNotNull(speechRecoCommand.statistics.chainInfo);
        assertEquals(currentIndex, speechRecoCommand.statistics.chainInfo.index);
        assertEquals(false, speechRecoCommand.statistics.chainInfo.isFinalCommand);
        assertNotNull(speechRecoCommand.statistics.chainInfo.id);
        assertEquals(metadata, speechRecoCommand.statistics.metadata);
    }

    @Test
    public void testSetAudioStreamingToEngineStatistics()
    {
        SpeechRecognitionCommand command = new SpeechRecognitionCommand(request, testConfs, null);
        Command.CommandStatistics stats = command.getStatistics();

        Calendar first = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar last = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        stats.setAudioStreamingToEngineStatistics(1, 2, first, last);

        assertEquals(1, stats.engineBufferedAudioChunks.intValue());
        assertEquals(2, stats.engineSentAudioChunks.intValue());
        assertEquals(first, stats.engineFirstAudioChunk);
        assertEquals(last, stats.engineLastAudioChunk);
    }

    @Test
    public void formatResponse()
            throws CobraException
    {
        requestCommand.parameters = new HashMap<>();
        requestCommand.id = "abc123";
        requestCommand.name = "endSession";
        EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        ResponseResult responseResult = new ResponseResult("endSession");
        command.formatResponse(responseResult);
        responseResult = new ResponseResult();
        testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
        command = new EndSessionCommand(request, testConfs, null);
        when(WebApiConfigManager.getInstance()).thenReturn(testConfs);
        command.formatResponse(responseResult);
        responseResult = new EndSessionResponse(ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
        command.formatResponse(responseResult);
    }

    @Test
    public void testEncryptJSON() {
        requestCommand.parameters = new HashMap<>();
        requestCommand.id = "abc123";
        requestCommand.name = "endSession";
        testConf.setProperty(WebApiConfig.Property.SENSITIVITY.key, MASKED.toString());
        EndSessionCommand command = new EndSessionCommand(request, testConfs, null);
        Encryptor encryptor = new Encryptor();
        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.MASK);
        List<Object> arrayValue = List.of("anotherString", 5, 5.5, Map.of("key1", "value1"));
        assertEquals("[xxxxxxxxxxxxx, 5, 5.5, {key1=xxxxxx}]", command.encryptJSONObject(encryptor, arrayValue).toString());
    }

}
