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

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPChannelHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageDecoder;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessageEncoder;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;

import java.util.Optional;

public class NVCGWServerPipelineFactory extends ChannelInitializer<SocketChannel> {
    public static final String SSL_PROVIDER_NAME = "sslProvider";
    public static final String NMSP_MESSAGE_DECODER_NAME = "msgDecoder";
    public static final String NMSP_MESSAGE_ENCODER_NAME = "msgEncoder";
    public static final String NMSP_MESSAGE_HANDLER_NAME = "msgHandler";
    private static final int MAX_MESSAGE_SIZE_BYTES = 153600;
    private static final Optional<Integer> MAX_MESSAGE_SIZE = Optional.of(MAX_MESSAGE_SIZE_BYTES);
    private final NMSPMessageEncoder msgEncoder;
    private final NMSPChannelHandler msgHandler;

    public NVCGWServerPipelineFactory(final ChannelGroup allChannels, final IWebApiFeatures webApiFeatures) {
        msgEncoder = new NMSPMessageEncoder(); // stateless, we can use the same for all channels
        msgHandler = new NMSPChannelHandler(allChannels, webApiFeatures); // stateless, we can use the same for all channels
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        // Add Channel Handlers
        // The singleThreadExecutionHandler will be added once the pipeline is created i.e. once the session is created
        // pipeline.addLast("singleThreadExecutor", NVCGWServer.singleThreadExecutionHandler); // ensures single thread per session
        if (SSLHandlerProvider.isSSLEnabled()) {
            ChannelHandler sslHandler = SSLHandlerProvider.getSSLHandler();
            pipeline.addLast(SSL_PROVIDER_NAME, sslHandler);
        }
        pipeline.addLast(NMSP_MESSAGE_DECODER_NAME, new NMSPMessageDecoder(MAX_MESSAGE_SIZE)); // decodes NMSP message coming from the client
        pipeline.addLast(NMSP_MESSAGE_ENCODER_NAME, msgEncoder); // encodes NMSP messages before sending to the client
        pipeline.addLast(NMSP_MESSAGE_HANDLER_NAME, msgHandler); // handles NMSP messages coming from the client
    }
}
