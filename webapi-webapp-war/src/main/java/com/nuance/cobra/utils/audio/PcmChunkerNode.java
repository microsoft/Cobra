package com.nuance.cobra.utils.audio;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class PcmChunkerNode extends AudioChunkerNode
{
    private final byte[] buffer;

    private int count = 0;
    private long sampleOffset = 0;

    public PcmChunkerNode(int sampleRate)
    {
        this(sampleRate, 2);
    }


    public PcmChunkerNode(int sampleRate, int bytesPerSample)
    {
        final int frameSampleCount = sampleRate / 50;
        final int frameByteCount = frameSampleCount * bytesPerSample;
        buffer = new byte[frameByteCount];
    }


    @Override
    public void onAudio(byte[] bytes) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        while (byteBuffer.hasRemaining())
        {
            final int bufferRemaining = byteBuffer.remaining();
            final int chunkSpace = buffer.length - count;

            if (bufferRemaining >= chunkSpace)
            {
                byteBuffer.get(buffer, count, chunkSpace);
                count += chunkSpace;

                _finish();
            }
            else
            {
                byteBuffer.get(buffer, count, bufferRemaining);
                count += bufferRemaining;
            }
        }

    }

    @Override
    public void finish()
    {
        super.finish();

        if (0 < count)
        {
            _finish();
        }
    }

    private void _finish()
    {
        final AudioChunk chunk = new AudioChunk(sampleOffset, Arrays.copyOf(buffer, count));
        sampleOffset += count >> 1;
        count = 0;

        dispatchChunk(chunk);
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {

    }
}
