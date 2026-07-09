package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.plugin.ncs.utils.ICodecResource;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main PDX client facade replacing the NMSP SDK Manager/NMASResource/Command trio.
 * Lifecycle: connect() → loadResource() → beginCommand() → sendXxxParam*() → endCommand() → freeResource() → shutdown()
 */
public class PdxClient {
    private static final Logger log = LogManager.getLogger(PdxClient.class);

    // Protocol bytes
    private static final byte PROTO_COP = 0x03;
    private static final byte PROTO_BCP = 0x02;
    private static final byte PROTO_VAP = 0x01;

    // COP versions
    private static final byte COP_VERSION = 0x17;
    // BCP versions
    private static final byte BCP_VERSION = 0x22;
    // VAP versions
    private static final byte VAP_VERSION = 0x12;

    // COP commands
    private static final int COP_CONNECT = 0x0001;

    // BCP commands
    private static final int BCP_LOAD_RESOURCE  = 0x0003;
    private static final int BCP_BEGIN          = 0x0004;
    private static final int BCP_DATA           = 0x0005;
    private static final int BCP_LOG            = 0x0007;
    private static final int BCP_FREE_RESOURCE  = 0x000E;

    // VAP commands
    private static final int VAP_RECORD_BEGIN = 0x0101;
    private static final int VAP_RECORD       = 0x0201;
    private static final int VAP_RECORD_END   = 0x0100;

    // BCP_DATA PDX commands
    private static final int CMD_QUERY_BEGIN = 0x0202;
    private static final int CMD_QUERY_PARAM = 0x0203;
    private static final int CMD_QUERY_END   = 0x0204;

    // Param type IDs
    private static final int PARAM_TYPE_AUDIO   = 1;
    private static final int PARAM_TYPE_TEXT     = 2;
    private static final int PARAM_TYPE_CHOICE   = 3;
    private static final int PARAM_TYPE_DATA     = 4;
    private static final int PARAM_TYPE_DICT     = 5;
    private static final int PARAM_TYPE_SEQ_START = 6;
    private static final int PARAM_TYPE_SEQ_CHUNK = 7;
    private static final int PARAM_TYPE_SEQ_END   = 8;

    private static final int SESSION_ID_LENGTH = 16;
    private static final int RESOURCE_ID = 1;
    private static final byte TRANSACTION_ID = 0x01;
    private static final long AUDIO_ID = 1L;

    private final String host;
    private final int port;
    private final String appId;
    private final byte[] appKey;
    private final String userId;
    private final ICodecResource.Codec inCodec;
    private final ICodecResource.Codec outCodec;
    private final boolean sslEnabled;
    private final boolean sslSelfSigned;
    private final long commandTimeout;
    private final PdxResponseListener listener;

