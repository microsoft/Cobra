package com.nuance.cobra.api.globalregistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GrammarTest {

    @Test
    public void versionTest(){
        Grammar grammar = new Grammar();
        Map<String, String> versionMap = new HashMap<String, String>();

        versionMap.put("Test", "testVersion");
        grammar.setVersionURLMap(versionMap);

        grammar.setCurrentVersion("Test");

        Assert.assertEquals("Test", grammar.getCurrentVersion());

        grammar.addVersionURL("Test 1", "testVersion1");

        Assert.assertEquals(versionMap.get("Test"), grammar.getVersionURL("Test"));

        grammar.setVersionURLMap(null);

        Assert.assertNull(grammar.getVersionURL("test"));
        Assert.assertNull(grammar.getVersionURLMap().get("Test"));

        grammar.addVersionURL("Test 1", "testVersion1");
        Assert.assertEquals(versionMap.get("Test 1"),grammar.getVersionURLMap().get("Test 1"));



    }
}
