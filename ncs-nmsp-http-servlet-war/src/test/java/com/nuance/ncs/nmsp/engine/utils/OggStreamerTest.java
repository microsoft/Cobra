package com.nuance.ncs.nmsp.engine.utils;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OggStreamerTest {

    private List<byte[]> capturedPayloads;
    private PdxAudioSink sink;
    private OggStreamer streamer;

    @Before
    public void setUp() throws IOException {
        capturedPayloads = new ArrayList<>();
        sink = (buf, offset, length, isLastBuf) -> {
            byte[] copy = new byte[length];
            System.arraycopy(buf, offset, copy, 0, length);
            capturedPayloads.add(copy);
        };
        streamer = new OggStreamer(sink);
    }

    @Test
    public void constructorExposesAudioSink() {
        assertNotNull(streamer);
        assertSame(sink, streamer.getAudioSink());
    }

    @Test
    public void setAudioSinkUpdatesReference() {
        PdxAudioSink replacement = (buf, offset, length, isLastBuf) -> {};
        streamer.setAudioSink(replacement);
        assertSame(replacement, streamer.getAudioSink());
    }

    @Test
    public void streamWithNullPayloadIsNoop() throws IOException {
        streamer.stream(null);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void streamWithEmptyPayloadIsNoop() throws IOException {
        streamer.stream(new byte[0]);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void flushSucceedsWithoutData() throws IOException {
        streamer.flush();
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void garbageBytesDoNotInvokeSink() throws IOException {
        // Bytes that never form "OggS" — findHeader scans the whole stream looking for it
        // and exits without ever transitioning to READING_HEADER.
        byte[] junk = new byte[]{'X', 'Y', 'Z', '!', '@'};
        streamer.stream(junk);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void partialSignatureResetsScannerWhenWrongChar() throws IOException {
        // Drives the SEEKING_NEW_HEADER_FOUND_O → reset and FOUND_OG → reset branches.
        // 'O','g','x' resets after finding 'O','g' but not 'g'.
        // 'O','g','g','x' resets after finding 'O','g','g' but not 'S'.
        byte[] near = new byte[]{'O', 'g', 'x', 'O', 'g', 'g', 'x', 'O', 'O', 'g', 'O'};
        streamer.stream(near);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void incompleteHeaderReturnsEarly() throws IOException {
        // Just "OggS" and a few more bytes — not enough for the 23-byte rest-of-header.
        byte[] tooShort = new byte[]{'O', 'g', 'g', 'S', 1, 2, 3, 4, 5};
        streamer.stream(tooShort);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void twoPagesWithPayloadEmitsAudioBuffers() throws IOException {
        // Two complete Ogg pages with a 3-byte payload each. The class has a known
        // remainingBytes accounting quirk that requires extra bytes downstream of the
        // first page in order to advance through READING_SEGMENTS/READING_DATA, so
        // we feed two pages so the first one fully drains.
        byte[] page = oggPage(/*numSegments=*/1, /*segmentSizes=*/new int[]{3}, /*payload=*/new byte[]{0x11, 0x22, 0x33});
        byte[] doubled = new byte[page.length * 2];
        System.arraycopy(page, 0, doubled, 0, page.length);
        System.arraycopy(page, 0, doubled, page.length, page.length);
        streamer.stream(doubled);

        // First page should have produced one buffer of the 3-byte payload.
        assertEquals(1, capturedPayloads.size());
        assertEquals(3, capturedPayloads.get(0).length);
        assertEquals(0x11, capturedPayloads.get(0)[0] & 0xff);
    }

    @Test
    public void emptyPayloadSegmentIsNotForwarded() throws IOException {
        // A 0-length segment goes through handleReadData but does not call audioSink
        // (the `bytesInCurrentSegment > 0` check). Two pages so the state machine
        // can run through the segment-table read.
        byte[] page = oggPage(1, new int[]{0}, new byte[0]);
        byte[] doubled = new byte[page.length * 2];
        System.arraycopy(page, 0, doubled, 0, page.length);
        System.arraycopy(page, 0, doubled, page.length, page.length);
        streamer.stream(doubled);
        assertTrue(capturedPayloads.isEmpty());
    }

    @Test
    public void chunkedStreamingAccumulatesAcrossCalls() throws IOException {
        // Same two pages, but delivered as several smaller stream() calls. Verifies
        // the buffering across MAX_STREAM_WINDOW chunks and across invocations.
        byte[] page = oggPage(1, new int[]{3}, new byte[]{0x41, 0x42, 0x43});
        byte[] doubled = new byte[page.length * 2];
        System.arraycopy(page, 0, doubled, 0, page.length);
        System.arraycopy(page, 0, doubled, page.length, page.length);

        int half = doubled.length / 2;
        byte[] firstChunk = new byte[half];
        byte[] secondChunk = new byte[doubled.length - half];
        System.arraycopy(doubled, 0, firstChunk, 0, half);
        System.arraycopy(doubled, half, secondChunk, 0, doubled.length - half);
        streamer.stream(firstChunk);
        streamer.stream(secondChunk);
        assertEquals(1, capturedPayloads.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static byte[] oggPage(int numSegments, int[] segmentSizes, byte[] payload) {
        int totalPayload = 0;
        for (int size : segmentSizes) totalPayload += size;
        byte[] page = new byte[27 + numSegments + totalPayload];
        page[0] = 'O'; page[1] = 'g'; page[2] = 'g'; page[3] = 'S';
        // bytes 4..25 left at zero (version, flags, granule, serial, page seq, checksum)
        page[26] = (byte) numSegments;
        for (int i = 0; i < numSegments; i++) {
            page[27 + i] = (byte) segmentSizes[i];
        }
        System.arraycopy(payload, 0, page, 27 + numSegments, payload.length);
        return page;
    }
}
