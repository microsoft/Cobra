package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IErrorCode;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

public class WebApiCommonExceptionTest {

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
    public void testWebApiCommonException() {

        WebApiCommonException e = new WebApiCommonException(TestErrorCode.ERROR_CODE, DETAILS, 42);

        assertEquals(ERROR_CODE_VALUE, e.getErrorCodeValue());
        assertEquals(HTTP_STATUS_CODE_VALUE, e.getHttpStatusCode());
        assertEquals("Internal error message is 'extra details' (value: 42)", e.getMessage());

        // Exception with a cause (!)
        e = new WebApiCommonException(TestErrorCode.ERROR_CODE, new Exception("cause"), DETAILS, 42);
        assertEquals("cause", e.getCause().getMessage());
    }


}