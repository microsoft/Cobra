package com.nuance.cobra.utils.encryption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DecryptionToolTest
{
    @Test(expected = Test.None.class)
    public void decryptText() throws Exception
    {
        String expected = "pepperoni pizza";
        String encryptedText = "DATA:cI3is6AcaZdhuQXfK86aOw==:IV:iNOgupQ7a6Pe9bGQm7ALw653SuR8RRuonUW5iGnPt+3EDlOcd9/OhayTj+e84ebF9EQrudEaNhhIf1xAHmFw4MziftZ4xTTMbzkEQLBlWd/onqc58sTPp9flDQRfff/aHUyrqNYVI5zaKSAmxXnPt2iVUhU2KZbnIJTOYCZ1ojnL2ID992wkBubGljT9wde9zRjFgazZvqExPo9gWZH+1mJFFoCEJe5SwSAHy1ozctX+CXb7o8Q0Ne9ZE3drLG0cUdM0nV/tlwvN+ihbDIB4jtFkPsKynKSZ/I9P2gx/x8Pzf3/7w+djc5MrOalPaHcY8zvjmSib9r+ADbqLpebGLA==:KEY:VuI7pdFCaE0SX7t8VqIxDQv/7raNCdv/b3F4YpDbDlKtPcdZc7W1VRFvxoIRKq2RrWCW2UsUXYo5x8q0JnEM4t0fYBQsR8JdoNf5//aZepVYMkBXgUESwuiGd9C0bragnxgP35nraer/ohIlud0qXj6EDkL4BjsirULQVeo6jks+a+ZNlu7zCXZZaLaiStzWK5R3OvWicd6yco7jt/9uT3kcfo3X6A3V9ZdihHz6jY6yWlZG7kvj01lV9E/cfKtrQGAf0UXb76ARzCAsx/Hb0iOOHdQzdV0CBax8YKckxE+f4IBXtx+9cpnGoSCYCkrhaIUBixWP31qn/fYwaU5GPQ==:END";
        String decryptedText = DecryptionTool.decryptText(encryptedText);
        assertEquals(expected, decryptedText);
    }

    @Test(expected = Test.None.class)
    public void decryptAudio() throws Exception
    {
        DecryptionTool.decryptAudio("test/encryptedAudio.dat", "test/decryptedAudio.wav");
        File expected = new File("test/expectedAudio.wav");
        File decrypted = new File("test/decryptedAudio.wav");
        assertTrue("The files differ!", FileUtils.contentEquals(expected, decrypted));
        decrypted.delete();
    }
}

class DecryptionTool {

    public static final String privateKeyString = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDmHljG4sauD3UKSRdsCQuQIMDbrHcG7/RIY5L24DGW+ZHWHz4LmA6FsicOGpjfJKgZSGcrTQz95hTNwcRfY8+7hZLV2F3UQJtm/gcK57dCb/BUI51XntzgGHqUiUCnIICB/0ejTNWl6Ui00Gh/T4GDXg4o+6aO85v8snJ+UpDKY5uNO+5eRWc829vWU9BhtEOPjEyQqJWs19gqp7F41sb5dit1ym8JYhnBMI/xsBcsj89UlOVelHVQcuRaixvn6FOm0VaLt6TNRhFmipWoCRfahTaiudR9h3tyCcB1GHbBYEdbTKBJj2pN3IroZDNvju0HEZwEUj3qsJzEXrZshDVjAgMBAAECggEAKN2xyVwjiWzATbvON4bhsU5HwZs+phI98Q8R3drRo6ydfAefVqLBzXdmXShXBqW1XowPmk58JKjT//hNvdz1WqlteqGKhDOXBXLOYF7nGhrT9LVAb3uMYRWnyzIWs7dfavahS7xM4sJrgYTTUeyWJPSykg+itBdKF9W+5HUYNoCkvEdg7Ckyv346Xqy9+YKoF1+H4Ag9rhJ5xeTJ9Eouj06FF5pdJV1R6qaJQdipn24H5CJ3YtwMAtb1ps6YIDw+ccSePJEp0KgHgvnPU6WmQRqC4nvQJrWOj+Ho9MVjJDfg0TgSggVQjM50T3lqj1WaWGhMdjVIlntapOTNrDhPbQKBgQD1dlzejdHaY662dlzfr6cb4DRl8TjScEgpzhEKUCzpTAmLkUmxRIyR3bL0DGEEWwREYJizqrVBk2Ha2a9Qm6a0K387zE07M7BK+Ifqo6Qp4YbNV9pMcw1jCyBi9rguLsseoPxTGxg4ufqLM2RkzQNaqLbwP/mpqTZ8SAAiAZHe1QKBgQDv/1rqprYcqivn/00HdK4FLwFXX2oul4tL2wq6n6AuFXxG5Ov6JIzfOWhty3cjzCF1z7Hm/0aH7jppZl66AHYL4KedGktQE90xOyM/lv90EMEsv6rZHhe+0ySxHkpKGVcppkRMNi3YuOm0s851XqxPDLlYLqLXi+x1ieuM59cPVwKBgQDHsJc05yNGC7pi7j/4KipE301BcdDMQlQyzlXGkhs3YIiTZosRD7dcI4QmTrvOk00RY5RW9hWnV4NQS6/4X+hqwsgrmbVq6E4Pb8kC9kXjXHoSK9qIP0Ewxih+2kBUMfVE4ark4TH/VC2jeRe/eZm083SAV0uA3wxYZyrUf3pwUQKBgFyvg48nvyEm3zoDsN32Q9agpqX1pW5E/HjZKTWVvCjQB2Tht/is8PKsmxX8BBEBHtL+2Tfa0oRgjpJyBJdU4kIICvNzWi6c3UnMCvrd9939Mn1zsCmVo5oAtSTqC8q+KpldKvc73UzbbPZqkotleqo0wce6iw0vbqyeDSgnyTiVAoGBAN48+mZWHAPQ+ul7cowuquwDGb4ZJNM03d0+1caLknK1ygU8TXnI7IlqKDZzKUCLVUJ3Ncfonqw+1M9x72sVvfCBzx080Ztvfd5ZY29OKbwj2QMO1pWlcnluUd1gMjWDMgVqIIeRwCI6v2bZTXTBQJC+dy/Q29rhk0Ka1CN4Yom6";

