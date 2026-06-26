package com.nuance.ncs.nmsp.pdx;

import java.util.ArrayList;
import java.util.List;

/** ArrayList-backed NmspSequence. Replaces PDXSequence. */
public class NmspSequenceImpl implements NmspSequence {

    private final List<Entry> entries = new ArrayList<>();

    private static class Entry {
        final NmspValueType type;
        final Object value;
        Entry(NmspValueType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    @Override public int size() { return entries.size(); }
    @Override public NmspValueType getType(int index) { return entries.get(index).type; }

    @Override public int getInteger(int index) { return (Integer) entries.get(index).value; }
    @Override public void addInteger(int value) { entries.add(new Entry(NmspValueType.INTEGER, value)); }

    @Override public byte[] getByteString(int index) { return (byte[]) entries.get(index).value; }
    @Override public void addByteString(byte[] value) { entries.add(new Entry(NmspValueType.BYTESTRING, value)); }

    @Override public String getUTF8String(int index) { return (String) entries.get(index).value; }
    @Override public void addUTF8String(String value) { entries.add(new Entry(NmspValueType.UTF8STRING, value)); }

    @Override public String getAsciiString(int index) { return (String) entries.get(index).value; }
    @Override public void addAsciiString(String value) { entries.add(new Entry(NmspValueType.ASCIISTRING, value)); }

    @Override public NmspDictionary getDictionary(int index) { return (NmspDictionary) entries.get(index).value; }
    @Override public void addDictionary(NmspDictionary value) { entries.add(new Entry(NmspValueType.DICTIONARY, value)); }

    @Override public NmspSequence getSequence(int index) { return (NmspSequence) entries.get(index).value; }
    @Override public void addSequence(NmspSequence value) { entries.add(new Entry(NmspValueType.SEQUENCE, value)); }

    @Override public double getDouble(int index) { return (Double) entries.get(index).value; }
    @Override public void addDouble(double value) { entries.add(new Entry(NmspValueType.DOUBLE, value)); }

    @Override public boolean getBoolean(int index) { return (Boolean) entries.get(index).value; }
    @Override public void addBoolean(boolean value) { entries.add(new Entry(NmspValueType.BOOLEAN, value)); }
}
