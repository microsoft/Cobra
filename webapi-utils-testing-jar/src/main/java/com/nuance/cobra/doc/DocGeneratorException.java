package com.nuance.cobra.doc;

public class DocGeneratorException extends Exception {

    public DocGeneratorException(String message) {
        super(message);
    }

    public DocGeneratorException(String message, Exception reason) {
        super(message, reason);
    }

}
