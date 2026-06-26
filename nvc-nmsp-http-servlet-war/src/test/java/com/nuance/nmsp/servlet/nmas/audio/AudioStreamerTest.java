package com.nuance.nmsp.servlet.nmas.audio;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.SecureRandom;

import static org.mockito.Mockito.mock;

public class AudioStreamerTest
{
    private AudioStreamer audioStreamer;
    private SecureRandom secureRandom = new SecureRandom();

    @Before
    public void setUp()
            throws Exception
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        byte[] input = new byte[1024];
        secureRandom.nextBytes(input);
        httpServletRequest.setContent(input);
        httpServletRequest.addHeader("Decode-Audio-UTF8", "true");

        PdxAudioSink audioSink = mock(PdxAudioSink.class);
        audioStreamer = new AudioStreamer(httpServletRequest, audioSink);
    }

    @Test(expected = Test.None.class)
    public void run()
    {
        audioStreamer.start();
        audioStreamer.waitAudioStreamingFinished();
    }
}
