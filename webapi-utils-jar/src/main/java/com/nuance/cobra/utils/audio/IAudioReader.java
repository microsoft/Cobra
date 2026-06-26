package com.nuance.cobra.utils.audio;

import java.io.IOException;

public interface IAudioReader {
    byte[] getNextFrame() throws IOException;
    boolean hasNextFrame();
}
