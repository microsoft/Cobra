package com.nuance.plugin.nmsp.v1.nmspgw.server.utils;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.powermock.api.mockito.PowerMockito.mock;

public class QuiesceTest {

    @Test
    public void startQuiesceAndAwait() {
        ChannelGroup testChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        ScheduledExecutorService testScheduler = new ScheduledThreadPoolExecutor(1);
        EventLoopGroup mockBossGroup = mock(EventLoopGroup.class);
        EventLoopGroup mockWorkerGroup = mock(EventLoopGroup.class);
        Channel mockChannel = mock(Channel.class);
        Quiesce testQuiesce = new Quiesce(1, 1);
        testQuiesce.startQuiesceAndAwait(mockBossGroup, mockWorkerGroup, mockChannel, testChannels, testScheduler);
    }
}