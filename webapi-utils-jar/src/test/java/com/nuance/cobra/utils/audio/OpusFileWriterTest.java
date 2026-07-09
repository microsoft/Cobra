package com.nuance.cobra.utils.audio;

import org.gagravarr.opus.OpusAudioData;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OpusFileWriterTest {

    @Test
    public void writeWithLibrary() {
        OutputStream opusFileStream = new ByteArrayOutputStream();
        OpusFileWriter fileWriter = new OpusFileWriter(opusFileStream);

        List<OpusAudioData> data = new ArrayList<>();
        byte[] opusData = new byte[100];
        Arrays.fill( opusData, (byte) 1 );
        data.add(new OpusAudioData(opusData));
        fileWriter.writeWithLibrary(data);

        assertEquals(287, ((ByteArrayOutputStream) opusFileStream).size()); //basic and a bit stupid check
    }
}