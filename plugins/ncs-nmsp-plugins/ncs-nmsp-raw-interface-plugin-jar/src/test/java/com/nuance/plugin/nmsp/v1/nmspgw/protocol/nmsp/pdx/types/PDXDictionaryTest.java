package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PDXDictionaryTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailUTF8() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addUTF8String("pow", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailAscii() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addAsciiString("pow", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailBool() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addBoolean(null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailInt() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addInteger(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailDouble() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addDouble(null, (double)12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addFailByteString() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addByteString("pow", null);
    }

    @Test
    public void test_add() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addUTF8String("pow", "wow");
        dictionary.addAsciiString("pow2", "wow2");

        assertEquals("wow", dictionary.getUTF8String("pow"));
        assertEquals("wow2", dictionary.getAsciiString("pow2"));

        dictionary.addBoolean("pow4", true);
        assertTrue(dictionary.getBoolean("pow4"));

        dictionary.addInteger("pow5", 5);
        assertEquals(5, dictionary.getInteger("pow5"));

        dictionary.addDouble("pow6", (double) 12);
        assertEquals(12, dictionary.getDouble("pow6"), 0.5);

        byte[] bytes = "meow".getBytes();
        dictionary.addByteString("pow7", bytes);
        byte[] newBytes = dictionary.getByteString("pow7");
        assertEquals(bytes, newBytes);
    }

    @Test
    public void test_put() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", "wow", PDXClass.UTF8);
        dictionary.put("pow2", "wow2", PDXClass.ASCII);

        assertEquals("wow", dictionary.getUTF8String("pow"));
        assertEquals("wow2", dictionary.getAsciiString("pow2"));

        dictionary.put("pow4", true, PDXClass.BOOLEAN);
        assertTrue(dictionary.getBoolean("pow4"));

        dictionary.put("pow5", 5, PDXClass.INT);
        assertEquals(5, dictionary.getInteger("pow5"));

        dictionary.put("pow6", (double)12, PDXClass.DOUBLE);
        assertEquals(12, dictionary.getDouble("pow6"), 0.5);

        dictionary.put("pow7", "true".getBytes(), PDXClass.BOOLEAN);
        assertTrue(dictionary.getBoolean("pow7"));

        byte[] bytes = new PDXInteger(5).toByteArray();
        dictionary.put("pow8", Arrays.copyOfRange(bytes, 2, bytes.length), PDXClass.INT);
        assertEquals(5, dictionary.getInteger("pow8"));

        bytes = new PDXDouble(12).toByteArray();
        dictionary.put("pow9", Arrays.copyOfRange(bytes, 2, bytes.length), PDXClass.DOUBLE);
        assertEquals(12, dictionary.getDouble("pow9"), 0.5);

        bytes = new PDXByteString("meow".getBytes()).toByteArray();
        dictionary.put("pow10", bytes, PDXClass.BYTES);
        byte[] newBytes = dictionary.getByteString("pow10");
        assertEquals(bytes, newBytes);

        bytes = new PDXUTF8String("true").toByteArray();
        dictionary.put("pow11", Arrays.copyOfRange(bytes, 2, bytes.length), PDXClass.UTF8);
        assertEquals("true", dictionary.getUTF8String("pow11"));

        bytes = new PDXAsciiString("true").toByteArray();
        dictionary.put("pow12", Arrays.copyOfRange(bytes, 2, bytes.length), PDXClass.ASCII);
        assertEquals("true", dictionary.getAsciiString("pow12"));

        assertTrue(dictionary.containsKey("pow12"));
        dictionary.put("pow12", "null".getBytes(), PDXClass.NULL);
        PDXClass pdxClass = dictionary.getClass("pow12");
        assertEquals(PDXNull.class, pdxClass.getClass());
    }

    @Test(expected = RuntimeException.class)
    public void test_invalidString() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", "wow", PDXClass.INT);
        dictionary.getUTF8String("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_wrongKeyString() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", 5, PDXClass.INT);
        dictionary.getUTF8String("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_invalidInt() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", 5, PDXClass.DOUBLE);
        dictionary.getInteger("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_wrongKeyInt() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", (double)5, PDXClass.DOUBLE);
        dictionary.getInteger("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_invalidBool() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", true, PDXClass.INT);
        dictionary.getBoolean("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_wrongKeyBool() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", 5, PDXClass.INT);
        dictionary.getBoolean("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_invalidDouble() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", (double)12, PDXClass.INT);
        dictionary.getDouble("pow");
    }

    @Test(expected = RuntimeException.class)
    public void test_wrongKeyDouble() {
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.put("pow", 5, PDXClass.INT);
        dictionary.getDouble("pow");
    }
}
