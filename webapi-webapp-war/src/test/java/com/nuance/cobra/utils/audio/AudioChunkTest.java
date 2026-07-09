package com.nuance.cobra.utils.audio;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class AudioChunkTest
{
    private AudioChunk audioChunk;
    private long testOffset = 10;
    private byte[] testBytes = new byte[100];

    @Before
    public void setUp()
    {
        Arrays.fill(testBytes, (byte) 1);
        audioChunk = new AudioChunk(testOffset, testBytes);
    }

    @Test
    public void test_getOffset()
    {
        long testOffset = audioChunk.getOffset();
        Assert.assertTrue(testOffset == this.testOffset);
    }

    @Test
    public void test_getBytes()
    {
        MatcherAssert.assertThat(this.testBytes, IsEqual.equalTo(audioChunk.getBytes()));
    }

    @Test
    public void test_getEnergy_zero()
    {
        final byte[] bytes = new byte[] { (byte)0x00, (byte)0x00 };
        final AudioChunk chunk = new AudioChunk(testOffset, bytes);
        final double energy = chunk.getEnergy();
        Assert.assertTrue(energy == 0.0);
    }

    @Test
    public void test_getEnergy_one()
    {
        final byte[] bytes = new byte[] { (byte)0x01, (byte)0x00 };
        final AudioChunk chunk = new AudioChunk(testOffset, bytes);
        final double energy = chunk.getEnergy();
        Assert.assertTrue(energy == 1.0);
    }

    @Test
    public void test_getEnergy_negativeOne()
    {
        final byte[] bytes = new byte[] { (byte)0xff, (byte)0xff };
        final AudioChunk chunk = new AudioChunk(testOffset, bytes);
        final double energy = chunk.getEnergy();
        Assert.assertTrue(energy == 1.0);
    }

    @Test
    public void test_getEnergy_endianness()
    {
        final byte[] bytes = new byte[] { (byte)0x44, (byte)0x88 };
        final AudioChunk chunk = new AudioChunk(testOffset, bytes);
        final double energy = chunk.getEnergy();
        Assert.assertTrue(energy == (double)30652);
    }

    @Test
    public void test_getEnergy_oddByteCount()
    {
        final byte[] bytes = new byte[] { (byte)0x01, (byte)0x00, (byte)0xff };
        final AudioChunk chunk = new AudioChunk(testOffset, bytes);
        final double energy = chunk.getEnergy();
        Assert.assertTrue(energy == 1.0);
    }


}
