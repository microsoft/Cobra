package com.nuance.cobra.session;

import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.Test;

import java.security.Key;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtKeyResolverTest {
    private CombinedConfiguration cfgs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration cfg = cfgs.getConfiguration(0);
    private JsonWebSignature jws = mock(JsonWebSignature.class);

    @Test
    public void getResolver() {
        cfg.setProperty(WebApiConfig.Property.APPLICATION_KEY.key,"test");
        Map<String,String> publicKeys = new HashedMap();
        publicKeys.put("test", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhL469A+uVmMBfVBPovEvBPhXkuGct5CN5YMpfFoTjPbbDsX9P8RKvRVpDNlnvQUY7BojKQfsd/LHJi+F92OGgC9y4qVvaI1JJKbl502cLc5SRnSvj/VV6/tN3x7kR9Yu1jOsEZ/czpVNwnogQH1Vn6+5gcZuA4rxxbAkh2NaGkBM2htUaMfggsa/x1eCvkUs1YdkiEJji1BdbJR4MFAc0tw2sWjd05P0GjccVC0e4VuhUOyx8a9VEKNcS134mknt1KX9fH2fhOWOepLcFXpg/moHmt7an4nTDJhpjpJQwG2hTNmJGNE0ZSL0Yth59nIIBPNmz1Rxw+R+T2/WU24LCQIDAQAB-----END PUBLIC KEY-----");
        cfg.setProperty(WebApiConfig.Property.JWT_PUBLIC_KEYS.key, publicKeys);

        JwtKeyResolver keyVerifier = null;
        try {
            keyVerifier = JwtKeyResolver.getResolver(cfgs);
        } catch (JoseException | InvalidPublicKeyInConfigException | InvalidKeySpecException e) {
            fail(e.getMessage());
        }
        assertNotNull(keyVerifier);
    }

    @Test
    public void resolveKey() throws UnresolvableKeyException {
        cfg.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test");
        Map<String,String> publicKeys = new HashedMap();
        publicKeys.put("test", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhL469A+uVmMBfVBPovEvBPhXkuGct5CN5YMpfFoTjPbbDsX9P8RKvRVpDNlnvQUY7BojKQfsd/LHJi+F92OGgC9y4qVvaI1JJKbl502cLc5SRnSvj/VV6/tN3x7kR9Yu1jOsEZ/czpVNwnogQH1Vn6+5gcZuA4rxxbAkh2NaGkBM2htUaMfggsa/x1eCvkUs1YdkiEJji1BdbJR4MFAc0tw2sWjd05P0GjccVC0e4VuhUOyx8a9VEKNcS134mknt1KX9fH2fhOWOepLcFXpg/moHmt7an4nTDJhpjpJQwG2hTNmJGNE0ZSL0Yth59nIIBPNmz1Rxw+R+T2/WU24LCQIDAQAB-----END PUBLIC KEY-----");
        cfg.setProperty(WebApiConfig.Property.JWT_PUBLIC_KEYS.key, publicKeys);

        JwtKeyResolver keyVerifier = null;
        try {
            keyVerifier = JwtKeyResolver.getResolver(cfgs);
        } catch (JoseException | InvalidKeySpecException | InvalidPublicKeyInConfigException e) {
            fail(e.getMessage());
        }
        Key testKey = mock(Key.class);
        List<JsonWebStructure> testList = new ArrayList<>();
        when(jws.getKeyIdHeaderValue()).thenReturn("test");
        keyVerifier.resolveKey(jws, testList);
    }

    @Test(expected = InvalidKeySpecException.class)
    public void resolveKeyInvalidKeySpec() throws Exception {
        cfg.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test");
        Map<String,String> publicKeys = new HashedMap();
        publicKeys.put("test", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFCAQ8AMIIBCgKCAQEAhL469A+uVmMBfVBPovEvBPhXkuGct5CN5YMpfFoTjPbbDsX9P8RKvRVpDNlnvQUY7BojKQfsd/LHJi+F92OGgC9y4qVvaI1JJKbl502cLc5SRnSvj/VV6/tN3x7kR9Yu1jOsEZ/czpVNwnogQH1Vn6+5gcZuA4rxxbAkh2NaGkBM2htUaMfggsa/x1eCvkUs1YdkiEJji1BdbJR4MFAc0tw2sWjd05P0GjccVC0e4VuhUOyx8a9VEKNcS134mknt1KX9fH2fhOWOepLcFXpg/moHmt7an4nTDJhpjpJQwG2hTNmJGNE0ZSL0Yth59nIIBPNmz1Rxw+R+T2/WU24LCQIDAQAB-----END PUBLIC KEY-----");
        cfg.setProperty(WebApiConfig.Property.JWT_PUBLIC_KEYS.key, publicKeys);

        JwtKeyResolver keyVerifier = JwtKeyResolver.getResolver(cfgs);
    }

    @Test(expected = InvalidPublicKeyInConfigException.class)
    public void resolveKeyInvalidKeyInConfig() throws Exception {
        cfg.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "test");
        Map<String,String> publicKeys = new HashedMap();
        publicKeys.put("test", "test-key");
        cfg.setProperty(WebApiConfig.Property.JWT_PUBLIC_KEYS.key, publicKeys);

        JwtKeyResolver keyVerifier = JwtKeyResolver.getResolver(cfgs);
    }

}