package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.response.ResponseHandler;

public class ResponseHandlerErrorListener
        implements ErrorListener
{
    private ResponseHandler localHandler;

    public ResponseHandlerErrorListener(ResponseHandler handler)
    {
        localHandler = handler;
    }

    @Override
    public void notify(IResponse error)
    {
        localHandler.onResponse((ResponseResult) error);
    }
}
