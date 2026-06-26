package com.nuance.cobra.client.stub;

import com.nuance.cobra.client.SpeechSynthesisClient;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechSynthesisStubClient
        extends SpeechSynthesisClient
{
    private static final Logger logger = LogManager.getLogger(SpeechSynthesisStubClient.class);

    private final String sessionId;
    private final ResponseHandler responseHandler;
    private final AtomicBoolean isSpeechSynthesisDone;

    public SpeechSynthesisStubClient(String sessionId, ResponseHandler responseHandler)
    {
        super(sessionId);
        this.sessionId = sessionId;
        this.responseHandler = responseHandler;
        this.isSpeechSynthesisDone = new AtomicBoolean(true);
    }

    @Override
    public void startSpeechSynthesis()
    {
        this.isSpeechSynthesisDone.set(false);
        logger.debug("Using stubbed speech synthesis for session {}", this.sessionId);

        try
        {
            SessionManager.getBoundedExecutorService()
                          .submitTask(new SpeechSynthesisStubTask(responseHandler, isSpeechSynthesisDone));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }
    }

    @Override
    public void stopSpeechSynthesis()
    {

    }

    @Override
    public void cancelSpeechSynthesis(boolean isFinal)
    {

    }

    @Override
    public boolean isSpeechSynthesisDone()
    {
        logger.trace("Getting atomic bool in SpeechSynthesisStubClient isSpeechSynthesisDone");
        return this.isSpeechSynthesisDone.get();
    }
}
