package com.nuance.cobra.session;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nuance.cobra.testing.JwtAuthenticationTestBase;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.plugin.v1.IJwtValidationsConfigurator;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SecretCacheProvider.class, JWTVerifier.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.*"})
public class SessionValidatorTest extends JwtAuthenticationTestBase
{
    private CombinedConfiguration cfgs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private WebApiJSONConfiguration cfg = (WebApiJSONConfiguration) cfgs.getConfiguration(0);

    private static String jwtToken;

    private static String verCode = "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c";

    private static String DEFAULT_ISSUER = "1stIssuer";
    private static final String CLAIM_SUBJECT_VALUE = "WebAPI";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private String createJwtWithoutKid() {
        String headerWithoutKid = Base64.getEncoder().encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder().encodeToString("{\"sub\":\"1234567890\",\"name\":\"John Doe\"}".getBytes());

        return headerWithoutKid + "." + payload + ".";
    }

    private void setupCommonAADConfiguration() {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        cfg.setProperty(WebApiConfig.Property.AZURE_TENANT_ID.key, "testAzureTenantId");
        cfg.setProperty(WebApiConfig.Property.AZURE_CLIENT_ID.key, "testAzureClientId");
    }

    private void setupAndThrowAADTokenException(Class<? extends Exception> exceptionType, boolean isClassCast) throws Exception {
        setupCommonAADConfiguration();

        Jwk mockJwk = mock(Jwk.class);
        JwkProvider mockProvider = mock(JwkProvider.class);
        JWTVerifier mockVerifier = mock(JWTVerifier.class);

        if(isClassCast) {
            when(mockJwk.getPublicKey()).thenThrow(ClassCastException.class);
        } else {
            when(mockJwk.getPublicKey()).thenReturn(mock(RSAPublicKey.class));
            doThrow(exceptionType).when(mockVerifier).verify(anyString());
        }
        when(mockProvider.get(anyString())).thenReturn(mockJwk);

        SessionValidator validator = new SessionValidator(cfgs) {
            @Override
            protected JwkProvider createJwkProvider(String azureTenantId, String azureKeysUrlFormat, long azureMaxCachedKeys, long azureCachedLifespanHours) {
                return mockProvider;
            }

            @Override
            protected JWTVerifier createJwtVerifier(Algorithm algorithm, String azureTenantId, String azureClientId, String subject, String azureIssuerURLFormat) {
                return mockVerifier;
            }
        };

        assertFalse(validator.validate());
    }
    private JwtClaims toJwtClaims() {
        JwtClaims jwtClaims = createBasicClaims();

        // Standard claims
        jwtClaims.setIssuer(DEFAULT_ISSUER); // who creates the token and signs it
        jwtClaims.setAudience(SessionValidator.CLAIM_AUDIENCE_VALUE); // to whom the token is intended to be sent
        jwtClaims.setSubject(CLAIM_SUBJECT_VALUE); // the subject/principal is whom the token is about
        jwtClaims.setStringClaim(SessionValidator.SESSION_TIMEOUT_SECONDS, "100");

        return jwtClaims;
    }


