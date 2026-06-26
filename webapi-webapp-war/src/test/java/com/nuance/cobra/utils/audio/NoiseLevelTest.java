package com.nuance.cobra.utils.audio;

import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.testing.LoggingTest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggingTest.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class NoiseLevelTest extends CobraTest
{
    double mean = 55.5555;
    double stdDev = 11.1111;

    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();
    }

    @After
    public void tearDown()
            throws Exception
    {
    }

    @Test
    public void noiseLevelValidator()
            throws Exception
    {
        Map<String, Number> noiseLevelMap = new LinkedHashMap<>();
        noiseLevelMap.put("mean", mean);
        noiseLevelMap.put("standardDeviation", stdDev);
        NoiseLevel noiseLevel = NoiseLevel.noiseLevelValidator(noiseLevelMap, "noiseLevel");

        assertEquals(55.55, noiseLevel.getMean(), 0);
        assertEquals(11.11, noiseLevel.getStandardDeviation(), 0);
        noiseLevel.setMean(22.22);
        noiseLevel.setStandardDeviation(33.33);
        assertEquals(22.22, noiseLevel.getMean(), 0);
        assertEquals(33.33, noiseLevel.getStandardDeviation(), 0);
        assertEquals(22.22, NoiseLevel.noiseLevelToMap(noiseLevel).get("mean"));
        assertEquals(33.33, NoiseLevel.noiseLevelToMap(noiseLevel).get("standardDeviation"));
    }

    @Test
    public void noiseLevelJsonObjectToMapValidator(){
        JSONObject json = new JSONObject();
        json.put("mean", 22.22);
        json.put("standardDeviation", 33.33);
        assertEquals(22.22, NoiseLevel.noiseLevelJsonObjectToMap(json).get("mean"));
        assertEquals(33.33, NoiseLevel.noiseLevelJsonObjectToMap(json).get("standardDeviation"));
        NoiseLevel.noiseLevelValidator(json,"noiseLevel");

    }

    @Test(expected = IllegalArgumentException.class)
    public void noiseLevelNullMapValidator()
            throws Exception
    {
        Map<String, Number> noiseLevelMap = null;
        NoiseLevel.noiseLevelValidator(noiseLevelMap, "noiseLevel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapToNoiseNotNullMapValidator()
    throws Exception
    {
        Map<String, Number> noiseLevelMap = new HashMap<String, Number>();
        noiseLevelMap.put("mean",0);
        noiseLevelMap.put("standardDeviation",-1);
        NoiseLevel.mapToNoiseLevel(noiseLevelMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noiseLevelEmptyMapValidator()
            throws Exception
    {
        Map<String, Number> noiseLevelMap = new LinkedHashMap<>();
        NoiseLevel.noiseLevelValidator(noiseLevelMap, "noiseLevel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noiseLevelBogusMapValidator()
            throws Exception
    {
        Map<String, Number> noiseLevelMap = new LinkedHashMap<>();
        noiseLevelMap.put("bogus", 80615);
        NoiseLevel.noiseLevelValidator(noiseLevelMap, "noiseLevel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noiseLevelHalfEmptyMapValidator()
            throws Exception
    {
        Map<String, Number> noiseLevelMap = new LinkedHashMap<>();
        noiseLevelMap.put("mean", 55.5555);
        NoiseLevel.noiseLevelValidator(noiseLevelMap, "noiseLevel");
    }
}