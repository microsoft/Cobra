package com.nuance.cobra.utils.audio;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import org.concentus.OpusPacketInfo;

import java.util.Arrays;

public class OpusChunkerNode extends PcmChunkerNode
{
    private static final Logger logger = LogManager.getLogger(OpusChunkerNode.class);

    private static final int NUM_CHANNELS = 1;
    private static final int MAXIMUM_FRAME_DURATION_MS = 120; // From Opus documentation

    private final OpusDecoder opusDecoder;
    private final int frameSize;
    private final byte[] decodeBuffer;


    public OpusChunkerNode(int sampleRate) throws OpusException
    {
        super(sampleRate);

        opusDecoder = new OpusDecoder(sampleRate, NUM_CHANNELS);

        frameSize = MAXIMUM_FRAME_DURATION_MS * sampleRate / 1000;
        final int decodeBufferSize = frameSize << 1;
        decodeBuffer = new byte[decodeBufferSize];
    }


    @Override
    public void onAudio(byte[] bytes)
    {
        if (null == opusDecoder)
        {
            return;
        }

        try
        {
            final OpusPacketInfo opusPacketInfo = OpusPacketInfo.parseOpusPacket(bytes, 0, bytes.length);

            int inOffset = 0;
            for (final Byte[] frame : opusPacketInfo.Frames)
            {
                final int decodedSampleCount = opusDecoder.decode(bytes, inOffset, bytes.length, decodeBuffer, 0, frameSize, false);
                inOffset += frame.length;

                super.onAudio(Arrays.copyOfRange(decodeBuffer,0, decodedSampleCount << 1));

            }

        }
        catch (Exception e)
        {
            logger.warn("OpusException decoding packet. Bytes dropped: {}", bytes.length, e);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
    }

}
