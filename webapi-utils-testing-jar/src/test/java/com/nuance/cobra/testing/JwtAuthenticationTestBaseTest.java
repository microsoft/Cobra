package com.nuance.cobra.testing;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.junit.Test;

import static org.junit.Assert.*;


public class JwtAuthenticationTestBaseTest
{

    class TestClass
            extends JwtAuthenticationTestBase
    {
        /**
         * simulates a test
         */
        public void pseudoTest()
                throws JoseException, InvalidJwtException
        {
            JwtClaims claims = createBasicClaims();
            String jwtToken = createToken(claims);

            JwtConsumerBuilder jwtBuilder = new JwtConsumerBuilder();
            jwtBuilder
                    .setAllowedClockSkewInSeconds(5)
                    .setVerificationKeyResolver(getKeyResolver());

            JwtConsumer jwtConsumer = jwtBuilder.build();

            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwtToken);

            System.out.println("claims = " + jwtClaims);
        }
    }


    @Test
    public void authenticationTestTest()
            throws JoseException, InvalidJwtException
    {
        TestClass testClass = new TestClass();

        TestClass.initKeys();

        testClass.pseudoTest();

        System.out.println("Public key" + TestClass.getPublicKey());
        System.out.println("Private key" + TestClass.getPrivateKey());
        System.out.println("key id" + TestClass.getKeyId());

        assertNotNull(TestClass.getPublicKey());
        assertNotNull(TestClass.getPrivateKey());
        assertNotNull(TestClass.getKeyId());
    }


}