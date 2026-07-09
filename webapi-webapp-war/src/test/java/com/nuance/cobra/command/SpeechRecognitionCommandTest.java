package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.SpeechRecognitionClients;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({SpeechRecognitionCommandTest.class, SessionManager.class, ClientFactory.class, Response.class})
public class SpeechRecognitionCommandTest extends CobraTest
{
    private SpeechRecognitionCommand speechRecognitionCommand;
    private Request request;
    private Session mockSession;


    @Mock
    ClientFactory testClientFactory;

    @InjectMocks
    private ClientFactory clientFactory = mock(ClientFactory.class);

    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration testConf = CobraConfigurationUtils.getHighestConfig(testConfs);

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception
    {
        // Call parent setup()
        super.setUp();

        // Setup System.err to put it in a stream
        System.setOut(new PrintStream(outContent));

        // Build speech synthesis request
        String json = "{'type': 'command', 'sessionId': '12345', 'command': {'name': 'speechSynthesis', 'id': '1', 'parameters': {'data': 'Hello world this is a long sentence', 'type': 'text'}}}";
        json = json.replaceAll("'", "\"");
        request = Request.fromJson(json);

        // Setup mock SessionManager
        mockSession = mock(Session.class);

        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key,"123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key,"DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key,"Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key,"4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key,"as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key,"appName");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key,"1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key,"MTIzNDU2Nzg5");
        testConf.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        testConf.setProperty(WebApiConfig.Property.SENSITIVITY.key,"encrypted");

        when(mockSession.getConfigWithMerge(any())).thenReturn(mockServerConfigs);
        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        when(mockSession.getSessionId()).thenReturn(request.sessionId);

        when(mockSessionManager.getSession(request.sessionId, true)).thenReturn(mockSession);
        when(mockSessionManager.getSession(request.sessionId, false)).thenReturn(mockSession);
        when(mockSession.getStartSessionTime()).thenReturn(Calendar.getInstance());

        mockServerConfig.setProperty(WebApiConfig.Property.COMMAND_DATA.key,"How can I help you?");

