package com.nuance.cobra.utils.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

public class WavFrameReader implements IAudioReader {
    AudioInputStream audioInputStream;
    int bytesPerFrame;

    public WavFrameReader(InputStream input) throws IOException {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(input);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
        bytesPerFrame =
                audioInputStream.getFormat().getSampleSizeInBits();
        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
        }
    }

    @Override
    public byte[] getNextFrame() throws IOException {
        byte[] audioBytes = new byte[bytesPerFrame];
        int z = audioInputStream.read(audioBytes);
        if (z != bytesPerFrame) throw new IOException("Frame size does not match stream size.");
        return audioBytes;
    }

    @Override
    public boolean hasNextFrame() {
        try {
            return audioInputStream.available() != 0;
        } catch (IOException e) {
            return false;
        }
    }
}
