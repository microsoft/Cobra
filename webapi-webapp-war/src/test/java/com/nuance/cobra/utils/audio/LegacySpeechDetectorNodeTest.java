package com.nuance.cobra.utils.audio;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static com.nuance.cobra.utils.audio.SpeechDetector.State.DURING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class LegacySpeechDetectorNodeTest  {
    private LegacySpeechDetectorNode testLegacySpeechDetectorNode;

    @Test
    public void onAudioTest(){
        int reuseInt = 0;
        double reuseDouble = 0.0;
        boolean testBool = true;
        NoiseLevel mockNoise = new NoiseLevel(0.0, 0.0);
        List<AudioNodeObserver> testList = new ArrayList<>();
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);

        testList.add(mockObserver);

        testLegacySpeechDetectorNode = new LegacySpeechDetectorNode(reuseInt, reuseInt,
                        reuseInt, reuseInt, reuseInt, reuseDouble, testBool, mockNoise, testList);


        assertEquals(mockNoise, testLegacySpeechDetectorNode.getProvidedBackgroundNoiseLevel());
        assertEquals(new NoiseLevel(0.0,0.0).getMean(),
                testLegacySpeechDetectorNode.getCalculatedBackgroundNoiseLevel().getMean(), 0);


        byte[] bytes = {(byte)0xaa};

        AudioChunk testAudio = new AudioChunk(0, bytes);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "beginDelayCount", 2);
        Whitebox.setInternalState(testLegacySpeechDetectorNode, "backgroundNoiseFrames", 1 );
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "backgroundNoiseFrames", 0 );
        Whitebox.setInternalState(testLegacySpeechDetectorNode, "thresholdRatio", -1.0);

        LegacySpeechDetectorNode.Voiced[] voicedArr = new LegacySpeechDetectorNode.Voiced[1];
        LegacySpeechDetectorNode.Voiced a = LegacySpeechDetectorNode.Voiced.POSITIVE;
        voicedArr[0] = a;

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "voicedHistory", voicedArr);
        Whitebox.setInternalState(testLegacySpeechDetectorNode, "endOfSpeechHistoryFrameCount", 1);
        Whitebox.setInternalState(testLegacySpeechDetectorNode, "startOfSpeechHistoryFrameCount", 1);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "state", DURING);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "counter", -1);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "counter", 2);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "endOfSpeechHistoryFrameCount", Integer.MAX_VALUE);
        Whitebox.setInternalState(testLegacySpeechDetectorNode, "startOfSpeechHistoryFrameCount", Integer.MAX_VALUE);
        testLegacySpeechDetectorNode.onAudio(testAudio);

        Whitebox.setInternalState(testLegacySpeechDetectorNode, "considerNegativeRatios", false );
        testLegacySpeechDetectorNode.onAudio(testAudio);
    }
}