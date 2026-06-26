package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class PdxPackerTest {

    // Test for integer value between -128 and 127
    @Test
    public void testPackNumberByteRange() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        int value = 100;
        pdxPacker.packNumber(value);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(value, (int) PdxUnpacker.unpackDatum(packet));
    }

    // Test for integer value between -128 and 127
    @Test
    public void testPackNumber10000() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        int value = 10000;
        pdxPacker.packNumber(value);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(value, (int) PdxUnpacker.unpackDatum(packet));
    }

    // Test for integer value between -128 and 127
    @Test
    public void testPackNumber1000000() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        int value = 1000000;
        pdxPacker.packNumber(value);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(value, (int) PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackHeaderSizeLessThanEqual127() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.pushStream();
        int length = 50;
        String s = "";
        for(int i = 0; i < length; i++) {
            s += "a";
        }
        pdxPacker.packUtf8String(s);
        ByteArrayOutputStream stream = pdxPacker.popStream();
        byte[] result = stream.toByteArray();
        assertEquals(length, result[1]);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(s, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackHeaderSizeLessThanEqual255() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.pushStream();
        int length = 220;
        String s = "";
        for(int i = 0; i < length; i++) {
            s += "a";
        }
        pdxPacker.packUtf8String(s);
        ByteArrayOutputStream stream = pdxPacker.popStream();
        byte[] result = stream.toByteArray();
        assertEquals(length + 3, result.length);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(s, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackHeaderSizeLessThanEqual65535() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.pushStream();
        int length = 300;
        String s = "";
        for(int i = 0; i < length; i++) {
            s += "a";
        }
        pdxPacker.packUtf8String(s);
        ByteArrayOutputStream stream = pdxPacker.popStream();
        byte[] result = stream.toByteArray();
        assertEquals(length + 4, result.length);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(s, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackHeaderSizeOver65535() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.pushStream();
        int length = 65635;
        String s = "";
        for(int i = 0; i < length; i++) {
            s += "a";
        }
        pdxPacker.packUtf8String(s);
        ByteArrayOutputStream stream = pdxPacker.popStream();
        byte[] result = stream.toByteArray();
        assertEquals(length + 6, result.length);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(s, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackString() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        String value = "test";
        pdxPacker.packDatum(value);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(value, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackDouble() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        double value = 123.45;
        pdxPacker.packDatum(value);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(value, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackBoolean() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.packDatum(true);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertTrue((boolean)PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackNull() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        pdxPacker.packDatum(null);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertNull(PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackDictionary() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        HashMap<String, Object> dict = new HashMap<>();
        dict.put("key1", "value1");
        dict.put("key2", 123);
        pdxPacker.packDatum(dict);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(dict, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testPackSequence() throws IOException {
        PdxPacker pdxPacker = new PdxPacker();
        List<Object> list = Arrays.asList("value1", 123, true);
        pdxPacker.packDatum(list);
        byte[] result = pdxPacker.toArray();
        assertNotNull(result);
        PdxPacket packet = new PdxPacket(result, 0);
        assertEquals(list, PdxUnpacker.unpackDatum(packet));
    }

    @Test
    public void testGetName() {
        PdxPacker.PdxNameMap pdxNameMap = new PdxPacker.PdxNameMap("key", "path", PdxClass.Utf8String);
        assertEquals("key", pdxNameMap.getName());
    }

    @Test
    public void testGetPath() {
        PdxPacker.PdxNameMap pdxNameMap = new PdxPacker.PdxNameMap("key", "path", PdxClass.Utf8String);
        assertEquals("path", pdxNameMap.getPath());
    }

    @Test
    public void testGetPdxClass() {
        PdxPacker.PdxNameMap pdxNameMap = new PdxPacker.PdxNameMap("key", "path", PdxClass.Utf8String);
        assertEquals(PdxClass.Utf8String, pdxNameMap.getPdxClass());
    }

    @Test
    public void testMatch() {
        PdxPacker.PdxNameMap nameMap = new PdxPacker.PdxNameMap("key", "path:to:element", PdxClass.Utf8String);
        PdxPacker.ExpandedNameMap expandedNameMap = new PdxPacker.ExpandedNameMap(nameMap);

        Stack<String> currentPath = new Stack<>();
        currentPath.push("element");
        currentPath.push("to");
        currentPath.push("path");

        assertTrue(expandedNameMap.match(currentPath));
    }

    @Test
    public void testMatchFalse() {
        PdxPacker.PdxNameMap nameMap = new PdxPacker.PdxNameMap("key", "path:to:element", PdxClass.Utf8String);
        PdxPacker.ExpandedNameMap expandedNameMap = new PdxPacker.ExpandedNameMap(nameMap);

        Stack<String> currentPath = new Stack<>();
        currentPath.push("wrong");
        currentPath.push("to");
        currentPath.push("path");

        assertFalse(expandedNameMap.match(currentPath));
    }

    @Test
    public void testExpandedGetName() {
        PdxPacker.PdxNameMap nameMap = new PdxPacker.PdxNameMap("key", "path", PdxClass.Utf8String);
        PdxPacker.ExpandedNameMap expandedNameMap = new PdxPacker.ExpandedNameMap(nameMap);

        assertEquals("key", expandedNameMap.getName());
    }

    @Test
    public void testExpandedGetPdxClass() {
        PdxPacker.PdxNameMap nameMap = new PdxPacker.PdxNameMap("key", "path", PdxClass.Utf8String);
        PdxPacker.ExpandedNameMap expandedNameMap = new PdxPacker.ExpandedNameMap(nameMap);

        assertEquals(PdxClass.Utf8String, expandedNameMap.getPdxClass());
    }
}
