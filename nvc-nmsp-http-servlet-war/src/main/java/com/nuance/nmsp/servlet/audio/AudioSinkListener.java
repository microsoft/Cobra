package com.nuance.nmsp.servlet.audio;


/**
 * An AudioSink listener interface
 */
public interface AudioSinkListener
{

    /**
     * The audio sink has received all the audio.
     */
    void audioCompleted();

    /**
     * The audio sink failed to send the audio.
     */
    void audioFailed();

}
