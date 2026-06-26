package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PDXDSequenceTest {

    @Test
    public void test_getInteger() {
        PDXSequence sequence = new PDXSequence();
        sequence.addInteger(5);
        assertEquals(5, sequence.getInteger(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getIntegerOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getInteger(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getIntegerWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addDouble(0.0);
        sequence.getInteger(0);
    }

    @Test
    public void test_getBoolean() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        assertTrue(sequence.getBoolean(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getBooleanOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getBoolean(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getBooleanWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addDouble(0.0);
        sequence.getBoolean(0);
    }

    @Test
    public void test_getDouble() {
        PDXSequence sequence = new PDXSequence();
        sequence.addDouble(1.0);
        assertEquals(1.0, sequence.getDouble(0), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getDoubleOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getDouble(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getDoubleWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getDouble(0);
    }

    @Test
    public void test_getByteString() {
        PDXSequence sequence = new PDXSequence();
        byte[] byteArray = "test".getBytes();
        sequence.addByteString(byteArray);
        assertEquals(byteArray, sequence.getByteString(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getByteStringOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getByteString(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getByteStringWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getByteString(0);
    }

    @Test
    public void test_getUTF8String() {
        PDXSequence sequence = new PDXSequence();
        sequence.addUTF8String("test");
        assertEquals("test", sequence.getUTF8String(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getUTF8StringOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getUTF8String(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getUTF8StringWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getUTF8String(0);
    }

    @Test
    public void test_getAsciiString() {
        PDXSequence sequence = new PDXSequence();
        sequence.addAsciiString("test");
        assertEquals("test", sequence.getAsciiString(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getAsciiStringOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getAsciiString(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getAsciiStringWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getAsciiString(0);
    }

    @Test
    public void test_getDictionary() {
        PDXSequence sequence = new PDXSequence();
        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addBoolean("key", true);
        sequence.addDictionary(dictionary);
        assertTrue(sequence.getDictionary(0).getBoolean("key"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getDictionaryOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getDictionary(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getDictionaryWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getDictionary(0);
    }

    @Test
    public void test_getSequence() {
        PDXSequence sequence = new PDXSequence();
        PDXSequence innerSequence = new PDXSequence();
        innerSequence.addUTF8String("test");
        sequence.addSequence(innerSequence);
        assertEquals("test", sequence.getSequence(0).getUTF8String(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_getSequenceOutOfBounds() {
        PDXSequence sequence = new PDXSequence();
        sequence.getSequence(0);
    }

    @Test(expected = RuntimeException.class)
    public void test_getSequenceWrongType() {
        PDXSequence sequence = new PDXSequence();
        sequence.addBoolean(true);
        sequence.getSequence(0);
    }

    @Test
    public void test_toString() {
        PDXSequence sequence = getPdxSequence();

        assertEquals("[ \n" +
                "<UTF8> \"hello\" ,\n" +
                "<ASCII> \"hello\" ,\n" +
                "<INT> 1,\n" +
                "<DOUBLE> 1.0,\n" +
                "<BOOLEAN> true,\n" +
                "[ \n" +
                "    <UTF8> \"hello\" \n" +
                "] ,\n" +
                "{ \n" +
                "    key: <BOOLEAN> true\n" +
                "} \n" +
                "] ", sequence.toString());
    }

    @NotNull
    private static PDXSequence getPdxSequence() {
        PDXSequence sequence = new PDXSequence();
        sequence.addUTF8String("hello");
        sequence.addAsciiString("hello");
        sequence.addInteger(0x01);
        sequence.addDouble(0x01);
        sequence.addBoolean(true);

        PDXSequence sequence2 = new PDXSequence();
        sequence2.addUTF8String("hello");
        sequence.addSequence(sequence2);

        PDXDictionary dictionary = new PDXDictionary();
        dictionary.addBoolean("key", true);
        sequence.addDictionary(dictionary);
        return sequence;
    }

    @Test
    public void test_toByte() {
        PDXSequence sequence = getPdxSequence();

        byte[] bytes = sequence.toByteArray();
        PDXSequence sequence3 = new PDXSequence(bytes);

        assertEquals("[\n" + sequence.toString().replaceAll(" ", "") + "\n]", sequence3.toString().replaceAll(" ", ""));
    }
}
