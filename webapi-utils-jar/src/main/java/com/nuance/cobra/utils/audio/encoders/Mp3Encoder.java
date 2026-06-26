package com.nuance.cobra.utils.audio.encoders;

import de.sciss.jump3r.lowlevel.LameEncoder;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

public class Mp3Encoder implements IAudioEncoder {
    private final LameEncoder mp3Encoder;

    public Mp3Encoder(AudioFormat sourceFormat, int bitRate, int channels, boolean variableBitRate) {
        int channelMode = channels > 1 ? LameEncoder.CHANNEL_MODE_DUAL_CHANNEL : LameEncoder.CHANNEL_MODE_MONO;
        mp3Encoder = new LameEncoder(sourceFormat, bitRate, channelMode, LameEncoder.QUALITY_HIGHEST, variableBitRate);
    }

    @Override
    public byte[] encode(byte[] pcmInput) {
        byte[] outputBuffer = new byte[Math.max(pcmInput.length, mp3Encoder.getMP3BufferSize())];

        int bytesWritten = mp3Encoder.encodeBuffer(pcmInput, 0, pcmInput.length, outputBuffer);

        if (bytesWritten > 0) {
            return Arrays.copyOfRange(outputBuffer, 0, bytesWritten);
        }
        return new byte[0];
    }

    @Override
    public void onDone() {
        mp3Encoder.close();
    }
}
