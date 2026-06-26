package com.nuance.cobra.utils.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AdaptiveSpeechDetectorNode extends SpeechDetector
{
    private static final Logger logger = LogManager.getLogger(AdaptiveSpeechDetectorNode.class);

    private final int backgroundNoiseFrames;
    private final int startOfSpeechVoicedFrameCount;
    private final int endOfSpeechVoicedFrameCount;
    private final double thresholdRatio;
    private final int beginDelayFrames;
    private final int startOfSpeechHistoryFrameCount;
    private final int endOfSpeechHistoryFrameCount;
    private final NoiseLevel noiseLevel;
    private final double noiseLevelStdevFactor;

    private int beginDelayCount = 0;

    private int counter;

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
        //NO_OP for now
    }

    private static class Tuple
    {
        final double energy;
        final double smoothedAverage;
        final double smoothedMedian;

        boolean voiced;

        Tuple(double energy, double smoothedAverage, double smoothedMedian)
        {
            this.energy = energy;
            this.smoothedAverage = smoothedAverage;
            this.smoothedMedian = smoothedMedian;
        }
    }

    private final ArrayList<Tuple> history = new ArrayList<>();

    private static final int SMOOTHING_LENGTH = 5; // Must be at least 2
    private final double[] smoothingBuffer = new double[SMOOTHING_LENGTH];

    private int absoluteFrameCount = 0;
    private int frameCount = 0;

    public AdaptiveSpeechDetectorNode(int backgroundNoiseFrames, int startOfSpeechVoicedFrameCount, int startOfSpeechHistoryFrameCount,
            int endOfSpeechVoicedFrameCount, int endOfSpeechHistoryFrameCount, double thresholdRatio,
            NoiseLevel noiseLevel, List<AudioNodeObserver> observer)
    {
        addObserver(observer);

        this.backgroundNoiseFrames = backgroundNoiseFrames;

        this.startOfSpeechVoicedFrameCount = startOfSpeechVoicedFrameCount;
        this.endOfSpeechVoicedFrameCount = endOfSpeechVoicedFrameCount;
        this.thresholdRatio = thresholdRatio;

        this.beginDelayFrames = 1; // FIXME not hard coded
        this.startOfSpeechHistoryFrameCount = startOfSpeechHistoryFrameCount;
        this.endOfSpeechHistoryFrameCount = endOfSpeechHistoryFrameCount;

        this.noiseLevel = noiseLevel;
        if (null != noiseLevel)
        {
            logger.trace("Using provided noiseLevel mean: {}, stdDev: {}", noiseLevel.getMean(), noiseLevel.getStandardDeviation());
            noiseLevelStdevFactor = 1.0 / noiseLevel.getStandardDeviation();
        }
        else
        {
            noiseLevelStdevFactor = 0.0;
        }

    }


    // 0: Average
    // 1: Stdev
    static
    double[] computeRecentStats(ArrayList<Tuple> tuples, int begin, int count)
    {
        double mean = 0.0;
        double m2 = 0.0;
        for (int i = 0; i < count; i++)
        {
            final int bi = begin + i;
            final Tuple tuple = tuples.get(bi);
            final double energy = tuple.energy;
            final double delta = energy - mean;
            mean += delta / count;
            m2 += delta * (energy - mean);
        }

        final double[] ret = new double[2];

        ret[0] = mean;
        ret[1] = Math.sqrt(m2 / (count - 1));

        return ret;
    }

    Tuple createTuple(double energy)
    {
        double sum = energy;

        smoothingBuffer[0] = energy;

        final int count = Math.min(1 + history.size(), SMOOTHING_LENGTH);
        for (int c = 1, i = history.size()-1; c < count; c++, i--)
        {
            final Tuple t = history.get(i);
            double e = t.energy;
            sum += e;

            int j;
            for (j = 0; j < c; j++)
            {
                if (e < smoothingBuffer[j])
                {
                    final double tmp = smoothingBuffer[j];
                    smoothingBuffer[j] = e;
                    e = tmp;
                }
            }
            smoothingBuffer[j] = e;
        }


        final double average = sum / count;
        final double median = smoothingBuffer[count >> 1];

        return new Tuple(energy, average, median);
    }

    boolean isVoiced(Tuple tuple)
    {
        final double average = null != noiseLevel ? noiseLevel.getMean() : lowSequenceAverage;
        final double stdevFactor = null != noiseLevel ? noiseLevelStdevFactor : lowSequenceStdevFactor;

        final double smoothedAverageRatio = (tuple.smoothedAverage - average) * stdevFactor;
        final double smoothedMedianRatio = (tuple.smoothedMedian - average) * stdevFactor;

        return smoothedAverageRatio >= thresholdRatio && smoothedMedianRatio >= thresholdRatio;
    }


    private double lowSequenceAverage = Double.MAX_VALUE;
    private double lowSequenceStdev = 0.0;
    private double lowSequenceStdevFactor = 0.0;
    private double maxEnergy = 0.0;

    public void onAudio(AudioChunk chunk)
    {
        if (absoluteFrameCount == 0) {
            notifyInputStart();
        }

        absoluteFrameCount += 1;
        final double energy = chunk.getEnergy();
        String message = String.format("onAudio frame %d energy %f -- ", absoluteFrameCount, energy);

        if (beginDelayCount < beginDelayFrames)
        {
            beginDelayCount += 1;
            logger.trace(message + "ignoring begin-delay frame {}/{}", beginDelayCount, beginDelayFrames);

            dispatchChunk(chunk);
            return;
        }

        if (frameCount == 0 && energy <= 0.001)
        {
            logger.trace(message + "ignoring initial no-energy frame");


            dispatchChunk(chunk);
            return;
        }

        frameCount += 1;
        final Tuple tuple = createTuple(energy);
        history.add(tuple);

        if (frameCount < backgroundNoiseFrames)
        {
            logger.trace(message + "initial background-noise frame {}/{}", frameCount, backgroundNoiseFrames);

            dispatchChunk(chunk);
            return;
        }

        final int compareSequenceStart = frameCount - backgroundNoiseFrames;
        final double[] compareResult = computeRecentStats(history, compareSequenceStart, backgroundNoiseFrames);

        final boolean lowSequenceChanged = lowSequenceAverage > compareResult[0];
        if (lowSequenceChanged)
        {
            lowSequenceAverage = compareResult[0];
            lowSequenceStdev = compareResult[1];
            lowSequenceStdevFactor = 1.0 / lowSequenceStdev;

            notifyNoiseLevel();
            for (final Tuple t : history)
            {
                t.voiced = isVoiced(t);
            }
        }
        else
        {
            tuple.voiced = isVoiced(tuple);
        }

        maxEnergy = Math.max(maxEnergy, energy);
        final double maxEnergyThreshold = (maxEnergy - lowSequenceAverage) * lowSequenceStdevFactor;

        if (maxEnergyThreshold < thresholdRatio)
        {
            logger.trace(message + "observed energy range has not met threshold {}/{}", maxEnergyThreshold, thresholdRatio);

            dispatchChunk(chunk);
            return;
        }

        int voicedCount;
        switch (state)
        {
            case BEFORE:

                if (! lowSequenceChanged)
                {
                    voicedCount = 0;
                    for (int i = 0, p = history.size() - 1; i < startOfSpeechHistoryFrameCount && p >= 0;
                            i += 1, p -= 1)
                    {
                        if (history.get(p).voiced)
                        {
                            voicedCount++;
                        }
                    }
                    logger.trace("onAudio -- state: {}, voicedCount: {} of {}", state, voicedCount,
                            startOfSpeechHistoryFrameCount);

                    logger.trace("onAudio -- voicedCount : {}, startOfSpeechVoicedFrameCount: {}", voicedCount, startOfSpeechVoicedFrameCount);

                    if (voicedCount >= startOfSpeechVoicedFrameCount)
                    {
                        logger.debug(message + ", SoS detected. voicedCount: {} , speechVoicedFrame: {}", voicedCount, startOfSpeechVoicedFrameCount);
                        counter = 0;
                        state = State.DURING;
                        notifySpeechStart();
                    }

                    break;
                }
                else
                {
                    // Re-evaluate entire history
                    logger.trace("onAudio -- state: {}, low sequence changed, re-evaluating entire history for SoS", state);

                    //TODO: use index trickery instead of array list
                    final ArrayList<Tuple> considering = new ArrayList<>(startOfSpeechHistoryFrameCount);
                    voicedCount = 0;
                    int index;
                    for (index = 0; index < history.size(); index++)
                    {
                        if (considering.size() == startOfSpeechHistoryFrameCount)
                        {
                            final Tuple r = considering.remove(0);
                            if (r.voiced)
                            {
                                voicedCount -= 1;
                            }
                        }

                        final Tuple t = history.get(index);
                        considering.add(t);

                        if (t.voiced)
                        {
                            voicedCount += 1;
                        }

                        if (voicedCount >= startOfSpeechVoicedFrameCount)
                        {
                            logger.debug(message + ", SoS detected at frame {} while re-evaluating history", index);

                            counter = 0;
                            state = State.DURING;
                            notifySpeechStart();

                            for (; index < history.size(); index++)
                            {
                                if (counter < endOfSpeechHistoryFrameCount)
                                {
                                    counter++;
                                }
                            }

                            break; // the for loop
                        }
                    }
                }

                // Fall through to process for EoS

            case DURING:
                if (counter < endOfSpeechHistoryFrameCount)
                {
                    logger.trace("onAudio -- state: {} endOfSpeech history not yet ready, count: {}", state, counter);
                    counter++;
                    break;
                }

                voicedCount = 0;
                for (int i = 0, p = history.size() - 1; i < endOfSpeechHistoryFrameCount;
                        i += 1, p -= 1)
                {
                    if (history.get(p).voiced)
                    {
                        voicedCount++;
                    }
                }

                logger.trace("onAudio -- state: {}, voicedCount: {} of {}", state, voicedCount, endOfSpeechHistoryFrameCount);

                if (voicedCount < endOfSpeechVoicedFrameCount)
                {
                    logger.debug(message + ", EoS detected");

                    state = State.AFTER;
                    notifySpeechEnd();
                }
                break;

            case AFTER:
                // Nothing to do
                break;

            default:
                // Should never happen
                break;
        }


        dispatchChunk(chunk);
    }

    public NoiseLevel getCalculatedBackgroundNoiseLevel()
    {
        return new NoiseLevel(lowSequenceAverage, lowSequenceStdev);
    }


}
