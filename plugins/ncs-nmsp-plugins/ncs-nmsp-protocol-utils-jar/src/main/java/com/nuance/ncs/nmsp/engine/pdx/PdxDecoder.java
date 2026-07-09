package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequenceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** PDX binary decoder for parsing server responses */
public final class PdxDecoder {
    private static final Logger log = LogManager.getLogger(PdxDecoder.class);

    // PDX class bytes
    private static final int PDX_NUMBER    = 0xC0;
    private static final int PDX_ASCII     = 0x16;
    private static final int PDX_UTF8      = 0xC1;
    private static final int PDX_BYTES     = 0x04;
    private static final int PDX_NULL      = 0x05;
    private static final int PDX_SEQUENCE  = 0x10;
    private static final int PDX_DICT      = 0xE0;
    private static final int PDX_DOUBLE    = 0x06;
    private static final int PDX_BOOLEAN   = 0x07;

    private PdxDecoder() {}

    /** Decode a PDX-encoded byte array that contains a Dictionary at the root */
    public static NmspDictionaryImpl decodeDictionary(byte[] data) {
        try {
            int[] pos = {0};
            Object obj = decodeValue(data, pos);
            if (obj instanceof NmspDictionaryImpl) return (NmspDictionaryImpl) obj;
            log.warn("Expected dictionary at root, got {}", obj == null ? "null" : obj.getClass());
            return null;
        } catch (Exception e) {
            log.error("Failed to decode PDX dictionary", e);
            return null;
        }
    }

    private static Object decodeValue(byte[] data, int[] pos) {
        if (pos[0] >= data.length) return null;
        int clazz = data[pos[0]++] & 0xFF;
        int size = decodeSize(data, pos);
        int dataStart = pos[0];
        pos[0] += size;

        switch (clazz) {
            case PDX_NUMBER:  return decodeNumber(data, dataStart, size);
            case PDX_ASCII:   return new String(data, dataStart, size, StandardCharsets.US_ASCII);
            case PDX_UTF8:    return new String(data, dataStart, size, StandardCharsets.UTF_8);
            case PDX_BYTES: {
                byte[] b = new byte[size];
                System.arraycopy(data, dataStart, b, 0, size);
                return b;
            }
            case PDX_NULL:    return null;
            case PDX_DICT:    return decodeDictBlock(data, dataStart, size);
            case PDX_SEQUENCE:return decodeSeqBlock(data, dataStart, size);
            case PDX_DOUBLE:  return decodeDouble(data, dataStart);
            case PDX_BOOLEAN: return data[dataStart] != 0;
            default:
                log.warn("Unknown PDX class: 0x{}", Integer.toHexString(clazz));
                return null;
        }
    }

    private static int decodeSize(byte[] data, int[] pos) {
        int b0 = data[pos[0]++] & 0xFF;
        if (b0 <= 0x7F) return b0;
        if (b0 == 0x81) return data[pos[0]++] & 0xFF;
        if (b0 == 0x82) {
            int lo = data[pos[0]++] & 0xFF;
            int hi = data[pos[0]++] & 0xFF;
            return lo | (hi << 8);
        }
        if (b0 == 0x84) {
            int v = (data[pos[0]] & 0xFF)
                  | ((data[pos[0]+1] & 0xFF) << 8)
                  | ((data[pos[0]+2] & 0xFF) << 16)
                  | ((data[pos[0]+3] & 0xFF) << 24);
            pos[0] += 4;
            return v;
        }
        log.warn("Unknown PDX size prefix: 0x{}", Integer.toHexString(b0));
        return 0;
    }

    private static int decodeNumber(byte[] data, int offset, int size) {
        if (size == 1) return (byte)(data[offset] & 0xFF);
        if (size == 2) return (short)((data[offset+1] & 0xFF) << 8 | (data[offset] & 0xFF));
        return (data[offset+3] & 0xFF) << 24 | (data[offset+2] & 0xFF) << 16
             | (data[offset+1] & 0xFF) << 8  | (data[offset] & 0xFF);
    }

    private static double decodeDouble(byte[] data, int offset) {
        // PDX doubles are 8-byte LE
        ByteBuffer bb = ByteBuffer.wrap(data, offset, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }

    private static NmspDictionaryImpl decodeDictBlock(byte[] data, int start, int size) {
        NmspDictionaryImpl dict = new NmspDictionaryImpl();
        int[] pos = {start};
        int end = start + size;
        while (pos[0] < end) {
            // Key
            Object keyObj = decodeValue(data, pos);
            if (!(keyObj instanceof String)) {
                log.warn("Expected string key in PDX dict, got {}", keyObj);
                break;
            }
            String key = (String) keyObj;
            if (pos[0] >= end) {
                log.warn("Odd number of entries in PDX dict, missing value for key '{}'", key);
                break;
            }
            // Value
            int clazz = data[pos[0]] & 0xFF;
            Object value = decodeValue(data, pos);
            addToDict(dict, key, clazz, value);
        }
        return dict;
    }

    private static void addToDict(NmspDictionaryImpl dict, String key, int clazz, Object value) {
        switch (clazz) {
            case PDX_NUMBER:   dict.addInteger(key, (Integer) value); break;
            case PDX_ASCII:    dict.addAsciiString(key, (String) value); break;
            case PDX_UTF8:     dict.addUTF8String(key, (String) value); break;
            case PDX_BYTES:    if (value != null) dict.addByteString(key, (byte[]) value); break;
            case PDX_DICT:     if (value != null) dict.addDictionary(key, (NmspDictionaryImpl) value); break;
            case PDX_SEQUENCE: if (value != null) dict.addSequence(key, (NmspSequenceImpl) value); break;
            case PDX_DOUBLE:   dict.addDouble(key, (Double) value); break;
            case PDX_BOOLEAN:  dict.addBoolean(key, (Boolean) value); break;
            default: break;
        }
    }

    private static NmspSequenceImpl decodeSeqBlock(byte[] data, int start, int size) {
        NmspSequenceImpl seq = new NmspSequenceImpl();
        int[] pos = {start};
        int end = start + size;
        while (pos[0] < end) {
            int clazz = data[pos[0]] & 0xFF;
            Object value = decodeValue(data, pos);
            addToSeq(seq, clazz, value);
        }
        return seq;
    }

    private static void addToSeq(NmspSequenceImpl seq, int clazz, Object value) {
        switch (clazz) {
            case PDX_NUMBER:   seq.addInteger((Integer) value); break;
            case PDX_ASCII:    seq.addAsciiString((String) value); break;
            case PDX_UTF8:     seq.addUTF8String((String) value); break;
            case PDX_BYTES:    if (value != null) seq.addByteString((byte[]) value); break;
            case PDX_DICT:     if (value != null) seq.addDictionary((NmspDictionaryImpl) value); break;
            case PDX_SEQUENCE: if (value != null) seq.addSequence((NmspSequenceImpl) value); break;
            default: break;
        }
    }
}
