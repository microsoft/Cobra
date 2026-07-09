package com.nuance.cobra.utils.audio;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.junit.Assert.*;

public class AdaptiveSpeechDetectorNodeTest {
    private AdaptiveSpeechDetectorNode testNode;

    @Before
    public void setUp(){
        testNode = new AdaptiveSpeechDetectorNode(2, 2, 3,
                0, 5, 2.0,
                new NoiseLevel(2.0, 0.23), new ArrayList<>());

    }

    @Test
    public void nullNoiseCondition() {
        AdaptiveSpeechDetectorNode node = new AdaptiveSpeechDetectorNode(1, 2, 3,
                4, 5, 2.0,
                null, new ArrayList<>());
    }

    @Test
    public void createTupleTest(){
        assertFalse(testNode.isVoiced(testNode.createTuple(0.2)));
    }

    @Test
    // TODO: Some conditions left to cover
    public void onAudioTest(){
        byte [] bytes = {0, 20, 1, 2, 3, 4};
        AudioChunk chunk = new AudioChunk(10, bytes);
        assertNotNull(chunk);
        testNode.onAudio(chunk);

        Whitebox.setInternalState(testNode, "beginDelayFrames", 0);
        testNode.onAudio(chunk);


        Whitebox.setInternalState(testNode, "frameCount", 1);
        testNode.onAudio(chunk);

        Whitebox.setInternalState(testNode, "thresholdRatio", 0.0);
        testNode.onAudio(chunk);

        Whitebox.setInternalState(testNode, "lowSequenceAverage", 200.0);
        testNode.onAudio(chunk);

        Whitebox.setInternalState(testNode, "endOfSpeechHistoryFrameCount", 2);
        testNode.onAudio(chunk);

        testNode.state = SpeechDetector.State.DURING;
        testNode.onAudio(chunk);

        Whitebox.setInternalState(testNode, "endOfSpeechHistoryFrameCount", 3);
        testNode.onAudio(chunk);
    }

    class TestAudioNodeObserver implements AudioNodeObserver {
        long currentChunk = 0;
        HashMap<String, Long> eventsMap = new HashMap<>();
        public void setChunk(long chunk) {
            currentChunk = chunk;
        }

        @Override
        public void onEvent(AudioNode eventOwner, NodeEvent event) {
            eventsMap.put(event.name(), currentChunk);

            // For speech detection debugging uncomment this
//            System.out.println(event + " " + currentChunk);
        }

        public HashMap<String, Long> getEventsMap() {
            return eventsMap;
        }
    }

    @Test
    public void testSpeechEventsDetection() {
        TestAudioNodeObserver observer = new TestAudioNodeObserver();

        AdaptiveSpeechDetectorNode node = new AdaptiveSpeechDetectorNode(10, 5, 15,
                5, 50, 4.0,
                null, new ArrayList<>());
        node.addObserver(Collections.singletonList(observer));

        for (AudioChunk chunk: buildAudioChunkList(
                "10|S", //frames to calculate env noise level
                "10|S", //silence frames
                "10|V", //voiced frames to trigger the startSpeechVoicedFrames

                /// voice has been triggered

                "10|S", //40
                "5|V", //
                "10|S",
                "40|V", //95
                "20|S",
                "40|V", //155
                "30|S",
                "40|V", //225
                "40|S",
                "40|V", //305
                "50|S",
                "40|V", //395
                "60|S",
                "40|V", //495
                "70|S",
                "40|V", //605
                "80|S",
                "40|V", //725
                "90|S",
                "40|V", //855
                "100|S",
                "40|V", //995
                "110|S",
                "40|V", //1145
                "120|S",
                "40|V", //1305
                "40|S",
                "1000|S"

                //110 + 100 + 240 = 460
        )) {
            observer.setChunk(chunk.getOffset());
            node.onAudio(chunk);
        }

        HashMap<String, Long> eventsMap =  observer.getEventsMap();

        assertEquals(352, (long)eventsMap.get("END_OF_SPEECH"));
        assertEquals(26, (long)eventsMap.get("START_OF_SPEECH"));
    }

    private List<AudioChunk> buildAudioChunkList(String ... noise) {
        final int frameSIzeInBytes = 32;
        byte [] SilenceFrameBytes = generateWave(frameSIzeInBytes, 5);
        byte [] VoiceFrameBytes = generateWave(frameSIzeInBytes, 120);
        int offset = 0;
        List<AudioChunk> result = new LinkedList<>();
        for (String level: noise) {
            String[] split = level.split("\\|");
            Integer count = Integer.parseInt(split[0]);

            for (int i = 0; i < count; ++i) {
                switch (split[1]) {
                    case "V":
                        result.add(new AudioChunk(offset, addNoise(VoiceFrameBytes)));
                        break;
                    case "S":
                    default:
                        result.add(new AudioChunk(offset, addNoise(SilenceFrameBytes)));
                        break;
                }
                offset++;
            }
        }

        return result;
    }

    private byte[] generateWave(int size, int level) {
        final int sinPeriod = size/2;

        double anglePerI = (2 * Math.PI)/sinPeriod;
        byte[] result = new byte[size];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte)(Math.sin(i* anglePerI)*level);
        }

        return result;
    }

    private byte[] addNoise(byte [] bytes) {
        final int NOISE_LEVEL = 5;
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            result[i] = (byte)(bytes[i] + ((Math.random()*NOISE_LEVEL) - (NOISE_LEVEL/2)));
        }

        return result;
    }
}