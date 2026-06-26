package com.nuance.cobra.utils.audio;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeoutNodeTest {
    private long timeoutMs = (long)(Math.random()*1000);
    private int sampleRate = 5;
    private String timerID = "Expected ID";
    private AudioNodeObserver mockedObserver = mock(AudioNodeObserver.class);
    private List<AudioNodeObserver> observer = new ArrayList<>();
    private TimeoutNode timeoutNode;

    @Before
    public void setUp(){
        timeoutNode = new TimeoutNode(timeoutMs, sampleRate, observer, true);
    }

    @Test
    public void init(){
        observer.add(mockedObserver);
        TimeoutNode timeoutNode1 = new TimeoutNode(timeoutMs, sampleRate, timerID, observer, true);

        assertEquals("DEFAULT_ID", timeoutNode.getTimerID());
        assertEquals("Expected ID", timeoutNode1.getTimerID());

    }


    @Test
    public void onAudioTest(){
        when(mock(AudioChunk.class).getOffset()).thenReturn(Long.MAX_VALUE);

        assertNotNull(timeoutNode);

        timeoutNode.onAudio(mock(AudioChunk.class));

        timeoutNode.onEvent(mock(AudioNode.class), AudioNodeObserver.NodeEvent.TIMERS_CANCEL);

        timeoutNode.onEvent(mock(AudioNode.class), AudioNodeObserver.NodeEvent.TIMERS_START);
    }
}