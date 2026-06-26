/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.server.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Pool of single thread Event Executor Groups.
 * Used in Netty's Pipeline to make sure execution
 * in a session is only handled by one thread.
 *
 * @author epigeon
 */
public class SingleThreadExecutionHandlerPool {
    private static final int defaultPoolSize = 100;
    private static final Vector<EventExecutorGroup> pool = new Vector<EventExecutorGroup>();
    private static int poolSize = 0;
    private static int index = 0;
    private static boolean initialized = false;

    private SingleThreadExecutionHandlerPool() {
    }

    /**
     * Creates the pool.
     *
     * @param size
     * @param logger
     */
    public static synchronized void init(int size, final Logger logger) {
        if (initialized) {
            logger.warn("pool already initialized");
        } else {
            initialized = true;
            if (size >= 0) {
                poolSize = size;
            }
            final UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    logger.error(String.format("uncaughtException in thread '%s': %s", t, e), e);
                }
            };
            final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("gw-exec-hdlr-%s").setDaemon(true).setUncaughtExceptionHandler(uncaughtExceptionHandler).build();

            for (int i = 0; i < poolSize; i++) {
                // Create single-threaded event executor group (equivalent to single thread executor)
                pool.add(new DefaultEventExecutorGroup(1, threadFactory));
            }
        }
    }

    public static synchronized EventExecutorGroup getHandler(Log logger) {
        return getHandler(LogManager.getLogger(SingleThreadExecutionHandlerPool.class));
    }

    public static synchronized EventExecutorGroup getHandler(Logger logger) {
        if (!initialized) {
            init(defaultPoolSize, logger);
        }
        index++;
        if (index >= poolSize) {
            index = 0;
        }
        return pool.get(index);
    }

    public static String checkThreadHealth(Logger logger) {
        ThreadCallable callable = new ThreadCallable();
        for (EventExecutorGroup handler : pool) {
            try {
                handler.submit(callable).get(1000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warn("Thread health status failed: " + e.toString(), e);
                if(e instanceof InterruptedException) Thread.currentThread().interrupt();
                return "TIMEDOUT";
            }
        }
        return "OK";
    }

    public static int checkThreadsHealth(Logger logger) {
        int blockedThreads = 0;
        ThreadCallable callable = new ThreadCallable();
        for (EventExecutorGroup handler : pool) {
            try {
                handler.submit(callable).get(1000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warn("Thread health status failed: " + e.toString(), e);
                if(e instanceof InterruptedException) Thread.currentThread().interrupt();
                blockedThreads++;
            }
        }
        logger.warn("pool.size() " + pool.size() + "blockedThreads " + blockedThreads);
        return (100 * (pool.size() - blockedThreads)) / pool.size();
    }

    public static void releaseExternalResources() {
        for (EventExecutorGroup handler : pool) {
            handler.shutdownGracefully(0, 5, TimeUnit.SECONDS);
        }
    }

    private static class ThreadCallable implements Callable<String> {
        @Override
        public String call() throws Exception {
            return "OK";
        }
    }
}
