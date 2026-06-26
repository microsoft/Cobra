package com.nuance.plugin.nmsp.v1;

import com.nuance.cobra.doc.ConfigDocGenerator;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdxInterfaceConfigTest  {


    @Test
    public void generateMarkdownDocs() throws Exception {
        ConfigDocGenerator generator = new ConfigDocGenerator(PdxInterfaceConfig.Property.class);
        generator.generateMarkdownDocs();
        assertTrue(Files.exists(Path.of(generator.getOutputPath().toString(), "plugins.md")));
    }

    @Test
    public void getPluginConfMapNotFoung() {
        CombinedConfiguration mockConfig = PowerMockito.mock(CombinedConfiguration.class);
        PowerMockito.when(mockConfig.configurationAt("ncsNmspRawServer")).thenReturn(null);

        BaseHierarchicalConfiguration config = PdxInterfaceConfig.getPluginConfMap(mockConfig);
        assertEquals(8911, config.getInt(PdxInterfaceConfig.Property.SERVER_PORT.getKey()));
        assertTrue(config.getBoolean(PdxInterfaceConfig.Property.SSL_ENABLED.getKey()));
    }

    @Test
    public void getPluginConfMapConfigException() {
        CombinedConfiguration mockConfig = PowerMockito.mock(CombinedConfiguration.class);
        PowerMockito.when(mockConfig.configurationAt("ncsNmspRawServer")).thenThrow(ConfigurationRuntimeException.class);

        BaseHierarchicalConfiguration config = PdxInterfaceConfig.getPluginConfMap(mockConfig);
        assertEquals(8911, config.getInt(PdxInterfaceConfig.Property.SERVER_PORT.getKey()));
        assertTrue(config.getBoolean(PdxInterfaceConfig.Property.SSL_ENABLED.getKey()));
    }
}