    private Channel channel;
    private byte[] sessionId;
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);
    private final CountDownLatch connectedLatch = new CountDownLatch(1);

    public PdxClient(String host, int port,
                     String appId, byte[] appKey, String userId,
                     ICodecResource.Codec inCodec, ICodecResource.Codec outCodec,
                     boolean sslEnabled, boolean sslSelfSigned,
                     long commandTimeout,
                     PdxResponseListener listener) {
        this.host = host;
        this.port = port;
        this.appId = appId;
        this.appKey = appKey;
        this.userId = userId;
        this.inCodec = inCodec;
        this.outCodec = outCodec;
        this.sslEnabled = sslEnabled;
        this.sslSelfSigned = sslSelfSigned;
        this.commandTimeout = commandTimeout;
        this.listener = wrapListener(listener);
    }

    private PdxResponseListener wrapListener(final PdxResponseListener delegate) {
        return new PdxResponseListener() {
            @Override public void onConnected(String sessionIdHex) {
                setSessionId(sessionIdHex);
                connectedLatch.countDown();
                delegate.onConnected(sessionIdHex);
            }
            @Override public void onConnectionFailed(short reasonCode) {
                connectedLatch.countDown();
                delegate.onConnectionFailed(reasonCode);
            }
            @Override public void onDisconnected(short reasonCode) { delegate.onDisconnected(reasonCode); }
            @Override public void onQueryResult(PdxQueryResult result) { delegate.onQueryResult(result); }
            @Override public void onQueryError(PdxQueryError error) { delegate.onQueryError(error); }
            @Override public void onQueryRetry(PdxQueryRetry retry) { delegate.onQueryRetry(retry); }
            @Override public void onAudioReceived(byte[] audioData) { delegate.onAudioReceived(audioData); }
            @Override public void onError(Throwable cause) {
                connectedLatch.countDown();
                delegate.onError(cause);
            }
        };
    }

    /** Block until COP_CONNECTED arrives (sessionId becomes available) or timeout/connection-failure. */
    public boolean awaitConnected(long timeoutMs) {
        try {
            if (!connectedLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return sessionId != null;
    }

    /** Connect to NMSP gateway and send COP_CONNECT */
    public boolean connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(PdxClientBootstrap.workerGroup())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws SSLException {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslEnabled) {
                                SslContextBuilder builder = SslContextBuilder.forClient();
                                if (sslSelfSigned) builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                                SslContext sslContext = builder.build();
                                pipeline.addLast(sslContext.newHandler(ch.alloc(), host, port));
                            }
                            pipeline.addLast(new NmspFrameDecoder());
                            pipeline.addLast(new PdxClientHandler(listener));
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (!future.isSuccess()) {
                log.error("Failed to connect to {}:{}", host, port);
                listener.onConnectionFailed((short) -1);
                return false;
            }
            channel = future.channel();
            log.debug("Connected to {}:{}", host, port);

            // Send COP_CONNECT
            sendCopConnect();
            return true;
        } catch (Exception e) {
            log.error("Exception during connect", e);
            listener.onConnectionFailed((short) -1);
            return false;
        }
    }

    public void onSessionIdReceived(byte[] sid) {
        this.sessionId = sid;
    }

    /** Called after COP_CONNECTED with the sessionId received */
    public void setSessionId(String sessionIdHex) {
        this.sessionId = hexToBytes(sessionIdHex);
    }

    private void sendCopConnect() throws IOException {
        String xml = "<fsc>" + inCodec.getNmaidCode() + "</fsc>"
                   + "<tsc>" + outCodec.getNmaidCode() + "</tsc>"
                   + "<nmaid>" + appId + "</nmaid>"
                   + "<uid>" + userId + "</uid>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        // COP_CONNECT payload: [4 LE: xmlLen+1][1 byte: 0x00][xml bytes]
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeUInt32LE(payload, xmlBytes.length + 1);
        payload.write(0x00);
        payload.write(xmlBytes);
        sendFrame(PROTO_COP, COP_VERSION, COP_CONNECT, payload.toByteArray());
    }

    /** Load NMAS resource (BCP_LOAD_RESOURCE) */
    public void loadResource(String appName) throws IOException {
        assertConnected();
        String xml = "<rid>" + RESOURCE_ID + "</rid><username>" + appName + "</username><reco/><tts/>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(sessionId);
        payload.write(TRANSACTION_ID);
        writeUInt32LE(payload, xmlBytes.length);
        payload.write(xmlBytes);
        sendFrame(PROTO_BCP, BCP_VERSION, BCP_LOAD_RESOURCE, payload.toByteArray());
    }

    /** Begin a command (BCP_BEGIN) */
    public void beginCommand(String commandName, String appName, String appVersion,
                              String language, String carrier, String phoneModel,
                              NmspDictionary optionalKeys) throws IOException {
        assertConnected();
        NmspDictionaryImpl beginDict = new NmspDictionaryImpl();
        beginDict.addAsciiString("command", commandName);
        beginDict.addAsciiString("application", appName);
        beginDict.addAsciiString("uid", userId);
        beginDict.addAsciiString("dictation_language", language);
        beginDict.addAsciiString("carrier", carrier);
        beginDict.addAsciiString("client_hardware_model", phoneModel);
        beginDict.addAsciiString("client_app_version", appVersion);

        // Merge optional keys
        if (optionalKeys != null) {
            for (String key : optionalKeys.keySet()) {
                com.nuance.ncs.nmsp.pdx.NmspValueType vt = optionalKeys.getType(key);
                switch (vt) {
                    case INTEGER: beginDict.addInteger(key, optionalKeys.getInteger(key)); break;
                    case UTF8STRING: beginDict.addUTF8String(key, optionalKeys.getUTF8String(key)); break;
                    case ASCIISTRING: beginDict.addAsciiString(key, optionalKeys.getAsciiString(key)); break;
                    case BYTESTRING: beginDict.addByteString(key, optionalKeys.getByteString(key)); break;
                    case DICTIONARY: beginDict.addDictionary(key, optionalKeys.getDictionary(key)); break;
                    case SEQUENCE: beginDict.addSequence(key, optionalKeys.getSequence(key)); break;
                    default: break;
                }
            }
        }

        byte[] dictBytes = PdxEncoder.encodeDictionary(beginDict);
        String optXml = "<nmaid>" + appId + "</nmaid>"
                      + "<obfuscatedUserId>" + userId + "</obfuscatedUserId>"
                      + "<storageDeviceId>" + userId + "</storageDeviceId>";
        byte[] optXmlBytes = optXml.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(sessionId);
        payload.write(TRANSACTION_ID);
        writeUInt32LE(payload, dictBytes.length);
        payload.write(dictBytes);
        writeUInt32LE(payload, optXmlBytes.length);
        payload.write(optXmlBytes);
        sendFrame(PROTO_BCP, BCP_VERSION, BCP_BEGIN, payload.toByteArray());
    }

    /** Returns a PdxAudioSink that sends VAP_RECORD frames */
    public PdxAudioSink createAudioSink(String paramName) {
        return new PdxAudioSink() {
            private boolean started = false;
            @Override
            public void addAudioBuf(byte[] buf, int offset, int length, boolean isLastBuf) throws IOException {
                if (channel == null || !channel.isActive()) return;
                if (!started) {
                    // Send QueryParam + VAP_RECORD_BEGIN
                    sendQueryParam(paramName, PARAM_TYPE_AUDIO, new NmspDictionaryImpl());
                    sendVapBegin();
                    started = true;
                }
                if (buf != null && length > 0) {
                    sendVapRecord(buf, offset, length);
                }
                if (isLastBuf) {
                    sendVapEnd();
                }
            }
        };
    }

    private void sendQueryParam(String paramName, int paramType, NmspDictionary paramDict) throws IOException {
        NmspDictionaryImpl outer = new NmspDictionaryImpl();
        outer.addAsciiString("NVC_PARAM_NAME", paramName);
        outer.addInteger("NVC_PARAM_TYPE", paramType);
        if (paramDict != null && !paramDict.keySet().isEmpty()) {
            outer.addDictionary("NVC_PARAM_DICT", paramDict);
        }
        sendBcpData(CMD_QUERY_PARAM, PdxEncoder.encodeDictionary(outer));
    }

    public boolean sendChoiceParam(String paramName, String choice) {
        try {
            NmspDictionaryImpl dict = new NmspDictionaryImpl();
            dict.addUTF8String("NVC_CHOICE_VALUE", choice);
            sendQueryParam(paramName, PARAM_TYPE_CHOICE, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send ChoiceParam", e);
            return false;
        }
    }

    public boolean sendTextParam(String paramName, String text) {
        try {
            NmspDictionaryImpl dict = new NmspDictionaryImpl();
            dict.addUTF8String("NVC_TEXT_VALUE", text);
            sendQueryParam(paramName, PARAM_TYPE_TEXT, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send TextParam", e);
            return false;
        }
    }

    public boolean sendDataParam(String paramName, byte[] data) {
        try {
            NmspDictionaryImpl dict = new NmspDictionaryImpl();
            dict.addByteString("NVC_DATA_VALUE", data);
            sendQueryParam(paramName, PARAM_TYPE_DATA, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send DataParam", e);
            return false;
        }
    }

    public boolean sendDictParam(String paramName, NmspDictionary dict) {
        try {
            sendQueryParam(paramName, PARAM_TYPE_DICT, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send DictParam", e);
            return false;
        }
    }

    public boolean sendSeqStartParam(String paramName, NmspDictionary dict) {
        try {
            sendQueryParam(paramName, PARAM_TYPE_SEQ_START, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send SeqStartParam", e);
            return false;
        }
    }

    public boolean sendSeqChunkParam(String paramName, NmspDictionary dict) {
        try {
            sendQueryParam(paramName, PARAM_TYPE_SEQ_CHUNK, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send SeqChunkParam", e);
            return false;
        }
    }

    public boolean sendSeqEndParam(String paramName, NmspDictionary dict) {
        try {
            sendQueryParam(paramName, PARAM_TYPE_SEQ_END, dict);
            return true;
        } catch (IOException e) {
            log.error("Failed to send SeqEndParam", e);
            return false;
        }
    }

    public boolean sendTtsParam(String paramName, NmspDictionary dict, PdxAudioSink ttsAudioSink) {
        // TTS: send a QueryParam with the TTS dict; audio comes back via VAP_PLAY
        return sendDictParam(paramName, dict);
    }

    /** Send BCP_DATA with QueryEnd to signal end of command */
    public void endCommand() {
        try {
            NmspDictionaryImpl empty = new NmspDictionaryImpl();
            sendBcpData(CMD_QUERY_END, PdxEncoder.encodeDictionary(empty));
        } catch (IOException e) {
            log.error("Failed to send QueryEnd", e);
        }
    }

    /** Send call log bytes via BCP_LOG */
    public void sendBcpLog(byte[] logBytes) {
        if (channel == null || !channel.isActive() || sessionId == null) return;
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            payload.write(sessionId);
            payload.write(TRANSACTION_ID);
            writeUInt32LE(payload, logBytes.length);
            payload.write(logBytes);
            sendFrame(PROTO_BCP, BCP_VERSION, BCP_LOG, payload.toByteArray());
        } catch (IOException e) {
            log.error("Failed to send BCP_LOG", e);
        }
    }

    /** Free NMAS resource (BCP_FREE_RESOURCE) */
    public void freeResource() {
        if (channel == null || !channel.isActive() || sessionId == null) return;
        try {
            String xml = "<rid>" + RESOURCE_ID + "</rid>";
            byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            payload.write(sessionId);
            payload.write(TRANSACTION_ID);
            writeUInt32LE(payload, xmlBytes.length);
            payload.write(xmlBytes);
            sendFrame(PROTO_BCP, BCP_VERSION, BCP_FREE_RESOURCE, payload.toByteArray());
        } catch (IOException e) {
            log.error("Failed to send BCP_FREE_RESOURCE", e);
        }
    }

    /** Shut down connection */
    public void shutdown() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        channel = null;
    }

    // ---------- Private VAP helpers ----------

    private void sendVapBegin() throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeUInt64LE(payload, AUDIO_ID);
        sendFrame(PROTO_VAP, VAP_VERSION, VAP_RECORD_BEGIN, payload.toByteArray());
    }

    private void sendVapRecord(byte[] buf, int offset, int length) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeUInt64LE(payload, AUDIO_ID);
        payload.write(buf, offset, length);
        sendFrame(PROTO_VAP, VAP_VERSION, VAP_RECORD, payload.toByteArray());
    }

    private void sendVapEnd() throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        writeUInt64LE(payload, AUDIO_ID);
        sendFrame(PROTO_VAP, VAP_VERSION, VAP_RECORD_END, payload.toByteArray());
    }

    // ---------- Private BCP helpers ----------

    private void sendBcpData(int pdxCommand, byte[] dictBytes) throws IOException {
        assertConnected();
        // payload: [2 LE: pdxCommand] + dictBytes
        ByteArrayOutputStream pdxPayload = new ByteArrayOutputStream();
        pdxPayload.write(pdxCommand & 0xFF);
        pdxPayload.write((pdxCommand >> 8) & 0xFF);
        pdxPayload.write(dictBytes);

        int requestId = requestIdCounter.getAndIncrement();
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(sessionId);
        payload.write(TRANSACTION_ID);
        writeUInt32LE(payload, requestId);
        writeUInt32LE(payload, pdxPayload.size());
        payload.write(pdxPayload.toByteArray());
        sendFrame(PROTO_BCP, BCP_VERSION, BCP_DATA, payload.toByteArray());
    }

    // ---------- Low-level helpers ----------

    private void sendFrame(byte protocol, byte version, int command, byte[] payload) throws IOException {
        if (channel == null || !channel.isActive()) {
            throw new IOException("Channel is not active");
        }
        ByteBuf buf = channel.alloc().buffer(8 + payload.length);
        buf.writeByte(protocol);
        buf.writeByte(version);
        buf.writeShortLE(command);
        buf.writeIntLE(payload.length);
        buf.writeBytes(payload);
        channel.writeAndFlush(buf);
    }

    private void assertConnected() throws IOException {
        if (sessionId == null) throw new IOException("Not yet connected (no sessionId)");
        if (channel == null || !channel.isActive()) throw new IOException("Channel not active");
    }

    private static void writeUInt32LE(ByteArrayOutputStream out, long value) throws IOException {
        out.write((int)(value & 0xFF));
        out.write((int)((value >> 8) & 0xFF));
        out.write((int)((value >> 16) & 0xFF));
        out.write((int)((value >> 24) & 0xFF));
    }

    private static void writeUInt64LE(ByteArrayOutputStream out, long value) throws IOException {
        for (int i = 0; i < 8; i++) {
            out.write((int)((value >> (i * 8)) & 0xFF));
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                  + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
