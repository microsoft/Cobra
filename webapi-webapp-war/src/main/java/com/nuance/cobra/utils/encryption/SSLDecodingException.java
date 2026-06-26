package com.nuance.cobra.utils.encryption;

/**
 * SSL decoding exception.
 */
public class SSLDecodingException extends Exception {

    SSLDecodingException(String msg) {
        super(msg);
    }

    SSLDecodingException(String msg, Exception e) {
        super(msg, e);
    }

}
