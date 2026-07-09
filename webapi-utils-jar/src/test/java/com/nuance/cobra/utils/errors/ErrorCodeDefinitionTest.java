package com.nuance.cobra.utils.errors;

import com.nuance.plugin.v1.shareable.IErrorCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ErrorCodeDefinitionTest {


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
    }

    @Test
    public void testErrorCodeDefinitions() {
        assertEquals(ERROR_CODE_VALUE, TestErrorCode.GENERAL_ERROR_CODE.getValue());
        assertEquals(SEVERITY.getIntValue(), TestErrorCode.GENERAL_ERROR_CODE.getSeverity());
        assertEquals(ERROR_MESSAGE, TestErrorCode.GENERAL_ERROR_CODE.getMessage());
        assertEquals(ERROR_INTERNAL_MESSAGE_FORMAT, TestErrorCode.GENERAL_ERROR_CODE.getInternalErrorMessage());
        assertEquals(HTTP_STATUS_CODE_VALUE, TestErrorCode.GENERAL_ERROR_CODE.getHttpStatusCode());

        assertEquals(ERROR_CODE_NO_INTERNAL_MSG_VALUE, TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG.getValue());
        assertNull(TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG.getInternalErrorMessage());

        assertEquals(ERROR_CODE_NO_STATUS_CODE_VALUE, TestErrorCode.ERROR_CODE_NO_STATUS_CODE.getValue());
        assertEquals(0, TestErrorCode.ERROR_CODE_NO_STATUS_CODE.getHttpStatusCode());
        assertEquals(ERROR_INTERNAL_MESSAGE_FORMAT, TestErrorCode.ERROR_CODE_NO_STATUS_CODE.getInternalErrorMessage());

        assertEquals(ERROR_CODE_NO_INTERNAL_MSG_AND_STATUS_CODE_VALUE,
                TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG_AND_STATUS_CODE.getValue());
        assertEquals(0, TestErrorCode.ERROR_CODE_NO_STATUS_CODE.getHttpStatusCode());
        assertEquals(ERROR_INTERNAL_MESSAGE_FORMAT, TestErrorCode.ERROR_CODE_NO_STATUS_CODE.getInternalErrorMessage());
    }
}