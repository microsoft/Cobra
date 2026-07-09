package com.nuance.plugin.audio;

import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class AudioEncoder {
    private static final Logger logger = LogManager.getLogger();
    public static final String AUDIO_ENCODER_VBR_KEY = "audioEncoderVBR";

    public static AudioEncoder buildEncoder(ICodecResource.Codec toCodec, BaseHierarchicalConfiguration configs) {
        if (toCodec.getNmaidCode() >= ICodecResource.Codec.MP3.getNmaidCode() && toCodec.getNmaidCode() <= ICodecResource.Codec.MP3_160KBPS.getNmaidCode()) {
            try {
                return new Mp3EncoderAudioEncoder(toCodec, AudioCodec.fromString(toCodec.getWebAPIString()), configs.getBoolean(AUDIO_ENCODER_VBR_KEY));
            } catch (IOException e) {
                logger.info("Requested codec '{}' is not supported by webapi. Defaulting to: {}", toCodec.getMimeType(), AudioCodec.PCM_16_8K.name());
                return new Mp3EncoderAudioEncoder(toCodec, AudioCodec.PCM_16_8K, configs.getBoolean(AUDIO_ENCODER_VBR_KEY));
            }
        } else {
            return new PassThroughAudioEncoder();
        }
    }

    public abstract byte[] onAudio(byte[] data);
    public abstract void onDone();
}
