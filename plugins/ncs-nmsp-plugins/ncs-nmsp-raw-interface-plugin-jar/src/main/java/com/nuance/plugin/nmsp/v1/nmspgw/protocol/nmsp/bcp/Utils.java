package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp;

public class Utils {
    public enum DeferNSSLoading {
        OFF, WAIT_AUDIO, WAIT_SPEECH
    }

    public enum Status {
        PASS, FAIL, ABORT, DONT_REPORT;
    }
}
