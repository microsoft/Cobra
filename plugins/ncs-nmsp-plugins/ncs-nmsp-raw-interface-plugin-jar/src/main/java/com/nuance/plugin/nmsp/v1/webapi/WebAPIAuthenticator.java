package com.nuance.plugin.nmsp.v1.webapi;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IAuthenticationListener;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class WebAPIAuthenticator
{
    private static final Logger logger = LogManager.getLogger();

    private WebAPIAuthenticator () {
        //utility class.
    }

    public static void onConnect(Session session, IAuthenticationListener authListener) {
        authListener.authenticationChallenged(session);
    }

    public static boolean onAcceptClient(Session session, byte[] clientEncryptSessionId) {
        if (clientEncryptSessionId == null || clientEncryptSessionId.length == 0) {
            logger.warn("Trying to confirm with an empty session encryption string.");
            return false;
        }

        byte[] serverEncryptedSessionId;

        Object serverNmaidKey;
        try {
            serverNmaidKey = session.getServerNmaidKey();
        } catch (IOException e) {
            logger.warn("Failed to read nmain keys.", e);
            return false;
        }

        for (Object keyObj : (Object[]) serverNmaidKey) {
            String key = null;
            if(keyObj instanceof String) {
                key = (String)keyObj;
            }
            else if(keyObj instanceof Map){
                Map<String, String> keyInfo = (Map<String, String>) keyObj;
                key = keyInfo.get("key");
            }
            else {
                logger.error("Failed to authenticate client.");
                return false;
            }
            try {
                serverEncryptedSessionId = encryptSessionIdWithPossibleKeys(session.getSessionId(), key);
            } catch (NoSuchAlgorithmException | InvalidKeyException | DecoderException e) {
                logger.warn("Error while verifying encrypted session id. failed on key: '{}'.", key.substring(0, Math.min(key.length(), 4)), e);
                continue;
            }

            if (key.length() > 0
                    && MessageDigest.isEqual(serverEncryptedSessionId, clientEncryptSessionId)) {
                session.setClientNmaidKey(key); //needed for webapi auth
                return true;
            }
        }

        logger.warn("Failed to authenticate client.");
        return false;
    }

    private static byte[] encryptSessionIdWithPossibleKeys(byte[] sessionId, String serverNmaidKey) throws NoSuchAlgorithmException, InvalidKeyException, DecoderException {
        return encrypt(sessionId, serverNmaidKey);
    }

    private static byte[] encrypt(byte[] sessionId, String key) throws DecoderException
    {
        byte[] keyBytes = Hex.decodeHex(key.toCharArray());

        SecretKey secKey = new SecretKeySpec(keyBytes, "HmacSHA1");

        HashFunction mac = Hashing.hmacSha1(secKey);
        HashCode hc = mac.hashBytes(sessionId);
        return hc.asBytes();
    }
}
