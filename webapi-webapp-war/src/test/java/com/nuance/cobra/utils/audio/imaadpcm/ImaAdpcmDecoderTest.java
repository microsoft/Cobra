package com.nuance.cobra.utils.audio.imaadpcm;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;

public class ImaAdpcmDecoderTest {

    @Test
    public void decodeToPcm1616kLittleEndian() throws IOException {
        byte[] actualImaAdpcmAudio = readBytesFromFile("./src/test/resources/audio/life.imaadpcm");

        short[] expectedPcmAudio = readShortsFromFile("./src/test/resources/audio/life.pcm1616klittleendian");
        short[] actualPcmAudio = new short[actualImaAdpcmAudio.length * Short.BYTES];

        ImaAdpcmDecoderState decoderState = new ImaAdpcmDecoderState();
        ImaAdpcmDecoder.decodeToPcm1616kLittleEndian(actualImaAdpcmAudio, actualPcmAudio, actualImaAdpcmAudio.length * Short.BYTES, decoderState, 0);

        assertArrayEquals(expectedPcmAudio, actualPcmAudio);
    }

    private byte[] readBytesFromFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    private short[] readShortsFromFile(String filePath) throws IOException {
        ShortBuffer shortBuffer = ByteBuffer.wrap(readBytesFromFile(filePath)).asShortBuffer();

        ArrayList<Short> shortList = new ArrayList<>();
        while (shortBuffer.hasRemaining()) {
            shortList.add(shortBuffer.get());
        }

        short[] shorts = new short[shortList.size()];

        for (int i = 0; i < shortList.size(); i++) {
            shorts[i] = shortList.get(i);
        }
        return shorts;
    }
}
