package com.nuance.plugin.ncs.utils;

import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class IdObfuscatorTest {

    @Test
    public void testObfuscator()
    {
        String hashedValue = IdObfuscator.obfuscate(Base64.getDecoder().decode("fakeSalt"), "valueToHash").toString();
        assertEquals("ceb1f8c1-51b3-3111-a90a-bf820fe051ba", hashedValue);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testObfuscatorInvalidKey()
    {
        IdObfuscator.obfuscate(Base64.getDecoder().decode("fakeSalt"), null).toString();
    }
}
