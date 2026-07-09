package com.nuance.nmsp.servlet.nmas.tts;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.audio.AudioSinkListener;
import com.nuance.nmsp.servlet.nmas.NMASListener;
import com.nuance.nmsp.servlet.util.HttpUtil;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NMDPTTSCmdListener extends NMASListener
        implements AudioSinkListener
{

    private static final Logger log = ESAPI.getLogger(NMDPTTSCmdListener.class);
    protected HttpServletResponse response;
    protected PdxAudioSink audioSink;

    public NMDPTTSCmdListener(Transaction transaction, HttpServletResponse response)
    {
        super(transaction);
        this.response = response;
    }

    public void setAudioSink(PdxAudioSink audioSink)
    {
        this.audioSink = audioSink;
    }

    @Override
    public void onConnected(String sessionId)
    {
        super.onConnected(sessionId);
        ESAPI.httpUtilities().setHeader(response, HttpUtil.SESSIONID_HEADER, transaction.getSessionId());
        ESAPI.httpUtilities().setHeader(response, HttpUtil.NUANCE_SESSIONID_HEADER, transaction.getSessionId());
    }

    @Override
    public void onAudioReceived(byte[] audioData)
    {
        if (audioSink == null || audioData == null) return;
        try
        {
            audioSink.addAudioBuf(audioData, 0, audioData.length, false);
        }
        catch (IOException e)
        {
            log.error(Logger.EVENT_UNSPECIFIED, "Failed to forward TTS audio to sink", e);
        }
    }

    @Override
    public void onQueryResult(PdxQueryResult result)
    {
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + " Received QueryResult final=" + result.isFinal());
        if (audioSink != null && result.isFinal())
        {
            try
            {
                audioSink.addAudioBuf(null, 0, 0, true);
            }
            catch (IOException e)
            {
                log.error(Logger.EVENT_UNSPECIFIED, "Failed to flush TTS audio sink", e);
            }
        }
    }

    @Override
    public void audioCompleted()
    {
        transaction.setRequestCompleted(true);
    }

    @Override
    public void audioFailed()
    {
        transaction.setError("Failed to send the audio.");
        transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        transaction.setRequestCompleted(false);
    }

}
