package com.nuance.plugin.ncs.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ICodecResourceTest {
    @Test
    public void testFromMimeTypeString() {
        ICodecResource.Codec testCodec = ICodecResource.Codec.fromMimeTypeString("audio/l16;rate=16000");
        assertEquals(ICodecResource.Codec.PCM16_16K, testCodec);

        testCodec = ICodecResource.Codec.fromMimeTypeString("audio/opus;rate=16000");
        assertEquals(ICodecResource.Codec.OPUS_16K_MONO_CBR_CPLX0, testCodec);

        testCodec = ICodecResource.Codec.fromMimeTypeString("audio/speex;rate=16000");
        assertEquals(ICodecResource.Codec.SPEEX_WB_NR0, testCodec);
        assertEquals("audio/speex;rate=16000", testCodec.getMimeType());

        testCodec = ICodecResource.Codec.fromMimeTypeString("video/speex;rate=16000");
        assertEquals(ICodecResource.Codec.UNKNOWN, testCodec);
    }

    @Test
    public void testFromWebApiString() {
        ICodecResource.Codec testCodec = ICodecResource.Codec.fromWebApiString("pcm_16_16k");
        assertEquals(ICodecResource.Codec.PCM16_16K, testCodec);
    }
}