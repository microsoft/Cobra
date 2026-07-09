package com.nuance.ncs.nmsp.servlet.utils;

import com.nuance.plugin.ncs.utils.ICodecResource;
import org.junit.Test;

import static org.junit.Assert.*;

public class AudioCodecTypeTest
{
    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new AudioCodecType();
    }

    @Test
    public void isPcm()
    {
        assertTrue(AudioCodecType.isPcm(ICodecResource.Codec.PCM16_8K));
        assertTrue(AudioCodecType.isPcm(ICodecResource.Codec.PCM16_11K));
        assertTrue(AudioCodecType.isPcm(ICodecResource.Codec.PCM16_16K));
        assertTrue(AudioCodecType.isPcm(ICodecResource.Codec.PCM16_22K));
        assertTrue(AudioCodecType.isPcm(ICodecResource.Codec.PCM16_32K));
        assertFalse(AudioCodecType.isPcm(ICodecResource.Codec.MP3));
    }

    @Test
    public void isMpeg()
    {
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_8KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_16KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_24KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_32KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_40KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_48KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_56KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_64KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_80KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_96KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_112KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_128KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_144KBPS));
        assertTrue(AudioCodecType.isMpeg(ICodecResource.Codec.MP3_160KBPS));
        assertFalse(AudioCodecType.isMpeg(ICodecResource.Codec.PCM16_8K));
    }

    @Test
    public void isSpeex()
    {
        assertTrue(AudioCodecType.isSpeex(ICodecResource.Codec.SPEEX_NB_NR6));
        assertTrue(AudioCodecType.isSpeex(ICodecResource.Codec.SPEEX_WB_NR8));
        assertFalse(AudioCodecType.isSpeex(ICodecResource.Codec.PCM16_8K));
    }
}
