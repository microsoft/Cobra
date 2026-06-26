package com.nuance.cobra.client.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AzureTokenProvider {
    private final ManagedIdentityCredential managedIdentityCredential;
    private static final Logger logger = LogManager.getLogger(AzureTokenProvider.class);
    private AccessToken cachedToken;

    public AzureTokenProvider(String clientId) {
        this.managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                .clientId(clientId)
                .build();
    }

    public synchronized String getAccessToken() {
        if (cachedToken == null || cachedToken.isExpired()) {
            TokenRequestContext request = new TokenRequestContext()
                    .addScopes("https://cognitiveservices.azure.com/.default");
            cachedToken = managedIdentityCredential.getToken(request).block();

            if (cachedToken == null) {
                logger.error("Failed to obtain access token");
                throw new NullPointerException("Failed to obtain access token.");
            } else {
                logger.info("Successfully obtained new access token");
            }
        } else {
            logger.info("Using cached access token");
        }

        return cachedToken.getToken();
    }
}
