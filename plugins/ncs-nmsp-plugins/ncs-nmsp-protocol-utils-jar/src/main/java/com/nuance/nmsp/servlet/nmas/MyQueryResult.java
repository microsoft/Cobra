package com.nuance.nmsp.servlet.nmas;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.ncs.nmsp.pdx.NmspValueType;

import java.util.Set;

public class MyQueryResult
{
    private final String resultType;
    private boolean finalResponse = false;
    private byte[] rawInput = new byte[0];
    private final NmspDictionaryImpl dictionary = new NmspDictionaryImpl();

    public MyQueryResult(String resultType)
    {
        this.resultType = resultType;
    }

    public String getResultType()
    {
        return resultType;
    }

    public byte[] getRawInput()
    {
        return rawInput;
    }

    public void setRawInput(byte[] rawInput)
    {
        this.rawInput = rawInput;
    }

    public boolean isFinalResponse()
    {
        return finalResponse;
    }

    public void setFinalResponse(boolean finalResponse)
    {
        this.finalResponse = finalResponse;
    }

    public NmspDictionary getDictionary()
    {
        return dictionary;
    }

    public Set<String> keySet() { return dictionary.keySet(); }
    public boolean containsKey(String s) { return dictionary.containsKey(s); }
    public NmspValueType getType(String s) { return dictionary.getType(s); }
    public boolean getBoolean(String s) { return dictionary.getBoolean(s); }
    public void addBoolean(String s, boolean b) { dictionary.addBoolean(s, b); }
    public double getDouble(String s) { return dictionary.getDouble(s); }
    public void addDouble(String s, double v) { dictionary.addDouble(s, v); }
    public int getInteger(String s) { return dictionary.getInteger(s); }
    public void addInteger(String s, int i) { dictionary.addInteger(s, i); }
    public byte[] getByteString(String s) { return dictionary.getByteString(s); }
    public void addByteString(String s, byte[] bytes) { dictionary.addByteString(s, bytes); }
    public String getUTF8String(String s) { return dictionary.getUTF8String(s); }
    public void addUTF8String(String s, String s1) { dictionary.addUTF8String(s, s1); }
    public String getAsciiString(String s) { return dictionary.getAsciiString(s); }
    public void addAsciiString(String s, String s1) { dictionary.addAsciiString(s, s1); }
    public NmspDictionary getDictionary(String s) { return dictionary.getDictionary(s); }
    public void addDictionary(String s, NmspDictionary d) { dictionary.addDictionary(s, d); }
    public NmspSequence getSequence(String s) { return dictionary.getSequence(s); }
    public void addSequence(String s, NmspSequence sequence) { dictionary.addSequence(s, sequence); }
}
