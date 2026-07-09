package com.nuance.cobra.api.globalregistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ApplicationVersionTest {
    private Map<String, Grammar> data;
    private ApplicationVersion applicationVersion;

    @Test
    public void setApplicationVersion(){
        applicationVersion = new ApplicationVersion();
        data = new HashMap<String, Grammar>();
        data.put("Test", new Grammar());
        applicationVersion.setGrammars(data);

        applicationVersion.addGrammars("Test 1", new Grammar());

        assert(applicationVersion.getGrammar("Test") instanceof Grammar);

        applicationVersion.setGrammars(null);

        Assert.assertNull(applicationVersion.getGrammar("test"));
        Assert.assertNull(applicationVersion.getGrammars().get("test"));

        applicationVersion.addGrammars("Test 1", new Grammar());

        assert(applicationVersion.getGrammar("Test 1") instanceof Grammar);

        assert(applicationVersion.getGrammars().get("Test 1") instanceof Grammar);
    }
}
