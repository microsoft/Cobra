package com.nuance.cobra.utils.audio;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class DuplicatorNodeTest {
    private DuplicatorNode nodeToTest;

    @Before
    public void setUp() throws Exception {
        nodeToTest = new DuplicatorNode();
    }

    @Test
    public void testConnect(){
        // honestly this test adds line coverage but doesn't really.. check things...

        assertNotNull(nodeToTest);
        nodeToTest.connect(mock(AudioDestination.class));

        List<Object> a = Whitebox.getInternalState(nodeToTest, "destinations");
        assertNotNull(a);
        assertFalse(a.isEmpty());
    }

    @Test
    public void testDispatchChunk(){
        assertNotNull(nodeToTest);

        nodeToTest.connect(mock(AudioDestination.class));

        List<Object> a = Whitebox.getInternalState(nodeToTest, "destinations");
        
        nodeToTest.dispatchChunk(mock(AudioChunk.class));
    }

    @Test
    public void testDispatchFinish(){
        nodeToTest.connect(mock(AudioDestination.class));
        List<Object> a = Whitebox.getInternalState(nodeToTest, "destinations");
        assertFalse(a.isEmpty());
        nodeToTest.dispatchFinish();
    }
}