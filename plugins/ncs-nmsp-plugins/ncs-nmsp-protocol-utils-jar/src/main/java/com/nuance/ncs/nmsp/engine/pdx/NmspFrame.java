package com.nuance.ncs.nmsp.engine.pdx;

/** Decoded inbound NMSP frame */
public class NmspFrame {
    public final byte protocol;
    public final byte version;
    public final int command;
    public final byte[] payload;

    public NmspFrame(byte protocol, byte version, int command, byte[] payload) {
        this.protocol = protocol;
        this.version = version;
        this.command = command;
        this.payload = payload;
    }
}
