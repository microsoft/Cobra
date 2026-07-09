package com.nuance.cobra.utils.audio;

import org.hamcrest.core.IsEqual;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class AudioCodecTest
{
    private AudioCodec audioCodec;

    @Before
    public void setUp()
    {

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_fromString()
    {
        audioCodec = AudioCodec.fromString("Pcm_16_22k");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_22K));
        audioCodec = AudioCodec.fromString("audio/l16;rate=22050");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_22K));
        audioCodec = AudioCodec.fromString("audio/x-raw;format=s16le;rate=22050");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_22K));

        audioCodec = AudioCodec.fromString("Pcm_16_16k");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_16K));
        audioCodec = AudioCodec.fromString("audio/l16;rate=16000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_16K));
        audioCodec = AudioCodec.fromString("audio/x-raw;format=s16le;rate=16000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_16K));

        audioCodec = AudioCodec.fromString("Pcm_16_8k");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_8K));
        audioCodec = AudioCodec.fromString("audio/l16;rate=8000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_8K));
        audioCodec = AudioCodec.fromString("audio/x-raw;format=s16le;rate=8000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.PCM_16_8K));

        audioCodec = AudioCodec.fromString("opus_wb");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.OPUS_16K));

        audioCodec = AudioCodec.fromString("opus_nb");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.OPUS_8K));

        audioCodec = AudioCodec.fromString("alaw");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.ALAW));
        audioCodec = AudioCodec.fromString("audio/x-alaw-basic;rate=8000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.ALAW));

        audioCodec = AudioCodec.fromString("ulaw");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.ULAW));
        audioCodec = AudioCodec.fromString("audio/basic;rate=8000");
        MatcherAssert.assertThat(audioCodec, IsEqual.equalTo(AudioCodec.ULAW));

        audioCodec = AudioCodec.fromString("Pcm_16_22k");
        assertEquals(audioCodec.getEncoding(), AudioCodec.Encoding.PCM);
        assertEquals(audioCodec.getSampleRate(), 22050);
        assertEquals(audioCodec.getSampleSizeBits(), 16);

        audioCodec = AudioCodec.fromString("INVALID_CODEC");
    }
}
