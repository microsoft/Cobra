package com.nuance.cobra.utils.webapi;

import org.apache.logging.log4j.ThreadContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThreadContextRunnableTest {

    @Test
    public void runWithContext() {
        Runnable testRunnable = new ThreadContextRunnable() {
            @Override
            public void runWithContext() {
                assertTrue(ThreadContext.getContext().isEmpty());
            }
        };

        testRunnable.run();
    }

    @Test(expected=IllegalStateException.class)
    public void runWithContextThrowException() {
        Runnable testRunnable = new ThreadContextRunnable() {
            @Override
            public void runWithContext() {
                throw new IllegalStateException("simulated error");
            }
        };

        testRunnable.run();
    }

}