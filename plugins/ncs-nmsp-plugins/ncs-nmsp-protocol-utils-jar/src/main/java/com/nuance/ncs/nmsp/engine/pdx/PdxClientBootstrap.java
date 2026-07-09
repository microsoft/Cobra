package com.nuance.ncs.nmsp.engine.pdx;

import io.netty.channel.nio.NioEventLoopGroup;

/** Shared NioEventLoopGroup factory - one group per JVM */
public final class PdxClientBootstrap {
    private static final NioEventLoopGroup WORKER_GROUP = new NioEventLoopGroup(4);

    private PdxClientBootstrap() {}

    public static NioEventLoopGroup workerGroup() {
        return WORKER_GROUP;
    }
}
