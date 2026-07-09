package com.nuance.cobra.utils.encryption;

import org.apache.logging.log4j.LogManager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that supports encrypted encryption through Log4J2, as well as
 * general encryption.
 * <p>
 *
 */
public class Encryptor {

    public enum ENCRYPTION_STATE {
        ENCRYPT, MASK, NONE
    }

    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_PADDING = "CBC/PKCS5Padding";

    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final String ASYMMETRIC_PADDING = "ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final char MASKING_CHAR = 'x';

    private byte[] tenantPublicKey;

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Encryptor.class);
    private ENCRYPTION_STATE encryptionState = Encryptor.ENCRYPTION_STATE.NONE;

    private Queue<String> logQueue;

    public Encryptor() {
        logQueue = new LinkedList<>();
    }

    /**
     * Sets the method of encryption to be used by instances of the Encryptor class.
     * @param encryptionState - {MASK, ENCRYPTION, NONE}
     */
    public void setEncryptionState(ENCRYPTION_STATE encryptionState) {
        this.encryptionState = encryptionState;
    }

    /**
     * Check if the input string is already encrypted (contains the substrings necessary).
     * @param inputString - String
     * @return boolean
     */
    @SuppressWarnings("unused")
    public static boolean isEncoded(String inputString) {
        return inputString.contains(":IV:")
                && inputString.contains("DATA:")
                && inputString.contains(":END")
                && inputString.contains("KEY");
    }

    /**
     * Check if the input string only contains x's (aka is masked).
     * @param inputString - String
     * @return boolean
     */
    @SuppressWarnings("unused")
    public static boolean isMasked(String inputString) {
        return inputString.matches("[x\\s]*");
    }

    /**
     * Add an unencrypted String to the log queue.
     * @param inputMessage - String
     * @return Encryptor
     */
    public Encryptor log(String inputMessage) {
        logQueue.add(inputMessage);
        return this;
    }


    /**
     * Encrypts a byte array. First, a symmetric (AES) key is generated. The key is then used to encrypt the data.
     * After encryption, an RSA Cipher encrypts the key using RSA encryption. The original AES key (encrypted), its
     * IV and the encrypted data are returned as a byte array. The encrypted key must be decrypted using the private key
     * corresponding to the public key's keypair for the data to be decrypted.
     *
     * @param inputData byte[]
     * @return encryptedData - byte[]
     */

    private byte[] encryptData(byte[] inputData) {

        try {

            if (!tenantKey())
            {
                logger.info("Encryption was attempted, but could not be performed -- public key was not found. Masking Audio");
                return mask(new String(inputData)).getBytes(StandardCharsets.UTF_8);
            }


            PublicKey publicKey = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM).generatePublic(new X509EncodedKeySpec(tenantPublicKey));

            //Symmetric key to support long key size
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGenerator.init(128);
            SecretKey symmetricKey = keyGenerator.generateKey();

            Cipher initialCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM + "/" + SYMMETRIC_PADDING);
            initialCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encryptedData = initialCipher.doFinal(inputData);
            byte[] iv = initialCipher.getIV();


            Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM + "/" + ASYMMETRIC_PADDING);
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedKey = rsaCipher.doFinal(symmetricKey.getEncoded());
            byte[] encryptedIV = rsaCipher.doFinal(iv);

            return packageDataString(encryptedData,encryptedIV,encryptedKey);

        } catch (Exception e) {
            logger.info("Encryption was attempted, but could not be performed: " + e.getMessage());
        }

        //This should never trigger, data was not encrypted if so
        return inputData;

    }


    public byte[] getEncryptedData(byte[] inputData)
    {
        byte[] encryptedData;
        try
        {
            encryptedData =  encryptData(inputData);
            return encryptedData;
        }catch (Exception e)
        {
            logger.debug("Failed to encrypt data : " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the argument, encrypted, as a String.
     *
     * @param inputMessage - String
     * @return encryptedinputMessage - String
     */
    private String encryptString(String inputMessage) {
        byte[] necessaryData = encryptData(inputMessage.getBytes(StandardCharsets.UTF_8));
        return new String(necessaryData, StandardCharsets.UTF_8);
    }

    /**
     * Encrypt the input String depending on the Encryptor's encryption
     * method, and add it to the log queue.
     * @param inputMessage - String
     * @return Encryptor
     */
    public Encryptor secure(String inputMessage) {

        switch (this.encryptionState) {
            case MASK:
                maskLog(inputMessage);
            case ENCRYPT:
                encryptedLog(inputMessage);
            case NONE:
                log(inputMessage);
        }
        return this;

    }

    /**
     * Adds the string in encrypted form to the log queue.
     *
     * @param inputMessage - String
     * @return this
     */

    public Encryptor encryptedLog(String inputMessage) {

        logQueue.add(encryptString(inputMessage));
        return this;

    }

    /**
     * Encrypts a byte array directly and adds it to the log queue.
     *
     * @param inputAudio - byte[]
     * @return this
     */
    public Encryptor encryptedLog(byte[] inputAudio) {
        try {
            logQueue.add(new String(encryptData(inputAudio)));
        } catch (Exception e) {
            logger.debug("Attempted to add encrypted log to queue, failed : " + e.getMessage());
        }
        return this;
    }

    /**
     * Returns an encrypted version of the input String, depending on the encryption method of
     * Encryptor.
     * @param inputMessage - String
     * @return String
     * @see ENCRYPTION_STATE
     */
    public String processString(String inputMessage) {
        switch (this.encryptionState) {
            case ENCRYPT:
                return encryptString(inputMessage);
            case MASK:
                return mask(inputMessage);
            case NONE:
                return inputMessage;
            default:
                return inputMessage;
        }
    }

    /**
     * Returns the inputData, encrypted, as a Base64 String.
     * @param inputData - byte[]
     * @return String
     */
    public String processString(byte[] inputData) {
        return new String(encryptData(inputData));
    }

    public Encryptor maskLog(String inputMessage) {

        logQueue.add(mask(inputMessage));
        return this;

    }

    private String mask(String inputString) {

        StringBuilder sb = new StringBuilder();
        char[] charArray = inputString.toCharArray();
        for (char c : charArray) {
            if (Character.isWhitespace(c)) {
                sb.append(' ');
            } else {
                sb.append(MASKING_CHAR);
            }
        }
        return sb.toString();
    }

    /**
     * When end is called, the Log Queue will chain all of its resting messages together as one String,
     * and then log the result.
     *
     * @return success - boolean
     */
    public boolean end() {
        StringBuilder sb = new StringBuilder();
        while (!logQueue.isEmpty()) {
            sb.append(logQueue.remove()).append(" ");
        }

        if (logQueue.isEmpty()) {
            logger.warn(sb.toString());
            return true;
        }
        return false;
    }

    public void setTenantPublicKey(byte[] inputKeyBytes)
    {
        this.tenantPublicKey = inputKeyBytes;
    }

    private boolean tenantKey()
    {
        return tenantPublicKey != null;
    }

    private byte[] packageDataString(byte[] encryptedData, byte[] encryptedIV, byte[] encryptedKey)
    {
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DATA:");
        stringBuilder.append(Base64.getEncoder().encodeToString(encryptedData));
        stringBuilder.append(":IV:");
        stringBuilder.append(Base64.getEncoder().encodeToString(encryptedIV));
        stringBuilder.append(":KEY:");
        stringBuilder.append(Base64.getEncoder().encodeToString(encryptedKey));
        stringBuilder.append(":END");

        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

}
