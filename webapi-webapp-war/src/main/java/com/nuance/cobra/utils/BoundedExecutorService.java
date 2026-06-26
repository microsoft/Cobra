package com.nuance.cobra.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class BoundedExecutorService {
    private static final Logger logger = LogManager.getLogger(BoundedExecutorService.class);

    private final ExecutorService exec;
    private final Semaphore semaphore;

    public BoundedExecutorService(int bound) {
        this.exec = Executors.newFixedThreadPool(bound);
        this.semaphore = new Semaphore(bound);
    }

    public void submitTask(final Runnable command)
            throws InterruptedException, RejectedExecutionException {
        //StopWatch stopWatch = new Log4JStopWatch("submitTask");
        semaphore.acquire();
        //stopWatch.stop();
        try {
            exec.execute(() ->
            {
                try {
                    command.run();
                } finally {
                    semaphore.release();
                }
            });
        } catch (RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    public void shutdownAndAwaitTermination() {
        exec.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!exec.awaitTermination(60, TimeUnit.SECONDS)) {
                exec.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!exec.awaitTermination(60, TimeUnit.SECONDS))
                    logger.warn("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            exec.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}