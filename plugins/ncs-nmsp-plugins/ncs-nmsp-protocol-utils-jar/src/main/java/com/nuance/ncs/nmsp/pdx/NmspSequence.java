package com.nuance.ncs.nmsp.pdx;

/** Replaces com.nuance.nmsp.client.sdk.components.core.pdx.Sequence */
public interface NmspSequence {
    int size();
    NmspValueType getType(int index);

    int getInteger(int index);
    void addInteger(int value);

    byte[] getByteString(int index);
    void addByteString(byte[] value);

    String getUTF8String(int index);
    void addUTF8String(String value);

    String getAsciiString(int index);
    void addAsciiString(String value);

    NmspDictionary getDictionary(int index);
    void addDictionary(NmspDictionary value);

    NmspSequence getSequence(int index);
    void addSequence(NmspSequence value);

    double getDouble(int index);
    void addDouble(double value);

    boolean getBoolean(int index);
    void addBoolean(boolean value);
}
