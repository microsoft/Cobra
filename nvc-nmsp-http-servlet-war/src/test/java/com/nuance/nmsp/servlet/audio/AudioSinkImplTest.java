package com.nuance.nmsp.servlet.audio;

import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class AudioSinkImplTest
{
    private AudioSinkImpl audioSink;
    private AudioSinkImpl audioSinkConvert;
    private AudioSinkImpl audioSinkSpeex;
    private AudioSinkImpl audioSinkConvertSpeex;
    private AudioSinkImpl audioSinkSpeex8k;
    private HttpServletResponse httpServletResponse;
    private SecureRandom secureRandom = new SecureRandom();

    @Before
    public void setUp()
    {
        httpServletResponse = new MockHttpServletResponse();
        audioSink = new AudioSinkImpl(httpServletResponse, false, ServletCodecWrapper.DEFAULT);
        audioSinkConvert = new AudioSinkImpl(httpServletResponse, true, ServletCodecWrapper.DEFAULT);
        audioSinkSpeex = new AudioSinkImpl(httpServletResponse, false, ServletCodecWrapper.SPEEX_16K);
        audioSinkConvertSpeex = new AudioSinkImpl(httpServletResponse, true, ServletCodecWrapper.SPEEX_16K);
        audioSinkSpeex8k = new AudioSinkImpl(httpServletResponse, false, ServletCodecWrapper.SPEEX_8K);
    }

    @Test
    public void getSubArray()
    {
        byte[] rc = AudioSinkImpl.getSubArray(null, 0, 0);
        assertNull(rc);

        byte[] input = { 0xA, 0xB, 0xC };
        rc = AudioSinkImpl.getSubArray(input, 1, 1);
        assertEquals(input[1], rc[0]);
    }

    @Test(expected = Test.None.class)
    public void addAudioBuf()
    {
        AudioSinkListener audioSinkListener = mock(AudioSinkListener.class);
        audioSink.registerListener(audioSinkListener);
        audioSink.removeListener(audioSinkListener);
        audioSink.registerListener(audioSinkListener);

        byte[] input = new byte[1024];
        secureRandom.nextBytes(input);

        audioSink.addAudioBuf(input, 0, input.length, false);
        audioSink.addAudioBuf(null, 0, 0, true);

        audioSinkConvert.addAudioBuf(input, 0, input.length, false);
        audioSinkConvert.addAudioBuf(null, 0, 0, true);

        audioSinkSpeex.addAudioBuf(input, 0, input.length, false);
        audioSinkSpeex.addAudioBuf(null, 0, 0, true);

        audioSinkConvertSpeex.addAudioBuf(input, 0, input.length, false);
        audioSinkConvertSpeex.addAudioBuf(null, 0, 0, true);

        audioSinkSpeex8k.addAudioBuf(input, 0, input.length, false);
        audioSinkSpeex8k.addAudioBuf(null, 0, 0, true);
    }


}