/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json.JsonConst;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.utils.ByteHelper;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/// <summary>
/// Encodes C# objects into PDX.  The supported object types are: 
/// string, int, List&lt;object&gt;, Dictionary&lt;object&gt; byte[] and Bitmap.
/// (Bitmaps are converted to Dictionary)
/// The &lt;object&gt; member of a collection may be any of the supported types, 
/// including another collection.
/// </summary>
public class PdxPacker {
    private static final int MEMORY_STREAM_CHUNK_SIZE = 16384; // use the same as com.nuance.nvc.utils.MemoryStream.Chunk to minimize disruption when changing to ByteArrayOutputStream

    // In order to maintain backward compatibility with core 7.2, allow the option to encode using ASCII instead of UTF-8.
    private static final boolean envVarEncodeKeysUsingAscii = Boolean.parseBoolean(System.getenv("PDX_DICTIONARY_ASCII_ENCODING"));
    private static final HashMap<String, ExpandedNameMap> nameMap = new HashMap<String, ExpandedNameMap>();
    private ByteArrayOutputStream stream;
    private final Stack<ByteArrayOutputStream> streamStack;
    private final Stack<String> keyStack;
    // / <summary>
    // / Constructs a new PdxPacker to encode a pdx data block
    // / </summary>
    public PdxPacker() {
        streamStack = new Stack<ByteArrayOutputStream>();
        keyStack = new Stack<String>();
        stream = newByteArrayOutputStream();
    }

    public static void addNameMap(PdxNameMap map) {
        nameMap.put(map.getName(), new ExpandedNameMap(map));
    }

    public static void addNameMaps(PdxNameMap[] maps) {
        for (PdxNameMap map : maps) {
            addNameMap(map);
        }
    }

    // / <summary>
    // / For a payload of a given size what size header is needed to preceed it?
    // / </summary>
    // / <param name="size"></param>
    // / <returns></returns>
    public static int sizeofHeader(int size) {
        if (size <= 127) {
            return 2;
        } else if (size <= 255) {
            return 3;
        } else if (size <= 65535) {
            return 4;
        } else if (size < 4294967295L) {
            return 6;
        }
        return -1;
    }

    private static ByteArrayOutputStream newByteArrayOutputStream() {
        return new ByteArrayOutputStream(MEMORY_STREAM_CHUNK_SIZE);
    }

