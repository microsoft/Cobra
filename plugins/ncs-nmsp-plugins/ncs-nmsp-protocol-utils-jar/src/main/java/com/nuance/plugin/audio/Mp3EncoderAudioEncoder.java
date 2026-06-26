package com.nuance.plugin.audio;

import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.audio.encoders.Mp3Encoder;
import com.nuance.plugin.ncs.utils.ICodecResource;

import javax.sound.sampled.AudioFormat;

public class Mp3EncoderAudioEncoder extends AudioEncoder {
    private final Mp3Encoder mp3Encoder;

    public Mp3EncoderAudioEncoder(ICodecResource.Codec toCodec, AudioCodec fromCodec, boolean vbrEnabled) {
        AudioFormat inAudioFormat = new AudioFormat(fromCodec.getSampleRate(), fromCodec.getSampleSizeBits(), 1, false, false);
        mp3Encoder = new Mp3Encoder(inAudioFormat, toCodec.getRate().getRate()/1000, 1, vbrEnabled);
    }

    @Override
    public byte[] onAudio(byte[] data) {
        return mp3Encoder.encode(data);
    }

    @Override
    public void onDone() {
        mp3Encoder.onDone();
    }
}
