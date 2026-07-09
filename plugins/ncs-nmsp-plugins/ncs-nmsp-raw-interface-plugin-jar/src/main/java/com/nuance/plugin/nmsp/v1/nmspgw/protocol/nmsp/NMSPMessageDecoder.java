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

import com.google.common.annotations.VisibleForTesting;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.COPSAXException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * @author epigeon
 */
public class NMSPMessageDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger();

    private final static short MIN_COP_SUPPORTED_VERSION = getMinCopSupportedVersion();
    private final static short MAX_COP_SUPPORTED_VERSION = COPMessage.COP_MAXIMUM_VERSION;
    private final static short MIN_BCP_SUPPORTED_VERSION = getMinBcpSupportedVersion();
    private final static short MAX_BCP_SUPPORTED_VERSION = BCPMessage.BCP_MAXIMUM_VERSION;
    private final static Optional<Integer> MAX_CLIENT_LOG_BYTES = Optional.of(2097152);
    private final Optional<Integer> maxMessageSize;

    public NMSPMessageDecoder(Optional<Integer> maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    private static short getMinCopSupportedVersion() {
        return COPMessage.COP_MINIMUM_LEGACY_VERSION;
    }

    private static short getMinBcpSupportedVersion() {
        return BCPMessage.BCP_MINIMUM_LEGACY_VERSION;
    }

    public static NMSPMessage decode(final ByteBuf buf, final Optional<Integer> maxMessageSize, final Session session) throws CorruptedFrameException {
        // Make sure if the length field was received.
        if (buf.readableBytes() < NMSPMessage.headerSize) {
            // The length field was not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            logger.trace("Received {} waiting for {},", buf.readableBytes(), NMSPMessage.headerSize);
            return null;
        }

        // The length field is in the buffer.

        // Mark the current buffer position before reading the length field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.
        buf.markReaderIndex();

        // Read the protocol
        final short protocol = buf.readUnsignedByte();
        // Read the version
        final short version = buf.readUnsignedByte();
        // Read the command
        final int command = buf.readUnsignedShort();
        // Read the length field
        final long length = buf.readUnsignedInt();

        if (protocol == NMSPMessage.HANDSHAKEBYTE) {
            // No more parsing needed
            return new NMSPMessage(NMSPMessage.HANDSHAKEBYTE, (short) 0, 0);
        }
        final Optional<Integer> specificMaxMessageSize = getMaxMessageSize(maxMessageSize, protocol, command, session);
        checkMessageSize(specificMaxMessageSize, protocol, command, length, buf);

        switch (protocol) {
            case NMSPMessage.COP:
                // Validate version
                if (version < MIN_COP_SUPPORTED_VERSION || version > MAX_COP_SUPPORTED_VERSION) {
                    logger.warn("Invalid COP version: {}", version);
                    buf.clear();
                    throw new CorruptedFrameException("Invalid COP version: " + version);
                }
                break;
            case NMSPMessage.BCP:
                // Validate version
                if (version < MIN_BCP_SUPPORTED_VERSION || version > MAX_BCP_SUPPORTED_VERSION) {
                    logger.warn("Invalid BCP version: {}", version);
                    buf.clear();
                    throw new CorruptedFrameException("Invalid BCP version: " + version);
                }
                break;
            case NMSPMessage.VAP:
                // Validate version
                if (version < VAPMessage.VAP_MINIMUM_VERSION || version > VAPMessage.VAP_MAXIMUM_VERSION) {
                    logger.warn("Invalid VAP version: {}", version);
                    buf.clear();
                    throw new CorruptedFrameException("Invalid VAP version: " + version);
                }
                break;
            default:
                logger.warn("Invalid protocol: {}", protocol);
                buf.clear();
                throw new CorruptedFrameException("Invalid protocol: " + protocol);
        }

        // Make sure if there's enough bytes in the buffer.
        if (buf.readableBytes() < length) {
            logger.trace("Received {} waiting for {}.", buf.readableBytes(), length);
            // The whole bytes were not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.

            // Reset to the marked position to read the length field again
            // next time.
            buf.resetReaderIndex();

            return null;
        }

        // There's enough bytes in the buffer. Read it.
        final ByteBuf message = buf.readBytes((int) length);
        return NMSPMessageDeserializer.deserialize(protocol, version, command, message, session);
    }

    @VisibleForTesting
    static void checkMessageSize(
            final Optional<Integer> maxMessageSize,
            final short protocol,
            final int command,
            final long length,
            final ByteBuf buf)
            throws CorruptedFrameException {
        if (maxMessageSize.isPresent() && (length > maxMessageSize.get())) {
            final String errorMessage = "Message " + NMSPMessage.commandToString(protocol, command) + " length too big: " + length + " bytes, maximum allowed is: " + maxMessageSize + " bytes";
            logger.error(errorMessage);
            buf.clear();
            throw new CorruptedFrameException(errorMessage);
        }
    }

    @VisibleForTesting
    static Optional<Integer> getMaxMessageSize(
            final Optional<Integer> defaultMaxMessageSize,
            final short protocol,
            final int command,
            final Session session) {
        if (protocol == NMSPMessage.BCP) {
            if (command == BCPMessage.BCP_LOG) {
                // Does not apply to at the GW HTTP interface
                return MAX_CLIENT_LOG_BYTES;
            } else {
                final Optional<Integer> derogatoryMaxMessageSizeForCurrentNMAID = session.getDerogatoryMaxMessageSizeForCurrentNMAID();
                if (derogatoryMaxMessageSizeForCurrentNMAID.isPresent()) {
                    return derogatoryMaxMessageSizeForCurrentNMAID;
                }
            }
        }
        return defaultMaxMessageSize;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf buf, final List<Object> out) throws Exception {
        final Session session = (Session) ctx.channel().attr(io.netty.util.AttributeKey.valueOf("session")).get();

        // Log raw bytes to diagnose byte order issue
        if (logger.isInfoEnabled() && buf.readableBytes() >= 8) {
            StringBuilder rawBytes = new StringBuilder("RAW HEADER BYTES: ");
            for (int i = 0; i < Math.min(8, buf.readableBytes()); i++) {
                rawBytes.append(String.format("%02X ", buf.getByte(buf.readerIndex() + i)));
            }
            logger.info(rawBytes.toString());

            // Try reading as both byte orders
            int savedIndex = buf.readerIndex();
            buf.skipBytes(2); // skip protocol and version
            int commandBE = buf.readUnsignedShort();
            long lengthBE = buf.readUnsignedInt();
            buf.readerIndex(savedIndex + 2);
            int commandLE = buf.readUnsignedShortLE();
            long lengthLE = buf.readUnsignedIntLE();
            buf.readerIndex(savedIndex);

            logger.info("IF BIG-ENDIAN:    command=0x{} length={}", Integer.toHexString(commandBE), lengthBE);
            logger.info("IF LITTLE-ENDIAN: command=0x{} length={}", Integer.toHexString(commandLE), lengthLE);
        }

        // NMSP protocol uses little-endian byte order - configure the buffer accordingly
        final ByteBuf orderedBuf = buf.order(NMSPMessage.byteOrder);

        try {
            NMSPMessage message = decode(orderedBuf, maxMessageSize, session);
            if (message != null) {
                out.add(message);
            }
        } catch (final CorruptedFrameException cfe) {
            logger.warn("Closing connection: {}", cfe.toString());
            session.setDisconnectionReason(cfe.toString());
            ctx.channel().close();
        } catch (final COPSAXException cfe) {
            logger.warn("Closing connection: {}", cfe.toString());
            NMSPClientManager instance = NMSPClientManager.getInstance();
            instance.connectFailed(session, IClientHandler.ConnectFailureReason.PARAMETERERROR, cfe.getMessage());
        } catch (final Exception e) {
            logger.error("decoding failure", e);

            NMSPSessionController.getInstance().handleInternalError(session);
        }
    }
}