        // Create and set speech synthesis command
        speechRecognitionCommand = new SpeechRecognitionCommand(request, testConfs, null);
        speechRecognitionCommand.setResponseHandler(mock(ResponseHandler.class));
    }

    @Test
    public void testFormatResponseEvent() throws CobraException
    {
        mockStatic(Response.class);
        ResponseEvent mockResponseEvent = mock(ResponseEvent.class);
        Response mockResponse = mock(Response.class);
        when(Response.eventFromRequest(any(Request.class), any(ResponseEvent.class))).thenReturn(mockResponse);
        when(mockResponse.toJson()).thenReturn("");
        speechRecognitionCommand.formatResponse(mockResponseEvent);
        verify(mockResponse).toJson();
    }

    @Test
    public void testFormatResponseResult() throws CobraException
    {
        SpeechRecognitionCommand speechRecognitionCommandSpy = spy(speechRecognitionCommand);
        mockStatic(Response.class);
        SpeechRecoResponse mockResponseResult = mock(SpeechRecoResponse.class);
        SpeechRecoResponse.Data mockData = mock(SpeechRecoResponse.Data.class);
        Response mockResponse = mock(Response.class);
        CommandLog mockCommandLog = mock(CommandLog.class);
        EdfLogForwarder mockEdfLogForwarder = mock(EdfLogForwarder.class);
        EdfMessageQueueable mockEdfMessageQueueable = mock(EdfMessageQueueable.class);

        when(Response.fromRequest(any(Request.class))).thenReturn(mockResponse);

        doNothing().when(mockResponse).setResult(any(ResponseResult.class));
        when(mockResponse.toJson()).thenReturn("");
        when(mockResponse.getResult()).thenReturn(mockResponseResult);
        when(mockResponseResult.isFinal()).thenReturn(true);
        when(mockResponseResult.getData()).thenReturn(mockData);

        doReturn(mockCommandLog).when(speechRecognitionCommandSpy).formatLog(any(Response.class));
        doReturn(mockEdfLogForwarder).when(speechRecognitionCommandSpy).getEDFLogForwarder();
        doReturn(mockEdfMessageQueueable).when(speechRecognitionCommandSpy).getEdfMessageQueueable(any(CommandLog.class));
        doNothing().when(mockEdfLogForwarder).forwardLog(any(EdfMessageQueueable.class),any(CombinedConfiguration.class));


        speechRecognitionCommandSpy.formatResponse(mockResponseResult);
        verify(mockResponse).setResult(any(ResponseResult.class));
        verify(speechRecognitionCommandSpy).formatLog(any(Response.class));
        verify(mockResponse).toJson();
    }

    @Test
    public void testFormatLog() throws CobraException, JsonProcessingException
    {
        String json = "{'type':'command','sessionId':'12345','command':{'name':'speechRecognition','id':'1','parameters':{'language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}}}";
        json = json.replaceAll("'", "\"");
        request = Request.fromJson(json);
        Response response = Response.fromRequest(request);
        SpeechRecoResponse speechRecoResponse = new SpeechRecoResponse(ResponseResult.ResponseStatus.SUCCESS, new NoiseLevel(0.0,0.0));
        speechRecoResponse.setBestResult("Good Morning How Are You Doing Today");
        speechRecoResponse.setData(new SpeechRecoResponse.Data(new String[]{"Good","Good Morning", "Good Morning How Are", "Good Morning How Are You Doing Today"},new int[]{1000},1000,new SpeechRecoResponse.Word[10][10],  -1));
        response.setResult(speechRecoResponse);
        CommandLog commandLog = speechRecognitionCommand.formatLog(response);

        assert null != commandLog;
        String commandLogString = commandLog.toJson(false);
        assert null != commandLogString;
        assert commandLogString.contains("fileName");

        testConf.setProperty(WebApiConfig.Property.SENSITIVITY.key,"masked");


        commandLog = speechRecognitionCommand.formatLog(response);

        assert null != commandLog;
        assert !commandLog.toJson(false).contains("fileName");
        assert commandLog.toJson(false).contains("xxx");
    }

    @Test
    public void onMessage_NewNoiseLevelCalculated_Event() {
        SpeechDetector mockNode = mock(SpeechDetector.class);

        speechRecognitionCommand.onEvent(mockNode, AudioNodeObserver.NodeEvent.NEW_NOISE_LEVEL_CALCULATED);
    }

    @Test
    public void onMessage_Timeout_Event() {
        SpeechRecognitionCommand speechRecognitionCommandSpy = spy(speechRecognitionCommand);
        AudioNode mockNode = mock(AudioNode.class);
        doNothing().when(speechRecognitionCommandSpy).onTimeout(any(AudioNode.class));

        speechRecognitionCommandSpy.onEvent(mockNode, AudioNodeObserver.NodeEvent.TIMEOUT);
        verify(speechRecognitionCommandSpy).onTimeout(any(AudioNode.class));
    }

    @Test
    public void onMessage_StartOfSpeech_Event()
    {
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        speechRecognitionCommand.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        verify(mockResponseHandler).sendEvent(any());
    }

    @Test
    public void onMessage_EndOfSpeech_Event() {
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        speechRecognitionCommand.onEvent(null, AudioNodeObserver.NodeEvent.END_OF_SPEECH);
        verify(mockResponseHandler).sendEvent(any());
    }

    @Test
    public void onMessage_AudioStatistics_Event() {
        StatisticsCalculatorNode mockNode = mock(StatisticsCalculatorNode.class);
        Map<String, Object> stats = new HashMap<>();
        stats.put("mediaDuration", 2);
        PowerMockito.when(mockNode.getStatistics(StatisticsCalculatorNode.ALL_STATISTICS)).thenReturn(stats);

        speechRecognitionCommand.onEvent(mockNode, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);

        assertEquals((Integer)2, speechRecognitionCommand.statistics.mediaDuration);
    }

    @Test
    public void onMessage_AudioStatistics_Event_Invalid() {
        AudioNode mockNode = mock(AudioNode.class);
        speechRecognitionCommand.onEvent(mockNode, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);
    }

    @Test
    public void executeImplementationTest() throws Exception {
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);

        whenNew(ClientFactory.class).withNoArguments().thenReturn(clientFactory);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any())).thenReturn(new SpeechRecoClient("") {
            @Override
            public void onEvent(AudioNode eventOwner, NodeEvent event) {

            }

            @Override
            public void onAudio(AudioChunk chunk) {

            }

            @Override
            public void finish() {

            }

            @Override
            public void startSpeechRecognition() {

            }

            @Override
            public void stopSpeechRecognition() {

            }

            @Override
            public void cancelSpeechRecognition(boolean isFinal) {

            }

            @Override
            public void startRecognitionTimers()
            {

            }

            @Override
            public boolean setRecResultType(RecResultType recResultType) {
                return false;
            }

            @Override
            public float getMaxConfidence() {
                return 0;
            }
        });

        try {
            speechRecognitionCommand.executeImplementation();
        } catch (Exception e) {
            fail();
        }

        mockServerConfig.setProperty(WebApiConfig.Property.STATISTICS.key, true);

        try {
            speechRecognitionCommand.executeImplementation();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSensitivityFromRequest() throws CobraException {
        String json = "{'type':'command','sessionId':'12345','command':{'name':'speechRecognition','id':'1','parameters':{'"+WebApiConfig.Property.SENSITIVITY.key+"':'open','language':'en-us','endPointDetection':true,'startOfSpeechHistoryFrames':15,'startOfSpeechVoicedFrames':7,'endOfSpeechHistoryFrames':50,'endOfSpeechVoicedFrames':5,'stopOnEndOfSpeech':true,'wordStream':true}}}";
        json = json.replaceAll("'", "\"");
        request = Request.fromJson(json);
        Request requestSpy = spy(request);


        SpeechRecognitionCommand SpeechRecog = new SpeechRecognitionCommand(request, testConfs, null);

        Logger mockLogger = mock(Logger.class);

        //find the key for the newly created entry in WavDumperNode
        final SessionManager sessionManager = SessionManager.getInstance();
        final Session session = sessionManager.getSession(request.sessionId, true);
        String createdKey = String.format(Locale.US, "%s_%s_%s_raw", SpeechRecog.getSessionId(), SpeechRecog.getCommandId(), session.getStepId());

        //mock clientFactory
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                .thenReturn(mockSessionManager);

        SpeechRecoClient mockSpeechRecoClient = mock(SpeechRecoClient.class);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any()))
                .thenReturn(mockSpeechRecoClient);

        SpeechRecog.setClientFactory(clientFactory);
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        SpeechRecog.setResponseHandler(mockResponseHandler);

        SpeechRecog.executeImplementation();

        assertEquals("open", WavDumperNode.getEncryptedFiles().get(createdKey));


        when(request.requestCommand.parameters.containsKey((WebApiConfig.Property.SENSITIVITY.key))).thenThrow(NullPointerException.class);
        SpeechRecog.executeImplementation();
    }

    @Test
    public void onTimeout_StartSpeechTimer_Test() {
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);
        AudioNode timeoutNode = new TimeoutNode(0, 0,
                SpeechRecoClient.START_SPEECH_TIMER_ID, new ArrayList<>(), true);
        speechRecognitionCommand.onTimeout(timeoutNode);
        verify(mockResponseHandler).onResponse(any());
    }

    @Test
    public void onTimeout_MaxSpeechTimer_Test() {
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);
        AudioNode timeoutNode = new TimeoutNode(0, 0,
                SpeechRecoClient.MAX_SPEECH_TIMER_ID, new ArrayList<>(), true);
        speechRecognitionCommand.onTimeout(timeoutNode);
        verify(mockResponseHandler).sendEvent(any());
    }

    @Test
    public void onTimeout_UnknownTimer_Test() {
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);
        AudioNode timeoutNode = new TimeoutNode(0, 0,
                "Nothing", new ArrayList<>(), true);
        startCapturingLogOutput(Level.INFO, SpeechRecognitionCommand.class.getName());
        speechRecognitionCommand.onTimeout(timeoutNode);
        assertTrue(getCapturedLogOutput().contains("Command received timeout from unknown source: Nothing"));
    }

    @Test
    public void testgetResources(){
        testConf.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key, "someRandomEngine");
        assertEquals("someRandomEngine",
                SpeechRecognitionCommand.getResources(testConfs).getResourcesMap().get("speechRecognitionEngine"));

        assertEquals("speechRecognition", SpeechRecognitionCommand.getResources(testConfs).getName());

    }

    @Test
    public void testGetResourcesLanguageMapping() {
        CombinedConfiguration mockConfig = spy(testConfs);
        HierarchicalConfiguration mockHierarchicalConfiguration = mock(HierarchicalConfiguration.class);
        List<String> keys = new ArrayList<>(Arrays.asList("testKey"));
        when(mockConfig.configurationAt(anyString())).thenReturn(mockHierarchicalConfiguration);
        when(mockHierarchicalConfiguration.getKeys()).thenReturn(keys.iterator());

        List<String> output = SpeechRecognitionCommand.getLanguageList(mockConfig);
        assertEquals("testKey", output.get(0));
    }

    @Test
    public void testOnDirectiveCancel() throws Exception
    {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        SpeechRecoClient mockSpeechRecoClient = mock(SpeechRecoClient.class);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any()))
                    .thenReturn(mockSpeechRecoClient);

        speechRecognitionCommand.setClientFactory(clientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.CANCEL);

        speechRecognitionCommand.execute();
        speechRecognitionCommand.onDirective(requestDirective, true, "tests");

        verify(mockSpeechRecoClient).startSpeechRecognition();
        verify(mockSpeechRecoClient).cancelSpeechRecognition(true);
    }

    @Test
    public void testOnDirective() throws Exception
    {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                    .thenReturn(mockSessionManager);

        SpeechRecoClient mockSpeechRecoClient = mock(SpeechRecoClient.class);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any()))
                    .thenReturn(mockSpeechRecoClient);

        speechRecognitionCommand.setClientFactory(clientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        //Stop
        RequestDirective requestDirectiveStop = new RequestDirective();
        requestDirectiveStop.setName(RequestDirective.RequestDirectiveName.STOP);

        speechRecognitionCommand.execute();
        speechRecognitionCommand.onDirective(requestDirectiveStop, true, "tests");

        verify(mockSpeechRecoClient).startSpeechRecognition();
        verify(mockSpeechRecoClient).stopSpeechRecognition();

        //startRecognitionTimers
        RequestDirective requestDirectiveStartRecognitionTimers = new RequestDirective();
        requestDirectiveStartRecognitionTimers.setName(RequestDirective.RequestDirectiveName.START_RECOGNITION_TIMERS);

        speechRecognitionCommand.execute();
        speechRecognitionCommand.onDirective(requestDirectiveStartRecognitionTimers, true, "tests");
    }

    @Test
    public void onResponseImplementation() throws Exception
    {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                .thenReturn(mockSessionManager);

        SpeechRecoClient mockSpeechRecoClient = mock(SpeechRecoClient.class);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any()))
                .thenReturn(mockSpeechRecoClient);

        speechRecognitionCommand.setClientFactory(clientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        //Cancel
        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.STOP);

        // Call methods
        speechRecognitionCommand.execute();

        ResponseResult responseResult = mock(ResponseResult.class);
        when(responseResult.isFinal()).thenReturn(false);
        speechRecognitionCommand.onResponseImplementation(responseResult);

        when(responseResult.isFinal()).thenReturn(true);
        speechRecognitionCommand.onResponseImplementation(responseResult);

        AudioNode audioNode = mock(AudioNode.class);
        speechRecognitionCommand.onEvent(audioNode, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
        speechRecognitionCommand.onEvent(audioNode, AudioNodeObserver.NodeEvent.END_OF_SPEECH);


        when(responseResult.isFinal()).thenReturn(false);
        speechRecognitionCommand.onResponseImplementation(responseResult);

        when(responseResult.isFinal()).thenReturn(true);
        speechRecognitionCommand.onResponseImplementation(responseResult);
    }


    @Test
    public void onAudioImplementation() throws Exception {
        // Setup
        mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance())
                .thenReturn(mockSessionManager);

        SpeechRecoClient mockSpeechRecoClient = mock(SpeechRecoClient.class);
        PowerMockito.when(clientFactory.createSpeechRecoClient(any(), any(), any(), any()))
                .thenReturn(mockSpeechRecoClient);

        speechRecognitionCommand.setClientFactory(clientFactory);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        speechRecognitionCommand.setResponseHandler(mockResponseHandler);

        //Cancel
        RequestDirective requestDirective = new RequestDirective();
        requestDirective.setName(RequestDirective.RequestDirectiveName.STOP);

        //Test Pre-execute audio
        speechRecognitionCommand.onAudioImplementation(new byte[] { 22, 12, 44, 56, 78 });

        // Call methods
        speechRecognitionCommand.execute();

        speechRecognitionCommand.onAudioImplementation(new byte[] { 22, 12, 44, 56, 78 });
        speechRecognitionCommand.onAudioImplementation(new byte[] { 22, 12, 44, 56, 78 });

        speechRecognitionCommand.onDirective(requestDirective, true, "tests");

        speechRecognitionCommand.onAudioImplementation(new byte[] { 22, 12, 44, 56, 78 });

        verify(mockSpeechRecoClient).startSpeechRecognition();
        verify(mockSpeechRecoClient).stopSpeechRecognition();
    }

}