    @Before
    public void setUp() throws Exception {
        jwtToken = createToken(toJwtClaims());

        String publicKeyPEM = RsaKeyUtil.pemEncode(getPublicKey()).replaceAll("\r\n", "");
        System.out.println("\"" + getKeyId() + "\" : \"" + publicKeyPEM + "\"");

        String privateKeyString = "-----BEGIN PRIVATE KEY-----\n" // gitleaks:allow
                               + Base64.getEncoder()
                                       .encodeToString(getPrivateKey().getEncoded()) + "\n"
                               + "-----END PRIVATE KEY-----\n";

        System.out.println("private key: \"" + getKeyId() + "\" : \"" + privateKeyString + "\"");

        System.out.println();
        System.out.println("\"token\" : \"" + jwtToken + "\"");
        System.out.println();

        cfg.setProperty(WebApiConfig.Property.JWT_PUBLIC_KEYS.key + "." + getKeyId(), publicKeyPEM);

        cfg.setProperty(WebApiConfig.Property.NMAID_KEY.key,"b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        cfg.setProperty(WebApiConfig.Property.VERIFICATION_CODE.key,verCode);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,130);
        cfg.setProperty(WebApiConfig.Property.JWT_ISSUER.key, DEFAULT_ISSUER);
        cfg.setProperty(WebApiConfig.Property.JWT_ALLOWED_CLOCK_SKEW_SECONDS.key,30);
        cfg.setProperty(WebApiConfig.Property.JWT_FORCE_EXPIRATION_CHECK.key,false);
        cfg.setProperty(WebApiConfig.Property.APPLICATION_KEY.key,"testTenant");

        ArrayList<String> validAlgorithms = new ArrayList<>();
        validAlgorithms.add(AlgorithmIdentifiers.RSA_USING_SHA256);
        cfg.setProperty(WebApiConfig.Property.JWT_HASH_ALGORITHM_WHITELIST.key,validAlgorithms);

        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void validateNoAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.no_auth.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.VERIFICATION_CODE.key, null);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.no_auth.toString());

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());
    }

    @Test
    public void validateInvalidNoAuth(){
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.no_auth.toString());
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_USER.key,"xyz");
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_PASS.key,"xyz");

        HashMap<String, String> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "Basic eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key,authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void validateNoAuthDisabled() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);

        HashMap<String, String> authData = null;

        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key,authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void validateHttpAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_USER.key,"xyz");
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_PASS.key,"xyz");

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "Basic eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());
    }

    @Test
    public void invalidAuthTypeHttpAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_USER.key,"wrong");
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_PASS.key,"pass");

        HashMap<String, String> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "Basic eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key,authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void acceptInvalidCredsWhenUsingDeletegedAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());
        authTypes.add(AuthTypeAndData.ValidationTypes.delegated.toString());

        // Having delegated as part of the authentication methods should accept the request even though the credential
        // presented are not valid.

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_USER.key,"wrong");
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_PASS.key,"pass");

        HashMap<String, String> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "Basic eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key,authData);

        SessionValidator validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());
    }

    @Test
    public void invalidateHttpAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_USER.key,"xyz");
        cfg.setProperty(WebApiConfig.Property.BASIC_AUTH_PASS.key,"xyz");

        HashMap<String, String> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "WrongField eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key,authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void validateHash() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, verCode);

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        SessionValidator validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());

        authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "");
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void validateHashWithExpiry() {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, verCode);

        WebApiJSONConfiguration authConfig = new WebApiJSONConfiguration();
        Object[] authInfo = new Object[1];
        authInfo[0] = Map.of("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b", "expires", LocalDate.now(ZoneId.systemDefault()).plus(1, ChronoUnit.DAYS).toString());
        authConfig.load(Map.of(WebApiConfig.Property.NMAID_KEY.key, authInfo));
        cfgs.addConfiguration(authConfig);

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        SessionValidator validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());
        assertFalse(outContent.toString().contains("Authentication key for testTenant is near expiration"));

        authInfo[0] = Map.of("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b", "expires", LocalDate.now(ZoneId.systemDefault()).toString());
        authConfig.load(Map.of(WebApiConfig.Property.NMAID_KEY.key, authInfo));

        validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());
        assertTrue(outContent.toString().contains("Authentication key for testTenant is near expiration"));

        authInfo[0] = Map.of("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b", "expires", LocalDate.now(ZoneId.systemDefault()).minus(1, ChronoUnit.DAYS).toString());
        authConfig.load(Map.of(WebApiConfig.Property.NMAID_KEY.key, authInfo));

        validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());

        String timeFormat = "yyyyMMddHHmm";
        DateTimeFormatter f = DateTimeFormatter.ofPattern(timeFormat);
        cfg.setProperty(WebApiConfig.Property.TIME_FORMAT.key, timeFormat);
        authInfo[0] = Map.of("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b", "expires", f.format(LocalDateTime.now(ZoneId.systemDefault()).plus(1, ChronoUnit.DAYS)));
        authConfig.load(Map.of(WebApiConfig.Property.NMAID_KEY.key, authInfo));

        validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());

        timeFormat = "yyyyMMddHHmm z";
        f = DateTimeFormatter.ofPattern(timeFormat);
        cfg.setProperty(WebApiConfig.Property.TIME_FORMAT.key, timeFormat);
        authInfo[0] = Map.of("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b", "expires", f.format(ZonedDateTime.now(ZoneId.systemDefault()).plus(1, ChronoUnit.DAYS)));
        authConfig.load(Map.of(WebApiConfig.Property.NMAID_KEY.key, authInfo));

        validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());
    }

    @Test
    public void validateHashFalse() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        CombinedConfiguration cfgs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration cfg = cfgs.getConfiguration(0);
        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.NMAID_KEY.key,"b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        cfg.setProperty(WebApiConfig.Property.VERIFICATION_CODE.key,"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1");
        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void validateJwtSessionUpdate() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,130);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, "JWS");
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        cfg.setProperty(WebApiConfig.Property.JWT_FORCE_EXPIRATION_CHECK.key,true);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());
    }

    @Test
    public void validateJwtNoSessionUpdate() throws Exception
    {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key, 30);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());
    }

    @Test
    public void validateJwtBasicCustomValidationsOk() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,30);

        JwtClaims claims = createBasicClaims();
        String jwtToken = createToken(claims);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        SessionValidator validator = new SessionValidator(cfgs, Optional.of(new IJwtValidationsConfigurator()
        {
            @Override
            public void configureBuilder(JwtConsumerBuilder claimsConsumerBuilder)
            {
                // intentionally left empty, we only want to validate that basic validation (expiry) pass.
            }
        }));

        assertTrue(validator.validate());
    }


    @Test
    public void validateJwtCustomAudienceValidations() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,30);

        final String AUD1 = "aud1";
        final String AUD2 = "aud2";

        final String[] ALT_AUDIENCE = {AUD1, AUD2};

        JwtClaims claims = createBasicClaims();
        claims.setAudience(ALT_AUDIENCE);

        String jwtToken = createToken(claims);

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        SessionValidator validator = new SessionValidator(cfgs, Optional.of(new IJwtValidationsConfigurator()
        {
            @Override
            public void configureBuilder(JwtConsumerBuilder claimsConsumerBuilder)
            {
                claimsConsumerBuilder.setExpectedAudience(AUD1);
            }
        }));

        assertTrue(validator.validate());

        // Create token with invalid audience, check that validation fails.
        claims = createBasicClaims();
        claims.setAudience(AUD2);
        jwtToken = createToken(claims);

        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        assertFalse(validator.validate());
    }


    @Test
    public void validateGenericAuth() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.jws.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,130);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());

        authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);

        authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, verCode);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,130);

        validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());
    }

    @Test
    public void invalidGenericAuthType() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.jws.toString());
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        HashMap<String, String> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, "invalid type");
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, jwtToken);

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);
        cfg.setProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        cfg.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key,130);

        SessionValidator validator = new SessionValidator(cfgs);

        assertFalse(validator.validate());
    }

    @Test
    public void validateHashcodeKeyVault() throws Exception {
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.hashcode.toString());

        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key,authTypes);
        cfg.setProperty(WebApiConfig.Property.KEY_VAULT_TENANT_SECRET_NAME.key, "keyVaultTenantSecretNameTest");
        cfg.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "7d7f1b8e-038a-4b41-850b-3a8b949f7135");
        cfg.setProperty(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key, "https://entrd-dev-secrets.vault.azure.net");

        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.hashcode.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, verCode);
        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode node = factory.objectNode();

        //Make NmaidKey a string
        node.put("nmaidKey", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");

        // Mock SecretCacheProvider and stub the methods
        SecretCacheProvider providerMock = mock(SecretCacheProvider.class);
        when(providerMock.getSecretFromKeyVault(anyString(), any(CombinedConfiguration.class))).thenReturn(node);

        // Mock the static method SecretCacheProvider.getInstance()
        PowerMockito.mockStatic(SecretCacheProvider.class);
        when(SecretCacheProvider.getInstance()).thenReturn(providerMock);

        SessionValidator validator = new SessionValidator(cfgs);

        assertTrue(validator.validate());

        //Make NmaidKey an array of objects
        JsonNodeFactory factory2 = JsonNodeFactory.instance;
        ArrayNode arrayNode = factory2.arrayNode();

        String timeFormat = "yyyyMMddHHmm z";
        cfg.setProperty(WebApiConfig.Property.TIME_FORMAT.key, timeFormat);
        DateTimeFormatter f = DateTimeFormatter.ofPattern(timeFormat);

        ObjectNode mainNode = factory.objectNode();
        ObjectNode nnmaidKey1 = factory2.objectNode();

        nnmaidKey1.put("key", "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b");
        nnmaidKey1.put("expires", f.format(ZonedDateTime.now(ZoneId.systemDefault()).plus(1, ChronoUnit.DAYS)));

        arrayNode.add(nnmaidKey1);
        mainNode.put("nmaidKey", arrayNode);

        // Mock SecretCacheProvider and stub the methods
        SecretCacheProvider providerMock2 = mock(SecretCacheProvider.class);
        when(providerMock2.getSecretFromKeyVault(anyString(), any(CombinedConfiguration.class))).thenReturn(mainNode);
        when(SecretCacheProvider.getInstance()).thenReturn(providerMock2);

        assertTrue(validator.validate());
    }

    @Test
    public void validateHttpAuthKeyVault() throws Exception {
        // Setup authentication types
        List<String> authTypes = new ArrayList<>();
        authTypes.add(AuthTypeAndData.ValidationTypes.http_auth.toString());
        cfg.setProperty(WebApiConfig.Property.ACTIVE_AUTH_METHODS.key, authTypes);

        // Setup basic authentication accounts
        List<Map<String, String>> basicAuthAccounts = new ArrayList<>();

        Map<String, String> account1 = new HashMap<>();
        account1.put(WebApiConfig.Property.BASIC_AUTH_USER.key, "test");
        account1.put(WebApiConfig.Property.BASIC_AUTH_PASS.key, "test");
        basicAuthAccounts.add(account1);

        Map<String, String> account2 = new HashMap<>();
        account2.put(WebApiConfig.Property.BASIC_AUTH_USER.key, "xyz");
        account2.put(WebApiConfig.Property.BASIC_AUTH_PASS.key, "xyz");
        basicAuthAccounts.add(account2);

        WebApiJSONConfiguration authConfig = new WebApiJSONConfiguration();
        authConfig.load(Map.of(WebApiConfig.Property.BASIC_AUTH_ACCOUNTS.key, basicAuthAccounts));
        cfgs.addConfiguration(authConfig);

        // Setup authentication data
        HashMap<String, Object> authData = new HashMap<>();
        authData.put(SessionValidator.AUTHENTICATION_TYPE_NAME, AuthTypeAndData.ValidationTypes.http_auth.toString());
        authData.put(SessionValidator.AUTHENTICATION_GENERIC_DATA, "Basic eHl6Onh5eg=="); //Basic xyz:xyz

        cfg.setMapProperty(WebApiConfig.Property.AUTH_DATA.key, authData);
        // Validate with proper accounts
        SessionValidator validator = new SessionValidator(cfgs);
        assertTrue(validator.validate());

        // Clear the basic authentication accounts and validate again
        basicAuthAccounts.clear();
        SessionValidator validator2 = new SessionValidator(cfgs);
        assertFalse(validator2.validate());
    }

    @Test
    public void invalidateJwsAADToken() {
        setupCommonAADConfiguration();
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void invalidateJwsAADTokenInvalidKid() {
        jwtToken = createJwtWithoutKid();
        setupCommonAADConfiguration();
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void validateJwsAADTokenSignature() throws Exception {
        setupCommonAADConfiguration();

        // Create a mock of JwkProvider
        JwkProvider mockProvider = mock(JwkProvider.class);
        // Mock JSON Web Key
        Jwk mockJwk = mock(Jwk.class);
        when(mockProvider.get(anyString())).thenReturn(mockJwk);
        // Mock an RSA Public Key type
        RSAPublicKey mockRSAPublicKey = mock(RSAPublicKey.class);
        when(mockJwk.getPublicKey()).thenReturn(mockRSAPublicKey);
        // Mock JWTVerifier
        JWTVerifier mockVerifier = mock(JWTVerifier.class);
        // Define the behavior for the verify method to avoid the exception
        doAnswer(invocation -> null).when(mockVerifier).verify(anyString());

        // Creating a subclass for testing
        SessionValidator validator = new SessionValidator(cfgs) {
            @Override
            protected JwkProvider createJwkProvider(String azureTenantId, String azureKeysUrlFormat, long azureMaxCachedKeys, long azureCachedLifespanHours) {
                return mockProvider;
            }

            @Override
            protected JWTVerifier createJwtVerifier(Algorithm algorithm, String azureTenantId, String azureClientId, String subject, String azureIssuerURLFormat) {
                return mockVerifier;
            }
        };

        assertTrue(validator.validate());
    }

    @Test
    public void testIsValidAADTokenClassCastException() throws Exception {
        setupAndThrowAADTokenException(null, true);
    }

    @Test
    public void testIsValidAADTokenExpiredException() throws Exception {
        setupAndThrowAADTokenException(TokenExpiredException.class, false);
    }

    @Test
    public void testIsValidAADTokenSignatureException() throws Exception {
        setupAndThrowAADTokenException(SignatureVerificationException.class, false);
    }

    @Test
    public void testIsValidAADTokenJWTVerificationException() throws Exception {
        setupAndThrowAADTokenException(JWTVerificationException.class, false);
    }

    @Test
    public void testInvalidAzureKeysUrlFormatMalformedURLException() {
        setupCommonAADConfiguration();
        cfg.setProperty(WebApiConfig.Property.AZURE_KEYS_URL_FORMAT.key, "invalidURLFormat");
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void testAzureIssuerURLWithoutPlaceholderNetworkException() {
        setupCommonAADConfiguration();
        cfg.setProperty(WebApiConfig.Property.AZURE_ISSUER_URL_FORMAT.key, "https://sts.windows.net/");
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void testInvalidAzureCachedLifespanHoursNetworkException() {
        setupCommonAADConfiguration();
        cfg.setProperty(WebApiConfig.Property.AZURE_CACHE_LIFESPAN_HOURS.key, 0);
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }

    @Test
    public void testInvalidAzureMaxCachedKeysIllegalArgumentException() {
        setupCommonAADConfiguration();
        cfg.setProperty(WebApiConfig.Property.AZURE_MAX_CACHED_KEYS.key, -1);
        SessionValidator validator = new SessionValidator(cfgs);
        assertFalse(validator.validate());
    }
}