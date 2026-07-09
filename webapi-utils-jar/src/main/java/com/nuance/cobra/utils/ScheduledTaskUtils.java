package com.nuance.cobra.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"java:S1452", "java:S1181"})
public class ScheduledTaskUtils
{
    private static final Logger logger = LogManager.getLogger(ScheduledTaskUtils.class);

    // Have 10 execution threads in the pool
    // Note that tasks will execute inside the executor threads, not on the thread that submitted the work.
    private static ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10);

    ScheduledTaskUtils() { throw new IllegalStateException("Utility class!"); }

    static void setScheduler(ScheduledExecutorService ses)
    {
        scheduler = ses;
    }

    public static void destroy()
    {
        try
        {
            scheduler.shutdown();
        }
        catch (SecurityException se)
        {
            logger.error("Security Exception occurred while shutting down ScheduledExecutorService", se);
        }
    }

    /**
     * Schedule a task that will execute once.
     * @param task  Runnable to be executed by the scheduler.
     * @param initialDelay Initial delay to wait before the first execution.
     * @param timeUnits The time units. See java TimeUnit.
     */
    public static ScheduledFuture<?> scheduleTask(Runnable task, int initialDelay, TimeUnit timeUnits) {
        return scheduler.schedule(task, initialDelay, timeUnits);
    }

    //See the description in ScheduledExecutorService.
    //Keeps starting a task periodically, regardless of when the previous task ends
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period,
                                                         TimeUnit timeUnits)
    {
        return scheduler.scheduleAtFixedRate(wrapTaskToCatchExceptionsAndErrors(task), initialDelay, period, timeUnits);
    }

    //See the description in ScheduledExecutorService.
    //Run a task periodically, with the specified delay between the end of one task and the start of the next.
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
            TimeUnit timeUnits)
    {
        return scheduler.scheduleWithFixedDelay(wrapTaskToCatchExceptionsAndErrors(task), initialDelay, delay, timeUnits);
    }

    //https://stackoverflow.com/a/24902026/650246
    private static Runnable wrapTaskToCatchExceptionsAndErrors(Runnable task)
    {
        return () ->
        {
            try
            {
                task.run();
            } catch ( Throwable t )
            {
                logger.warn( "Caught exception in ScheduledExecutorService: ", t);
            }
        };
    }

}
