package com.nuance.cobra.utils.audio;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PcmChunkerNodeTest {
    private PcmChunkerNode pcmChunkerNode;
    private PcmChunkerNode pcmChunkerNode2;
    private int sampleRate = 20;
    private int bytesPerSample = 22;

    @Test
    public void init(){
        pcmChunkerNode = new PcmChunkerNode(20);
        pcmChunkerNode2 = new PcmChunkerNode(sampleRate, bytesPerSample);

        assertNotNull(pcmChunkerNode);
        assertNotNull(pcmChunkerNode2);
        pcmChunkerNode.finish();

    }


}