package com.nuance.plugin.driver_v2;

import java.net.URI;

/**
 * Created by peter_freshman on 9/19/16.
 */
public class Configuration {

    public static String APP_KEY = "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b";  //gitleaks:allow
    public static String APP_ID = "nina4";
    public static String ALGORITHM = "key";
    public static String SERVER_HOST = "test-ncs-http-v2.nuance.mobi";
    public static String PROXY_VERSION = "NmspServlet";
    public static String SERVER_PORT = "443";
    public static String SERVER_PROTOCOL = "https";

    public static URI SERVER_URI = buildURI();

    //Only needed if using NLU
    public static String CONTEXT_TAG = "!!PROVIDE YOUR MIX NLU MODEL CONTEXT_TAG!!";

    public static boolean requiresTrustedRootCertificate = false; // Change this to true if using one of our test environments
    
    public static URI buildURI() {
//    	return URI.create("https://127.0.0.1:8443/webapi-platform/http/xyz_a_grpc_mix/ncs7-http");
    	return URI.create( SERVER_PROTOCOL + "://" + SERVER_HOST + ':' + SERVER_PORT +
					                "/" + PROXY_VERSION + "/?" +
					                "app_id=" + APP_ID + '&' +
					                "algorithm=" + ALGORITHM + '&' +
					                "app_key=" + APP_KEY);
    }
    
    public static String toString2() {
    	String s = "";
    	s += "APP_ID: " + APP_ID + "\r\n";
    	s += "APP_KEY: " + APP_KEY + "\r\n";
    	s += "SERVER_PROTOCOL: " + SERVER_PROTOCOL + "\r\n";
    	s += "SERVER_HOST: " + SERVER_HOST + "\r\n";
    	s += "SERVER_PORT: " + SERVER_PORT+ "\r\n";
    	s += "PROXY_VERSION: " + PROXY_VERSION + "\r\n";
    	s += "REQUIRES_TRUSTED_ROOT_CERT: " + requiresTrustedRootCertificate + "\r\n";
    	s += "SERVER_URI: " + SERVER_URI+ "\r\n";
    	
    	return s;
    }
}