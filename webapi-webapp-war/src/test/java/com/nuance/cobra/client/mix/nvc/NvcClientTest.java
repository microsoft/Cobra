package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.tts.v1.SynthesizerGrpc;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.StringReader;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

public class NvcClientTest extends CobraTest {

    @ClassRule
    public static GrpcCleanupRule cleanupRule = new GrpcCleanupRule();

    private static String nvcServerName = InProcessServerBuilder.generateName();
    private static TestNvcService nvcService = new TestNvcService();
    private static ManagedChannel grpcChannel;

    private ResponseHandler mockResponseHandler;
    private ResponseErrorHandler mockErrorHandler;
    private NvcClient nvcClient;

    @BeforeClass
    public static void runsOnlyOnceBeforeTests() throws IOException {
        Server grpcServer = InProcessServerBuilder.forName(nvcServerName)
                .directExecutor() // directExecutor is fine for unit tests
                .addService(nvcService)
                .build()
                .start();
        cleanupRule.register(grpcServer);
    }

    @Before
    public void runsBeforeEachTest() throws Exception {
        super.setUp(); // required so that CobraTest.setUp() is called

        nvcService.resetMocks();
        nvcService.setBehavior(TestNvcService.Behavior.SUCCESS);
        nvcService.setSimulateLongProcessing(false);

        mockResponseHandler = mock(ResponseHandler.class);
        mockErrorHandler = mock(ResponseErrorHandler.class);
        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(buildCobraConfig()), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
    }

    private static SynthesizerGrpc.SynthesizerStub buildNvcStub() {
        // gRPC channel (client<->server)
        // we build a new channel each time because they are shut down after each tts request
        grpcChannel = InProcessChannelBuilder.forName(nvcServerName)
                .directExecutor() // directExecutor is fine for unit tests
                .build();

        // gRPC stub
        return SynthesizerGrpc.newStub(grpcChannel);
    }

    private static CombinedConfiguration buildCobraConfig() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        // start session command
        config.addProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
        config.addProperty("type", "text");
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "NvcClientTest");

        return configs;
    }

    @Test
    public void testStartSpeechSynthesis_200_ok() throws InterruptedException {
        nvcClient.startSpeechSynthesis();

        verify(mockResponseHandler, atLeastOnce()).onAudio(any(), anyBoolean());
        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_ttsInputType_ssml() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        // start session command
        config.addProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
        config.addProperty("type", "ssml");
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");
        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "NvcClientTest");

        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(configs), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        nvcClient.startSpeechSynthesis();

        verify(mockResponseHandler, atLeastOnce()).onAudio(any(), anyBoolean());
        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_ttsInputType_null() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration config = configs.getConfiguration(0);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");

        // start session command
        config.addProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
