package com.nuance.nmsp.servlet.nmas.audio;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.nmsp.servlet.util.HttpUtil;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Streams a standard audio file (e.g. PCM) to the NMSP Gateway via a PdxAudioSink.
 */
public class AudioStreamer
        extends Thread
{
    private static final Logger log = ESAPI.getLogger(AudioStreamer.class);

    private static final int MAX_BUFFERING_LENGTH = 24 * 1024;
    protected final InputStream inputStream;
    protected final PdxAudioSink audioSink;
    protected boolean finished = false;
    private boolean decodeUTF8 = false;

    public AudioStreamer(HttpServletRequest request, PdxAudioSink audioSink)
            throws IOException
    {
        this.inputStream = request.getInputStream();
        this.audioSink = audioSink;

        String decodeHeader = HttpUtil.getSanitizedHeader(request, "Decode-Audio-UTF8");
        decodeUTF8 = (decodeHeader != null && "true".equalsIgnoreCase(decodeHeader.trim()));
    }

    @Override
    public synchronized void run()
    {
        byte[] buffering = new byte[MAX_BUFFERING_LENGTH];
        int len;

        try
        {
            while ((len = inputStream.read(buffering, 0, buffering.length)) > -1)
            {
                byte[] audioBuf = new byte[len];
                System.arraycopy((decodeUTF8) ? decodeAudio(buffering) : buffering, 0, audioBuf, 0, len);
                audioSink.addAudioBuf(audioBuf, 0, audioBuf.length, false);
            }
            audioSink.addAudioBuf(null, 0, 0, true);
            finished = true;
            notifyAll();
        }
        catch (IOException e)
        {
            log.debug(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
            finished = true;
            notifyAll();
        }
    }

    private byte[] decodeAudio(byte[] encodedAudio)
    {
        Charset cs = StandardCharsets.UTF_8;
        ByteBuffer bb = ByteBuffer.wrap(encodedAudio);
        cs.decode(bb);
        bb.rewind();
        return bb.array();
    }

    public synchronized void waitAudioStreamingFinished()
    {
        while (!finished)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
