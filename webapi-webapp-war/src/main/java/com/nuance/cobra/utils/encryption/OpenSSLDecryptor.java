package com.nuance.cobra.utils.encryption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Class used to decrypt openssl encrypted strings.
 *
 * Use the following line to encrypt your string:
 * $> echo -n "password" | openssl aes-256-cbc -md sha256 -a -salt -pass pass:"webapiEncryptionLongString_123"
 *
 * Use the following line to decrypt your file:
 * $> openssl aes-256-cbc -d -md sha256 -a -salt -pass pass:"webapiEncryptionLongString_123" -in inputfile
 */

// https://stackoverflow.com/a/11786924/650246

public class OpenSSLDecryptor {
    private static final Charset ASCII = Charset.forName("ASCII");
    private static final int INDEX_KEY = 0;
    private static final int INDEX_IV = 1;
    private static final int ITERATIONS = 1;

    private static final int SALT_OFFSET = 8;
    private static final int SALT_SIZE = 8;
    private static final int CIPHERTEXT_OFFSET = SALT_OFFSET + SALT_SIZE;

    private static final int KEY_SIZE_BITS = 256;
    private static final Logger logger = LogManager.getLogger(OpenSSLDecryptor.class);


    public static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md,
            byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (;;) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }


    public static String decrypt(String base64Data) throws SSLDecodingException {
        try {
            logger.trace("base64Data: {}", base64Data);

            byte[] headerSaltAndCipherText = Base64.getMimeDecoder().decode(base64Data);
            // --- extract salt & encrypted ---

            // header is "Salted__", ASCII encoded, if salt is being used (the default)
            byte[] salt = Arrays.copyOfRange(
                    headerSaltAndCipherText, SALT_OFFSET, SALT_OFFSET + SALT_SIZE);
            byte[] encrypted = Arrays.copyOfRange(
                    headerSaltAndCipherText, CIPHERTEXT_OFFSET, headerSaltAndCipherText.length);

            // --- specify cipher and digest for EVP_BytesToKey method ---

            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            MessageDigest md5 = MessageDigest.getInstance("SHA-256");

            byte[] theKey = new byte[]{'w', 'e', 'b', 'a', 'p', 'i', 'E', 'n', 'c', 'r', 'y', 'p', 't', 'i', 'o', 'n', 'L', 'o', 'n', 'g', 'S', 't', 'r', 'i', 'n', 'g', '_', '1', '2', '3'};

            // --- create key and IV  ---

            // the IV is useless, OpenSSL might as well have use zero's
            final byte[][] keyAndIV = EVP_BytesToKey(
                    KEY_SIZE_BITS / Byte.SIZE,
                    aesCBC.getBlockSize(),
                    md5,
                    salt,
                    theKey,
                    ITERATIONS);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[INDEX_IV]);

            // --- initialize cipher instance and decrypt ---

            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decrypted = aesCBC.doFinal(encrypted);

            return new String(decrypted, ASCII).replaceAll("\n$", "");
        }
        catch (BadPaddingException e) {
            // AKA "something went wrong"
            throw new SSLDecodingException(
                    "Bad password, algorithm, mode or padding;" +
                    " no salt, wrong number of iterations or corrupted ciphertext.");
        } catch (IllegalBlockSizeException | RuntimeException  e) {
            throw new SSLDecodingException(
                    "Bad algorithm, mode or corrupted (resized) ciphertext.", e);
        } catch (GeneralSecurityException e) {
            throw new SSLDecodingException("General security exception", e);
        }
    }
}