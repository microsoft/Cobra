package com.nuance.cobra.utils.audio.encoders;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class Mp3EncoderTest {

    @Test
    public void encode() {
        AudioFormat inAudioFormat = new AudioFormat(22050, 16, 1, false, true);
        Mp3Encoder mp3Encoder = new Mp3Encoder(inAudioFormat, 128, 1, false);

        byte[] encoded = mp3Encoder.encode("test".getBytes(StandardCharsets.UTF_8));
        assertTrue(encoded.length > 0);

        mp3Encoder.onDone();
    }
}