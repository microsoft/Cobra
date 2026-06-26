package com.nuance.plugin.ncs7.streaming;

public interface IStreamingEndpoint {

    void sendBinary(byte[] buffer);

    boolean isOpen();

    void onStreamingDone();
}
