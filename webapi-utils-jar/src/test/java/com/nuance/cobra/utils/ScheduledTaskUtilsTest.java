package com.nuance.cobra.utils;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class ScheduledTaskUtilsTest
{
    @Test(expected = IllegalStateException.class)
    public void noConstruct()
    {
        new ScheduledTaskUtils();
    }

    @Test(expected = Test.None.class)
    public void destroy()
    {
        ScheduledTaskUtils.destroy();
    }

    @Test(expected = Test.None.class)
    public void destroyWithException()
    {
        ScheduledExecutorService ses = mock(ScheduledExecutorService.class);
        doThrow(new SecurityException()).when(ses).shutdown();
        ScheduledTaskUtils.setScheduler(ses);
        ScheduledTaskUtils.destroy();
    }

    @Test(expected = Test.None.class)
    public void scheduleAtFixedRate()
            throws InterruptedException
    {
        CountDownLatch lock = new CountDownLatch(1);

        final Runnable someChoreRunnable = () ->
        {
            lock.countDown();
            throw new NullPointerException();
        };

        ScheduledFuture<?> sf = ScheduledTaskUtils.scheduleAtFixedRate(someChoreRunnable, 0, 10, TimeUnit.MILLISECONDS);
        lock.await(5, TimeUnit.SECONDS);
        sf.cancel(true);
    }
}
