package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequenceImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class PdxEncoderDecoderTest {

    // ── Encoder primitive tests ──────────────────────────────────────────────

    @Test
    public void encodeNumber1Byte() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeNumber(out, 42);
        byte[] b = out.toByteArray();
        assertEquals(3, b.length);
        assertEquals((byte) 0xC0, b[0]);
        assertEquals(1, b[1]);
        assertEquals(42, b[2]);
    }

    @Test
    public void encodeNumberNegative1Byte() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeNumber(out, -1);
        byte[] b = out.toByteArray();
        assertEquals(3, b.length);
        assertEquals(1, b[1]);
    }

    @Test
    public void encodeNumber2Byte() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeNumber(out, 200);
        byte[] b = out.toByteArray();
        assertEquals(4, b.length);
        assertEquals((byte) 0xC0, b[0]);
        assertEquals(2, b[1]);
    }

    @Test
    public void encodeNumber4Byte() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeNumber(out, 100000);
        byte[] b = out.toByteArray();
        assertEquals(6, b.length);
        assertEquals((byte) 0xC0, b[0]);
        assertEquals(4, b[1]);
    }

    @Test
    public void encodeUtf8String() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeUtf8String(out, "hi");
        byte[] b = out.toByteArray();
        assertEquals((byte) 0xC1, b[0]);
        assertEquals(2, b[1]);
        assertEquals('h', b[2]);
        assertEquals('i', b[3]);
    }

    @Test
    public void encodeAsciiString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeAsciiString(out, "ab");
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x16, b[0]);
        assertEquals(2, b[1]);
    }

    @Test
    public void encodeByteString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeByteString(out, new byte[]{1, 2, 3});
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x04, b[0]);
        assertEquals(3, b[1]);
        assertEquals(1, b[2]);
    }

    @Test
    public void encodeDouble() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeDouble(out, 3.14);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x06, b[0]);
        assertEquals(8, b[1]);
        ByteBuffer bb = ByteBuffer.wrap(b, 2, 8).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(3.14, bb.getDouble(), 0.0001);
    }

    @Test
    public void encodeBoolean() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeBoolean(out, true);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x07, b[0]);
        assertEquals(1, b[1]);
        assertEquals(1, b[2]);

        out.reset();
        PdxEncoder.writeBoolean(out, false);
        assertEquals(0, out.toByteArray()[2]);
    }

    @Test
    public void encodeNull() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeNull(out);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x05, b[0]);
        assertEquals(0, b[1]);
    }

    // ── Header size encoding variants ─────────────────────────────────────────

    @Test
    public void headerSizeInOneByte() throws Exception {
        // size <= 0x7F: single byte
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeByteString(out, new byte[10]);
        assertEquals(12, out.toByteArray().length); // 1 class + 1 size + 10 data
    }

    @Test
    public void headerSizeWith0x81Prefix() throws Exception {
        // size 128..255
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeByteString(out, new byte[200]);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x04, b[0]);
        assertEquals((byte) 0x81, b[1]);
        assertEquals((byte) 200, b[2]);
    }

    @Test
    public void headerSizeWith0x82Prefix() throws Exception {
        // size 256..0xFFFF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeByteString(out, new byte[300]);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x04, b[0]);
        assertEquals((byte) 0x82, b[1]);
        // 300 = 0x012C, LE: 0x2C, 0x01
        assertEquals((byte) 0x2C, b[2]);
        assertEquals((byte) 0x01, b[3]);
    }

    @Test
    public void headerSizeWith0x84Prefix() throws Exception {
        // size > 0xFFFF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdxEncoder.writeByteString(out, new byte[70000]);
        byte[] b = out.toByteArray();
        assertEquals((byte) 0x04, b[0]);
        assertEquals((byte) 0x84, b[1]);
    }

    // ── Round-trip tests ─────────────────────────────────────────────────────

    @Test
    public void roundTripInteger() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("n", 42);
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals(42, out.getInteger("n"));
    }

    @Test
    public void roundTripNegativeInteger() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("n", -100);
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals(-100, out.getInteger("n"));
    }

    @Test
    public void roundTripLargeInteger() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("n", 200000);
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals(200000, out.getInteger("n"));
    }

    @Test
    public void roundTripUtf8String() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addUTF8String("s", "hello world");
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals("hello world", out.getUTF8String("s"));
    }

    @Test
    public void roundTripAsciiString() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addAsciiString("s", "ascii");
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals("ascii", out.getAsciiString("s"));
    }

    @Test
    public void roundTripByteString() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addByteString("b", new byte[]{10, 20, 30});
        NmspDictionaryImpl out = roundTrip(dict);
        assertArrayEquals(new byte[]{10, 20, 30}, out.getByteString("b"));
    }

    @Test
    public void roundTripDouble() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addDouble("d", 2.718);
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals(2.718, out.getDouble("d"), 0.0001);
    }

    @Test
    public void roundTripBooleanTrue() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addBoolean("flag", true);
        NmspDictionaryImpl out = roundTrip(dict);
        assertTrue(out.getBoolean("flag"));
    }

    @Test
    public void roundTripBooleanFalse() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addBoolean("flag", false);
        NmspDictionaryImpl out = roundTrip(dict);
        assertFalse(out.getBoolean("flag"));
    }

    @Test
    public void roundTripNestedDictionary() {
        NmspDictionaryImpl inner = new NmspDictionaryImpl();
        inner.addUTF8String("inner_key", "inner_val");
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addDictionary("nested", inner);
        NmspDictionaryImpl out = roundTrip(dict);
        assertEquals("inner_val", out.getDictionary("nested").getUTF8String("inner_key"));
    }

    @Test
    public void roundTripSequenceInDictionary() {
        NmspSequenceImpl seq = new NmspSequenceImpl();
        seq.addInteger(1);
        seq.addUTF8String("item");
        seq.addAsciiString("ascii");
        seq.addByteString(new byte[]{5});
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addSequence("seq", seq);
        NmspDictionaryImpl out = roundTrip(dict);
        assertNotNull(out.getSequence("seq"));
        assertEquals(4, out.getSequence("seq").size());
        assertEquals(1, out.getSequence("seq").getInteger(0));
        assertEquals("item", out.getSequence("seq").getUTF8String(1));
    }

    @Test
    public void roundTripNestedSequenceInSequence() {
        NmspSequenceImpl inner = new NmspSequenceImpl();
        inner.addInteger(99);
        NmspSequenceImpl outer = new NmspSequenceImpl();
        outer.addSequence(inner);

        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addSequence("s", outer);
        NmspDictionaryImpl out = roundTrip(dict);
        NmspSequenceImpl outerOut = (NmspSequenceImpl) out.getSequence("s");
        assertEquals(1, outerOut.size());
        NmspSequenceImpl innerOut = (NmspSequenceImpl) outerOut.getSequence(0);
        assertEquals(99, innerOut.getInteger(0));
    }

    @Test
    public void roundTripDictionaryInSequence() {
        NmspDictionaryImpl inner = new NmspDictionaryImpl();
        inner.addInteger("x", 5);
        NmspSequenceImpl seq = new NmspSequenceImpl();
        seq.addDictionary(inner);

        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addSequence("s", seq);
        NmspDictionaryImpl out = roundTrip(dict);
        assertNotNull(out.getSequence("s").getDictionary(0));
        assertEquals(5, out.getSequence("s").getDictionary(0).getInteger("x"));
    }

    @Test
    public void roundTripAllPrimitives() {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addInteger("i", 7);
        dict.addUTF8String("u", "utf");
        dict.addAsciiString("a", "asc");
        dict.addByteString("b", new byte[]{3});
        dict.addDouble("d", 1.5);
        dict.addBoolean("t", true);
        dict.addBoolean("f", false);

        NmspDictionaryImpl out = roundTrip(dict);
        assertNotNull(out);
        assertEquals(7, out.getInteger("i"));
        assertEquals("utf", out.getUTF8String("u"));
        assertEquals("asc", out.getAsciiString("a"));
        assertArrayEquals(new byte[]{3}, out.getByteString("b"));
        assertEquals(1.5, out.getDouble("d"), 0.0001);
        assertTrue(out.getBoolean("t"));
        assertFalse(out.getBoolean("f"));
    }

    @Test
    public void roundTripLargeByteString() {
        byte[] big = new byte[300];
        for (int i = 0; i < big.length; i++) big[i] = (byte) i;
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        dict.addByteString("big", big);
        NmspDictionaryImpl out = roundTrip(dict);
        assertArrayEquals(big, out.getByteString("big"));
    }

    @Test
    public void decodeEmptyBytes() {
        assertNull(PdxDecoder.decodeDictionary(new byte[0]));
    }

    @Test
    public void decodeInvalidRootType() {
        // Encode a number at root instead of dict — decodeDictionary should return null
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try { PdxEncoder.writeNumber(out, 5); } catch (Exception ignored) {}
        assertNull(PdxDecoder.decodeDictionary(out.toByteArray()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private NmspDictionaryImpl roundTrip(NmspDictionaryImpl dict) {
        byte[] encoded = PdxEncoder.encodeDictionary(dict);
        return PdxDecoder.decodeDictionary(encoded);
    }
}
