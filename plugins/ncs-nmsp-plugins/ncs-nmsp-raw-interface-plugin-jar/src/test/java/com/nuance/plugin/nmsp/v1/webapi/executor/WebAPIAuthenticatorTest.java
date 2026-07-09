package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.WebAPIAuthenticator;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import io.netty.channel.Channel;
import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebAPIAuthenticatorTest extends TestCase {

    @Test
    public void testOnAcceptClient_sameNmaidKey() throws IOException, DecoderException, GeneralSecurityException {
        String nmaidKey = "a7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b"; //gitleaks:allow
        assertTrue(utilTestOnAcceptClient(nmaidKey, nmaidKey));

        nmaidKey = "1234";
        assertTrue(utilTestOnAcceptClient(nmaidKey, nmaidKey));

        nmaidKey = "a1b2c3d4e5f6";  //gitleaks:allow
        assertTrue(utilTestOnAcceptClient(nmaidKey, nmaidKey));
    }

    @Test
    public void testOnAcceptClient_differentNmaidKey() throws IOException, DecoderException, GeneralSecurityException {
        String clientNmaidKey = "a7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b"; //gitleaks:allow
        String serverNmaidKey = "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b"; //gitleaks:allow
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey));

        clientNmaidKey = "1234";
        serverNmaidKey = "4321";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey));

        clientNmaidKey = "abcdef";
        serverNmaidKey = "a8b2d049bd02ac9b";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey));

        clientNmaidKey = "";
        serverNmaidKey = "a8b2d049bd02ac9b";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey));
    }

    @Test
    public void testOnAcceptClient_authMap() throws IOException, DecoderException, GeneralSecurityException {
        String clientNmaidKey = "a7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b"; //gitleaks:allow
        String serverNmaidKey = "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b"; //gitleaks:allow
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey, true));

        clientNmaidKey = "1234";
        serverNmaidKey = "4321";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey, true));

        clientNmaidKey = "abcdef";
        serverNmaidKey = "a8b2d049bd02ac9b";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey, true));

        clientNmaidKey = "";
        serverNmaidKey = "a8b2d049bd02ac9b";
        assertFalse(utilTestOnAcceptClient(clientNmaidKey, serverNmaidKey, true));
    }

    public void test_failureToMatchEncryptedSession() throws IOException {
        // shared between client and server
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        when(session.getWebAPIOrchestrator().getWebAPISessionID()).thenReturn("djfeh2dj01-def2j3d-2fejd923-dj2efw");
        byte[] clientEncryptSessionId = "fakeSessionId".getBytes();

        // server side
        Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn(clientEncryptSessionId);
        when(session.getWebAPIOrchestrator().getWebAPISessionID()).thenReturn("djfeh2dj01-def2j3d-2fejd923-dj2efw");

        Object objKey = new Object[] {"serverNmaidKey_string"};
        when(mockSession.getServerNmaidKey()).thenReturn(objKey);

        assertFalse(WebAPIAuthenticator.onAcceptClient(mockSession, clientEncryptSessionId));
    }

    public void test_unsupportedNmaidKeyFormat() throws IOException {
        // shared between client and server
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        when(session.getWebAPIOrchestrator().getWebAPISessionID()).thenReturn("djfeh2dj01-def2j3d-2fejd923-dj2efw");
        byte[] clientEncryptSessionId = "fakeSessionId".getBytes();

        // server side
        Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn(clientEncryptSessionId);
        when(session.getWebAPIOrchestrator().getWebAPISessionID()).thenReturn("djfeh2dj01-def2j3d-2fejd923-dj2efw");

        Object objKey = new Object[] {7};
        when(mockSession.getServerNmaidKey()).thenReturn(objKey);

        assertFalse(WebAPIAuthenticator.onAcceptClient(mockSession, clientEncryptSessionId));
    }

    public void test_failureToGetNmaidKey() throws IOException {
        byte[] clientEncryptSessionId = "fakeSessionId".getBytes();
        Session mockSession = mock(Session.class);
        when(mockSession.getServerNmaidKey()).thenThrow(new IOException());

        assertFalse(WebAPIAuthenticator.onAcceptClient(mockSession, clientEncryptSessionId));
    }

    private boolean utilTestOnAcceptClient(String clientNmaidKey_string, String serverNmaidKey_string) throws IOException, DecoderException, GeneralSecurityException {
        return utilTestOnAcceptClient(clientNmaidKey_string, serverNmaidKey_string, false);
    }

    private boolean utilTestOnAcceptClient(String clientNmaidKey_string, String serverNmaidKey_string, boolean authAsMap) throws IOException, DecoderException, GeneralSecurityException {
        // shared between client and server
        Session session = new Session(mock(Channel.class), mock(WebAPIOrchestrator.class));
        when(session.getWebAPIOrchestrator().getWebAPISessionID()).thenReturn("djfeh2dj01-def2j3d-2fejd923-dj2efw");
        byte[] sessionId_bytes = session.getSessionId();

        // client side
        byte[] clientNmaidKey_bytes = Hex.decodeHex(clientNmaidKey_string.toCharArray());

        byte[] clientEncryptSessionId;
        if (clientNmaidKey_bytes.length == 0) {
            // SecretKeySpec rejects empty keys; emulate a malformed client by sending empty bytes
            clientEncryptSessionId = new byte[0];
        } else {
            Mac hmac = Mac.getInstance("HmacSHA1");
            hmac.init(new SecretKeySpec(clientNmaidKey_bytes, "HmacSHA1"));
            clientEncryptSessionId = hmac.doFinal(sessionId_bytes);
        }

        // server side
        Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn(sessionId_bytes);

        Object objKey = authAsMap ? new Object[] {Map.of("key", serverNmaidKey_string, "expires", "irrelevant")} : new Object[] {serverNmaidKey_string};
        when(mockSession.getServerNmaidKey()).thenReturn(objKey);

        return WebAPIAuthenticator.onAcceptClient(mockSession, clientEncryptSessionId);
    }
}
