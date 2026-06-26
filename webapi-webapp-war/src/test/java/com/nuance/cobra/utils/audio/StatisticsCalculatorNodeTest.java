package com.nuance.cobra.utils.audio;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class StatisticsCalculatorNodeTest {

    public static StatisticsCalculatorNode createStatisticsCalculatorNode(AudioNodeObserver observer, AudioDestination destination) {
        List<AudioNodeObserver> observers = Collections.singletonList(observer);
        StatisticsCalculatorNode node = new StatisticsCalculatorNode(observers);

        node.connect(destination);

        return node;
    }

    @Test
    public void testOnAudio() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);

        AudioChunk chunk = new AudioChunk(0, new byte[0]);
        node.onAudio(chunk);

        Map<String, Object> stats = node.getStatistics(StatisticsCalculatorNode.ALL_STATISTICS);

        Integer expectedAudioDuration = 20;
        Integer actualAudioDuration = (Integer)stats.get(StatisticsCalculatorNode.STATISTIC_MEDIA_DURATION);

        Assert.assertEquals(expectedAudioDuration, actualAudioDuration);

        verify(mockDestination, times(1)).onAudio(chunk);
    }

    @Test
    public void testOnEvent_null() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);
        node.onEvent(null, null);
    }

    @Test
    public void testOnEvent_timeout() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);
        node.onEvent(null, AudioNodeObserver.NodeEvent.TIMEOUT);

        verify(mockObserver, times(1)).onEvent(node, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);
    }

    @Test
    public void testOnEvent_endOfSpeech() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);
        node.onEvent(null, AudioNodeObserver.NodeEvent.END_OF_SPEECH);

        verify(mockObserver, times(1)).onEvent(node, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);
    }

    @Test
    public void testOnEvent_other() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);
        node.onEvent(null, AudioNodeObserver.NodeEvent.START_OF_SPEECH);
    }

    @Test
    public void testFinish() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);

        node.finish();

        verify(mockObserver, times(1)).onEvent(node, AudioNodeObserver.NodeEvent.AUDIO_STATISTICS);
        verify(mockDestination, times(1)).finish();
    }

    @Test
    public void testGetStatistics() {
        AudioNodeObserver mockObserver = mock(AudioNodeObserver.class);
        AudioDestination mockDestination = mock(AudioDestination.class);

        StatisticsCalculatorNode node = createStatisticsCalculatorNode(mockObserver, mockDestination);

        List<String> desiredStatistics = new ArrayList<>();
        Map<String, Object> stats = node.getStatistics(desiredStatistics);

        Assert.assertTrue(stats.isEmpty());
    }
}
