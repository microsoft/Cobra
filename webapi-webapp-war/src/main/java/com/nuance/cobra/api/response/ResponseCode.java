package com.nuance.cobra.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Arrays;
import java.util.Objects;


public class ResponseCode
{
    private final static String SERVICE_ERROR = "Service error.";
    private final static int NIM_ERROR_CODE_OFFSET = 10000;

    @JsonView(Views.Visible.class)
    private final String message;
    @JsonView(Views.Visible.class)
    private final int code;

    private final int httpEquivalentStatusCode;

    private final int severity;
    private final String internalErrorMessage;

    public static ResponseCode createResponseError(ResponseErrorCode errorCode, String... args)
    {
        // The following line fixes a warning in compilation: 'java: non-varargs call of varargs method
        // with inexact argument type for last parameter;' The casts suggested in the warning don't fix
        // it . This line creates an array of objects that matches the String.format() signature. It
        // works also when the method createResponseError() is called without any args e.g.
        // createResponseError(ResponseError.ResponseErrorCode.END_SESSION_NMAS_RETRY_ERROR)
        // In the latter case, args.length is zero.
        return new ResponseCode(errorCode, args);
    }

    public static ResponseCode createGenericResponse(GenericResponseCode responseCode, String... args)
    {
        return new ResponseCode(responseCode, args);
    }

    public static ResponseCode createResponseErrorForNimService(int code)
    {
        return new ResponseCode(code + NIM_ERROR_CODE_OFFSET, SERVICE_ERROR);
    }

    public static String createResponseErrorString(ResponseErrorCode errorCode)
    {
        String s = "{'type':'result','result':{'final':true,'status':'error','errors':[{'message':'%s','code': %d}]}}";
        return String.format(s, errorCode.getMessage(), errorCode.getValue()).replace("'", "\"");
    }

    public static ResponseErrorCode fromCode(int searchCode)
    {
        for (InternalResponseErrorCode codeIt : InternalResponseErrorCode.values())
        {
            if (codeIt.getValue() == searchCode)
            {
                return codeIt;
            }
        }
        for(ExternalResponseErrorCode codeIt : ExternalResponseErrorCode.values()){
            if(codeIt.getValue() == searchCode){
                return codeIt;
            }
        }
        return null;
    }

    private ResponseCode(int code, String message)
    {
        this.message = message;
        this.code = code;

        ResponseErrorCode crErrCode = fromCode(code);
        if (crErrCode != null)
        {
            this.severity = crErrCode.getSeverity();
            this.internalErrorMessage = crErrCode.getInternalErrorMessage();
            this.httpEquivalentStatusCode = crErrCode.getHttpStatusCode();
        }
        else
        {
            this.severity = 3;
            this.internalErrorMessage = message;
            this.httpEquivalentStatusCode = 0;
        }
    }

    private ResponseCode(ResponseErrorCode errorCode, String... args)
    {
        Object[] array = Arrays.copyOf(args, args.length);
        this.code = errorCode.getValue();

        this.message = String.format(errorCode.getMessage(), array);
        this.severity = errorCode.getSeverity();
        this.internalErrorMessage = String.format(errorCode.getInternalErrorMessage(), array);
        this.httpEquivalentStatusCode = errorCode.getHttpStatusCode();
    }

    private ResponseCode(GenericResponseCode responseCode, String... args)
    {
        this.code = responseCode.getValue();

        this.message = responseCode.getMessage();

        this.severity = 0;
        this.internalErrorMessage = responseCode.getMessage();
        this.httpEquivalentStatusCode = 0;

    }

    public String getMessage() { return message; }

    @JsonIgnore
    public String getInternalMessage() { return internalErrorMessage; }
    
    public int getCode()
    {
        return code;
    }

    public String formattedLogMessage() {
        return "code: " + getCode() + " message: " + getMessage() + " message description: " + getInternalMessage();
    }

    @JsonIgnore
    public int getSeverity()
    {
        return severity;
    }

    @JsonIgnore
    public int getHttpEquivalentStatusCode() {
        return httpEquivalentStatusCode;
    }

    @Override
    public boolean equals(Object object)
    {
        // Self check
        if (this == object)
        {
            return true;
        }

        // Null check
        if (object == null)
        {
            return false;
        }

        // Type check and cast
        if (getClass() != object.getClass())
        {
            return false;
        }

        // Field comparisons
        ResponseCode response = (ResponseCode) object;
        return (this.message.equals(response.message) && this.code == response.code);
    }

    @Override
    public int hashCode(){
        return Objects.hash(message, code, severity, internalErrorMessage);
    }
}