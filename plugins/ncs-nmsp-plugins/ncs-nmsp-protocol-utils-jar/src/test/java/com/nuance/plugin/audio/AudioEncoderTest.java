package com.nuance.plugin.audio;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class AudioEncoderTest {

    @Test
    public void buildEncoder() {
        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        CombinedConfiguration configs = new CombinedConfiguration();
        configs.addConfiguration(config);
        config.setProperty(AudioEncoder.AUDIO_ENCODER_VBR_KEY, true);

        AudioEncoder encoder = AudioEncoder.buildEncoder(ICodecResource.Codec.OPUS_8K_MONO_VBR_CPLX4, configs);
        assertTrue(encoder instanceof PassThroughAudioEncoder);
        byte[] response = encoder.onAudio("test".getBytes(StandardCharsets.UTF_8));
        assertTrue(response.length > 0);

        encoder = AudioEncoder.buildEncoder(ICodecResource.Codec.MP3_128KBPS, configs);
        assertTrue(encoder instanceof Mp3EncoderAudioEncoder);
        response = encoder.onAudio("test".getBytes(StandardCharsets.UTF_8));
        assertTrue(response.length > 0);
        encoder.onDone();
    }
}