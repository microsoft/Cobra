package com.nuance.cobra.utils.audio;

import com.nuance.cobra.utils.audio.imaadpcm.ImaAdpcmDecoder;
import com.nuance.cobra.utils.audio.imaadpcm.ImaAdpcmDecoderState;

import java.nio.ByteBuffer;

public class ImaAdpcmChunkerNode extends PcmChunkerNode
{
    ImaAdpcmDecoderState decoderState = new ImaAdpcmDecoderState();

    public ImaAdpcmChunkerNode(int sampleRate)
    {
        super(sampleRate);
    }

    @Override
    public void onAudio(byte[] imaAdpcmAudioBytes)
    {
        short[] pcmAudioShorts = new short[imaAdpcmAudioBytes.length * Short.BYTES];
        ImaAdpcmDecoder.decodeToPcm1616kLittleEndian(imaAdpcmAudioBytes, pcmAudioShorts, imaAdpcmAudioBytes.length * Short.BYTES, decoderState, 0);

        ByteBuffer pcmAudioBytes = ByteBuffer.allocate(pcmAudioShorts.length * Short.BYTES);

        for (short pcmShort : pcmAudioShorts)
        {
            pcmAudioBytes.putShort(pcmShort);
        }
        super.onAudio(pcmAudioBytes.array());
    }

    @Override
    public void finish()
    {
        super.finish();
    }
}