    // / <summary>
    // / Packs/encodes any of the supported objects.
    // / </summary>
    // / <param name="obj">The data to pack</param>
    // / <exception cref="PdxUnsupportedDataTypeException">Thrown if the data type presented for packing is not supported.</exception>
    public void packDatum(Object obj) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        packDatum(obj, PdxClass.Null);
    }

    @SuppressWarnings("unchecked")
    public void packDatum(Object obj, PdxClass overrideClass) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        packDatum(obj, overrideClass, false);
    }

    public void packDatum(Object obj, boolean encodeKeysUsingAscii) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        packDatum(obj, PdxClass.Null, encodeKeysUsingAscii);
    }

    private void packDatum(Object obj, PdxClass overrideClass, boolean encodeKeysUsingAscii) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        if (obj instanceof String) {
            if (overrideClass == PdxClass.AsciiString) {
                packAsciiString((String) obj);
            } else {
                packUtf8String((String) obj);
            }
        } else if (obj instanceof Integer || obj instanceof Short || obj instanceof Byte) {
            packNumber((Integer) obj);
        } else if (obj instanceof Double) {
            packDouble((Double) obj);
        } else if (obj instanceof HashMap<?, ?>) {
            packDictionary((HashMap<String, Object>) obj, encodeKeysUsingAscii);
        } else if (obj instanceof List<?>) {
            packSequence((List<Object>) obj, encodeKeysUsingAscii);
        } else if (obj instanceof byte[]) {
            packByteString((byte[]) obj);
        } else if (obj instanceof JsonConst) {
            String s = obj.toString();
            // Translate built-in constants to check
            // NOTE: Only "null" value is supported for now
            // Boolean ("true" or "false") are not. those are removed because PDX does not support for now.
            // NMAS-31 enhancement JIRA is added
            if (s == null || !s.equalsIgnoreCase("null")) {
                throw new PdxUnsupportedDataTypeException("Unsupported JSON const value " + s);
            }
            // packing null because null is the only supported json cont in PDX
            packNull();
        } else if (obj instanceof Boolean) {
            packBoolean((Boolean) obj);
        } else if (obj == null) {
            packNull();
        } else {
            throw new PdxUnsupportedDataTypeException();
        }
    }

    private void packHeader(PdxClass clz, int size) throws IOException {
        if (size <= 127) {
            stream.write((byte) clz.getValue());
            stream.write(size);
        } else if (size <= 255) {
            stream.write((byte) clz.getValue());
            stream.write(0x81);
            stream.write(size);
        } else if (size <= 65535) {
            stream.write((byte) clz.getValue());
            stream.write(0x82);
            short s = (short) size;
            // (short) Integer.reverseBytes(size);
            stream.write(ByteHelper.reverse(ByteHelper.getBytes(s)), 0, 2);
        } else if (size < Integer.MAX_VALUE) {
            stream.write((byte) clz.getValue());
            stream.write(0x84);
            // size = size;Integer.reverseBytes(size);
            stream.write(ByteHelper.reverse(ByteHelper.getBytes(size)), 0, 4);
        }
    }

    private void pack(PdxClass clz, byte[] payload) throws IOException {
        int len = payload.length;
        packHeader(clz, len);
        stream.write(payload, 0, len);
    }

    // / <summary>
    // / Puts a null value into the stream
    // / </summary>
    public void packNull() throws IOException {
        packHeader(PdxClass.Null, 0);
    }

    // / <summary>
    // / Push a new stream onto the encoding stack.
    // / It is imposible to from a pdx header until the size of its payload is known.
    // / For compound objects such as Lists and Dictionaries that means we need to know the encoded
    // / size of all their contents before we can build the header.
    // / By using a stack of streams we can encode the contents of an compound object in a secondary
    // / stream and then pop and add the contents to the enclosing stream
    // / </summary>
    public void pushStream() {
        streamStack.push(stream);
        stream = newByteArrayOutputStream();
    }

    // / <summary>
    // / Pops a working stream and restores the previously pushed.
    // / </summary>
    // / <returns></returns>
    public ByteArrayOutputStream popStream() {
        ByteArrayOutputStream m = stream;
        stream = streamStack.pop();
        return m;
    }

    // / <summary>
    // / Packs a dictionary
    // / </summary>
    // / <param name="dict"></param>
    public void packDictionary(HashMap<String, Object> dict) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        packDictionary(dict, false);
    }

    public void packDictionary(HashMap<String, Object> dict, boolean encodeKeysUsingAscii) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        ByteArrayOutputStream inner = null;
        pushStream();


        try {
            for (Map.Entry<String, Object> entry : dict.entrySet()) {
                keyStack.push(entry.getKey());
                if (envVarEncodeKeysUsingAscii || encodeKeysUsingAscii) {
                    packAsciiString(entry.getKey());
                } else {
                    packUtf8String(entry.getKey());
                }
                if (nameMap.containsKey(entry.getKey())) {
                    PdxClass overrideClass = PdxClass.Null;
                    ExpandedNameMap map = nameMap.get(entry.getKey());

                    // If the full path matches then use the override
                    if (map.match(keyStack)) {
                        overrideClass = map.getPdxClass();
                    }
                    packDatum(entry.getValue(), overrideClass);
                } else {
                    packDatum(entry.getValue(), encodeKeysUsingAscii);
                }
                keyStack.pop();
            }
        } finally {
            inner = popStream();
        }
        byte[] rv = inner.toByteArray();
        inner.close();
        pack(PdxClass.Dictionary, rv);
    }

    // / <summary>
    // / Packs a List
    // / </summary>
    // / <param name="seq"></param>
    public void packSequence(List<Object> seq) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        packSequence(seq, false);
    }

    private void packSequence(List<Object> seq, boolean encodeKeysUsingAscii) throws IOException, PdxException, PdxUnsupportedDataTypeException {
        ByteArrayOutputStream inner = null;
        pushStream();
        try {
            for (Object o : seq) {
                packDatum(o, encodeKeysUsingAscii);
            }
        } finally {
            inner = popStream();
        }
        byte[] rv = inner.toByteArray();
        inner.close();
        pack(PdxClass.Sequence, rv);
    }

    // / <summary>
    // / Packs a number
    // / </summary>
    // / <param name="val"></param>
    public void packNumber(int val) throws IOException, PdxException {
        byte[] payload;
        if (val >= -128 && val <= 127) {
            payload = new byte[1];
            byte sval = (byte) val;
            payload[0] = sval;
        } else if (val >= -32768 && val <= 32767) {
            payload = ByteHelper.getBytes((short) val);
        } else if (val >= -2147483648 && val <= 2147483647) {
            payload = ByteHelper.getBytes(val);
        } else {
            throw new PdxException("Integer too large to encode");
        }

        pack(PdxClass.Number, payload);
    }

    public void packDouble(double val) throws IOException {
        byte[] payload = ByteHelper.getBytes(val);
        pack(PdxClass.Double, payload);
    }

    // / <summary>
    // / Packs a Utf8 string
    // / </summary>
    // / <param name="s"></param>
    public void packUtf8String(String s) throws IOException {
        byte[] utf8s = s.getBytes(StandardCharsets.UTF_8);
        pack(PdxClass.Utf8String, utf8s);
    }

    // / <summary>
    // / Packs an ASCII string
    // / </summary>
    // / <param name="s"></param>
    public void packAsciiString(String s) throws IOException {
        byte[] asciis = s.getBytes(StandardCharsets.US_ASCII);
        pack(PdxClass.AsciiString, asciis);
    }

    // / <summary>
    // / Packs a byte array
    // / </summary>
    // / <param name="v"></param>
    public void packByteString(byte[] v) throws IOException {
        pack(PdxClass.ByteString, v);
    }

    public void packBoolean(boolean value) throws IOException {
        pack(PdxClass.Boolean, ByteHelper.getBytes(value));
    }

    // / <summary>
    // / Returns the bytes of the current stream
    // / </summary>
    // / <returns></returns>
    public byte[] toArray() {
        return stream.toByteArray();
    }

    static class PdxNameMap {
        private final String name;
        private final String path;
        private final PdxClass type;

        public PdxNameMap(String key, String path, PdxClass type) {
            this.name = key;
            this.path = path;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public PdxClass getPdxClass() {
            return type;
        }
    }

    static class ExpandedNameMap {
        private final PdxNameMap map;
        private final String[] pathParts;

        public ExpandedNameMap(PdxNameMap map) {
            this.map = map;
            String[] parts = map.getPath().split(":");
            this.pathParts = new String[parts.length];
            for (int x = parts.length - 1, y = 0; x >= 0; x--, y++) {
                this.pathParts[y] = parts[x];
            }
        }

        public boolean match(Stack<String> currentPath) {
            int depth = pathParts.length;
            if (currentPath.size() >= depth) {
                String[] currentPathParts = currentPath.toArray(new String[currentPath.size()]);
                for (int x = 0; x < depth; x++) {
                    if (!pathParts[x].equals(currentPathParts[x])) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        public String getName() {
            return map.getName();
        }

        public PdxClass getPdxClass() {
            return map.getPdxClass();
        }
    }
}
