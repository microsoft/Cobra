package com.nuance.cobra.utils.audio;


import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeoutNode extends AudioNode implements AudioDestination
{
    private final static String DEFAULT_ID = "DEFAULT_ID";

    private final long timeoutSampleCount;
    private long firstPacketOffset = -1;
    private boolean fireEvent = true;
    private final String timerID;

    /**
     * @param timeoutMs timeout value in milliseconds
     * @param sampleRate the recording sample rate
     * @param observer the observer
     */
    public TimeoutNode(long timeoutMs, int sampleRate,  List<AudioNodeObserver> observer, boolean startRecognitionTimers)
    {
        this(timeoutMs, sampleRate, DEFAULT_ID, observer, startRecognitionTimers);
    }

    /**
     * @param timeoutMs timeout value in milliseconds
     * @param sampleRate the recording sample rate
     * @param observer the observer
     */
    public TimeoutNode(long timeoutMs, int sampleRate, String timerID, List<AudioNodeObserver> observer, boolean startRecognitionTimers)
    {
        fireEvent = startRecognitionTimers;

        // Multiply first to prevent data loss
        timeoutSampleCount = TimeUnit.MILLISECONDS.toSeconds(timeoutMs * sampleRate);
        addObserver(observer);

        this.timerID = timerID;
    }

    public String getTimerID () {
        return timerID;
    }

    public void cancel()
    {
        fireEvent = false;
    }

    public void start() { fireEvent = true; }

    @Override
    public void onAudio(AudioChunk chunk)
    {
        if(firstPacketOffset == -1 && fireEvent)
        {
            firstPacketOffset = chunk.getOffset();
        }
        dispatchChunk(chunk);

        if (fireEvent && chunk.getOffset() > (timeoutSampleCount + firstPacketOffset))
        {
            fireEvent = false;
            notifyObservers(NodeEvent.TIMEOUT);
        }
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        if (event == NodeEvent.TIMERS_CANCEL)
        {
            cancel();
        } else if (event == NodeEvent.TIMERS_START)
        {
            start();
        }
    }
}
