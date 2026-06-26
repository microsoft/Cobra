package com.nuance.plugin.nmsp.v1.nmspgw.server.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyHelper {
    private static final Logger logger = LogManager.getLogger();
    public static final int DEFAULT_WORKER_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    public NettyHelper() {
    }

    /**
     * Creates an EventLoopGroup for server boss threads (accepts connections)
     */
    public static EventLoopGroup createServerBossGroup(String threadNamePrefix) {
        return new NioEventLoopGroup(1, daemonThreadFactory(makeBossFormat(threadNamePrefix)));
    }

    /**
     * Creates an EventLoopGroup for server worker threads (handles I/O)
     */
    public static EventLoopGroup createServerWorkerGroup(String threadNamePrefix) {
        return new NioEventLoopGroup(DEFAULT_WORKER_THREAD_COUNT, daemonThreadFactory(makeWrkrFormat(threadNamePrefix)));
    }

    /**
     * Creates an EventLoopGroup for client connections
     */
    public static EventLoopGroup createClientEventLoopGroup(String threadNamePrefix, int threadCount) {
        return new NioEventLoopGroup(threadCount, daemonThreadFactory(makeWrkrFormat(threadNamePrefix)));
    }

    /**
     * Creates an EventLoopGroup for client connections with default thread count
     */
    public static EventLoopGroup createClientEventLoopGroup(String threadNamePrefix) {
        return createClientEventLoopGroup(threadNamePrefix, DEFAULT_WORKER_THREAD_COUNT);
    }

    /**
     * Creates write buffer water mark for flow control
     */
    public static WriteBufferWaterMark createWriteBufferWaterMark(int writeBufferHighWaterMark) {
        if (writeBufferHighWaterMark > 0) {
            logger.trace("createWriteBufferWaterMark high={}", writeBufferHighWaterMark);
            return new WriteBufferWaterMark(writeBufferHighWaterMark / 2, writeBufferHighWaterMark);
        }
        return WriteBufferWaterMark.DEFAULT;
    }

    public static HashedWheelTimer createHashedWheelTimer(String threadNamePrefix) {
        ThreadFactory threadFactory = daemonThreadFactory(makeTimerFormat(threadNamePrefix));
        return new HashedWheelTimer(threadFactory, 100L, TimeUnit.MILLISECONDS, 512);
    }

    private static ThreadFactory daemonThreadFactory(String nameFormat) {
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build();
    }

    private static String makeTimerFormat(String threadNamePrefix) {
        return threadNamePrefix + "-netty-timer-%s";
    }

    private static String makeWrkrFormat(String threadNamePrefix) {
        return threadNamePrefix + "-netty-wrkr-%s";
    }

    private static String makeBossFormat(String threadNamePrefix) {
        return threadNamePrefix + "-netty-boss-%s";
    }
}

