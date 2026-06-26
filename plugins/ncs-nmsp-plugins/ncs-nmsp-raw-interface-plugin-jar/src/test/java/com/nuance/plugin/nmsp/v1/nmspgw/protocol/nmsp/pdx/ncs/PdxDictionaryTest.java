package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json.JsonException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json.JsonUnsupportedDataTypeException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class PdxDictionaryTest {

    @Test
    public void fromJson() throws JsonUnsupportedDataTypeException, JsonException {
        PdxDictionary testDictionary1 = PdxDictionary.fromJson("{}");
        assertTrue(testDictionary1.equals(new PdxDictionary()));
    }

    @Test
    public void toJsonString() throws Exception {
        PdxDictionary testDictionary1 = new PdxDictionary();
        testDictionary1.put("testString", "test string in pdx \\t dictionary.\\n and then some new lines.");
        testDictionary1.put("testInt", 42);
        testDictionary1.put("testDouble", 42.42);
        testDictionary1.put("testBool", true);
        testDictionary1.put("testByteArray", "test me here".getBytes());
        PdxList testList = new PdxList();
        testList.add("Hello list1");
        testList.add("Hello list2");
        testDictionary1.put("testList", testList);

        assertTrue(testDictionary1.toJsonString().contains("{"));
    }

    @Test
    public void testEquals() {
        PdxDictionary testDictionary1 = new PdxDictionary();
        testDictionary1.put("testString", "test string in pdx \\t dictionary.\\n and then some new lines.");
        testDictionary1.put("testInt", 42);
        testDictionary1.put("testDouble", 42.42);
        testDictionary1.put("testBool", true);
        testDictionary1.put("testByteArray", "test me here".getBytes());
        PdxList testList = new PdxList();
        testList.add("Hello list1");
        testList.add("Hello list2");
        testDictionary1.put("testList", testList);

        PdxDictionary testDictionary2 = new PdxDictionary(testDictionary1);

        assertTrue(testDictionary1.equals(testDictionary2));
        assertEquals(1702058200, testDictionary1.hashCode());
    }

    @Test
    public void isKeysEncodedUsingAscii() {
    }
}