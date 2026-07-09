package com.nuance.cobra.api.response;

import com.nuance.cobra.doc.ErrorsDocGenerator;
import org.apache.commons.lang3.text.StrBuilder;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ExternalResponseErrorCodeTest {

    @Test
    public void generateResponseCodesDoc() throws Exception {

        final String FILENAME = "api-error-codes.md";

        ErrorsDocGenerator docGenerator = new ErrorsDocGenerator("Web API Error Codes",
                FILENAME,
                new String[] {null},
                ExternalResponseErrorCode.class);

        docGenerator.setRelativeOutputPath("api-guide");
        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), FILENAME));
        System.out.println(generatedOutput);
        assertTrue(generatedOutput.contains("Error"));
    }

    @Test
    public void generateInternalExternalResponseCodesDoc() throws Exception {

        final String FILENAME = "api-error-codes-internal-external.md";

        ErrorsDocGenerator docGenerator = new ErrorsDocGenerator("Web API Error Codes",
                FILENAME,
                new String[] {null},
                true,
                ExternalResponseErrorCode.class);

        docGenerator.setRelativeOutputPath("errors-internal-guide");
        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), FILENAME));
        System.out.println(generatedOutput);
        assertTrue(generatedOutput.contains("Error"));
    }

    @Test
    public void generateInternalInternalResponseCodesDoc() throws Exception {

        final String FILENAME = "api-error-codes-internal-internal.md";

        ErrorsDocGenerator docGenerator = new ErrorsDocGenerator("Internal Web API Error Codes",
                FILENAME,
                new String[] {null},
                true,
                InternalResponseErrorCode.class);

        docGenerator.setRelativeOutputPath("errors-internal-guide");
        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), FILENAME));
        System.out.println(generatedOutput);
        assertTrue(generatedOutput.contains("Error"));
    }

    @Test
    public void generateResponseCodesCsvList() throws Exception {

        StrBuilder outputStr = new StrBuilder();
        ErrorsDocGenerator.generateErrorCsv(ExternalResponseErrorCode.class, '\t', outputStr);

        System.out.println(outputStr);
        assertTrue(outputStr.contains("Error"));
    }

}