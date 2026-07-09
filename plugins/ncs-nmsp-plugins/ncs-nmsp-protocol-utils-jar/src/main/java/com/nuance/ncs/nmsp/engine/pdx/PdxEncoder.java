package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.ncs.nmsp.pdx.NmspValueType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** PDX binary encoder for building client messages */
public final class PdxEncoder {
    private static final Logger log = LogManager.getLogger(PdxEncoder.class);

    // PDX class bytes
    private static final byte PDX_NUMBER    = (byte) 0xC0;
    private static final byte PDX_ASCII     = (byte) 0x16;
    private static final byte PDX_UTF8      = (byte) 0xC1;
    private static final byte PDX_BYTES     = (byte) 0x04;
    private static final byte PDX_NULL      = (byte) 0x05;
    private static final byte PDX_SEQUENCE  = (byte) 0x10;
    private static final byte PDX_DICT      = (byte) 0xE0;
    private static final byte PDX_DOUBLE    = (byte) 0x06;
    private static final byte PDX_BOOLEAN   = (byte) 0x07;

    private PdxEncoder() {}

    /** Encode a NmspDictionary to PDX bytes */
    public static byte[] encodeDictionary(NmspDictionary dict) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeDictionary(out, dict);
        } catch (IOException e) {
            log.error("Failed to encode PDX dictionary", e);
        }
        return out.toByteArray();
    }

    private static void writeDictionary(ByteArrayOutputStream outer, NmspDictionary dict) throws IOException {
        ByteArrayOutputStream inner = new ByteArrayOutputStream();
        for (String key : dict.keySet()) {
            NmspValueType type = dict.getType(key);
            // Write key as UTF8 string
            writeUtf8String(inner, key);
            // Write value
            writeValue(inner, dict, key, type);
        }
        byte[] payload = inner.toByteArray();
        writeHeader(outer, PDX_DICT, payload.length);
        outer.write(payload);
    }

    private static void writeValue(ByteArrayOutputStream out, NmspDictionary dict, String key, NmspValueType type) throws IOException {
        switch (type) {
            case INTEGER:    writeNumber(out, dict.getInteger(key)); break;
            case UTF8STRING: writeUtf8String(out, dict.getUTF8String(key)); break;
            case ASCIISTRING:writeAsciiString(out, dict.getAsciiString(key)); break;
            case BYTESTRING: writeByteString(out, dict.getByteString(key)); break;
            case DICTIONARY: writeDictionary(out, dict.getDictionary(key)); break;
            case SEQUENCE:   writeSequence(out, dict.getSequence(key)); break;
            case DOUBLE:     writeDouble(out, dict.getDouble(key)); break;
            case BOOLEAN:    writeBoolean(out, dict.getBoolean(key)); break;
            case NULL:       writeNull(out); break;
        }
    }

    private static void writeSequence(ByteArrayOutputStream outer, NmspSequence seq) throws IOException {
        ByteArrayOutputStream inner = new ByteArrayOutputStream();
        for (int i = 0; i < seq.size(); i++) {
            NmspValueType type = seq.getType(i);
            switch (type) {
                case INTEGER:    writeNumber(inner, seq.getInteger(i)); break;
                case UTF8STRING: writeUtf8String(inner, seq.getUTF8String(i)); break;
                case ASCIISTRING:writeAsciiString(inner, seq.getAsciiString(i)); break;
                case BYTESTRING: writeByteString(inner, seq.getByteString(i)); break;
                case DICTIONARY: writeDictionary(inner, seq.getDictionary(i)); break;
                case SEQUENCE:   writeSequence(inner, seq.getSequence(i)); break;
                default: break;
            }
        }
        byte[] payload = inner.toByteArray();
        writeHeader(outer, PDX_SEQUENCE, payload.length);
        outer.write(payload);
    }

    static void writeNumber(ByteArrayOutputStream out, int val) throws IOException {
        byte[] payload;
        if (val >= -128 && val <= 127) {
            payload = new byte[]{(byte) val};
        } else if (val >= -32768 && val <= 32767) {
            payload = new byte[]{(byte)(val & 0xFF), (byte)((val >> 8) & 0xFF)};
        } else {
            payload = new byte[]{(byte)(val&0xFF),(byte)((val>>8)&0xFF),(byte)((val>>16)&0xFF),(byte)((val>>24)&0xFF)};
        }
        writeHeader(out, PDX_NUMBER, payload.length);
        out.write(payload);
    }

    static void writeUtf8String(ByteArrayOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeHeader(out, PDX_UTF8, b.length);
        out.write(b);
    }

    static void writeAsciiString(ByteArrayOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.US_ASCII);
        writeHeader(out, PDX_ASCII, b.length);
        out.write(b);
    }

    static void writeByteString(ByteArrayOutputStream out, byte[] b) throws IOException {
        writeHeader(out, PDX_BYTES, b.length);
        out.write(b);
    }

    static void writeDouble(ByteArrayOutputStream out, double val) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        bb.putDouble(val);
        writeHeader(out, PDX_DOUBLE, 8);
        out.write(bb.array());
    }

    static void writeBoolean(ByteArrayOutputStream out, boolean val) throws IOException {
        writeHeader(out, PDX_BOOLEAN, 1);
        out.write(val ? 1 : 0);
    }

    static void writeNull(ByteArrayOutputStream out) throws IOException {
        writeHeader(out, PDX_NULL, 0);
    }

    private static void writeHeader(ByteArrayOutputStream out, byte clazz, int size) throws IOException {
        out.write(clazz);
        if (size <= 0x7F) {
            out.write(size);
        } else if (size <= 0xFF) {
            out.write(0x81);
            out.write(size);
        } else if (size <= 0xFFFF) {
            out.write(0x82);
            out.write(size & 0xFF);
            out.write((size >> 8) & 0xFF);
        } else {
            out.write(0x84);
            out.write(size & 0xFF);
            out.write((size >> 8) & 0xFF);
            out.write((size >> 16) & 0xFF);
            out.write((size >> 24) & 0xFF);
        }
    }
}
