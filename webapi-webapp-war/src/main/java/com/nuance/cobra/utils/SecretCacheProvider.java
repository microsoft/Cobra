package com.nuance.cobra.utils;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.session.WebApiConfig;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SecretCacheProvider {
    private static SecretCacheProvider instance;
    private Map<String, KeyVaultSecret> secretCache;
    private static final Logger logger = LogManager.getLogger();


    private SecretCacheProvider() {
        this.secretCache = new HashMap<>();
    }

    public static synchronized SecretCacheProvider getInstance() {
        if (instance == null) {
            instance = new SecretCacheProvider();
        }
        return instance;
    }

    /**
     * Authenticate with a User Assigned Managed identity.
     */
    protected SecretClient createUserAssignedManagedIdentityCredential(CombinedConfiguration localConfig) {
        String clientId = localConfig.getString(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key);
        String vaultUrl = localConfig.getString(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key);

        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                .clientId(clientId)
                .build();
       
        // Azure SDK client builders accept the credential as a parameter
        return new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(managedIdentityCredential)
                .buildClient();
    }

    /**
     * Get the secret from Azure Key Vault
     */
    public KeyVaultSecret getSecret(String secretName, CombinedConfiguration localConfig) {
        KeyVaultSecret secret = secretCache.get(secretName);

        // If secret is not in cache, fetch it and add it to the cache
        if (secret == null) {
            SecretClient client = createUserAssignedManagedIdentityCredential(localConfig);
            secret = client.getSecret(secretName);
            secretCache.put(secretName, secret);
        }

        return secret;
    }

    public JsonNode getSecretFromKeyVault(String keyVaultSecretName, CombinedConfiguration localConfig)  {
        KeyVaultSecret secret = getSecret(keyVaultSecretName, localConfig);
        if(secret != null){
            try{
                String secretValue = secret.getValue();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readTree(secretValue);
            }
            catch (IOException e) {
                logger.error("Cannot get secret from key vault", e);
            }
        }
        return null;
    }

    public Map<String, KeyVaultSecret> getSecretCache() {
        return secretCache;
    }
}