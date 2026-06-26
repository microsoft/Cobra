package com.nuance.cobra.utils.audio;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// annotations copied from com.nuance.cobra.command.CommandTest
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApiConfigManager.class})
@PowerMockIgnore({"org.xml.*", "jdk.xml.*", "java.xml.*", "javax.xml.*", "com.sun.org.apache.xerces.*", "javax.management.*", "org.w3c.dom.*"})
public class RawSpeexDecoderNodeTest
{
    private AudioChunkerNode audioPipe;

    private static final String INPUT_FILE_PREFIX = "audio/";
    private static final String OUTPUT_FILE_PREFIX = "src/test/resources/audio/";

    private String outputFilePath;

    private static CombinedConfiguration configs;
    private static Configuration config;

    @Before
    public void setUp()
    {
        configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        config = CobraConfigurationUtils.getHighestConfig(configs);

        config.addProperty("debugEnabled", false);
        config.addProperty("debugAudioCapture", 100);

        // speech recognition command
        config.addProperty("speechRecognitionCodec", "speex_16k");
    }

    @Test
    /**
     * Reads speex audio from a file, sends the audio data through our audio pipe (which uses a SpeexDecoderNode), and outputs the decoded audio to raw PCM.
     */
    public void testSpeexDecoderNode() throws IOException
    {
        ArrayList<String> speexFiles = new ArrayList<>();
//        speexFiles.add("audio_16k16bitSpeexWithUtility.spx");
//        speexFiles.add("NUAN-f019965a-491e-4d23-b20f-f0cc0c21d318-SESSION-a8988ede-c9bc-47f0-94f1-33043930f838-SPEEX_WB_NR8-audio_1.wspx8");
//        speexFiles.add("show_me_latest_labs.speex");
//        speexFiles.add("tts_generated_audio_2eng-USA_Ava.spx");
        speexFiles.add("tts_generated_audio_eng-USA_Ava.spx");
//        speexFiles.add("tts_generated_audio_speex_Hieng-USA_Ava.spx");

        for (String speexFileName : speexFiles) {
            buildAudioPipe();
            outputFilePath = OUTPUT_FILE_PREFIX + speexFileName + ".pcm";

            try (InputStream speexFile = getClass().getClassLoader().getResourceAsStream(INPUT_FILE_PREFIX + speexFileName)) {
                ByteBuffer bytes = ByteBuffer.wrap(speexFile.readAllBytes());
                byte[] frameByte = new byte[139];
                while (bytes.remaining() >= frameByte.length) {
                    bytes.get(frameByte);
                    audioPipe.onAudio(frameByte);
                }
                audioPipe.finish();
            }

            assertEquals(35840, Files.readAllBytes(Paths.get(outputFilePath)).length);

            //Comment to save pcm output
            FileUtils.deleteQuietly(new File(outputFilePath));
        }

        String speexChunksFileNamePrefix = "chunk";
        int numberOfChunks = 22;

        buildAudioPipe();
        outputFilePath = OUTPUT_FILE_PREFIX + speexChunksFileNamePrefix + ".pcm";

        for (int i = 1; i <= numberOfChunks; i++) {
            String speexChunkFileName = speexChunksFileNamePrefix + i + ".dat";
            try (InputStream speexChunkFile = getClass().getClassLoader().getResourceAsStream(INPUT_FILE_PREFIX + speexChunkFileName)) {
                audioPipe.onAudio(speexChunkFile.readAllBytes());
            }
        }
        audioPipe.finish();

        Assert.assertArrayEquals("Binary files differ",
                Files.readAllBytes(Paths.get(outputFilePath + ".canonical")),
                Files.readAllBytes(Paths.get(outputFilePath)));

        //Comment to save pcm output
        FileUtils.deleteQuietly(new File(outputFilePath));
    }

    @Test
    public void testSpeexDecoderNodeLargeChunks() throws IOException
    {
        buildAudioPipe();

        String inputFile = "./src/test/resources/audio/NUAN-f019965a-491e-4d23-b20f-f0cc0c21d318-SESSION-a8988ede-c9bc-47f0-94f1-33043930f838-SPEEX_WB_NR8-audio_1.wspx8";
        outputFilePath = inputFile + ".pcm";

        //NUAN-...wspx8 already has the NMT size headers
        byte[] fileContent = Files.readAllBytes(Paths.get(inputFile));
        byte[] rawSpeex = RawSpeexDecoderNode.removeNmspNmtSizeHeaders(fileContent);

        ByteBuffer bytes = ByteBuffer.wrap(rawSpeex);
        byte[] frameByte = new byte[106];
        while (bytes.remaining() >= frameByte.length) {
            bytes.get(frameByte);
            audioPipe.onAudio(frameByte);
        }
        audioPipe.finish();

        Assert.assertArrayEquals("Binary files differ",
                Files.readAllBytes(Paths.get(outputFilePath + ".canonical")),
                Files.readAllBytes(Paths.get(outputFilePath)));

        //Comment to save pcm output
        FileUtils.deleteQuietly(new File(outputFilePath));
    }

