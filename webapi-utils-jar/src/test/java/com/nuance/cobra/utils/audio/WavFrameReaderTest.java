package com.nuance.cobra.utils.audio;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AudioSystem.class)
public class WavFrameReaderTest{
    private WavFrameReader testReader;
    private InputStream testStream;
    private AudioInputStream mockAudioInputStream = mock(AudioInputStream.class);

    @Before
    public void setUp() throws IOException, UnsupportedAudioFileException {
        testStream = mock(InputStream.class);
        PowerMockito.mockStatic(AudioSystem.class);
        PowerMockito.when(AudioSystem.getAudioInputStream(testStream)).thenReturn(mockAudioInputStream);
        AudioFormat testFormat = new AudioFormat(0, 0, 0, true, false);
        when(mockAudioInputStream.getFormat()).thenReturn(testFormat);

        testReader = new WavFrameReader(testStream);

        assertNotNull(testReader);
        assertNotNull(mockAudioInputStream.getFormat());
    }

    @Test
    public void getNextFrame() throws IOException {
        assertNotNull(testReader.getNextFrame());
    }

    @Test
    public void hasNextFrame() throws IOException {
        when(testStream.available()).thenReturn(0);
        assertFalse(testReader.hasNextFrame());
    }

    @Test(expected = IOException.class)
    public void testUnsupported() throws IOException, UnsupportedAudioFileException {
        testStream = mock(InputStream.class);
        PowerMockito.mockStatic(AudioSystem.class);
        PowerMockito.when(AudioSystem.getAudioInputStream(testStream)).thenThrow(new UnsupportedAudioFileException());
//        AudioFormat testFormat = new AudioFormat(0, 0, 0, true, false);
//        when(mockAudioInputStream.getFormat()).thenReturn(testFormat);
        testReader = new WavFrameReader(testStream);
    }
}