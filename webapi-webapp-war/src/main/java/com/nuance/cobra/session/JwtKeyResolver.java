package com.nuance.cobra.session;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;

import java.security.Key;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JwtKeyResolver implements VerificationKeyResolver {

    /** special key id in tenant config publicKeysMap to identify the key to use to validate JWT tokens without the "kid" header. */
    public static final String NO_KID_KEY = "__no_kid__";

    private Map<String, Key> keys = new HashMap<>();
    private JwtKeyResolver() {
        //hide constructor
    }

    public static JwtKeyResolver getResolver(CombinedConfiguration configs) throws JoseException, InvalidKeySpecException, InvalidPublicKeyInConfigException {
        JwtKeyResolver tmpRes = new JwtKeyResolver();
        RsaKeyUtil rsaKeyUtil = new RsaKeyUtil();
        HierarchicalConfiguration<ImmutableNode> keyIdPair = configs.configurationAt(WebApiConfig.Property.JWT_PUBLIC_KEYS.key);
        Iterator<String> keysIt = keyIdPair.getKeys();
        while (keysIt.hasNext() ) {
            String key = keysIt.next();
            try {
                PublicKey publicKey = rsaKeyUtil.fromPemEncoded(keyIdPair.getString(key));
                tmpRes.addKey(key, publicKey);
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidPublicKeyInConfigException(e.getMessage());
            }
        }

        return tmpRes;
    }

    private void addKey(String keyId, Key key) {
        keys.put(keyId, key);
    }

    @Override
    public Key resolveKey(JsonWebSignature jws,
                          List<JsonWebStructure> nestingContext)
    {
        String keyId = jws.getKeyIdHeaderValue();
        return keys.get(keyId != null ? keyId : NO_KID_KEY);
    }
}
