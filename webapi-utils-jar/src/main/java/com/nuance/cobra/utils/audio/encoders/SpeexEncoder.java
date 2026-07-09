package com.nuance.cobra.utils.audio.encoders;

import java.util.Arrays;

public class SpeexEncoder implements IAudioEncoder {

    public static final int NARROWBAND = 0;
    public static final int WIDEBAND = 1;

    private final org.xiph.speex.SpeexEncoder speexEncoderEngine = new org.xiph.speex.SpeexEncoder();

    /** Defines the number of channels of the audio input (1=mono, 2=stereo). */
    private int channels   = 1;
    /** Defines the number of frames per Speex packet. */
    private int nFrames;

    private final int pcmPacketSize; // in bytes

    private byte[] pcmRemainder = new byte[0]; // used to store leftover pcm bytes

    public SpeexEncoder(int mode, int quality, int samplingRate) {
        speexEncoderEngine.init(mode, quality, samplingRate, channels);

        pcmPacketSize = 2 * channels * speexEncoderEngine.getFrameSize(); // getFrameSize() is the number of pcm samples per pcm frame, each pcm sample is 2 bytes
    }

    /**
     * @param pcmInput Any number of PCM audio bytes.
     * @return A Speex packet containing a number of Speex frames.
     */
    public byte[] encode(byte[] pcmInput) {
        byte[] pcmBytes = new byte[pcmRemainder.length + pcmInput.length];
        System.arraycopy(pcmRemainder, 0, pcmBytes, 0, pcmRemainder.length);
        System.arraycopy(pcmInput, 0, pcmBytes, pcmRemainder.length, pcmInput.length);
        pcmRemainder = new byte[0];

        int nbPcmPackets = pcmBytes.length / pcmPacketSize;
        int remainder = pcmBytes.length % pcmPacketSize;

        int beginIndex = 0;
        int endIndex = nbPcmPackets * pcmPacketSize;

        byte[] pcmChunk = Arrays.copyOfRange(pcmBytes, beginIndex, endIndex);
        byte[] speexChunk = encodeToSpeex(pcmChunk);

        // the remainder can be thrown away without losing speex output quality (can't hear the difference) but let's still keep it
        if (remainder > 0) {
            beginIndex = nbPcmPackets * pcmPacketSize;
            endIndex = beginIndex + remainder;

            pcmRemainder = Arrays.copyOfRange(pcmBytes, beginIndex, endIndex);
        }

        return speexChunk;
    }

    @Override
    public void onDone() {

    }

    private byte[] encodeToSpeex(byte[] pcmChunk) {
        nFrames = pcmChunk.length / pcmPacketSize;

        for (int i = 0; i < nFrames; i++) {
            speexEncoderEngine.processData(pcmChunk, i * pcmPacketSize, pcmPacketSize);
        }

        byte[] speexChunk = new byte[pcmChunk.length];
        int encsize = speexEncoderEngine.getProcessedData(speexChunk, 0);
        if (encsize > 0) {
            return Arrays.copyOfRange(speexChunk, 0, encsize);
        }
        return new byte[0];
    }
}
