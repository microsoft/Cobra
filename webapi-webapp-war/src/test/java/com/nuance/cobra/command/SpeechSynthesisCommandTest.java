package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({
        Request.class, CobraConfigurationUtils.class, Session.class
})
public class SpeechSynthesisCommandTest extends CobraTest
{
    private SpeechSynthesisCommand speechSynthesisCommand;
    private Request request;
    private Session mockSession;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp()
            throws Exception
    {
        // Call parent setup()
        super.setUp();

        // Setup System.err to put it in a stream
        System.setOut(new PrintStream(outContent));

        // Build speech synthesis request
        String json =
                "{'type': 'command', 'sessionId': '12345', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'data': 'Hello world this is a long sentence', 'type': 'text'}}}";
        json = json.replaceAll("'", "\"");
        request = Request.fromJson(json);

        // Setup mock SessionManager
        mockSession = mock(Session.class);
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key, "DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key, "Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key, "4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key, "as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key, "appName");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, "1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "MTIzNDU2Nzg5");
        testConf.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nvc.getValue());

        when(mockSession.getConfigWithMerge(any())).thenReturn(mockServerConfigs);
        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSessionManager.getSession(request.sessionId, false)).thenReturn(mockSession);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());


        mockServerConfig.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "How can I help you?");
        mockServerConfig.setProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "Eva");
        mockServerConfig.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nim.getValue());

        // Create and set speech synthesis command
        speechSynthesisCommand = new SpeechSynthesisCommand(request, testConfs, null);
        speechSynthesisCommand.setResponseHandler(mock(ResponseHandler.class));

    }

    @After
    public void tearDown()
            throws Exception
    {
        super.tearDown();
        System.setOut(null);
    }

    @Test
    public void testLogRequest()
    {
        // Build request
        String json =
                "{'type': 'command', 'sessionId': '12345', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'data': 'Hello world this is a long sentence', 'type': 'text'}}}";
        json = json.replaceAll("'", "\"");
        String expectedLog =
                "[main] DEBUG com.nuance.cobra.command.SpeechSynthesisCommand - logRequest - got request: {\"type\":\"command\",\"command\":{\"name\":\"speechSynthesis\",\"id\":\"1\",\"parameters\":{\"data\":\"-log level should be TRACE to see data-\",\"type\":\"text\"}},\"sessionId\":\"12345\"}";

        try
        {
            // Call methods
            request = Request.fromJson(json);
            speechSynthesisCommand.logRequest(request, speechSynthesisCommand.logger);

            // Check result
            assertTrue(outContent.toString()
                                 .contains(expectedLog));
        }
        catch (Exception e)
        {
            System.err.println("---testLogRequest() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testLogRequestNull()
            throws Exception
    {
        // Setup
        String expectedLog =
                "ERROR com.nuance.cobra.command.SpeechSynthesisCommand - logRequest - can't log received request since can't get json from request - error: null";

        // Call method
        speechSynthesisCommand.logRequest(null, speechSynthesisCommand.logger);

        // Check result
        assertTrue(outContent.toString()
                             .contains(expectedLog));
    }

    @Test
    public void testLogRequestError()
            throws Exception
    {
        mockStatic(Request.class);

        PowerMockito.when(Request.toJson(any()))
                    .thenThrow(new CobraException(
                            ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_SERIALIZE_ERROR,
                                    "Exception thrown from mock class")));

        startCapturingLogOutput(Level.INFO, SpeechSynthesisCommand.class.getName());
        this.speechSynthesisCommand.logRequest(request, speechSynthesisCommand.logger);
        Assert.assertTrue(getCapturedLogOutput().contains("can't log received request"));
    }

    @Test
    public void testLogResponse()
    {
        // Build request
        ResponseResult responseResult = new ResponseResult();
        String expectedLog =
                "[main] DEBUG com.nuance.cobra.command.SpeechSynthesisCommand - send result: {\"final\":true,\"status\":\"success\"}";

        try
        {
            // Call method
            speechSynthesisCommand.logResponse(responseResult);

            // Check result
            assertTrue(outContent.toString()
                                 .contains(expectedLog));
        }
        catch (Exception e)
        {
            System.err.println("---testLogResponse() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testLogResponseError()
    {
        // Build request
        ResponseResult responseResult = new ResponseResult();
        String expectedLog =
                "[main] WARN  com.nuance.cobra.command.SpeechSynthesisCommand - can't log response to send since can't get json from response - error: null";

        try
        {
            // Call method
            speechSynthesisCommand.logResponse(null);

            // Check result
            assertTrue(outContent.toString()
                                 .contains(expectedLog));
        }
        catch (Exception e)
        {
            System.err.println("---testLogResponseError() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testExecute()
    {
        // Call method
        speechSynthesisCommand.execute();

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the startSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testExecuteSSMLError()
            throws CobraException
    {

        mockServerConfig.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "ssml");

        // Call method
        Command baseCommand = speechSynthesisCommand;
        speechSynthesisCommand.execute();
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.TTS_TYPE_SSML_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(baseCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testSSMLErrorNoSession()
            throws Exception
    {
        testConf.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "ssml");
        String cfgPath = WebApiConfigManager.getConfigPath();
        mockStatic(WebApiConfigManager.class);
        mockStatic(CobraConfigurationUtils.class);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(any()))
                    .thenReturn(testConfs);
        PowerMockito.when(WebApiConfigManager.getInstance())
                    .thenReturn(testConfs);
        PowerMockito.when(WebApiConfigManager.getConfigPath())
                    .thenReturn(cfgPath);
        PowerMockito.when(CobraConfigurationUtils.lookupTenantConfigAndAddParams(any(), any())).thenReturn(testConfs);
        speechSynthesisCommand.currentSessionID = null;
        speechSynthesisCommand.execute();
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.TTS_TYPE_SSML_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testAllowSsml()
    {
        mockServerConfig.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "ssml");
        mockServerConfig.setProperty(WebApiConfig.Property.ALLOW_SSML.key, true);
        // Call method
        speechSynthesisCommand.execute();

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the startSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testExecuteWithDebugLogs()
            throws Exception
    {
        startCapturingLogOutput(Level.DEBUG, SpeechSynthesisCommand.class.getName());

        // Call method
        speechSynthesisCommand.execute();

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the startSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);

        String capturedOutput = getCapturedLogOutput();
        Assert.assertTrue(capturedOutput.contains("log level should be TRACE to see data"));
        Assert.assertFalse(capturedOutput.contains("Hello world this is a long sentence"));
    }

    @Test
    public void testExecuteWithTraceLogs()
            throws Exception
    {
        startCapturingLogOutput(Level.TRACE, SpeechSynthesisCommand.class.getName());

        // Call method
        speechSynthesisCommand.execute();

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the startSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);

        String capturedOutput = getCapturedLogOutput();
        Assert.assertFalse(capturedOutput.contains("log level should be TRACE to see data"));
        Assert.assertTrue(capturedOutput.contains("Hello world this is a long sentence"));
    }

    @Test
    public void testExecuteNoSessionFound()
    {
        // Setup
        when(mockSessionManager.getSession(request.sessionId, false)).thenReturn(null);

        // Call method
        speechSynthesisCommand.execute();

        // Check result
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testExecuteIllegalParameterError()
    {
        try
        {
            // Setup
            when(mockSession.getConfigWithMerge(any())).thenThrow(new IllegalArgumentException("type"));

            // Call method
            speechSynthesisCommand.execute();

            // Check result
            ResponseCode r =
                    ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR, "type");
            ResponseResult response = new SpeechSynthesisResponse(r);
            verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
        }
        catch (Exception e)
        {
            System.err.println("---testExecuteIllegalParameterError() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testOnDirectiveStop()
    {
        // Setup
        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.STOP);

        // Call methods
        speechSynthesisCommand.execute();
        speechSynthesisCommand.onDirective(requestDirective, true, "tests");

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the stopSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testOnAudioResponseImplementation()
            throws Exception
    {
        speechSynthesisCommand.execute();

        Assert.assertTrue(speechSynthesisCommand.onAudioResponseImplementation(new byte[] {}, true));
    }

    @Test
    public void testOnDirectiveCancel()
    {
        // Setup
        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.CANCEL);

        // Call methods
        speechSynthesisCommand.execute();
        speechSynthesisCommand.onDirective(requestDirective, true, "tests");

        // Check result. Note the NMAS command wrapper is not set in VocalizerNimClient with a mock wrapper.
        // It means the stopSpeechSynthesis() won't work and will return this error below
        ResponseCode r = ResponseCode.createResponseError(ExternalResponseErrorCode.CANNOT_EXECUTE_DIRECTIVE_ERROR);
        ResponseResult response = new SpeechSynthesisResponse(r);
        verify(speechSynthesisCommand.getResponseHandler()).onResponse(response);
    }

    @Test
    public void testSessionActiveCommand()
    {
        // Call method
        speechSynthesisCommand.resetSessionActiveCommand();

        // Check result
        verify(mockSession).setCommand(null);
    }

    @Test
    public void testGetResponseFormatter()
    {
        // Call method and check result
        assertEquals(BaseResponseFormatter.class, speechSynthesisCommand.getResponseFormatter()
                                                                        .getClass());
    }

    @Test
    public void testOnAudio()
    {
        try
        {
            // Call methods and make sure no exception is thrown
            speechSynthesisCommand.onAudio(null);
            speechSynthesisCommand.onAudio(new byte[0]);
            speechSynthesisCommand.onAudio(new byte[1]);
        }
        catch (Exception e)
        {
            System.err.println("---testOnAudio() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testFormatResponse()
    {
        // Setup
        String json =
                "{'type':'result','sessionId':'12345','command':{'name':'speechSynthesis','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        try
        {
            // Call method
            ResponseResult r = new ResponseResult();

            // Check result
            assertEquals(speechSynthesisCommand.formatResponse(r), json);
        }
        catch (Exception e)
        {
            System.err.println("---testFormatResponse() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testOnResponse()
    {
        // Setup
        ResponseResult r = new ResponseResult();
        String expectedLog =
                "[main] DEBUG com.nuance.cobra.command.SpeechSynthesisCommand - send result: {\"final\":true,\"status\":\"success\"}";

        try
        {
            // Call methods
            speechSynthesisCommand.execute();
            outContent.reset(); // flush any previous log
            speechSynthesisCommand.commandConfigs.setProperty(WebApiConfig.Property.TTS_AUDIO_CAPTURE.key, true);
            speechSynthesisCommand.audioBytes = new byte[10];
            speechSynthesisCommand.onResponse(r);

            // Check result
            assertTrue(outContent.toString()
                                 .contains(expectedLog));
        }
        catch (Exception e)
        {
            System.err.println("---testFormatResponse() - error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testFormatLog()
            throws JsonProcessingException
    {
        Response response = Response.fromRequest(request);
        response.setResult(new ResponseResult());
        String json =
                "{'type':'result','sessionId':'12345','command':{'name':'speechSynthesis','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        startCapturingLogOutput(Level.INFO, InterpretationCommand.class.getName());
        testConf.setProperty(WebApiConfig.Property.SENSITIVITY.key, "masked");

        CommandLog commandLog = speechSynthesisCommand.formatLog(response);

        assert (null != commandLog);
        assert (null != commandLog.toJson(false));
    }

    @Test
    public void testFormatLogWithSensitivityMasked()
            throws JsonProcessingException
    {
        Response response = Response.fromRequest(request);
        response.setResult(new ResponseResult());
        String json =
                "{'type':'result','sessionId':'12345','command':{'name':'speechSynthesis','id':'1'},'result':{'final':true,'status':'success'}}";
        json = json.replaceAll("'", "\"");

        testConf.setProperty(WebApiConfig.Property.SENSITIVITY.key, "masked");
        CommandLog commandLog = speechSynthesisCommand.formatLog(response);

        assertNotNull(commandLog);
        String commandLogStr = commandLog.toJson(false);
        assertNotNull(commandLogStr);
        assertTrue(commandLogStr.contains("xxx"));
    }

    @Test
    public void testVoiceObjects()
    {
        SpeechSynthesisCommand.VoiceDescription voiceDescription =
                new SpeechSynthesisCommand.VoiceDescription("pcm_16_22k", "someVoiceModel");
        assertEquals("pcm_16_22k", voiceDescription.getCodec());
        assertEquals("someVoiceModel", voiceDescription.getVoiceModel());

        SpeechSynthesisCommand.VoiceObject voiceObject = new SpeechSynthesisCommand.VoiceObject("josh");
        assertEquals("josh", voiceObject.getName());

        voiceObject.addVoiceDescription(voiceDescription);

        List<?> voices = (List<?>) voiceObject.getVoices();

        assertEquals("{" +
                     "\"codec\":\"pcm_16_22k\"," +
                     "\"voiceModel\":\"someVoiceModel\"" +
                     "}".trim(), new JSONObject(voices.get(0)).toString());

        ArrayList<SpeechSynthesisCommand.VoiceDescription> list = new ArrayList<>();
        list.add(new SpeechSynthesisCommand.VoiceDescription("someOtherCodec", "someOtherVoiceModel"));
        voiceObject = new SpeechSynthesisCommand.VoiceObject("joe", list);

        assertEquals("someOtherCodec",
                ((SpeechSynthesisCommand.VoiceDescription) voiceObject.getVoices()
                                                                      .get(0)).getCodec());
        assertEquals("someOtherVoiceModel",
                ((SpeechSynthesisCommand.VoiceDescription) voiceObject.getVoices()
                                                                      .get(0)).getVoiceModel());
    }

    @Test
    public void getResourcesTest()
    {
        testConf.addProperty("vocsMapping.Evan.language", "en-us");
        testConf.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nvc.getValue());


        assertEquals(VocalizerClients.nvc.getValue(), SpeechSynthesisCommand.getResources(testConfs)
                                                   .getResourcesMap()
                                                   .get("vocalizer"));

        assertEquals("speechSynthesis", SpeechSynthesisCommand.getResources(testConfs)
                                                              .getName());


    }

    @Test
    public void getResourcesTestPrime()
    {
        testConf.addProperty("vocsMapping.Evan.voice_age", "25");
        testConf.addProperty("vocsMapping.Evan.voice_gender", "male");
        testConf.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nvc.getValue());


        assertEquals(VocalizerClients.nvc.getValue(), SpeechSynthesisCommand.getResources(testConfs)
                                                                             .getResourcesMap()
                                                                             .get("vocalizer"));

        assertEquals("speechSynthesis", SpeechSynthesisCommand.getResources(testConfs)
                                                              .getName());


    }

    @Test(expected = Test.None.class)
    public void addVoiceQualityAndModelToStatisticsHappyTest()
            throws NoSuchFieldException, IllegalAccessException {
        Command.CommandStatistics mockCommandStatistics = mock(Command.CommandStatistics.class);
        TestUtils.inject(speechSynthesisCommand, SpeechSynthesisCommand.class, "statistics", mockCommandStatistics);

        //Happy Path
        testConf.addProperty("vocsMapping.Evan.language", "en-us");
        testConf.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConf.addProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "Evan");
        testConf.addProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key, "pcm_16_16k");
        speechSynthesisCommand.addVoiceQualityAndModelToStatistics(mockSession, testConfs);
        verify(mockCommandStatistics).setVoiceQuality("premium");
        verify(mockCommandStatistics).setVoiceModel("enhanced");
    }

    @Test(expected = Test.None.class)
    public void addVoiceQualityAndModelToStatisticsTest()
            throws NoSuchFieldException, IllegalAccessException
    {
        Command.CommandStatistics mockCommandStatistics = mock(Command.CommandStatistics.class);
        TestUtils.inject(speechSynthesisCommand, SpeechSynthesisCommand.class, "statistics", mockCommandStatistics);

        //Neural voice
        testConf.addProperty("vocsMapping.Evan.language", "en-us");
        testConf.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        testConf.addProperty("vocsMapping.Evan.nvcVersion", "nvc3");
        testConf.addProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key, "Evan");
        testConf.addProperty(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key, "pcm_16_16k");
        speechSynthesisCommand.addVoiceQualityAndModelToStatistics(mockSession, testConfs);
        verify(mockCommandStatistics).setVoiceQuality("neural");
        verify(mockCommandStatistics).setVoiceModel("enhanced");

        //Null Session
        speechSynthesisCommand.addVoiceQualityAndModelToStatistics(null, null);
        verifyNoMoreInteractions(mockCommandStatistics);

        //Vocalizer Client is nim
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nim.getValue());
        speechSynthesisCommand.addVoiceQualityAndModelToStatistics(mockSession, testConfs);
        verifyNoMoreInteractions(mockCommandStatistics);

        //Bad Vocalizer Client name
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, "blah");
        speechSynthesisCommand.addVoiceQualityAndModelToStatistics(mockSession, testConfs);
        verifyNoMoreInteractions(mockCommandStatistics);
    }

    @Test
    public void checkTTSTypeSSML()
    {
        boolean rc;
        rc = speechSynthesisCommand.checkTTSTypeSSML(null);
        assertFalse(rc);

        testConfs.getConfiguration("defaults").clearProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key);
        testConf.clearProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key);
        rc = speechSynthesisCommand.checkTTSTypeSSML(testConfs);
        assertTrue(rc);

        testConf.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "text");
        rc = speechSynthesisCommand.checkTTSTypeSSML(testConfs);
        assertTrue(rc);

        testConf.setProperty(WebApiConfig.Property.COMMAND_DATA_TYPE.key, "ssml");
        testConf.setProperty(WebApiConfig.Property.ALLOW_SSML.key, false);
        rc = speechSynthesisCommand.checkTTSTypeSSML(testConfs);
        assertFalse(rc);

        testConf.setProperty(WebApiConfig.Property.ALLOW_SSML.key, true);
        rc = speechSynthesisCommand.checkTTSTypeSSML(testConfs);
        assertTrue(rc);
    }

    @Test
    public void testOnAudio_captureAudio() {
        byte[] audio = {1, 3, 5, 7, 9};

        assertNull(speechSynthesisCommand.audioBytes);
        speechSynthesisCommand.onAudioImplementation(audio);
        assertNull(speechSynthesisCommand.audioBytes);

        testConfs.setProperty(WebApiConfig.Property.TTS_AUDIO_CAPTURE.key, true);
        speechSynthesisCommand = new SpeechSynthesisCommand(request, testConfs, null);

        speechSynthesisCommand.onAudioResponseImplementation(audio, false);
        assertEquals(audio.length, speechSynthesisCommand.audioBytes.length);

        speechSynthesisCommand.onAudioResponseImplementation(audio, true);
        assertEquals(audio.length*2, speechSynthesisCommand.audioBytes.length);
    }

    @Test
    public void testGetVoice() {
        CombinedConfiguration conf = new CombinedConfiguration();
        String voice = speechSynthesisCommand.getVoice(conf);

        assertNotNull(voice);
        assertEquals(MetricsConstants.UNDEFINED_LABEL_VALUE, voice);

        conf.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".fakeConfig", "Eva");
        voice = speechSynthesisCommand.getVoice(conf);

        assertNotNull(voice);
        assertEquals(MetricsConstants.UNDEFINED_LABEL_VALUE, voice);

        conf.addProperty(WebApiConfig.Property.VOCS_MAPPING.key + ".name", "Eva");
        voice = speechSynthesisCommand.getVoice(conf);

        assertNotNull(voice);
        assertEquals("Eva", voice);
    }
}