    public static void decryptAudio(String filename) throws Exception {
        decryptAudio(filename, "audioOutput/decrypted.wav");	// defaults to "decrypted.wav"
    }

    public static void decryptAudio(String filename, String output) throws Exception {
        System.out.println("Decrypting file: "+filename);
        //privKeyString = new String(Files.readAllBytes(Paths.get(args[0])));

        boolean audioFilePassed;
        String audioFileString;
        byte[] audioBytes;
        ArrayList<String> necessaryData;

        audioBytes = Files.readAllBytes(Paths.get(filename));
        audioFileString = new String(audioBytes);
        audioFilePassed = true;

        //if (audioFilePassed)
        //{
        necessaryData = unpackEncryptionString(audioFileString);
        //}
        //else
        //{
        //    necessaryData = unpackEncryptionString(filename);
        //}

        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(
                (Base64.getDecoder().decode(privateKeyString))));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"/*, "BC"*/);
        cipher.init(Cipher.PRIVATE_KEY, privateKey);


        byte[] decryptedKey = cipher.doFinal(Base64.getDecoder().decode(necessaryData.get(2)));
        byte[] decryptedIv = cipher.doFinal(Base64.getDecoder().decode(necessaryData.get(1)));

        byte[] encryptedDataBytes = Base64.getDecoder().decode(necessaryData.get(0));

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(decryptedKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(decryptedIv);
        aesCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = aesCipher.doFinal(encryptedDataBytes);
        if (audioFilePassed)
        {
            System.out.println("Writing decrypted file");
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            fileOutputStream.write(decrypted);
            fileOutputStream.close();
        }
    }

    public static String decryptText(String encryptedText) throws Exception {
        ArrayList<String> data = unpackEncryptionString(encryptedText);

        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString)));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"/*, "BC"*/);
        cipher.init(Cipher.PRIVATE_KEY, privateKey);

        byte[] decryptedKey = cipher.doFinal(Base64.getDecoder().decode(data.get(2)));
        byte[] decryptedIv = cipher.doFinal(Base64.getDecoder().decode(data.get(1)));
        byte[] encryptedDataBytes = Base64.getDecoder().decode(data.get(0));

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(decryptedKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(decryptedIv);
        aesCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = aesCipher.doFinal(encryptedDataBytes);

        System.out.println("Decrypted Text: "+new String(decrypted));

        return new String(decrypted);
    }

    private static ArrayList<String> unpackEncryptionString(String encryptionString)
    {
        ArrayList<String> returnList = new ArrayList<>();
        String inputCopy = encryptionString;

        String data = StringUtils.substringBetween(inputCopy,"DATA:",":IV:");
        String iv = StringUtils.substringBetween(inputCopy , ":IV:",":KEY:");
        String key = StringUtils.substringBetween(inputCopy,":KEY:",":END");

        returnList.add(data);
        returnList.add(iv);
        returnList.add(key);

        return returnList;
    }

}