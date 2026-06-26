package com.nuance.cobra.utils.audio;

import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.testing.LoggingTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggingTest.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class SpeechDetectorNodeTest extends CobraTest {

    private LegacySpeechDetectorNode legacySpeechDetectorNode;
    double mean = 55.5555;
    double stdDev = 11.1111;

    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();

        AudioNodeObserver speechDetectorObserver = mock(AudioNodeObserver.class);

        NoiseLevel noiseLevel = new NoiseLevel(mean, stdDev);
        legacySpeechDetectorNode = new LegacySpeechDetectorNode(10, 7, 15, 5, 50, 4.0, false, noiseLevel, new ArrayList<>());
    }

    @After
    public void tearDown()
            throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testGetBackgroundNoiseLevel()
            throws Exception
    {
        TestUtils.inject(legacySpeechDetectorNode, LegacySpeechDetectorNode.class, "backgroundNoiseMean", mean);
        TestUtils.inject(legacySpeechDetectorNode, LegacySpeechDetectorNode.class, "backgroundNoiseStdDev", stdDev);
        assertEquals(55.55, legacySpeechDetectorNode.getProvidedBackgroundNoiseLevel().getMean(), 0);
        assertEquals(11.11, legacySpeechDetectorNode.getProvidedBackgroundNoiseLevel().getStandardDeviation(), 0);
    }
}
