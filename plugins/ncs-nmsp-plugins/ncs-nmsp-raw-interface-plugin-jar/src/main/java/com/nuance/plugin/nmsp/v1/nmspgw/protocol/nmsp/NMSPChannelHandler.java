/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

import com.google.common.base.Throwables;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers.BCPMessageHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.handlers.COPMessageHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPDisconnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.handlers.VAPMessageHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.server.NVCGWServerPipelineFactory;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.GWThreadStarter;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.IGWThread;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Handles NMSP messages coming from the client.
 *
 * @author epigeon
 */
@ChannelHandler.Sharable
public class NMSPChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger();
    private static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");

    private static final int MAX_NUMBER_OF_CHANNELS = 100;
    private static final int MAX_NUMBER_OF_CHANNEL_PERCENT = 90;
    private static final boolean SEND_MESSAGE_ON_SOCKET_CONNECT = true;

    private final ChannelGroup allChannels;
    private final IWebApiFeatures webApiFeatures;

    public NMSPChannelHandler(
            final ChannelGroup allChannels, final IWebApiFeatures webApiFeatures) {
        this.allChannels = allChannels;
        this.webApiFeatures = webApiFeatures;
    }

    public static void messageReceived(final NMSPMessage message, final Session session) {
        try {
            if (session != null) {
                logger.debug(">====== Message received: {}", message);
                switch (message.getProtocol()) {
                    case NMSPMessage.COP:
                        session.clientMessageReceived();

                        COPMessageHandler.handleMessage(session, (COPMessage) message);
                        break;
                    case NMSPMessage.BCP:
                        session.clientMessageReceived();

                        // Buffer the messages if the client is not authenticated yet (state CONNECTED)
                        // or if the message queue hasn't been emptied yet
                        final List<BCPMessage> bcpMessageQueue = session.getMessageQueue();
                        final BCPMessage bcpMessage = (BCPMessage) message;
                        if (session.getState() == Session.STATE.CONNECTED && bcpMessageQueue.isEmpty()) {
                            BCPMessageHandler.handleMessage(session, bcpMessage, session.getClientSessionListener());
                        } else {
                            // COP_CONNECT not received yet
                            if (session.getState() == Session.STATE.INIT) {
                                throw new InvalidMessageException("Session in wrong state");
                            } else {
                                bcpMessageQueue.add(bcpMessage);
                            }
                        }
                        break;
                    case NMSPMessage.VAP:
                        session.clientMessageReceived();

                        // Don't buffer VAP messages, they will be buffered in the MGW
                        VAPMessageHandler.handleMessage(session, (VAPMessage) message);
                        break;
                    case NMSPMessage.HANDSHAKEBYTE:
                        logger.trace("HANDSHAKE");
                        // Nothing to do
                        break;
                }
            } else {
                logger.warn("Null session for message: {}", message);
                // Do not throw, it kills the Netty thread while it is not required
            }
        } catch (Throwable e) {
            logger.error("Exception caught while trying to parse message {}.", message, e);
            if (session != null)
            {
                session.getClient()
                       .response(session, session.getWebAPIOrchestrator()
                                                 .getTransactionID(), session.getLastPingRequest(),
                               ResponseCodes.StatusCode.OPERATION_FAILED.getCode(),
                               ResponseCodes.RequestState.COMPLETE.getState(),
                               ResponseCodes.CompletionCause.OPERATION_FAILED.getCause());
                session.end(0);
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        allChannels.add(ctx.channel());

        logger.debug("{} channels opened. ", allChannels.size());

        final int maxNumChannels = MAX_NUMBER_OF_CHANNELS;
        if (isMaxOpenedChannelCountReached(maxNumChannels)) {
            int numChannels = maxNumChannels * MAX_NUMBER_OF_CHANNEL_PERCENT / 100;

            logger.warn("Connections limit of {} is reached, will close the channel until it goes down to {}", maxNumChannels, numChannels);
        }

        super.channelRegistered(ctx);
    }

    private boolean isMaxOpenedChannelCountReached(final int maxNumChannels) {
        return maxNumChannels > 0 && allChannels.size() >= maxNumChannels;
    }

    /*
     * A client connected
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session session = new Session(ctx.channel(), new WebAPIOrchestrator(webApiFeatures));
        session.setClient(NMSPClientManager.getInstance());
        session.setClientSessionListener(NMSPClientManager.getInstance());
        session.setClientAudioListener(NMSPClientManager.getInstance());

        logger.trace("{}", ctx.channel());

        session.startSessionIdleTimer();

        // NOTE: In Netty 4.X, EventExecutorGroup cannot be added directly to the pipeline as a handler
        // The single-threaded execution is ensured by using session.getSingleThreadExecutionHandler().next().execute()
        // directly where needed (see messageReceived method)
        // In Netty 3.X this was: ctx.channel().pipeline().addFirst("singleThreadExecutor", new ExecutionHandler(...))

        // Set session in channel attribute (Netty 4.x way)
        ctx.channel().attr(SESSION_KEY).set(session);

        if (SEND_MESSAGE_ON_SOCKET_CONNECT) {
            // Send handshake bytes
            NMSPMessage message = new NMSPMessage(NMSPMessage.HANDSHAKEBYTE, (short) 0, 0);
            logger.debug("<====== Sending to client {}", message);
            ctx.channel().writeAndFlush(message);
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Session session = ctx.channel().attr(SESSION_KEY).get();
        NMSPMessage message = (NMSPMessage) msg;

        messageReceived(message, session);
    }

    /*
     * Client disconnected
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session = ctx.channel().attr(SESSION_KEY).get();
        if (session != null) {
            logger.trace(ctx.channel().toString());
            ctx.channel().attr(SESSION_KEY).set(null);

            GWThreadStarter thread = new GWThreadStarter(session, new ChannelDisconnection(session));
            session.getSingleThreadExecutionHandler().next().execute(thread);
        } else {
            logger.trace(ctx.channel().toString());
        }

        if (ctx.channel().parent() != null && !ctx.channel().parent().isActive()) {
            int numChannels = MAX_NUMBER_OF_CHANNELS * MAX_NUMBER_OF_CHANNEL_PERCENT / 100;
            if (allChannels.size() <= numChannels) {
                logger.warn("Number of connections is {}", allChannels.size());
            }
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        final Session session = ctx.channel().attr(SESSION_KEY).get();

        boolean closeChannel = false;
        if (session != null) {
            logger.warn("Channel handler caught exception: ", cause);
            if (cause instanceof java.io.IOException) {
                closeChannel = true;
            } else {
                session.getSingleThreadExecutionHandler().next().execute(() -> {
                    session.setDisconnectionReason("Caught exception: " + Throwables.getCausalChain(cause));
                    NMSPSessionController.getInstance().handleInternalError(session);
                });
            }
        } else {
            logger.warn("Channel handler caught exception:", cause);
            closeChannel = true;
        }
        if (closeChannel) {
            final Channel channel = ctx.channel();
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (final Exception exception) {
                    logger.debug("exception while closing channel on pre-existing exception", exception);
                }
            }
        }
    }

    private static class ChannelDisconnection implements IGWThread {
        private final Session session;

        public ChannelDisconnection(Session session) {
            this.session = session;
        }

        public void start() {
            NMSPSessionController.getInstance().clientDisconnected(session, COPDisconnectMessage.DISCONNECT_REASON_NORMAL);
        }
    }
}
