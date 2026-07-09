package com.nuance.cobra.doc;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public  class DocGeneratorTest {

    @Test
    public void setRelativeOutputPathTest() {

        DocGenerator docGenerator = new DocGenerator() {
            @Override
            protected void generateDocs() throws Exception {
            }
        };

        Path outputPath = docGenerator.getOutputPath();

        final String RELATIVE_PATH = "my-relative-path";
        docGenerator.setRelativeOutputPath(RELATIVE_PATH);
        assert(docGenerator.getOutputPath().toString().endsWith(RELATIVE_PATH));
        assert(docGenerator.getOutputPath().toString().startsWith(outputPath.toString()));
    }

}