    private void buildAudioPipe() {
        // prepare mocks
        Session mockSession = mock(Session.class);

        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        when(mockSession.getResponseHandler()).thenReturn(mockResponseHandler);
        when(mockSession.getSessionConfig()).thenReturn(configs);

        PowerMockito.mockStatic(WebApiConfigManager.class);
        when(WebApiConfigManager.getInstance()).thenReturn(configs);
        // end : prepare mocks

        // build audio pipe
        AudioPipeBuilder audioPipeBuilder = new AudioPipeBuilder(configs, mockSession); // will use SpeexDecoderNode

//        AudioNode wavDumperNode = audioPipeBuilder.buildWavDumperNode("rawpcm", false, new ArrayList<>()); // TODO pass observers?
//        audioPipeBuilder.appendNode(wavDumperNode);

        /* writes bytes it receives to a file */
        AudioChunkerNode fileWriterNode = new AudioChunkerNode() {
            ByteArrayOutputStream pcmAudio = new ByteArrayOutputStream();

            @Override
            public void onAudio(AudioChunk pcmAudioChunk) {
                try {
                    pcmAudio.write(pcmAudioChunk.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAudio(byte[] bytes) {
                // nothing
            }

            @Override
            public void onEvent(AudioNode eventOwner, NodeEvent event) {
                // nothing
            }

            @Override
            public void finish() {
                super.finish();

                try (OutputStream outputFile = new FileOutputStream(outputFilePath)) {
                    pcmAudio.writeTo(outputFile); // uncomment to write to disk
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        audioPipeBuilder.appendNode(fileWriterNode);

        audioPipe = audioPipeBuilder.getPipe();
        // end : build audio pipe

//        try (InputStream speexAudioFile = getClass().getClassLoader().getResourceAsStream(speexAudioFilePath)) {
//            audioPipe.onAudio(speexAudioFile.readAllBytes());
//            audioPipe.finish(); // output wav file is written to disk at ~ [if on windows add:'C:']/usr/local/Nuance/webapi/data/eventLogs/null/null/2020/08August/12/17/raw.wav
//        }
    }

    @Test(expected = Test.None.class)
    public void testConstructor_invalid() {
        new RawSpeexDecoderNode(-1, 16000, 8, 1, true);
    }

    @Test(expected = Test.None.class)
    public void testConstructor_mode0() {
        new RawSpeexDecoderNode(0, 8000, 8, 1, true);
    }

    @Test(expected = Test.None.class)
    public void testConstructor_mode1() {
        new RawSpeexDecoderNode(1, 16000, 8, 1, true);
    }

    @Test(expected = Test.None.class)
    public void testConstructor_mode2() {
        new RawSpeexDecoderNode(2, 32000, 8, 1, true);
    }

    @SuppressWarnings("java:S5961")
    @Test
    public void getNFrames()
    {
        RawSpeexDecoderNode rawSpeexDecoderNode = new RawSpeexDecoderNode(0, 8000, -1, 1, true);
        //Mode 0. Quality 10
        assertEquals(1, rawSpeexDecoderNode.getNFrames(62));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(123));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(185));

        //Mode 0. Quality 9
        assertEquals(1, rawSpeexDecoderNode.getNFrames(46));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(91));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(137));

        //Mode 0. Quality 8 & 7
        assertEquals(1, rawSpeexDecoderNode.getNFrames(38));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(75));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(113));

        //Mode 0. Quality 6 & 5
        assertEquals(1, rawSpeexDecoderNode.getNFrames(28));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(55));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(83));

        rawSpeexDecoderNode = new RawSpeexDecoderNode(1, 16000, -1, 1, true);
        //Mode 1. Quality 10
        assertEquals(1, rawSpeexDecoderNode.getNFrames(106));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(212));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(318));

        //Mode 1. Quality 9
        assertEquals(1, rawSpeexDecoderNode.getNFrames(86));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(172));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(258));

        //Mode 1. Quality 8
        assertEquals(1, rawSpeexDecoderNode.getNFrames(70));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(139));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(209));
        assertEquals(5, rawSpeexDecoderNode.getNFrames(348));
        assertEquals(8, rawSpeexDecoderNode.getNFrames(556));
        assertEquals(9, rawSpeexDecoderNode.getNFrames(626));
        assertEquals(10, rawSpeexDecoderNode.getNFrames(695));

        //Mode 1. Quality 7
        assertEquals(1, rawSpeexDecoderNode.getNFrames(60));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(119));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(179));

        //Mode 1. Quality 6
        assertEquals(1, rawSpeexDecoderNode.getNFrames(52));
        assertEquals(2, rawSpeexDecoderNode.getNFrames(103));
        assertEquals(3, rawSpeexDecoderNode.getNFrames(155));

        //Quality undetected: Return 1
        assertEquals(1, rawSpeexDecoderNode.getNFrames(59));
    }
}
