package com.nuance.cobra.session;

import com.auth0.jwk.NetworkException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.hash.CobraHash;
import com.nuance.plugin.v1.IJwtValidationsConfigurator;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.google.common.hash.HashCode;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

public class SessionValidator {
    private static final Logger logger = LogManager.getLogger();

    private static final String EMPTY_SHA256 = CobraHash.sha256("");
    private static final String USERNAME_KEY = "username";
    private static final String PWD_REFERENCE_KEY = "pwd_ref";
    private static final String BASIC_AUTH_ACCOUNTS_KEY = "basicAuthenticationAccounts";
    private static final String BASIC_AUTH_SEPARATOR = ":";

    public static final String AUTHENTICATION_TYPE_NAME = "type";
    public static final String AUTHENTICATION_GENERIC_DATA = "data";

    public static final String SESSION_TIMEOUT_SECONDS = "https://nuance.com/session_exp";
    public static final String CLAIM_AUDIENCE_VALUE = "NUANCE";
    public static final String AUTHENTICATION_KEY_IS_NEAR_EXPIRATION = "Authentication key for {} is near expiration";

    private Optional<IJwtValidationsConfigurator> jwtValidationsConfigurator;

    private final CombinedConfiguration localConfig;
    private List<AuthTypeAndData.ValidationTypes> activeValidators;

    public SessionValidator(CombinedConfiguration config, Optional<IJwtValidationsConfigurator> jwtValidationsConfigurator) {
        localConfig = config;
        this.jwtValidationsConfigurator = jwtValidationsConfigurator;

        activeValidators = AuthTypeAndData.ValidationTypes.buildFromStrings(localConfig.getList(String.class, WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,
                Collections.singletonList(AuthTypeAndData.ValidationTypes.hashcode.toString())));
    }

    public SessionValidator(CombinedConfiguration config) {
        this(config, Optional.empty());
    }

