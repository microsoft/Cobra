/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */
package com.nuance.plugin.nmsp.v1.nmspgw.server.utils;

import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.SingleThreadExecutionHandlerPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author epigeon
 */
public class QuiesceTimerTask implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Channel nmspChannel;
    private final ChannelGroup allAcceptedChannels;
    private final int maximumExecutionCount;

    private int taskExecutionCount = 0;
    private boolean completed;

    public QuiesceTimerTask(
            final EventLoopGroup bossGroup,
            final EventLoopGroup workerGroup,
            final Channel nmpsChannel,
            final ChannelGroup allAcceptedChannels,
            final int maximumExecutionCount) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.nmspChannel = nmpsChannel;
        this.allAcceptedChannels = allAcceptedChannels;
        this.maximumExecutionCount = maximumExecutionCount;
    }

    @Override
    public void run() {
        try {
            if (!completed) {
                taskExecutionCount++;
                final int allChannelCount = allAcceptedChannels.size();
                logger.info("{}: channels count = {}", taskExecutionCount, allChannelCount);
                if (allChannelCount == 0) {
                    closeChannelsReleaseResourcesAndNotifyQuiesceDone();
                } else if (taskExecutionCount >= maximumExecutionCount) {
                    logger.error("quiesceMaxTimeSecs elapsed");
                    ChannelGroupFuture future = allAcceptedChannels.close();
                    final boolean allChannelsClosureSuccessful = future.awaitUninterruptibly(1000, TimeUnit.MILLISECONDS);
                    logger.info(String.format("allChannelsClosureSuccessful %b", allChannelsClosureSuccessful));
                    closeChannelsReleaseResourcesAndNotifyQuiesceDone();
                }
            }
        } catch (Exception e) {
            logger.error("QuiesceTimerTask exception", e);
        }
    }

    private void closeChannelsReleaseResourcesAndNotifyQuiesceDone() {
        try {
            logger.info("closeChannelsReleaseResourcesAndNotifyQuiesceDone");

            final ChannelFuture channelFuture = nmspChannel.close();

            final boolean channelClosureSuccessful = channelFuture.awaitUninterruptibly(1000, TimeUnit.MILLISECONDS);
            logger.info(String.format("channelClosureSuccessful %b", channelClosureSuccessful));

            // Shutdown boss group gracefully
            bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            logger.info("bossGroup.shutdownGracefully - initiated");

            // Shutdown worker group gracefully
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            logger.info("workerGroup.shutdownGracefully - initiated");

            SingleThreadExecutionHandlerPool.releaseExternalResources();
            logger.info("SingleThreadExecutionHandlerPool.releaseExternalResources - completed");
        } finally {
            complete();
            logger.info("closeChannelsReleaseResourcesAndNotifyQuiesceDone - completed");
        }
    }

    synchronized private void complete() {
        completed = true;
        notifyAll();
    }

    synchronized boolean completed() {
        return completed;
    }

    synchronized void waitForCompletion(final int quiesceMaxTimeSecs) throws InterruptedException {
        wait(quiesceMaxTimeSecs * 1000L);
    }
}
