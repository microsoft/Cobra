package com.nuance.cobra.utils.audio;

public interface AudioDestination
{
    void onAudio(AudioChunk chunk);
    void finish();
}
