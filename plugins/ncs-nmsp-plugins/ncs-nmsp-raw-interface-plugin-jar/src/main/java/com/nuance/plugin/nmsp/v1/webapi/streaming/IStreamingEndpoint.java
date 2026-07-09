package com.nuance.plugin.nmsp.v1.webapi.streaming;

public interface IStreamingEndpoint {

    void sendBinary(byte[] buffer);

    boolean isOpen();

    void onStreamingDone();
}
