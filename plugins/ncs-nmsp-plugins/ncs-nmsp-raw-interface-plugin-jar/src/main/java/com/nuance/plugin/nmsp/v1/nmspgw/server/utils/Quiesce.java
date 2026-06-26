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

import io.netty.channel.EventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author epigeon
 */
public class Quiesce {
    private static final Logger logger = LogManager.getLogger();

    private final int quiesceMaxTimeSecs;
    private final int quiesceCheckIntervalsSecs;

    public Quiesce(final int quiesceMaxTimeSecs, final int quiesceCheckIntervalsSecs) {
        this.quiesceMaxTimeSecs = quiesceMaxTimeSecs;
        this.quiesceCheckIntervalsSecs = quiesceCheckIntervalsSecs;
    }

    public void startQuiesceAndAwait(final EventLoopGroup bossGroup, final EventLoopGroup workerGroup,
                                     final Channel nmspChannel, final ChannelGroup allAcceptedChannels,
                                     final ScheduledExecutorService scheduledExecutorService) {
        logger.info("quiesceMaxTimeSecs = {}, quiesceCheckIntervalsSecs = {}", quiesceMaxTimeSecs, quiesceCheckIntervalsSecs);
        final QuiesceTimerTask quiesceTimerTask =
                new QuiesceTimerTask(bossGroup, workerGroup, nmspChannel, allAcceptedChannels, 1 + (quiesceMaxTimeSecs / quiesceCheckIntervalsSecs));
        final ScheduledFuture<?> quiesceFuture = scheduledExecutorService.scheduleAtFixedRate(quiesceTimerTask, 0, quiesceCheckIntervalsSecs, TimeUnit.SECONDS);
        try {
            while (!quiesceTimerTask.completed()) {
                quiesceTimerTask.waitForCompletion(quiesceMaxTimeSecs);
            }
        } catch (final InterruptedException e) {
            logger.error("Quiesce interrupted", e);
            Thread.currentThread().interrupt();
        }
        if (!quiesceTimerTask.completed()) {
            logger.error("Aborting Quiesce");
        }
        quiesceFuture.cancel(false);
    }
}
