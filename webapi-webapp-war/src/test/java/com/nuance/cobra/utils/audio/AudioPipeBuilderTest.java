package com.nuance.cobra.utils.audio;

import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AudioPipeBuilderTest {
    private final CombinedConfiguration mockConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private final Configuration mockConfig = CobraConfigurationUtils.getHighestConfig(mockConfigs);
    private final Session mockSession = mock(Session.class);

    @Before
    public void setUp() throws Exception {
        ResponseHandler responseHandlerMock = mock(ResponseHandler.class);
        Command mockCommand = mock(Command.class);
        when(mockSession.getCommand()).thenReturn(mockCommand);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key,"pcm_16_16k");
        mockConfig.setProperty(WebApiConfig.Property.WAV_ENABLED.key, false);
        mockConfig.setProperty(WebApiConfig.Property.EDF_POSTING_ENABLED_TENANT.key,false);
        mockConfig.setProperty(WebApiConfig.Property.AUDIO_CAPTURE.key, 0);
        mockConfig.setProperty(WebApiConfig.Property.DEBUG_ENABLED.key, false);
        mockConfig.setProperty(WebApiConfig.Property.BEGIN_NOISE_SAMPLE_FRAMES.key, 100);
        mockConfig.setProperty( WebApiConfig.Property.START_OF_SPEECH_FRAMES.key,
               100);
        mockConfig.setProperty(WebApiConfig.Property.START_OF_SPEECH_HISTORY_FRAMES.key, 100);
        mockConfig.setProperty(
                 WebApiConfig.Property.END_OF_SPEECH_FRAMES.key,
                100);
        mockConfig.setProperty(WebApiConfig.Property.END_OF_SPEECH_HISTORY_FRAMES.key, 100);
        mockConfig.setProperty(WebApiConfig.Property.VOICE_THRESHOLD.key, 100.0);
        mockConfig.setProperty(WebApiConfig.Property.CONSIDER_NEGATIVE_RATIOS.key, false);
        mockConfig.setProperty(WebApiConfig.Property.NOISE_LEVEL.key, null);
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, "adaptive");

        when(mockSession.getSessionConfig()).thenReturn(mockConfigs);


        when(mockCommand.getResponseHandler()).thenReturn(responseHandlerMock);
    }

    @Test
    public void buildWavDumperNode() {
        mockConfig.setProperty(WebApiConfig.Property.WAV_ENABLED.key, true);
        mockConfig.setProperty(WebApiConfig.Property.AUDIO_CAPTURE.key, 100);
        mockConfig.setProperty(WebApiConfig.Property.DEBUG_ENABLED.key, true);
        mockConfig.setProperty(WebApiConfig.Property.DEBUG_PATH.key, "/usr/local/Nuance/webapi/");

        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.buildWavDumperNode(new ArrayList<>()) instanceof WavDumperNode);
    }

    @Test
    public void buildTimeoutNode() {
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.buildTimeoutNode(10, new ArrayList<>(), true) instanceof TimeoutNode);
    }

    @Test
    public void buildSpeechDetectorNode() {
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, "adaptive");
        assertTrue(pipeBuilder.buildSpeechDetectorNode(new ArrayList<>()) instanceof AdaptiveSpeechDetectorNode);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_DETECTOR.key, "bla");
        assertTrue(pipeBuilder.buildSpeechDetectorNode(new ArrayList<>()) instanceof LegacySpeechDetectorNode);
    }

    @Test
    public void buildStatisticsCalculatorNode() {
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.buildStatisticsCalculatorNode(new ArrayList<>()) instanceof StatisticsCalculatorNode);
    }

    @Test
    public void appendNode() {
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        pipeBuilder.appendNode(pipeBuilder.buildSpeechDetectorNode(new ArrayList<>()));
    }

    @Test
    public void appendDestination() {
    }

    @Test
    public void getPipe() {
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        pipeBuilder.appendNode(pipeBuilder.buildSpeechDetectorNode(new ArrayList<>()));
        pipeBuilder.getPipe();
    }

    @Test
    public void buildPipe() {
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "pcm_16_8k");
        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof PcmChunkerNode);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "pcm_16_16k");
        pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof PcmChunkerNode);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "ulaw");
        pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof LawChunkerNode);

        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "opus_wb");
        pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof OpusChunkerNode);
    }

    @Test
    public void buildPipeSpeexWb() {
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "speex_wb");

        mockConfig.setProperty("asrDecoder.speex_wb.mode", 1);
        mockConfig.setProperty("asrDecoder.speex_wb.sampleRate", 16000);
        mockConfig.setProperty("asrDecoder.speex_wb.quality", 8);
        mockConfig.setProperty("asrDecoder.speex_wb.nbAudioChannels", 1);
        mockConfig.setProperty("asrDecoder.speex_wb.perceptualEnhancement", true);

        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof PcmChunkerNode);
    }

    @Test
    public void buildPipeSpeexNb() {
        mockConfig.setProperty(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key, "speex_nb");

        mockConfig.setProperty("asrDecoder.speex_wb.mode", 0);
        mockConfig.setProperty("asrDecoder.speex_wb.sampleRate", 8000);
        mockConfig.setProperty("asrDecoder.speex_wb.quality", 6);
        mockConfig.setProperty("asrDecoder.speex_wb.nbAudioChannels", 1);
        mockConfig.setProperty("asrDecoder.speex_wb.perceptualEnhancement", true);

        AudioPipeBuilder pipeBuilder = new AudioPipeBuilder(mockConfigs, mockSession);
        assertTrue(pipeBuilder.getPipe() instanceof PcmChunkerNode);
    }
}