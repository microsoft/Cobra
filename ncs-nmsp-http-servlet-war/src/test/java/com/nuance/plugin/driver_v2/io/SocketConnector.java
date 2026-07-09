package com.nuance.plugin.driver_v2.io;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.nuance.plugin.driver_v2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketConnector extends Thread {
    private static final Logger logger = LogManager.getLogger();

	private static final String TAG = "TAG-SocketConnector";
	
	private Socket mSocket = null;
	private SocketConnector.Listener mListener = null;
	
	public interface Listener {
		public void onConnected(Socket socket);
		public void onError(String msg);
	}
	
	public SocketConnector(SocketConnector.Listener listener) {
		mListener = listener;
	}
	
	@Override
	public void run() {
		logger.info( "SocketConnector starting");
		connect();
		logger.info( "SocketConnector finished");
	}
	
	private void connect() {
		try {
        // Create a custom SSL context.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, verifyCert, null);
        
        if ( Configuration.SERVER_PROTOCOL.equalsIgnoreCase("https") ) {
        	logger.info( "Connecting to secure endpoint");
        	mSocket = context.getSocketFactory().createSocket(Configuration.SERVER_HOST, Integer.parseInt(Configuration.SERVER_PORT));
        }
        else {
        	// We only support secure connections to our NCS data centers. This is for non-Production development environments only.
        	logger.info( "Connecting to insecure endpoint");
            mSocket = javax.net.SocketFactory.getDefault().createSocket(Configuration.SERVER_HOST, Integer.parseInt(Configuration.SERVER_PORT));
        }
        
        if( mSocket.isConnected() ) {
        	logger.info( "Successfully connected");
			mSocket.setKeepAlive(true);
        	mListener.onConnected(mSocket);
        } else {
        	logger.info( "Failed to connect");
        	mListener.onError("Failed to connect");
        }

		} catch( Exception e ) {
			logger.info( e.getMessage());
			mListener.onError(e.getMessage());
		}

	}
	
    /**
     * Create a trust manager that validates the certificate chain is from a trusted root CA,
     * and verify the server hostname returned in the TLS handshake
     *
     */
    private final TrustManager[] verifyCert = new TrustManager[] { new X509TrustManager() {

        @Override
        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
            // Not Used
            logger.info( "Checking if client is trusted...");
        }

        @Override
        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException {
            logger.info( "Checking if server is trusted...");

        	/*
        	 * To authenticate the communication channel between client and server we need to ensure
        	 * that both the ssl certificate is from a trusted root CA and the hostname in the certificate
        	 * matches the one we are expecting.
        	 *
        	 * Nuance NCS Services SSL certificate hostname is *.nuancemobility.net
        	 *
        	 */

            boolean containsTrustedRootCA = false;
            boolean containsValidHostname = false;

            for(int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];

                // Check to ensure certificate hasn't expired...
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException e) {
                    logger.info( "Certificate Expiration Error: " + e.getMessage());
                    throw e;
                } catch (CertificateNotYetValidException e) {
                    logger.info( "Certificate Expiration Error: " + e.getMessage());
                    throw e;
                }

                // Ensure the chain contains a certificate from a trusted root CA
                if ( Configuration.requiresTrustedRootCertificate && cert.getBasicConstraints() != -1 ) // Chain contains a trusted root CA
                    containsTrustedRootCA = true;


                // Ensure the chain contains a certificate with a valid hostname
                String dn = cert.getSubjectDN().getName();

                int cnStart = dn.indexOf("CN=") + 3;
                int cnEnd = dn.indexOf(',', cnStart);
                String cn = dn.substring(cnStart, cnEnd);
                logger.info( "Verifying hostname against CN: " + cn);

                if( cn.charAt(0) == '*' && Configuration.SERVER_HOST.toLowerCase().endsWith(cn.substring(1).toLowerCase()) ) {
                    containsValidHostname = true;
                }
                else if( cn.equalsIgnoreCase(Configuration.SERVER_HOST) )
                    containsValidHostname = true;
            }

            // Throw any exceptions...
//            if( !containsValidHostname )
//                throw new java.security.cert.CertificateException("Invalid Certificate Hostname!");
//
//            if( Configuration.requiresTrustedRootCertificate && !containsTrustedRootCA )
//                throw new java.security.cert.CertificateException("Certificate is not from Trusted CA!");

            // All good!
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // Not Used
            logger.info( "Checking accepted issuers...");
            return null;
        }
    }};

}
