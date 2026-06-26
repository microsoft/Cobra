package com.nuance.cobra.utils.audio;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;

import java.io.IOException;
import java.io.InputStream;

public class OpusFrameReader implements IAudioReader {
    private OpusFile opusFile;
    OpusAudioData opusAudioData;

    public OpusFrameReader(InputStream stream) throws IOException {
        opusFile = new OpusFile(new OggFile(stream));
        opusAudioData = opusFile.getNextAudioPacket();
    }

    @Override
    public byte[] getNextFrame() throws IOException {
        byte[] frame = opusAudioData.getData();
        opusAudioData = opusFile.getNextAudioPacket();
        return frame;
    }

    @Override
    public boolean hasNextFrame() {
        return opusAudioData != null;
    }
}
