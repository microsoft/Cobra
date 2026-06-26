package com.nuance.ncs.nmsp.engine.pdx;

import java.io.IOException;

/** Replaces NMSPAudioSink */
public interface PdxAudioSink {
    void addAudioBuf(byte[] buf, int offset, int length, boolean isLastBuf) throws IOException;
}
