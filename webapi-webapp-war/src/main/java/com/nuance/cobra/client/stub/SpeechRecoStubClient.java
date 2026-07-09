package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.audio.AudioChunk;
import com.nuance.cobra.utils.audio.AudioNode;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechRecoStubClient
        extends SpeechRecoClient
{
    private static final Logger logger = LogManager.getLogger(SpeechRecoStubClient.class);

    private final String sessionId;
    private final ResponseHandler responseHandler;
    private final AtomicBoolean gotSomeAudio;

    public SpeechRecoStubClient(String sessionId, ResponseHandler responseHandler)
    {
        super(sessionId);
        this.sessionId = sessionId;
        this.responseHandler = responseHandler;
        this.gotSomeAudio = new AtomicBoolean(false);
    }

    @Override
    public void startSpeechRecognition()
    {
        logger.debug("Using stubbed speech recognition for session {}", this.sessionId);
    }

    @Override
    public void stopSpeechRecognition()
    {

    }

    @Override
    public void cancelSpeechRecognition(boolean isFinal)
    {
        try
        {
            Runnable task = new ThreadContextRunnable() {
                @Override
                public void runWithContext() {
                    responseHandler.onResponse(new SpeechRecoResponse(isFinal, ResponseResult.ResponseStatus.CANCELLED, null));
                }
            };
            SessionManager.getBoundedExecutorService()
                          .submitTask(task);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }
    }

    @Override
    public void startRecognitionTimers()
    {
        //not for stub client
    }

    @Override
    public boolean setRecResultType(RecResultType recResultType)
    {
        return true;
    }

    @Override
    public float getMaxConfidence()
    {
        return 1000.0f;
    }

    public boolean gotSomeAudio()
    {
        return gotSomeAudio.get();
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {

    }

    @Override
    public void onAudio(AudioChunk chunk) {
        //Return from this method on subsequent audio calls
        logger.trace("Entering compareAndSet in SpeechRecoStubClient onAudio");
        boolean gotSomeAudioForFirstTime = this.gotSomeAudio.compareAndSet(false, true);
        if (!gotSomeAudioForFirstTime)
        {
            logger.trace("Not the first audio!");
            return;
        }

        logger.trace("This is the first audio!");

        try
        {
            SessionManager.getBoundedExecutorService()
                    .submitTask(new SpeechRecoStubTask(responseHandler));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }
    }

    @Override
    public void finish() {

    }
}
