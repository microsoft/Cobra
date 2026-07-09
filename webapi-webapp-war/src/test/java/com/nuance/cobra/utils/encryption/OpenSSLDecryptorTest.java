package com.nuance.cobra.utils.encryption;

import com.nuance.cobra.test.CobraTest;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import static org.junit.Assert.assertEquals;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*","javax.crypto.*" })
public class OpenSSLDecryptorTest extends CobraTest {

    @Test
    public void decrypt() throws SSLDecodingException {
        String decrypted = OpenSSLDecryptor.decrypt("U2FsdGVkX1/5caKLJTOmLBx3O2MHvHiHJY1/P5lzXMk="); //gitleaks:allow
        assertEquals("password ", decrypted);

        decrypted = OpenSSLDecryptor.decrypt("U2FsdGVkX1/aEnXiJ5WX+4nTts6nZfIiKGaRoYyLO7o="); //gitleaks:allow
        assertEquals("new4now", decrypted);
    }

    @Test(expected = SSLDecodingException.class )
    public void decryptWrongPassword() throws SSLDecodingException {
        assertEquals("password ", OpenSSLDecryptor.decrypt("U2FsdGVkX19I9swumWLwFWzIqTVk+qGQyLxiXgnnnPs=")); //gitleaks:allow
    }

    @Test(expected = SSLDecodingException.class )
    public void decryptInvalidShortPassword() throws SSLDecodingException {
        OpenSSLDecryptor.decrypt("U2F=");
    }


}