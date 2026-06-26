package com.nuance.cobra.doc;

import com.nuance.cobra.utils.errors.ErrorCodeDefinition;
import com.nuance.plugin.v1.shareable.IErrorCode;
import org.apache.commons.lang3.text.StrBuilder;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ErrorsDocGeneratorTest {

    static final int ERROR_CODE_VALUE = 12345;
    static final int ERROR_CODE_NO_INTERNAL_MSG_VALUE = 12346;
    static final int ERROR_CODE_NO_STATUS_CODE_VALUE = 12347;
    static final int ERROR_CODE_NO_INTERNAL_MSG_AND_STATUS_CODE_VALUE = 12348;

    static final String ERROR_MESSAGE = "Error message";
    static final String ERROR_INTERNAL_MESSAGE_FORMAT = "Internal error message is '%s' (value: %d)";

    // Common constants
    static final int HTTP_STATUS_CODE_VALUE = 500;
    static final IErrorCode.Severity SEVERITY = IErrorCode.Severity.ERROR;


    enum TestErrorCode implements IErrorCode {

        // General case: message and internal message defined and different from each other.
        // HTTP status code defined.
        GENERAL_ERROR_CODE(ERROR_CODE_VALUE, SEVERITY, ERROR_MESSAGE, ERROR_INTERNAL_MESSAGE_FORMAT, HTTP_STATUS_CODE_VALUE),

        // No internal message
        ERROR_CODE_NO_INTERNAL_MSG(ERROR_CODE_NO_INTERNAL_MSG_VALUE, SEVERITY, ERROR_MESSAGE, HTTP_STATUS_CODE_VALUE),

        // No status code
        ERROR_CODE_NO_STATUS_CODE(ERROR_CODE_NO_STATUS_CODE_VALUE, SEVERITY, ERROR_MESSAGE, ERROR_INTERNAL_MESSAGE_FORMAT),

        // No status code, no internal message
        ERROR_CODE_NO_INTERNAL_MSG_AND_STATUS_CODE(ERROR_CODE_NO_INTERNAL_MSG_AND_STATUS_CODE_VALUE, SEVERITY, ERROR_MESSAGE);


        TestErrorCode (int errorCode, Severity severity, String message, String internalMessageFormat, int statusCode) {
            errorDefinition = new ErrorCodeDefinition(errorCode, severity, message, internalMessageFormat, statusCode);
        }

        TestErrorCode (int errorCode, Severity severity, String message, int statusCode) {
            errorDefinition = new ErrorCodeDefinition(errorCode, severity, message);
        }

        TestErrorCode (int errorCode, Severity severity, String message, String internalMessageFormat) {
            errorDefinition = new ErrorCodeDefinition(errorCode, severity, message, internalMessageFormat);
        }

        TestErrorCode (int errorCode, Severity severity, String message) {
            errorDefinition = new ErrorCodeDefinition(errorCode, severity, message);
        }


        final ErrorCodeDefinition errorDefinition;

        @Override
        public int getValue() {
            return errorDefinition.getValue();
        }

        @Override
        public String getMessage() {
            return errorDefinition.getMessage();
        }

        @Override
        public int getSeverity() {
            return errorDefinition.getSeverity();
        }

        @Override
        public String getInternalErrorMessage() {
            return errorDefinition.getInternalErrorMessage();
        }

        @Override
        public int getHttpStatusCode() {
            return errorDefinition.getHttpStatusCode();
        }

        @Override
        public String getEnumValueName() {
            return name();
        }
    }

    @Test
    public void testErrorCodeDefinitions() throws Exception {

        ErrorsDocGenerator docGenerator = new ErrorsDocGenerator("Test Error Codes",
                "test-error-codes.md",
                new String[] {"Unit Test Error"},
                TestErrorCode.class);

        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), "test-error-codes.md"));
        assertTrue(generatedOutput.contains(Integer.toString(ERROR_CODE_VALUE)));
        assertTrue(generatedOutput.contains(ERROR_MESSAGE));
    }


    @Test
    public void testErrorCode_internal() throws Exception {

        ErrorsDocGenerator docGenerator = new ErrorsDocGenerator("Test Error Codes",
                "test-error-codes.md",
                new String[] {"Unit Test Error"},
                true,
                TestErrorCode.class);

        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), "test-error-codes.md"));
        assertTrue(generatedOutput.contains(Integer.toString(ERROR_CODE_VALUE)));
        assertTrue(generatedOutput.contains(ERROR_MESSAGE));
    }


    @Test
    public void testErrorCodeDefinitionsCsv() throws Exception {

        StrBuilder outputStr = new StrBuilder();
        ErrorsDocGenerator.generateErrorCsv(TestErrorCode.class, '\t', outputStr);

        System.out.println(outputStr);
        assertTrue(outputStr.contains(TestErrorCode.GENERAL_ERROR_CODE.name()));
    }
}