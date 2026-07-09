package com.nuance.cobra.client.response;

import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;

public class SpeechSynthesisResponse extends ResponseResult
{
    public SpeechSynthesisResponse(ResponseStatus status, boolean isFinal)
    {
        super(status, isFinal);
    }

    public SpeechSynthesisResponse(boolean isFinal, ResponseCode... errors) {
        super(isFinal, errors);
    }

    public SpeechSynthesisResponse(ResponseCode... errors)
    {
        super(errors);
    }
}