package com.nuance.plugin.driver_v2.Audio;

public interface AudioChopper
{
    public byte[] getNextFrame();
    public byte[] getXFrame(int numFrames);
    public byte[] getNextChunk(int maxSize);
    public boolean hasMoreFrame();
    public int getFrameLengthInMs();
}
