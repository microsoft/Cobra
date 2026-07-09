package com.nuance.cobra.utils.audio;

public abstract class AudioChunkerNode extends AudioNode
{
    abstract public void onAudio(byte[] bytes);
}
