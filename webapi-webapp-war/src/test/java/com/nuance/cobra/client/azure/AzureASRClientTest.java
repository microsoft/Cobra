package com.nuance.cobra.client.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.cognitiveservices.speech.*;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.audio.*;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpeechRecognitionEventArgs.class, SpeechRecognitionCanceledEventArgs.class, SessionManager.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.*"})
public class AzureASRClientTest {

    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private AzureTokenProvider tokenProvider;

    private AzureASRClient azureASRClient;
    private org.apache.commons.configuration2.CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private List<DynamicVocabulary> dynamicVocabularies = new ArrayList<>();
    private void setUpAzureSpeechClient() throws Exception {
        responseHandler = mock(ResponseHandler.class);
        tokenProvider = mock(AzureTokenProvider.class);

        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_END_SILENCE_TIMEOUT_MS.key, "500");
        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_SEGMENTATION_SILENCE_TIMEOUT_MS.key, "500");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "pcm_16_16k");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "en-US");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.key, "eastus");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_API_KEY.key, "dummyApiKey");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key, false);
        cc.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "dummyKeyVaultClientId");

        when(tokenProvider.getAccessToken()).thenReturn("mockAccessToken");

        // Initialize AzureSpeechClient
        azureASRClient = new AzureASRClient("testSessionId", responseHandler, cc, dynamicVocabularies, tokenProvider);

    }

    private void setupAzureSpeechClientWithCodec(String codec) {
        responseHandler = mock(ResponseHandler.class);
        tokenProvider = mock(AzureTokenProvider.class);

        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, codec);
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "en-US");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.key, "eastus");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_API_KEY.key, "dummyApiKey");
        azureASRClient = new AzureASRClient("testSessionId", responseHandler, cc, dynamicVocabularies, tokenProvider);
    }

    private void testSensitivity(String sensitivity) throws Exception {
        cc.setProperty(WebApiConfig.Property.SENSITIVITY.key, sensitivity);
        setUpAzureSpeechClient();
        assertNotNull(azureASRClient);
    }

    @Test
    public void testAllSensitivities() throws Exception {
        String[] sensitivities = {"open", "masked", "encrypted"};
        for (String sensitivity : sensitivities) {
            testSensitivity(sensitivity);
        }
    }

    @Test
    public void testStartSpeechRecognition() throws Exception {
        setUpAzureSpeechClient();
        azureASRClient.startSpeechRecognition();
        assertNotNull((azureASRClient));
    }

    @Test
    public void testStopSpeechRecognition() throws Exception {
        setUpAzureSpeechClient();
        azureASRClient.stopSpeechRecognition();
        assertNotNull((azureASRClient));
    }

    @Test
    public void testCancelSpeechRecognition() throws Exception {
        setUpAzureSpeechClient();
        azureASRClient.cancelSpeechRecognition(true);
        verify(responseHandler, times(1)).onResponse(any(SpeechRecoResponse.class));
    }

    @Test
    public void testCancelSpeechRecognitionAlreadyStopped() throws Exception {
        setUpAzureSpeechClient();
        azureASRClient.cancelSpeechRecognition(true);
        // The 2nd cancellation won't trigger the recognizer.stop because finalResponseSent is set to true.
        azureASRClient.cancelSpeechRecognition(true);
        verify(responseHandler, times(1)).onResponse(any(SpeechRecoResponse.class));
    }

    @Test
    public void testOnAudio() throws Exception {
        setUpAzureSpeechClient();
        AudioChunk audioChunk = new AudioChunk(10L, new byte[0]);
        azureASRClient.onAudio(audioChunk);
        assertNotNull((azureASRClient));
    }

    @Test
    public void testOnEventNoiseLevelCalculated() throws Exception {
        setUpAzureSpeechClient();

        AudioNode eventOwner = mock(SpeechDetector.class);

        NoiseLevel expectedNoiseLevel = new NoiseLevel(5, 5);
        when(((SpeechDetector) eventOwner).getCalculatedBackgroundNoiseLevel()).thenReturn(expectedNoiseLevel);

        azureASRClient.onEvent(eventOwner, AudioNodeObserver.NodeEvent.NEW_NOISE_LEVEL_CALCULATED);

        assertEquals(expectedNoiseLevel, azureASRClient.getNoiseLevel());
    }

    @Test
    public void testOnEventEndOfSpeech() throws Exception {
        setUpAzureSpeechClient();

        AudioNode eventOwner = mock(SpeechDetector.class);

        azureASRClient.onEvent(eventOwner, AudioNodeObserver.NodeEvent.END_OF_SPEECH);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testOnEventAudioStatisticsInvalidSession() throws Exception {
        setUpAzureSpeechClient();

        cc.setProperty(WebApiConfig.Property.STATISTICS.key, true);

        Map<String, Object> mockStatistics = Map.of(
                StatisticsCalculatorNode.STATISTIC_MEDIA_DURATION, 100,
                StatisticsCalculatorNode.STATISTIC_MEDIA_SIZE, 200
        );

        StatisticsCalculatorNode statisticsCalculatorNode = mock(StatisticsCalculatorNode.class);
        when(statisticsCalculatorNode.getStatistics(StatisticsCalculatorNode.ALL_STATISTICS)).thenReturn(mockStatistics);

        azureASRClient.onEvent(statisticsCalculatorNode, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testOnEventAudioStatisticsValidSession() throws Exception {
        // Set the property for statistics
        cc.setProperty(WebApiConfig.Property.STATISTICS.key, true);

        // Set up the Azure Speech Client
        setUpAzureSpeechClient();

        // Mock the statistics data
        Map<String, Object> mockStatistics = Map.of(
                StatisticsCalculatorNode.STATISTIC_MEDIA_DURATION, 100,
                StatisticsCalculatorNode.STATISTIC_MEDIA_SIZE, 200
        );

        // Mock the StatisticsCalculatorNode
        StatisticsCalculatorNode statisticsCalculatorNode = mock(StatisticsCalculatorNode.class);
        when(statisticsCalculatorNode.getStatistics(StatisticsCalculatorNode.ALL_STATISTICS)).thenReturn(mockStatistics);

        // Mock the SessionManager and Session
        SessionManager mockSessionManager = mock(SessionManager.class);
        Session mockSession = mock(Session.class);

        PowerMockito.mockStatic(SessionManager.class);
        when(SessionManager.getInstance()).thenReturn(mockSessionManager);

        Command command = mock(Command.class);
        Command.CommandStatistics commandStatistics = mock(Command.CommandStatistics.class);

        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(command);
        when(command.getStatistics()).thenReturn(commandStatistics);

        // Set finalResultsJson to a non-empty value
        // Set finalResultsJson to a non-empty value using reflection
        Field finalResultsJsonField = AzureASRClient.class.getDeclaredField("finalResultsJson");
        finalResultsJsonField.setAccessible(true);
        finalResultsJsonField.set(azureASRClient, "{\"NBest\": [{\"Lexical\": \"test result\", \"Confidence\": 0.9}]}");

        // Trigger the event
        azureASRClient.onEvent(statisticsCalculatorNode, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);

        // Verify that the statistics were updated correctly
        assertNotNull(azureASRClient);
        assertNotNull(command.getStatistics());
        assertEquals(100, (int) command.getStatistics().mediaDuration);
        assertEquals(200, (int) command.getStatistics().mediaSize);
    }

    @Test
    public void testOnEventTimeoutStartSpeech() throws Exception {
        setUpAzureSpeechClient();

        TimeoutNode eventOwner = mock(TimeoutNode.class);
        when(eventOwner.getTimerID()).thenReturn(AzureASRClient.START_SPEECH_TIMER_ID);

        azureASRClient.onEvent(eventOwner, AudioNodeObserver.NodeEvent.TIMEOUT);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testOnEventTimeoutMaxSpeech() throws Exception {
        setUpAzureSpeechClient();

        TimeoutNode eventOwner = mock(TimeoutNode.class);
        when(eventOwner.getTimerID()).thenReturn(AzureASRClient.MAX_SPEECH_TIMER_ID);

        azureASRClient.onEvent(eventOwner, AudioNodeObserver.NodeEvent.TIMEOUT);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testInitializationWithDifferentCodecs() {
        Collection<String> codecs = Arrays.asList("pcm_16_8k", "pcm_16_22k", "pcm_16_24k");

        for (String codec : codecs) {
            setupAzureSpeechClientWithCodec(codec);
            assertNotNull(azureASRClient);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializationWithUnsupportedCodec() {
        setupAzureSpeechClientWithCodec("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializationWithUnsupportedRecoMode() throws Exception {
        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_MODE.key, "invalidRecoMode");
        setUpAzureSpeechClient();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializationWithUnsupportedBestResultFormat() throws Exception {
        setUpAzureSpeechClient();
        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_BEST_RESULT_FORMAT.key, "invalidBestResultFormat");

        // Create a sample JSON string
        String jsonText = "{\"DisplayText\": \"recognized text\", \"NBest\": [{\"Lexical\": \"recognized text\", \"Confidence\": 0.9, \"Words\": [{\"Word\": \"recognized\", \"Confidence\": 0.9}, {\"Word\": \"text\", \"Confidence\": 0.9}]}]}";

        // Use reflection to access the private method
        Method createSpeechRecoResponseFromJsonMethod = AzureASRClient.class.getDeclaredMethod("createSpeechRecoResponseFromJson", String.class);
        createSpeechRecoResponseFromJsonMethod.setAccessible(true);

        // Call the method to trigger exception error
       createSpeechRecoResponseFromJsonMethod.invoke(azureASRClient, jsonText);
    }

    @Test
    public void testCreateSpeechRecoResponseFromJson() throws Exception {
        setUpAzureSpeechClient();

        // Create a sample JSON string
        String jsonText = "{\"DisplayText\": \"recognized text\", \"NBest\": [{\"Lexical\": \"recognized text\", \"Confidence\": 0.9, \"Words\": [{\"Word\": \"recognized\", \"Confidence\": 0.9}, {\"Word\": \"text\", \"Confidence\": 0.9}]}]}";

        // Use reflection to access the private method
        Method createSpeechRecoResponseFromJsonMethod = AzureASRClient.class.getDeclaredMethod("createSpeechRecoResponseFromJson", String.class);
        createSpeechRecoResponseFromJsonMethod.setAccessible(true);

        // Call the method and get the result
        SpeechRecoResponse response = (SpeechRecoResponse) createSpeechRecoResponseFromJsonMethod.invoke(azureASRClient, jsonText);

        // Verify the response
        assertNotNull(response);
        assertTrue(response.isFinal());
        assertEquals("recognized text", response.getBestResult());
    }

    @Test
    public void testCreateSpeechRecoResponseFromJsonSorting() throws Exception {
        setUpAzureSpeechClient();

        // Create a sample JSON string with multiple NBest entries
        String jsonText = "{\"DisplayText\": \"\", \"NBest\": [" +
                "{\"Lexical\": \"test three\", \"Confidence\": 0.3, \"Words\": [{\"Word\": \"test\", \"Confidence\": 0.3}, {\"Word\": \"three\", \"Confidence\": 0.3}]}, " +
                "{\"Lexical\": \"test one\", \"Confidence\": 0.9, \"Words\": [{\"Word\": \"test\", \"Confidence\": 0.9}, {\"Word\": \"one\", \"Confidence\": 0.9}]}, " +
                "{\"Lexical\": \"test two\", \"Confidence\": 0.5, \"Words\": [{\"Word\": \"test\", \"Confidence\": 0.5}, {\"Word\": \"two\", \"Confidence\": 0.5}]}" +
                "]}";

        // Use reflection to access the private method
        Method createSpeechRecoResponseFromJsonMethod = AzureASRClient.class.getDeclaredMethod("createSpeechRecoResponseFromJson", String.class);
        createSpeechRecoResponseFromJsonMethod.setAccessible(true);

        // Call the method and get the result
        SpeechRecoResponse response = (SpeechRecoResponse) createSpeechRecoResponseFromJsonMethod.invoke(azureASRClient, jsonText);

        // Verify the response
        assertNotNull(response);
        assertTrue(response.isFinal());

    }

    @Test
    public void testExtractDataFromJson() throws Exception {
        setUpAzureSpeechClient();
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_MAX_TRANSCRIPTIONS.key, 3);
        // Create a sample JsonNode
        String jsonText = "{\n" +
                "  \"Id\": \"2b701f0c12f84046bff5ed124a66fd82\",\n" +
                "  \"RecognitionStatus\": \"Success\",\n" +
                "  \"Offset\": 5500000,\n" +
                "  \"Duration\": 21100000,\n" +
                "  \"Channel\": 0,\n" +
                "  \"DisplayText\": \"Good morning. How are you doing today?\",\n" +
                "  \"NBest\": [\n" +
                "    {\n" +
                "      \"Confidence\": 0.967186,\n" +
                "      \"Lexical\": \"good morning how are you doing today\",\n" +
                "      \"ITN\": \"good morning how are you doing today\",\n" +
                "      \"MaskedITN\": \"good morning how are you doing today\",\n" +
                "      \"Display\": \"Good morning. How are you doing today?\",\n" +
                "      \"Words\": [\n" +
                "        {\"Word\": \"good\", \"Offset\": 5500000, \"Duration\": 3500000, \"Confidence\": 0.9675267},\n" +
                "        {\"Word\": \"morning\", \"Offset\": 9100000, \"Duration\": 4100000, \"Confidence\": 0.9339547},\n" +
                "        {\"Word\": \"how\", \"Offset\": 13300000, \"Duration\": 2600000, \"Confidence\": 0.98104954},\n" +
                "        {\"Word\": \"are\", \"Offset\": 16000000, \"Duration\": 1400000, \"Confidence\": 0.9948927},\n" +
                "        {\"Word\": \"you\", \"Offset\": 17500000, \"Duration\": 1100000, \"Confidence\": 0.9788227},\n" +
                "        {\"Word\": \"doing\", \"Offset\": 18700000, \"Duration\": 3300000, \"Confidence\": 0.9462203},\n" +
                "        {\"Word\": \"today\", \"Offset\": 22100000, \"Duration\": 4500000, \"Confidence\": 0.9925493}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Confidence\": 0.9582986,\n" +
                "      \"Lexical\": \"good morning how are you doing today i\",\n" +
                "      \"ITN\": \"good morning how are you doing today i\",\n" +
                "      \"MaskedITN\": \"good morning how are you doing today i\",\n" +
                "      \"Display\": \"good morning how are you doing today i\",\n" +
                "      \"Words\": [\n" +
                "        {\"Word\": \"good\", \"Offset\": 5500000, \"Duration\": 3500000, \"Confidence\": 0.9675267},\n" +
                "        {\"Word\": \"morning\", \"Offset\": 9100000, \"Duration\": 4100000, \"Confidence\": 0.9339547},\n" +
                "        {\"Word\": \"how\", \"Offset\": 13300000, \"Duration\": 2600000, \"Confidence\": 0.98104954},\n" +
                "        {\"Word\": \"are\", \"Offset\": 16000000, \"Duration\": 1400000, \"Confidence\": 0.9948927},\n" +
                "        {\"Word\": \"you\", \"Offset\": 17500000, \"Duration\": 1100000, \"Confidence\": 0.9788227},\n" +
                "        {\"Word\": \"doing\", \"Offset\": 18700000, \"Duration\": 3300000, \"Confidence\": 0.9462203},\n" +
                "        {\"Word\": \"today\", \"Offset\": 22100000, \"Duration\": 4200000, \"Confidence\": 0.97248054},\n" +
                "        {\"Word\": \"i\", \"Offset\": 26400000, \"Duration\": 200000, \"Confidence\": 0.6521657}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Confidence\": 0.9521875,\n" +
                "      \"Lexical\": \"good morning how are you doing today at\",\n" +
                "      \"ITN\": \"good morning how are you doing today at\",\n" +
                "      \"MaskedITN\": \"good morning how are you doing today at\",\n" +
                "      \"Display\": \"good morning how are you doing today at\",\n" +
                "      \"Words\": [\n" +
                "        {\"Word\": \"good\", \"Offset\": 5500000, \"Duration\": 3500000, \"Confidence\": 0.9675267},\n" +
                "        {\"Word\": \"morning\", \"Offset\": 9100000, \"Duration\": 4100000, \"Confidence\": 0.9339547},\n" +
                "        {\"Word\": \"how\", \"Offset\": 13300000, \"Duration\": 2600000, \"Confidence\": 0.98104954},\n" +
                "        {\"Word\": \"are\", \"Offset\": 16000000, \"Duration\": 1400000, \"Confidence\": 0.9948927},\n" +
                "        {\"Word\": \"you\", \"Offset\": 17500000, \"Duration\": 1100000, \"Confidence\": 0.9788227},\n" +
                "        {\"Word\": \"doing\", \"Offset\": 18700000, \"Duration\": 3300000, \"Confidence\": 0.9462203},\n" +
                "        {\"Word\": \"today\", \"Offset\": 22100000, \"Duration\": 3900000, \"Confidence\": 0.97023153},\n" +
                "        {\"Word\": \"at\", \"Offset\": 26100000, \"Duration\": 500000, \"Confidence\": 0.6113881}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Confidence\": 0.95169353,\n" +
                "      \"Lexical\": \"good morning how are you doing today is\",\n" +
                "      \"ITN\": \"good morning how are you doing today is\",\n" +
                "      \"MaskedITN\": \"good morning how are you doing today is\",\n" +
                "      \"Display\": \"good morning how are you doing today is\",\n" +
                "      \"Words\": [\n" +
                "        {\"Word\": \"good\", \"Offset\": 5500000, \"Duration\": 3500000, \"Confidence\": 0.9675267},\n" +
                "        {\"Word\": \"morning\", \"Offset\": 9100000, \"Duration\": 4100000, \"Confidence\": 0.9339547},\n" +
                "        {\"Word\": \"how\", \"Offset\": 13300000, \"Duration\": 2600000, \"Confidence\": 0.98104954},\n" +
                "        {\"Word\": \"are\", \"Offset\": 16000000, \"Duration\": 1400000, \"Confidence\": 0.9948927},\n" +
                "        {\"Word\": \"you\", \"Offset\": 17500000, \"Duration\": 1100000, \"Confidence\": 0.9788227},\n" +
                "        {\"Word\": \"doing\", \"Offset\": 18700000, \"Duration\": 3300000, \"Confidence\": 0.9462203},\n" +
                "        {\"Word\": \"today\", \"Offset\": 22100000, \"Duration\": 3900000, \"Confidence\": 0.97023153},\n" +
                "        {\"Word\": \"is\", \"Offset\": 26100000, \"Duration\": 500000, \"Confidence\": 0.5939343}\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Confidence\": 0.9528376,\n" +
                "      \"Lexical\": \"good morning how are you doing today in\",\n" +
                "      \"ITN\": \"good morning how are you doing today in\",\n" +
                "      \"MaskedITN\": \"good morning how are you doing today in\",\n" +
                "      \"Display\": \"good morning how are you doing today in\",\n" +
                "      \"Words\": [\n" +
                "        {\"Word\": \"good\", \"Offset\": 5500000, \"Duration\": 3500000, \"Confidence\": 0.9675267},\n" +
                "        {\"Word\": \"morning\", \"Offset\": 9100000, \"Duration\": 4100000, \"Confidence\": 0.9339547},\n" +
                "        {\"Word\": \"how\", \"Offset\": 13300000, \"Duration\": 2600000, \"Confidence\": 0.98104954},\n" +
                "        {\"Word\": \"are\", \"Offset\": 16000000, \"Duration\": 1400000, \"Confidence\": 0.9948927},\n" +
                "        {\"Word\": \"you\", \"Offset\": 17500000, \"Duration\": 1100000, \"Confidence\": 0.9788227},\n" +
                "        {\"Word\": \"doing\", \"Offset\": 18700000, \"Duration\": 3300000, \"Confidence\": 0.9462203},\n" +
                "        {\"Word\": \"today\", \"Offset\": 22100000, \"Duration\": 3900000, \"Confidence\": 0.97023153},\n" +
                "        {\"Word\": \"in\", \"Offset\": 26100000, \"Duration\": 500000, \"Confidence\": 0.6343573}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode bestResultNode = mapper.readTree(jsonText);

        // Use reflection to access the private method
        Method extractDataFromJsonMethod = AzureASRClient.class.getDeclaredMethod("extractDataFromJson", JsonNode.class);
        extractDataFromJsonMethod.setAccessible(true);

        // Call the method and get the result
        SpeechRecoResponse.Data data = (SpeechRecoResponse.Data) extractDataFromJsonMethod.invoke(azureASRClient, bestResultNode);

        // Verify the data
        assertNotNull(data);
        assertEquals("good morning how are you doing today", data.getTranscriptions()[0]);
        assertEquals("good morning how are you doing today i", data.getTranscriptions()[1]);
        assertEquals("good morning how are you doing today at", data.getTranscriptions()[2]);
        assertEquals(970, data.getAverageWeightedConfidences()[0]);
        assertEquals(7, data.getWords()[0].length);
        assertEquals(8, data.getWords()[1].length);
        assertEquals(8, data.getWords()[2].length);
    }

    @Test
    public void testCleanUpResources() throws Exception {
        setUpAzureSpeechClient();

        // Access the private method cleanUpResources
        Method cleanUpResourcesMethod = AzureASRClient.class.getDeclaredMethod("cleanUpResources");
        cleanUpResourcesMethod.setAccessible(true);

        // Call the method
        cleanUpResourcesMethod.invoke(azureASRClient);

        // Verify that the resources are cleaned up
        assertNotNull(Whitebox.getInternalState(azureASRClient, "recognizer"));
        assertNotNull(Whitebox.getInternalState(azureASRClient, "speechConfig"));
        assertNotNull(Whitebox.getInternalState(azureASRClient, "pushStream"));
    }

    @Test
    public void testSetRecResultType() throws Exception {
        setUpAzureSpeechClient();
        boolean result = azureASRClient.setRecResultType(RecResultType.getDefaultType());
        assertFalse(result);
    }

    @Test
    public void testGetMaxConfidence() throws Exception {
        setUpAzureSpeechClient();
        float maxConfidence = azureASRClient.getMaxConfidence();
        assertEquals(1000, maxConfidence, 0);
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersFinalResults() throws Exception {
        setUpAzureSpeechClient();
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.key, true);

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");
        // Set the startOfSpeechWasDetected field to true via reflection
        Whitebox.setInternalState(azureASRClient, "startOfSpeechWasDetected", true);

        // Create a mock SpeechRecognitionResult
        SpeechRecognitionResult recognitionResult = mock(SpeechRecognitionResult.class);
        when(recognitionResult.getReason()).thenReturn(ResultReason.RecognizedSpeech);
        when(recognitionResult.getText()).thenReturn("Recognized text");
        PropertyCollection properties = mock(PropertyCollection.class);
        when(recognitionResult.getProperties()).thenReturn(properties);
        when(properties.getProperty(PropertyId.SpeechServiceResponse_JsonResult))
                .thenReturn("{\"DisplayText\": \"Recognized text\", \"NBest\": [{\"Lexical\": \"Recognized text\", \"Confidence\": 0.9, \"Words\": [{\"Word\": \"Recognized\", \"Confidence\": 0.9}, {\"Word\": \"text\", \"Confidence\": 0.9}]}]}");

        // Create a mock SpeechRecognitionEventArgs
        SpeechRecognitionEventArgs recognizedEventArgs = PowerMockito.mock(SpeechRecognitionEventArgs.class);
        when(recognizedEventArgs.getResult()).thenReturn(recognitionResult);

        // Invoke the listener manually
        recognizer.recognized.fireEvent(recognizer, recognizedEventArgs);

        // Verify that the final response is created and sent to the response handler
        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(responseHandler, times(1)).onResponse(captor.capture());

        SpeechRecoResponse response = captor.getValue();
        assertTrue(response.isFinal());
        assertEquals("Recognized text", response.getBestResult());
    }


    @Test
    public void testSetupSpeechRecognitionEventListenersFinalResultsWithNoMatchResponse() throws Exception {
        responseHandler = mock(ResponseHandler.class);
        tokenProvider = mock(AzureTokenProvider.class);

        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_END_SILENCE_TIMEOUT_MS.key, "500");
        cc.setProperty(WebApiConfig.Property.MCSFT_SPEECH_RECOGNITION_SEGMENTATION_SILENCE_TIMEOUT_MS.key, "500");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "pcm_16_16k");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key, "en-US");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_SERVICE_REGION.key, "eastus");
        cc.setProperty(WebApiConfig.Property.MICROSOFT_SPEECH_API_KEY.key, "dummyApiKey");
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_ENABLE_PROFANITY_FILTER.key, true);
        cc.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_VERBOSE_RESULT.key, true);
        cc.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "dummyKeyVaultClientId");

        when(tokenProvider.getAccessToken()).thenReturn("mockAccessToken");

        // Initialize AzureSpeechClient
        azureASRClient = new AzureASRClient("testSessionId", responseHandler, cc, dynamicVocabularies, tokenProvider);

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");
        // Set the startOfSpeechWasDetected field to true via reflection
        Whitebox.setInternalState(azureASRClient, "startOfSpeechWasDetected", true);

        // Create a mock SpeechRecognitionResult
        SpeechRecognitionResult recognitionResult = mock(SpeechRecognitionResult.class);
        when(recognitionResult.getReason()).thenReturn(ResultReason.RecognizedSpeech);
        when(recognitionResult.getText()).thenReturn("Recognized text");
        PropertyCollection properties = mock(PropertyCollection.class);
        when(recognitionResult.getProperties()).thenReturn(properties);
        when(properties.getProperty(PropertyId.SpeechServiceResponse_JsonResult))
                .thenReturn("{\"DisplayText\": \"Recognized text\", \"NBest\": [{\"Lexical\": \"Recognized text\", \"Confidence\": 0.9, \"Words\": [{\"Word\": \"Recognized\", \"Confidence\": 0.9}, {\"Word\": \"text\", \"Confidence\": 0.9}]}]}");

        // Create a mock SpeechRecognitionEventArgs
        SpeechRecognitionEventArgs recognizedEventArgs = PowerMockito.mock(SpeechRecognitionEventArgs.class);
        when(recognizedEventArgs.getResult()).thenReturn(recognitionResult);

        // Invoke the listener manually
        recognizer.recognized.fireEvent(recognizer, recognizedEventArgs);

        // Verify that the final response is created and sent to the response handler
        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(responseHandler, times(1)).onResponse(captor.capture());

        SpeechRecoResponse response = captor.getValue();
        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.NO_MATCH, response.getStatus());
        assertNull("Recognized text", response.getBestResult());
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersNoMatchFinalResults() throws Exception {
        setUpAzureSpeechClient();

        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        SpeechRecognitionResult recognitionResult = mock(SpeechRecognitionResult.class);
        when(recognitionResult.getReason()).thenReturn(ResultReason.NoMatch);

        SpeechRecognitionEventArgs recognizedEventArgs = PowerMockito.mock(SpeechRecognitionEventArgs.class);
        when(recognizedEventArgs.getResult()).thenReturn(recognitionResult);

        recognizer.recognized.fireEvent(recognizer, recognizedEventArgs);

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(responseHandler, times(1)).onResponse(captor.capture());

        SpeechRecoResponse response = captor.getValue();
        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.NO_MATCH, response.getStatus());
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersNoMatchPartialResults() throws Exception {
        setUpAzureSpeechClient();

        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        SpeechRecognitionResult partialRecognitionResult = mock(SpeechRecognitionResult.class);
        when(partialRecognitionResult.getReason()).thenReturn(ResultReason.NoMatch);

        SpeechRecognitionEventArgs partialEventArgs = PowerMockito.mock(SpeechRecognitionEventArgs.class);
        when(partialEventArgs.getResult()).thenReturn(partialRecognitionResult);

        recognizer.recognizing.fireEvent(recognizer, partialEventArgs);

        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        assertNotNull(azureASRClient);
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersPartialResults() throws Exception {
        setUpAzureSpeechClient();

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        // Create a mock SpeechRecognitionResult for partial results
        SpeechRecognitionResult partialRecognitionResult = mock(SpeechRecognitionResult.class);
        when(partialRecognitionResult.getReason()).thenReturn(ResultReason.RecognizingSpeech);
        when(partialRecognitionResult.getText()).thenReturn("Partial Recognized text");
        PropertyCollection properties = mock(PropertyCollection.class);
        when(partialRecognitionResult.getProperties()).thenReturn(properties);
        when(properties.getProperty(PropertyId.SpeechServiceResponse_JsonResult))
                .thenReturn("{\"type\": \"result\", \"sessionId\": \"dummySessionId\", \"command\": {\"name\": \"speechRecognition\", \"id\": \"2\"}, \"result\": {\"final\": false, \"status\": \"success\", \"noiseLevel\": {\"mean\": 5.21, \"standardDeviation\": 5.43}}}");


        // Create a mock SpeechRecognitionEventArgs for partial results
        SpeechRecognitionEventArgs partialEventArgs = PowerMockito.mock(SpeechRecognitionEventArgs.class);
        when(partialEventArgs.getResult()).thenReturn(partialRecognitionResult);

        // Invoke the listener manually for partial results
        recognizer.recognizing.fireEvent(recognizer, partialEventArgs);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersCanceled() throws Exception {
        setUpAzureSpeechClient();

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        // Create a mock SpeechRecognitionCanceledEventArgs
        SpeechRecognitionCanceledEventArgs canceledEventArgs = PowerMockito.mock(SpeechRecognitionCanceledEventArgs.class);
        // Create a mock SpeechRecognitionResult
        SpeechRecognitionResult recognitionResult = PowerMockito.mock(SpeechRecognitionResult.class);

        when(recognitionResult.getReason()).thenReturn(ResultReason.Canceled);

        when(canceledEventArgs.getResult()).thenReturn(recognitionResult);
        when(canceledEventArgs.getReason()).thenReturn(CancellationReason.Error);
        when(canceledEventArgs.getErrorCode()).thenReturn(CancellationErrorCode.ServiceError);

        // Invoke the listener manually for canceled event
        recognizer.canceled.fireEvent(recognizer, canceledEventArgs);

        // Verify that the canceled response is created and sent to the response handler
        ArgumentCaptor<SpeechRecoResponse> captor = ArgumentCaptor.forClass(SpeechRecoResponse.class);
        verify(responseHandler, times(1)).onResponse(captor.capture());

        SpeechRecoResponse response = captor.getValue();
        assertTrue(response.isFinal());
        assertNull(response.getBestResult());
        assertEquals(ResponseResult.ResponseStatus.CANCELLED, response.getStatus());
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersSessionStarted() throws Exception {
        setUpAzureSpeechClient();

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        // Create a mock SpeechRecognitionEventArgs
        SessionEventArgs sessionEventArgs = PowerMockito.mock(SessionEventArgs.class);

        // Invoke the listener manually
        recognizer.sessionStarted.fireEvent(recognizer, sessionEventArgs);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testSetupSpeechRecognitionEventListenersSessionStopped() throws Exception {
        setUpAzureSpeechClient();

        // Access the private recognizer via reflection
        SpeechRecognizer recognizer = Whitebox.getInternalState(azureASRClient, "recognizer");

        // Create a mock SpeechRecognitionEventArgs
        SessionEventArgs sessionEventArgs = PowerMockito.mock(SessionEventArgs.class);

        // Invoke the listener manually
        recognizer.sessionStopped.fireEvent(recognizer, sessionEventArgs);

        assertNotNull(azureASRClient);
    }

    @Test
    public void testSetObservers() throws Exception {
        setUpAzureSpeechClient();

        // Create a list of mocked AudioNodeObserver objects
        List<AudioNodeObserver> observers = new ArrayList<>();
        AudioNodeObserver observer1 = mock(AudioNodeObserver.class);
        AudioNodeObserver observer2 = mock(AudioNodeObserver.class);
        observers.add(observer1);
        observers.add(observer2);

        // Call the setObservers method
        azureASRClient.setObservers(observers);

        // Verify that the observers size is correct
        assertEquals(2, azureASRClient.observers.size());
    }

    @Test
    public void testPopulateDynamicVocabularies() throws Exception {
        // Creating vocabulary items
        DynamicVocabulary.Vocabulary vocab1 = new DynamicVocabulary.Vocabulary();
        vocab1.setLiteral("Los Angeles");
        vocab1.addSpoken("l a");
        vocab1.addSpoken("los angeles");

        DynamicVocabulary.Vocabulary vocab2 = new DynamicVocabulary.Vocabulary();
        vocab2.setLiteral("Montreal Canadiens");
        vocab2.addSpoken("montreal canadiens");
        vocab2.addSpoken("habs");

        DynamicVocabulary.Vocabulary vocab3 = new DynamicVocabulary.Vocabulary();
        vocab3.setLiteral("Minnesota Vikings");
        vocab3.addSpoken("minnesota vikings");
        vocab3.addSpoken("vikes");

        DynamicVocabulary.Vocabulary vocab4 = new DynamicVocabulary.Vocabulary();
        vocab4.setLiteral("Good morning, how are you doing today?");
        // No spoken forms for this vocabulary item

        // Creating vocabulary bodies
        Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body1 = new HashMap<>();
        body1.put("place-name", new ArrayList<>(Arrays.asList(vocab1)));
        body1.put("sport", new ArrayList<>(Arrays.asList(vocab2, vocab3)));

        Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body2 = new HashMap<>();
        body2.put("greeting", new ArrayList<>(Arrays.asList(vocab4)));

        // Creating dynamic vocabulary objects
        DynamicVocabulary dynamicVocabulary1 = new DynamicVocabulary("1000", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, body1);
        DynamicVocabulary dynamicVocabulary2 = new DynamicVocabulary("2000", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET, body2);

        // Adding dynamic vocabulary objects to the list
        dynamicVocabularies.add(dynamicVocabulary1);
        dynamicVocabularies.add(dynamicVocabulary2);
        setUpAzureSpeechClient();
        assertNotNull(azureASRClient);
    }
}
