package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxList;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonUnpackerTest {

    @Test
    public void unpack() throws Exception {
        PdxDictionary testDictionary = new PdxDictionary();
        testDictionary.put("testString", "test string in pdx \\t dictionary.\\n and then some new lines.");
        testDictionary.put("testInt", 42);
        testDictionary.put("testDouble", 42.42);
        testDictionary.put("testBool", true);
        PdxList testList = new PdxList();
        testList.add("Hello list1");
        testList.add("Hello list2");
        testDictionary.put("testList", testList);
        Object result = JsonUnpacker.unpack(testDictionary.toJsonString());

        assertTrue(result instanceof PdxDictionary);
    }
}