package com.nuance.cobra.utils.audio;

import org.gagravarr.opus.OpusAudioData;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OpusFrameReaderTest {

    @Test
    public void getNextFrame() {
        OutputStream opusFileStream = new ByteArrayOutputStream();
        OpusFileWriter fileWriter = new OpusFileWriter(opusFileStream);

        List<OpusAudioData> data = new ArrayList<>();
        byte[] opusData = new byte[100];
        Arrays.fill( opusData, (byte) 1 );
        data.add(new OpusAudioData(opusData));
        fileWriter.writeWithLibrary(data);

        try {
            byte[] source = ((ByteArrayOutputStream) opusFileStream).toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(source);
            OpusFrameReader opus = new OpusFrameReader(bis);
            assertTrue(opus.getNextFrame().length > 0);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void hasNextFrame() {
        OutputStream opusFileStream = new ByteArrayOutputStream();
        OpusFileWriter fileWriter = new OpusFileWriter(opusFileStream);

        List<OpusAudioData> data = new ArrayList<>();
        byte[] opusData = new byte[100];
        Arrays.fill( opusData, (byte) 1 );
        data.add(new OpusAudioData(opusData));
        fileWriter.writeWithLibrary(data);

        try {
            byte[] source = ((ByteArrayOutputStream) opusFileStream).toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(source);
            OpusFrameReader opus = new OpusFrameReader(bis);
            assertTrue(opus.hasNextFrame());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}