package com.nuance.cobra.utils.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;

public class LawChunkerNode
        extends PcmChunkerNode
{
    private static final Logger logger = LogManager.getLogger(LawChunkerNode.class);
    private AudioFormat.Encoding encoding;

    public LawChunkerNode(AudioFormat.Encoding encoding)
    {
        //We will be writing PCM
        super(8000, 2);
        this.encoding = encoding;
    }

    @Override
    public void onAudio(byte[] bytes) {
        //logger.error("Raw bytes = {}", Hex.encodeHexString(bytes));
        byte[] pcmBytes = LawToPcm.toPcm(bytes, encoding);
        super.onAudio(pcmBytes);
        //logger.error("Converted bytes = {}", Hex.encodeHexString(pcmBytes));
    }
}
