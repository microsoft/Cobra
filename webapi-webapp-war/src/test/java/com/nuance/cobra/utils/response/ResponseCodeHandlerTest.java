package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.response.error.listener.ErrorListener;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ResponseCodeHandlerTest
{
    @Test
    public void testAddListener()
            throws Exception
    {
        ResponseErrorHandler errorHandler = new ResponseErrorHandler();
        ErrorListener listener = mock(ErrorListener.class);
        errorHandler.addListener(listener);
    }

    @Test
    public void testRemoveListener()
            throws Exception
    {
        ResponseErrorHandler errorHandler = new ResponseErrorHandler();
        ErrorListener listener = mock(ErrorListener.class);
        errorHandler.addListener(listener);
        errorHandler.removeListener(listener);
    }

    @Test
    public void testHandleError()
            throws Exception
    {
        ResponseResult mockResponseRes = mock(ResponseResult.class);
        ResponseErrorHandler errorHandler = new ResponseErrorHandler();
        ErrorListener listener = mock(ErrorListener.class);
        errorHandler.addListener(listener);
        errorHandler.handleError(mockResponseRes);
    }

}