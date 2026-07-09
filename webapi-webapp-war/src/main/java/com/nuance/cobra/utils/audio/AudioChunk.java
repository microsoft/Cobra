package com.nuance.cobra.utils.audio;

public class AudioChunk
{
    private final long offset;
    private final byte[] bytes;
    private final double energy;

    public AudioChunk(long offset, byte[] bytes)
    {
        this.offset = offset;
        this.bytes = bytes;
        this.energy = computeEnergy();
    }

    public long getOffset()
    {
        return offset;
    }

    public byte[] getBytes()
    {
        return bytes;
    }

    private double computeEnergy()
    {
        double sum = 0.0;
        int count = 0;

        int i = 0;
        while (i + 1 < bytes.length)
        {
            int l = bytes[i++] & 0xff;
            int h = bytes[i++];
            int s = (h << 8) | l;
            sum += (double)(s * s);
            count += 1;
        }

        if(count == 0){
            return 0;
        }else{
            return Math.sqrt(sum / (double)count);
        }
    }

    public double getEnergy()
    {
        return energy;
    }
}
