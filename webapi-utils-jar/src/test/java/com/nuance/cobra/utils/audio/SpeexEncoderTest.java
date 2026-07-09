package com.nuance.cobra.utils.audio;

import com.nuance.cobra.utils.audio.encoders.SpeexEncoder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SpeexEncoderTest {

    private SpeexEncoder speexEncoder;

    @Before
    public void runsBeforeEachTest() throws Exception {
        speexEncoder = new SpeexEncoder(SpeexEncoder.WIDEBAND, 8, 16000);
    }

    @Test
    public void testPcmToSpeex() {
        byte[] pcm = new byte[641];
        Arrays.fill(pcm, (byte) 1);

        byte[] speex = speexEncoder.encode(pcm);

        assertEquals(70, speex.length);
    }
}
