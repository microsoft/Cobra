package com.nuance.cobra.utils.audio;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.assertNotNull;

public class LawToPcmTest {

    @Test
    public void toPcMTest(){
        byte [] testBytes = {0, 1, 2, 3, 4};
        LawToPcm.toPcm(testBytes, AudioFormat.Encoding.ULAW);

        assertNotNull(LawToPcm.toPcm(testBytes, AudioFormat.Encoding.ULAW));
        assert(LawToPcm.toPcm(testBytes, AudioFormat.Encoding.ULAW) instanceof byte[]);
    }
}