    public boolean validate() {
        if (doValidation()) {
            return true;
        } else {
            try {
                Thread.sleep(1000); // prevent brute force attacks
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private boolean doValidation() {
        try {
            // Special case, if delegated is specfied, the request is considered preauthorized and valid.
            // Not to be confused with no_auth, which expects that the Authorization header be absent.
            if (activeValidators.contains(AuthTypeAndData.ValidationTypes.delegated)) {
                return true;
            }

            AuthTypeAndData authTypeAndData = new AuthTypeAndData(localConfig);

            if (activeValidators.contains(authTypeAndData.getAuthType())) {
                switch (authTypeAndData.getAuthType()) {
                    case jws:
                        return jwsValidator(authTypeAndData.getAuthData());
                    case hashcode:
                        return sha256Validator(authTypeAndData.getAuthData());
                    case http_auth:
                        return httpAuth(authTypeAndData.getAuthData());
                    case no_auth:
                        return true; //No authentication.
                    default:
                        logger.warn("AuthType not supported {}", authTypeAndData.getAuthType());
                }
            } else {
                logger.error("Validation method not permitted: {}", authTypeAndData.getAuthType());
                return false;
            }
        } catch (Exception e) {
            logger.error("Validation exception.", e);
        }
        return false;
    }

    private boolean httpAuth(String authData) {
        if (authData == null) {
            logger.error("Malformed http_auth data, terminating validation.");
            return false;
        }

        String authType = authData.trim().split(" ")[0];

        if (authType.toLowerCase().equalsIgnoreCase("basic")) {
            String decodedData = new String(Base64.getDecoder().decode(authData.trim().split(" ")[1]));

            // Initialize the list with the single user-pass combination from the local config.
            List<Map<String, String>> basicAuthAccounts = new ArrayList<>();
            Map<String, String> defaultAuthAccount = new HashMap<>();
            if(localConfig.getString(WebApiConfig.Property.BASIC_AUTH_USER.key) != null && localConfig.getString(WebApiConfig.Property.BASIC_AUTH_PASS.key) != null){
                defaultAuthAccount.put(WebApiConfig.Property.BASIC_AUTH_USER.key, localConfig.getString(WebApiConfig.Property.BASIC_AUTH_USER.key));
                defaultAuthAccount.put(WebApiConfig.Property.BASIC_AUTH_PASS.key, localConfig.getString(WebApiConfig.Property.BASIC_AUTH_PASS.key));
                basicAuthAccounts.add(defaultAuthAccount);
            }

            // Add additional basic authentication accounts if they exist in key vault
            List<Map<String, String>> keyVaultAuthAccounts = (List<Map<String, String>>) localConfig.getProperty(WebApiConfig.Property.BASIC_AUTH_ACCOUNTS.key);
            if (keyVaultAuthAccounts != null && !keyVaultAuthAccounts.isEmpty()) {
                basicAuthAccounts.addAll(keyVaultAuthAccounts);
            }

            // Validate all the basic authentication accounts
            for (Map<String, String> account : basicAuthAccounts) {
                String basicAuthenticationUsername = account.get(WebApiConfig.Property.BASIC_AUTH_USER.key);
                String basicAuthenticationPasscode = account.get(WebApiConfig.Property.BASIC_AUTH_PASS.key);

                HashCode hashUsernamePass = HashCode.fromBytes((basicAuthenticationUsername + BASIC_AUTH_SEPARATOR + basicAuthenticationPasscode).getBytes(StandardCharsets.UTF_8));
                HashCode hashDecodedData = HashCode.fromBytes(decodedData.getBytes(StandardCharsets.UTF_8));

                if (hashUsernamePass.equals(hashDecodedData)) {
                    return true;
                }
            }
        } else {
            logger.error("Invalid http_auth authorization header, terminating validation.");
        }
        return false;
    }

    private boolean sha256Validator (String verificationCode) {
        try {
            if (Strings.isBlank(verificationCode) || verificationCode.equals(EMPTY_SHA256)) {
                logger.warn("Authentication failed. Client is trying to authenticate with an empty string.");
                return false;
            }

            Object nmaidKey = localConfig.getProperty(WebApiConfig.Property.NMAID_KEY.key);
            return isNmaidKeyValid(nmaidKey, verificationCode);
        } catch (Exception e) {
            logger.debug("Authentication failed", e);
        }

        logger.debug("Authentication failed, invalid verification code.");
        return false;
    }

    private boolean hasKeyExpired(String tenant, String input, DateTimeFormatter f) {
        boolean keyHasExpired;
        boolean keyisExpiringSoon;
        try {
            ZonedDateTime expiration = ZonedDateTime.parse(input, f);
            ZonedDateTime now = ZonedDateTime.now(expiration.getZone());
            keyHasExpired = now.isAfter(expiration);
            keyisExpiringSoon = now.plus(1, ChronoUnit.DAYS).isAfter(expiration);
        }
        catch (DateTimeParseException e) {
            // Fallback if no timezone is defined
            try {
                LocalDateTime expiration = LocalDateTime.parse(input, f);
                LocalDateTime now = LocalDateTime.now();
                keyHasExpired = now.isAfter(expiration);
                keyisExpiringSoon = now.plus(1, ChronoUnit.DAYS).isAfter(expiration);
            }
            catch (DateTimeParseException e2) {
                // Fallback if no time component is defined
                LocalDate expiration = LocalDate.parse(input, f);
                LocalDate now = LocalDate.now();
                keyHasExpired = now.isAfter(expiration);
                keyisExpiringSoon = now.plus(1, ChronoUnit.DAYS).isAfter(expiration);
            }
        }

        if(keyisExpiringSoon) {
            logger.warn(AUTHENTICATION_KEY_IS_NEAR_EXPIRATION, tenant);
        }
        return keyHasExpired;
    }

    private boolean jwsValidator(String token) {
        String azureTenantId = localConfig.getString(WebApiConfig.Property.AZURE_TENANT_ID.key);
        String azureClientId = localConfig.getString(WebApiConfig.Property.AZURE_CLIENT_ID.key);

        if(azureTenantId != null && azureClientId != null){
            return isValidAzureToken(token, azureTenantId, azureClientId);
        }

        try {
            String issuer = localConfig.getString(WebApiConfig.Property.JWT_ISSUER.key);
            if (issuer == null)
            {
                issuer = localConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key);
            }

            List<Object> allowedAlgorithm = localConfig.getList(WebApiConfig.Property.JWT_HASH_ALGORITHM_WHITELIST.key);
            JwtConsumerBuilder jwtBuilder = new JwtConsumerBuilder();
            jwtBuilder
                    .setAllowedClockSkewInSeconds(localConfig.getInt(WebApiConfig.Property.JWT_ALLOWED_CLOCK_SKEW_SECONDS.key)) // allow some leeway in validating time based claims to account for clock skew
                    .setVerificationKeyResolver(JwtKeyResolver.getResolver(localConfig)) // the resolver maps the key id to the public key.
                    .setJwsAlgorithmConstraints( // only allow RS256
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
                                    allowedAlgorithm.toArray(new String[allowedAlgorithm.size()])));

            if (localConfig.getBoolean(WebApiConfig.Property.JWT_FORCE_EXPIRATION_CHECK.key)) {
                jwtBuilder.setRequireExpirationTime() // enable expiration check.
                        .setRequireNotBefore() // enable not before.
                        .setRequireIssuedAt(); // enable issued at.
            }

            if (jwtValidationsConfigurator.isPresent()) {
                // a custom configurator for the validations to perform was provided
                jwtValidationsConfigurator.get().configureBuilder(jwtBuilder);
            }
            else {
                // additional default validations needed for the session validator.
                jwtBuilder
                        .setRequireSubject() // the JWT must have a subject claim.
                        .setExpectedAudience(CLAIM_AUDIENCE_VALUE) // to whom the JWT is intended for.
                        .setExpectedIssuer(issuer); // whom the JWT needs to have been issued by.
            }

            JwtConsumer jwtConsumer = jwtBuilder.build();
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            logger.trace("jwt claims = {}", jwtClaims);

            String jwtSessionTimeout = (String)jwtClaims.getClaimValue(SESSION_TIMEOUT_SECONDS);

            if (jwtSessionTimeout != null && !jwtSessionTimeout.isEmpty()) {
                Integer timeout = Integer.parseInt(jwtSessionTimeout);
                if (localConfig.getInt(
                        WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key) > timeout) {
                    CobraConfigurationUtils.setProperty(localConfig, WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key, timeout);
                }
            }

            return true;
        } catch (InvalidJwtException | InvalidKeySpecException | JoseException | NumberFormatException | InvalidPublicKeyInConfigException e) {
            logger.error("Caught exception while validating jwt code. ", e);
        }

        return false;
    }

    private boolean isNmaidKeyValid(Object nmaidKey, String verificationCode){
        if (nmaidKey instanceof String) {
            nmaidKey = new Object[]{nmaidKey};
        }
        for (Object keyObj : (Object[]) nmaidKey) {
            String key = null;
            boolean keyHasExpired = true;
            if(keyObj instanceof String) {
                key = (String)keyObj;
                keyHasExpired = false;
            }
            else {
                Map<String, String> keyInfo = (Map<String, String>) keyObj;
                key = keyInfo.get("key");
                String input = keyInfo.get("expires");
                DateTimeFormatter f = DateTimeFormatter.ofPattern(localConfig.getString(WebApiConfig.Property.TIME_FORMAT.key, (String)WebApiConfig.Property.TIME_FORMAT.getDefaultValue()));
                keyHasExpired = hasKeyExpired((String)localConfig.getProperty(WebApiConfig.Property.APPLICATION_KEY.key), input, f);
            }
            HashCode clientData = HashCode.fromBytes(verificationCode.getBytes(StandardCharsets.UTF_8));
            HashCode nmaidKeyData = HashCode.fromBytes(CobraHash.sha256(key).getBytes(StandardCharsets.UTF_8));

            if (!keyHasExpired && clientData.equals(nmaidKeyData)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidAzureToken(String token, String azureTenantId, String azureClientId) {
        try {
            // Decode the json web token
            DecodedJWT jwt = JWT.decode(token);

            // Validate the kid
            String kid = jwt.getKeyId();
            if (kid == null || kid.isEmpty()) {
                logger.error("Missing or empty 'kid' in JWT header.");
                return false;
            }

            // Grab the JwkProvider
            String azureKeysUrlFormat = localConfig.getString(WebApiConfig.Property.AZURE_KEYS_URL_FORMAT.key, (String)WebApiConfig.Property.AZURE_KEYS_URL_FORMAT.getDefaultValue());
            long azureMaxCachedKeys = localConfig.getLong(WebApiConfig.Property.AZURE_MAX_CACHED_KEYS.key, (Integer)WebApiConfig.Property.AZURE_MAX_CACHED_KEYS.getDefaultValue());
            long azureCachedLifespanHours = localConfig.getLong(WebApiConfig.Property.AZURE_CACHE_LIFESPAN_HOURS.key, (Integer)WebApiConfig.Property.AZURE_CACHE_LIFESPAN_HOURS.getDefaultValue());
            JwkProvider provider = createJwkProvider(azureTenantId, azureKeysUrlFormat, azureMaxCachedKeys, azureCachedLifespanHours);

            // Grab the JSON Web Key which contains the kid and public key
            Jwk jwk = provider.get(kid);


            // Validate the algorithm
            List<Object> allowedAlgorithm = localConfig.getList(WebApiConfig.Property.JWT_HASH_ALGORITHM_WHITELIST.key, (List)WebApiConfig.Property.JWT_HASH_ALGORITHM_WHITELIST.getDefaultValue());
            if (!allowedAlgorithm.contains(jwt.getAlgorithm())) {
                logger.error("Invalid signing algorithm: {}",  jwt.getAlgorithm());
                return false;
            }

            // Define the algorithm using the public key mapped to a specific kid
            PublicKey publicKey = jwk.getPublicKey();
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);

            // Create a JWT verifier
            String azureIssuerURLFormat = localConfig.getString(WebApiConfig.Property.AZURE_ISSUER_URL_FORMAT.key, (String)WebApiConfig.Property.AZURE_ISSUER_URL_FORMAT.getDefaultValue());
            JWTVerifier verifier = createJwtVerifier(algorithm, azureTenantId, azureClientId, jwt.getSubject(), azureIssuerURLFormat);

            // Verify the token
            verifier.verify(token);

            logger.info("AAD Token is valid");
            return true;
        }
        catch (ClassCastException e) {
            logger.error("Public Key from JWK is not an instance of RSAPublicKey", e);
        }
        catch (TokenExpiredException e) {
            logger.error("Token expired", e);
        }
        catch (SignatureVerificationException e) {
            logger.error("Signature verification", e);
        }
        catch (JWTVerificationException e) {
            logger.error("JWT verification error", e);
        }
        catch (MalformedURLException e) {
            logger.error("Not a valid URL.", e);
        }
        catch (NetworkException e) {
            logger.error("Network exception, invalid URL.", e);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid argument value", e);
        }
        catch (Exception e) {
            logger.error("Exception error", e);
        }

        return false;
    }

    protected JwkProvider createJwkProvider(String azureTenantId, String azureKeysUrlFormat, long azureMaxCachedKeys, long azureCachedLifespanHours) throws MalformedURLException {
        return new JwkProviderBuilder(new URL(String.format(azureKeysUrlFormat, azureTenantId)))
                .cached(azureMaxCachedKeys, azureCachedLifespanHours, TimeUnit.HOURS)
                .build();
    }

    protected JWTVerifier createJwtVerifier(Algorithm algorithm, String azureTenantId, String azureClientId, String subject, String azureIssuerURLFormat) {
        return JWT.require(algorithm)
                .withIssuer(String.format(azureIssuerURLFormat, azureTenantId))
                .withAudience(azureClientId)
                .withSubject(subject)
                .build();
    }
}
