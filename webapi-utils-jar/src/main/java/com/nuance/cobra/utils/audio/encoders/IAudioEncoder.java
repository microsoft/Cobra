package com.nuance.cobra.utils.audio.encoders;

public interface IAudioEncoder {
    byte[] encode(byte[] pcmInput);
    void onDone();
}
