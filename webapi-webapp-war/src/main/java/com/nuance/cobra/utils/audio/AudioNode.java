package com.nuance.cobra.utils.audio;

import java.util.ArrayList;
import java.util.List;

public abstract class AudioNode implements AudioDestination, AudioNodeObserver
{
    List<AudioNodeObserver> observers = new ArrayList<>();

    public void addObserver(List<AudioNodeObserver> observer) {
        this.observers.addAll(observer);
    }

    void notifyObservers(NodeEvent event) {
        for (AudioNodeObserver obs: observers) {
            if (obs != null) { //ignore null observers
                obs.onEvent(this, event);
            }
        }
    }

    private AudioDestination destination;

    public void connect(AudioDestination destination)
    {
        synchronized (this)
        {
            this.destination = destination;
        }
    }

    public void onAudio(AudioChunk audioChunk)
    {
        dispatchChunk(audioChunk);
    }

    public void finish()
    {
        dispatchFinish();
    }

    public void dispatchChunk(AudioChunk audioChunk)
    {
        synchronized (this)
        {
            if (null != destination)
            {
                destination.onAudio(audioChunk);
            }
        }
    }

    public void dispatchFinish()
    {
        synchronized (this)
        {
            if (null != destination)
            {
                destination.finish();
            }
        }
    }
}
