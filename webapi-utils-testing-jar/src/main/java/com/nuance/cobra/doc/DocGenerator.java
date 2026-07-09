package com.nuance.cobra.doc;

import org.apache.commons.lang3.text.StrBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class DocGenerator {

    public static final Path DEFAULT_OUTPUT_PATH = Paths.get(System.getProperty("user.dir") + "/target/temp/generated");

    private Path outputPath = DEFAULT_OUTPUT_PATH;


    /**
     * Returns the location where the generated files will be stored
     * @return Generated file location
     */
    public Path getOutputPath() {
        return outputPath;
    }

    /**
     * Sets the output path where the generated files will be stored, relative to the default location
     * @param relPath
     */
    public void setRelativeOutputPath(String relPath) {
        outputPath = Paths.get(DEFAULT_OUTPUT_PATH.toString() + "/" + relPath);
    }

    public void generateMarkdownDocs() throws Exception {
        generateDocs();
    }

    protected abstract void generateDocs() throws Exception;

    protected void generateMarkdownHeader (String apiGroup, StrBuilder outputStr) {
        outputStr.clear();
        outputStr.append("# ").append(apiGroup).appendNewLine();
    }

    protected void generateConfigSectionHeader(String group, String apiName, String description, StrBuilder
            outputStr)
    {
        outputStr.appendNewLine().append("## ").append(apiName).appendNewLine();
        outputStr.appendNewLine().append(description).appendNewLine();
        outputStr.appendNewLine().append("### Parameter").appendNewLine();
        outputStr.append("| Name     | Type       | Description                           |").appendNewLine();
        outputStr.append("|----------|------------|---------------------------------------|").appendNewLine();
    }


    protected void createMarkdownFile(String fileName, StrBuilder outputStr) throws IOException {
        Files.createDirectories(outputPath);

        File file = new File(outputPath + "/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(outputStr.toString());
        }
    }
}
