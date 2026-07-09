package com.nuance.cobra.utils.audio;

public abstract class SpeechDetector extends AudioNode {

    public enum State
    {
        BEFORE,
        DURING,
        AFTER
    }

    protected State state = State.BEFORE;

    public State getState() {
        return state;
    }

    protected void notifySpeechStart() {
        notifyObservers(NodeEvent.START_OF_SPEECH);
        cancelTimers(); //cancel any timer that is dependent on this node
    }

    protected void notifyInputStart() {
        notifyObservers(NodeEvent.ON_NODE_ENABLED);
    }

    protected void notifySpeechEnd() {
        notifyObservers(NodeEvent.END_OF_SPEECH);
    }

    protected void notifyNoiseLevel() {
        notifyObservers(NodeEvent.NEW_NOISE_LEVEL_CALCULATED);
    }

    protected void cancelTimers() {
        notifyObservers(NodeEvent.TIMERS_CANCEL);
    }

    public abstract NoiseLevel getCalculatedBackgroundNoiseLevel();
}
