package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspValueType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Dispatches inbound NmspFrames to PdxResponseListener.
 *
 * BCP protocol byte = 0x02, COP = 0x03, VAP = 0x01
 * BCP commands from server: QueryResult=0x7201, QueryError=0x7202, QueryRetry=0x7205
 * COP commands from server: COP_CONNECTED=0x0002
 * VAP commands from server: VAP_PLAY=0x0200, VAP_PLAY_BEGIN=0x0210, VAP_PLAY_END=0x0400
 */
public class PdxClientHandler extends SimpleChannelInboundHandler<NmspFrame> {
    private static final Logger log = LogManager.getLogger(PdxClientHandler.class);

    // Protocol bytes
    private static final byte PROTO_VAP = 0x01;
    private static final byte PROTO_BCP = 0x02;
    private static final byte PROTO_COP = 0x03;

    // COP commands from server
    private static final int COP_CONNECTED    = 0x0002;
    private static final int COP_DISCONNECTED = 0x0003;

    // BCP PDX commands (inside payload)
    private static final int CMD_QUERY_RESULT = 0x7201;
    private static final int CMD_QUERY_ERROR  = 0x7202;
    private static final int CMD_QUERY_RETRY  = 0x7205;

    // VAP commands from server
    private static final int VAP_PLAY       = 0x0200;
    private static final int VAP_PLAY_BEGIN = 0x0210;

    private static final int SESSION_ID_LENGTH = 16;

    private final PdxResponseListener listener;

    public PdxClientHandler(PdxResponseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NmspFrame frame) {
        try {
            if (frame.protocol == PROTO_COP) {
                handleCop(frame);
            } else if (frame.protocol == PROTO_BCP) {
                handleBcp(frame);
            } else if (frame.protocol == PROTO_VAP) {
                handleVap(frame);
            } else {
                log.debug("Unknown NMSP protocol byte: 0x{}", Integer.toHexString(frame.protocol & 0xFF));
            }
        } catch (Exception e) {
            log.error("Error processing NMSP frame", e);
            listener.onError(e);
        }
    }

    private void handleCop(NmspFrame frame) {
        if (frame.command == COP_CONNECTED) {
            // Payload starts with sessionId (16 bytes)
            if (frame.payload.length >= SESSION_ID_LENGTH) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < SESSION_ID_LENGTH; i++) {
                    sb.append(String.format("%02x", frame.payload[i] & 0xFF));
                }
                String sessionId = sb.toString();
                log.debug("COP_CONNECTED sessionId={}", sessionId);
                listener.onConnected(sessionId);
            } else {
                log.warn("COP_CONNECTED payload too short: {} bytes", frame.payload.length);
                listener.onConnected("");
            }
        } else if (frame.command == COP_DISCONNECTED) {
            short reasonCode = 0;
            if (frame.payload.length >= 2) {
                reasonCode = (short) ((frame.payload[1] & 0xFF) << 8 | (frame.payload[0] & 0xFF));
            }
            log.debug("COP_DISCONNECTED reason={}", reasonCode);
            listener.onDisconnected(reasonCode);
        }
    }

    private void handleBcp(NmspFrame frame) {
        // BCP_DATA payload: sessionId(16) + transId(1) + requestId(4 LE) + pdxLen(4 LE) + pdxPayload
        byte[] p = frame.payload;
        int offset = SESSION_ID_LENGTH + 1 + 4; // skip sessionId + transId + requestId
        if (p.length < offset + 4) {
            log.warn("BCP frame payload too short: {}", p.length);
            return;
        }
        int pdxLen = (int) readUInt32LE(p, offset);
        offset += 4;
        if (p.length < offset + pdxLen || pdxLen < 2) {
            log.warn("BCP frame PDX payload too short or missing command ID");
            return;
        }
        // PDX payload: [2 LE: commandId] + [PDX dict bytes]
        int cmdId = ((p[offset + 1] & 0xFF) << 8) | (p[offset] & 0xFF);
        int dictOffset = offset + 2;
        int dictLen = pdxLen - 2;
        byte[] dictBytes = new byte[dictLen];
        System.arraycopy(p, dictOffset, dictBytes, 0, dictLen);

        if (cmdId == CMD_QUERY_RESULT) {
            NmspDictionaryImpl dict = PdxDecoder.decodeDictionary(dictBytes);
            if (dict == null) {
                log.warn("Failed to decode QueryResult PDX dict");
                return;
            }
            boolean isFinal = true;
            if (dict.containsKey("final_response") && dict.getType("final_response") == NmspValueType.INTEGER) {
                isFinal = dict.getInteger("final_response") != 0;
            }
            listener.onQueryResult(new PdxQueryResult(dict, isFinal));
        } else if (cmdId == CMD_QUERY_ERROR) {
            NmspDictionaryImpl dict = PdxDecoder.decodeDictionary(dictBytes);
            int errorCode = 0;
            String description = "";
            if (dict != null) {
                if (dict.containsKey("error") && dict.getType("error") == NmspValueType.INTEGER)
                    errorCode = dict.getInteger("error");
                if (dict.containsKey("description") && dict.getType("description") == NmspValueType.UTF8STRING)
                    description = dict.getUTF8String("description");
            }
            listener.onQueryError(new PdxQueryError(errorCode, description));
        } else if (cmdId == CMD_QUERY_RETRY) {
            NmspDictionaryImpl dict = PdxDecoder.decodeDictionary(dictBytes);
            String name = "";
            int cause = 0;
            String prompt = "";
            if (dict != null) {
                if (dict.containsKey("name") && dict.getType("name") == NmspValueType.UTF8STRING)
                    name = dict.getUTF8String("name");
                if (dict.containsKey("cause") && dict.getType("cause") == NmspValueType.INTEGER)
                    cause = dict.getInteger("cause");
                if (dict.containsKey("prompt") && dict.getType("prompt") == NmspValueType.UTF8STRING)
                    prompt = dict.getUTF8String("prompt");
            }
            listener.onQueryRetry(new PdxQueryRetry(name, cause, prompt));
        } else {
            log.debug("Ignoring BCP PDX command: 0x{}", Integer.toHexString(cmdId));
        }
    }

    private void handleVap(NmspFrame frame) {
        // VAP_PLAY payload: audioId(8 bytes LE) + audio data
        if (frame.command == VAP_PLAY || frame.command == VAP_PLAY_BEGIN) {
            int audioDataOffset = 8;
            if (frame.payload.length > audioDataOffset) {
                int audioLen = frame.payload.length - audioDataOffset;
                byte[] audio = new byte[audioLen];
                System.arraycopy(frame.payload, audioDataOffset, audio, 0, audioLen);
                listener.onAudioReceived(audio);
            }
        }
    }

    private static long readUInt32LE(byte[] data, int offset) {
        return ((long)(data[offset+3] & 0xFF) << 24)
             | ((long)(data[offset+2] & 0xFF) << 16)
             | ((long)(data[offset+1] & 0xFF) << 8)
             | (long)(data[offset] & 0xFF);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("PdxClientHandler exception", cause);
        listener.onError(cause);
        ctx.close();
    }
}
