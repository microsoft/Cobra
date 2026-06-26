package com.nuance.nmsp.servlet.nmas.audio;

import com.nuance.ncs.nmsp.engine.pdx.PdxAudioSink;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.xiph.speex.OggCrc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OggAudioStreamerTest {

    private static class CapturedBuf {
        final byte[] buf;
        final int offset;
        final int length;
        final boolean isLastBuf;
        CapturedBuf(byte[] b, int o, int l, boolean last) {
            buf = (b == null) ? null : b.clone();
            offset = o;
            length = l;
            isLastBuf = last;
        }
    }

    private List<CapturedBuf> sinkCalls;
    private PdxAudioSink sink;

    @Before
    public void setUp() {
        sinkCalls = new ArrayList<>();
        sink = (buf, offset, length, isLastBuf) ->
            sinkCalls.add(new CapturedBuf(buf, offset, length, isLastBuf));
    }

    private OggAudioStreamer streamerFor(byte[] body) throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent(body);
        return new OggAudioStreamer(req, sink);
    }

    @Test
    public void emptyInputClosesSinkAndReturns() throws IOException {
        OggAudioStreamer s = streamerFor(new byte[0]);
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void truncatedHeaderClosesSinkAndReturns() throws IOException {
        // Less than OGG_HEADERSIZE (27) bytes — readFully throws.
        OggAudioStreamer s = streamerFor(new byte[]{'O', 'g', 'g', 'S', 0, 0, 0, 0, 0, 0});
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void wrongMagicEmitsErrorAndReturns() throws IOException {
        // 27 zero bytes — readFully succeeds but OGGID mismatches.
        OggAudioStreamer s = streamerFor(new byte[27]);
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void truncatedSegmentTableReturnsEarly() throws IOException {
        // Valid magic, segments=5 (header[26]=5) but no segment table bytes follow.
        byte[] header = new byte[27];
        header[0] = 'O'; header[1] = 'g'; header[2] = 'g'; header[3] = 'S';
        header[26] = 5;
        OggAudioStreamer s = streamerFor(header);
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void segmentSize255IsRejected() throws IOException {
        // segments=1, segment[0]=255 -> "sorry, don't handle 255 sizes!"
        byte[] page = new byte[27 + 1];
        page[0] = 'O'; page[1] = 'g'; page[2] = 'g'; page[3] = 'S';
        page[26] = 1;
        page[27] = (byte) 0xFF;
        OggAudioStreamer s = streamerFor(page);
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void truncatedPayloadReturnsEarly() throws IOException {
        // segments=1, segment[0]=10 but no payload bytes follow.
        byte[] page = new byte[27 + 1];
        page[0] = 'O'; page[1] = 'g'; page[2] = 'g'; page[3] = 'S';
        page[26] = 1;
        page[27] = 10;
        OggAudioStreamer s = streamerFor(page);
        s.readOGG();
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void checksumMismatchEmitsError() throws IOException {
        // Valid magic, 1 segment of 4 bytes payload; CRC field left at 0 will not match
        // the computed CRC, triggering the "Ogg CheckSums do not match" path.
        byte[] page = new byte[27 + 1 + 4];
        page[0] = 'O'; page[1] = 'g'; page[2] = 'g'; page[3] = 'S';
        page[26] = 1;
        page[27] = 4;
        page[28] = 1; page[29] = 2; page[30] = 3; page[31] = 4;

        OggAudioStreamer s = streamerFor(page);
        s.readOGG();

        // packetNo == 0 with a non-speex 4-byte payload: readSpeexHeader returns false
        // (bytes != 80) so packetNo stays 0 and no sink data is emitted. CRC mismatch
        // then closes the sink.
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void firstPageWithValidSpeexHeaderAdvancesPacketCounter() throws IOException {
        // First page: 80-byte Speex identification header packet. Must have correct CRC
        // for the loop to continue past the CRC check at the bottom of the page.
        byte[] page = buildOggPageWithCrc(speexHeader());
        // Append empty bytes so the next loop iteration's readFully on the second page
        // header throws EOFException and exits cleanly.
        OggAudioStreamer s = streamerFor(page);
        s.readOGG();
        // Speex header packet is consumed (packetNo 0 -> 1); EOF on next page closes sink.
        assertEquals(1, sinkCalls.size());
        assertNull(sinkCalls.get(0).buf);
        assertTrue(sinkCalls.get(0).isLastBuf);
    }

    @Test
    public void thirdPacketIsForwardedToSink() throws IOException {
        // Three pages: speex id header, speex comment header (skipped), data payload.
        byte[] page1 = buildOggPageWithCrc(speexHeader());
        byte[] page2 = buildOggPageWithCrc(new byte[]{0x01, 0x02, 0x03, 0x04});
        byte[] dataPayload = new byte[]{0x55, 0x66, 0x77, (byte) 0x88};
        byte[] page3 = buildOggPageWithCrc(dataPayload);

        byte[] all = concat(concat(page1, page2), page3);
        OggAudioStreamer s = streamerFor(all);
        s.readOGG();

        // page3 produces an addAudioBuf call with the 4-byte payload (isLastBuf=false),
        // then EOF closes the sink with null/true.
        assertEquals(2, sinkCalls.size());
        assertEquals(4, sinkCalls.get(0).length);
        assertEquals(false, sinkCalls.get(0).isLastBuf);
        assertEquals(0x55, sinkCalls.get(0).buf[0] & 0xFF);
        assertNull(sinkCalls.get(1).buf);
        assertTrue(sinkCalls.get(1).isLastBuf);
    }

    @Test
    public void runWrapsReadOggAndSignalsFinished() throws IOException {
        // Drive the run()/Thread path end-to-end.
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent(new byte[0]);
        OggAudioStreamer s = new OggAudioStreamer(req, sink);
        s.start();
        s.waitAudioStreamingFinished();
        assertEquals(1, sinkCalls.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static byte[] speexHeader() {
        byte[] payload = new byte[80];
        // bytes 0..7: "Speex   " magic
        byte[] magic = "Speex   ".getBytes();
        System.arraycopy(magic, 0, payload, 0, 8);
        // bytes 36..39: sample rate (little-endian) = 16000
        payload[36] = (byte) 0x80; payload[37] = 0x3E;
        // byte 40: mode = 0 (narrowband)
        payload[40] = 0;
        // bytes 48..51: channels = 1
        payload[48] = 1;
        // bytes 64..67: nframes = 1
        payload[64] = 1;
        return payload;
    }

    /**
     * Build a single Ogg page with one segment carrying {@code payload}, then patch the
     * page-header CRC field with the value OggCrc.checksum() expects.
     */
    private static byte[] buildOggPageWithCrc(byte[] payload) {
        if (payload.length > 254) {
            throw new IllegalArgumentException("payload must fit in a single segment");
        }
        byte[] page = new byte[27 + 1 + payload.length];
        page[0] = 'O'; page[1] = 'g'; page[2] = 'g'; page[3] = 'S';
        // bytes 4..21 are header/version/flags/granule/serial/page sequence — leave zero.
        // bytes 22..25 are the CRC slot — leave zero so OggCrc computes correctly.
        page[26] = 1;
        page[27] = (byte) payload.length;
        System.arraycopy(payload, 0, page, 28, payload.length);

        int crc = OggCrc.checksum(0, page, 0, 27);
        crc = OggCrc.checksum(crc, page, 27, 1);
        crc = OggCrc.checksum(crc, payload, 0, payload.length);
        page[22] = (byte) (crc & 0xFF);
        page[23] = (byte) ((crc >> 8) & 0xFF);
        page[24] = (byte) ((crc >> 16) & 0xFF);
        page[25] = (byte) ((crc >> 24) & 0xFF);
        return page;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
