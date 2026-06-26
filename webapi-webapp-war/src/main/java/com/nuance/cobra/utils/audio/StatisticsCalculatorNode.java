package com.nuance.cobra.utils.audio;

import java.util.*;

public class StatisticsCalculatorNode extends AudioNode {
    // constants
    public static final String STATISTIC_MEDIA_DURATION = "mediaDuration";
    public static final String STATISTIC_MEDIA_SIZE = "mediaSize";
    public static final List<String> ALL_STATISTICS = Collections.unmodifiableList(Arrays.asList(STATISTIC_MEDIA_DURATION, STATISTIC_MEDIA_SIZE));

    // statistics
    //   non-computed
    private int totalNumberOfSamples = 0;
    //   computed
    private Integer mediaDuration = 0; // millis
    private Integer size = 0; // bytes

    public StatisticsCalculatorNode(List<AudioNodeObserver> observers) {
        addObserver(observers);
    }

    private void computeStatistics() {
        computeMediaDuration();
    }

    private void computeMediaDuration() {
        mediaDuration = totalNumberOfSamples * 20; // 20 ms per sample, '* 20 ' is the same as '/ 0.05'
    }

    @Override
    public void onAudio(AudioChunk audioChunk) {
        totalNumberOfSamples++;
        size += audioChunk.getBytes().length;

        computeStatistics();

        super.onAudio(audioChunk);
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        if (event == NodeEvent.TIMEOUT || event == NodeEvent.END_OF_SPEECH) {
            notifyObservers(NodeEvent.AUDIO_STATISTICS);
        }
    }

    @Override
    public void finish() {
        notifyObservers(NodeEvent.AUDIO_STATISTICS);

        super.finish();
    }

    public Map<String, Object> getStatistics(List<String> desiredStatistics) {
        Map<String, Object> statistics = new HashMap<>();

        if (desiredStatistics.contains(STATISTIC_MEDIA_DURATION)) {
            statistics.put(STATISTIC_MEDIA_DURATION, mediaDuration);
        }
        if (desiredStatistics.contains(STATISTIC_MEDIA_SIZE)) {
            statistics.put(STATISTIC_MEDIA_SIZE, size);
        }
        return statistics;
    }
}
