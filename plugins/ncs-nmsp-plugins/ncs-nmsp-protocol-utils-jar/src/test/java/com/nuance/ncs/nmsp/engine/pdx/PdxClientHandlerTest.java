package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PdxClientHandlerTest {

    private static final byte PROTO_COP = 0x03;
    private static final byte PROTO_BCP = 0x02;
    private static final byte PROTO_VAP = 0x01;

    private List<String> connectedIds;
    private List<Short> disconnectedReasons;
    private List<PdxQueryResult> results;
    private List<PdxQueryError> errors;
    private List<PdxQueryRetry> retries;
    private List<byte[]> audioChunks;
    private List<Throwable> thrownErrors;

    private PdxResponseListener listener;
    private EmbeddedChannel ch;

    @Before
    public void setUp() {
        connectedIds = new ArrayList<>();
        disconnectedReasons = new ArrayList<>();
        results = new ArrayList<>();
        errors = new ArrayList<>();
        retries = new ArrayList<>();
        audioChunks = new ArrayList<>();
        thrownErrors = new ArrayList<>();

        listener = new PdxResponseListener() {
            @Override public void onConnected(String sessionId) { connectedIds.add(sessionId); }
            @Override public void onConnectionFailed(short reasonCode) {}
            @Override public void onDisconnected(short reason) { disconnectedReasons.add(reason); }
            @Override public void onQueryResult(PdxQueryResult r) { results.add(r); }
            @Override public void onQueryError(PdxQueryError e) { errors.add(e); }
            @Override public void onQueryRetry(PdxQueryRetry r) { retries.add(r); }
            @Override public void onAudioReceived(byte[] audio) { audioChunks.add(audio); }
            @Override public void onError(Throwable t) { thrownErrors.add(t); }
        };
        ch = new EmbeddedChannel(new PdxClientHandler(listener));
    }

    // ── COP frames ───────────────────────────────────────────────────────────

    @Test
    public void copConnectedWith16BytePayload() {
        // 16-byte session ID
        byte[] payload = new byte[16];
        for (int i = 0; i < 16; i++) payload[i] = (byte) i;
        ch.writeInbound(new NmspFrame(PROTO_COP, (byte)1, 0x0002, payload));
        assertEquals(1, connectedIds.size());
        assertEquals(32, connectedIds.get(0).length()); // hex-encoded
    }

    @Test
    public void copConnectedShortPayload() {
        byte[] payload = new byte[8]; // less than 16
        ch.writeInbound(new NmspFrame(PROTO_COP, (byte)1, 0x0002, payload));
        assertEquals(1, connectedIds.size());
        assertEquals("", connectedIds.get(0));
    }

    @Test
    public void copDisconnectedWithReasonCode() {
        // 2-byte reason LE: 0x05, 0x00 → reason = 5
        byte[] payload = {0x05, 0x00};
        ch.writeInbound(new NmspFrame(PROTO_COP, (byte)1, 0x0003, payload));
        assertEquals(1, disconnectedReasons.size());
        assertEquals((short) 5, (short) disconnectedReasons.get(0));
    }

    @Test
    public void copDisconnectedEmptyPayload() {
        ch.writeInbound(new NmspFrame(PROTO_COP, (byte)1, 0x0003, new byte[0]));
        assertEquals(1, disconnectedReasons.size());
        assertEquals((short) 0, (short) disconnectedReasons.get(0));
    }

    @Test
    public void copUnknownCommandIgnored() {
        ch.writeInbound(new NmspFrame(PROTO_COP, (byte)1, 0x9999, new byte[4]));
        assertTrue(connectedIds.isEmpty());
        assertTrue(disconnectedReasons.isEmpty());
    }

    // ── BCP frames ───────────────────────────────────────────────────────────

    @Test
    public void bcpQueryResult() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("final_response", 1);
        dict.addUTF8String("result_text", "hello");
        byte[] bcpPayload = buildBcpPayload(0x7201, PdxEncoder.encodeDictionary(dict));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, results.size());
        assertTrue(results.get(0).isFinal());
        assertEquals("hello", results.get(0).getData().getUTF8String("result_text"));
    }

    @Test
    public void bcpQueryResultNotFinal() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("final_response", 0);
        byte[] bcpPayload = buildBcpPayload(0x7201, PdxEncoder.encodeDictionary(dict));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, results.size());
        assertFalse(results.get(0).isFinal());
    }

    @Test
    public void bcpQueryResultNoFinalResponseKey() {
        // No "final_response" key → defaults to isFinal=true
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addUTF8String("x", "y");
        byte[] bcpPayload = buildBcpPayload(0x7201, PdxEncoder.encodeDictionary(dict));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, results.size());
        assertTrue(results.get(0).isFinal());
    }

    @Test
    public void bcpQueryError() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("error", 503);
        dict.addUTF8String("description", "Service unavailable");
        byte[] bcpPayload = buildBcpPayload(0x7202, PdxEncoder.encodeDictionary(dict));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, errors.size());
        assertEquals(503, errors.get(0).getError());
        assertEquals("Service unavailable", errors.get(0).getDescription());
    }

    @Test
    public void bcpQueryRetry() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addUTF8String("name", "retry_name");
        dict.addInteger("cause", 4);
        dict.addUTF8String("prompt", "Please try again");
        byte[] bcpPayload = buildBcpPayload(0x7205, PdxEncoder.encodeDictionary(dict));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, retries.size());
        assertEquals("retry_name", retries.get(0).getName());
        assertEquals(4, retries.get(0).getCause());
        assertEquals("Please try again", retries.get(0).getPrompt());
    }

    @Test
    public void bcpQueryRetryEmptyDict() {
        byte[] bcpPayload = buildBcpPayload(0x7205, PdxEncoder.encodeDictionary(new NmspDictionaryImpl()));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertEquals(1, retries.size());
        assertEquals("", retries.get(0).getName());
        assertEquals(0, retries.get(0).getCause());
    }

    @Test
    public void bcpTooShortPayloadIgnored() {
        // Less than 21 bytes needed
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, new byte[10]));
        assertTrue(results.isEmpty());
        assertTrue(errors.isEmpty());
    }

    @Test
    public void bcpUnknownCommandIgnored() {
        byte[] bcpPayload = buildBcpPayload(0xAAAA, PdxEncoder.encodeDictionary(new NmspDictionaryImpl()));
        ch.writeInbound(new NmspFrame(PROTO_BCP, (byte)1, 0, bcpPayload));
        assertTrue(results.isEmpty());
        assertTrue(errors.isEmpty());
    }

    // ── VAP frames ───────────────────────────────────────────────────────────

    @Test
    public void vapPlayWithAudio() {
        // 8-byte audioId + audio data
        byte[] payload = new byte[8 + 4];
        payload[8] = (byte)0x01; payload[9] = (byte)0x02; payload[10] = (byte)0x03; payload[11] = (byte)0x04;
        ch.writeInbound(new NmspFrame(PROTO_VAP, (byte)1, 0x0200, payload));
        assertEquals(1, audioChunks.size());
        assertEquals(4, audioChunks.get(0).length);
    }

    @Test
    public void vapPlayBeginWithAudio() {
        byte[] payload = new byte[8 + 3];
        payload[8] = (byte)0xAA; payload[9] = (byte)0xBB; payload[10] = (byte)0xCC;
        ch.writeInbound(new NmspFrame(PROTO_VAP, (byte)1, 0x0210, payload));
        assertEquals(1, audioChunks.size());
        assertEquals(3, audioChunks.get(0).length);
        assertEquals((byte) 0xAA, audioChunks.get(0)[0]);
    }

    @Test
    public void vapPlayJustAudioIdNoData() {
        byte[] payload = new byte[8]; // exactly 8-byte audioId, no audio
        ch.writeInbound(new NmspFrame(PROTO_VAP, (byte)1, 0x0200, payload));
        assertTrue(audioChunks.isEmpty());
    }

    @Test
    public void vapUnknownCommandIgnored() {
        ch.writeInbound(new NmspFrame(PROTO_VAP, (byte)1, 0x9999, new byte[20]));
        assertTrue(audioChunks.isEmpty());
    }

    // ── Unknown protocol ─────────────────────────────────────────────────────

    @Test
    public void unknownProtocolIgnored() {
        ch.writeInbound(new NmspFrame((byte)0xFF, (byte)1, 0, new byte[4]));
        assertTrue(connectedIds.isEmpty());
        assertTrue(results.isEmpty());
        assertTrue(audioChunks.isEmpty());
    }

    // ── Exception handling ───────────────────────────────────────────────────

    @Test
    public void exceptionCaughtNotifiesListener() {
        ch.pipeline().fireExceptionCaught(new RuntimeException("test error"));
        assertEquals(1, thrownErrors.size());
        assertFalse(ch.isActive());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Build a BCP payload: sessionId(16) + transId(1) + requestId(4 LE) +
     *                      pdxLen(4 LE) + cmdId(2 LE) + dictBytes
     */
    private byte[] buildBcpPayload(int cmdId, byte[] dictBytes) {
        int pdxLen = 2 + dictBytes.length;
        byte[] payload = new byte[16 + 1 + 4 + 4 + 2 + dictBytes.length];
        int offset = 21; // skip sessionId(16) + transId(1) + requestId(4)
        payload[offset]   = (byte)(pdxLen & 0xFF);
        payload[offset+1] = (byte)((pdxLen >> 8) & 0xFF);
        payload[offset+2] = 0;
        payload[offset+3] = 0;
        offset += 4;
        payload[offset]   = (byte)(cmdId & 0xFF);
        payload[offset+1] = (byte)((cmdId >> 8) & 0xFF);
        offset += 2;
        System.arraycopy(dictBytes, 0, payload, offset, dictBytes.length);
        return payload;
    }
}
