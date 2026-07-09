package com.nuance.plugin.nmsp.v1.nmspgw.server;

import com.nuance.cobra.utils.Tools;
import com.nuance.plugin.nmsp.v1.PdxInterfaceConfig;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

public class SSLHandlerProvider {
    private static final Logger logger = LogManager.getLogger();

    private static final String PROTOCOL = "TLSv1.2";
    private static final String ALGORITHM_SUN_X509 = "SunX509";
    private static final String ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    private static final String KEYSTORE = "/etc/keystore";
    private static final String KEYSTORE_TYPE = "JKS";
    private static SSLContext serverSSLContext = null;
    private static boolean useSSL = false;

    public static boolean isSSLEnabled() {
        return useSSL;
    }

    public static SslHandler getSSLHandler() {
        SSLEngine sslEngine = null;
        if (serverSSLContext == null) {
            logger.error("Server SSL context is null");
            return null;
        } else {
            sslEngine = serverSSLContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);

        }
        return new SslHandler(sslEngine);
    }

    public static void initSSLContext(BaseHierarchicalConfiguration configs) {
        logger.info("Initiating SSL context");
        String algorithm = Security.getProperty(ALGORITHM);
        if (algorithm == null) {
            algorithm = ALGORITHM_SUN_X509;
        }
        KeyStore ks = null;
        InputStream inputStream = null;

        String keyStorePath = Tools.getJettyPath() + KEYSTORE;

        logger.info("Loading keystore: {}", keyStorePath);

        try {
            inputStream = new FileInputStream(keyStorePath);
            ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(inputStream, configs.getString(PdxInterfaceConfig.Property.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray());
        } catch (IOException e) {
            logger.error("Cannot load the keystore file", e);
        } catch (CertificateException e) {
            logger.error("Cannot get the certificate", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Somthing wrong with the SSL algorithm", e);
        } catch (KeyStoreException e) {
            logger.error("Cannot initialize keystore", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("Cannot close keystore file stream ", e);
            }
        }
        try {

            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, configs.getString(PdxInterfaceConfig.Property.SSL_CERTIFICATE_PASSWORD.getKey()).toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();
            // Setting trust store null since we don't need a CA certificate or Mutual Authentication
            TrustManager[] trustManagers = null;

            serverSSLContext = SSLContext.getInstance(PROTOCOL);
            serverSSLContext.init(keyManagers, trustManagers, null);
        } catch (Exception e) {
            logger.error("Failed to initialize the server-side SSLContext", e);
        }

        useSSL = true;
    }
}
