package com.nuance.cobra.utils.audio;

public interface AudioNodeObserver {
    enum NodeEvent {
        TIMEOUT,
        TIMERS_CANCEL,
        ON_NODE_ENABLED,
        START_OF_SPEECH,
        END_OF_SPEECH,
        NEW_NOISE_LEVEL_CALCULATED,
        CANCEL_DIRECTIVE,
        AUDIO_STATISTICS,
        TIMERS_START
    }

    void onEvent(AudioNode eventOwner, NodeEvent event);
}
