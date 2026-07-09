package com.nuance.plugin.v1;

import org.jose4j.jwt.consumer.JwtConsumerBuilder;


/**
 * Configures the validations before the claims are processed.
 *
 * Plugins can provide their own configurator to customize the validations on the JWT token.
 */
public interface IJwtValidationsConfigurator
{
    /**
     * Configures the consumer builder used to vailodate the JWT token
     * @param claimsConsumerBuilder
     */
    void configureBuilder(JwtConsumerBuilder claimsConsumerBuilder);
}
