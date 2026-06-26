package com.nuance.ncs.nmsp.pdx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** LinkedHashMap-backed NmspDictionary. Replaces PDXDictionary. */
public class NmspDictionaryImpl implements NmspDictionary {

    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

    private static class Entry {
        final NmspValueType type;
        final Object value;
        Entry(NmspValueType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    @Override public Set<String> keySet() { return entries.keySet(); }
    @Override public boolean containsKey(String key) { return entries.containsKey(key); }
    @Override public NmspValueType getType(String key) {
        Entry e = entries.get(key);
        return e != null ? e.type : null;
    }

    @Override public int getInteger(String key) { return (Integer) entries.get(key).value; }
    @Override public void addInteger(String key, int value) { entries.put(key, new Entry(NmspValueType.INTEGER, value)); }

    @Override public byte[] getByteString(String key) { return (byte[]) entries.get(key).value; }
    @Override public void addByteString(String key, byte[] value) { entries.put(key, new Entry(NmspValueType.BYTESTRING, value)); }

    @Override public String getUTF8String(String key) { return (String) entries.get(key).value; }
    @Override public void addUTF8String(String key, String value) { entries.put(key, new Entry(NmspValueType.UTF8STRING, value)); }

    @Override public String getAsciiString(String key) { return (String) entries.get(key).value; }
    @Override public void addAsciiString(String key, String value) { entries.put(key, new Entry(NmspValueType.ASCIISTRING, value)); }

    @Override public NmspDictionary getDictionary(String key) { return (NmspDictionary) entries.get(key).value; }
    @Override public void addDictionary(String key, NmspDictionary value) { entries.put(key, new Entry(NmspValueType.DICTIONARY, value)); }

    @Override public NmspSequence getSequence(String key) { return (NmspSequence) entries.get(key).value; }
    @Override public void addSequence(String key, NmspSequence value) { entries.put(key, new Entry(NmspValueType.SEQUENCE, value)); }

    @Override public double getDouble(String key) { return (Double) entries.get(key).value; }
    @Override public void addDouble(String key, double value) { entries.put(key, new Entry(NmspValueType.DOUBLE, value)); }

    @Override public boolean getBoolean(String key) { return (Boolean) entries.get(key).value; }
    @Override public void addBoolean(String key, boolean value) { entries.put(key, new Entry(NmspValueType.BOOLEAN, value)); }
}
