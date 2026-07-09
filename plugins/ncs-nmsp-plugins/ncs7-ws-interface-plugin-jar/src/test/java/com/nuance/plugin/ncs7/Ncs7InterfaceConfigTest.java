package com.nuance.plugin.ncs7;

import com.nuance.cobra.doc.ConfigDocGenerator;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class Ncs7InterfaceConfigTest {


    @Test
    public void generateMarkdownDocs() throws Exception {
        ConfigDocGenerator generator = new ConfigDocGenerator(Ncs7InterfaceConfig.Property.class);
        generator.generateMarkdownDocs();
        assertTrue(Files.exists(Path.of(generator.getOutputPath().toString(), "plugins.md")));
    }
}