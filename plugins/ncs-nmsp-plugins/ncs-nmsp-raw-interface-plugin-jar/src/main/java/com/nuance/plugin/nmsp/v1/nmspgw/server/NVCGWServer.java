/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.server;

import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers.BCPBeginDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.deserializers.COPConnectDeserializerRegistry;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.NamingThreadFactory;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.SingleThreadExecutionHandlerPool;
import com.nuance.plugin.nmsp.v1.nmspgw.server.utils.NettyHelper;
import com.nuance.plugin.nmsp.v1.nmspgw.server.utils.Quiesce;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author epigeon
 */
public class NVCGWServer {
    public static final int THREAD_POOL_SIZE = 100;
    public static final ScheduledExecutorService gwTimer = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("gw-timer"));
    private static final Logger logger = LogManager.getLogger();
    private final ChannelGroup allAcceptedChannels = new DefaultChannelGroup("nvc-gw-server", GlobalEventExecutor.INSTANCE);
    private final Quiesce quiesce;
    private final AtomicBoolean quiesceStarted = new AtomicBoolean();
    private final IWebApiFeatures webApiFeatures;
    private ServerBootstrap nmspBootstrap;
    private Channel nmspChannel = null;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NVCGWServer(final Quiesce quiesce, final IWebApiFeatures webApiFeatures) {
        this.quiesce = quiesce;
        this.webApiFeatures = webApiFeatures;
    }

    private void initializeNetty() throws IOException {
        if (PdxInterfaceConfig.getPluginConfMap(webApiFeatures.getConfigs()).getBoolean(
                PdxInterfaceConfig.Property.SSL_ENABLED.getKey())) {
            SSLHandlerProvider.initSSLContext(PdxInterfaceConfig.getPluginConfMap(webApiFeatures.getConfigs()));
        }

        SingleThreadExecutionHandlerPool.init(THREAD_POOL_SIZE, logger);

        // Create EventLoopGroups for boss and worker threads
        bossGroup = NettyHelper.createServerBossGroup("gw-nmsp");
        workerGroup = NettyHelper.createServerWorkerGroup("gw-nmsp");

        // Configure the server.
        nmspBootstrap = new ServerBootstrap();
        nmspBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NVCGWServerPipelineFactory(allAcceptedChannels, webApiFeatures))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 4194304) // 4 MB
                .childOption(ChannelOption.SO_SNDBUF, 4194304) // 4 MB
                .option(ChannelOption.SO_REUSEADDR, true);

        // Bind and start to accept incoming connections.
        bindNMSPPort(PdxInterfaceConfig.getPluginConfMap(webApiFeatures.getConfigs()).getInt(
                PdxInterfaceConfig.Property.SERVER_PORT.getKey()));
    }

    private void bindNMSPPort(int port) {
        nmspChannel = bindPort(nmspBootstrap, nmspChannel, port);
    }

    private Channel bindPort(final ServerBootstrap serverBootstrap, final Channel channel, final int port) {
        if (serverBootstrap != null) {
            try {
                return serverBootstrap.bind(new InetSocketAddress(port)).sync().channel();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while binding to port {}", port, e);
                throw new RuntimeException("Failed to bind to port " + port, e);
            }
        }
        return channel;
    }

    private void onNMMAfterApplicationStartedEvent() throws IOException {
        initializeDecoders();
        initializeNetty();
    }

    private void initializeDecoders() {
        BCPBeginDeserializerRegistry.findBCPBeginDeserializerResource();
        COPConnectDeserializerRegistry.findCOPConnectDeserializerResource();
    }

    private void onNMMBeforeApplicationStoppedEvent() {
        if (quiesceStarted.compareAndSet(false, true)) {
            quiesce.startQuiesceAndAwait(bossGroup, workerGroup, nmspChannel, allAcceptedChannels, gwTimer);

            logger.info("NVCGWServer - gwTimer.shutdown");
            gwTimer.shutdown();
            logger.info("NVCGWServer - gwTimer.shutdown - completed");
        } else {
            logger.warn("NVCGWServer - onNMMBeforeApplicationStoppedEvent already called");
        }
    }

    public void onApplicationEvent(String event) {
        final String eventName = event.getClass().getSimpleName();
        logger.info("NVCGWServer - processing {}", eventName);

        try {
            if (event.equalsIgnoreCase("Start")) {
                onNMMAfterApplicationStartedEvent();
            } else if (event.equalsIgnoreCase("Stop")) {
                onNMMBeforeApplicationStoppedEvent();
            }
        } catch (IOException e) {
            logger.error("Failed to initialize netty server.", e);
        }

        logger.info("NVCGWServer - processing {} - completed", eventName);
    }
}
