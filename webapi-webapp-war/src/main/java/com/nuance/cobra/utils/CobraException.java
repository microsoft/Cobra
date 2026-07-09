package com.nuance.cobra.utils;


import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;

public class CobraException extends Exception {
    private final ResponseResult result;

    public CobraException(ResponseCode error)
    {
        result = new ResponseResult(error);
    }

    public CobraException(ResponseCode error, Throwable cause)
    {
        super(cause);
        result = new ResponseResult(error);
    }
    public ResponseResult getResult() {
        return result;
    }
}
