package com.nuance.cobra.testing;


import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.BeforeClass;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * Base class for tests using JWT authentication. Contains the needed keys meterial to generate and validate tokens.
 */
public class JwtAuthenticationTestBase extends LoggingTest
{
    static final String KEY_ID = "keyID-" + UUID.randomUUID().toString().split("-")[0];

    private static PrivateKey privateKey;
    private static PublicKey publicKey;


    @BeforeClass
    public  static void initKeys() throws JoseException
    {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        privateKey = rsaJsonWebKey.getPrivateKey();
        publicKey = rsaJsonWebKey.getPublicKey();
    }

    /**
     * Creates a claims object that will pass the minimal validations.
     */
    protected JwtClaims createBasicClaims()
    {
        JwtClaims jwtClaims = new JwtClaims();

        // Standard claims
        jwtClaims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        jwtClaims.setGeneratedJwtId(); // a unique identifier for the token
        jwtClaims.setIssuedAtToNow();  // when the token was issued/created (now)
        jwtClaims.setNotBeforeMinutesInThePast(0); // time before which the token is not yet valid (0 minutes ago)

        return jwtClaims;
    }

    protected String createToken(JwtClaims jwtClaims) throws JoseException
    {
        JsonWebSignature jws = new JsonWebSignature();

        jws.setPayload(jwtClaims.toJson());
        jws.setKey(privateKey);
        jws.setKeyIdHeaderValue(KEY_ID);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        return jws.getCompactSerialization();
    }

    protected static String getKeyId()
    {
        return KEY_ID;
    }

    protected static PublicKey getPublicKey()
    {
        return publicKey;
    }

    protected static PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    protected VerificationKeyResolver getKeyResolver()
    {
        return new VerificationKeyResolver()
        {
            @Override
            public Key resolveKey(JsonWebSignature jsonWebSignature, List<JsonWebStructure> list)
                    throws UnresolvableKeyException
            {
                String keyId = jsonWebSignature.getKeyIdHeaderValue();
                if (KEY_ID.equals(keyId))
                {
                    return publicKey;
                }
                else
                {
                    return null;
                }
            }
        };
    }
}
