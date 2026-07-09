package com.nuance.cobra.utils.audio;

import java.util.ArrayList;
import java.util.List;

public class DuplicatorNode extends AudioNode implements AudioDestination
{
    private final List<AudioDestination> destinations = new ArrayList<>();

    @Override
    public void connect(AudioDestination destination)
    {
        synchronized (this)
        {
            if (!destinations.contains(destination))
            {
                destinations.add(destination);
            }
        }
    }

    @Override
    public void dispatchChunk(AudioChunk audioChunk)
    {
        synchronized (this)
        {
            for (final AudioDestination destination : destinations)
            {
                destination.onAudio(audioChunk);
            }
        }
    }

    @Override
    public void dispatchFinish()
    {
        synchronized (this)
        {
            for (final AudioDestination destination : destinations)
            {
                destination.finish();
            }
        }
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {

    }
}
