package com.nuance.ncs.nmsp.servlet.utils;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HexToArrayTest {

    @Test
    public void privateConstructorThrows() throws NoSuchMethodException, IllegalAccessException, InstantiationException {
        Constructor<HexToArray> ctor = HexToArray.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        try {
            ctor.newInstance();
            fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    public void nullInputReturnsNull() {
        assertNull(HexToArray.hexStringToByteArray(null));
    }

    @Test
    public void emptyInputReturnsEmptyArray() {
        assertArrayEquals(new byte[0], HexToArray.hexStringToByteArray(""));
    }

    @Test
    public void decodesEvenLengthHexString() {
        byte[] expected = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        assertArrayEquals(expected, HexToArray.hexStringToByteArray("DEADBEEF"));
        assertArrayEquals(expected, HexToArray.hexStringToByteArray("deadbeef"));
    }

    @Test
    public void decodesSingleByte() {
        byte[] result = HexToArray.hexStringToByteArray("a1");
        assertEquals(1, result.length);
        assertEquals((byte) 0xA1, result[0]);
    }
}
