package com.nuance.ncs.nmsp.pdx;

import java.util.Set;

/** Replaces com.nuance.nmsp.client.sdk.components.core.pdx.Dictionary */
public interface NmspDictionary {
    Set<String> keySet();
    boolean containsKey(String key);
    NmspValueType getType(String key);

    int getInteger(String key);
    void addInteger(String key, int value);

    byte[] getByteString(String key);
    void addByteString(String key, byte[] value);

    String getUTF8String(String key);
    void addUTF8String(String key, String value);

    String getAsciiString(String key);
    void addAsciiString(String key, String value);

    NmspDictionary getDictionary(String key);
    void addDictionary(String key, NmspDictionary value);

    NmspSequence getSequence(String key);
    void addSequence(String key, NmspSequence value);

    double getDouble(String key);
    void addDouble(String key, double value);

    boolean getBoolean(String key);
    void addBoolean(String key, boolean value);
}
