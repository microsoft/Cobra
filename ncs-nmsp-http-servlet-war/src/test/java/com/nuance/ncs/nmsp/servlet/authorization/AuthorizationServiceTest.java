package com.nuance.ncs.nmsp.servlet.authorization;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class AuthorizationServiceTest
{

    @Test(expected = IllegalStateException.class)
    public void construct()
    {
        new AuthorizationService();
    }

    @Test
    public void authorizeAppIdAndKey()
    {
        assertFalse(AuthorizationService.authorizeAppIdAndKey(null, "nina4", "abcd".getBytes(StandardCharsets.UTF_8)));
        assertFalse(AuthorizationService.authorizeAppIdAndKey("", "nina4", "abcd".getBytes(StandardCharsets.UTF_8)));
        assertFalse(AuthorizationService.authorizeAppIdAndKey("[xyz]", "nina4", "abcd".getBytes(StandardCharsets.UTF_8)));
        assertTrue(AuthorizationService.authorizeAppIdAndKey("[xyz],.*", "nina4", "abcd".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void authorizeGwIp()
    {
        assertFalse(AuthorizationService.authorizeGwIp(null, null));
        assertFalse(AuthorizationService.authorizeGwIp(null, "127.0.0.1"));
        assertFalse(AuthorizationService.authorizeGwIp("[xyz]", "127.0.0.1"));
        assertTrue(AuthorizationService.authorizeGwIp("[xyz],.*", "127.0.0.1"));
    }

    @Test
    public void authorizeCrossOrigin()
    {
        assertFalse(AuthorizationService.authorizeCrossOrigin(null, null));
        assertFalse(AuthorizationService.authorizeCrossOrigin(null, "127.0.0.1"));
        assertFalse(AuthorizationService.authorizeCrossOrigin("[xyz]", "127.0.0.1"));
        assertTrue(AuthorizationService.authorizeCrossOrigin("[xyz],.*", "127.0.0.1"));
    }

    @Test
    public void validateAppIdAppKey()
    {
        assertFalse(AuthorizationService.validateAppIdAppKey(null, null));
        assertFalse(AuthorizationService.validateAppIdAppKey("nina4", null));
        assertFalse(AuthorizationService.validateAppIdAppKey("", "abcd".getBytes(StandardCharsets.UTF_8)));
        assertFalse(AuthorizationService.validateAppIdAppKey("nina4", "abc".getBytes(StandardCharsets.UTF_8)));
        assertTrue(AuthorizationService.validateAppIdAppKey("nina4", "abcd".getBytes(StandardCharsets.UTF_8)));
    }

}