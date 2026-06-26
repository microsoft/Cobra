package com.nuance.plugin.ncs.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HashTypeTest {

    @Test
    public void testHashType() {
        assertEquals("ncsHash", HashType.HASH_TYPE_NCS.getValue());
        assertEquals("none", HashType.HASH_TYPE_NONE.getValue());
        assertEquals("sha256", HashType.HASH_TYPE_SHA256.getValue());
        assertEquals(HashType.HASH_TYPE_NONE, HashType.fromValue("none"));
        assertEquals(HashType.HASH_TYPE_SHA256, HashType.fromValue("sha256"));
        assertEquals(HashType.HASH_TYPE_NCS, HashType.fromValue("ncsHash"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testHashType_invalidString() {
        HashType.fromValue("fake");
    }
}
