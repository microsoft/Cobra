package com.nuance.cobra.command;

import com.nuance.cobra.api.endpoint.WebApiEndpoint;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestCommand;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.command.CommandChain.CommandChainLogType;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.cobra.utils.response.EDF.NeapLog;
import com.nuance.cobra.utils.response.EDF.RequestLog;
import com.nuance.cobra.utils.webapi.ResponseSender;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.EDF_V1_LOG_FORMAT_NAME;
import static com.nuance.cobra.logging.edf.EdfMessageQueueableConstants.NEAP_V1_LOG_FORMAT_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandChainTest extends CobraTest {
    ResponseSender responseSender = mock(ResponseSender.class);
    Session mockSession = mock(Session.class);
    private CommandChain testChain;
    IResponseSender mockSender = mock(IResponseSender.class);
    protected List<String> sentResponses = new ArrayList<>();
    Request commandChainRequest;

    @Before
    public void setUp() throws CobraException
    {
        String request_json = "{\"type\": \"commandChain\",\"chainId\": \"myChain\",\"metadata\": {\"key1\":\"value1\",\"key2\":\"value2\"}," +
                "  \"commands\": [{\"name\": \"startSession\",\"id\": \"XXX\",\"parameters\": {\"applicationKey\": \"companyName_appName\",\"verificationCode\": \"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\",\"speechSynthesisCodec\": \"pcm_16_16k\",\"speechRecognitionCodec\": \"pcm_16_16k\"}},{\"name\": \"speechRecognition\",\"id\": \"XXX\",\"parameters\": {\"language\": \"_(result.bestResult)_\",\"endPointDetection\": true,\"startOfSpeechHistoryFrames\": 15,\"startOfSpeechVoicedFrames\": 7,\"endOfSpeechHistoryFrames\": 50,\"endOfSpeechVoicedFrames\": 5,\"stopOnEndOfSpeech\": true,\"wordStream\": true}},{\"name\": \"speechSynthesis\",\"id\": \"XXX\",\"parameters\": {\"type\": \"text\",\"data\": \"_(1:result.bestResult)_\",\"voice\": \"Carol\"}}]}";
        commandChainRequest = Request.fromJson(request_json);

        sentResponses.add("{\"type\": \"result\",\"sessionId\": \"zzz\",\"command\": {\"name\": \"startSession\",\"id\": \"XXX\"},\"result\": {\"final\": true,\"status\": \"success\"}}");
        sentResponses.add("{\"type\": \"result\",\"sessionId\": \"a9610dd8-3d99-4b9d-a48c-145ec92004b6\",\"command\": {\"name\": \"speechRecognition\",\"id\": \"XXX\"},\"result\": {\"final\": true,\"status\": \"success\",\"data\": {\"transcriptions\": [\"This is a test\"],\"confidence\": 1000,\"averageWeightedConfidences\": [1000],\"words\": [[{\"confidence\": 1,\"word\": \"This\\\\*no-space-before\"},{\"confidence\": 1,\"word\": \"is\"},{\"confidence\": 1,\"word\": \"a\"},{\"confidence\": 1,\"word\": \"test\"}]]},\"bestResult\": \"This is a test\",\"noiseLevel\": {\"mean\": 0.48,\"standardDeviation\": 0.51}}}");
        sentResponses.add("{\"type\": \"result\",\"sessionId\": \"a9610dd8-3d99-4b9d-a48c-145ec92004b6\",\"command\": {\"name\": \"speechSynthesis\",\"id\": \"XXX\"},\"result\": {\"final\": true,\"status\": \"success\"}}");
    }


    @Test
    public void testSendLogsNone() throws Exception
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key, CommandChain.CommandChainLogType.PER_REQUEST.name());
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);

        EdfLogForwarder mockForwarder = spy(EdfLogForwarder.class);
        PowerMockito.whenNew(EdfLogForwarder.class).withAnyArguments().thenReturn(mockForwarder);

        commandChain.sendLogs();
        verify(mockForwarder, times(0)).forwardLog(any(), any());
    }

    @Test
    public void testSendLogsTwice() throws Exception
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key, CommandChainLogType.SUMMARY_ONLY.name());
        combinedConfiguration.addProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, false);
        combinedConfiguration.addProperty(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, true);
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);

        commandChain = spy(commandChain);
        EdfLogForwarder mockForwarder = spy(EdfLogForwarder.class);
        when(commandChain.getEdfLogForwarder()).thenReturn(mockForwarder);

        commandChain.sendLogs();
        commandChain.sendLogs();
        verify(mockForwarder, times(1)).addListener(any());
        verify(mockForwarder, times(1)).forwardLog(any(), any());
    }

    @Test
    public void testSendLogsInterceptNoListener() throws Exception
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key, CommandChainLogType.SUMMARY_ONLY.name());
        combinedConfiguration.addProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, false);
        combinedConfiguration.addProperty(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, false);
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);

        commandChain = spy(commandChain);
        EdfLogForwarder mockForwarder = spy(EdfLogForwarder.class);
        when(commandChain.getEdfLogForwarder()).thenReturn(mockForwarder);

        commandChain.sendLogs();
        verify(mockForwarder, times(0)).addListener(any());
        verify(mockForwarder, times(1)).forwardLog(any(), any());
    }

    @Test
    public void testSendLogsInterceptAllListener() throws Exception
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.COMMAND_CHAIN_LOG_TYPE.key, CommandChainLogType.SUMMARY_ONLY.name());
        combinedConfiguration.addProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, true);
        combinedConfiguration.addProperty(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, true);
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);

        commandChain = spy(commandChain);
        EdfLogForwarder mockForwarder = spy(EdfLogForwarder.class);
        when(commandChain.getEdfLogForwarder()).thenReturn(mockForwarder);

        commandChain.sendLogs();
        verify(mockForwarder, times(1)).addListener(any());
        verify(mockForwarder, times(1)).forwardLog(any(), any());
    }

    @Test
    public void testGetEdfMessageQueueableNeapLog() throws Exception
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.FLUENT_LOG_FORMAT.key, NEAP_V1_LOG_FORMAT_NAME);
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);

        CommandLog commandLog = mock(CommandLog.class);
        commandLog.request = new RequestLog();
        commandLog.session = mock(com.nuance.cobra.session.Session.class);
        when(commandLog.toJson(anyBoolean())).thenReturn("{\"request\":{\"key\":5},\"requestHeader\":{\"Channel\":\"wapiChannel\",\"ClientID\":\"wapiClientId\",\"key\":5}}");
        EdfMessageQueueable neapLog = new NeapLog(commandLog);
        EdfMessageQueueable edfMessageQueueable = commandChain.getEdfMessageQueueable(List.of(neapLog));
        assertNotNull(edfMessageQueueable);
    }

    @Test
    public void testGetEdfMessageQueueableEdfLog()
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addProperty(WebApiConfig.Property.FLUENT_LOG_FORMAT.key, EDF_V1_LOG_FORMAT_NAME);
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, combinedConfiguration);
        EdfMessageQueueable edfMessageQueueable = commandChain.getEdfMessageQueueable(null);
        assertNull(edfMessageQueueable);
    }

    @Test
    public void testGetEdfMessageQueueableEmptyList()
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        EdfMessageQueueable edfMessageQueueable = commandChain.getEdfMessageQueueable(null);
        assertNull(edfMessageQueueable);
    }

    @Test
    public void testConstructor() throws CobraException {
        WebApiEndpoint testEndpoint = new WebApiEndpoint(responseSender);
        Request testReq = commandChainRequest;

        CombinedConfiguration testConfig = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();

        testChain = new CommandChain(testEndpoint, mockSession, testReq, mockSender, testConfig);
        assertNotNull(testChain);
        assertEquals(3, testChain.getCommandCount());
        assertEquals(-1, testChain.getCurrentCommandIndex());
        assertEquals("myChain", testChain.getChainId());
        assertEquals(2, testChain.getChainMetadata().size());
    }


    @Test
    public void testOnResponseWasSent() {
        WebApiEndpoint testEndpoint = new WebApiEndpoint(responseSender);
        Request testReq = commandChainRequest;

        ResponseResult testResponse = new ResponseResult(ResponseResult.ResponseStatus.SUCCESS, true);
        Whitebox.setInternalState(testResponse, "nextCommand", "startSession" );

        String formattedResponse = testResponse.toString();

        CombinedConfiguration config = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        config.addProperty(WebApiConfig.Property.SEND_LOGS_TO_QUEUE.key, false);
        config.addProperty(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key, false);
        testChain = new CommandChain(testEndpoint, mockSession, testReq, mockSender,  config);

        try {
            testChain.onResponseWasSent(testResponse, formattedResponse);
            fail();
        }catch (NullPointerException e) {}

        try
        {
            Whitebox.setInternalState(testResponse, "finalResponse", false);
            testChain.onResponseWasSent(testResponse, formattedResponse);

            ResponseResult newResponse = new ResponseResult(ResponseResult.ResponseStatus.ERROR, true);
            testChain.onResponseWasSent(newResponse, formattedResponse);

            testResponse = new ResponseResult(ResponseResult.ResponseStatus.SUCCESS, true);
            commandChainRequest.sessionId = "test";
            Request newReq = commandChainRequest;
            testChain = new CommandChain(testEndpoint, mockSession, newReq, mockSender,
                    config);
            testChain.onResponseWasSent(testResponse, formattedResponse);
        } catch (Exception e)
        {
            fail();
        }
    }



    @Test
    public void testResolveJsonPointers() throws Exception {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        RequestCommand requestCommand = commandChain.resolveJsonPointers(new RequestCommand(commandChainRequest.requestCommands.get(2)));

        String expected = "{\n" +
                "  \"name\": \"speechSynthesis\",\n" +
                "  \"id\": \"XXX\",\n" +
                "  \"parameters\": {\n" +
                "    \"voice\": \"Carol\",\n" +
                "    \"data\": \"This is a test\",\n" +
                "    \"type\": \"text\"\n" +
                "  }\n" +
                "}";
        String actual = requestCommand.toJsonString();

        assertEquals(expected, actual);
    }

    @Test
    public void testResolveJsonPointers_notFound()
    {
        RequestCommand requestCommand = new RequestCommand(commandChainRequest.requestCommands.get(1));
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertThrows(Exception.class, () -> commandChain.resolveJsonPointers(requestCommand));
    }

    @Test
    public void testGetJsonPointerValue()
    {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertEquals("zzz", commandChain.getJsonPointerValueJsonPath(0, "sessionId"));
    }

    @Test
    public void testGetJsonPointerValueJavax()
    {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertEquals("zzz", commandChain.getJsonPointerValueJavax(0, "sessionId"));
    }

    @Test
    public void testGetJsonPointerValueJsonPath()
    {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertEquals("zzz", commandChain.getJsonPointerValueJsonPath(0, "sessionId"));
    }

    @Test
    public void testGetJsonPointerValue_notFound()
    {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertThrows(Exception.class, () -> commandChain.getJsonPointerValueJsonPath(0, "notFound"));
    }

    @Test
    public void testGetJsonPointerValue_outOfBounds()
    {
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);
        commandChain.sentResponses = sentResponses;
        assertThrows(IndexOutOfBoundsException.class, () -> commandChain.getJsonPointerValueJsonPath(4, "outOfBounds"));
    }

    @Test
    public void testForwardLog()
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);

        assertEquals(0, commandChain.commandLogs.size());
        commandChain.forwardLog(mock(EdfMessageQueueable.class), null);
        assertEquals(1, commandChain.commandLogs.size());
    }

    @Test
    public void testLogForwarderDestroy()
    {
        Request commandChainRequest = mock(Request.class);
        commandChainRequest.chainId = "";
        commandChainRequest.requestCommands = new ArrayList<>();
        CommandChain commandChain = new CommandChain(null, null, commandChainRequest, null, null);

        commandChain.destroy();
        assertNotNull(commandChain);
    }
}
