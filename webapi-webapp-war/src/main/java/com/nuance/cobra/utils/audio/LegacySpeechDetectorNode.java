package com.nuance.cobra.utils.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LegacySpeechDetectorNode
        extends SpeechDetector  implements AudioDestination
{
    private static final Logger logger = LogManager.getLogger(LegacySpeechDetectorNode.class);

    private final int backgroundNoiseFrames;
    private final int startOfSpeechVoicedFrameCount;
    private final int endOfSpeechVoicedFrameCount;
    private final double thresholdRatio;
    private final double negativeThresholdRatio;
    private final int beginDelayFrames;
    private final int startOfSpeechHistoryFrameCount;
    private final int endOfSpeechHistoryFrameCount;
    private final boolean considerNegativeRatios;
    private final NoiseLevel noiseLevel;

    public enum Voiced
    {
        POSITIVE,
        NEGATIVE
    }

    private int beginDelayCount = 0;
    private int beginNoiseCount = 0;

    private double backgroundNoiseMean = 0.0;
    private double backgroundNoiseStdDev = 0.0;
    private double m2 = 0.0;

    private static final int SMOOTHING_LENGTH = 5; // Must be at least 2
    private final double[] energyHistory = new double[SMOOTHING_LENGTH];
    private final double[] sortedHistory = new double[SMOOTHING_LENGTH];
    private int energyHistoryPos = -1;

    private final Voiced[] voicedHistory;
    private int voicedHistoryPos = -1;

    private int counter;
    private int frameCount = 0;

    public LegacySpeechDetectorNode(int backgroundNoiseFrames, int startOfSpeechVoicedFrameCount, int startOfSpeechHistoryFrameCount,
                                    int endOfSpeechVoicedFrameCount, int endOfSpeechHistoryFrameCount, double thresholdRatio,
                                    boolean considerNegativeRatios, NoiseLevel noiseLevel, List<AudioNodeObserver> observer)
    {
        addObserver(observer);
        this.backgroundNoiseFrames = backgroundNoiseFrames;
        this.startOfSpeechVoicedFrameCount = startOfSpeechVoicedFrameCount;
        this.endOfSpeechVoicedFrameCount = endOfSpeechVoicedFrameCount;
        this.thresholdRatio = thresholdRatio;
        negativeThresholdRatio = -1.0 + (1.0 / thresholdRatio);

        this.beginDelayFrames = 1; // FIXME not hard coded
        this.startOfSpeechHistoryFrameCount = startOfSpeechHistoryFrameCount;
        this.endOfSpeechHistoryFrameCount = endOfSpeechHistoryFrameCount;

        this.considerNegativeRatios = considerNegativeRatios;

        final int historyMax = Math.max(this.startOfSpeechHistoryFrameCount, this.endOfSpeechHistoryFrameCount);
        voicedHistory = new Voiced[historyMax];

        for (int i = 0; i < energyHistory.length; i++)
        {
            energyHistory[i] = 0.0;
        }

        for (int i = 0; i < voicedHistory.length; i++)
        {
            voicedHistory[i] = null;
        }

        this.noiseLevel = noiseLevel;
        if (noiseLevel != null)
        {
            logger.trace("LegacySpeechDetectorNode - Using provided backgroundNoise mean: {}, stdDev: {}", getBackgroundNoiseMean(), getBackgroundNoiseStdDev());
        }
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        //NO_OP for now
    }

    public void onAudio(AudioChunk chunk)
    {
        if (frameCount == 0) {
            notifyInputStart();
        }

        frameCount += 1;
        String message = String.format("onAudio frame %d -- ", frameCount);

        final double energy = chunk.getEnergy();
        logger.debug(message + "energy: {}", energy);

        if (beginDelayCount < beginDelayFrames)
        {
            beginDelayCount += 1;
            logger.trace(message + "ignoring begin-delay frame {}", beginDelayCount);

            dispatchChunk(chunk);
            return;
        }

        energyHistoryPos += 1;
        if (energyHistoryPos >= energyHistory.length)
        {
            energyHistoryPos = 0;
        }
        energyHistory[energyHistoryPos] = energy;

        beginNoiseCount += 1;

        if (backgroundNoiseFrames >= beginNoiseCount)
        {
            logger.trace(message + "backgroundNoiseFrame: {}/{}", beginNoiseCount, backgroundNoiseFrames);
            final double delta = energy - backgroundNoiseMean;
            backgroundNoiseMean += delta / beginNoiseCount;
            m2 += delta * (energy - backgroundNoiseMean);
            if (backgroundNoiseFrames == beginNoiseCount)
            {
                backgroundNoiseStdDev = Math.sqrt(m2 / (backgroundNoiseFrames - 1));
                logger.trace(message + "backgroundNoise mean: {}, stdev: {}", backgroundNoiseMean, backgroundNoiseStdDev);
            }

            notifyNoiseLevel();
            dispatchChunk(chunk);
            return;
        }

        double sum = 0.0;
        for (int i = 0; i < SMOOTHING_LENGTH; i++)
        {
            double e = energyHistory[i];

            sum += e;

            int j;
            for (j = 0; j < i; j++)
            {
                if (e < sortedHistory[j])
                {
                    final double tmp = sortedHistory[j];
                    sortedHistory[j] = e;
                    e = tmp;
                }
            }
            sortedHistory[j] = e;
        }

        final double average = sum / SMOOTHING_LENGTH;
        final double median = sortedHistory[SMOOTHING_LENGTH >> 1];

        final double averageRatio = (average - getBackgroundNoiseMean()) / getBackgroundNoiseStdDev();
        final double medianRatio = (median - getBackgroundNoiseMean()) / getBackgroundNoiseStdDev();
        Voiced voiced = null;
        if (medianRatio >= thresholdRatio && averageRatio >= thresholdRatio)
        {
            voiced = Voiced.POSITIVE;
        }
        if (medianRatio <= negativeThresholdRatio && averageRatio <= negativeThresholdRatio)
        {
            voiced = Voiced.NEGATIVE;
        }

        logger.trace(message + "medianRatio: {}, averageRatio: {}, voiced: {}", medianRatio, averageRatio, voiced);

        voicedHistoryPos += 1;
        if (voicedHistoryPos >= voicedHistory.length)
        {
            voicedHistoryPos = 0;
        }
        voicedHistory[voicedHistoryPos] = voiced;

        message += String.format("state: %s", state);
        int voicedCount;
        int negativeVoicedCount;
        switch (state)
        {
            case BEFORE:
                voicedCount = 0;
                for (int i = 0, p = voicedHistoryPos; i < startOfSpeechHistoryFrameCount;
                        i++, p = -1 + (p > 0 ? p : voicedHistory.length))
                {
                    if (Voiced.POSITIVE == voicedHistory[p])
                    {
                        voicedCount++;
                    }
                }

                logger.trace(message + ", voicedCount: {} of {}", voicedCount, startOfSpeechHistoryFrameCount);

                if (voicedCount >= startOfSpeechVoicedFrameCount)
                {
                    logger.debug(message + ", SoS detected, frame {}", frameCount);
                    counter = 0;
                    state = State.DURING;
                    notifySpeechStart();
                    break;
                }

                if (! considerNegativeRatios)
                {
                    break;
                }

                negativeVoicedCount = 0;

                for (int i = 0, p = voicedHistoryPos; i < endOfSpeechHistoryFrameCount;
                        i++, p = -1 + (p > 0 ? p : voicedHistory.length))
                {
                    if (Voiced.NEGATIVE == voicedHistory[p])
                    {
                        negativeVoicedCount++;
                    }
                }
                logger.trace(message + ", negativeVoicedCount: {} of {}", negativeVoicedCount, endOfSpeechHistoryFrameCount);

                if (negativeVoicedCount >= endOfSpeechHistoryFrameCount - endOfSpeechVoicedFrameCount)
                {
                    logger.debug(message + ", implied EoS detected, frame {}", frameCount);
                    state = State.AFTER;
                    notifySpeechStart();
                    notifySpeechEnd();
                }

                break;

            case DURING:
                if (counter < endOfSpeechHistoryFrameCount)
                {
                    logger.trace(message + ", endOfSpeech history not yet ready, count: {}", counter);
                    counter++;
                    break;
                }

                voicedCount = 0;
                for (int i = 0, p = voicedHistoryPos; i < endOfSpeechHistoryFrameCount;
                        i++, p = -1 + (p > 0 ? p : voicedHistory.length))
                {
                    if (Voiced.POSITIVE == voicedHistory[p])
                    {
                        voicedCount++;
                    }
                }

                logger.trace(message + ", voicedCount: {}/{}", voicedCount, endOfSpeechHistoryFrameCount);

                if (voicedCount < endOfSpeechVoicedFrameCount)
                {
                    logger.debug(message + ", EoS detected, frame {}", frameCount);
                    state = State.AFTER;
                    notifySpeechEnd();
                }
                break;

            case AFTER:
                // Falls through

            default:
                // Nothing to do
                break;
        }

        dispatchChunk(chunk);
    }

    public NoiseLevel getCalculatedBackgroundNoiseLevel()
    {
        return new NoiseLevel(backgroundNoiseMean, backgroundNoiseStdDev);
    }

    NoiseLevel getProvidedBackgroundNoiseLevel()
    {
        return noiseLevel;
    }

    private double getBackgroundNoiseMean()
    {
        if (noiseLevel != null)
        {
            return noiseLevel.getMean();
        } else
        {
            return backgroundNoiseMean;
        }
    }

    private double getBackgroundNoiseStdDev()
    {
        if (noiseLevel != null)
        {
            return noiseLevel.getStandardDeviation();
        } else
        {
            return backgroundNoiseStdDev;
        }
    }

}
