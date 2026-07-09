package com.nuance.cobra.utils.encryption;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncryptorTest
{
    Encryptor encryptor;
    private static final String PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy7GUCbm/cn+IYoBOQccg8S/cYZyCSM4YO8cR6ChNDxd70eont5UbV2ODTGKCQwTw6qlthzRRce9g+VT3KY2RDJ8o/CfIuz5+gkerx6GJuEiyax2F8zzE6IYC5wclCTrTG3A8K3UcGmM3/QmM7V4Mn+catWMHm4Dn0S+p654y/agidE9KnO9GVj4OJ21wzHXwJAHaZ7AuXxIFZ1BleCZ7MBUAdPy1MLq0yV32X177RkAw3MXR7oJdOhu4uoNf/JBxjADwoo5/tzxwvRuqYXz4lJlUaDKm8rIaRbmqh2mDW/KBWxNM1ZEb32W0h57xbYy+UzX0lFTrZKpw99EdLiRhwQIDAQAB";

    private static final String DUMMY_STRING = "bob";
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Before
    public void setup()
    {
        encryptor = new Encryptor();
        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.ENCRYPT);
        encryptor.setTenantPublicKey(Base64.getDecoder().decode(PUBLIC_KEY));
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @Test
    public void testIsEncoded()
    {

        String encodedString = "DATA:bob:IV:bob:KEY:bob:END";
        assertTrue (Encryptor.isEncoded(encodedString));

        assert(!Encryptor.isEncoded(DUMMY_STRING));

    }

    @Test
    public void testIsMasked()
    {
        String isMasked = "xxx";
        assertTrue (Encryptor.isMasked(isMasked));

        assertTrue (!Encryptor.isMasked(DUMMY_STRING));
    }

    @Test
    public void testLogging()
    {
        encryptor.log(DUMMY_STRING);
        encryptor.end();
        String[] logParts = byteArrayOutputStream.toString().split("-");
        String msg = logParts[1];
        assertTrue(msg.contains(DUMMY_STRING));
        byteArrayOutputStream.reset();

        encryptor.encryptedLog(DUMMY_STRING);
        encryptor.end();
        logParts = byteArrayOutputStream.toString().split("-");
        msg = logParts[1];
        assertTrue(Encryptor.isEncoded(msg));
        byteArrayOutputStream.reset();

        encryptor.encryptedLog(DUMMY_STRING.getBytes());
        encryptor.end();
        logParts = byteArrayOutputStream.toString().split("-");
        msg = logParts[1];
        assertTrue(Encryptor.isEncoded(msg));
        byteArrayOutputStream.reset();

        encryptor.maskLog(DUMMY_STRING);
        encryptor.end();
        logParts = byteArrayOutputStream.toString().split("-");
        msg = logParts[1];
        assertTrue(Encryptor.isMasked(logParts[1]));
        byteArrayOutputStream.reset();
    }

    @Test
    public void encryptionTest()
    {

        String encrypted = encryptor.processString(DUMMY_STRING);
        assert(Encryptor.isEncoded(encrypted));

        encrypted = encryptor.processString(DUMMY_STRING.getBytes());
        assert(Encryptor.isEncoded(encrypted));

        byte[] encryptedBytes = encryptor.getEncryptedData(DUMMY_STRING.getBytes());
        assert(encryptedBytes!=DUMMY_STRING.getBytes());

        encryptor.setTenantPublicKey(null);
        encryptedBytes = encryptor.getEncryptedData(DUMMY_STRING.getBytes());
        assertTrue (Encryptor.isMasked(new String(encryptedBytes)));
    }

    @Test
    public void errorTest()
    {
        try
        {
            encryptor.setTenantPublicKey(new byte[5]);
        }
        catch (Exception e)
        {
            assertTrue (e.getMessage().contains("invalid key format"));
        }
    }

    @Test
    public void testHelpers()
    {

        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.ENCRYPT);
        assertTrue (Encryptor.isEncoded(encryptor.processString(DUMMY_STRING)));
        encryptor.secure(DUMMY_STRING);

        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.MASK);
        assertTrue (Encryptor.isMasked(encryptor.processString(DUMMY_STRING)));
        encryptor.secure(DUMMY_STRING);

        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.NONE);
        assertTrue (encryptor.processString(DUMMY_STRING).equals(DUMMY_STRING));
        encryptor.secure(DUMMY_STRING);

        encryptor.end();

        byteArrayOutputStream.reset();

        //test log output
        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.NONE);
        encryptor.secure("hello, my name is : ");
        encryptor.setEncryptionState(Encryptor.ENCRYPTION_STATE.ENCRYPT);
        encryptor.secure("secret");

        encryptor.end();

        assertTrue (byteArrayOutputStream.toString().contains("hello, my name is : "));
        assertTrue (byteArrayOutputStream.toString().contains("DATA:"));
        assertFalse(byteArrayOutputStream.toString().contains("hello, my name is : secret"));

        byteArrayOutputStream.reset();

    }

}
