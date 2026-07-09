package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.audio.NoiseLevel;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class SpeechRecoStubTask
        implements Runnable
{
    private static final Logger logger = LogManager.getLogger(SpeechRecoStubTask.class);

    private final ResponseHandler responseHandler;
    private final CombinedConfiguration serverConfig;

    public SpeechRecoStubTask(ResponseHandler responseHandler)
    {
        this.responseHandler = responseHandler;
        this.serverConfig = WebApiConfigManager.getInstance();
    }

    @Override
    public void run()
    {
        int stubRecoDelayMs = serverConfig.getInt(WebApiConfig.Property.STUB_RECO_DELAY_MILLISECONDS.key);
        int stubSosEosRecoDelayMs =
                serverConfig.getInt(WebApiConfig.Property.STUB_RECO_SOS_EOS_DELAY_MILLISECONDS.key);

        //Delay between first audio and start of speech
        sleep(stubRecoDelayMs);

        logger.trace("Sending stub startOfSpeech");
        responseHandler.sendEvent(new ResponseEvent("startOfSpeech"));

        sleep(stubSosEosRecoDelayMs / 2);

        logger.trace("Sending stub partial results");
        responseHandler.onResponse(buildStubSpeechRecoResponse(false));

        sleep(stubSosEosRecoDelayMs / 2);

        logger.trace("Sending stub endOfSpeech");
        responseHandler.sendEvent(new ResponseEvent("endOfSpeech"));

        //Delay between end of speech and rec complete
        sleep(stubRecoDelayMs);

        logger.trace("Sending stub response");
        responseHandler.onResponse(buildStubSpeechRecoResponse(true));
    }

    private void sleep(int sleepMs)
    {
        try
        {
            if (sleepMs > 0)
            {
                Thread.sleep(sleepMs);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
        }
    }

    private SpeechRecoResponse buildStubSpeechRecoResponse(boolean isFinal)
    {
        final String[] transcriptions = { "this is a stub", "this isn't a stub" };
        final int confidence = 100;
        final int[] averageWeightedConfidences = { 1000, 0 };
        final SpeechRecoResponse.Word[][] words = new SpeechRecoResponse.Word[2][];
        for (int i = 0; i < words.length; i++)
        {
            words[i] = new SpeechRecoResponse.Word[4];
            words[i][0] = new SpeechRecoResponse.Word("this", 100);
            if (i == 0)
            {
                words[i][1] = new SpeechRecoResponse.Word("is", 100);
            }
            else
            {
                words[i][1] = new SpeechRecoResponse.Word("isn't", 0);
            }
            words[i][2] = new SpeechRecoResponse.Word("a", 100);
            words[i][3] = new SpeechRecoResponse.Word("stub", 100);
        }

        //This is a stub simulation, after all.
        NoiseLevel noiseLevel = new NoiseLevel(66.6666, 22.2222);

        SpeechRecoResponse.Data data = new SpeechRecoResponse.Data(transcriptions, averageWeightedConfidences, confidence, words, -1);
        return new SpeechRecoResponse(isFinal, data, noiseLevel);
    }
}
