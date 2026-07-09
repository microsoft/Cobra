package com.nuance.plugin.driver_v2.Endpointers;

import java.nio.ByteBuffer;

public interface IEndpointer {
    public int processAudio(ByteBuffer audiobuf, int num_samples);
    public int processAudioAdpcm(ByteBuffer audiobuf, int num_samples, int high_nibble_first);
    public IVADResult getVADResult();
    public int getMaxBufferSamples();
    public int processAudioByteArray(byte[] audiobuf, int offset, int len);
    public int getFrameSamples();
    public int getFrameBytes();
    public int getOpusEncodedBytes(byte[] out, int pos, int maxlen);
    public boolean supportsOpus();
    public void setHighNibbleFirst();
    public void setLowNibbleFirst();

}
