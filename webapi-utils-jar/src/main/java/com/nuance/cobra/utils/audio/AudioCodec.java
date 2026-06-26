package com.nuance.cobra.utils.audio;

import java.util.Arrays;
import java.util.List;

public enum AudioCodec
{
    PCM_16_8K(Encoding.PCM, 8000, 16, "pcm_16_8k", "audio/l16;rate=8000", "audio/x-raw;format=s16le;rate=8000"),
    PCM_16_16K(Encoding.PCM, 16000, 16, "pcm_16_16k", "audio/l16;rate=16000", "audio/x-raw;format=s16le;rate=16000"),
    PCM_16_22K(Encoding.PCM, 22050, 16, "pcm_16_22k", "audio/l16;rate=22050", "audio/l16;rate=22000", "audio/x-raw;format=s16le;rate=22050"),
    PCM_16_24K(Encoding.PCM, 24000, 16, "pcm_16_24k", "audio/l16;rate=24000", "audio/l16;rate=24000", "audio/x-raw;format=s16le;rate=24000"),
    IMA_ADPCM(Encoding.IMA_ADPCM, 16000, 16, "ima_adpcm", "audio/adpcm;rate=16000", "audio/16kadpcm;rate=16000"),
    OPUS_24K(Encoding.OPUS, 24000, 16, "opus_16_24k", "audio/opus;rate=24000"),
    OPUS_16K(Encoding.OPUS, 16000, 16, "opus_wb", "opus_16_16k", "opus_16k", "audio/opus;rate=16000"),
    OPUS_8K(Encoding.OPUS, 8000, 16, "opus_nb", "opus_16_8k", "opus_8k", "audio/opus;rate=8000"),
    SPEEX_WB_NR8(Encoding.SPEEX, 16000, 16, "speex_wb", "speex_16k", "speex_278", "audio/speex;rate=16000", "audio/x-speex;mode=wb"),
    SPEEX_NB_NR6(Encoding.SPEEX, 8000, 16, "speex_nb", "speex_8k", "speex_262", "audio/speex;rate=8000"),
    ULAW(Encoding.ULAW, 8000, 16, "ulaw", "audio/basic;rate=8000"),
    ALAW(Encoding.ALAW, 8000, 16, "alaw", "audio/x-alaw-basic;rate=8000");

    public enum Encoding
    {
        PCM,
        IMA_ADPCM,
        OPUS,
        SPEEX,
        ULAW,
        ALAW
    }

    private final Encoding encoding;
    private final int sampleRate;
    private final int sampleSizeBits;
    private final List<String> stringNames;

    AudioCodec(Encoding encoding, int sampleRate, int sampleSizeBits, String ... names)
    {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeBits = sampleSizeBits;
        this.stringNames = Arrays.asList(names);
    }

    public String getWebapiString() {
        return this.stringNames.get(0);
    }

    public Encoding getEncoding()
    {
        return encoding;
    }

    public int getSampleRate()
    {
        return sampleRate;
    }
    public int getSampleSizeBits()
    {
        return sampleSizeBits;
    }

    public static AudioCodec fromString(String string)
    {
        String searchString = string.toLowerCase();

        for (AudioCodec codec: AudioCodec.values()) {
            if (codec.stringNames.contains(searchString)) {
                return codec;
            }
        }
        throw new IllegalArgumentException("Unsupported or unknown codec: " + string);
    }

}
