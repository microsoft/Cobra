package com.nuance.plugin.v1.shareable;

import org.junit.Test;

import javax.annotation.Nullable;

import static org.junit.Assert.*;

public class IErrorCodeTest {

    static final String ERROR_MESSAGE = "Error message";
    static final String ERROR_INTERNAL_MESSAGE_FORMAT = "Internal error message is '%s' (value: %d)";
    static final String ERROR_MESSAGE_WITH_PERIOD = "Error message with period.";

    static final int ERROR_CODE_VALUE = 12345;
    static final int ERROR_CODE_NO_INTERNAL_MSG_VALUE = 12346;
    static final int ERROR_CODE_MSG_WITH_PERIOD_VALUE = 12347;


    // Common constants
    static final int HTTP_STATUS_CODE_VALUE = 500;
    static final IErrorCode.Severity SEVERITY = IErrorCode.Severity.ERROR;

    static final String DETAILS = "extra details";



    enum TestErrorCode implements IErrorCode {

        // General case: message and internal message defined and different from each other.
        // HTTP status code defined.
        ERROR_CODE(ERROR_CODE_VALUE, HTTP_STATUS_CODE_VALUE, ERROR_MESSAGE, ERROR_INTERNAL_MESSAGE_FORMAT),

        // Internal message format not specified.
        ERROR_CODE_NO_INTERNAL_MSG(ERROR_CODE_NO_INTERNAL_MSG_VALUE, HTTP_STATUS_CODE_VALUE, ERROR_MESSAGE, null),

        // No internal message, error message with period.
        ERROR_CODE_WITH_PERIOD_MSG(ERROR_CODE_MSG_WITH_PERIOD_VALUE, HTTP_STATUS_CODE_VALUE,  ERROR_MESSAGE_WITH_PERIOD, null);


        TestErrorCode(int errorCodeValue, int httpStatusCode, String message, @Nullable String internalMessageFormat) {
            this.errorCodeValue = errorCodeValue;
            this.httpStatusCode = httpStatusCode;
            this.message = message;
            this.internalMessageFormat = internalMessageFormat;
        }

        final int errorCodeValue;
        final int httpStatusCode;
        final String message;
        final String internalMessageFormat;

        @Override
        public int getValue() {
            return errorCodeValue;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public int getSeverity() {
            return 0;
        }

        @Override
        public String getInternalErrorMessage() {
            return internalMessageFormat;
        }

        @Override
        public int getHttpStatusCode() {
            return httpStatusCode;
        }
    }


    @Test
    public void errorCodeTest() {
        IErrorCode testErrorCode = new IErrorCode() {
            @Override
            public int getValue() {
                return 0;
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public int getSeverity() {
                return 0;
            }

            @Override
            public String getInternalErrorMessage() {
                return null;
            }
        };

        // verify default value
        assertEquals(0, testErrorCode.getHttpStatusCode());
        assertEquals(false, testErrorCode.isPotentialSecurityThreat());
        assertNull(testErrorCode.getDocumentation());
        assertNull(testErrorCode.getNOCRecommendation());
        assertNull(testErrorCode.getTroubleshoot());
        assertNull(testErrorCode.getVersion());
        assertNull(testErrorCode.getAreaOfIssue());
    }





    @Test
    public void testFormatInternalMessage() {

        // format with expected arguments
        assertEquals("Internal error message is 'extra details' (value: 42)",
                IErrorCode.formatInternalMessage(TestErrorCode.ERROR_CODE, DETAILS, 42));

        // format with extraneous arguments
        assertEquals("Internal error message is 'extra details' (value: 42)",
                IErrorCode.formatInternalMessage(TestErrorCode.ERROR_CODE, DETAILS, 42, 66));

        // missing one argument to format the message. Defaulting to external message + print warning.
        String formattedMessage = IErrorCode.formatInternalMessage(
                TestErrorCode.ERROR_CODE, DETAILS);
        assertTrue(formattedMessage.contains(ERROR_MESSAGE));

        // No internal error message format, no args passed passed will default to base error message
        assertEquals(ERROR_MESSAGE,
                IErrorCode.formatInternalMessage(TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG));

        // No internal error message format, 1 arg passed passed, will generate error message including the
        // details
        assertEquals("Error message: extra details",
                IErrorCode.formatInternalMessage(
                        TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG, DETAILS));

        // No internal error message format, 2 arg passed passed, will generate error message
        assertEquals("Error message: extra details",
                IErrorCode.formatInternalMessage(
                        TestErrorCode.ERROR_CODE_NO_INTERNAL_MSG, DETAILS, 23));

        // error code without an internal message, no arguments, defaults to the internal message.
        assertEquals(ERROR_MESSAGE_WITH_PERIOD,
                IErrorCode.formatInternalMessage(
                        TestErrorCode.ERROR_CODE_WITH_PERIOD_MSG));

        // Automatically generated format for errror without internal message. 1 arguments passed (modified external
        // message, with period replaced by :)
        assertEquals("Error message with period: extra details",
                IErrorCode.formatInternalMessage(
                        TestErrorCode.ERROR_CODE_WITH_PERIOD_MSG, DETAILS));
    }



}