//        config.addProperty("type", "text"); // not set
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");

        config.addProperty(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, 1);

        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(configs), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        nvcClient.startSpeechSynthesis();

        verifyError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_EXECUTE_ERROR);

        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_ttsInputType_unsupported() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration config = configs.getConfiguration(0);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");

        // start session command
        config.addProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
        config.addProperty("type", "oeiarweowrai"); // unsupported
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");

        config.addProperty(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, 1);

        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(configs), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        nvcClient.startSpeechSynthesis();

        verifyError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_EXECUTE_ERROR);
        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_stop() {
        nvcService.setSimulateLongProcessing(true);

        nvcClient.startSpeechSynthesis();
        nvcClient.stopSpeechSynthesis();

        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Test
    public void testStartSpeechSynthesis_cancel() {
        nvcService.setSimulateLongProcessing(true);

        nvcClient.startSpeechSynthesis();
        nvcClient.cancelSpeechSynthesis(true);

        verifyResponse(ResponseResult.ResponseStatus.CANCELLED);
    }

    private void verifyResponse(ResponseResult.ResponseStatus responseStatus) {
        verify(mockResponseHandler, times(1)).onResponse(any());
        verify(mockErrorHandler, never()).handleError(any());

        ArgumentCaptor<SpeechSynthesisResponse> captor = ArgumentCaptor.forClass(SpeechSynthesisResponse.class);
        verify(mockResponseHandler).onResponse(captor.capture());
        SpeechSynthesisResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(responseStatus, response.getStatus());

        assertTrue(nvcClient.isSpeechSynthesisDone());
        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_500_internalServerError() {
        nvcService.setBehavior(TestNvcService.Behavior.INTERNAL_SERVER_ERROR_500);

        nvcClient.startSpeechSynthesis();

        verifyError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechSynthesis_server_error() {
        Session mockSession = mock(Session.class);
        Command mockCommand = mock(Command.class);
        Command.CommandStatistics mockCommandStatistics = mock(Command.CommandStatistics.class);

        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockCommand.getStatistics()).thenReturn(mockCommandStatistics);
        when(mockSession.getSessionConfig()).thenReturn(mockServerConfigs);

        nvcService.setBehavior(TestNvcService.Behavior.ERROR);

        nvcClient.startSpeechSynthesis();

        verifyError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_GENERIC_ERROR);
    }

    @Test
    public void testStartSpeechSynthesis_client_error()
            throws InvalidKeySpecException
    {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

        NvcRequestParams mockParams = mock(NvcRequestParams.class);
        doThrow(new RuntimeException("expected error message"))
                .when(mockParams)
                .getVoiceName();

        when(mockParams.getConfig()).thenReturn(configs);
        when(mockParams.getCodec()).thenReturn(AudioCodec.fromString("pcm_16_16k"));

        nvcClient = new NvcClient("session-id-test", mockParams, buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        nvcClient.startSpeechSynthesis();

        verifyError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_EXECUTE_ERROR);
    }

    private void verifyError(ExternalResponseErrorCode speechSynthesisCommandExecuteError) {
        verify(mockResponseHandler, never()).onAudio(any(), anyBoolean());
        verify(mockResponseHandler, never()).onResponse(any());
        verify(mockErrorHandler, times(1)).handleError(any());

        ArgumentCaptor<SpeechSynthesisResponse> captor = ArgumentCaptor.forClass(SpeechSynthesisResponse.class);
        verify(mockErrorHandler).handleError(captor.capture());
        SpeechSynthesisResponse response = captor.getValue();

        assertTrue(response.isFinal());
        assertEquals(ResponseResult.ResponseStatus.ERROR, response.getStatus());

        List<ResponseCode> errors = response.getErrors();
        assertEquals(1, errors.size());

        ResponseCode error = errors.get(0);
        ResponseCode expectedError = ResponseCode.createResponseError(speechSynthesisCommandExecuteError);

        assertEquals(expectedError, error);

        assertTrue(nvcClient.isSpeechSynthesisDone());
        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_withResources() throws ConfigurationException {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "  \"data\": \"hi\",\n" +
                "  \"speechSynthesisCodec\": \"pcm_16_16k\",\n" +
                "  \"type\": \"text\",\n" +
                "  \"vocsMapping\": {\n" +
                "    \"Evan\": {\n" +
                "      \"language\": \"en-us\",\n" +
                "      \"name\": \"Evan\",\n" +
                "      \"pcm_16_16k\": {\n" +
                "        \"voice_model\": \"enhanced\"\n" +
                "      },\n" +
                "      \"tuneable_data\": [\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-activeprompt-db\",\n" +
                "          \"uri\": \"activeprompturi\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-rettt+text\",\n" +
                "          \"uri\": \"ruleseturi\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"user_dictionary\": [\n" +
                "        {\n" +
                "          \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "          \"uri\": \"userdictionaryuri\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"applicationKey\": \"NvcClientTest\"," +
                "  \"voice\": \"Evan\"\n" +
                "}"));

        testStartSpeechSynthesis_withResources_generic(configs);

        List<SynthesizerProto.SynthesisResource> resources = nvcService.getResources();
        assertEquals(3, resources.size());

        boolean hasActivePrompt = false;
        boolean hasRuleset = false;
        boolean hasUserDictionary = false;

        for (SynthesizerProto.SynthesisResource resource : resources) {
            if (resource.getType() == SynthesizerProto.EnumResourceType.ACTIVEPROMPT_DB) {
                assertEquals("activeprompturi", resource.getUri());
                hasActivePrompt = true;

            } else if (resource.getType() == SynthesizerProto.EnumResourceType.TEXT_USER_RULESET) {
                assertEquals("ruleseturi", resource.getUri());
                hasRuleset = true;

            } else if (resource.getType() == SynthesizerProto.EnumResourceType.USER_DICTIONARY) {
                assertEquals("userdictionaryuri", resource.getUri());
                hasUserDictionary = true;

            } else {
                fail();
            }
        }
        assertTrue(hasActivePrompt && hasRuleset && hasUserDictionary);
    }

    @Test
    public void testStartSpeechSynthesis_withResources_error1() throws ConfigurationException {
        // error is: user_dictionary is not an array

        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);

        config.read(new StringReader("{\n" +
                "  \"data\": \"hi\",\n" +
                "  \"speechSynthesisCodec\": \"pcm_16_16k\",\n" +
                "  \"type\": \"text\",\n" +
                "  \"vocsMapping\": {\n" +
                "    \"Evan\": {\n" +
                "      \"language\": \"en-us\",\n" +
                "      \"name\": \"Evan\",\n" +
                "      \"pcm_16_16k\": {\n" +
                "        \"voice_model\": \"enhanced\"\n" +
                "      },\n" +
                "      \"tuneable_data\": [\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-activeprompt-db\",\n" +
                "          \"uri\": \"activeprompturi\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-rettt+text\",\n" +
                "          \"uri\": \"ruleseturi\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"user_dictionary\": {\n" +
                "        \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "        \"uri\": \"userdictionaryuri\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"applicationKey\": \"NvcClientTest\"," +
                "  \"voice\": \"Evan\"\n" +
                "}"));

        testStartSpeechSynthesis_withResources_generic(configs);

        List<SynthesizerProto.SynthesisResource> resources = nvcService.getResources();
        assertEquals(2, resources.size());
    }

    @Test
    public void testStartSpeechSynthesis_withResources_error2() throws ConfigurationException {
        // error is: tuneable_data ruleset content_type is invalid: is 'application/x-vocalizer-rettttext' but should be 'application/x-vocalizer-rettt+text'

        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration config = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(configs);
        config.read(new StringReader("{\n" +
                "  \"data\": \"hi\",\n" +
                "  \"speechSynthesisCodec\": \"pcm_16_16k\",\n" +
                "  \"type\": \"text\",\n" +
                "  \"vocsMapping\": {\n" +
                "    \"Evan\": {\n" +
                "      \"language\": \"en-us\",\n" +
                "      \"name\": \"Evan\",\n" +
                "      \"pcm_16_16k\": {\n" +
                "        \"voice_model\": \"enhanced\"\n" +
                "      },\n" +
                "      \"tuneable_data\": [\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-activeprompt-db\",\n" +
                "          \"uri\": \"activeprompturi\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"content_type\": \"application/x-vocalizer-rettttext\",\n" +
                "          \"uri\": \"ruleseturi\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"user_dictionary\": [\n" +
                "        {\n" +
                "          \"content_type\": \"application/edct-bin-dictionary\",\n" +
                "          \"uri\": \"userdictionaryuri\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"applicationKey\": \"NvcClientTest\"," +
                "  \"voice\": \"Evan\"\n" +
                "}"));

        testStartSpeechSynthesis_withResources_generic(configs);

        List<SynthesizerProto.SynthesisResource> resources = nvcService.getResources();
        assertEquals(2, resources.size());
    }

    private void testStartSpeechSynthesis_withResources_generic(CombinedConfiguration config) {
        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(config), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());
        nvcClient.startSpeechSynthesis();

        verify(mockResponseHandler, atLeastOnce()).onAudio(any(), anyBoolean());
        verifyResponse(ResponseResult.ResponseStatus.SUCCESS);

        //assertTrue(grpcChannel.isShutdown());
    }

    @Test
    public void testStartSpeechSynthesis_statistics_mediaDuration() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        // start session command
        config.addProperty("speechSynthesisCodec", "pcm_16_16k");

        // speech synthesis command
        config.addProperty("type", "ssml");
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");
        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "NvcClientTest");

        // actual test
        config.addProperty(WebApiConfig.Property.STATISTICS.key, true);

        nvcClient = new NvcClient("session-id-test", new NvcRequestParams(configs), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        Session mockSession = mock(Session.class);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);

        Command mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getSessionConfig()).thenReturn(mock(CombinedConfiguration.class));

        Command.CommandStatistics mockCommandStatistics = mock(Command.CommandStatistics.class);
        when(mockCommand.getStatistics()).thenReturn(mockCommandStatistics);

        nvcClient.startSpeechSynthesis();

        ArgumentCaptor<Integer> mediaDurationArgument = ArgumentCaptor.forClass(Integer.class);
        verify(mockCommandStatistics).setMediaDuration(mediaDurationArgument.capture());

        assertEquals(844, (int) mediaDurationArgument.getValue());
    }

    @Test
    public void testSpeex() {
        CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

        // tenant config
        config.addProperty("vocsMapping.Evan.language", "en-us");
        config.addProperty("vocsMapping.Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("vocsMapping.Evan.name", "Evan");

        // start session command
        config.addProperty("speechSynthesisCodec", "speex_wb");

        // speech synthesis command
        config.addProperty("type", "text");
        config.addProperty("data", "hi");
        config.addProperty("voice", "Evan");

        config.addProperty(WebApiConfig.Property.APPLICATION_KEY.key, "NvcClientTest");

        mockResponseHandler = mock(ResponseHandler.class);
        mockErrorHandler = mock(ResponseErrorHandler.class);
        nvcClient = new NvcClient("test", new NvcRequestParams(configs), buildNvcStub(), mockResponseHandler, mockErrorHandler, new HeaderEventListener());

        byte[] pcm = new byte[640];
        Arrays.fill(pcm, (byte) 1);
        nvcClient.sendAudio(pcm);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(mockResponseHandler).onAudio(captor.capture(), anyBoolean());

        assertEquals(70, captor.getValue().length);
    }
}
