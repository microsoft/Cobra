package com.nuance.cobra.session;

import com.nuance.cobra.doc.ConfigDocGenerator;
import org.apache.commons.lang3.text.StrBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({ WebApiConfigManager.class })
public class WebApiConfigTest
{
    @Test
    public void generateMarkdownDocs() throws Exception {
        ConfigDocGenerator generator = new ConfigDocGenerator(WebApiConfig.Property.class);
        generator.generateMarkdownDocs();
    }

    @Test
    public void generateParameterList()
            throws Exception
    {
        StrBuilder outputStr = new StrBuilder();

        for (WebApiConfig.Property property : WebApiConfig.Property.values()) {
            outputStr.append(property.key).appendNewLine();
        }


        Path outputPath = Paths.get(System.getProperty("user.dir") + "/target/temp/generated");
        Files.createDirectories(outputPath);

        File file = new File(outputPath + "/ParamList.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
        {
            writer.write(outputStr.toString());
        }
    }

    @Test
    public void checkForDuplicateKeys()
    {
        Map<String, WebApiConfig.Property> propertiesByKey = new HashMap<>();

        for (WebApiConfig.Property property : WebApiConfig.Property.values()) {
            if (propertiesByKey.containsKey(property.key)) {
                fail("Duplicate key detected: \"" + property.key + "\", properties " + property + " and " +
                        propertiesByKey.get(property.key));
            }

            propertiesByKey.put(property.key, property);
        }
    }

}