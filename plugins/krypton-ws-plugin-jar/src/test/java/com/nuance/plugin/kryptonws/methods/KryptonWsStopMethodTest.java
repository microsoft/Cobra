package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ICommand;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class KryptonWsStopMethodTest
{

    KryptonWsResponseSender mockKryptonWsResponseSender = mock(KryptonWsResponseSender.class);
    KryptonWsWebapiResponseListener mockKryptonWsWebapiResponseListener = mock(KryptonWsWebapiResponseListener.class);
    KryptonWsSession kryptonWsSession = mock(KryptonWsSession.class);
    protected KryptonWsStopMethod kryptonWsStopMethod;
    IWebApiEndpoint mockWebApiEndpoint = mock(IWebApiEndpoint.class);
    ISession mockSession = mock(ISession.class);
    String sessionId = "123";

    @Before
    public void prepare()
    {
        String request = String.format("{\"method\": \"Stop\",\"version\": \"1.0\",\"sessionId\": \"%s\",\"requestId\": \"request1\"}", sessionId);

        when(kryptonWsSession.getKryptonWsSessionId()).thenReturn(sessionId);
        when(kryptonWsSession.getWebapiSessionId()).thenReturn(sessionId);
        when(kryptonWsSession.getKryptonWsResponseSender()).thenReturn(mockKryptonWsResponseSender);
        when(kryptonWsSession.getWebapiResponseListener()).thenReturn(mockKryptonWsWebapiResponseListener);
        when(mockKryptonWsWebapiResponseListener.getCurrentMethod()).thenReturn(null);

        when(kryptonWsSession.getWebApiEndpoint()).thenReturn(mockWebApiEndpoint);
        when(kryptonWsSession.getWebapiSession()).thenReturn(mockSession);

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        when(mockConfig.getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey())).thenReturn(false);
        when(kryptonWsSession.getWebapiSessionConfig()).thenReturn(mockConfig);

        kryptonWsStopMethod = new KryptonWsStopMethod(new JSONObject(request), kryptonWsSession);
    }

    @Test
    public void testGetLogger() {
        assertNotNull(kryptonWsStopMethod.getLogger());
    }

    @Test(expected = Test.None.class)
    public void testProcessAudio() {
        byte[] bytes = new byte[0];
        kryptonWsStopMethod.onAudioImplementation(bytes);
    }

    @Test
    public void testProcessStopCommand() throws Exception {
        String commandId = "commandIdToTest";
        ICommand mockCommand = mock(ICommand.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn(commandId);
        String command = String.format("{\"sessionId\":\"%s\",\"type\":\"directive\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"%s\"},\"directive\":{\"name\":\"stop\"}}",
                sessionId,
                commandId);
        KryptonWsStopMethod kryptonWsStopMethodToTest = spy(kryptonWsStopMethod);
        kryptonWsStopMethodToTest.process();
        verify(kryptonWsStopMethodToTest).sendToWebapi(command, kryptonWsStopMethodToTest);
    }

    @Test
    public void testProcessStopRecognize() throws Exception {
        String commandId = "commandIdToTest";
        ICommand mockCommand = mock(ICommand.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getCommandId()).thenReturn(commandId);

        KryptonWsRecognizeMethod recognizeMethod = spy(new KryptonWsRecognizeMethod(null, mock(KryptonWsSession.class)));
        when(mockKryptonWsWebapiResponseListener.getCurrentMethod()).thenReturn(recognizeMethod);

        String command = String.format("{\"sessionId\":\"%s\",\"type\":\"directive\",\"command\":{\"name\":\"speechRecognition\",\"id\":\"%s\"},\"directive\":{\"name\":\"stop\"}}",
                sessionId,
                commandId);
        KryptonWsStopMethod kryptonWsStopMethodToTest = spy(kryptonWsStopMethod);
        kryptonWsStopMethodToTest.process();
        verify(kryptonWsStopMethodToTest).sendToWebapi(command, kryptonWsStopMethodToTest);
        verify(recognizeMethod).stopRecognitionTimer();
    }

    @Test
    public void testProcessNoAction() throws Exception {
        KryptonWsStopMethod kryptonWsStopMethodToTest = spy(kryptonWsStopMethod);
        kryptonWsStopMethodToTest.process();
        verify(kryptonWsStopMethodToTest).sendNoActionResponse();
    }

    @Test
    public void testProcessWebapiResponseSuccess() {
        String requestId = "request1";
        kryptonWsStopMethod.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"whatever\",\"command\":{\"id\":\"whatever\",\"name\":\"speechRecognition\"},\"result\":{\"final\":true,\"status\":\"success\"}}");
        verify(mockKryptonWsResponseSender).sendResponse(String.format("{\"method\":\"Response\",\"version\":\"1.0\",\"sessionId\":\"%s\",\"requestId\":\"%s\",\"responseTo\":\"Stop\",\"status\":\"complete\"}",
                sessionId,
                requestId));
    }

    @Test
    public void testProcessWebapiResponseError() {
        KryptonWsStopMethod kryptonWsStopMethodToTest = spy(kryptonWsStopMethod);
        kryptonWsStopMethodToTest.processWebapiResponse("{\"type\":\"result\",\"sessionId\":\"123\",\"command\":{\"id\":\"whatever\",\"name\":\"uploadDynamicVocabulary\"},\"result\":{\"final\":true,\"status\":\"error\",\"errors\":[\"an error\":\"is an error\"]}}");
        verify(kryptonWsStopMethodToTest).sendErrorResponse(anyString(), isNull());
    }

    @Test
    public void canRunWhile()
    {
        kryptonWsSession = mock(KryptonWsSession.class);
        String request1 = String.format("{\"method\": \"Stop\",\"version\": \"1.0\",\"sessionId\": \"%s\",\"requestId\": \"request1\"}", sessionId);
        kryptonWsStopMethod = new KryptonWsStopMethod(new JSONObject(request1), kryptonWsSession);

        String request2 = "{\"method\": \"Load\",\"version\": \"1.0\",\"sessionId\": \"123\",\"requestId\": \"request1\",\"objects\": [{\"id\": \"some_id1\", \"type\": \"application/x-nuance-wordset-pkg\",\"url\": \"https://host/path/wordset1\"},{\"id\": \"some_id2\",\"type\": \"application/x-nuance-wordset\",  \"body\": \"{\\\"<category-name_1>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}],\\\"<category-name_M>\\\" : [{ \\\"literal\\\" : \\\"<written form>\\\", \\\"spoken\\\" : [\\\"<spoken form 1>\\\", \\\"<spoken form n>\\\"]}]}}\"},{\"id\": \"some_id3\", \"type\": \"application/x-nuance-domainlm\",\"url\": \"https://host/path/domainlm\"}]}\n";
        KryptonWsLoadMethod
                commandThatIsAlreadyRunning = new KryptonWsLoadMethod(new JSONObject(request2), kryptonWsSession);

        assertTrue(kryptonWsStopMethod.canRunWhile(commandThatIsAlreadyRunning));
    }
}
