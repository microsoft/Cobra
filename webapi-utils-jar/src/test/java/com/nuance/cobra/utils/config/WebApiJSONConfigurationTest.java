package com.nuance.cobra.utils.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WebApiJSONConfigurationTest
{

    @Test
    public void read()
            throws IOException, ConfigurationException
    {
        WebApiJSONConfiguration theConfig = new WebApiJSONConfiguration(new WebApiJSONConfiguration());
        FileReader fr = new FileReader("src/test/resources/tenant_config_complex_objects.json");
        theConfig.read(fr);
        assertEquals("string", theConfig.getString("simple_string"));

        StringWriter sw = new StringWriter();
        theConfig.write(sw);

        assertFalse(sw.toString()
                      .isBlank());
    }

    @Test
    public void setMapProperty()
    {
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        Map<String, Object> configMap = Map.of("key1", "value1");
        webApiJSONConfiguration.setMapProperty("test", configMap);
        HierarchicalConfiguration<ImmutableNode> hierarchicalConfiguration =
                webApiJSONConfiguration.configurationAt("test");
        String value1 = hierarchicalConfiguration.getString("key1");
        assertEquals("value1", value1);
    }

    @Test
    public void mergeMap()
    {
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        Map<String, Object> configMap = Map.of("key1", "value1");
        webApiJSONConfiguration.mergeMap(null);
        webApiJSONConfiguration.mergeMap(configMap);
        String value1 = webApiJSONConfiguration.getString("key1");
        assertEquals("value1", value1);
    }

    @Test
    public void merge()
    {
        WebApiJSONConfiguration webApiJSONConfiguration1 = new WebApiJSONConfiguration();
        WebApiJSONConfiguration webApiJSONConfiguration2 = new WebApiJSONConfiguration();
        webApiJSONConfiguration2.setProperty("key1", "value1");
        webApiJSONConfiguration1.merge(null);
        webApiJSONConfiguration1.merge(webApiJSONConfiguration2);
        assertEquals("value1", webApiJSONConfiguration1.getString("key1"));
    }
}