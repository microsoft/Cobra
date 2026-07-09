package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.encryption.Utils;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

class SpeechSynthesisStubTask
        implements Runnable
{
    private static final Logger logger = LogManager.getLogger(SpeechSynthesisStubTask.class);
    private final ResponseHandler responseHandler;
    private final AtomicBoolean isSpeechSynthesisDone;
    private final CombinedConfiguration serverConfig;

    public SpeechSynthesisStubTask(ResponseHandler responseHandler, AtomicBoolean isSpeechSynthesisDone)
    {
        this.responseHandler = responseHandler;
        this.isSpeechSynthesisDone = isSpeechSynthesisDone;
        this.serverConfig = WebApiConfigManager.getInstance();
    }

    @Override
    public void run()
    {
        int stubTtsBufferSize = serverConfig.getInt(WebApiConfig.Property.STUB_TTS_BUFFER_SIZE.key);
        int stubTtsDelayMs = serverConfig.getInt(WebApiConfig.Property.STUB_TTS_DELAY_MILLISECONDS.key);

        SecureRandom random = Utils.getSecureRandom();
        byte[] audioBuffer = new byte[stubTtsBufferSize];
        random.nextBytes(audioBuffer);

        try
        {
            if (stubTtsDelayMs > 0)
            {
                Thread.sleep(stubTtsDelayMs);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }

        responseHandler.onAudio(audioBuffer, true);

        SpeechSynthesisResponse response = new SpeechSynthesisResponse(ResponseResult.ResponseStatus.SUCCESS, true);

        logger.trace("Setting atomic bool in SpeechSynthesisStubTask run()");
        this.isSpeechSynthesisDone.set(true);
        responseHandler.onResponse(response);
    }
}