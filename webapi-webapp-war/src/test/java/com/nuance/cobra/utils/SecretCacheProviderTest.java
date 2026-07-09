package com.nuance.cobra.utils;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.fasterxml.jackson.databind.JsonNode;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyVaultSecret.class, SecretClient.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.*"})
public class SecretCacheProviderTest {
    private KeyVaultSecret secret;
    private SecretCacheProvider provider;
    private SecretClient secretClient;
    private CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();

    @Before
    public void setUp() throws Exception {
        secret = PowerMockito.mock(KeyVaultSecret.class);
        secretClient = PowerMockito.mock(SecretClient.class);
        provider = PowerMockito.spy(SecretCacheProvider.getInstance());
        Map<String, KeyVaultSecret> secretCache = new HashMap<>();
        secretCache.put("test", secret);
        Whitebox.setInternalState(provider, "secretCache", secretCache);
        cc.setProperty(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key, "https://entrd-dev-secrets.vault.azure.net");
        cc.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "clientId12345");
    }

    @Test
    public void testGetSecretFromKeyVault() throws IOException {
        String secretValue = "{\"nmaidKey\": \"nmaidTestValue\"}";
        String keyVaultSecretName = "testSecret";
        when(secret.getValue()).thenReturn(secretValue);
        doReturn(secret).when(provider).getSecret(anyString(), any());

        JsonNode result = provider.getSecretFromKeyVault(keyVaultSecretName, cc);

        assertNotNull(result);
        assertEquals("nmaidTestValue", result.get("nmaidKey").asText());
    }

    @Test
    public void testGetSecretFromKeyVault_ReturnsNull() throws IOException {
        String keyVaultSecretName = "bob";

        Mockito.doReturn(secretClient).when(provider).createUserAssignedManagedIdentityCredential(any(CombinedConfiguration.class));

        JsonNode result = provider.getSecretFromKeyVault(keyVaultSecretName, cc);

        assertNull(result);
    }

    @Test
    public void testGetSecret() {
        String secretName = "testSecret";

        Mockito.doReturn(secretClient).when(provider).createUserAssignedManagedIdentityCredential(any(CombinedConfiguration.class));
        when(provider.getSecret(secretName, cc)).thenReturn(secret);

        KeyVaultSecret result = provider.getSecret(secretName, cc);
        assertNotNull(result);
        assertEquals(secret, result);
    }

    @Test
    public void testGetSecretCache() {
        Map<String, KeyVaultSecret> secretCache = provider.getSecretCache();

        assertNotNull(secretCache);
        assertTrue(secretCache.containsKey("test"));
    }
}