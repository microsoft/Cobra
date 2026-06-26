package com.nuance.cobra.utils.audio;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.encryption.Utils;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.ResponseHandlerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concentus.OpusException;

import javax.sound.sampled.AudioFormat;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

public class AudioPipeBuilder {
    private static final Logger logger = LogManager.getLogger(AudioPipeBuilder.class);

    /**
     * Error handling object
     */
    private ResponseErrorHandler errorHandler;

    private final CombinedConfiguration configs;
    private final Session session;
    private final AudioCodec codec;
    private ResponseHandler responseHandler;
    private AudioChunkerNode audioChunkerNode; //entry point
    private AudioNode pipeTail; //entry point
    private int randomPercentage;
    private final int sampleRate;

    private static final AudioChunkerNode NOP_CHUNKER = new AudioChunkerNode()
    {
        @Override
        public void onEvent(AudioNode eventOwner, NodeEvent event) {
            //NO_OP
        }

        @Override
        public void onAudio(byte[] bytes)
        {
            // Do nothing
        }
    };

    private ResponseErrorHandler getErrorHandler()
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new ResponseHandlerErrorListener(responseHandler));
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    private void initRandomNumber() {
        long startTime = System.nanoTime();

        SecureRandom random = Utils.getSecureRandom();
        randomPercentage = (int)(random.nextDouble() * 100);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        logger.debug("initRandomNumber took {} milliseconds", duration);

        //Seen: com.nuance.cobra.utils.audio.AudioPipeBuilder - initRandomNumber took 19352 milliseconds
        //on mtl-blade20-vm59. But mtl-blade20 doesn't have processors with rdrand.
        //I believe ECS might allow /dev/urandom in the JRE, but not sure.
    }

    public AudioPipeBuilder(CombinedConfiguration config, Session session) {
        this.configs = config;
        this.session = session;
        this.responseHandler = session.getResponseHandler();
        codec = getInputCodec();
        sampleRate = codec.getSampleRate();

        initRandomNumber();

        switch (codec.getEncoding())
        {
            case ULAW:
                audioChunkerNode = new LawChunkerNode(AudioFormat.Encoding.ULAW);
                break;

            case ALAW:
                audioChunkerNode = new LawChunkerNode(AudioFormat.Encoding.ALAW);
                break;

            case PCM:
                audioChunkerNode = new PcmChunkerNode(sampleRate);
                break;

            case IMA_ADPCM:
                audioChunkerNode = new ImaAdpcmChunkerNode(sampleRate);
                break;

            case OPUS:
                AudioChunkerNode acn;
                try
                {
                    acn = new OpusChunkerNode(sampleRate);
                }
                catch (OpusException e)
                {
                    logger.error("Failure creating OpusDecoder", e);

                    audioChunkerNode = NOP_CHUNKER;
                    pipeTail = audioChunkerNode;

                    final ResponseCode error = ResponseCode.createResponseError(
                            ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR,
                            "" + codec.getEncoding() + " encoding is not currently working, failure initializing decoder");
                    getErrorHandler().handleError(new SpeechRecoResponse(true, null, error));
                    return;
                }

                audioChunkerNode = acn;
                break;

            case SPEEX:

                // Defaults
                int mode;
                int sampleRate2 = sampleRate;
                int quality;
                int nbAudioChannels = 1;
                boolean enablePerceptualEnhancement = false;

                if (sampleRate == 8000) {
                    mode = 0;
                    quality = -1;
                } else if (sampleRate == 16000) {
                    mode = 1;
                    quality = -1;
                } else {
                    mode = -1;
                    quality = -1;
                }

                // Custom configuration for tenant
                HierarchicalConfiguration asrDecoderSettings = CobraConfigurationUtils.configurationAtOrNull(session.getSessionConfig(), "asrDecoder");
                if (asrDecoderSettings != null) {
                    HierarchicalConfiguration currentCodecDecoderSettings = CobraConfigurationUtils.configurationAtOrNull(asrDecoderSettings, codec.getWebapiString());
                    if (currentCodecDecoderSettings != null) {
                        mode = currentCodecDecoderSettings.getInt("mode", mode);
                        sampleRate2 = currentCodecDecoderSettings.getInt("sampleRate", sampleRate2);
                        quality = currentCodecDecoderSettings.getInt("quality", quality);
                        nbAudioChannels = currentCodecDecoderSettings.getInt("nbAudioChannels", nbAudioChannels);
                        enablePerceptualEnhancement = currentCodecDecoderSettings.getBoolean("perceptualEnhancement", enablePerceptualEnhancement);
                    }
                }

                audioChunkerNode = new RawSpeexDecoderNode(mode, sampleRate2, quality, nbAudioChannels, enablePerceptualEnhancement);
                break;
        }

        pipeTail = audioChunkerNode;
    }

    public AudioNode buildEndpointingWavDumperNode(List<AudioNodeObserver> observer){
        final String fileName = String.format(Locale.US, "%s_%s_%s_raw", session.getSessionId(), session.getCommandId(), session.getStepId());
        return buildWavDumperNode(fileName, true, observer);
    }

    public AudioNode buildWavDumperNode(List<AudioNodeObserver> observer)
    {
        final String fileName = String.format(Locale.US, "%s_%s_%s_raw", session.getSessionId(), session.getCommandId(), session.getStepId());
        return buildWavDumperNode(fileName, false, observer);
    }

    /**
     * Builds a speech detector node
     * @param observer List of observers interested in events form this node
     * @return The new AudiNode object
     */
    public AudioNode buildWavDumperNode(String fileName, boolean dumpEndpointedUtterance, List<AudioNodeObserver> observer) {
        final boolean wavEnabled = session.getSessionConfig().getBoolean(WebApiConfig.Property.WAV_ENABLED.key);
        final boolean tenantEdfEnabled = session.getSessionConfig().getBoolean(WebApiConfig.Property.EDF_POSTING_ENABLED_TENANT.key);

        int audioCaptureEnabled = configs.getInt(WebApiConfig.Property.AUDIO_CAPTURE.key);
        int tenantAudioCapture = session.getSessionConfig().getInt(WebApiConfig.Property.AUDIO_CAPTURE.key);
        int audioCapturePercentage = 0;
        boolean isDebugEnabled = session.getSessionConfig().getBoolean(WebApiConfig.Property.DEBUG_ENABLED.key);

        logger.debug("Audio Capture Enabled Value :  " + audioCaptureEnabled);
        //AudioCapture was set in either start tenantKey or speech reco command
        if (isDebugEnabled && 0 <= audioCaptureEnabled && audioCaptureEnabled <= 100)
        {
            logger.debug("Setting percentage value to :  " + audioCaptureEnabled + ", speech reco param found");
            audioCapturePercentage = audioCaptureEnabled;

            //Audio Capture is only set in the tenant
        } else if (isDebugEnabled && 0 < tenantAudioCapture && tenantAudioCapture <= 100){
            logger.debug("Setting percentage to : " + tenantAudioCapture + ", tenantKey or tenant percentage found");

            //we only care about tenant audio capture if audiocapture is not enabled in the other commands
            audioCapturePercentage = tenantAudioCapture;
        }

        WavDumperNode dumperNode = null;
        //TODO: complete opus file posting (if ever needed)

        if ( (isDebugEnabled && (randomPercentage <  audioCapturePercentage )) || tenantEdfEnabled)
        {
            logger.debug("Dumping .wav, Percentage is: " + audioCapturePercentage + " and generated rand is: " + randomPercentage);

            if (wavEnabled)
            {
                logger.debug("Wav Enabled : {}", wavEnabled);
                String logFormat = session.getSessionConfig().getString(WebApiConfig.Property.FLUENT_LOG_FORMAT.key);
                dumperNode = new WavDumperNode(codec.getSampleRate(), fileName, session, tenantEdfEnabled, logFormat, (randomPercentage < audioCapturePercentage), dumpEndpointedUtterance, responseHandler, observer);
            }
            else
            {
                logger.debug("Wav Not Enabled ");
            }

        }

        return dumperNode;
    }

    /**
     * Builds a timeout node
     * @param observer List of observers interested in events form this node
     * @return The new AudiNode object
     */
    public AudioNode buildTimeoutNode(int timeoutSeconds, String timerID, List<AudioNodeObserver> observer, boolean startRecognitionTimers) {
        TimeoutNode sosTimeoutNode = null;

        if (0 <= timeoutSeconds)
        {

            long sosTimeoutMilliseconds = 1000L * timeoutSeconds;
            if (0 == timeoutSeconds) sosTimeoutMilliseconds = Integer.MAX_VALUE - 1L;

            if (timerID != null && !timerID.isEmpty()) {
                sosTimeoutNode = new TimeoutNode(sosTimeoutMilliseconds, sampleRate, timerID, observer, startRecognitionTimers);
            } else {
                sosTimeoutNode = new TimeoutNode(sosTimeoutMilliseconds, sampleRate, observer, startRecognitionTimers);
            }
        }

        return sosTimeoutNode;
    }

    /**
     * Builds a timeout node
     * @param observer List of observers interested in events form this node
     * @return The new AudiNode object
     */
    public AudioNode buildTimeoutNode(int timeoutSeconds, List<AudioNodeObserver> observer, boolean startRecognitionTimers) {
        return buildTimeoutNode(timeoutSeconds, null, observer, startRecognitionTimers);
    }

    /**
     * Builds a speech detector node
     * @param observer List of observers interested in events form this node
     * @return The new AudiNode object
     */
    public AudioNode buildSpeechDetectorNode(List<AudioNodeObserver> observer) {
        int backgroundNoiseFrames = configs.getInt(WebApiConfig.Property.BEGIN_NOISE_SAMPLE_FRAMES.key);

        Integer startOfSpeechVoicedFrames = configs.getInt(
                WebApiConfig.Property.START_OF_SPEECH_VOICED_FRAMES.key);

        if (startOfSpeechVoicedFrames == -1){
            startOfSpeechVoicedFrames =  configs.getInt(WebApiConfig.Property.START_OF_SPEECH_FRAMES.key);
        }

        configs.getInt(WebApiConfig.Property.START_OF_SPEECH_FRAMES.key);

        int startOfSpeechHistoryFrames = configs.getInt(WebApiConfig.Property.START_OF_SPEECH_HISTORY_FRAMES.key);

        Integer endOfSpeechVoicedFrames = configs.getInt(
                WebApiConfig.Property.END_OF_SPEECH_VOICED_FRAMES.key);

        if (endOfSpeechVoicedFrames == -1){
            endOfSpeechVoicedFrames =  configs.getInt(WebApiConfig.Property.END_OF_SPEECH_FRAMES.key);
        }

        int endOfSpeechHistoryFrames = configs.getInt(WebApiConfig.Property.END_OF_SPEECH_HISTORY_FRAMES.key);
        double thresholdRatio = Double.parseDouble(configs.getString(WebApiConfig.Property.VOICE_THRESHOLD.key));
        boolean considerNegativeRatios = configs.getBoolean(WebApiConfig.Property.CONSIDER_NEGATIVE_RATIOS.key);

        NoiseLevel noiseLevel = null;
        HierarchicalConfiguration<ImmutableNode> noiseLevelConfig = CobraConfigurationUtils.configurationAtOrNull(configs, WebApiConfig.Property.NOISE_LEVEL.key);
        if (noiseLevelConfig != null)
        {
            double mean = Double.parseDouble(noiseLevelConfig.getString("mean"));
            double standardDeviation = Double.parseDouble(noiseLevelConfig.getString("standardDeviation"));
            noiseLevel = new NoiseLevel(mean, standardDeviation);
        }

        String speechDetector = configs.getString(WebApiConfig.Property.SPEECH_DETECTOR.key);
        AudioNode speechDetectorNode = null;
        if (SpeechDetectorTypes.ADAPTIVE.getValue().equals(speechDetector))
        {
            speechDetectorNode = new AdaptiveSpeechDetectorNode(backgroundNoiseFrames,
                    startOfSpeechVoicedFrames, startOfSpeechHistoryFrames,
                    endOfSpeechVoicedFrames, endOfSpeechHistoryFrames,
                    thresholdRatio, noiseLevel, observer);
        }
        else if (!SpeechDetectorTypes.ENGINE.getValue().equals(speechDetector)) // Invalid values default to legacy
        {
            speechDetectorNode = new LegacySpeechDetectorNode(backgroundNoiseFrames,
                    startOfSpeechVoicedFrames, startOfSpeechHistoryFrames,
                    endOfSpeechVoicedFrames, endOfSpeechHistoryFrames,
                    thresholdRatio, considerNegativeRatios, noiseLevel, observer);
        }

        return speechDetectorNode;
    }

    public AudioNode buildStatisticsCalculatorNode(List<AudioNodeObserver> observers) {
        return new StatisticsCalculatorNode(observers);
    }

    public AudioNode createdMultipleDestination(AudioDestination ... destinations) {
        DuplicatorNode node = new DuplicatorNode();
        for (AudioDestination dest: destinations) {
            node.connect(dest);
        }
        return node;
    }

    public AudioNode appendNode(AudioNode toInsert) {
        if (toInsert != null) {
            pipeTail.connect(toInsert);
            pipeTail = toInsert;
        }

        return pipeTail;
    }

    public AudioNode appendDestination(AudioDestination destinations) {
        if (destinations != null) {
            pipeTail.connect(destinations);
        }

        return pipeTail;
    }

    public AudioChunkerNode getPipe() {
        return audioChunkerNode;
    }

    public AudioCodec getInputCodec()
    {
        final String string = configs.getString(WebApiConfig.Property.SPEECH_RECOGNITION_CODEC.key);
        return AudioCodec.fromString(string);
    }

    public AudioCodec getOutputCodec()
    {
        final String string = configs.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_CODEC.key);
        return AudioCodec.fromString(string);
    }
}
