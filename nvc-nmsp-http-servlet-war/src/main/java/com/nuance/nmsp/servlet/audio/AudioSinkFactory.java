package com.nuance.nmsp.servlet.audio;

import com.nuance.nvc.httpservlet.codecwrapper.ServletCodecWrapper;

import javax.servlet.http.HttpServletResponse;

public class AudioSinkFactory
{
    public AudioSinkImpl newAudioSink(HttpServletResponse response, boolean convert, ServletCodecWrapper codec)
    {
        return new AudioSinkImpl(response, convert, codec);
    }
}
