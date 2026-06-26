package com.nuance.cobra.client.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ManagedIdentityCredential.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.*"})
public class AzureTokenProviderTest {

    @Mock
    private ManagedIdentityCredential mockManagedIdentityCredential;

    private AzureTokenProvider azureTokenProvider;

    private AccessToken validToken;
    private AccessToken expiredToken;

    @Before
    public void setUp() {
        azureTokenProvider = new AzureTokenProvider("test-client-id");

        Whitebox.setInternalState(azureTokenProvider, "managedIdentityCredential", mockManagedIdentityCredential);

        validToken = new AccessToken("valid_token", OffsetDateTime.now().plusMinutes(30));
        expiredToken = new AccessToken("expired_token", OffsetDateTime.now().minusMinutes(30));
    }

    @Test
    public void testGetAccessToken() {
        when(mockManagedIdentityCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(validToken));

        String validToken = azureTokenProvider.getAccessToken();

        assertEquals("valid_token", validToken);
    }

    @Test
    public void testGetAccessTokenWithExpiredToken() {
        Whitebox.setInternalState(azureTokenProvider, "cachedToken", expiredToken);

        when(mockManagedIdentityCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(validToken));

        String newToken = azureTokenProvider.getAccessToken();

        assertEquals("valid_token", newToken);
    }

    @Test
    public void testGetAccessCachedToken() {
        when(mockManagedIdentityCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(validToken));

        String validToken = azureTokenProvider.getAccessToken();

        assertEquals("valid_token", validToken);

        String cachedToken = azureTokenProvider.getAccessToken();

        assertEquals("valid_token", cachedToken);

    }

    @Test(expected = NullPointerException.class)
    public void testGetAccessTokenFailsToObtainEmptyToken() {
        when(mockManagedIdentityCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.empty());

        azureTokenProvider.getAccessToken();
    }

}
