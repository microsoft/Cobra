package com.nuance.nmsp.servlet.nmas;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.ncs.nmsp.pdx.NmspSequenceImpl;
import com.nuance.ncs.nmsp.pdx.NmspValueType;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class MyQueryResultTest
{
    @Test
    public void myQueryResultTest()
    {
        String resultType = "NVC_ASR_CMD";
        MyQueryResult myQueryResult = new MyQueryResult(resultType);
        assertEquals(resultType, myQueryResult.getResultType());

        String rawInput = "abc123";
        myQueryResult.setRawInput(rawInput.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(rawInput.getBytes(StandardCharsets.UTF_8), myQueryResult.getRawInput());

        myQueryResult.setFinalResponse(false);
        assertFalse(myQueryResult.isFinalResponse());

        myQueryResult.addBoolean("testBoolean", true);
        assertTrue(myQueryResult.getBoolean("testBoolean"));

        assertEquals(NmspValueType.BOOLEAN, myQueryResult.getType("testBoolean"));
        assertFalse(myQueryResult.keySet().isEmpty());
        assertTrue(myQueryResult.containsKey("testBoolean"));

        myQueryResult.addDouble("testDouble", 1.0d);
        assertEquals(1.0d, myQueryResult.getDouble("testDouble"), 0.0d);

        myQueryResult.addInteger("testInt", 1);
        assertEquals(1, myQueryResult.getInteger("testInt"));

        myQueryResult.addByteString("testByteString", rawInput.getBytes());
        assertArrayEquals(rawInput.getBytes(), myQueryResult.getByteString("testByteString"));

        myQueryResult.addAsciiString("testAsciiString", rawInput);
        assertSame(rawInput, myQueryResult.getAsciiString("testAsciiString"));

        String ole = "olé";
        myQueryResult.addUTF8String("testUtf8", ole);
        assertSame(ole, myQueryResult.getUTF8String("testUtf8"));

        NmspDictionary nmspDictionary = new NmspDictionaryImpl();
        nmspDictionary.addBoolean("testBoolean", true);
        myQueryResult.addDictionary("testDict", nmspDictionary);
        nmspDictionary = myQueryResult.getDictionary("testDict");
        assertTrue(nmspDictionary.getBoolean("testBoolean"));

        NmspSequence sequence = new NmspSequenceImpl();
        sequence.addBoolean(true);
        myQueryResult.addSequence("testSeq", sequence);
        sequence = myQueryResult.getSequence("testSeq");
        assertTrue(sequence.getBoolean(0));
    }
}
