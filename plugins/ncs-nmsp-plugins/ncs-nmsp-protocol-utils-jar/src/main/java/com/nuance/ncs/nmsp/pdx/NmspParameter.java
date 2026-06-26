package com.nuance.ncs.nmsp.pdx;

/** Replaces com.nuance.nmsp.client.sdk.components.general.Parameter */
public class NmspParameter {
    private final String name;
    private final byte[] value;
    private final NmspParamType type;

    public NmspParameter(String name, byte[] value, NmspParamType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() { return name; }
    public byte[] getValue() { return value; }
    public NmspParamType getType() { return type; }
}
