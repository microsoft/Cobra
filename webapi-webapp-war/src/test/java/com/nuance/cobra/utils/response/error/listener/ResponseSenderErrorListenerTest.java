package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.webapi.ResponseSender;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ResponseSenderErrorListenerTest
{
    private final ResponseSender mockResponseSender = mock(ResponseSender.class);

    @Test
    public void notifyTest()
    {
        ResponseSenderErrorListener tester = new ResponseSenderErrorListener(mockResponseSender);
        // Setup
        String expectedJson =
                "{'final':true,'status':'error','errors':[{'message':'StartSession service failed.','code':1302}]}"
                        .replaceAll("'", "\"");
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);

        ResponseResult responseResult = new ResponseResult(responseCode1);
        tester.notify(responseResult);

        verify(mockResponseSender).sendResponse(expectedJson);
    }

}