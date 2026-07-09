package com.nuance.cobra.utils.config;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfigHelpersTest {

    @Test
    public void extractRootKeys() {
        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        // tenant config
        config.addProperty("Evan.language", "en-us");
        config.addProperty("Evan.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("Evan.name", "Evan");

        config.addProperty("Ava.language", "no-match");
        config.addProperty("Ava.pcm_16_16k.voice_model", "enhanced");
        config.addProperty("Ava.name", "Evan");

        Set<String> expected = new HashSet<>();
        expected.add("Ava");
        expected.add("Evan");
        assertArrayEquals(new Set[]{expected}, new Set[]{ConfigHelpers.extractRootKeys(config)});
    }

    @Test
    public void clearCombinedConfiguration()
            throws InterruptedException
    {
        ConfigHelpers.clearCombinedConfiguration(null, null);
        CombinedConfiguration cc = new CombinedConfiguration();
        cc.setProperty("combinedConfigLingerTimeMilliseconds", 1);
        ConfigHelpers.clearCombinedConfiguration(cc, null);
        Thread.sleep(5);
        assertEquals(0, cc.getConfigurations().size());
    }

    @Test
    public void clearCombinedConfigurationSync()
    {
        CombinedConfiguration cc = new CombinedConfiguration();
        cc.setProperty("combinedConfigLingerTimeMilliseconds", 0);
        ConfigHelpers.clearCombinedConfiguration(cc, null);
        assertEquals(0, cc.getConfigurations().size());
    }
}