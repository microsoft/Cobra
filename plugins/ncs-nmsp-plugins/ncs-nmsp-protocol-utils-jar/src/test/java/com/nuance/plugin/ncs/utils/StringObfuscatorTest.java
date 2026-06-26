package com.nuance.plugin.ncs.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StringObfuscatorTest {

    private final String salt = "fakeSalt";

    @Test
    public void testNone() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NONE, salt, "value");
        assertEquals("value", hash);
    }

    @Test
    public void testNcs() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, salt, "value");
        assertEquals("80724c5a-472a-36a7-98cb-100334a3618c", hash);
    }

    @Test
    public void testNcs_double() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, salt, "value", true);
        assertEquals("96020382-5c29-32cb-9620-eec8f0fc7b2e", hash);
    }

    @Test
    public void testSha256() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_SHA256, salt, "value");
        assertEquals("cd42404d52ad55ccfa9aca4adc828aa5800ad9d385a0671fbcbf724118320619", hash);
    }

    @Test
    public void testSha256_nullValue() throws IOException {
        String value = null;
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, salt, value);
        assertEquals(value, hash);
    }

    @Test
    public void testSha256_emptyValue() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, salt, " ");
        assertEquals("317a2434-496a-35b8-a741-a1dcac217798", hash);
    }

    @Test
    public void testSha256_nullSecret() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, null, "value");
        assertEquals(null, hash);
    }

    @Test
    public void testSha256_invalidSecret() throws IOException {
        String hash = StringObfuscator.getObfuscatedString(HashType.HASH_TYPE_NCS, "", "value");
        assertEquals(null, hash);
    }
}
